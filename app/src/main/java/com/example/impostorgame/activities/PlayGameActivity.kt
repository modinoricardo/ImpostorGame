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

class PlayGameActivity : AppCompatActivity() {

    private lateinit var btnNewGame: Button
    private lateinit var txtResumenTitulo: TextView
    private lateinit var listaJugadores: List<String>;
    private lateinit var palabraJugada: String
    private lateinit var btnRevelar: Button
    private lateinit var cardViewPalabra: CardView
    private lateinit var txtPalabra: TextView
    private lateinit var nombreImpostor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_game)

        //Todas las declaraciones
        btnNewGame = findViewById(R.id.btnNewGame)
        txtResumenTitulo = findViewById(R.id.txtImpostor)
        btnRevelar = findViewById(R.id.btnRevelar)
        cardViewPalabra = findViewById(R.id.cardViewPalabra)
        txtPalabra = findViewById(R.id.txtPalabra)

        //Cuando el usuario de hacia atras en la barra de navegacion
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@PlayGameActivity)
                    .setTitle("Salir")
                    .setMessage("¿Quieres salir de la partida?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ ->
                        // Volver atrás a la MainActivity que ya está debajo
                        finish()
                    }
                    .show()
            }
        })

        val root = findViewById<View>(R.id.main)
        val btnRow = findViewById<View>(R.id.btnRow)

        // Arriba (status bar + notch) y laterales
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val topInsets = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val sideInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(left = sideInsets.left, top = topInsets.top, right = sideInsets.right)
            insets
        }

        // Abajo (navigation bar) sumado al padding que ya tengas
        val basePaddingBottom = btnRow.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(btnRow) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = basePaddingBottom + nav.bottom + dpToPx(22))
            insets
        }

        //La logica comienza aqui

        //Cargamos los eventos
        loadEvents()

        // Carga de jugadores desde el intent
        listaJugadores = intent.getStringArrayListExtra("LISTA_JUGADORES")?.toList().orEmpty()

        // Elegir un jugador aleatorio (si la lista no está vacía)
        val jugadorHabla = listaJugadores.randomOrNull()

        // Texto del resumen
        txtResumenTitulo.text = if (jugadorHabla != null) {
            "¡$jugadorHabla " +
                    "hablas tú!"
        } else {
            "No hay jugadores disponibles"
        }

        // Carga palabra jugada desde el intent
        palabraJugada = intent.getStringExtra("PALABRA") ?: ""
        //Cargamos el nombre del impostor desde el intent
        nombreImpostor = intent.getStringExtra("IMPOSTOR") ?: ""

        //Ocultamos el segundo cardView hasta que revelamos
        cardViewPalabra.visibility = View.GONE

        //Mostramos el boton de revelar impostor
        btnRevelar.visibility = View.VISIBLE
    }

    private fun loadEvents() {
        btnNewGame.setOnClickListener {
            pulsadoBotonNewGame()
        }
        btnRevelar.setOnClickListener {
            pulsadoBotonRevelar()
        }
    }

    private fun pulsadoBotonNewGame() {
        AlertDialog.Builder(this@PlayGameActivity)
            .setTitle("Salir")
            .setMessage("¿Quieres salir de la partida?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salir") { _, _ ->
                // Volver atrás a la MainActivity que ya está debajo
                finish()
            }
            .show()
    }

    private fun pulsadoBotonRevelar() {
        AlertDialog.Builder(this@PlayGameActivity)
            .setTitle("Revelar impostor")
            .setMessage("¿Quieres revelar al impostor?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Revelar") { _, _ ->
                cargarDatosRevelando()
            }
            .show()
    }

    private fun cargarDatosRevelando() {
        cardViewPalabra.visibility = View.VISIBLE

        val colorImpostor = ContextCompat.getColor(this, R.color.colorImpostor)
        val colorPalabra  = ContextCompat.getColor(this, R.color.colorPalabra)

        // ---------- TEXTO IMPOSITOR ----------
        val textoImpostor = "El impostor era: $nombreImpostor"
        val spannableImpostor = SpannableString(textoImpostor)

        val startImp = textoImpostor.indexOf(nombreImpostor)
        val endImp = startImp + nombreImpostor.length

        // Color
        spannableImpostor.setSpan(
            ForegroundColorSpan(colorImpostor),
            startImp,
            endImp,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Negrita
        spannableImpostor.setSpan(
            StyleSpan(Typeface.BOLD),
            startImp,
            endImp,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        txtResumenTitulo.text = spannableImpostor

        // ---------- TEXTO PALABRA ----------
        val textoPalabra = "La palabra era: $palabraJugada"
        val spannablePalabra = SpannableString(textoPalabra)

        val startPal = textoPalabra.indexOf(palabraJugada)
        val endPal = startPal + palabraJugada.length

        // Color
        spannablePalabra.setSpan(
            ForegroundColorSpan(colorPalabra),
            startPal,
            endPal,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Negrita
        spannablePalabra.setSpan(
            StyleSpan(Typeface.BOLD),
            startPal,
            endPal,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        txtPalabra.text = spannablePalabra

        btnRevelar.visibility = View.GONE
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}



