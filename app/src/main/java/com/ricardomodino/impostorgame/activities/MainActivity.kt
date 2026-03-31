package com.ricardomodino.impostorgame.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.adapters.CategoryAdapterMain
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel
import com.ricardomodino.impostorgame.bottomsheets.EditPlayersBottomSheet
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.adapters.PlayerAdapterMain
import com.ricardomodino.impostorgame.viewmodel.PlayerViewModel
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.bottomsheets.SelectCategoriesBottomSheet
import com.ricardomodino.impostorgame.bottomsheets.SelectDatosCategoriesBottomSheet
import com.ricardomodino.impostorgame.bottomsheets.MenuBottomSheet
import com.ricardomodino.impostorgame.bottomsheets.SelectGameModeBottomSheet
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel
import com.ricardomodino.impostorgame.viewmodel.MainViewModel
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.switchmaterial.SwitchMaterial

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : BaseGameActivity(),
    SelectCategoriesBottomSheet.Listener,
    SelectDatosCategoriesBottomSheet.Listener,
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
    private lateinit var cardOpcionPistaCompleta: CardView
    private lateinit var cardOpcionPrimeraLetra: CardView
    private lateinit var checkPistaCompleta: TextView
    private lateinit var checkPrimeraLetra: TextView
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
    private lateinit var cardViewCategoriasDatos: CardView
    private lateinit var textResumenCategoriasDatos: TextView
    private lateinit var datosViewModel: DatosCuriososViewModel
    private lateinit var mainViewModel: MainViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(
            when {
                ThemeManager.esFinal(this) -> R.layout.activity_main_final
                ThemeManager.esCarmesi(this) -> R.layout.activity_main_carmesi
                else -> R.layout.activity_main
            }
        )

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
        ImmersiveModeManager.applyRootInsets(main, includeBottomInset = false, includeTopInset = false)
        switchModoLoco = findViewById(R.id.switchModoLoco)
        cardOpcionPistaCompleta = findViewById(R.id.cardOpcionPistaCompleta)
        cardOpcionPrimeraLetra  = findViewById(R.id.cardOpcionPrimeraLetra)
        checkPistaCompleta      = findViewById(R.id.checkPistaCompleta)
        checkPrimeraLetra       = findViewById(R.id.checkPrimeraLetra)

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

        cardViewCategoriasDatos  = findViewById(R.id.cardViewCategoriasDatos)
        textResumenCategoriasDatos = findViewById(R.id.textResumenCategoriasDatos)

        txtResumenImpostores.text = ""
        txtResumenSenoresBlancos.text = ""
        textResumenCategorias.text = ""
        textResumenCategoriasDatos.text = ""
        txtNumMinutos.text = ""

        playerViewModel   = ViewModelProvider(this).get(PlayerViewModel::class.java)
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        datosViewModel    = ViewModelProvider(this).get(DatosCuriososViewModel::class.java)
        mainViewModel     = ViewModelProvider(this).get(MainViewModel::class.java)

        val playersLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        playersRecyclerView.layoutManager = playersLayoutManager
        val playerAdapter = PlayerAdapterMain(playerViewModel.players.value ?: emptyList())
        playersRecyclerView.adapter = playerAdapter
        playerViewModel.players.observe(this) { lista ->
            playerAdapter.updatePlayers(lista)
            val opts = mainViewModel.opcionesActuales
            actualizarResumenImpostores(opts)
            actualizarResumenSenoresBlancos(opts)
            actualizarBotonEmpezar(opts)
        }

        val categoriesLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        categoriesRecyclerView.layoutManager = categoriesLayoutManager
        categoryAdapterMain = CategoryAdapterMain(emptyList())
        categoriesRecyclerView.adapter = categoryAdapterMain

        // Observer central: cualquier cambio en opciones actualiza toda la UI
        mainViewModel.opciones.observe(this) { opts ->
            actualizarResumenImpostores(opts)
            actualizarResumenSenoresBlancos(opts)
            actualizarBotonEmpezar(opts)
            if (switchModoLoco.isChecked != opts.modoLoco) switchModoLoco.isChecked = opts.modoLoco
            if (switchTiempoLimitado.isChecked != opts.tiempoLimitado) switchTiempoLimitado.isChecked = opts.tiempoLimitado
            if (switchCamara.isChecked != opts.camaraActiva) switchCamara.isChecked = opts.camaraActiva
            txtNumMinutos.text = getString(R.string.main_duration_value, opts.minutos)
            layoutSelectorMinutos.visibility = if (opts.tiempoLimitado) View.VISIBLE else View.GONE
            txtModoJuegoSeleccionado.text = textoModoJuegoSeleccionado(opts)
            cardViewNumSenoresBlancos.visibility = if (opts.modoMisterioso) View.VISIBLE else View.GONE
            actualizarSeleccionPista(opts)
            val cardPista = findViewById<androidx.cardview.widget.CardView>(R.id.cardViewPistaImpostor)
            val cardLoco  = findViewById<androidx.cardview.widget.CardView>(R.id.cardViewModoLoco)
            if (opts.modoMisterioso || opts.modoDatosCuriosos) {
                cardPista.visibility = View.GONE
                cardLoco.visibility  = View.GONE
            } else {
                cardPista.visibility = View.VISIBLE
                cardLoco.visibility  = View.VISIBLE
            }
            cardViewCategorias.visibility     = if (opts.modoDatosCuriosos) View.GONE  else View.VISIBLE
            cardViewCategoriasDatos.visibility = if (opts.modoDatosCuriosos) View.VISIBLE else View.GONE
        }

        SelfieManager.init(cacheDir)
        aplicarDrawablesTema()
        lanzarEventos()
    }

    private fun numJugadores() = playerViewModel.players.value?.size ?: 3


    private fun actualizarBotonEmpezar(opts: GameOptions) {
        btnStartGame.alpha = if (mainViewModel.esConfiguracionValida(numJugadores(), opts)) 1f else 0.4f
    }

    private fun actualizarResumenImpostores(opts: GameOptions) {
        val imp = opts.numImpostores
        val civs = mainViewModel.civiles(numJugadores(), opts)
        val impText = resources.getQuantityString(R.plurals.main_count_impostor, imp, imp)
        val civText = resources.getQuantityString(R.plurals.main_count_civil, civs, civs)
        txtResumenImpostores.text = getString(R.string.main_summary_pair, impText, civText)
        txtNumImpostores.text = imp.toString()
    }

    private fun actualizarResumenSenoresBlancos(opts: GameOptions) {
        val blancos = opts.numSenoresBlancos
        val civs = mainViewModel.civiles(numJugadores(), opts)
        val blancosText = resources.getQuantityString(R.plurals.main_count_white_mister, blancos, blancos)
        val civText = resources.getQuantityString(R.plurals.main_count_civil, civs, civs)
        txtResumenSenoresBlancos.text = getString(R.string.main_summary_pair, blancosText, civText)
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
            textResumenCategorias.text = resumenCategorias(total, seleccionadas)
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
        btnMasImpostores.setOnClickListener { mainViewModel.incrementarImpostores(numJugadores()) }
        btnMenosImpostores.setOnClickListener { mainViewModel.decrementarImpostores(numJugadores()) }

        // ── Selector señores blancos ──
        btnMasSenoresBlancos.setOnClickListener { mainViewModel.incrementarBlancos(numJugadores()) }
        btnMenosSenoresBlancos.setOnClickListener { mainViewModel.decrementarBlancos(numJugadores()) }

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

        // ── Categorías Datos Curiosos ──
        datosViewModel.categorias.observe(this) { list ->
            val total = list.size
            val sel   = list.count { it.isSelected }
            textResumenCategoriasDatos.text = resumenCategorias(total, sel)
        }
        cardViewCategoriasDatos.setOnClickListener {
            SelectDatosCategoriesBottomSheet().show(supportFragmentManager, SelectDatosCategoriesBottomSheet.TAG)
        }

        // ── Modo de juego ──
        cardViewSeleccionModo.setOnClickListener {
            SelectGameModeBottomSheet.newInstance(mainViewModel.opcionesActuales)
                .show(supportFragmentManager, SelectGameModeBottomSheet.TAG)
        }

        // ── Menú ──
        btnMenu.setOnClickListener {
            MenuBottomSheet().show(supportFragmentManager, MenuBottomSheet.TAG)
        }

        // ── Switches ──
        switchModoLoco.setOnCheckedChangeListener { _, isChecked ->
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(modoLoco = isChecked))
        }
        switchTiempoLimitado.setOnCheckedChangeListener { _, isChecked ->
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(tiempoLimitado = isChecked))
        }
        switchCamara.setOnCheckedChangeListener { _, isChecked ->
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(camaraActiva = isChecked))
        }

        // ── Minutos ──
        btnMasMinutos.setOnClickListener {
            val nuevo = (mainViewModel.opcionesActuales.minutos + 1).coerceAtMost(10)
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(minutos = nuevo))
        }
        btnMenosMinutos.setOnClickListener {
            val nuevo = (mainViewModel.opcionesActuales.minutos - 1).coerceAtLeast(1)
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(minutos = nuevo))
        }

        // ── Tipo de pista ──
        cardOpcionPistaCompleta.setOnClickListener {
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(tipoPista = GameOptions.PISTA_COMPLETA))
        }
        cardOpcionPrimeraLetra.setOnClickListener {
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(tipoPista = GameOptions.PRIMERA_LETRA))
        }

        // ── Botón empezar ──
        btnStartGame.setOnClickListener {
            val opts = mainViewModel.opcionesActuales
            val nj   = numJugadores()
            if (!mainViewModel.esConfiguracionValida(nj, opts)) {
                val noCiviles = opts.numImpostores + (if (opts.modoMisterioso) opts.numSenoresBlancos else 0)
                val mensaje = when {
                    opts.modoMisterioso && (opts.numImpostores + opts.numSenoresBlancos) == 0 ->
                        getString(R.string.main_invalid_need_non_civil_mysterious)
                    !opts.modoMisterioso && opts.numImpostores == 0 ->
                        getString(R.string.main_invalid_need_impostor)
                    noCiviles > nj / 2 ->
                        getString(R.string.main_invalid_too_many_non_civilians, nj, nj / 2)
                    else ->
                        getString(R.string.main_invalid_generic)
                }
                mensajeAlerta(getString(R.string.main_invalid_config_title), mensaje)
                return@setOnClickListener
            }
            val intent = Intent(this, CountdownActivity::class.java).apply {
                putParcelableArrayListExtra(IntentKeys.PLAYERS, ArrayList(playerViewModel.players.value ?: emptyList()))
                putParcelableArrayListExtra(IntentKeys.CATEGORIES, ArrayList(categoryViewModel.categories.value ?: emptyList()))
                putExtra(IntentKeys.OPCIONES, opts)
            }
            SelfieManager.clear()
            mainViewModel.guardar()
            startGameLauncher.launch(intent)
        }
    }

    override fun onGameModeConfirmed(nuevasOpciones: GameOptions) {
        // La lógica de negocio (resetear modoLoco, validar impostores, etc.)
        // vive en el ViewModel; el observer central actualiza toda la UI.
        mainViewModel.confirmarModoJuego(nuevasOpciones, numJugadores())
    }

    private fun actualizarSeleccionPista(opts: GameOptions) {
        val pistaCompleta = opts.tipoPista == GameOptions.PISTA_COMPLETA
        checkPistaCompleta.visibility = if (pistaCompleta) View.VISIBLE else View.GONE
        checkPrimeraLetra.visibility  = if (!pistaCompleta) View.VISIBLE else View.GONE
    }

    private var originalColor: Int = 0
    private var originalColorsSaved = false

    fun mensajeAlerta(titulo: String, mensaje: String) {
        GameDialog(this)
            .icon("!")
            .title(titulo)
            .message(mensaje)
            .positiveButton(getString(R.string.dialog_ok))
            .show()
    }

    override fun onDatosCategoriesConfirmed() {
        val list  = datosViewModel.categorias.value ?: emptyList()
        val total = list.size
        val sel   = list.count { it.isSelected }
        textResumenCategoriasDatos.text = resumenCategorias(total, sel)
    }

    override fun onCategoriesConfirmed(selected: List<Category>) {
        val total = categoryViewModel.categories.value?.size ?: 0
        val seleccionadas = selected.size
        textResumenCategorias.text = resumenCategorias(total, seleccionadas)
    }

    private fun resumenCategorias(total: Int, seleccionadas: Int): String =
        if (seleccionadas == 0) getString(R.string.main_categories_available, total)
        else getString(R.string.main_categories_selected, seleccionadas, total)

    private fun aplicarDrawablesTema() {
        ThemeManager.aplicarDrawables(this)
    }

    private fun textoModoJuegoSeleccionado(opts: GameOptions): String = when {
        opts.modoMisterioso    -> getString(if (ThemeManager.esCarmesi(this)) R.string.main_modo_misterioso_carmesi else R.string.main_modo_misterioso)
        opts.modoDatosCuriosos -> getString(if (ThemeManager.esCarmesi(this)) R.string.main_modo_datos_curiosos_carmesi else R.string.main_modo_datos_curiosos)
        else                   -> getString(if (ThemeManager.esCarmesi(this)) R.string.main_modo_clasico_carmesi else R.string.main_modo_clasico)
    }

}
