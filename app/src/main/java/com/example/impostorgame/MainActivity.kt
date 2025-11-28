package com.example.impostorgame

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity(), SelectCategoriesBottomSheet.Listener {
    @SuppressLint("ClickableViewAccessibility")

    private lateinit var cardViewModoJuego: CardView
    private lateinit var main: FrameLayout
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var overlay: View
    private lateinit var cardViewCategorias: CardView
    private lateinit var textResumenCategorias: android.widget.TextView
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapterMain: CategoryAdapterMain
    private var originalCategoriasColor: Int = 0
    private var originalCategoriasColorsSaved = false


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
        relativeLayout = findViewById(R.id.relativeLayout)
        overlay = findViewById(R.id.darkOverlay)
        textResumenCategorias = findViewById(R.id.textResumenCategorias)
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)

        // Insets (usa main que ya tenemos referenciado)
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ViewModels
        val playerViewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)

        // RecyclerView de jugadores (Flexbox)
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

        // Pulsar en el CardView de jugadores o en la lista → abrir bottom sheet de edición
        cardViewModoJuego.setOnTouchListener { _, event ->
            clickEditarJugadores(event)
            true
        }

        playersRecyclerView.setOnTouchListener { _, event ->
            clickEditarJugadores(event)
            true
        }

        // Click en CardView de categorías → abrir bottom sheet de selección de categorías
        cardViewCategorias.setOnTouchListener { _, event ->
            clickCategorias(event)
            true
        }

        categoriesRecyclerView.setOnTouchListener { _, event ->
            clickCategorias(event)
            true
        }


        // RecyclerView de categorías (dentro del CardView, altura fija)
        val categoriesLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        categoriesRecyclerView.layoutManager = categoriesLayoutManager

        // IMPORTANTE: inicializar vacío; el observer lo llenará
        categoryAdapterMain = CategoryAdapterMain(emptyList())
        categoriesRecyclerView.adapter = categoryAdapterMain

        // Observar categorías y actualizar lista + resumen
        categoryViewModel.categories.observe(this) { list ->
            // 1) calcular las seleccionadas
            val seleccionadasList = list.filter { it.isSelected }

            // 2) decidir qué mostrar en el main
            val categoriasParaMostrar = if (seleccionadasList.isNotEmpty()) {
                // si hay seleccionadas → solo esas
                seleccionadasList
            } else {
                // si no hay ninguna seleccionada → todas
                list
            }

            // 3) actualizar el RecyclerView del main
            categoryAdapterMain.updateCategories(categoriasParaMostrar)

            // 4) actualizar el texto de resumen
            val total = list.size
            val seleccionadas = seleccionadasList.size
            textResumenCategorias.text = if (seleccionadas == 0) {
                "Categorías disponibles: $total"
            } else {
                "Categorías seleccionadas: $seleccionadas de $total"
            }
        }
    }


    //Detecta cuando vuelve a primer plano
    override fun onResume() {
        super.onResume()
        overlay.visibility = View.GONE
    }

    fun onBottomSheetClosed() {
        overlay.visibility = View.GONE
    }


    private var originalColor: Int = 0
    private var originalColorsSaved = false
    fun clickEditarJugadores(event: MotionEvent) {
        //mensajeAlerta("Alerta", "Esta opcion aun no esta implementada en la app")

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                if (!originalColorsSaved) {
                    originalColor = cardViewModoJuego.cardBackgroundColor.defaultColor
                    originalColorsSaved = true
                }

                val pressedColor = getColor(R.color.button_pressed)
                cardViewModoJuego.setCardBackgroundColor(pressedColor)
                playersRecyclerView.setBackgroundColor(pressedColor)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                cardViewModoJuego.setCardBackgroundColor(originalColor)
                playersRecyclerView.setBackgroundColor(originalColor)

                editarJugadores()
            }
        }
    }



    fun clickCategorias(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!originalCategoriasColorsSaved) {
                    originalCategoriasColor = cardViewCategorias.cardBackgroundColor.defaultColor
                    originalCategoriasColorsSaved = true
                }

                val pressedColor = getColor(R.color.button_pressed)
                cardViewCategorias.setCardBackgroundColor(pressedColor)
                // NO toques el fondo del RecyclerView aquí
                // categoriesRecyclerView.setBackgroundColor(pressedColor)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cardViewCategorias.setCardBackgroundColor(originalCategoriasColor)
                // Y aquí, si quieres, lo dejas transparente
                // categoriesRecyclerView.setBackgroundColor(Color.TRANSPARENT)

                SelectCategoriesBottomSheet().show(
                    supportFragmentManager,
                    SelectCategoriesBottomSheet.TAG
                )
            }
        }
    }



    fun editarJugadores() {
        overlay.visibility = View.VISIBLE
        EditPlayersBottomSheet().show(supportFragmentManager, "EditPlayers")
    }

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
