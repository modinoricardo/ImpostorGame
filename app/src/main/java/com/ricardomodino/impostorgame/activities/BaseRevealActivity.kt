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
import com.ricardomodino.impostorgame.managers.*
import com.ricardomodino.impostorgame.modelos.*
import com.ricardomodino.impostorgame.usecases.AssignRolesUseCase
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel
import com.ricardomodino.impostorgame.viewmodel.PlayerViewModel
import com.ricardomodino.impostorgame.viewmodel.RevealViewModel
import java.io.File
import kotlin.random.Random

/**
 * Activity base para todas las pantallas de reveal.
 * Centraliza la logica de juego: asignacion de roles, seleccion de contenido,
 * camara, victoria inmediata y navegacion.
 */
abstract class BaseRevealActivity : BaseGameActivity() {

    // ── ViewModels ──────────────────────────────────────────────────────────
    protected val playerViewModel: PlayerViewModel by viewModels()
    protected val categoryViewModel: CategoryViewModel by viewModels()
    protected val datosViewModel: DatosCuriososViewModel by viewModels()
    private val revealViewModel: RevealViewModel by viewModels()

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
    protected abstract fun provideLayoutRes(): Int
    protected abstract val touchTarget: View
    protected abstract val btnSiguiente: View
    protected abstract val txtBtnSiguiente: TextView
    protected abstract fun onBindViews()
    protected abstract fun onShowPlayer(index: Int)
    protected abstract fun onRevealContent(index: Int)
    protected abstract fun onHideContent()
    protected abstract fun onPlayerTransition(onSwap: () -> Unit)

    protected open fun onPostSetup() {}
    protected open fun configurarInteraccion() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(provideLayoutRes())

        onBindViews()

        if (!revealViewModel.initialized) {
            listaJugadores  = intent.getParcelableArrayListExtra<Jugador>(IntentKeys.PLAYERS)?.toList() ?: emptyList()
            listaCategorias = intent.getParcelableArrayListExtra<Category>(IntentKeys.CATEGORIES)?.toList() ?: emptyList()
            opciones        = intent.getParcelableExtra(IntentKeys.OPCIONES) ?: GameOptions()

            if (listaJugadores.isEmpty()) { finish(); return }

            inicializarJuego()
            guardarEstadoEnViewModel()
            revealViewModel.initialized = true
        } else {
            restaurarEstadoDesdeViewModel()
            if (listaJugadores.isEmpty()) { finish(); return }
            verificarVictoriaInmediata()
        }

        savedInstanceState?.let { bundle ->
            playerInGame        = bundle.getInt(KEY_PLAYER_IN_GAME, playerInGame)
            palabra             = bundle.getString(KEY_PALABRA, palabra) ?: palabra
            pista               = bundle.getString(KEY_PISTA, pista) ?: pista
            pistaActivaModoLoco = bundle.getBoolean(KEY_PISTA_MODO_LOCO, pistaActivaModoLoco)
            bundle.getIntArray(KEY_SELFIES_TOMADOS)?.let { arr ->
                selfiesTomados.clear()
                selfiesTomados.addAll(arr.toList())
            }
        }

        // Si es modo datos curiosos, los datos pueden no estar cargados aún (coroutine async).
        // Observamos el LiveData y reasignamos cuando lleguen.
        if (opciones.modoDatosCuriosos) {
            datosViewModel.categorias.observe(this) { cats ->
                if (cats.isNotEmpty() && datosAsignados.isEmpty()) {
                    asignarDatos()
                    guardarEstadoEnViewModel()
                    onShowPlayer(playerInGame)
                }
            }
        }

