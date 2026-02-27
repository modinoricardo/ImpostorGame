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
    private lateinit var txtResumenTitulo: TextView
    private lateinit var listaJugadores: List<Jugador>
    private lateinit var palabraJugada: String
    private lateinit var btnRevelar: Button
    private lateinit var cardViewPalabra: CardView
    private lateinit var txtPalabra: TextView
    private lateinit var nombreImpostor: String
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

        btnNewGame = findViewById(R.id.btnNewGame)
        txtResumenTitulo = findViewById(R.id.txtImpostor)
        btnRevelar = findViewById(R.id.btnRevelar)
        cardViewPalabra = findViewById(R.id.cardViewPalabra)
        txtPalabra = findViewById(R.id.txtPalabra)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@PlayGameActivity)
                    .setTitle("Salir")
                    .setMessage("¿Quieres salir de la partida?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ -> finish() }
                    .show()
            }
        })

        val root = findViewById<View>(R.id.main)
        val btnRow = findViewById<View>(R.id.btnRow)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val topInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout())
            val sideInsets = insets.getInsets(WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = sideInsets.left, top = topInsets.top, right = sideInsets.right)
            insets
        }

        val basePaddingBottom = btnRow.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(btnRow) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = basePaddingBottom + nav.bottom + dpToPx(22))
            insets
        }

        loadEvents()

        listaJugadores = intent.getParcelableArrayListExtra<Jugador>("LISTA_JUGADORES")?.toList().orEmpty()
        val jugadorHabla = listaJugadores.randomOrNull()
        txtResumenTitulo.text = if (jugadorHabla != null) "¡${jugadorHabla.nombre} hablas tu!" else "No hay jugadores disponibles"

        palabraJugada = intent.getStringExtra("PALABRA") ?: ""
        nombreImpostor = intent.getStringExtra("IMPOSTOR") ?: ""

        cardViewPalabra.visibility = View.GONE
        btnRevelar.visibility = View.VISIBLE

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
        val mp = MediaPlayer.create(this, R.raw.campana)
        if (mp == null) { android.util.Log.e("PlayGameActivity", "No se pudo crear MediaPlayer"); return }
        mediaPlayer = mp.apply {
            isLooping = true
            setOnErrorListener { _, what, extra -> android.util.Log.e("PlayGameActivity", "Error what=$what extra=$extra"); stopBell(); true }
            start()
        }
    }

    private fun stopBell() { mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null }

    private fun pulsadoBotonNewGame() {
        AlertDialog.Builder(this@PlayGameActivity)
            .setTitle("Salir").setMessage("¿Quieres salir de la partida?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salir") { _, _ -> finish() }
            .show()
    }

    private fun pulsadoBotonRevelar() {
        AlertDialog.Builder(this@PlayGameActivity)
            .setTitle("Revelar impostor").setMessage("¿Quieres revelar al impostor?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Revelar") { _, _ -> cargarDatosRevelando() }
            .show()
    }

    private fun cargarDatosRevelando() {
        if (!impostorContado) { playerViewModel.incrementImpostorByName(nombreImpostor); impostorContado = true }

        cardViewPalabra.visibility = View.VISIBLE
        // Reaplicar tema al card recién visible
        ThemeManager.aplicarDrawables(this)

        val colorImpostor = ContextCompat.getColor(this, R.color.colorImpostor)
        val colorPalabra  = ContextCompat.getColor(this, R.color.colorPalabra)

        val textoImpostor = "El impostor era: $nombreImpostor"
        val spannableImpostor = SpannableString(textoImpostor)
        val startImp = textoImpostor.indexOf(nombreImpostor)
        val endImp = startImp + nombreImpostor.length
        spannableImpostor.setSpan(ForegroundColorSpan(colorImpostor), startImp, endImp, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableImpostor.setSpan(StyleSpan(Typeface.BOLD), startImp, endImp, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtResumenTitulo.text = spannableImpostor

        val textoPalabra = "La palabra era: $palabraJugada"
        val spannablePalabra = SpannableString(textoPalabra)
        val startPal = textoPalabra.indexOf(palabraJugada)
        val endPal = startPal + palabraJugada.length
        spannablePalabra.setSpan(ForegroundColorSpan(colorPalabra), startPal, endPal, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannablePalabra.setSpan(StyleSpan(Typeface.BOLD), startPal, endPal, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtPalabra.text = spannablePalabra

        btnRevelar.visibility = View.GONE
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun mensajeAlerta(titulo: String, msg: String) {
        AlertDialog.Builder(this).setTitle(titulo).setMessage(msg)
            .setPositiveButton("OK") { _, _ -> stopBell() }
            .setOnDismissListener { stopBell() }
            .show()
    }

    override fun onStop() { super.onStop(); stopBell() }
}