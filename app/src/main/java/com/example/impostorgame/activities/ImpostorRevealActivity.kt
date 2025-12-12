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
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.impostorgame.GameOptions
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlinx.coroutines.launch
import com.example.impostorgame.CategoryViewModel
import com.example.impostorgame.WordItem


@Suppress("DEPRECATION")
class ImpostorRevealActivity : AppCompatActivity() {
    private val categoryViewModel: CategoryViewModel by viewModels()
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
    private var pistaActivaModoLoco: Boolean = false
    private lateinit var categoriaInGame: Category
    private lateinit var wordItemInGame: WordItem

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

        // después de leer listaCategorias y comprobar que no está vacía
        categoryViewModel.setCategories(listaCategorias)

        //cargamos los eventos
        onEventos()

        //Cargamos datos del juego
        datosJuego()
    }

    private fun datosJuego() {
        indiceImpostor = listaJugadores.indices.random()
        nameImpostorInGame = listaJugadores[indiceImpostor]

        //cargamos la categoria a jugar
        categoriaInGame = listaCategorias.random()
        if (categoryViewModel.itemsVacio(categoriaInGame.id)) {
            categoryViewModel.restoreItems(categoriaInGame.id)
            categoriaInGame = categoryViewModel.categories.value!!.first { it.id == categoriaInGame.id }
        }


        if (categoryViewModel.itemsVacio(categoriaInGame.id)) {
            categoryViewModel.restoreItems(categoriaInGame.id)
            categoriaInGame = categoryViewModel.categories.value!!
                .first { it.id == categoriaInGame.id }
        }

        wordItemInGame = categoriaInGame.items.random()

        palabra = wordItemInGame.name
        pista = wordItemInGame.hints.random()

        playerInGame = 0
        ocultarPalabra()
        //hacemos un aleatorio del X% para el modo loco
        modoLocoActivo = random(45)

        if (opciones.modoLoco && modoLocoActivo) cargarInformacionModoLoco() else cargarInformacionNormal()
    }

    fun random(num: Int): Boolean {
        return Random.nextInt(100) < num
    }


    private fun cargarInformacionModoLoco() {
        // Nombre del jugador del turno
        turnPlayerName.text = listaJugadores[playerInGame]

        // Es el impostor
        detailsPlayer.text = "ERES EL \nIMPOSTOR"
        detailsPlayer.setTextColor(getColor(R.color.colorImpostor))

        // Preparamos el texto de la pista (sin mostrarla aún)
        if (opciones.pista) {

            if(!pistaActivaModoLoco){

                val categoriaInGame = listaCategorias.random()
                val wordItemInGame = categoriaInGame.items.random()
                palabra = wordItemInGame.name
                pista = wordItemInGame.hints.random()

            }

            hintPlayer.text = "Pista: $pista"

        } else {
            hintPlayer.text = ""
        }
        hintPlayer.visibility = View.GONE

        pistaActivaModoLoco = true

    }

    private fun cargarInformacionNormal() {
        // Nombre del jugador del turno
        turnPlayerName.text = listaJugadores[playerInGame]

        if (indiceImpostor == playerInGame) {
            // Es el impostor
            detailsPlayer.text = "ERES EL \nIMPOSTOR"
            detailsPlayer.setTextColor(getColor(R.color.colorImpostor))

            // Preparamos el texto de la pista (sin mostrarla aún)
            if (opciones.pista) {
                hintPlayer.text = "Pista: $pista"
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
                    if (opciones.modoLoco && modoLocoActivo) {
                        mostrarPalabraModoLoco()
                    } else {
                        mostrarPalabraNormal()
                    }
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

        nenxtPlayer.setOnClickListener { btnNextPlayer() }
    }


    private fun ocultarPalabra() {
        // Al soltar el dedo, nadie ve nada
        detailsPlayer.visibility = View.GONE
        hintPlayer.visibility = View.GONE

        imgDedo.visibility = View.VISIBLE
        txtTwo.visibility = View.VISIBLE
        presText.visibility = View.VISIBLE
    }

    private fun mostrarPalabraNormal() {
        cargarInformacionNormal()

        imgDedo.visibility = View.GONE
        txtTwo.visibility = View.GONE
        presText.visibility = View.GONE

        if (indiceImpostor == playerInGame && opciones.pista) {
            hintPlayer.visibility = View.VISIBLE
        } else {
            hintPlayer.visibility = View.GONE
        }

        detailsPlayer.visibility = View.VISIBLE
    }

    //Pulsamos encima del cardView
    private fun mostrarPalabraModoLoco() {
        cargarInformacionModoLoco()

        imgDedo.visibility = View.GONE
        txtTwo.visibility = View.GONE
        presText.visibility = View.GONE

        detailsPlayer.visibility = View.VISIBLE

        // Aquí decides la regla: ¿todos ven pista? ¿solo este jugador?
        hintPlayer.visibility = if (opciones.pista) View.VISIBLE else View.GONE
    }


    private var isAnimating = false

    private fun btnNextPlayer() {
        pistaActivaModoLoco = false
        val lastIndex = listaJugadores.lastIndex

        // Si ya estamos en el último y le damos a siguiente, vamos a la partida
        if (playerInGame == lastIndex) {
            val intent = Intent(this, PlayGameActivity::class.java).apply {
                putStringArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
                putParcelableArrayListExtra("LISTA_CATEGORIAS", ArrayList(listaCategorias))
                putExtra(
                    "PALABRA",
                    if (opciones.modoLoco && modoLocoActivo) "NO HABIA PALABRA" else palabra
                )
                putExtra(
                    "IMPOSTOR",
                    if (opciones.modoLoco && modoLocoActivo) "TODOS SOIS IMPOSTORES" else nameImpostorInGame
                )
            }
            startActivity(intent)

            val updatedCategories = ArrayList(categoryViewModel.categories.value ?: emptyList())

            finishWithUpdatedCategories()

            return
        }

        if (isAnimating) return
        isAnimating = true
        nenxtPlayer.isEnabled = false

        // Avanzar al siguiente jugador que vamos a mostrar
        playerInGame++

        slideOutIn(cardViewPrincipal, outExtra = 120f) {
            // 1) Resetea la UI
            nenxtPlayer.visibility = View.INVISIBLE
            ocultarPalabra()

            // 2) Carga la info DEL NUEVO jugador (playerInGame ya está incrementado)
            if (opciones.modoLoco && modoLocoActivo) {
                cargarInformacionModoLoco()
            } else {
                cargarInformacionNormal()
            }

            // 3) Actualiza el texto del botón según lo que venga después
            val esProximoElUltimo = (playerInGame == lastIndex)
            textNextPlayer.text =
                if (esProximoElUltimo) "¡EMPEZAR PARTIDA!" else "⏭ SIGUIENTE JUGADOR"

            categoryViewModel.deleteWordItem(categoriaInGame.id, wordItemInGame)
            if(esProximoElUltimo)categoryViewModel.logItems(categoriaInGame.id)



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

    private fun finishWithUpdatedCategories() {
        setResult(RESULT_OK, Intent().apply {
            putParcelableArrayListExtra(
                "UPDATED_CATEGORIES",
                ArrayList(categoryViewModel.categories.value ?: emptyList())
            )
        })
        finish()
    }

}