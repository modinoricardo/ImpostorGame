package com.ricardomodino.impostorgame.activities

import android.Manifest
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.graphics.Color
import android.view.ViewGroup
import com.ricardomodino.impostorgame.CategoryViewModel
import com.ricardomodino.impostorgame.PlayerViewModel
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador
import com.ricardomodino.impostorgame.modelos.WordItem
import java.io.File
import kotlin.random.Random

@Suppress("DEPRECATION")
class ImpostorRevealActivity : AppCompatActivity() {

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    private lateinit var listaCategorias: List<Category>
    private lateinit var detailsPlayer: TextView
    private lateinit var textNextPlayer: TextView
    private lateinit var layout: LinearLayout
    private lateinit var cardViewPrincipal: CardView
    private lateinit var imgDedo: ImageView
    private lateinit var txtTwo: TextView
    private lateinit var nenxtPlayer: CardView
    private lateinit var presText: TextView
    private lateinit var turnPlayerName: TextView
    private lateinit var hintPlayer: TextView
    private lateinit var imgWord: ImageView

    private var playerInGame: Int = 0
    private lateinit var indicesImpostores: Set<Int>
    private lateinit var indicesSenoresBlancos: Set<Int>
    private lateinit var palabra: String
    private lateinit var pista: String
    private lateinit var pistaMisteriosa: String
    private lateinit var nombresImpostores: List<String>
    private lateinit var opciones: GameOptions
    private var modoLocoActivo: Boolean = false
    private var pistaActivaModoLoco: Boolean = false
    private lateinit var categoriaInGame: Category
    private lateinit var wordItemInGame: WordItem
    private lateinit var listaJugadores: List<Jugador>
    private var imageResTurno: Int = 0
    private val impostorImageRes = R.drawable.impostor
    private lateinit var imagenPorJugador: IntArray
    private var isAnimating = false
    private val selfiesTomados = mutableSetOf<Int>() // índices ya fotografiados

    // CameraX
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var pendingSelfieForPlayer: Int = -1

