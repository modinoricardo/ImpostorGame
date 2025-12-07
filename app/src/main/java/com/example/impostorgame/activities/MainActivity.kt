package com.example.impostorgame.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.impostorgame.Category
import com.example.impostorgame.CategoryAdapterMain
import com.example.impostorgame.CategoryViewModel
import com.example.impostorgame.EditPlayersBottomSheet
import com.example.impostorgame.GameOptions
import com.example.impostorgame.activities.ImpostorRevealActivity
import com.example.impostorgame.PlayerAdapterMain
import com.example.impostorgame.PlayerViewModel
import com.example.impostorgame.R
import com.example.impostorgame.SelectCategoriesBottomSheet
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.switchmaterial.SwitchMaterial

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
    private lateinit var switchModoLoco: SwitchMaterial
    private lateinit var switchPista: SwitchMaterial
    private lateinit var btnStartGame: Button
    private var originalCategoriasColor: Int = 0
    private var originalCategoriasColorsSaved = false
    private var allCategoriesSelected: Boolean = false
    private lateinit var opciones: GameOptions

    @SuppressLint("ClickableViewAccessibility")
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Declaración de vistas
        main = findViewById(R.id.main)
        playersRecyclerView = findViewById(R.id.playersRecyclerView)
        cardViewModoJuego = findViewById(R.id.cardViewModoJuego)
        cardViewCategorias = findViewById(R.id.cardViewCategorias)
        overlay = findViewById(R.id.darkOverlay)
        textResumenCategorias = findViewById(R.id.textResumenCategorias)
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        btnStartGame = findViewById(R.id.btnStartGame)
        switchModoLoco = findViewById(R.id.switchModoLoco)
        switchPista = findViewById(R.id.switchPista)

        playersRecyclerView.isNestedScrollingEnabled = false
        // categoriesRecyclerView.isNestedScrollingEnabled = false

        // Insets arriba: status bar + notch. Laterales: por si hay recortes.
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val top = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            ).top

            v.updatePadding(top = top) // sin left/right
            insets
        }


        // Insets abajo: solo navigation bar. Mantén tu margen base.
        val baseBottomMargin =
            (btnStartGame.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(btnStartGame) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            (v.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = baseBottomMargin + nav.bottom
            v.requestLayout()
            insets
        }

        // ViewModels
        playerViewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
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

        // RecyclerView de categorías (Flexbox)
        val categoriesLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        categoriesRecyclerView.layoutManager = categoriesLayoutManager

        categoryAdapterMain = CategoryAdapterMain(emptyList())
        categoriesRecyclerView.adapter = categoryAdapterMain

        lanzarEventos()

        //Hacemos un objeto para todas las opciones extras
        opciones = GameOptions(pista = true, modoLoco = false)

    }

    fun lanzarEventos(){

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
        cardViewModoJuego.setOnClickListener {
            EditPlayersBottomSheet().show(supportFragmentManager, "EditPlayers")
        }

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
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    cardViewModoJuego.setCardBackgroundColor(originalColor)
                }
            }
            false
        }

        playersRecyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!originalColorsSaved) {
                        originalColor = cardViewModoJuego.cardBackgroundColor.defaultColor
                        originalColorsSaved = true
                    }
                    val pressedColor = getColor(R.color.button_pressed)
                    cardViewModoJuego.setCardBackgroundColor(pressedColor)
                }
                MotionEvent.ACTION_UP -> {
                    cardViewModoJuego.setCardBackgroundColor(originalColor)
                    cardViewModoJuego.performClick()
                }
                MotionEvent.ACTION_CANCEL -> {
                    cardViewModoJuego.setCardBackgroundColor(originalColor)
                }
            }
            false
        }

        // ====== CARDVIEW CATEGORÍAS ======
        cardViewCategorias.setOnClickListener {
            SelectCategoriesBottomSheet().show(
                supportFragmentManager,
                SelectCategoriesBottomSheet.Companion.TAG
            )
        }

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
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    cardViewCategorias.setCardBackgroundColor(originalCategoriasColor)
                }
            }
            false
        }

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

        // Botón fijo abajo
        btnStartGame.setOnClickListener {
            val listaJugadores = ArrayList(playerViewModel.players.value ?: emptyList())

            val listaCategorias = ArrayList(categoryViewModel.categories.value ?: emptyList())
            val listaCategoriasSeleccionadas = ArrayList<Category>().apply {
                listaCategorias.forEach { if (it.isSelected) add(it) }
            }

            val categoriasAEnviar =
                if (listaCategoriasSeleccionadas.isEmpty()) listaCategorias else listaCategoriasSeleccionadas

            val intent = Intent(this, ImpostorRevealActivity::class.java).apply {
                putStringArrayListExtra("PLAYERS", listaJugadores)
                putParcelableArrayListExtra("CATEGORIES", categoriasAEnviar)
                putExtra("OPCIONES", opciones)
            }

            startActivity(intent)
        }

        switchModoLoco.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Ahora el modo loco está en true
                opciones = opciones.copy(modoLoco = true)
            } else {
                // Ahora el modo loco está en false
                opciones = opciones.copy(modoLoco = false)
            }
        }

        switchPista.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Ahora el modo loco está en true
                opciones = opciones.copy(pista = true)
            } else {
                // Ahora el modo loco está en false
                opciones = opciones.copy(pista = false)
            }
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