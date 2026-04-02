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
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.ricardomodino.impostorgame.viewmodel.PlayerViewModel
import com.ricardomodino.impostorgame.viewmodel.PlayGameViewModel
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.extensions.applyWordSafeText
import com.ricardomodino.impostorgame.managers.*
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.Jugador

class PlayGameActivity : BaseGameActivity() {

    private lateinit var btnNewGame: Button
    private lateinit var btnVotar: Button
    private lateinit var txtHabla: TextView
    private lateinit var txtTimer: TextView
    private lateinit var txtSubtitle: TextView
    private lateinit var txtFooter: TextView
    private lateinit var txtLabelPalabra: TextView
    private lateinit var btnRevelar: Button
    private lateinit var cardViewPalabra: CardView
    private lateinit var cardResumen: CardView
    private lateinit var cardSenorBlanco: CardView
    private lateinit var scrollPalabra: NestedScrollView
    private lateinit var txtPalabra: TextView
    private lateinit var txtImpostorNombre: TextView
    private lateinit var txtSenorBlancoNombre: TextView
    private val playerViewModel: PlayerViewModel by viewModels()
    private val gameViewModel: PlayGameViewModel by viewModels()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(
            when {
                ThemeManager.esFinal(this) -> R.layout.activity_play_game_final
                ThemeManager.esCarmesi(this) -> R.layout.activity_play_game_carmesi
                else -> R.layout.activity_play_game
            }
        )
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

        configurarBackPressed { SelfieManager.clear(); finish() }

        val root   = findViewById<View>(R.id.main)
        val btnRow = findViewById<View>(R.id.btnRow)

        ImmersiveModeManager.applyRootInsets(root)
        ImmersiveModeManager.applyRootInsets(btnRow, includeBottomInset = true, extraBottom = dpToPx(22))

        gameViewModel.cargarDesdeIntent(
            jugadores             = intent.getParcelableArrayListExtra<Jugador>(IntentKeys.LISTA_JUGADORES)?.toList().orEmpty(),
            datosPartida          = intent.getParcelableArrayListExtra<DatoCurioso>(IntentKeys.DATOS_PARTIDA)?.toList().orEmpty(),
            palabraJugada         = intent.getStringExtra(IntentKeys.PALABRA) ?: "",
            nombreImpostor        = intent.getStringExtra(IntentKeys.IMPOSTOR) ?: "",
            nombresSenoresBlancos = intent.getStringExtra(IntentKeys.SENORES_BLANCOS) ?: "",
            modoMisterioso        = intent.getBooleanExtra(IntentKeys.MODO_MISTERIOSO, false),
            modoDatosCuriosos     = intent.getBooleanExtra(IntentKeys.MODO_DATOS_CURIOSOS, false),
            tiempoLimitado        = intent.getBooleanExtra(IntentKeys.TIEMPO_LIMITADO, false),
            minutos               = intent.getIntExtra(IntentKeys.MINUTOS, 3)
        )

        if (gameViewModel.jugadorEmpiezaNombre.isEmpty()) {
            val nombreEmpieza = intent.getStringExtra(IntentKeys.JUGADOR_EMPIEZA) ?: ""
            val jugadorHabla = if (nombreEmpieza.isNotBlank())
                gameViewModel.listaJugadores.firstOrNull { it.nombre == nombreEmpieza }
            else gameViewModel.listaJugadores.randomOrNull()
            gameViewModel.jugadorEmpiezaNombre = jugadorHabla?.nombre ?: ""
        }
        txtHabla.text = if (gameViewModel.jugadorEmpiezaNombre.isNotBlank())
            getString(R.string.play_turn_speaks, gameViewModel.jugadorEmpiezaNombre)
        else getString(R.string.play_no_players_available)

        val victoriaInmediata = intent.getBooleanExtra(IntentKeys.VICTORIA_INMEDIATA, false)

        btnVotar.setOnClickListener { abrirVotos() }
        btnNewGame.setOnClickListener { pulsadoBotonNewGame() }
        btnRevelar.setOnClickListener { pulsadoBotonRevelar() }

