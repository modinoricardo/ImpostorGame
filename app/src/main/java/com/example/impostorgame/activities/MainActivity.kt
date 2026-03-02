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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.impostorgame.modelos.Category
import com.example.impostorgame.CategoryAdapterMain
import com.example.impostorgame.CategoryViewModel
import com.example.impostorgame.bottomsheets.EditPlayersBottomSheet
import com.example.impostorgame.modelos.GameOptions
import com.example.impostorgame.PlayerAdapterMain
import com.example.impostorgame.PlayerViewModel
import com.example.impostorgame.R
import com.example.impostorgame.bottomsheets.SelectCategoriesBottomSheet
import com.example.impostorgame.bottomsheets.MenuBottomSheet
import com.example.impostorgame.bottomsheets.SelectGameModeBottomSheet
import com.example.impostorgame.managers.ThemeManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.switchmaterial.SwitchMaterial

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(),
    SelectCategoriesBottomSheet.Listener,
    SelectGameModeBottomSheet.Listener {

    private lateinit var cardViewModoJuego: CardView
    private lateinit var cardViewSeleccionModo: CardView
    private lateinit var cardViewNumSenoresBlancos: CardView
    private lateinit var txtModoJuegoSeleccionado: TextView
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
    private lateinit var switchTiempoLimitado: SwitchMaterial
    private lateinit var switchCamara: SwitchMaterial
    private lateinit var btnMasMinutos: TextView
    private lateinit var btnMenosMinutos: TextView
    private lateinit var txtNumMinutos: TextView
    private lateinit var layoutSelectorMinutos: android.widget.LinearLayout
    private lateinit var btnStartGame: Button
    private lateinit var btnMenu: TextView

    // Selectores de impostores
    private lateinit var btnMasImpostores: TextView
    private lateinit var btnMenosImpostores: TextView
    private lateinit var txtNumImpostores: TextView
    private lateinit var txtResumenImpostores: TextView

    // Selectores de señores blancos
    private lateinit var btnMasSenoresBlancos: TextView
    private lateinit var btnMenosSenoresBlancos: TextView
    private lateinit var txtNumSenoresBlancos: TextView
    private lateinit var txtResumenSenoresBlancos: TextView

    private var originalCategoriasColor: Int = 0
    private var originalCategoriasColorsSaved = false
    private lateinit var opciones: GameOptions

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)  // ← aplicar ANTES de setContentView
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        main = findViewById(R.id.main)
        playersRecyclerView = findViewById(R.id.playersRecyclerView)
        cardViewModoJuego = findViewById(R.id.cardViewModoJuego)
        cardViewCategorias = findViewById(R.id.cardViewCategorias)
        cardViewSeleccionModo = findViewById(R.id.cardViewSeleccionModo)
        cardViewNumSenoresBlancos = findViewById(R.id.cardViewNumSenoresBlancos)
        txtModoJuegoSeleccionado = findViewById(R.id.txtModoJuegoSeleccionado)
        overlay = findViewById(R.id.darkOverlay)
        textResumenCategorias = findViewById(R.id.textResumenCategorias)
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        btnStartGame = findViewById(R.id.btnStartGame)
        btnMenu = findViewById(R.id.btnMenu)
        switchModoLoco = findViewById(R.id.switchModoLoco)
        switchPista = findViewById(R.id.switchPista)

        switchTiempoLimitado = findViewById(R.id.switchTiempoLimitado)
        switchCamara        = findViewById(R.id.switchCamara)
        btnMasMinutos       = findViewById(R.id.btnMasMinutos)
        btnMenosMinutos     = findViewById(R.id.btnMenosMinutos)
        txtNumMinutos       = findViewById(R.id.txtNumMinutos)
        layoutSelectorMinutos = findViewById(R.id.layoutSelectorMinutos)

        btnMasImpostores = findViewById(R.id.btnMasImpostores)
        btnMenosImpostores = findViewById(R.id.btnMenosImpostores)
        txtNumImpostores = findViewById(R.id.txtNumImpostores)
        txtResumenImpostores = findViewById(R.id.txtResumenImpostores)

        btnMasSenoresBlancos = findViewById(R.id.btnMasSenoresBlancos)
        btnMenosSenoresBlancos = findViewById(R.id.btnMenosSenoresBlancos)
        txtNumSenoresBlancos = findViewById(R.id.txtNumSenoresBlancos)
        txtResumenSenoresBlancos = findViewById(R.id.txtResumenSenoresBlancos)

        playersRecyclerView.isNestedScrollingEnabled = false

        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val top = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            ).top
            v.updatePadding(top = top)
            insets
        }

        val baseBottomMargin = (btnStartGame.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(btnStartGame) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            (v.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = baseBottomMargin + nav.bottom
            v.requestLayout()
            insets
        }

        playerViewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)

        val playersLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        playersRecyclerView.layoutManager = playersLayoutManager
        val playerAdapter = PlayerAdapterMain(playerViewModel.players.value ?: emptyList())
        playersRecyclerView.adapter = playerAdapter
        playerViewModel.players.observe(this) { lista ->
            playerAdapter.updatePlayers(lista)
            actualizarResumenImpostores()
            actualizarResumenSenoresBlancos()
            actualizarBotonEmpezar()
        }

        val categoriesLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        categoriesRecyclerView.layoutManager = categoriesLayoutManager
        categoryAdapterMain = CategoryAdapterMain(emptyList())
        categoriesRecyclerView.adapter = categoryAdapterMain

        opciones = GameOptions(pista = true, modoLoco = false, modoMisterioso = false, numImpostores = 1, numSenoresBlancos = 0)

        actualizarResumenImpostores()
        actualizarResumenSenoresBlancos()
        actualizarBotonEmpezar()

        SelfieManager.init(cacheDir)
        aplicarDrawablesTema()
        lanzarEventos()
    }

    private fun numJugadores() = playerViewModel.players.value?.size ?: 3

    private fun civiles(impostores: Int = opciones.numImpostores, blancos: Int = opciones.numSenoresBlancos): Int {
        return numJugadores() - impostores - (if (opciones.modoMisterioso) blancos else 0)
    }

    private fun esConfiguracionValida(impostores: Int = opciones.numImpostores, blancos: Int = opciones.numSenoresBlancos): Boolean {
        val total = numJugadores()
        val noCiviles = impostores + (if (opciones.modoMisterioso) blancos else 0)
        val hayAlMenosUnNoCivil = if (opciones.modoMisterioso) (impostores + blancos) > 0 else impostores > 0
        return hayAlMenosUnNoCivil && noCiviles <= total / 2
    }

    private fun actualizarBotonEmpezar() {
        val valido = esConfiguracionValida()
        btnStartGame.alpha = if (valido) 1f else 0.4f
    }

    private fun actualizarResumenImpostores() {
        val imp = opciones.numImpostores
        val civs = civiles()
        txtResumenImpostores.text = "$imp impostor${if (imp > 1) "es" else ""} · $civs civil${if (civs != 1) "es" else ""}"
        txtNumImpostores.text = imp.toString()
    }

    private fun actualizarResumenSenoresBlancos() {
        val blancos = opciones.numSenoresBlancos
        val civs = civiles()
        txtResumenSenoresBlancos.text = "$blancos señor${if (blancos > 1) "es" else ""} blanco${if (blancos > 1) "s" else ""} · $civs civil${if (civs != 1) "es" else ""}"
        txtNumSenoresBlancos.text = blancos.toString()
    }

    private val startGameLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updated = result.data
                    ?.getParcelableArrayListExtra<Category>("UPDATED_CATEGORIES")
                    ?.toList()
                if (updated != null) categoryViewModel.setCategories(updated)
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    fun lanzarEventos() {

        categoryViewModel.categories.observe(this) { list ->
            val seleccionadasList = list.filter { it.isSelected }
            val categoriasParaMostrar = seleccionadasList.ifEmpty { list }
            categoryAdapterMain.updateCategories(categoriasParaMostrar)
            val total = list.size
            val seleccionadas = seleccionadasList.size
            textResumenCategorias.text = if (seleccionadas == 0) "Categorías disponibles: $total"
            else "Categorías seleccionadas: $seleccionadas de $total"
        }

        // ── Jugadores ──
        cardViewModoJuego.setOnClickListener {
            EditPlayersBottomSheet().show(supportFragmentManager, "EditPlayers")
        }
        cardViewModoJuego.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!originalColorsSaved) { originalColor = cardViewModoJuego.cardBackgroundColor.defaultColor; originalColorsSaved = true }
                    cardViewModoJuego.setCardBackgroundColor(getColor(R.color.button_pressed))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> cardViewModoJuego.setCardBackgroundColor(originalColor)
            }
            false
        }
        playersRecyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!originalColorsSaved) { originalColor = cardViewModoJuego.cardBackgroundColor.defaultColor; originalColorsSaved = true }
                    cardViewModoJuego.setCardBackgroundColor(getColor(R.color.button_pressed))
                }
                MotionEvent.ACTION_UP -> { cardViewModoJuego.setCardBackgroundColor(originalColor); cardViewModoJuego.performClick() }
                MotionEvent.ACTION_CANCEL -> cardViewModoJuego.setCardBackgroundColor(originalColor)
            }
            false
        }

        // ── Selector impostores ──
        btnMasImpostores.setOnClickListener {
            val nuevo = opciones.numImpostores + 1
            val max = maxImpostoresPermitidos()
            if (nuevo <= max) {
                opciones = opciones.copy(numImpostores = nuevo)
                ajustarOpcionesALimites()
                actualizarResumenImpostores()
                actualizarResumenSenoresBlancos()
                actualizarBotonEmpezar()
            }
        }
        btnMenosImpostores.setOnClickListener {
            val nuevo = opciones.numImpostores - 1
            if (nuevo >= 0) {
                opciones = opciones.copy(numImpostores = nuevo)
                ajustarOpcionesALimites()
                actualizarResumenImpostores()
                actualizarResumenSenoresBlancos()
                actualizarBotonEmpezar()
            }
        }

        // ── Selector señores blancos ──
        btnMasSenoresBlancos.setOnClickListener {
            val nuevo = opciones.numSenoresBlancos + 1
            val max = maxBlancosPermitidos()
            if (nuevo <= max) {
                opciones = opciones.copy(numSenoresBlancos = nuevo)
                ajustarOpcionesALimites()
                actualizarResumenSenoresBlancos()
                actualizarBotonEmpezar()
            }
        }
        btnMenosSenoresBlancos.setOnClickListener {
            val nuevo = opciones.numSenoresBlancos - 1
            if (nuevo >= 0) {
                opciones = opciones.copy(numSenoresBlancos = nuevo)
                ajustarOpcionesALimites()
                actualizarResumenSenoresBlancos()
                actualizarBotonEmpezar()
            }
        }

        // ── Categorías ──
        cardViewCategorias.setOnClickListener {
            SelectCategoriesBottomSheet().show(supportFragmentManager, SelectCategoriesBottomSheet.Companion.TAG)
        }
        cardViewCategorias.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!originalCategoriasColorsSaved) { originalCategoriasColor = cardViewCategorias.cardBackgroundColor.defaultColor; originalCategoriasColorsSaved = true }
                    cardViewCategorias.setCardBackgroundColor(getColor(R.color.button_pressed))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> cardViewCategorias.setCardBackgroundColor(originalCategoriasColor)
            }
            false
        }
        categoriesRecyclerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!originalCategoriasColorsSaved) { originalCategoriasColor = cardViewCategorias.cardBackgroundColor.defaultColor; originalCategoriasColorsSaved = true }
                    cardViewCategorias.setCardBackgroundColor(getColor(R.color.button_pressed))
                }
                MotionEvent.ACTION_UP -> { cardViewCategorias.setCardBackgroundColor(originalCategoriasColor); cardViewCategorias.performClick() }
                MotionEvent.ACTION_CANCEL -> cardViewCategorias.setCardBackgroundColor(originalCategoriasColor)
            }
            false
        }

        // ── Modo de juego ──
        cardViewSeleccionModo.setOnClickListener {
            SelectGameModeBottomSheet.newInstance(opciones)
                .show(supportFragmentManager, SelectGameModeBottomSheet.TAG)
        }

        // ── Menú ──
        btnMenu.setOnClickListener {
            MenuBottomSheet().show(supportFragmentManager, MenuBottomSheet.TAG)
        }

        // ── Switches ──
        switchModoLoco.setOnCheckedChangeListener { _, isChecked -> opciones = opciones.copy(modoLoco = isChecked) }

        switchTiempoLimitado.setOnCheckedChangeListener { _, isChecked ->
            opciones = opciones.copy(tiempoLimitado = isChecked)
            layoutSelectorMinutos.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        switchCamara.setOnCheckedChangeListener { _, isChecked ->
            opciones = opciones.copy(camaraActiva = isChecked)
        }

        btnMasMinutos.setOnClickListener {
            val nuevo = (opciones.minutos + 1).coerceAtMost(10)
            opciones = opciones.copy(minutos = nuevo)
            txtNumMinutos.text = "$nuevo min"
        }

        btnMenosMinutos.setOnClickListener {
            val nuevo = (opciones.minutos - 1).coerceAtLeast(1)
            opciones = opciones.copy(minutos = nuevo)
            txtNumMinutos.text = "$nuevo min"
        }
        switchPista.setOnCheckedChangeListener { _, isChecked -> opciones = opciones.copy(pista = isChecked) }

        // ── Botón empezar ──
        btnStartGame.setOnClickListener {
            if (!esConfiguracionValida()) {
                val total = numJugadores()
                val noCiviles = opciones.numImpostores + (if (opciones.modoMisterioso) opciones.numSenoresBlancos else 0)
                val mensaje = when {
                    opciones.modoMisterioso && (opciones.numImpostores + opciones.numSenoresBlancos) == 0 ->
                        "Debe haber al menos un impostor o un señor blanco para poder jugar."
                    !opciones.modoMisterioso && opciones.numImpostores == 0 ->
                        "Debe haber al menos un impostor para poder jugar."
                    noCiviles > total / 2 ->
                        "Hay demasiados no civiles. Con $total jugadores puede haber como máximo ${total / 2} no civiles."
                    else ->
                        "La configuración no es válida. Revisa el número de impostores y señores blancos."
                }
                mensajeAlerta("Configuración inválida", mensaje)
                return@setOnClickListener
            }
            val intent = Intent(this, CountdownActivity::class.java).apply {
                putParcelableArrayListExtra("PLAYERS", ArrayList(playerViewModel.players.value ?: emptyList()))
                putParcelableArrayListExtra("CATEGORIES", ArrayList(categoryViewModel.categories.value ?: emptyList()))
                putExtra("OPCIONES", opciones)
            }
            startGameLauncher.launch(intent)
        }
    }

    override fun onGameModeConfirmed(nuevasOpciones: GameOptions) {
        opciones = nuevasOpciones
        txtModoJuegoSeleccionado.text = if (opciones.modoMisterioso) "🌑 Misterioso" else "🕵️ Clásico"

        cardViewNumSenoresBlancos.visibility = if (opciones.modoMisterioso) View.VISIBLE else View.GONE

        if (!opciones.modoMisterioso) {
            opciones = opciones.copy(numSenoresBlancos = 0)
        } else {
            // En modo misterioso: ocultar y desactivar pista y modo loco
            opciones = opciones.copy(pista = false, modoLoco = false)
            switchPista.isChecked = false
            switchModoLoco.isChecked = false
        }

        // Ocultar/mostrar cards según modo
        val cardPista = findViewById<androidx.cardview.widget.CardView>(R.id.cardViewPistaImpostor)
        val cardLoco  = findViewById<androidx.cardview.widget.CardView>(R.id.cardViewModoLoco)
        if (opciones.modoMisterioso) {
            cardPista.visibility = View.GONE
            cardLoco.visibility  = View.GONE
        } else {
            cardPista.visibility = View.VISIBLE
            cardLoco.visibility  = View.VISIBLE
        }

        if (!esConfiguracionValida(opciones.numImpostores, opciones.numSenoresBlancos)) {
            opciones = opciones.copy(numImpostores = 1, numSenoresBlancos = 0)
        }

        actualizarResumenImpostores()
        actualizarResumenSenoresBlancos()
        actualizarBotonEmpezar()
    }

    override fun onResume() { super.onResume() }
    fun onBottomSheetClosed() {}

    private var originalColor: Int = 0
    private var originalColorsSaved = false

    fun mensajeAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(this).setTitle(titulo).setMessage(mensaje).setPositiveButton("OK", null).show()
    }

    override fun onCategoriesConfirmed(selected: List<Category>) {
        val total = categoryViewModel.categories.value?.size ?: 0
        val seleccionadas = selected.size
        textResumenCategorias.text = if (seleccionadas == 0) "Categorías disponibles: $total"
        else "Categorías seleccionadas: $seleccionadas de $total"
    }

    private fun maxNoCiviles(): Int = numJugadores() / 2

    private fun maxImpostoresPermitidos(): Int {
        val max = maxNoCiviles()
        val blancos = if (opciones.modoMisterioso) opciones.numSenoresBlancos else 0
        return (max - blancos).coerceAtLeast(0)
    }

    private fun maxBlancosPermitidos(): Int {
        if (!opciones.modoMisterioso) return 0
        val max = maxNoCiviles()
        val imp = opciones.numImpostores
        return (max - imp).coerceAtLeast(0)
    }

    private fun ajustarOpcionesALimites() {
        val impMax = maxImpostoresPermitidos()
        val blancosMax = maxBlancosPermitidos()
        val imp = opciones.numImpostores.coerceIn(0, impMax)
        val blancos = opciones.numSenoresBlancos.coerceIn(0, blancosMax)
        opciones = opciones.copy(numImpostores = imp, numSenoresBlancos = blancos)
    }

    private fun aplicarDrawablesTema() {
        ThemeManager.aplicarDrawables(this)
    }
}