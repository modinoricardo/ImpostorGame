package com.ricardomodino.impostorgame.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.ToneGenerator
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.ricardomodino.impostorgame.PlayerViewModel
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador

class PlayGameActivity : AppCompatActivity() {

    private lateinit var btnNewGame: Button
    private lateinit var btnVotar: Button
    private lateinit var txtHabla: TextView
    private lateinit var txtTimer: TextView
    private lateinit var txtSubtitle: TextView
    private lateinit var txtFooter: TextView
    private lateinit var txtLabelPalabra: TextView
    private lateinit var listaJugadores: List<Jugador>
    private lateinit var palabraJugada: String
    private lateinit var btnRevelar: Button
    private lateinit var cardViewPalabra: CardView
    private lateinit var cardResumen: CardView
    private lateinit var cardSenorBlanco: CardView
    private lateinit var scrollPalabra: NestedScrollView
    private lateinit var txtPalabra: TextView
    private lateinit var txtImpostorNombre: TextView
    private lateinit var txtSenorBlancoNombre: TextView
    private lateinit var nombreImpostor: String
    private lateinit var nombresSenoresBlancos: String
    private var datosPartida: List<DatoCurioso> = emptyList()
    private var modoMisterioso: Boolean = false
    private var modoDatosCuriosos: Boolean = false
    private var tiempoLimitado: Boolean = false
    private var minutos: Int = 3
    private val playerViewModel: PlayerViewModel by viewModels()
    private var impostorContado = false
    private var mediaPlayer: MediaPlayer? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var cardsContainer: android.widget.LinearLayout
    private var lottieFinalInvitado: LottieAnimationView? = null
    private var animacionFinalSeleccionada: String? = null
    private val animacionesFinales = listOf(
        "final_reveal/cute_doggie.json",
        "final_reveal/delivery_riding.json",
        "final_reveal/groovy_walk_cycle.json",
        "final_reveal/loading_50_among_us.json",
        "final_reveal/run_forrest_run.json",
        "final_reveal/running_character.json",
        "final_reveal/walk_cycling_shoes.json",
        "final_reveal/walker_man.json",
        "final_reveal/walking_avocado.json",
        "final_reveal/walking_orange.json"
    )
    private val intervaloAnimacionFinalMs = 10_000L
    private val duracionAnimacionFinalMs = 6_860L
    private val repetirAnimacionFinal = Runnable {
        if (!isFinishing &&
            !isDestroyed &&
            ThemeManager.esFinal(this) &&
            ::cardsContainer.isInitialized &&
            cardsContainer.visibility == View.VISIBLE) {
            animarInvitadoFinal()
        }
    }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(if (ThemeManager.esFinal(this)) R.layout.activity_play_game_final else R.layout.activity_play_game)
        ThemeManager.aplicarDrawables(this)

