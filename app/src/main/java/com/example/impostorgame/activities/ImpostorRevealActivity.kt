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
import com.example.impostorgame.modelos.Category
import com.example.impostorgame.R
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.activity.OnBackPressedCallback
import android.graphics.Color
import androidx.activity.viewModels
import com.example.impostorgame.modelos.GameOptions
import kotlin.random.Random
import com.example.impostorgame.CategoryViewModel
import com.example.impostorgame.modelos.Jugador
import com.example.impostorgame.modelos.WordItem
import com.example.impostorgame.PlayerViewModel


@Suppress("DEPRECATION")
class ImpostorRevealActivity : AppCompatActivity() {
    private val categoryViewModel: CategoryViewModel by viewModels()

    //Lista donde guardamos los jugadores
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
    private lateinit var listaJugadores: List<Jugador>
    private val playerViewModel: PlayerViewModel by viewModels()
    private lateinit var imgWord: ImageView
    private var imageResTurno: Int = 0
    private val impostorImageRes = R.drawable.impostor
    private lateinit var imagenPorJugador: IntArray

    private val wordImages = listOf(
        R.drawable.civil1,
        R.drawable.civil2,
        R.drawable.civil3,
        R.drawable.civil4,
        R.drawable.civil5,
        R.drawable.civil6,
        R.drawable.civil7,
        R.drawable.civil8,
        R.drawable.civil9,
        R.drawable.civil10
    )


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
        imgWord = findViewById(R.id.imgWord)

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
        listaJugadores =
            intent.getParcelableArrayListExtra<Jugador>("PLAYERS")?.toList() ?: emptyList()

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

    private fun isActiveCategory(category: Category): Boolean {
        return listaCategorias.any { it.id == category.id && it.isSelected }
    }

    private fun datosJuego() {
        indiceImpostor = playerViewModel.pickImpostorIndex(listaJugadores)

        imagenPorJugador = IntArray(listaJugadores.size)

        val pool = wordImages.shuffled().toMutableList()
        for (i in listaJugadores.indices) {
            imagenPorJugador[i] =
                if (i == indiceImpostor) impostorImageRes
                else (pool.removeFirstOrNull() ?: wordImages.random())
        }

        nameImpostorInGame = listaJugadores[indiceImpostor].nombre


        //cargamos la categoria a jugar
        do{
            categoriaInGame = listaCategorias.random()
        }while (listaCategorias.any { it.isSelected } && !isActiveCategory(categoriaInGame))

        if (categoryViewModel.itemsVacio(categoriaInGame.id)) {
            categoryViewModel.restoreItems(categoriaInGame.id)
            categoriaInGame =
                categoryViewModel.categories.value!!.first { it.id == categoriaInGame.id }
        }


        if (categoryViewModel.itemsVacio(categoriaInGame.id)) {
            categoryViewModel.restoreItems(categoriaInGame.id)
            categoriaInGame = categoryViewModel.categories.value!!
                .first { it.id == categoriaInGame.id }
        }

        wordItemInGame = categoriaInGame.items.random()

        palabra = wordItemInGame.name
        pista = wordItemInGame.hints.random()

        imageResTurno = wordImages.random()

        playerInGame = 0
        ocultarPalabra()
        //hacemos un aleatorio del X% para el modo loco
        modoLocoActivo = random(10)

        if (opciones.modoLoco && modoLocoActivo) cargarInformacionModoLoco() else cargarInformacionNormal()
    }

    fun random(num: Int): Boolean {
        return Random.nextInt(100) < num
    }


    // Carga la informacion para el modo loco:
// - Siempre muestra "ERES EL IMPOSTOR"
// - Prepara (una vez por turno) una palabra/pista aleatoria si la opcion de pista esta activa
// - Asigna la imagen del turno (en modo loco: siempre la del impostor)
    private fun cargarInformacionModoLoco() {

        // Nombre del jugador del turno (Jugador -> nombre)
        turnPlayerName.text = listaJugadores[playerInGame].nombre

        // Imagen del turno: en modo loco siempre impostor
        imageResTurno = impostorImageRes

        // En modo loco: siempre impostor
        detailsPlayer.text = "ERES EL \nIMPOSTOR"
        detailsPlayer.setTextColor(getColor(R.color.colorImpostor))

        // Preparamos el texto de la pista (sin mostrarla aun)
        if (opciones.pista) {

            // Solo generamos nueva palabra/pista la primera vez de este turno
            if (!pistaActivaModoLoco) {

                val categoriaRandom = listaCategorias.randomOrNull()

                // Evita crash si no hay categorias o si vienen sin items
                val wordItemRandom = categoriaRandom?.items?.randomOrNull()
                if (wordItemRandom != null) {
                    palabra = wordItemRandom.name
                    pista = wordItemRandom.hints.randomOrNull() ?: ""
                } else {
                    palabra = ""
                    pista = ""
                }
            }

            hintPlayer.text = if (pista.isNotEmpty()) "Pista: $pista" else ""
        } else {
            hintPlayer.text = ""
        }

        // Se oculta por defecto; se hace visible cuando tu UI lo decida
        hintPlayer.visibility = View.GONE

        // Marcamos que ya hemos generado la pista de este turno
        pistaActivaModoLoco = true
    }


