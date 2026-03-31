package com.ricardomodino.impostorgame.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.PlayerImageManager
import com.ricardomodino.impostorgame.managers.SoundManager
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador
import com.ricardomodino.impostorgame.modelos.WordItem
import com.ricardomodino.impostorgame.usecases.AssignRolesUseCase
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel
import com.ricardomodino.impostorgame.viewmodel.PlayerViewModel
import java.io.File
import kotlin.random.Random

/**
 * Activity base para todas las pantallas de reveal.
 * Centraliza la logica de juego: asignacion de roles, seleccion de contenido,
 * camara, victoria inmediata y navegacion.
 *
 * Las subclases implementan la presentacion visual mediante los metodos abstractos.
 *
 * Jerarquia:
 *   BaseRevealActivity (abstracta) : BaseGameActivity
 *   ├── ClassicRevealActivity  (estilos clasico, carmesi, JMC)
 *   └── CoverRevealActivity    (estilos final y datos curiosos)
 */
abstract class BaseRevealActivity : BaseGameActivity() {

    // ── ViewModels ──────────────────────────────────────────────────────────
    protected val playerViewModel: PlayerViewModel by viewModels()
    protected val categoryViewModel: CategoryViewModel by viewModels()
    protected val datosViewModel: DatosCuriososViewModel by viewModels()

    // ── Estado de juego ─────────────────────────────────────────────────────
    protected lateinit var listaJugadores: List<Jugador>
    protected lateinit var listaCategorias: List<Category>
    protected lateinit var opciones: GameOptions
    protected lateinit var indicesImpostores: Set<Int>
    protected lateinit var indicesSenoresBlancos: Set<Int>

    // Datos de la ronda
    protected var datosAsignados: Map<Int, DatoCurioso> = emptyMap()
    protected var palabra: String = ""
    protected var pista: String = ""
    protected var pistaMisteriosa: String = ""

    // Progreso de jugadores
    protected var playerInGame: Int = 0
    protected lateinit var imagenPorJugador: Array<Bitmap?>
    protected var imageResTurno: Bitmap? = null
    protected var modoLocoActivo: Boolean = false
    protected var pistaActivaModoLoco: Boolean = false

    // Categoria y palabra activa (modo no-datos)
    protected lateinit var categoriaInGame: Category
    protected lateinit var wordItemInGame: WordItem
    protected lateinit var nombresImpostores: List<String>

    // Estado de cubierta/selfie
    protected var cubiertaRevelada: Boolean = false
    protected val selfiesTomados = mutableSetOf<Int>()

    // CameraX
    protected var cameraProvider: ProcessCameraProvider? = null
    protected var imageCapture: ImageCapture? = null

