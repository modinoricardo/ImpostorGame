package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.graphics.Color
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.ricardomodino.impostorgame.R
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.activity.viewModels
import com.ricardomodino.impostorgame.PlayerViewModel
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Jugador
import android.media.MediaPlayer
import com.ricardomodino.impostorgame.modelos.TipoJugador

class PlayGameActivity : AppCompatActivity() {

    private lateinit var btnNewGame: Button
    private lateinit var btnVotar: Button
    private lateinit var txtHabla: TextView
    private lateinit var txtTimer: TextView
    private lateinit var listaJugadores: List<Jugador>
    private lateinit var palabraJugada: String
    private lateinit var btnRevelar: Button
    private lateinit var cardViewPalabra: CardView
    private lateinit var cardResumen: CardView
    private lateinit var cardSenorBlanco: CardView
    private lateinit var txtPalabra: TextView
    private lateinit var txtImpostorNombre: TextView
    private lateinit var txtSenorBlancoNombre: TextView
    private lateinit var nombreImpostor: String
    private lateinit var nombresSenoresBlancos: String
    private var modoMisterioso: Boolean = false
    private var tiempoLimitado: Boolean = false
    private var minutos: Int = 3
    private val playerViewModel: PlayerViewModel by viewModels()
    private var impostorContado = false
    private var mediaPlayer: MediaPlayer? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var cardsContainer: android.widget.LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_game)
        ThemeManager.aplicarDrawables(this)

        btnNewGame           = findViewById(R.id.btnNewGame)
        btnVotar             = findViewById(R.id.btnVotar)
        txtHabla             = findViewById(R.id.txtImpostor)
        txtTimer             = findViewById(R.id.txtTimer)
        btnRevelar           = findViewById(R.id.btnRevelar)
        cardViewPalabra      = findViewById(R.id.cardViewPalabra)
        cardResumen          = findViewById(R.id.cardResumen)
        cardSenorBlanco      = findViewById(R.id.cardSenorBlanco)
        txtPalabra           = findViewById(R.id.txtPalabra)
        txtImpostorNombre    = findViewById(R.id.txtImpostorNombre)
        txtSenorBlancoNombre = findViewById(R.id.txtSenorBlancoNombre)
        cardsContainer = findViewById(R.id.cardsContainer)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@PlayGameActivity)
                    .setTitle("Salir").setMessage("¿Quieres salir de la partida?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ -> finish() }.show()
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
        palabraJugada        = intent.getStringExtra("PALABRA") ?: ""
        nombreImpostor       = intent.getStringExtra("IMPOSTOR") ?: ""
        nombresSenoresBlancos = intent.getStringExtra("SENORES_BLANCOS") ?: ""
        modoMisterioso       = intent.getBooleanExtra("MODO_MISTERIOSO", false)
        tiempoLimitado       = intent.getBooleanExtra("TIEMPO_LIMITADO", false)
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
            putExtra("GANADOR", "CIVILES")
            putExtra("MOTIVO", "¡Todos los impostores han sido eliminados!")
            putExtra("IR_A_REVEAL", true)
            putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
            putParcelableArrayListExtra("LISTA_CATEGORIAS", ArrayList(listaJugadores)) // pasa tus categorias
            putExtra("PALABRA", palabraJugada)
            putExtra("IMPOSTOR", nombreImpostor)
            putExtra("SENORES_BLANCOS", nombresSenoresBlancos)
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
                            putExtra("PALABRA", palabraJugada)
                            putExtra("IMPOSTOR", nombreImpostor)
                            putExtra("SENORES_BLANCOS", nombresSenoresBlancos)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    noCiviles >= civiles -> {
                        // Impostores igualan o superan a civiles — impostores ganan
                        startActivity(Intent(this, VictoryActivity::class.java).apply {
                            putExtra("GANADOR", "IMPOSTORES")
                            putExtra("MOTIVO", "Los civiles estan en minoria.")
                            putExtra("IR_A_REVEAL", true)
                            putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
                            putExtra("PALABRA", palabraJugada)
                            putExtra("IMPOSTOR", nombreImpostor)
                            putExtra("SENORES_BLANCOS", nombresSenoresBlancos)
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
        AlertDialog.Builder(this).setTitle("Salir").setMessage("¿Quieres salir de la partida?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salir") { _, _ -> startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }) }.show()
    }

    private fun pulsadoBotonRevelar() {
        AlertDialog.Builder(this).setTitle("Revelar impostor").setMessage("¿Quieres revelar al impostor?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Revelar") { _, _ -> cargarDatosRevelando() }.show()
    }

    private fun cargarDatosRevelando() {
        txtHabla.visibility = View.GONE
        cardsContainer.visibility = View.VISIBLE
        countDownTimer?.cancel()
        if (!impostorContado) { playerViewModel.incrementImpostorByName(nombreImpostor); impostorContado = true }

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
        val sp = SpannableString(palabraJugada)
        sp.setSpan(ForegroundColorSpan(colorPalabra), 0, palabraJugada.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sp.setSpan(StyleSpan(Typeface.BOLD), 0, palabraJugada.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtPalabra.text = sp

        ThemeManager.aplicarDrawables(this)
        btnRevelar.visibility = View.GONE
        btnVotar.visibility   = View.GONE
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun mensajeAlerta(titulo: String, msg: String) {
        AlertDialog.Builder(this).setTitle(titulo).setMessage(msg)
            .setPositiveButton("OK") { _, _ -> stopBell() }
            .setOnDismissListener { stopBell() }.show()
    }

    private fun stopBell() { mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null }

    override fun onStop() { super.onStop(); stopBell() }
    override fun onDestroy() { super.onDestroy(); countDownTimer?.cancel() }
}