    // Carga la informacion del turno en modo normal:
    // - Muestra el nombre del jugador actual
    // - Si es el impostor, muestra el texto de impostor y prepara la pista
    // - Si es civil, muestra la palabra y oculta cualquier pista
    private fun cargarInformacionNormal() {

        // Nombre del jugador del turno (Jugador -> nombre)
        turnPlayerName.text = listaJugadores[playerInGame].nombre

        // Imagen del turno: impostor fija / civil aleatoria
        imageResTurno = imagenPorJugador[playerInGame]

        if (indiceImpostor == playerInGame) {
            // Impostor
            detailsPlayer.text = "ERES EL \nIMPOSTOR"
            detailsPlayer.setTextColor(getColor(R.color.colorImpostor))

            // Preparar pista (sin mostrarla aun)
            hintPlayer.text = if (opciones.pista) "Pista: $pista" else ""
            hintPlayer.visibility = View.GONE
        } else {
            // Civil
            detailsPlayer.text = palabra
            detailsPlayer.setTextColor(Color.BLACK)

            // Civil nunca ve pista
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
        imgWord.visibility = View.GONE

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

        imgWord.setImageResource(imageResTurno)
        imgWord.visibility = View.VISIBLE

    }

    //Pulsamos encima del cardView
    private fun mostrarPalabraModoLoco() {
        cargarInformacionModoLoco()

        imgDedo.visibility = View.GONE
        txtTwo.visibility = View.GONE
        presText.visibility = View.GONE

        detailsPlayer.visibility = View.VISIBLE

        imgWord.setImageResource(imageResTurno)
        imgWord.visibility = View.VISIBLE

        // Aquí decides la regla: ¿todos ven pista? ¿solo este jugador?
        hintPlayer.visibility = if (opciones.pista) View.VISIBLE else View.GONE
    }


    private var isAnimating = false

    // Pasa al siguiente jugador:
    // - Si ya es el ultimo jugador, lanza PlayGameActivity y devuelve a MainActivity las categorias actualizadas
    // - Si no, avanza el turno, anima la tarjeta y borra el WordItem usado para no repetirlo
    private fun btnNextPlayer() {
        pistaActivaModoLoco = false
        val lastIndex = listaJugadores.lastIndex

        // Si estamos en el ultimo jugador y se pulsa "siguiente", terminamos la fase de revelado
        if (playerInGame == lastIndex) {

            // Importante: si listaJugadores ahora es List<Jugador>, esto debe ser putParcelableArrayListExtra
            // putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))

            val intent = Intent(this, PlayGameActivity::class.java).apply {
                putParcelableArrayListExtra("LISTA_JUGADORES", ArrayList(listaJugadores))
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


            // Devolvemos a MainActivity el estado actualizado de categorias (items borrados, etc.)
            finishWithUpdatedCategories()
            return
        }

        // Evita doble click / doble animacion
        if (isAnimating) return
        isAnimating = true
        nenxtPlayer.isEnabled = false

        // Avanzamos al siguiente jugador a mostrar
        playerInGame++

        slideOutIn(cardViewPrincipal, outExtra = 120f) {
            // 1) Resetea la UI del reveal
            nenxtPlayer.visibility = View.INVISIBLE
            ocultarPalabra()

            // 2) Carga la info del nuevo turno (playerInGame ya esta actualizado)
            if (opciones.modoLoco && modoLocoActivo) {
                cargarInformacionModoLoco()
            } else {
                cargarInformacionNormal()
            }

            // 3) Ajusta el texto del boton segun si el proximo es el ultimo
            val esProximoElUltimo = (playerInGame == lastIndex)
            textNextPlayer.text = if (esProximoElUltimo) "¡EMPEZAR PARTIDA!" else "⏭ SIGUIENTE JUGADOR"

            // 4) Marca como "usada" la palabra para que no vuelva a salir en esta sesion
            categoryViewModel.deleteWordItem(categoriaInGame.id, wordItemInGame)

            // Debug opcional: imprime items solo si ya viene el ultimo
            if (esProximoElUltimo) categoryViewModel.logItems(categoriaInGame.id)
        }

        // Rehabilita el boton al acabar la animacion
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