    private val assignRolesUseCase = AssignRolesUseCase()

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) iniciarCameraX()
    }

    // ── Metodos abstractos que implementan las subclases ────────────────────

    /** Devuelve el ID del layout a inflar. */
    protected abstract fun provideLayoutRes(): Int

    /** Vista que recibe el OnTouchListener de revelacion. */
    protected abstract val touchTarget: View

    /** Vista del boton "Siguiente jugador". */
    protected abstract val btnSiguiente: View

    /** TextView interno del boton siguiente (para cambiar su texto). */
    protected abstract val txtBtnSiguiente: TextView

    /** Bindea las vistas propias de la subclase tras setContentView(). */
    protected abstract fun onBindViews()

    /** Actualiza la UI para mostrar los datos del jugador en la posicion [index]. */
    protected abstract fun onShowPlayer(index: Int)

    /** Muestra el contenido (rol/palabra/dato) al recibir ACTION_DOWN. */
    protected abstract fun onRevealContent(index: Int)

    /** Oculta el contenido al recibir ACTION_UP/CANCEL. */
    protected abstract fun onHideContent()

    /**
     * Ejecuta la animacion de transicion al siguiente jugador.
     * Cuando se llama, playerInGame ya ha sido incrementado.
     * El parametro onSwap es un gancho opcional; las subclases pueden ignorarlo.
     */
    protected abstract fun onPlayerTransition(onSwap: () -> Unit)

    // ── Punto de extension opcional post-setup ──────────────────────────────
    /** Llamado al final de onCreate(), permite a la subclase ejecutar logica de entrada. */
    protected open fun onPostSetup() {}

    /** Instala el listener de interaccion en [touchTarget]. Las subclases pueden sobrescribir. */
    protected open fun configurarInteraccion() {}

    // ── onCreate orquestador ────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(provideLayoutRes())

        onBindViews()

        listaJugadores  = intent.getParcelableArrayListExtra<Jugador>(IntentKeys.PLAYERS)?.toList() ?: emptyList()
        listaCategorias = intent.getParcelableArrayListExtra<Category>(IntentKeys.CATEGORIES)?.toList() ?: emptyList()
        opciones        = intent.getParcelableExtra(IntentKeys.OPCIONES) ?: GameOptions()

        if (listaJugadores.isEmpty()) { finish(); return }

        configurarBackPressed()
        inicializarJuego()

        SelfieManager.init(cacheDir)

        if (opciones.camaraActiva) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                iniciarCameraX()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        configurarInteraccion()
        onShowPlayer(playerInGame)
        onPostSetup()
    }

    // ── Inicializacion del juego ────────────────────────────────────────────

    /** Asigna roles, imagenes y selecciona contenido para la partida. */
    protected open fun inicializarJuego() {
        // Asignar roles mediante el use case
        val roles = assignRolesUseCase.execute(
            listaJugadores, opciones, playerViewModel::pickImpostorIndices
        )
        listaJugadores        = roles.jugadores
        indicesImpostores     = roles.indicesImpostores
        indicesSenoresBlancos = roles.indicesSenoresBlancos
        nombresImpostores     = roles.nombresImpostores

        // Asignar imagenes desde assets
        val playerImages = PlayerImageManager.getShuffledPool(this, listaJugadores.size)
        imagenPorJugador = Array(listaJugadores.size) {
            if (playerImages.isNotEmpty()) playerImages[it] else null
        }
        imageResTurno = PlayerImageManager.getRandom(this)

        seleccionarContenido()
        modoLocoActivo = random(10)

        verificarVictoriaInmediata()
    }

    /** Selecciona palabra o datos curiosos segun el modo de juego. */
    protected open fun seleccionarContenido() {
        if (opciones.modoDatosCuriosos) {
            palabra = ""; pista = ""; pistaMisteriosa = ""
            asignarDatos()
        } else {
            seleccionarPalabra()
        }
    }

    /** Elige categoria y palabra, inicializa categoryViewModel si hace falta. */
    protected open fun seleccionarPalabra() {
        if (listaCategorias.isEmpty()) return
        categoryViewModel.setCategories(listaCategorias)

        val disponibles = listaCategorias
            .filter { it.isSelected && it.items.isNotEmpty() }
            .ifEmpty { listaCategorias.filter { it.items.isNotEmpty() } }
            .ifEmpty { listaCategorias }

        val categoria = disponibles.randomOrNull() ?: return

        val catActual = if (categoryViewModel.itemsVacio(categoria.id)) {
            categoryViewModel.restoreItems(categoria.id)
            categoryViewModel.categories.value?.firstOrNull { it.id == categoria.id } ?: categoria
        } else categoria

        categoriaInGame = catActual
        val items = catActual.items.filter { it.name != "Impostor" }
        val item  = if (items.isNotEmpty()) items.random() else catActual.items.random()
        wordItemInGame  = item
        palabra         = item.name
        pista           = item.hints.randomOrNull() ?: ""
        pistaMisteriosa = pista
    }

    /** Asigna un DatoCurioso a cada civil. */
    protected open fun asignarDatos() {
        val todos = datosViewModel.getCategoriasActivas().flatMap { it.datos }.shuffled().toMutableList()
        val mapa  = mutableMapOf<Int, DatoCurioso>()
        var idx   = 0
        for (i in listaJugadores.indices) {
            if (i !in indicesImpostores && listaJugadores[i].tipo != TipoJugador.SENOR_BLANCO) {
                if (idx < todos.size) mapa[i] = todos[idx++]
            }
        }
        datosAsignados = mapa
    }

    /** Construye la lista de datos para pasar a PlayGameActivity. */
    protected fun datosPartida(): ArrayList<DatoCurioso> =
        ArrayList(listaJugadores.indices.mapNotNull { datosAsignados[it] })

    /** Devuelve el texto del dato en el idioma activo. */
    protected fun getTextoDato(dato: DatoCurioso): String = when (LocaleManager.getLanguage(this)) {
        "en"      -> dato.en
        "zh-Hans" -> dato.zhHans
        "zh-Hant" -> dato.zhHant
        else      -> dato.es
    }

    // ── Victoria inmediata ──────────────────────────────────────────────────

    protected fun verificarVictoriaInmediata() {
        val noCiviles = indicesImpostores.size + indicesSenoresBlancos.size
        val civiles   = listaJugadores.size - noCiviles
        if (noCiviles >= civiles) mostrarVictoriaInmediata()
    }

    private fun mostrarVictoriaInmediata() {
        GameDialog(this)
            .icon("\u26A0\uFE0F")
            .title(getString(R.string.reveal_roles_unbalanced_title))
            .message(getString(R.string.reveal_roles_unbalanced_msg))
            .cancelable(false)
            .positiveButton(getString(R.string.dialog_view_result)) { irAVictoria() }
            .show()
    }

    private fun irAVictoria() {
        val impostorNombres = nombresImpostores.joinToString(", ")
        val senoresBlancos  = indicesSenoresBlancos.map { listaJugadores[it].nombre }.joinToString(", ")

        startActivity(Intent(this, VictoryActivity::class.java).apply {
            putExtra(IntentKeys.GANADOR, "IMPOSTORES")
            putExtra(IntentKeys.MOTIVO, getString(R.string.reveal_victory_no_civilians_reason))
            putExtra(IntentKeys.IR_A_REVEAL, true)
            putParcelableArrayListExtra(IntentKeys.LISTA_JUGADORES, ArrayList(listaJugadores))
            putParcelableArrayListExtra(IntentKeys.LISTA_CATEGORIAS, ArrayList(listaCategorias))
            putExtra(IntentKeys.PALABRA, palabra)
            putExtra(IntentKeys.IMPOSTOR, impostorNombres)
            putExtra(IntentKeys.SENORES_BLANCOS, senoresBlancos)
            putExtra(IntentKeys.MODO_MISTERIOSO, opciones.modoMisterioso)
            putExtra(IntentKeys.TIEMPO_LIMITADO, opciones.tiempoLimitado)
            putExtra(IntentKeys.MINUTOS, opciones.minutos)
        })
        finish()
    }

    // ── Avance de jugadores ─────────────────────────────────────────────────

    /**
     * Avanza al siguiente jugador o lanza PlayGameActivity si era el ultimo.
     * Llamado por las subclases desde el listener de btnSiguiente.
     *
     * playerInGame se incrementa ANTES de llamar a onPlayerTransition para que
     * las subclases puedan acceder al nuevo indice durante la animacion de transicion.
     * El parametro onSwap se usa como gancho para el momento exacto del intercambio visual.
     */
    protected fun avanzarJugador() {
        if (playerInGame == listaJugadores.lastIndex) {
            irAPartida()
            return
        }

        pistaActivaModoLoco = false
        playerInGame++

        onPlayerTransition {}
    }

    /** Construye el Intent completo hacia PlayGameActivity y lanza la partida. */
    protected fun irAPartida() {
        val impostorNombres = if (opciones.modoLoco && modoLocoActivo)
            "TODOS SOIS IMPOSTORES"
        else
            nombresImpostores.joinToString(", ")

        val senoresBlancos = indicesSenoresBlancos.map { listaJugadores[it].nombre }.joinToString(", ")

        val jugadoresConRoles = listaJugadores.mapIndexed { i, j ->
            when {
                i in indicesImpostores             -> j.copy(tipo = TipoJugador.IMPOSTOR)
                j.tipo == TipoJugador.SENOR_BLANCO -> j
                else                               -> j.copy(tipo = TipoJugador.NORMAL)
            }
        }

        startActivity(Intent(this, PlayGameActivity::class.java).apply {
            putParcelableArrayListExtra(IntentKeys.LISTA_JUGADORES, ArrayList(jugadoresConRoles))
            putExtra(IntentKeys.JUGADOR_EMPIEZA, playerViewModel.pickJugadorQueEmpieza(jugadoresConRoles)?.nombre ?: "")
            putParcelableArrayListExtra(IntentKeys.LISTA_CATEGORIAS, ArrayList(listaCategorias))
            putParcelableArrayListExtra(IntentKeys.DATOS_PARTIDA, datosPartida())
            putExtra(IntentKeys.PALABRA, when {
                opciones.modoDatosCuriosos          -> ""
                opciones.modoLoco && modoLocoActivo -> getString(R.string.play_no_word_value)
                else                                -> palabra
            })
            putExtra(IntentKeys.IMPOSTOR, impostorNombres)
            putExtra(IntentKeys.SENORES_BLANCOS, senoresBlancos)
            putExtra(IntentKeys.MODO_MISTERIOSO, opciones.modoMisterioso)
            putExtra(IntentKeys.MODO_DATOS_CURIOSOS, opciones.modoDatosCuriosos)
            putExtra(IntentKeys.TIEMPO_LIMITADO, opciones.tiempoLimitado)
            putExtra(IntentKeys.MINUTOS, opciones.minutos)
        })
        finishWithUpdatedCategories()
    }

    protected fun finishWithUpdatedCategories() {
        setResult(RESULT_OK, Intent().apply {
            putParcelableArrayListExtra(
                "UPDATED_CATEGORIES",
                ArrayList(categoryViewModel.categories.value ?: emptyList())
            )
        })
        finish()
    }

    // ── Texto de ayuda al impostor ──────────────────────────────────────────
    protected fun textoAyudaImpostor(): String = when (opciones.tipoPista) {
        GameOptions.PRIMERA_LETRA -> getString(
            R.string.reveal_first_letter_prefix,
            palabra.firstOrNull()?.uppercase() ?: ""
        )
        else -> if (pista.isNotEmpty()) getString(R.string.reveal_hint_prefix, pista) else ""
    }

    // ── CameraX ─────────────────────────────────────────────────────────────
    protected fun iniciarCameraX() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                cameraProvider = future.get()
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, CameraSelector.DEFAULT_FRONT_CAMERA, imageCapture!!
                )
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(this))
    }

    protected fun tomarSelfie(playerIndex: Int) {
        val ic = imageCapture ?: return
        val outFile = File(cacheDir, "selfie_reveal_$playerIndex.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(outFile).build()
        ic.takePicture(options, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val raw = BitmapFactory.decodeFile(outFile.absolutePath) ?: return
                    val bmp = corregirOrientacion(raw, outFile)
                    SelfieManager.saveBitmap(listaJugadores[playerIndex].nombre, bmp)
                }
                override fun onError(e: ImageCaptureException) {}
            }
        )
    }

    protected fun corregirOrientacion(bmp: Bitmap, file: File): Bitmap {
        val orientation = try {
            ExifInterface(file.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (_: Exception) { ExifInterface.ORIENTATION_NORMAL }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90  -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL   -> { matrix.postRotate(180f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSPOSE       -> { matrix.postRotate(90f);  matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE      -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
            else -> return bmp
        }
        return try {
            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        } catch (_: Exception) { bmp }
    }

    // ── Utilidades ──────────────────────────────────────────────────────────
    protected fun random(num: Int): Boolean = Random.nextInt(100) < num

    protected fun playRevealTone() = SoundManager.playRevealTone(this)


    // ── Ciclo de vida ────────────────────────────────────────────────────────
    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
    }
}