        configurarBackPressed()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PLAYER_IN_GAME, playerInGame)
        outState.putString(KEY_PALABRA, palabra)
        outState.putString(KEY_PISTA, pista)
        outState.putBoolean(KEY_PISTA_MODO_LOCO, pistaActivaModoLoco)
        outState.putIntArray(KEY_SELFIES_TOMADOS, selfiesTomados.toIntArray())
    }

    private fun guardarEstadoEnViewModel() {
        revealViewModel.listaJugadores        = listaJugadores
        revealViewModel.listaCategorias       = listaCategorias
        revealViewModel.opciones              = opciones
        revealViewModel.indicesImpostores     = indicesImpostores
        revealViewModel.indicesSenoresBlancos = indicesSenoresBlancos
        revealViewModel.nombresImpostores     = nombresImpostores
        revealViewModel.datosAsignados        = datosAsignados
        revealViewModel.palabra               = palabra
        revealViewModel.pista                 = pista
        revealViewModel.pistaMisteriosa       = pistaMisteriosa
        if (this::categoriaInGame.isInitialized) revealViewModel.categoriaInGame = categoriaInGame
        if (this::wordItemInGame.isInitialized)  revealViewModel.wordItemInGame  = wordItemInGame
        revealViewModel.modoLocoActivo        = modoLocoActivo
        revealViewModel.imagenPorJugador      = imagenPorJugador
        revealViewModel.imageResTurno         = imageResTurno
    }

    private fun restaurarEstadoDesdeViewModel() {
        listaJugadores        = revealViewModel.listaJugadores
        listaCategorias       = revealViewModel.listaCategorias
        opciones              = revealViewModel.opciones
        indicesImpostores     = revealViewModel.indicesImpostores
        indicesSenoresBlancos = revealViewModel.indicesSenoresBlancos
        nombresImpostores     = revealViewModel.nombresImpostores
        datosAsignados        = revealViewModel.datosAsignados
        palabra               = revealViewModel.palabra
        pista                 = revealViewModel.pista
        pistaMisteriosa       = revealViewModel.pistaMisteriosa
        revealViewModel.categoriaInGame?.let { categoriaInGame = it }
        revealViewModel.wordItemInGame?.let  { wordItemInGame  = it }
        modoLocoActivo        = revealViewModel.modoLocoActivo
        imagenPorJugador      = revealViewModel.imagenPorJugador
        imageResTurno         = revealViewModel.imageResTurno
    }

    protected open fun inicializarJuego() {
        val roles = assignRolesUseCase.execute(
            listaJugadores, opciones, playerViewModel::pickImpostorIndices
        )
        listaJugadores        = roles.jugadores
        indicesImpostores     = roles.indicesImpostores
        indicesSenoresBlancos = roles.indicesSenoresBlancos
        nombresImpostores     = roles.nombresImpostores

        val playerImages = PlayerImageManager.getShuffledPool(this, listaJugadores.size)
        imagenPorJugador = Array(listaJugadores.size) {
            if (playerImages.isNotEmpty()) playerImages[it] else null
        }
        // Pre-asignar la misma imagen al fallbackCache por nombre,
        // para que VoteActivity muestre la misma imagen que el reveal
        listaJugadores.forEachIndexed { i, jugador ->
            imagenPorJugador[i]?.let { PlayerImageManager.preAssign(jugador.nombre, it) }
        }
        imageResTurno = PlayerImageManager.getRandom(this)

        seleccionarContenido()
        modoLocoActivo = random(10)
        verificarVictoriaInmediata()
    }

    protected open fun seleccionarContenido() {
        if (opciones.modoDatosCuriosos) {
            palabra = ""; pista = ""; pistaMisteriosa = ""
            asignarDatos()
        } else {
            seleccionarPalabra()
        }
    }

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

    protected fun datosPartida(): ArrayList<DatoCurioso> =
        ArrayList(listaJugadores.indices.mapNotNull { datosAsignados[it] })

    protected fun getTextoDato(dato: DatoCurioso): String = when (LocaleManager.getLanguage(this)) {
        "en"      -> dato.en
        "zh-Hans" -> dato.zhHans
        "zh-Hant" -> dato.zhHant
        else      -> dato.es
    }

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

    protected fun avanzarJugador() {
        if (playerInGame == listaJugadores.lastIndex) {
            irAPartida()
            return
        }
        pistaActivaModoLoco = false
        playerInGame++
        onPlayerTransition {}
    }

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

    protected fun textoAyudaImpostor(): String = when (opciones.tipoPista) {
        GameOptions.PRIMERA_LETRA -> getString(
            R.string.reveal_first_letter_prefix,
            buildFirstLetterMask(palabra)
        )
        else -> if (pista.isNotEmpty()) getString(R.string.reveal_hint_prefix, pista) else ""
    }

    private fun buildFirstLetterMask(palabra: String): String {
        if (palabra.isEmpty()) return ""
        val sb = StringBuilder()
        var esPrimeraLetra = true
        for (char in palabra) {
            when {
                char == ' ' -> { sb.append(' '); esPrimeraLetra = false }
                esPrimeraLetra -> { sb.append(char.uppercaseChar()); esPrimeraLetra = false }
                else -> sb.append('_')
            }
        }
        return sb.toString()
    }

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
                    onSelfieGuardado(playerIndex, bmp)
                }
                override fun onError(e: ImageCaptureException) {}
            }
        )
    }

    protected open fun onSelfieGuardado(playerIndex: Int, bmp: Bitmap) {}

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

    protected fun random(num: Int): Boolean = Random.nextInt(100) < num
    protected fun playRevealTone() = SoundManager.playRevealTone(this)

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
    }

    companion object {
        private const val KEY_PLAYER_IN_GAME  = "reveal_player_in_game"
        private const val KEY_PALABRA         = "reveal_palabra"
        private const val KEY_PISTA           = "reveal_pista"
        private const val KEY_PISTA_MODO_LOCO = "reveal_pista_modo_loco"
        private const val KEY_SELFIES_TOMADOS = "reveal_selfies_tomados"
    }
}
