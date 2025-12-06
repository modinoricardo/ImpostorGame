package com.example.impostorgame.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.impostorgame.Category
import com.example.impostorgame.CategoryAdapterMain
import com.example.impostorgame.CategoryViewModel
import com.example.impostorgame.EditPlayersBottomSheet
import com.example.impostorgame.activities.ImpostorRevealActivity
import com.example.impostorgame.PlayerAdapterMain
import com.example.impostorgame.PlayerViewModel
import com.example.impostorgame.R
import com.example.impostorgame.SelectCategoriesBottomSheet
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(), SelectCategoriesBottomSheet.Listener {

    private lateinit var cardViewModoJuego: CardView
    private lateinit var main: FrameLayout
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var overlay: View
    private lateinit var cardViewCategorias: CardView
    private lateinit var textResumenCategorias: TextView
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapterMain: CategoryAdapterMain
    private var originalCategoriasColor: Int = 0
    private var originalCategoriasColorsSaved = false
    private var allCategoriesSelected: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Desactivamos el modo noche
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Declaración de vistas
        main = findViewById(R.id.main)
        playersRecyclerView = findViewById(R.id.playersRecyclerView)
        cardViewModoJuego = findViewById(R.id.cardViewModoJuego)
        cardViewCategorias = findViewById(R.id.cardViewCategorias)
        overlay = findViewById(R.id.darkOverlay)
        textResumenCategorias = findViewById(R.id.textResumenCategorias)
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        val btnStartGame: Button = findViewById(R.id.btnStartGame)

        // Desactivamos el scroll propio de la lista de jugadores:
        playersRecyclerView.isNestedScrollingEnabled = false
        // Si quieres que Categorías actúe igual también en scroll:
        // categoriesRecyclerView.isNestedScrollingEnabled = false

        // Ajuste de insets del sistema (barra de estado, navegación, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ViewModels
        playerViewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)

        // RecyclerView de jugadores (Flexbox para que se distribuyan como "chips")
        val playersLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        playersRecyclerView.layoutManager = playersLayoutManager

        val playerAdapter = PlayerAdapterMain(playerViewModel.players.value ?: emptyList())
        playersRecyclerView.adapter = playerAdapter

        playerViewModel.players.observe(this) { lista ->
            playerAdapter.updatePlayers(lista)
        }

        // RecyclerView de categorías (dentro del CardView, en Flexbox)
        val categoriesLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        categoriesRecyclerView.layoutManager = categoriesLayoutManager

        categoryAdapterMain = CategoryAdapterMain(emptyList())
        categoriesRecyclerView.adapter = categoryAdapterMain

        // Observar categorías y actualizar lista + resumen
        categoryViewModel.categories.observe(this) { list ->
            val seleccionadasList = list.filter { it.isSelected }

            val categoriasParaMostrar = seleccionadasList.ifEmpty { list }

            categoryAdapterMain.updateCategories(categoriasParaMostrar)

            val total = list.size
            val seleccionadas = seleccionadasList.size
            textResumenCategorias.text = if (seleccionadas == 0) {
                "Categorías disponibles: $total"
            } else {
                "Categorías seleccionadas: $seleccionadas de $total"
            }
        }

        // ====== CARDVIEW JUGADORES ======

        // CLICK en el CardView de jugadores → abrir bottom sheet de edición
        cardViewModoJuego.setOnClickListener {
            EditPlayersBottomSheet().show(supportFragmentManager, "EditPlayers")
        }

        // Animación de pulsado CUANDO PULSAS EL CARDVIEW (zona fuera del RecyclerView)
        cardViewModoJuego.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!originalColorsSaved) {
                        originalColor = cardViewModoJuego.cardBackgroundColor.defaultColor
                        originalColorsSaved = true
                    }
                    val pressedColor = getColor(R.color.button_pressed)
                    cardViewModoJuego.setCardBackgroundColor(pressedColor)
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    cardViewModoJuego.setCardBackgroundColor(originalColor)
                }
            }
            false
        }

        // Animación + disparar click CUANDO PULSAS DENTRO DEL RECYCLERVIEW
        playersRecyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // mismo efecto visual
                    if (!originalColorsSaved) {
                        originalColor = cardViewModoJuego.cardBackgroundColor.defaultColor
                        originalColorsSaved = true
                    }
                    val pressedColor = getColor(R.color.button_pressed)
                    cardViewModoJuego.setCardBackgroundColor(pressedColor)
                }

                MotionEvent.ACTION_UP -> {
                    // quitamos el color y lanzamos el click del card
                    cardViewModoJuego.setCardBackgroundColor(originalColor)
                    cardViewModoJuego.performClick()
                }

                MotionEvent.ACTION_CANCEL -> {
                    // solo restaurar color si el gesto se cancela (scroll, etc.)
                    cardViewModoJuego.setCardBackgroundColor(originalColor)
                }
            }
            // false → dejamos que el sistema siga procesando (scroll, etc.)
            false
        }

        // ====== CARDVIEW CATEGORÍAS ======

        // CLICK en el CardView de categorías → abrir bottom sheet de selección
        cardViewCategorias.setOnClickListener {
            SelectCategoriesBottomSheet().show(
                supportFragmentManager,
                SelectCategoriesBottomSheet.Companion.TAG
            )
        }

        // Animación de pulsado al tocar fuera del RecyclerView
        cardViewCategorias.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!originalCategoriasColorsSaved) {
                        originalCategoriasColor = cardViewCategorias.cardBackgroundColor.defaultColor
                        originalCategoriasColorsSaved = true
                    }
                    val pressedColor = getColor(R.color.button_pressed)
                    cardViewCategorias.setCardBackgroundColor(pressedColor)
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    cardViewCategorias.setCardBackgroundColor(originalCategoriasColor)
                }
            }
            false
        }

        // Animación + click al tocar dentro del RecyclerView
        categoriesRecyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!originalCategoriasColorsSaved) {
                        originalCategoriasColor = cardViewCategorias.cardBackgroundColor.defaultColor
                        originalCategoriasColorsSaved = true
                    }
                    val pressedColor = getColor(R.color.button_pressed)
                    cardViewCategorias.setCardBackgroundColor(pressedColor)
                }

                MotionEvent.ACTION_UP -> {
                    cardViewCategorias.setCardBackgroundColor(originalCategoriasColor)
                    cardViewCategorias.performClick()
                }

                MotionEvent.ACTION_CANCEL -> {
                    cardViewCategorias.setCardBackgroundColor(originalCategoriasColor)
                }
            }
            false
        }



        // Botón fijo abajo (de momento placeholder)
        btnStartGame.setOnClickListener {

            //Hacemos una lista de jugadores para pasarsela a la segunda actividad
            val listaJugadores = ArrayList(playerViewModel.players.value)

            //Hacemos una lista de categorias para pasarsela a la segunda actividad
            val listaCategorias = ArrayList(categoryViewModel.categories.value)
            //Lista solo con las listas seleccionadas
            var listaCategoriasSeleccionadas = ArrayList<Category>();
            listaCategorias.forEach { item->
                if(item.isSelected){
                    listaCategoriasSeleccionadas.add(item)
                }
            }

            val categoriasAEnviar = if (listaCategoriasSeleccionadas.isEmpty()) listaCategorias else listaCategoriasSeleccionadas

            val intent = Intent(this, ImpostorRevealActivity::class.java).apply {
                putStringArrayListExtra("PLAYERS", listaJugadores)
            }
            intent.putExtra("CATEGORIES", categoriasAEnviar)

            // Lanzar la nueva Activity
            startActivity(intent)
        }
    }


    //Detecta cuando vuelve a primer plano
    override fun onResume() {
        super.onResume()
    }

    fun onBottomSheetClosed() {

    }

    private var originalColor: Int = 0
    private var originalColorsSaved = false

    fun mensajeAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onCategoriesConfirmed(selected: List<Category>) {
        val total = categoryViewModel.categories.value?.size ?: 0
        val seleccionadas = selected.size

        textResumenCategorias.text = if (seleccionadas == 0) {
            "Categorías disponibles: $total"
        } else {
            "Categorías seleccionadas: $seleccionadas de $total"
        }
    }

}