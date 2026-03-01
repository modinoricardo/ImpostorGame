package com.example.impostorgame.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.impostorgame.R
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
import com.example.impostorgame.PlayerViewModel
import com.example.impostorgame.ThemeManager
import com.example.impostorgame.modelos.Jugador
import android.media.MediaPlayer
import com.example.impostorgame.OptionMain
import kotlin.random.Random

class PlayGameActivity : AppCompatActivity() {
    private lateinit var btnNewGame: Button
    private lateinit var txtHabla: TextView
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
    private val playerViewModel: PlayerViewModel by viewModels()
    private var impostorContado = false
    private var mediaPlayer: MediaPlayer? = null
    private val startSoundDelayMs = Random.nextLong(60_000L, 65_001L)

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_game)
        ThemeManager.aplicarDrawables(this)

        btnNewGame        = findViewById(R.id.btnNewGame)
        txtHabla          = findViewById(R.id.txtImpostor)
        btnRevelar        = findViewById(R.id.btnRevelar)
        cardViewPalabra   = findViewById(R.id.cardViewPalabra)
        cardResumen       = findViewById(R.id.cardResumen)
        cardSenorBlanco   = findViewById(R.id.cardSenorBlanco)
        txtPalabra        = findViewById(R.id.txtPalabra)
        txtImpostorNombre = findViewById(R.id.txtImpostorNombre)
        txtSenorBlancoNombre = findViewById(R.id.txtSenorBlancoNombre)

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

        loadEvents()

        listaJugadores = intent.getParcelableArrayListExtra<Jugador>("LISTA_JUGADORES")?.toList().orEmpty()
        val jugadorHabla = listaJugadores.randomOrNull()
        txtHabla.text = if (jugadorHabla != null) "¡${jugadorHabla.nombre} hablas tú!" else "No hay jugadores disponibles"

        palabraJugada        = intent.getStringExtra("PALABRA") ?: ""
        nombreImpostor       = intent.getStringExtra("IMPOSTOR") ?: ""
        nombresSenoresBlancos = intent.getStringExtra("SENORES_BLANCOS") ?: ""
        modoMisterioso       = intent.getBooleanExtra("MODO_MISTERIOSO", false)

        cardViewPalabra.visibility  = View.GONE
        cardResumen.visibility      = View.GONE
        cardSenorBlanco.visibility  = View.GONE
        btnRevelar.visibility       = View.VISIBLE

        if (OptionMain.tiempoLimitado) window.decorView.postDelayed(startSoundRunnable, startSoundDelayMs)
    }

    private fun loadEvents() {
        btnNewGame.setOnClickListener { pulsadoBotonNewGame() }
        btnRevelar.setOnClickListener { pulsadoBotonRevelar() }
    }

    private val startSoundRunnable = Runnable {
        startBell()
        mensajeAlerta("Jugador eliminado", "Quien fue el ultimo en poner la mano sobre la mesa")
    }

    private fun startBell() {
        stopBell()
        val mp = MediaPlayer.create(this, R.raw.campana) ?: return
        mediaPlayer = mp.apply {
            isLooping = true
            setOnErrorListener { _, what, extra -> android.util.Log.e("PlayGameActivity", "Error what=$what extra=$extra"); stopBell(); true }
            start()
        }
    }

    private fun stopBell() { mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null }

    private fun pulsadoBotonNewGame() {
        AlertDialog.Builder(this).setTitle("Salir").setMessage("¿Quieres salir de la partida?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salir") { _, _ -> finish() }.show()
    }

    private fun pulsadoBotonRevelar() {
        AlertDialog.Builder(this).setTitle("Revelar impostor").setMessage("¿Quieres revelar al impostor?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Revelar") { _, _ -> cargarDatosRevelando() }.show()
    }

    private fun cargarDatosRevelando() {
        if (!impostorContado) { playerViewModel.incrementImpostorByName(nombreImpostor); impostorContado = true }

        val colorImpostor = ContextCompat.getColor(this, R.color.colorImpostor)
        val colorPalabra  = ContextCompat.getColor(this, R.color.colorPalabra)

        // ── Card Impostor ──
        if (nombreImpostor.isNotBlank()) {
            cardResumen.visibility = View.VISIBLE
            val spannable = SpannableString(nombreImpostor)
            spannable.setSpan(ForegroundColorSpan(colorImpostor), 0, nombreImpostor.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), 0, nombreImpostor.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtImpostorNombre.text = spannable
        }

        // ── Card Señor Blanco ──
        if (nombresSenoresBlancos.isNotBlank()) {
            cardSenorBlanco.visibility = View.VISIBLE
            val spannable = SpannableString(nombresSenoresBlancos)
            spannable.setSpan(ForegroundColorSpan(colorImpostor), 0, nombresSenoresBlancos.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), 0, nombresSenoresBlancos.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtSenorBlancoNombre.text = spannable
        }

        // ── Card Palabra ──
        cardViewPalabra.visibility = View.VISIBLE
        val spannablePalabra = SpannableString(palabraJugada)
        spannablePalabra.setSpan(ForegroundColorSpan(colorPalabra), 0, palabraJugada.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannablePalabra.setSpan(StyleSpan(Typeface.BOLD), 0, palabraJugada.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtPalabra.text = spannablePalabra

        ThemeManager.aplicarDrawables(this)
        btnRevelar.visibility = View.GONE
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun mensajeAlerta(titulo: String, msg: String) {
        AlertDialog.Builder(this).setTitle(titulo).setMessage(msg)
            .setPositiveButton("OK") { _, _ -> stopBell() }
            .setOnDismissListener { stopBell() }.show()
    }

    override fun onStop() { super.onStop(); stopBell() }
}