        if (gameViewModel.revelado) {
            if (victoriaInmediata) btnNewGame.text = getString(R.string.play_btn_nueva_partida)
            cargarDatosRevelando()
        } else {
            cardViewPalabra.visibility = View.GONE
            cardResumen.visibility     = View.GONE
            cardSenorBlanco.visibility = View.GONE
            btnRevelar.visibility      = View.VISIBLE

            if (gameViewModel.tiempoLimitado) {
                txtTimer.visibility = View.VISIBLE
                val duracion = if (gameViewModel.tiempoRestanteMs > 0L) gameViewModel.tiempoRestanteMs
                               else gameViewModel.minutos * 60 * 1000L
                startTimer(duracion)
            } else {
                txtTimer.visibility = View.GONE
            }

            if (victoriaInmediata) {
                txtHabla.visibility = View.GONE
                btnVotar.visibility = View.GONE
                btnRevelar.visibility = View.GONE
                btnNewGame.text = getString(R.string.play_btn_nueva_partida)
                cargarDatosRevelando()
            }
        }
    }

    private fun startTimer(millis: Long) {
        countDownTimer?.cancel()
        gameViewModel.timerActivo = true
        countDownTimer = object : CountDownTimer(millis, 1000L) {
            override fun onTick(remaining: Long) {
                gameViewModel.tiempoRestanteMs = remaining
                val m = remaining / 60000
                val s = (remaining % 60000) / 1000
                txtTimer.text = getString(R.string.play_timer_format, m, s)
                if (remaining < 30_000) {
                    txtTimer.setTextColor(if ((remaining / 1000) % 2 == 0L) Color.RED else Color.WHITE)
                }
            }
            override fun onFinish() {
                gameViewModel.timerActivo = false
                gameViewModel.tiempoRestanteMs = 0L
                txtTimer.text = getString(R.string.play_timer_zero)
                tiempoAgotado()
            }
        }.start()
    }

    private fun tiempoAgotado() {
        try { ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_PROP_NACK, 1000) } catch (_: Exception) {}
        startActivity(intentVictoria("IMPOSTORES", getString(R.string.play_time_up_reason)))
        finish()
    }

    private fun abrirVotos() {
        val intent = Intent(this, VoteActivity::class.java).apply {
            putParcelableArrayListExtra(IntentKeys.JUGADORES, ArrayList(gameViewModel.listaJugadores))
            putExtra(IntentKeys.PALABRA, gameViewModel.palabraJugada)
            putExtra(IntentKeys.IMPOSTOR, gameViewModel.nombreImpostor)
            putExtra(IntentKeys.SENORES_BLANCOS, gameViewModel.nombresSenoresBlancos)
            putExtra(IntentKeys.MODO_DATOS_CURIOSOS, gameViewModel.modoDatosCuriosos)
        }
        startActivityForResult(intent, REQUEST_VOTE)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_VOTE || resultCode != RESULT_OK) return
        val actualizados = data?.getParcelableArrayListExtra<Jugador>(IntentKeys.JUGADORES_ACTUALIZADOS) ?: return
        when (gameViewModel.evaluarVotos(actualizados.toList())) {
            PlayGameViewModel.ResultadoVoto.CivilesGanan -> startActivity(
                intentVictoria("CIVILES", getString(R.string.play_civilians_win_reason))
            )
            PlayGameViewModel.ResultadoVoto.ImpostoresGanan -> startActivity(
                intentVictoria("IMPOSTORES", getString(R.string.play_impostors_majority_reason))
            )
            PlayGameViewModel.ResultadoVoto.ContinuaPartida -> { }
        }
    }

    private fun intentVictoria(ganador: String, motivo: String) =
        Intent(this, VictoryActivity::class.java).apply {
            putExtra(IntentKeys.GANADOR, ganador)
            putExtra(IntentKeys.MOTIVO, motivo)
            putExtra(IntentKeys.IR_A_REVEAL, true)
            putParcelableArrayListExtra(IntentKeys.LISTA_JUGADORES, ArrayList(gameViewModel.listaJugadores))
            putParcelableArrayListExtra(IntentKeys.DATOS_PARTIDA, ArrayList(gameViewModel.datosPartida))
            putExtra(IntentKeys.PALABRA, gameViewModel.palabraJugada)
            putExtra(IntentKeys.IMPOSTOR, gameViewModel.nombreImpostor)
            putExtra(IntentKeys.SENORES_BLANCOS, gameViewModel.nombresSenoresBlancos)
            putExtra(IntentKeys.MODO_DATOS_CURIOSOS, gameViewModel.modoDatosCuriosos)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    companion object {
        const val REQUEST_VOTE = 2001
    }

    private fun pulsadoBotonNewGame() {
        GameDialog(this)
            .icon("\uD83D\uDEAA")
            .title(getString(R.string.dialog_salir_titulo))
            .message(getString(R.string.dialog_salir_msg))
            .cancelable(true)
            .positiveButton(getString(R.string.dialog_salir_si)) {
                SelfieManager.clear()
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .negativeButton(getString(R.string.dialog_salir_no))
            .show()
    }

    private fun pulsadoBotonRevelar() {
        GameDialog(this)
            .icon("\uD83D\uDD0D")
            .title(getString(R.string.dialog_reveal_impostor_title))
            .message(getString(R.string.dialog_reveal_impostor_msg))
            .cancelable(true)
            .positiveButton(getString(R.string.dialog_reveal)) { cargarDatosRevelando() }
            .negativeButton(getString(R.string.dialog_salir_no))
            .show()
    }

    private fun cargarDatosRevelando() {
        gameViewModel.revelado = true
        txtHabla.visibility = View.GONE
        cardsContainer.visibility = View.VISIBLE
        countDownTimer?.cancel()
        gameViewModel.timerActivo = false
        txtTimer.visibility = View.GONE
        txtSubtitle.text = getString(R.string.play_subtitle_reveal)
        txtFooter.text = getString(R.string.play_footer_reveal)
        if (gameViewModel.debeContarImpostor()) {
            gameViewModel.nombreImpostor.split(",")
                .forEach { playerViewModel.incrementImpostorByName(it.trim()) }
        }
        val colorImpostor = ContextCompat.getColor(this, R.color.colorImpostor)
        val colorPalabra  = ContextCompat.getColor(this, R.color.colorPalabra)
        if (gameViewModel.nombreImpostor.isNotBlank()) {
            cardResumen.visibility = View.VISIBLE
            txtImpostorNombre.setTextColor(colorImpostor)
            txtImpostorNombre.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            txtImpostorNombre.text = gameViewModel.nombreImpostor
        }
        if (gameViewModel.nombresSenoresBlancos.isNotBlank()) {
            cardSenorBlanco.visibility = View.VISIBLE
            txtSenorBlancoNombre.setTextColor(colorImpostor)
            txtSenorBlancoNombre.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            txtSenorBlancoNombre.text = gameViewModel.nombresSenoresBlancos
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
        if (gameViewModel.modoDatosCuriosos) {
            txtLabelPalabra.text = getString(R.string.play_label_facts_reveal)
            params.height = dpToPx(240)
            scrollPalabra.layoutParams = params
            txtPalabra.gravity = Gravity.START
            txtPalabra.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            txtPalabra.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
            txtPalabra.setLineSpacing(0f, 1.2f)
            txtPalabra.applyWordSafeText(
                rawText = construirResumenDatosPartida(),
                preferSingleLine = false,
                maxTextSp = 15f,
                minTextSp = 13f
            )
        } else {
            txtLabelPalabra.text = getString(R.string.play_label_word_reveal)
            params.height = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            scrollPalabra.layoutParams = params
            txtPalabra.gravity = Gravity.START
            txtPalabra.setTextColor(colorPalabra)
            txtPalabra.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            txtPalabra.setLineSpacing(0f, 1f)
            txtPalabra.applyWordSafeText(
                rawText = gameViewModel.palabraJugada,
                preferSingleLine = true,
                maxTextSp = 28f,
                minTextSp = 12f,
                preferredSingleLineMinSp = 11f,
                preferredWrappedMaxLines = 3
            )
        }
    }

    private fun construirResumenDatosPartida(): String {
        if (gameViewModel.datosPartida.isEmpty()) return getString(R.string.play_facts_unavailable)
        return gameViewModel.datosPartida.mapIndexed { index, dato ->
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
            invitado.bringToFront()
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
