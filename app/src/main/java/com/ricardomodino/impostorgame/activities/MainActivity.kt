package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.adapters.CategoryAdapterMain
import com.ricardomodino.impostorgame.adapters.PlayerAdapterMain
import com.ricardomodino.impostorgame.bottomsheets.*
import com.ricardomodino.impostorgame.databinding.ActivityMainBinding
import com.ricardomodino.impostorgame.managers.*
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.viewmodel.*

class MainActivity : BaseGameActivity(),
    SelectCategoriesBottomSheet.Listener,
    SelectDatosCategoriesBottomSheet.Listener,
    SelectGameModeBottomSheet.Listener {

    private lateinit var binding: ActivityMainBinding
    
    private lateinit var mainViewModel: MainViewModel
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var datosViewModel: DatosCuriososViewModel

    private lateinit var categoryAdapterMain: CategoryAdapterMain
    private lateinit var playerAdapter: PlayerAdapterMain

    private val startGameLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updated = result.data
                    ?.getParcelableArrayListExtra<Category>("UPDATED_CATEGORIES")
                    ?.toList()
                if (updated != null) categoryViewModel.setCategories(updated)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // El tema se aplica en BaseGameActivity.onCreate()
        if (ThemeManager.esFinal(this)) {
            val root = layoutInflater.inflate(R.layout.activity_main_final, null)
            binding = ActivityMainBinding.bind(root)
            setContentView(root)
        } else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ImmersiveModeManager.applyRootInsets(binding.main, includeBottomInset = false, includeTopInset = false)
        SelfieManager.init(cacheDir)
        
        setupViewModels()
        setupRecyclerViews()
        setupObservers()
        setupListeners()
        
        ThemeManager.aplicarDrawables(this)
    }

    private fun setupViewModels() {
        mainViewModel     = ViewModelProvider(this).get(MainViewModel::class.java)
        playerViewModel   = ViewModelProvider(this).get(PlayerViewModel::class.java)
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        datosViewModel    = ViewModelProvider(this).get(DatosCuriososViewModel::class.java)
    }

    private fun setupRecyclerViews() {
        // Jugadores
        playerAdapter = PlayerAdapterMain(emptyList())
        binding.playersRecyclerView.apply {
            layoutManager = FlexboxLayoutManager(this@MainActivity).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            adapter = playerAdapter
            isNestedScrollingEnabled = false
        }

        // Categorías
        categoryAdapterMain = CategoryAdapterMain(emptyList())
        binding.categoriesRecyclerView.apply {
            layoutManager = FlexboxLayoutManager(this@MainActivity).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoryAdapterMain
        }
    }

    private fun setupObservers() {
        // Opciones y Estado Global
        mainViewModel.opciones.observe(this) { opts ->
            actualizarInterfaz(opts)
        }

        mainViewModel.eventos.observe(this) { evento ->
            when (evento) {
                is MainEvent.MostrarAjuste -> {
                    mostrarToastAjuste(evento.ajuste)
                    mainViewModel.consumirEvento()
                }
                null -> {}
            }
        }

        // Jugadores
        playerViewModel.players.observe(this) { lista ->
            playerAdapter.updatePlayers(lista)
            if (supportFragmentManager.findFragmentByTag("EditPlayers") != null) {
                mainViewModel.ajustarNoCivilesSiNecesario(lista.size)
            }
            actualizarResumenes(mainViewModel.opcionesActuales)
        }

        // Categorías
        categoryViewModel.categories.observe(this) { list ->
            val seleccionadasList = list.filter { it.isSelected }
            categoryAdapterMain.updateCategories(seleccionadasList.ifEmpty { list })
            binding.textResumenCategorias.text = resumenCategorias(list.size, seleccionadasList.size)
        }

        // Datos Curiosos
        datosViewModel.categorias.observe(this) { list ->
            binding.textResumenCategoriasDatos.text = resumenCategorias(list.size, list.count { it.isSelected })
        }
    }

    private fun setupListeners() {
        binding.btnMenu.setOnClickListener {
            MenuBottomSheet().show(supportFragmentManager, MenuBottomSheet.TAG)
        }

        binding.cardViewModoJuego.setOnClickListener {
            EditPlayersBottomSheet().show(supportFragmentManager, "EditPlayers")
        }

        // Selector impostores
        binding.btnMasImpostores.setOnClickListener { mainViewModel.incrementarImpostores(numJugadores()) }
        binding.btnMenosImpostores.setOnClickListener { mainViewModel.decrementarImpostores(numJugadores()) }

        // Selector señores blancos
        binding.btnMasSenoresBlancos.setOnClickListener { mainViewModel.incrementarBlancos(numJugadores()) }
        binding.btnMenosSenoresBlancos.setOnClickListener { mainViewModel.decrementarBlancos(numJugadores()) }

        // Categorías
        binding.cardViewCategorias.setOnClickListener {
            SelectCategoriesBottomSheet().show(supportFragmentManager, SelectCategoriesBottomSheet.TAG)
        }
        binding.cardViewCategoriasDatos.setOnClickListener {
            SelectDatosCategoriesBottomSheet().show(supportFragmentManager, SelectDatosCategoriesBottomSheet.TAG)
        }

        // Modo de juego
        binding.cardViewSeleccionModo.setOnClickListener {
            SelectGameModeBottomSheet.newInstance(mainViewModel.opcionesActuales)
                .show(supportFragmentManager, SelectGameModeBottomSheet.TAG)
        }

        // Switches
        binding.switchModoLoco.setOnCheckedChangeListener { _, isChecked ->
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(modoLoco = isChecked))
        }
        binding.switchTiempoLimitado.setOnCheckedChangeListener { _, isChecked ->
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(tiempoLimitado = isChecked))
        }
        binding.switchCamara.setOnCheckedChangeListener { _, isChecked ->
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(camaraActiva = isChecked))
        }

        // Minutos
        binding.btnMasMinutos.setOnClickListener {
            val nuevo = (mainViewModel.opcionesActuales.minutos + 1).coerceAtMost(10)
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(minutos = nuevo))
        }
        binding.btnMenosMinutos.setOnClickListener {
            val nuevo = (mainViewModel.opcionesActuales.minutos - 1).coerceAtLeast(1)
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(minutos = nuevo))
        }

        // Tipo de pista
        binding.cardOpcionPistaCompleta.setOnClickListener {
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(tipoPista = GameOptions.PISTA_COMPLETA))
        }
        binding.cardOpcionPrimeraLetra.setOnClickListener {
            mainViewModel.actualizarOpciones(mainViewModel.opcionesActuales.copy(tipoPista = GameOptions.PRIMERA_LETRA))
        }

        binding.btnStartGame.setOnClickListener { intentarEmpezarPartida() }
    }

    private fun actualizarInterfaz(opts: GameOptions) {
        actualizarResumenes(opts)
        
        with(binding) {
            if (switchModoLoco.isChecked != opts.modoLoco) switchModoLoco.isChecked = opts.modoLoco
            if (switchTiempoLimitado.isChecked != opts.tiempoLimitado) switchTiempoLimitado.isChecked = opts.tiempoLimitado
            if (switchCamara.isChecked != opts.camaraActiva) switchCamara.isChecked = opts.camaraActiva
            
            txtNumMinutos.text = getString(R.string.main_duration_value, opts.minutos)
            layoutSelectorMinutos.visibility = if (opts.tiempoLimitado) View.VISIBLE else View.GONE
            txtModoJuegoSeleccionado.text = textoModoJuegoSeleccionado(opts)
            cardViewNumSenoresBlancos.visibility = if (opts.modoMisterioso) View.VISIBLE else View.GONE
            
            actualizarSeleccionPista(opts)
            
            val visibilityEspeciales = if (opts.modoMisterioso || opts.modoDatosCuriosos) View.GONE else View.VISIBLE
            cardViewPistaImpostor.visibility = visibilityEspeciales
            cardViewModoLoco.visibility      = visibilityEspeciales
            
            cardViewCategorias.visibility      = if (opts.modoDatosCuriosos) View.GONE else View.VISIBLE
            cardViewCategoriasDatos.visibility = if (opts.modoDatosCuriosos) View.VISIBLE else View.GONE
            
            btnStartGame.alpha = if (mainViewModel.esConfiguracionValida(numJugadores(), opts)) 1f else 0.4f
        }
    }

    private fun actualizarResumenes(opts: GameOptions) {
        val nj = numJugadores()
        val civs = mainViewModel.civiles(nj, opts)
        val civText = resources.getQuantityString(R.plurals.main_count_civil, civs, civs)

        // Impostores
        val imp = opts.numImpostores
        val impText = resources.getQuantityString(R.plurals.main_count_impostor, imp, imp)
        binding.txtResumenImpostores.text = getString(R.string.main_summary_pair, impText, civText)
        binding.txtNumImpostores.text = imp.toString()

        // Señores Blancos
        val blancos = opts.numSenoresBlancos
        val blancosText = resources.getQuantityString(R.plurals.main_count_white_mister, blancos, blancos)
        binding.txtResumenSenoresBlancos.text = getString(R.string.main_summary_pair, blancosText, civText)
        binding.txtNumSenoresBlancos.text = blancos.toString()
    }

    private fun intentarEmpezarPartida() {
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
            return
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

    override fun onGameModeConfirmed(nuevasOpciones: GameOptions) {
        mainViewModel.confirmarModoJuego(nuevasOpciones, numJugadores())
    }

    private fun actualizarSeleccionPista(opts: GameOptions) {
        val pistaCompleta = opts.tipoPista == GameOptions.PISTA_COMPLETA
        binding.checkPistaCompleta.visibility = if (pistaCompleta) View.VISIBLE else View.GONE
        binding.checkPrimeraLetra.visibility  = if (!pistaCompleta) View.VISIBLE else View.GONE
    }

    private fun numJugadores() = playerViewModel.players.value?.size ?: 3

    private fun mostrarToastAjuste(ajuste: AjusteNoCiviles) {
        val msg = if (ajuste.modoMisterioso) {
            getString(R.string.main_ajuste_nociviles, ajuste.impostoresDespues, ajuste.blancosDespues)
        } else {
            getString(R.string.main_ajuste_impostores, ajuste.impostoresDespues)
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun mensajeAlerta(titulo: String, mensaje: String) {
        GameDialog(this)
            .icon("!")
            .title(titulo)
            .message(mensaje)
            .positiveButton(getString(R.string.dialog_ok))
            .show()
    }

    override fun onDatosCategoriesConfirmed() {
        // El observer de datosViewModel ya actualiza la UI
    }

    override fun onCategoriesConfirmed(selected: List<Category>) {
        // El observer de categoryViewModel ya actualiza la UI
    }

    private fun resumenCategorias(total: Int, seleccionadas: Int): String =
        if (seleccionadas == 0) getString(R.string.main_categories_available, total)
        else getString(R.string.main_categories_selected, seleccionadas, total)

    private fun textoModoJuegoSeleccionado(opts: GameOptions): String = when {
        opts.modoMisterioso    -> getString(if (ThemeManager.esCarmesi(this)) R.string.main_modo_misterioso_carmesi else R.string.main_modo_misterioso)
        opts.modoDatosCuriosos -> getString(if (ThemeManager.esCarmesi(this)) R.string.main_modo_datos_curiosos_carmesi else R.string.main_modo_datos_curiosos)
        else                   -> getString(if (ThemeManager.esCarmesi(this)) R.string.main_modo_clasico_carmesi else R.string.main_modo_clasico)
    }
}
