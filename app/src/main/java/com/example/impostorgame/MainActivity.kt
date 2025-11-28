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
import android.widget.Button


class MainActivity : AppCompatActivity(), SelectCategoriesBottomSheet.Listener {

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
        val playerViewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
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

        // CLICK en la zona del RecyclerView → comportarse igual que el CardView
        playersRecyclerView.setOnClickListener {
            cardViewModoJuego.performClick()
        }

        // MISMO OnTouchListener para CardView + RecyclerView (efecto visual unificado)
        val touchListenerModoJuego = View.OnTouchListener { _, event ->
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
            // devolvemos false para que el sistema siga procesando el evento
            // (así el click sigue funcionando y el ScrollView puede hacer scroll)
            false
        }

        // Aplicamos el mismo listener tanto al CardView como al RecyclerView
        cardViewModoJuego.setOnTouchListener(touchListenerModoJuego)
        playersRecyclerView.setOnTouchListener(touchListenerModoJuego)


        // ====== CARDVIEW CATEGORÍAS ======

        // CLICK en el CardView de categorías → abrir bottom sheet de selección
        cardViewCategorias.setOnClickListener {
            SelectCategoriesBottomSheet().show(
                supportFragmentManager,
                SelectCategoriesBottomSheet.TAG
            )
        }

        // CLICK en la zona del RecyclerView → comportarse igual que el CardView
        categoriesRecyclerView.setOnClickListener {
            cardViewCategorias.performClick()
        }

        // MISMO OnTouchListener para CardView + RecyclerView de categorías
        val touchListenerCategorias = View.OnTouchListener { _, event ->
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

        // Aplicamos el mismo listener al CardView y al RecyclerView
        cardViewCategorias.setOnTouchListener(touchListenerCategorias)
        categoriesRecyclerView.setOnTouchListener(touchListenerCategorias)


        // Botón fijo abajo (de momento placeholder)
        btnStartGame.setOnClickListener {
            // Aquí pondrás la lógica de empezar la partida
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
