package com.example.impostorgame.activities

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.impostorgame.Category
import com.example.impostorgame.R
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.activity.OnBackPressedCallback
import android.graphics.Color
import android.opengl.Visibility
import com.example.impostorgame.GameOptions
import kotlin.random.Random


@Suppress("DEPRECATION")
class ImpostorRevealActivity : AppCompatActivity() {
    //Lista donde guardamos los jugadores
    private lateinit var listaJugadores: List<String>;
    private lateinit var listaCategorias: List<Category>;
    private lateinit var detailsPlayer: TextView;
    private lateinit var textNextPlayer: TextView;
    private lateinit var layout: LinearLayout;
    private lateinit var cardViewPrincipal: CardView;
    private lateinit var imgDedo: ImageView;
    private lateinit var txtTwo: TextView;
    private lateinit var nenxtPlayer: CardView;
    private lateinit var presText: TextView;
    private lateinit var turnPlayerName: TextView;
    private lateinit var hintPlayer: TextView;
    private var playerInGame: Int = 0;
    private var indiceImpostor: Int = 0;
    private lateinit var palabra: String;
    private lateinit var pista: String;
    private lateinit var nameImpostorInGame: String;
    private lateinit var opciones: GameOptions
    private var modoLocoActivo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_impostor_reveal)

        //Cuando el usuario de hacia atras en la barra de navegacion
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@ImpostorRevealActivity)
                    .setTitle("Salir")
                    .setMessage("¿Quieres salir de la partida?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ ->
                        finish()
                    }
                    .show()
            }
        })

        // Vistas
        detailsPlayer = findViewById(R.id.detailsPlayer)
        layout = findViewById(R.id.layout)
        cardViewPrincipal = findViewById(R.id.cardViewPrincipal)
        imgDedo = findViewById(R.id.imgDedo)
        txtTwo = findViewById(R.id.txtTwo)
        nenxtPlayer = findViewById(R.id.nenxtPlayer)
        presText = findViewById(R.id.presText)
        turnPlayerName = findViewById(R.id.turnPlayerName)
        textNextPlayer = findViewById(R.id.textNextPlayer)
        hintPlayer = findViewById(R.id.hintPlayer)

        // Insets arriba: status bar + notch. Laterales por recortes/gestos.
        ViewCompat.setOnApplyWindowInsetsListener(layout) { v, insets ->
            val topInsets = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val sideInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.displayCutout()
            )

            v.updatePadding(left = sideInsets.left, top = topInsets.top, right = sideInsets.right)
            insets
        }

        // Insets abajo: solo navigation bar, sumado a tu margen base.
        val baseBottomMargin =
            (nenxtPlayer.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(nenxtPlayer) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            (v.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                baseBottomMargin + nav.bottom
            v.requestLayout()
            insets
        }

        // Intent data
        listaJugadores = intent.getStringArrayListExtra("PLAYERS")?.toList() ?: emptyList()
        listaCategorias =
            intent.getParcelableArrayListExtra<Category>("CATEGORIES")?.toList() ?: emptyList()
        opciones = intent.getParcelableExtra("OPCIONES") ?: GameOptions()

        if (listaJugadores.isEmpty() || listaCategorias.isEmpty()) {
            finish()
            return
        }

        layout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        onEventos()

        indiceImpostor = listaJugadores.indices.random()
        nameImpostorInGame = listaJugadores[indiceImpostor]

        val categoriaInGame = listaCategorias.random()
        if (categoriaInGame.items.isEmpty()) {
            finish()
            return
        }
        val wordItemInGame = categoriaInGame.items.random()
        palabra = wordItemInGame.name
        pista = wordItemInGame.hint

        playerInGame = 0
        ocultarPalabra()
        //hacemos un aleatorio del 25% para el modo loco
        modoLocoActivo = random90()

    }

    fun random25(): Boolean {
        // 0,1,2,3 todos con la misma probabilidad
        // solo cuando sea 0 devolvemos true → 1 de 4 = 25 %
        return Random.nextInt(4) == 0
    }

    fun random90(): Boolean {
        // Genera un entero entre 0 y 9 (10 valores posibles)
        // 0..8  -> true  (9 de 10 = 90 %)
        // 9     -> false (1 de 10 = 10 %)
        return Random.nextInt(10) < 9
    }

    private fun cargarInformacionModoLoco() {
        // Nombre del jugador del turno
        turnPlayerName.text = listaJugadores[playerInGame]

        // Es el impostor
        detailsPlayer.text = "ERES EL \nIMPOSTOR"
        detailsPlayer.setTextColor(Color.RED)

        // Preparamos el texto de la pista (sin mostrarla aún)
        if (opciones.pista) {

            val categoriaInGame = listaCategorias.random()
            if (categoriaInGame.items.isEmpty()) {
                finish()
                return
            }
            val wordItemInGame = categoriaInGame.items.random()
            palabra = wordItemInGame.name
            pista = wordItemInGame.hint

            hintPlayer.text = pista
        } else {
            hintPlayer.text = ""
        }
        hintPlayer.visibility = View.GONE

    }

    private fun cargarInformacion() {
        // Nombre del jugador del turno
        turnPlayerName.text = listaJugadores[playerInGame]

        if (indiceImpostor == playerInGame) {
            // Es el impostor
            detailsPlayer.text = "ERES EL \nIMPOSTOR"
            detailsPlayer.setTextColor(Color.RED)

            // Preparamos el texto de la pista (sin mostrarla aún)
            if (opciones.pista) {
                hintPlayer.text = pista
            } else {
                hintPlayer.text = ""
            }
            hintPlayer.visibility = View.GONE
        } else {
            // Es civil
            detailsPlayer.text = palabra
            detailsPlayer.setTextColor(Color.BLACK)

            // Los civiles nunca ven pista, ni texto residual
            hintPlayer.text = ""
            hintPlayer.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onEventos() {
        cardViewPrincipal.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (opciones.modoLoco && modoLocoActivo) mostrarPalabraModoLoco() else mostrarPalabra()
                    nenxtPlayer.visibility = View.VISIBLE
                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    ocultarPalabra()
                    true
                }

                else -> false
            }
        }
        nenxtPlayer.setOnClickListener {
            btnNextPlayer()
        }
    }

    private fun ocultarPalabra() {
        // Al soltar el dedo, nadie ve nada
        detailsPlayer.visibility = View.GONE
        hintPlayer.visibility = View.GONE

        imgDedo.visibility = View.VISIBLE
        txtTwo.visibility = View.VISIBLE
        presText.visibility = View.VISIBLE
    }

    private fun mostrarPalabraModoLoco() {
        if (opciones.modoLoco && modoLocoActivo) cargarInformacionModoLoco() else cargarInformacion()

        detailsPlayer.visibility = View.VISIBLE

            hintPlayer.visibility = View.VISIBLE

        imgDedo.visibility = View.GONE
        txtTwo.visibility = View.GONE
        presText.visibility = View.GONE
    }
    private fun mostrarPalabra() {
        // Actualiza textos (impostor / civil)
        if (opciones.modoLoco && modoLocoActivo) cargarInformacionModoLoco() else cargarInformacion()

        detailsPlayer.visibility = View.VISIBLE

        // Solo IMPOSITOR + pista activada ve la pista
        if (indiceImpostor == playerInGame && opciones.pista) {
            hintPlayer.visibility = View.VISIBLE
        } else {
            hintPlayer.visibility = View.GONE
        }

        imgDedo.visibility = View.GONE
        txtTwo.visibility = View.GONE
        presText.visibility = View.GONE
    }

    private var isAnimating = false

    private fun btnNextPlayer() {
        val lastIndex = listaJugadores.lastIndex

        // Si es el último, no animar. Ir directo.
        if (playerInGame == lastIndex) {
            val intent = Intent(this, PlayGameActivity::class.java).apply {
                putStringArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
                putParcelableArrayListExtra("LISTA_CATEGORIAS", ArrayList(listaCategorias))
                putExtra("PALABRA", palabra)
                putExtra("IMPOSTOR", nameImpostorInGame)
            }
            startActivity(intent)
            finish()
            return
        }

        if (isAnimating) return
        isAnimating = true
        nenxtPlayer.isEnabled = false

        slideOutIn(cardViewPrincipal, outExtra = 120f) {
            nenxtPlayer.visibility = View.INVISIBLE
            ocultarPalabra()

            playerInGame++

            textNextPlayer.text =
                if (playerInGame == lastIndex) "¡EMPEZAR PARTIDA!" else "⏭ SIGUIENTE JUGADOR"
        }

        cardViewPrincipal.postDelayed({
            nenxtPlayer.isEnabled = true
            isAnimating = false
        }, 420)
    }


    private fun slideOutIn(card: View, outExtra: Float = 0f, onSwap: () -> Unit) {
        val w = card.width.toFloat().takeIf { it > 0f } ?: return

        card.animate().cancel()

        // Sale a la izquierda
        card.animate()
            .translationX(-(w + outExtra))
            .alpha(0f)
            .setDuration(180L)
            .withEndAction {
                onSwap()

                // Aparece por la derecha
                card.translationX = (w + outExtra)
                card.alpha = 0f

                card.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(180L)
                    .start()
            }
            .start()
    }


}