    private val wordImages = listOf(
        R.drawable.civil1, R.drawable.civil2, R.drawable.civil3,
        R.drawable.civil4, R.drawable.civil5, R.drawable.civil6,
        R.drawable.civil7, R.drawable.civil8, R.drawable.civil9,
        R.drawable.civil10
    )

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) iniciarCameraX()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.aplicarTema(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_impostor_reveal)
        ThemeManager.aplicarDrawables(this)

        val imgLetraS = findViewById<ImageView>(R.id.imgLetraS)
        imgLetraS?.visibility = if (ThemeManager.esCarmesi(this)) View.VISIBLE else View.GONE

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@ImpostorRevealActivity)
                    .setTitle("Salir")
                    .setMessage("¿Quieres salir de la partida?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ -> finish() }
                    .show()
            }
        })

        detailsPlayer = findViewById(R.id.detailsPlayer)
        layout        = findViewById(R.id.layoutCard)
        cardViewPrincipal = findViewById(R.id.cardViewPrincipal)
        imgDedo       = findViewById(R.id.imgDedo)
        txtTwo        = findViewById(R.id.txtTwo)
        nenxtPlayer   = findViewById(R.id.nenxtPlayer)
        presText      = findViewById(R.id.presText)
        turnPlayerName = findViewById(R.id.turnPlayerName)
        textNextPlayer = findViewById(R.id.textNextPlayer)
        hintPlayer    = findViewById(R.id.hintPlayer)
        imgWord       = findViewById(R.id.imgWord)

        ViewCompat.setOnApplyWindowInsetsListener(layout) { v, insets ->
            val top  = insets.getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout())
            val side = insets.getInsets(WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = side.left, top = top.top, right = side.right)
            insets
        }

        val baseBottomMargin = (nenxtPlayer.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(nenxtPlayer) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            (v.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = baseBottomMargin + nav.bottom
            v.requestLayout()
            insets
        }

        listaJugadores = intent.getParcelableArrayListExtra<Jugador>("PLAYERS")?.toList() ?: emptyList()
        listaCategorias = intent.getParcelableArrayListExtra<Category>("CATEGORIES")?.toList() ?: emptyList()
        opciones = intent.getParcelableExtra("OPCIONES") ?: GameOptions()

        if (listaJugadores.isEmpty() || listaCategorias.isEmpty()) { finish(); return }

        layout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        categoryViewModel.setCategories(listaCategorias)

        // Iniciar cámara si está activa la opción
        if (opciones.camaraActiva) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                iniciarCameraX()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        SelfieManager.init(cacheDir)
        onEventos()
        datosJuego()
    }

    // ── CameraX: preparar captura sin preview ──
    private fun iniciarCameraX() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                cameraProvider = future.get()
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                val selector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, selector, imageCapture!!)
            } catch (_: Exception) {}
        }, ContextCompat.getMainExecutor(this))
    }

    // ── Tomar selfie para el jugador actual ──
    private fun tomarSelfie(playerIndex: Int) {
        val ic = imageCapture ?: return
        val outFile = File(cacheDir, "selfie_tmp_$playerIndex.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(outFile).build()
        ic.takePicture(options, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bmp = BitmapFactory.decodeFile(outFile.absolutePath)
                    if (bmp != null) {
                        SelfieManager.saveBitmap(listaJugadores[playerIndex].nombre, bmp)
                        if (playerIndex == playerInGame) {
                            runOnUiThread {
                                imgWord.setImageBitmap(bmp)
                                imgWord.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                override fun onError(e: ImageCaptureException) {}
            })
    }

    private fun isActiveCategory(category: Category): Boolean =
        listaCategorias.any { it.id == category.id && it.isSelected }

    private fun datosJuego() {
        // 2. Resetear todos los roles a NORMAL
        listaJugadores = listaJugadores.map { it.copy(tipo = TipoJugador.NORMAL) }

        // 3. Elegir señores blancos PRIMERO (solo modo misterioso)
        if (opciones.modoMisterioso) {
            listaJugadores = asignarRoles(listaJugadores, opciones, emptySet())
        }

        // 4. Elegir impostores EXCLUYENDO a los señores blancos
        val indicesBlancosPrevios = listaJugadores.indices
            .filter { listaJugadores[it].tipo == TipoJugador.SENOR_BLANCO }.toSet()
        indicesImpostores = playerViewModel.pickImpostorIndices(
            listaJugadores, opciones.numImpostores, indicesBlancosPrevios
        )

        // 5. Recalcular índices de señores blancos
        indicesSenoresBlancos = listaJugadores.indices
            .filter { listaJugadores[it].tipo == TipoJugador.SENOR_BLANCO }.toSet()

        nombresImpostores = indicesImpostores.map { listaJugadores[it].nombre }

        // Asignar imagen DIFERENTE por jugador
        imagenPorJugador = IntArray(listaJugadores.size)
        val pool = wordImages.shuffled().toMutableList()
        for (i in listaJugadores.indices) {
            imagenPorJugador[i] = when {
                i in indicesImpostores -> impostorImageRes
                listaJugadores[i].tipo == TipoJugador.SENOR_BLANCO -> impostorImageRes
                else -> {
                    if (pool.isEmpty()) pool.addAll(wordImages.shuffled())
                    pool.removeFirst()
                }
            }
        }

        // Elegir categoría y palabra
        val categoriasDisponibles = listaCategorias
            .filter { it.isSelected && it.items.isNotEmpty() }
            .ifEmpty { listaCategorias.filter { it.items.isNotEmpty() } }
            .ifEmpty { listaCategorias }

        categoriaInGame = categoriasDisponibles.random()

        if (categoryViewModel.itemsVacio(categoriaInGame.id)) {
            categoryViewModel.restoreItems(categoriaInGame.id)
            categoriaInGame = categoryViewModel.categories.value!!.first { it.id == categoriaInGame.id }
        }

        val itemsFiltrados = categoriaInGame.items.filter { it.name != "Impostor" }
        wordItemInGame   = if (itemsFiltrados.isNotEmpty()) itemsFiltrados.random() else categoriaInGame.items.random()
        palabra          = wordItemInGame.name
        pista            = wordItemInGame.hints.random()
        pistaMisteriosa  = pista
        imageResTurno    = wordImages.random()

        ocultarPalabra()
        modoLocoActivo = random(10)

        if (opciones.modoLoco && modoLocoActivo) cargarInformacionModoLoco()
        else cargarInformacionNormal()

        verificarVictoriaInmediata()
    }

    // ── Verificar si impostores+blancos >= civiles → victoria inmediata ──
    private fun verificarVictoriaInmediata() {
        val totalJugadores = listaJugadores.size
        val noCiviles = indicesImpostores.size + indicesSenoresBlancos.size
        val civiles = totalJugadores - noCiviles
        if (noCiviles >= civiles) {
            // Mostrar victoria ANTES de que empiece la partida
            mostrarVictoriaInmediata()
        }
    }

    private fun mostrarVictoriaInmediata() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Desequilibrio de roles")
            .setMessage("Los no civiles son iguales o más que los civiles.\n¡Los impostores ganan automáticamente!")
            .setCancelable(false)
            .setPositiveButton("Ver resultado") { _, _ ->
                irAVictoria()
            }
            .show()
    }

    private fun irAVictoria() {
        val impostorNombres = nombresImpostores.joinToString(", ")
        val senoresBlancos  = indicesSenoresBlancos.map { listaJugadores[it].nombre }.joinToString(", ")

        // Primero victoria, luego reveal
        val intentVictory = Intent(this, VictoryActivity::class.java).apply {
            putExtra("GANADOR", "IMPOSTORES")
            putExtra("MOTIVO", "Los no civiles superaban a los civiles.")
            putExtra("IR_A_REVEAL", true)
            putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
            putParcelableArrayListExtra("LISTA_CATEGORIAS", ArrayList(listaCategorias))
            putExtra("PALABRA", palabra)
            putExtra("IMPOSTOR", impostorNombres)
            putExtra("SENORES_BLANCOS", senoresBlancos)
            putExtra("MODO_MISTERIOSO", opciones.modoMisterioso)
            putExtra("TIEMPO_LIMITADO", opciones.tiempoLimitado)
            putExtra("MINUTOS", opciones.minutos)
        }
        startActivity(intentVictory)
        finish()
    }

    private fun asignarRoles(jugadores: List<Jugador>, opts: GameOptions, excluir: Set<Int>): List<Jugador> {
        if (!opts.modoMisterioso) return jugadores.map { it.copy(tipo = TipoJugador.NORMAL) }
        // Elegibles: no están en la lista de excluidos (impostores ya elegidos)
        val elegibles = jugadores.indices.filter { it !in excluir }
        val num = opts.numSenoresBlancos.coerceAtMost(elegibles.size - 1).coerceAtLeast(0)
        val indicesBlancos = elegibles.shuffled().take(num).toSet()
        return jugadores.mapIndexed { i, j ->
            j.copy(tipo = if (i in indicesBlancos) TipoJugador.SENOR_BLANCO else TipoJugador.NORMAL)
        }
    }

    fun random(num: Int): Boolean = Random.nextInt(100) < num

    private fun textoAyudaImpostor(): String = when (opciones.tipoPista) {
        GameOptions.PRIMERA_LETRA -> "Primera letra: ${palabra.firstOrNull()?.uppercase() ?: ""}"
        else -> if (pista.isNotEmpty()) "Pista: $pista" else ""
    }

    private fun cargarInformacionModoLoco() {
        turnPlayerName.text = listaJugadores[playerInGame].nombre
        imageResTurno = impostorImageRes
        detailsPlayer.text = "ERES EL \nIMPOSTOR"
        detailsPlayer.setTextColor(getColor(R.color.colorImpostor))

        if (!pistaActivaModoLoco) {
            val wordItemRandom = listaCategorias.randomOrNull()?.items?.randomOrNull()
            if (wordItemRandom != null) { palabra = wordItemRandom.name; pista = wordItemRandom.hints.randomOrNull() ?: "" }
            else { palabra = ""; pista = "" }
        }
        hintPlayer.text = textoAyudaImpostor()
        hintPlayer.visibility = View.GONE
        pistaActivaModoLoco = true
    }

    private fun cargarInformacionNormal() {
        turnPlayerName.text = listaJugadores[playerInGame].nombre
        imageResTurno = imagenPorJugador[playerInGame]

        val esSenorBlanco = listaJugadores[playerInGame].tipo == TipoJugador.SENOR_BLANCO
        val esImpostor    = playerInGame in indicesImpostores

        when {
            esSenorBlanco -> {
                detailsPlayer.text = "SEÑOR BLANCO\nNo tienes palabra"
                detailsPlayer.setTextColor(getColor(R.color.colorImpostor))
                hintPlayer.text = ""; hintPlayer.visibility = View.GONE
            }
            esImpostor && opciones.modoMisterioso -> {
                detailsPlayer.text = pistaMisteriosa
                detailsPlayer.setTextColor(Color.WHITE)
                hintPlayer.text = ""; hintPlayer.visibility = View.GONE
            }
            esImpostor -> {
                detailsPlayer.text = "ERES EL \nIMPOSTOR"
                detailsPlayer.setTextColor(getColor(R.color.colorImpostor))
                hintPlayer.text = textoAyudaImpostor()
                hintPlayer.visibility = View.GONE
            }
            else -> {
                detailsPlayer.text = palabra
                detailsPlayer.setTextColor(Color.WHITE)
                hintPlayer.text = ""; hintPlayer.visibility = View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onEventos() {
        cardViewPrincipal.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (opciones.modoLoco && modoLocoActivo) mostrarPalabraModoLoco()
                    else mostrarPalabraNormal()
                    nenxtPlayer.visibility = View.VISIBLE
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    ocultarPalabra(); true
                }
                else -> false
            }
        }
        nenxtPlayer.setOnClickListener { btnNextPlayer() }
    }

    private fun ocultarPalabra() {
        detailsPlayer.visibility = View.GONE
        hintPlayer.visibility    = View.GONE
        imgWord.visibility       = View.GONE
        imgDedo.visibility       = View.VISIBLE
        txtTwo.visibility        = View.VISIBLE
        presText.visibility      = View.VISIBLE
    }

    private fun mostrarPalabraNormal() {
        cargarInformacionNormal()
        imgDedo.visibility  = View.GONE
        txtTwo.visibility   = View.GONE
        presText.visibility = View.GONE

        // Selfie: solo la primera vez que este jugador ve su palabra
        if (opciones.camaraActiva && imageCapture != null && playerInGame !in selfiesTomados) {
            selfiesTomados.add(playerInGame)
            tomarSelfie(playerInGame)
        }

        val esImpostor    = playerInGame in indicesImpostores
        val esSenorBlanco = listaJugadores[playerInGame].tipo == TipoJugador.SENOR_BLANCO

        hintPlayer.visibility = if (esImpostor && !opciones.modoMisterioso && !esSenorBlanco)
            View.VISIBLE else View.GONE

        detailsPlayer.visibility = View.VISIBLE

        // Imagen: selfie si existe → mostrarlo
        // Si cámara activa y aún no hay selfie → no mostrar imagen (evitar flash de imagen por defecto)
        val selfie = SelfieManager.getBitmap(listaJugadores[playerInGame].nombre)
        when {
            selfie != null -> {
                imgWord.setImageBitmap(selfie)
                imgWord.visibility = View.VISIBLE
            }
            opciones.camaraActiva -> {
                imgWord.setImageResource(R.drawable.ic_touch_app)
                imgWord.visibility = View.VISIBLE
            }
            else -> {
                imgWord.setImageResource(imageResTurno)
                imgWord.visibility = View.VISIBLE
            }
        }
    }

    private fun mostrarPalabraModoLoco() {
        cargarInformacionModoLoco()
        imgDedo.visibility  = View.GONE
        txtTwo.visibility   = View.GONE
        presText.visibility = View.GONE
        detailsPlayer.visibility = View.VISIBLE

        if (opciones.camaraActiva && imageCapture != null && playerInGame !in selfiesTomados) {
            selfiesTomados.add(playerInGame)
            tomarSelfie(playerInGame)
        }

        val selfie = SelfieManager.getBitmap(listaJugadores[playerInGame].nombre)
        when {
            selfie != null -> {
                imgWord.setImageBitmap(selfie)
                imgWord.visibility = View.VISIBLE
            }
            opciones.camaraActiva -> {
                imgWord.visibility = View.INVISIBLE
            }
            else -> {
                imgWord.setImageResource(imageResTurno)
                imgWord.visibility = View.VISIBLE
            }
        }

        hintPlayer.visibility = View.VISIBLE
    }

    private fun btnNextPlayer() {
        pistaActivaModoLoco = false
        val lastIndex = listaJugadores.lastIndex

        if (playerInGame == lastIndex) {
            val impostorNombres = if (opciones.modoLoco && modoLocoActivo)
                "TODOS SOIS IMPOSTORES"
            else
                nombresImpostores.joinToString(", ")

            val senoresBlancos = indicesSenoresBlancos.map { listaJugadores[it].nombre }.joinToString(", ")

            // Pasar jugadores con TipoJugador correcto a PlayGameActivity
            val jugadoresConRoles = listaJugadores.mapIndexed { i, j ->
                when {
                    i in indicesImpostores -> j.copy(tipo = TipoJugador.IMPOSTOR)
                    j.tipo == TipoJugador.SENOR_BLANCO -> j
                    else -> j.copy(tipo = TipoJugador.NORMAL)
                }
            }

            val intent = Intent(this, PlayGameActivity::class.java).apply {
                putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(jugadoresConRoles))
                putExtra("JUGADOR_EMPIEZA", playerViewModel.pickJugadorQueEmpieza(jugadoresConRoles)?.nombre ?: "")
                putParcelableArrayListExtra("LISTA_CATEGORIAS", ArrayList(listaCategorias))
                putExtra("PALABRA", if (opciones.modoLoco && modoLocoActivo) "NO HABIA PALABRA" else palabra)
                putExtra("IMPOSTOR", impostorNombres)
                putExtra("SENORES_BLANCOS", senoresBlancos)
                putExtra("MODO_MISTERIOSO", opciones.modoMisterioso)
                putExtra("TIEMPO_LIMITADO", opciones.tiempoLimitado)
                putExtra("MINUTOS", opciones.minutos)
            }
            startActivity(intent)
            finishWithUpdatedCategories()
            return
        }

        if (isAnimating) return
        isAnimating = true
        nenxtPlayer.isEnabled = false
        playerInGame++

        slideOutIn(cardViewPrincipal, outExtra = 120f) {
            nenxtPlayer.visibility = View.INVISIBLE
            ocultarPalabra()
            if (opciones.modoLoco && modoLocoActivo) cargarInformacionModoLoco()
            else cargarInformacionNormal()
            val esProximoElUltimo = (playerInGame == lastIndex)
            textNextPlayer.text = if (esProximoElUltimo) "¡EMPEZAR PARTIDA!" else "⏭ SIGUIENTE JUGADOR"
            categoryViewModel.deleteWordItem(categoriaInGame.id, wordItemInGame)
            if (esProximoElUltimo) categoryViewModel.logItems(categoriaInGame.id)
        }

        cardViewPrincipal.postDelayed({ nenxtPlayer.isEnabled = true; isAnimating = false }, 420)
    }

    private fun slideOutIn(card: View, outExtra: Float = 0f, onSwap: () -> Unit) {
        val w = card.width.toFloat().takeIf { it > 0f } ?: return
        card.animate().cancel()
        card.animate()
            .translationX(-(w + outExtra)).alpha(0f).setDuration(180L)
            .withEndAction {
                onSwap()
                card.translationX = (w + outExtra); card.alpha = 0f
                card.animate().translationX(0f).alpha(1f).setDuration(180L).start()
            }.start()
    }

    private fun finishWithUpdatedCategories() {
        setResult(RESULT_OK, Intent().apply {
            putParcelableArrayListExtra("UPDATED_CATEGORIES",
                ArrayList(categoryViewModel.categories.value ?: emptyList()))
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
    }
}