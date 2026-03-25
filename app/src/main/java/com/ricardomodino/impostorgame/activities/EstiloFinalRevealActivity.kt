package com.ricardomodino.impostorgame.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.Matrix
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.core.widget.TextViewCompat
import androidx.exifinterface.media.ExifInterface
import com.ricardomodino.impostorgame.CategoryViewModel
import com.ricardomodino.impostorgame.PlayerViewModel
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

class EstiloFinalRevealActivity : AppCompatActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()
    private val datosViewModel: DatosCuriososViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private lateinit var txtContador: TextView
    private lateinit var txtNombre: TextView
    private lateinit var lineaAcento: View
    private lateinit var cardReveal: FrameLayout
    private lateinit var capaCubierta: FrameLayout
    private lateinit var imgJugador: ImageView
    private lateinit var layoutHintTap: LinearLayout
    private lateinit var layoutContenido: LinearLayout
    private lateinit var layoutImpostor: LinearLayout
    private lateinit var layoutDato: LinearLayout
    private lateinit var txtDatoCurioso: TextView
    private lateinit var txtImpostorEmoji: TextView
    private lateinit var txtImpostorTitulo: TextView
    private lateinit var txtImpostorSubtitulo: TextView
    private lateinit var txtLabelContenido: TextView
    private lateinit var btnSiguiente: CardView
    private lateinit var txtBtnSiguiente: TextView

    private lateinit var listaJugadores: List<Jugador>
    private lateinit var listaCategorias: List<Category>
    private lateinit var opciones: GameOptions
    private lateinit var indicesImpostores: Set<Int>
    private var indicesSenoresBlancos: Set<Int> = emptySet()

    // Datos curiosos (solo cuando modoDatosCuriosos = true)
    private var datosAsignados: Map<Int, DatoCurioso> = emptyMap()

    // Palabra (solo cuando modoDatosCuriosos = false)
    private var palabra: String = ""
    private var pista: String = ""
    private var pistaMisteriosa: String = ""

    private var playerInGame = 0
    private var cubiertaRevelada = false
    private val selfiesTomados = mutableSetOf<Int>()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null

    private val imagenesCiviles = listOf(
        R.drawable.civil1, R.drawable.civil2, R.drawable.civil3,
        R.drawable.civil4, R.drawable.civil5, R.drawable.civil6,
        R.drawable.civil7, R.drawable.civil8, R.drawable.civil9,
        R.drawable.civil10
    )
    private lateinit var imagenPorJugador: IntArray

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) iniciarCameraX()
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_datos_curiosos_reveal)

        bindViews()
        aplicarTema()
        configurarInsets()

        listaJugadores  = intent.getParcelableArrayListExtra<Jugador>("PLAYERS")?.toList() ?: emptyList()
        listaCategorias = intent.getParcelableArrayListExtra<Category>("CATEGORIES")?.toList() ?: emptyList()
        opciones        = intent.getParcelableExtra("OPCIONES") ?: GameOptions()

        if (listaJugadores.isEmpty()) { finish(); return }

        // Asignar roles
        listaJugadores = listaJugadores.map { it.copy(tipo = TipoJugador.NORMAL) }
        if (opciones.modoMisterioso) {
            listaJugadores = asignarSenoresBlancos(listaJugadores, opciones)
        }
        indicesSenoresBlancos = listaJugadores.indices
            .filter { listaJugadores[it].tipo == TipoJugador.SENOR_BLANCO }.toSet()
        indicesImpostores = playerViewModel.pickImpostorIndices(
            listaJugadores, opciones.numImpostores, indicesSenoresBlancos
        )

        // Asignar imágenes
        val pool = imagenesCiviles.shuffled().toMutableList()
        imagenPorJugador = IntArray(listaJugadores.size) {
            if (it in indicesImpostores || listaJugadores[it].tipo == TipoJugador.SENOR_BLANCO)
                R.drawable.impostor
            else {
                if (pool.isEmpty()) pool.addAll(imagenesCiviles.shuffled())
                pool.removeAt(0)
            }
        }

        // Contenido según modo
        if (opciones.modoDatosCuriosos) {
            asignarDatos()
        } else {
            seleccionarPalabra()
        }

        SelfieManager.init(cacheDir)

        if (opciones.camaraActiva) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                iniciarCameraX()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                GameDialog(this@EstiloFinalRevealActivity)
                    .icon("🚪")
                    .title("Salir")
                    .message("¿Quieres salir de la partida?")
                    .cancelable(true)
                    .positiveButton("Salir") { finish() }
                    .negativeButton("Cancelar")
                    .show()
            }
        })

        mostrarJugador(playerInGame)
        configurarBotones()
    }

    private fun bindViews() {
        txtContador         = findViewById(R.id.txtContadorJugador)
        txtNombre           = findViewById(R.id.txtNombreJugador)
        lineaAcento         = findViewById(R.id.lineaAcento)
        cardReveal          = findViewById(R.id.cardReveal)
        capaCubierta        = findViewById(R.id.capaCubierta)
        imgJugador          = findViewById(R.id.imgJugador)
        layoutHintTap       = findViewById(R.id.layoutHintTap)
        layoutContenido     = findViewById(R.id.layoutContenido)
        layoutImpostor      = findViewById(R.id.layoutImpostor)
        layoutDato          = findViewById(R.id.layoutDato)
        txtDatoCurioso      = findViewById(R.id.txtDatoCurioso)
        txtImpostorEmoji    = findViewById(R.id.txtImpostorEmoji)
        txtImpostorTitulo   = findViewById(R.id.txtImpostorTitulo)
        txtImpostorSubtitulo = findViewById(R.id.txtImpostorSubtitulo)
        txtLabelContenido   = findViewById(R.id.txtLabelContenido)
        btnSiguiente        = findViewById(R.id.btnSiguienteJugador)
        txtBtnSiguiente     = findViewById(R.id.txtBtnSiguiente)
    }

    private fun aplicarTema() {
        val accent = ThemeManager.getAccentColor(this)
        lineaAcento.setBackgroundColor(accent)
        ThemeManager.aplicarDrawables(this)
    }

    private fun configurarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutDatosCuriosos)) { v, insets ->
            val top = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            ).top
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            v.updatePadding(top = top + 24, bottom = bottom + 24)
            insets
        }
    }

    // ── Selección de palabra para modos clásico y misterioso ──
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

    private fun tomarSelfie(playerIndex: Int) {
        val captura = imageCapture ?: return
        val outFile = File(cacheDir, "selfie_final_$playerIndex.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(outFile).build()

        captura.takePicture(options, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val raw = BitmapFactory.decodeFile(outFile.absolutePath) ?: return
                    val bmp = corregirOrientacion(raw, outFile)
                    SelfieManager.saveBitmap(listaJugadores[playerIndex].nombre, bmp)
                    if (playerIndex == playerInGame) {
                        runOnUiThread { imgJugador.setImageBitmap(bmp) }
                    }
                }

                override fun onError(e: ImageCaptureException) {}
            }
        )
    }

    private fun corregirOrientacion(
        bmp: android.graphics.Bitmap,
        file: File
    ): android.graphics.Bitmap {
        val orientation = try {
            ExifInterface(file.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.postRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bmp
        }

        return try {
            android.graphics.Bitmap.createBitmap(
                bmp,
                0,
                0,
                bmp.width,
                bmp.height,
                matrix,
                true
            )
        } catch (_: Exception) {
            bmp
        }
    }

    private fun seleccionarPalabra() {
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

        val items = catActual.items.filter { it.name != "Impostor" }
        val item  = if (items.isNotEmpty()) items.random() else catActual.items.random()
        palabra         = item.name
        pista           = item.hints.randomOrNull() ?: ""
        pistaMisteriosa = pista
    }

    // ── Asignación de datos curiosos ──
    private fun asignarDatos() {
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

    private fun datosPartida(): ArrayList<DatoCurioso> =
        ArrayList(listaJugadores.indices.mapNotNull { datosAsignados[it] })

    // ── Asignación de señores blancos (modo misterioso) ──
    private fun asignarSenoresBlancos(jugadores: List<Jugador>, opts: GameOptions): List<Jugador> {
        val elegibles = jugadores.indices.toList()
        val num = opts.numSenoresBlancos.coerceAtMost(elegibles.size - 1).coerceAtLeast(0)
        val indices = elegibles.shuffled().take(num).toSet()
        return jugadores.mapIndexed { i, j ->
            j.copy(tipo = if (i in indices) TipoJugador.SENOR_BLANCO else TipoJugador.NORMAL)
        }
    }

    // ── Mostrar datos del jugador actual ──
    private fun mostrarJugador(index: Int) {
        cubiertaRevelada = false
        val jugador = listaJugadores[index]
        val total   = listaJugadores.size

        txtContador.text = "Jugador ${index + 1} de $total"
        txtNombre.text   = jugador.nombre.uppercase()

        val selfie = SelfieManager.getBitmap(jugador.nombre)
        if (selfie != null) imgJugador.setImageBitmap(selfie)
        else imgJugador.setImageResource(imagenPorJugador[index])

        // Ocultar contenido, mostrar cubierta
        layoutImpostor.visibility = View.GONE
        layoutDato.visibility     = View.GONE
        capaCubierta.visibility   = View.VISIBLE
        layoutHintTap.visibility  = View.VISIBLE
        btnSiguiente.visibility   = View.GONE

        txtBtnSiguiente.text = if (index == total - 1) "⏭  EMPEZAR PARTIDA" else "⏭  SIGUIENTE JUGADOR"

        txtNombre.translationX = 60f
        txtNombre.alpha = 0f
        txtNombre.animate().translationX(0f).alpha(1f).setDuration(300L).start()

        prepararContenido()
    }

    // ── Preparar el contenido que se verá al revelar ──
    private fun prepararContenido() {
        val index        = playerInGame
        val esImpostor   = index in indicesImpostores
        val esSenorBlanco = listaJugadores[index].tipo == TipoJugador.SENOR_BLANCO
        configurarTextoPrincipal(esPalabra = false)

        when {
            esSenorBlanco -> {
                txtImpostorEmoji.text     = "⚪"
                txtImpostorTitulo.text    = "SEÑOR BLANCO"
                txtImpostorTitulo.setTextColor(Color.WHITE)
                txtImpostorSubtitulo.text = "No tienes palabra"
                layoutImpostor.visibility = View.VISIBLE
                layoutDato.visibility     = View.GONE
            }
            esImpostor -> {
                when {
                    opciones.modoMisterioso -> {
                        // Impostor ve su pista misteriosa en la tarjeta de contenido
                        txtDatoCurioso.text    = pistaMisteriosa
                        txtLabelContenido.text = "TU PISTA"
                        layoutDato.visibility     = View.VISIBLE
                        layoutImpostor.visibility = View.GONE
                    }
                    else -> {
                        // Clásico o datos curiosos
                        txtImpostorEmoji.text     = "🕵️"
                        txtImpostorTitulo.text    = getString(R.string.datos_eres_impostor)
                        txtImpostorTitulo.setTextColor(Color.parseColor("#FF4444"))
                        txtImpostorSubtitulo.text = if (opciones.modoDatosCuriosos)
                            getString(R.string.datos_inventa_dato)
                        else
                            textoAyudaImpostor()
                        layoutImpostor.visibility = View.VISIBLE
                        layoutDato.visibility     = View.GONE
                    }
                }
            }
            else -> {
                // Civil
                if (opciones.modoDatosCuriosos) {
                    txtDatoCurioso.text    = datosAsignados[index]?.let { getTextoDato(it) } ?: ""
                    txtLabelContenido.text = getString(R.string.datos_label_dato)
                } else {
                    configurarTextoPrincipal(esPalabra = true)
                    txtDatoCurioso.text    = palabra
                    txtLabelContenido.text = "TU PALABRA"
                }
                layoutDato.visibility     = View.VISIBLE
                layoutImpostor.visibility = View.GONE
            }
        }
    }

    private fun configurarTextoPrincipal(esPalabra: Boolean) {
        txtDatoCurioso.includeFontPadding = false

        val layoutDatoParams = layoutDato.layoutParams as LinearLayout.LayoutParams
        val textoParams = txtDatoCurioso.layoutParams as LinearLayout.LayoutParams

        if (esPalabra) {
            layoutDatoParams.height = LinearLayout.LayoutParams.MATCH_PARENT
            layoutDato.layoutParams = layoutDatoParams
            layoutDato.gravity = Gravity.CENTER_HORIZONTAL

            textoParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            textoParams.height = 0
            textoParams.weight = 1f
            textoParams.bottomMargin = dp(24)
            txtDatoCurioso.layoutParams = textoParams

            txtDatoCurioso.maxLines = 3
            txtDatoCurioso.gravity = Gravity.CENTER
            txtDatoCurioso.setLineSpacing(0f, 0.95f)
            txtDatoCurioso.setTypeface(txtDatoCurioso.typeface, Typeface.BOLD)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                txtDatoCurioso,
                26,
                92,
                2,
                TypedValue.COMPLEX_UNIT_SP
            )
        } else {
            layoutDatoParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutDato.layoutParams = layoutDatoParams
            layoutDato.gravity = Gravity.CENTER

            textoParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            textoParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            textoParams.weight = 0f
            textoParams.bottomMargin = dp(20)
            txtDatoCurioso.layoutParams = textoParams

            txtDatoCurioso.maxLines = Int.MAX_VALUE
            txtDatoCurioso.gravity = Gravity.CENTER
            txtDatoCurioso.setLineSpacing(0f, 1.5f)
            txtDatoCurioso.setTypeface(txtDatoCurioso.typeface, Typeface.NORMAL)
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                txtDatoCurioso,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
            )
            txtDatoCurioso.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()

    private fun textoAyudaImpostor(): String = when (opciones.tipoPista) {
        GameOptions.PRIMERA_LETRA -> "Primera letra: ${palabra.firstOrNull()?.uppercase() ?: ""}"
        else -> if (pista.isNotEmpty()) "Pista: $pista" else ""
    }

    private fun getTextoDato(dato: DatoCurioso): String = when (LocaleManager.getLanguage(this)) {
        "en"      -> dato.en
        "zh-Hans" -> dato.zhHans
        "zh-Hant" -> dato.zhHant
        else      -> dato.es
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configurarBotones() {
        capaCubierta.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    capaCubierta.parent?.requestDisallowInterceptTouchEvent(true)
                    if (opciones.camaraActiva && imageCapture != null && playerInGame !in selfiesTomados) {
                        selfiesTomados.add(playerInGame)
                        tomarSelfie(playerInGame)
                    }
                    capaCubierta.visibility  = View.INVISIBLE
                    layoutHintTap.visibility = View.GONE
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    capaCubierta.visibility  = View.VISIBLE
                    layoutHintTap.visibility = View.VISIBLE
                    if (!cubiertaRevelada) {
                        cubiertaRevelada = true
                        btnSiguiente.visibility = View.VISIBLE
                    }
                    true
                }
                else -> false
            }
        }

        btnSiguiente.setOnClickListener {
            if (!cubiertaRevelada) return@setOnClickListener
            if (playerInGame == listaJugadores.lastIndex) {
                irAPartida()
            } else {
                playerInGame++
                mostrarJugador(playerInGame)
            }
        }
    }

    private fun irAPartida() {
        val jugadoresConRoles = listaJugadores.mapIndexed { i, j ->
            when {
                i in indicesImpostores          -> j.copy(tipo = TipoJugador.IMPOSTOR)
                j.tipo == TipoJugador.SENOR_BLANCO -> j
                else                            -> j
            }
        }
        val nombresImpostores = indicesImpostores.map { listaJugadores[it].nombre }.joinToString(", ")
        val senoresBlancos    = indicesSenoresBlancos.map { listaJugadores[it].nombre }.joinToString(", ")

        val intent = Intent(this, PlayGameActivity::class.java).apply {
            putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(jugadoresConRoles))
            putExtra("JUGADOR_EMPIEZA", playerViewModel.pickJugadorQueEmpieza(jugadoresConRoles)?.nombre ?: "")
            putParcelableArrayListExtra("LISTA_CATEGORIAS", ArrayList(listaCategorias))
            putParcelableArrayListExtra("DATOS_PARTIDA", datosPartida())
            putExtra("PALABRA", if (opciones.modoDatosCuriosos) "" else palabra)
            putExtra("IMPOSTOR", nombresImpostores)
            putExtra("SENORES_BLANCOS", senoresBlancos)
            putExtra("MODO_MISTERIOSO", opciones.modoMisterioso)
            putExtra("MODO_DATOS_CURIOSOS", opciones.modoDatosCuriosos)
            putExtra("TIEMPO_LIMITADO", opciones.tiempoLimitado)
            putExtra("MINUTOS", opciones.minutos)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
    }

    private fun playRevealTone() {
        if (!com.ricardomodino.impostorgame.managers.SoundManager.isSoundEnabled(this)) return
        try {
            val sampleRate = 44100
            val durationMs = 180
            val numSamples = sampleRate * durationMs / 1000
            val fadeLen    = (sampleRate * 0.02).toInt()
            val samples    = ShortArray(numSamples)
            val freq       = 528f
            for (i in 0 until numSamples) {
                val env = when {
                    i < fadeLen              -> i.toDouble() / fadeLen
                    i > numSamples - fadeLen -> (numSamples - i).toDouble() / fadeLen
                    else                     -> 1.0
                }
                samples[i] = (env * 0.6 * Short.MAX_VALUE *
                        sin(2.0 * PI * freq * i / sampleRate)).toInt().toShort()
            }
            val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val track  = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf, numSamples * 2), AudioTrack.MODE_STATIC)
            track.write(samples, 0, numSamples)
            track.setNotificationMarkerPosition(numSamples)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) { t.release() }
                override fun onPeriodicNotification(t: AudioTrack) {}
            })
            track.play()
        } catch (_: Exception) {}
    }
}
