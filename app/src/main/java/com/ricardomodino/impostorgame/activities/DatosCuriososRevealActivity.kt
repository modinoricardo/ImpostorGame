package com.ricardomodino.impostorgame.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.view.MotionEvent
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ricardomodino.impostorgame.PlayerViewModel
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.SoundManager
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.DatoCategoria
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel
import kotlin.math.PI
import kotlin.math.sin

class DatosCuriososRevealActivity : AppCompatActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()
    private val datosViewModel: DatosCuriososViewModel by viewModels()

    private lateinit var layoutRoot: android.widget.FrameLayout
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
    private lateinit var btnSiguiente: CardView
    private lateinit var txtBtnSiguiente: TextView

    private lateinit var listaJugadores: List<Jugador>
    private lateinit var opciones: GameOptions
    private lateinit var indicesImpostores: Set<Int>
    private lateinit var datosAsignados: Map<Int, DatoCurioso> // índice jugador → dato

    private var playerInGame = 0
    private var cubiertaRevelada = false
    private val imagenesCiviles = listOf(
        R.drawable.civil1, R.drawable.civil2, R.drawable.civil3,
        R.drawable.civil4, R.drawable.civil5, R.drawable.civil6,
        R.drawable.civil7, R.drawable.civil8, R.drawable.civil9,
        R.drawable.civil10
    )
    private lateinit var imagenPorJugador: IntArray

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

        listaJugadores = intent.getParcelableArrayListExtra<Jugador>("PLAYERS")?.toList() ?: emptyList()
        opciones       = intent.getParcelableExtra("OPCIONES") ?: GameOptions()

        if (listaJugadores.isEmpty()) { finish(); return }

        // Asignar impostores
        listaJugadores = listaJugadores.map { it.copy(tipo = TipoJugador.NORMAL) }
        indicesImpostores = playerViewModel.pickImpostorIndices(listaJugadores, opciones.numImpostores)

        // Asignar imágenes por jugador
        val pool = imagenesCiviles.shuffled().toMutableList()
        imagenPorJugador = IntArray(listaJugadores.size) {
            if (it in indicesImpostores) R.drawable.impostor
            else { if (pool.isEmpty()) pool.addAll(imagenesCiviles.shuffled()); pool.removeAt(0) }
        }

        // Asignar datos curiosos a civiles
        asignarDatos()

        // SelfieManager
        SelfieManager.init(cacheDir)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                GameDialog(this@DatosCuriososRevealActivity)
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
        layoutRoot       = findViewById<android.widget.FrameLayout>(R.id.rootDatosCuriosos)
        txtContador      = findViewById(R.id.txtContadorJugador)
        txtNombre        = findViewById(R.id.txtNombreJugador)
        lineaAcento      = findViewById(R.id.lineaAcento)
        cardReveal       = findViewById(R.id.cardReveal)
        capaCubierta     = findViewById(R.id.capaCubierta)
        imgJugador       = findViewById(R.id.imgJugador)
        layoutHintTap    = findViewById(R.id.layoutHintTap)
        layoutContenido  = findViewById(R.id.layoutContenido)
        layoutImpostor   = findViewById(R.id.layoutImpostor)
        layoutDato       = findViewById(R.id.layoutDato)
        txtDatoCurioso   = findViewById(R.id.txtDatoCurioso)
        btnSiguiente     = findViewById(R.id.btnSiguienteJugador)
        txtBtnSiguiente  = findViewById(R.id.txtBtnSiguiente)
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

    private fun asignarDatos() {
        val categoriasActivas = datosViewModel.getCategoriasActivas()
        val todosLosDatos = categoriasActivas.flatMap { it.datos }.shuffled().toMutableList()

        val mapa = mutableMapOf<Int, DatoCurioso>()
        var idx = 0
        for (i in listaJugadores.indices) {
            if (i !in indicesImpostores) {
                if (idx < todosLosDatos.size) {
                    mapa[i] = todosLosDatos[idx++]
                }
            }
        }
        datosAsignados = mapa
    }

    private fun datosPartida(): ArrayList<DatoCurioso> =
        ArrayList(listaJugadores.indices.mapNotNull { datosAsignados[it] })

    private fun mostrarJugador(index: Int) {
        cubiertaRevelada = false
        val jugador = listaJugadores[index]
        val total   = listaJugadores.size
        val esUltimo = index == total - 1

        txtContador.text = "Jugador ${index + 1} de $total"
        txtNombre.text   = jugador.nombre.uppercase()

        // Imagen
        val selfie = SelfieManager.getBitmap(jugador.nombre)
        if (selfie != null) {
            imgJugador.setImageBitmap(selfie)
        } else {
            imgJugador.setImageResource(imagenPorJugador[index])
        }

        // Ocultar contenido, mostrar cubierta
        layoutImpostor.visibility = View.GONE
        layoutDato.visibility     = View.GONE
        capaCubierta.translationY = 0f
        capaCubierta.alpha        = 1f
        capaCubierta.visibility   = View.VISIBLE
        layoutHintTap.visibility  = View.VISIBLE
        btnSiguiente.visibility   = View.GONE

        txtBtnSiguiente.text = if (esUltimo) "⏭  EMPEZAR PARTIDA" else "⏭  SIGUIENTE JUGADOR"

        // Animación entrada del nombre
        txtNombre.translationX = 60f
        txtNombre.alpha = 0f
        txtNombre.animate().translationX(0f).alpha(1f).setDuration(300L).start()

        // En modo no-JMC preparar contenido del nuevo jugador detrás de la cubierta
        if (!ThemeManager.esJmc(this)) prepararContenido()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configurarBotones() {
        if (ThemeManager.esJmc(this)) {
            // JMC: tap para revelar con animación deslizante
            capaCubierta.setOnClickListener {
                if (!cubiertaRevelada) revelarDatoJmc()
            }
        } else {
            // CLASICO / CARMESI / otros: mantener pulsado para ver, soltar para ocultar
            prepararContenido()
            capaCubierta.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
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

    private fun prepararContenido() {
        val index = playerInGame
        if (index in indicesImpostores) {
            layoutImpostor.visibility = View.VISIBLE
            layoutDato.visibility     = View.GONE
        } else {
            val dato = datosAsignados[index]
            txtDatoCurioso.text       = dato?.es ?: ""
            layoutDato.visibility     = View.VISIBLE
            layoutImpostor.visibility = View.GONE
        }
    }

    private fun revelarDatoJmc() {
        cubiertaRevelada = true
        prepararContenido()

        val alturaCard = capaCubierta.height.toFloat().takeIf { it > 0f }
            ?: resources.displayMetrics.heightPixels.toFloat()

        capaCubierta.animate()
            .translationY(-alturaCard)
            .alpha(0f)
            .setDuration(400L)
            .withEndAction {
                capaCubierta.visibility   = View.GONE
                btnSiguiente.visibility   = View.VISIBLE
                btnSiguiente.alpha        = 0f
                btnSiguiente.translationY = 20f
                btnSiguiente.animate().alpha(1f).translationY(0f).setDuration(250L).start()
            }.start()
        playRevealTone()
    }

    private fun playRevealTone() {
        if (!SoundManager.isSoundEnabled(this)) return
        try {
            val sampleRate = 44100
            val durationMs = 180
            val numSamples = sampleRate * durationMs / 1000
            val fadeLen    = (sampleRate * 0.02).toInt()
            val samples    = ShortArray(numSamples)
            val freq       = 528f // frecuencia "revelación"
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

    private fun irAPartida() {
        val jugadoresConRoles = listaJugadores.mapIndexed { i, j ->
            if (i in indicesImpostores) j.copy(tipo = TipoJugador.IMPOSTOR) else j
        }
        val nombresImpostores = indicesImpostores.map { listaJugadores[it].nombre }.joinToString(", ")

        val intent = Intent(this, PlayGameActivity::class.java).apply {
            putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(jugadoresConRoles))
            putExtra("JUGADOR_EMPIEZA", playerViewModel.pickJugadorQueEmpieza(jugadoresConRoles)?.nombre ?: "")
            putParcelableArrayListExtra("LISTA_CATEGORIAS", ArrayList<android.os.Parcelable>())
            putParcelableArrayListExtra("DATOS_PARTIDA", datosPartida())
            putExtra("PALABRA", "")
            putExtra("IMPOSTOR", nombresImpostores)
            putExtra("SENORES_BLANCOS", "")
            putExtra("MODO_MISTERIOSO", false)
            putExtra("MODO_DATOS_CURIOSOS", true)
            putExtra("TIEMPO_LIMITADO", opciones.tiempoLimitado)
            putExtra("MINUTOS", opciones.minutos)
        }
        startActivity(intent)
        finish()
    }
}