        btnNewGame           = findViewById(R.id.btnNewGame)
        btnVotar             = findViewById(R.id.btnVotar)
        txtHabla             = findViewById(R.id.txtImpostor)
        txtTimer             = findViewById(R.id.txtTimer)
        txtSubtitle          = findViewById(R.id.txtSubtitle)
        txtFooter            = findViewById(R.id.txtFooter)
        txtLabelPalabra      = findViewById(R.id.txtLabelPalabra)
        btnRevelar           = findViewById(R.id.btnRevelar)
        cardViewPalabra      = findViewById(R.id.cardViewPalabra)
        cardResumen          = findViewById(R.id.cardResumen)
        cardSenorBlanco      = findViewById(R.id.cardSenorBlanco)
        scrollPalabra        = findViewById(R.id.scrollPalabra)
        txtPalabra           = findViewById(R.id.txtPalabra)
        txtImpostorNombre    = findViewById(R.id.txtImpostorNombre)
        txtSenorBlancoNombre = findViewById(R.id.txtSenorBlancoNombre)
        cardsContainer       = findViewById(R.id.cardsContainer)
        lottieFinalInvitado  = findViewById(R.id.lottieFinalInvitado)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                GameDialog(this@PlayGameActivity)
                    .icon("🚪")
                    .title("Salir")
                    .message("\u00BFQuieres salir de la partida?")
                    .cancelable(true)
                    .positiveButton("Salir") { SelfieManager.clear(); finish() }
                    .negativeButton("Cancelar")
                    .show()
            }
        })

        val root   = findViewById<View>(R.id.main)
        val btnRow = findViewById<View>(R.id.btnRow)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val top  = insets.getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout())
            val side = insets.getInsets(WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = side.left, top = top.top, right = side.right); insets
        }
        val basePaddingBottom = btnRow.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(btnRow) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = basePaddingBottom + nav.bottom + dpToPx(22)); insets
        }

        listaJugadores       = intent.getParcelableArrayListExtra<Jugador>("LISTA_JUGADORES")?.toList().orEmpty()
        datosPartida         = intent.getParcelableArrayListExtra<DatoCurioso>("DATOS_PARTIDA")?.toList().orEmpty()
        palabraJugada        = intent.getStringExtra("PALABRA") ?: ""
        nombreImpostor       = intent.getStringExtra("IMPOSTOR") ?: ""
        nombresSenoresBlancos = intent.getStringExtra("SENORES_BLANCOS") ?: ""
        modoMisterioso        = intent.getBooleanExtra("MODO_MISTERIOSO", false)
        modoDatosCuriosos     = intent.getBooleanExtra("MODO_DATOS_CURIOSOS", false)
        tiempoLimitado        = intent.getBooleanExtra("TIEMPO_LIMITADO", false)
        minutos              = intent.getIntExtra("MINUTOS", 3)

        val nombreEmpieza = intent.getStringExtra("JUGADOR_EMPIEZA") ?: ""
        val jugadorHabla = if (nombreEmpieza.isNotBlank())
            listaJugadores.firstOrNull { it.nombre == nombreEmpieza }
        else listaJugadores.randomOrNull()
        txtHabla.text = if (jugadorHabla != null) "¡${jugadorHabla.nombre} hablas tú!" else "No hay jugadores disponibles"

        cardViewPalabra.visibility  = View.GONE
        cardResumen.visibility      = View.GONE
        cardSenorBlanco.visibility  = View.GONE
        btnRevelar.visibility       = View.VISIBLE

        // Timer
        if (tiempoLimitado) {
            txtTimer.visibility = View.VISIBLE
            startTimer(minutos * 60 * 1000L)
        } else {
            txtTimer.visibility = View.GONE
        }

        btnVotar.setOnClickListener { abrirVotos() }
        btnNewGame.setOnClickListener { pulsadoBotonNewGame() }
        btnRevelar.setOnClickListener { pulsadoBotonRevelar() }
        // Si viene de victoria, revelar directamente sin preguntar
        if (intent.getBooleanExtra("VICTORIA_INMEDIATA", false)) {
            txtHabla.visibility = View.GONE
            btnVotar.visibility = View.GONE
            btnRevelar.visibility = View.GONE
            btnNewGame.text = "NUEVA PARTIDA"
            cargarDatosRevelando()
        }
    }

    private fun startTimer(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000L) {
            override fun onTick(remaining: Long) {
                val m = remaining / 60000
                val s = (remaining % 60000) / 1000
                txtTimer.text = "⏱ %d:%02d".format(m, s)
                // Parpadea en rojo cuando quedan menos de 30 seg
                if (remaining < 30_000) {
                    txtTimer.setTextColor(if ((remaining / 1000) % 2 == 0L) Color.RED else Color.WHITE)
                }
            }
            override fun onFinish() {
                txtTimer.text = "⏱ 0:00"
                tiempoAgotado()
            }
        }.start()
    }

    private fun tiempoAgotado() {
        try { ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_PROP_NACK, 1000) } catch (_: Exception) {}
        val intent = Intent(this, VictoryActivity::class.java).apply {
            putExtra("GANADOR", "IMPOSTORES")
            putExtra("MOTIVO", "¡Se acabó el tiempo!")
            putExtra("IR_A_REVEAL", true)
            putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
            putParcelableArrayListExtra("DATOS_PARTIDA", ArrayList(datosPartida))
            putExtra("PALABRA", palabraJugada)
            putExtra("IMPOSTOR", nombreImpostor)
            putExtra("SENORES_BLANCOS", nombresSenoresBlancos)
            putExtra("MODO_DATOS_CURIOSOS", modoDatosCuriosos)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun abrirVotos() {
        val intent = Intent(this, VoteActivity::class.java).apply {
            putParcelableArrayListExtra("JUGADORES", ArrayList(listaJugadores))
            putExtra("PALABRA", palabraJugada)
            putExtra("IMPOSTOR", nombreImpostor)
            putExtra("SENORES_BLANCOS", nombresSenoresBlancos)
            putExtra("MODO_DATOS_CURIOSOS", modoDatosCuriosos)
        }
        startActivityForResult(intent, REQUEST_VOTE)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VOTE && resultCode == RESULT_OK) {
            val actualizados = data?.getParcelableArrayListExtra<Jugador>("JUGADORES_ACTUALIZADOS")
            if (actualizados != null) {
                listaJugadores = actualizados.toList()

                val noCiviles = listaJugadores.count {
                    it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO
                }
                val civiles = listaJugadores.count { it.tipo == TipoJugador.NORMAL }

                when {
                    noCiviles == 0 -> {
                        // No quedan impostores — civiles ganan
                        startActivity(Intent(this, VictoryActivity::class.java).apply {
                            putExtra("GANADOR", "CIVILES")
                            putExtra("MOTIVO", "¡Todos los impostores han sido eliminados!")
                            putExtra("IR_A_REVEAL", true)
                            putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
                            putParcelableArrayListExtra("DATOS_PARTIDA", ArrayList(datosPartida))
                            putExtra("PALABRA", palabraJugada)
                            putExtra("IMPOSTOR", nombreImpostor)
                            putExtra("SENORES_BLANCOS", nombresSenoresBlancos)
                            putExtra("MODO_DATOS_CURIOSOS", modoDatosCuriosos)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    noCiviles >= civiles -> {
                        // Impostores igualan o superan a civiles — impostores ganan
                        startActivity(Intent(this, VictoryActivity::class.java).apply {
                            putExtra("GANADOR", "IMPOSTORES")
                            putExtra("MOTIVO", "Los civiles están en minoria.")
                            putExtra("IR_A_REVEAL", true)
                            putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
                            putParcelableArrayListExtra("DATOS_PARTIDA", ArrayList(datosPartida))
                            putExtra("PALABRA", palabraJugada)
                            putExtra("IMPOSTOR", nombreImpostor)
                            putExtra("SENORES_BLANCOS", nombresSenoresBlancos)
                            putExtra("MODO_DATOS_CURIOSOS", modoDatosCuriosos)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    // Si no, la partida continúa normalmente
                }
            }
        }
    }
    companion object {
        const val REQUEST_VOTE = 2001
    }

    private fun pulsadoBotonNewGame() {
        GameDialog(this)
            .icon("🚪")
            .title("Salir")
            .message("\u00BFQuieres salir de la partida?")
            .cancelable(true)
            .positiveButton("Salir") {
                SelfieManager.clear()
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .negativeButton("Cancelar")
            .show()
    }

    private fun pulsadoBotonRevelar() {
        GameDialog(this)
            .icon("🔍")
            .title("Revelar impostor")
            .message("\u00BFQuieres revelar al impostor?")
            .cancelable(true)
            .positiveButton("Revelar") { cargarDatosRevelando() }
            .negativeButton("Cancelar")
            .show()
    }

    private fun cargarDatosRevelando() {
        txtHabla.visibility = View.GONE
        cardsContainer.visibility = View.VISIBLE
        countDownTimer?.cancel()
        txtTimer.visibility = View.GONE
        txtSubtitle.text = getString(R.string.play_subtitle_reveal)
        txtFooter.text = getString(R.string.play_footer_reveal)
        if (!impostorContado) {
            // nombreImpostor puede ser "Juan, Pedro" cuando hay varios impostores.
            // Dividimos por coma para incrementar el contador de cada uno por separado.
            nombreImpostor.split(",").forEach { playerViewModel.incrementImpostorByName(it.trim()) }
            impostorContado = true
        }

        val colorImpostor = ContextCompat.getColor(this, R.color.colorImpostor)
        val colorPalabra  = ContextCompat.getColor(this, R.color.colorPalabra)

        if (nombreImpostor.isNotBlank()) {
            cardResumen.visibility = View.VISIBLE
            val s = SpannableString(nombreImpostor)
            s.setSpan(ForegroundColorSpan(colorImpostor), 0, nombreImpostor.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            s.setSpan(StyleSpan(Typeface.BOLD), 0, nombreImpostor.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtImpostorNombre.text = s
        }

        if (nombresSenoresBlancos.isNotBlank()) {
            cardSenorBlanco.visibility = View.VISIBLE
            val s = SpannableString(nombresSenoresBlancos)
            s.setSpan(ForegroundColorSpan(colorImpostor), 0, nombresSenoresBlancos.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            s.setSpan(StyleSpan(Typeface.BOLD), 0, nombresSenoresBlancos.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtSenorBlancoNombre.text = s
        }

        cardViewPalabra.visibility = View.VISIBLE
        configurarTarjetaFinal(colorPalabra)

        ThemeManager.aplicarDrawables(this)
        btnRevelar.visibility = View.GONE
        btnVotar.visibility   = View.GONE
        iniciarAnimacionFinalPeriodica()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun configurarTarjetaFinal(colorPalabra: Int) {
        scrollPalabra.scrollTo(0, 0)
        val params = scrollPalabra.layoutParams

        if (modoDatosCuriosos) {
            txtLabelPalabra.text = getString(R.string.play_label_facts_reveal)
            params.height = dpToPx(240)
            scrollPalabra.layoutParams = params

            txtPalabra.gravity = Gravity.START
            txtPalabra.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            txtPalabra.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
            txtPalabra.setLineSpacing(0f, 1.2f)
            txtPalabra.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            txtPalabra.text = construirResumenDatosPartida()
        } else {
            txtLabelPalabra.text = getString(R.string.play_label_word_reveal)
            params.height = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            scrollPalabra.layoutParams = params

            val sp = SpannableString(palabraJugada)
            sp.setSpan(ForegroundColorSpan(colorPalabra), 0, palabraJugada.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sp.setSpan(StyleSpan(Typeface.BOLD), 0, palabraJugada.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtPalabra.gravity = Gravity.START
            txtPalabra.setLineSpacing(0f, 1f)
            txtPalabra.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            txtPalabra.text = sp
        }
    }

    private fun construirResumenDatosPartida(): String {
        if (datosPartida.isEmpty()) return getString(R.string.play_facts_unavailable)
        return datosPartida.mapIndexed { index, dato ->
            "${index + 1}. ${getTextoDato(dato)}"
        }.joinToString("\n\n")
    }

    private fun getTextoDato(dato: DatoCurioso): String = when (LocaleManager.getLanguage(this)) {
        "en" -> dato.en
        "zh-Hans" -> dato.zhHans
        "zh-Hant" -> dato.zhHant
        else -> dato.es
    }

    private fun iniciarAnimacionFinalPeriodica() {
        val invitado = lottieFinalInvitado ?: return
        if (!ThemeManager.esFinal(this)) return

        animacionFinalSeleccionada = animacionFinalSeleccionada ?: animacionesFinales.random()
        invitado.removeCallbacks(repetirAnimacionFinal)
        animarInvitadoFinal()
    }

    private fun detenerAnimacionFinalPeriodica() {
        val invitado = lottieFinalInvitado ?: return
        invitado.removeCallbacks(repetirAnimacionFinal)
        invitado.cancelAnimation()
        invitado.animate().cancel()
        invitado.visibility = View.GONE
    }

    private fun animarInvitadoFinal() {
        val invitado = lottieFinalInvitado ?: return
        if (!ThemeManager.esFinal(this)) return

        invitado.cancelAnimation()
        invitado.animate().cancel()
        invitado.clearAnimation()
        invitado.progress = 0f
        invitado.speed = 0.8f
        invitado.repeatCount = LottieDrawable.INFINITE
        invitado.setAnimation(animacionFinalSeleccionada ?: animacionesFinales.random())
        invitado.setFailureListener {
            invitado.cancelAnimation()
            invitado.visibility = View.GONE
        }

        invitado.post {
            val rootWidth = findViewById<View>(R.id.main).width.toFloat()
            val invitadoWidth = invitado.width.toFloat().takeIf { it > 0f } ?: dpToPx(220).toFloat()
            val inicioX = -(invitadoWidth + dpToPx(42))
            val puntoMedio = rootWidth * 0.2f
            val salidaX = rootWidth + invitadoWidth + dpToPx(36)

            invitado.visibility = View.VISIBLE
            invitado.alpha = 0f
            invitado.translationX = inicioX
            invitado.translationY = dpToPx(10).toFloat()
            invitado.scaleX = 1f
            invitado.scaleY = 1f
            invitado.rotation = 0f
            invitado.playAnimation()

            val entrada = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(invitado, View.TRANSLATION_X, invitado.translationX, puntoMedio),
                    ObjectAnimator.ofFloat(invitado, View.TRANSLATION_Y, invitado.translationY, 0f),
                    ObjectAnimator.ofFloat(invitado, View.ALPHA, 0f, 0.98f)
                )
                duration = 1300L
                interpolator = AccelerateDecelerateInterpolator()
            }

            val paseo = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(invitado, View.TRANSLATION_X, puntoMedio, rootWidth * 0.56f),
                    ObjectAnimator.ofFloat(invitado, View.TRANSLATION_Y, 0f, -dpToPx(6).toFloat(), 0f)
                )
                duration = 3600L
                interpolator = AccelerateDecelerateInterpolator()
            }

            val salida = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(invitado, View.TRANSLATION_X, rootWidth * 0.56f, salidaX.toFloat()),
                    ObjectAnimator.ofFloat(invitado, View.ALPHA, 0.98f, 0f)
                )
                duration = 1700L
                interpolator = AccelerateDecelerateInterpolator()
            }

            AnimatorSet().apply {
                playSequentially(entrada, paseo, salida)
                startDelay = 260L
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        invitado.cancelAnimation()
                        invitado.visibility = View.GONE
                        invitado.translationX = 0f
                        invitado.translationY = 0f
                        invitado.alpha = 1f
                        invitado.removeCallbacks(repetirAnimacionFinal)
                        invitado.postDelayed(
                            repetirAnimacionFinal,
                            (intervaloAnimacionFinalMs - duracionAnimacionFinalMs).coerceAtLeast(0L)
                        )
                    }
                })
                start()
            }
        }
    }

    private fun mensajeAlerta(titulo: String, msg: String) {
        GameDialog(this)
            .icon("🚪")
            .title(titulo)
            .message(msg)
            .positiveButton("OK") { stopBell() }
            .show()
    }

    private fun stopBell() { mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null }

    override fun onStart() {
        super.onStart()
        if (ThemeManager.esFinal(this) && ::cardsContainer.isInitialized && cardsContainer.visibility == View.VISIBLE) {
            iniciarAnimacionFinalPeriodica()
        }
    }

    override fun onStop() {
        super.onStop()
        detenerAnimacionFinalPeriodica()
        stopBell()
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerAnimacionFinalPeriodica()
        countDownTimer?.cancel()
    }
}
