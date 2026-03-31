package com.ricardomodino.impostorgame.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.extensions.applyWordSafeText
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import com.ricardomodino.impostorgame.managers.PlayerImageManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.TipoJugador

/**
 * Pantalla de reveal para los estilos Clasico, Carmesi y JMC.
 * Hereda de BaseRevealActivity que gestiona toda la logica de juego.
 *
 * Mecanica: mantener pulsado para ver el rol/palabra, soltar para ocultar.
 * En estilo JMC: tap unico con animacion de revelacion deslizante.
 */
class ClassicRevealActivity : BaseRevealActivity() {

    // ── Vistas propias ──────────────────────────────────────────────────────
    private lateinit var detailsPlayer: TextView
    private lateinit var textNextPlayer: TextView
    private lateinit var layout: LinearLayout
    private lateinit var cardViewPrincipal: View
    private lateinit var imgDedo: ImageView
    private lateinit var txtTwo: TextView
    private lateinit var nenxtPlayer: CardView
    private lateinit var presText: TextView
    private lateinit var turnPlayerName: TextView
    private lateinit var hintPlayer: TextView
    private lateinit var imgWord: ImageView

    // Vistas especificas del estilo carmesi
    private var imgLetraSReveal: ImageView? = null
    private var ringRevealPrimary: View? = null
    private var ringRevealSecondary: View? = null
    private var txtEditionReveal: TextView? = null
    private val carmesiAnimators = mutableListOf<Animator>()
    private val particleViews = mutableListOf<View>()

    // Estado de la revelacion
    private var isAnimating = false
    private var holdRevealActivo = false
    private var pendingHoldReveal: Runnable? = null

    // Tamanio base del texto de detalle y pista (leido desde el layout)
    private var detailsPlayerBaseTextSp = 0f
    private var hintPlayerBaseTextSp = 0f

    // ── BaseRevealActivity: layout segun tema ───────────────────────────────
    override fun provideLayoutRes(): Int = when {
        ThemeManager.esFinal(this)   -> R.layout.activity_impostor_reveal_final
        ThemeManager.esCarmesi(this) -> R.layout.activity_impostor_reveal_carmesi
        else                         -> R.layout.activity_impostor_reveal
    }

    // ── Vistas de interaccion que usa la base ───────────────────────────────
    override val touchTarget: View get() = cardViewPrincipal
    override val btnSiguiente: View get() = nenxtPlayer
    override val txtBtnSiguiente: TextView get() = textNextPlayer

    // ── Bindeo de vistas ────────────────────────────────────────────────────
    override fun onBindViews() {
        detailsPlayer  = findViewById(R.id.detailsPlayer)
        layout         = findViewById(R.id.layoutCard)
        cardViewPrincipal = findViewById(R.id.cardViewPrincipal)
        imgDedo        = findViewById(R.id.imgDedo)
        txtTwo         = findViewById(R.id.txtTwo)
        nenxtPlayer    = findViewById(R.id.nenxtPlayer)
        presText       = findViewById(R.id.presText)
        turnPlayerName = findViewById(R.id.turnPlayerName)
        textNextPlayer = findViewById(R.id.textNextPlayer)
        hintPlayer     = findViewById(R.id.hintPlayer)
        imgWord        = findViewById(R.id.imgWord)

        detailsPlayerBaseTextSp = detailsPlayer.textSize / resources.displayMetrics.scaledDensity
        hintPlayerBaseTextSp    = hintPlayer.textSize / resources.displayMetrics.scaledDensity

        imgLetraSReveal     = findViewById(R.id.imgLetraS)
        ringRevealPrimary   = findViewById(R.id.ring1)
        ringRevealSecondary = findViewById(R.id.ring2)
        txtEditionReveal    = findViewById(R.id.txtStaicyEditionReveal)
        imgLetraSReveal?.visibility = if (ThemeManager.esCarmesi(this)) View.VISIBLE else View.GONE

        if (ThemeManager.esCarmesi(this)) {
            particleViews.clear()
            listOf(R.id.particle0, R.id.particle1, R.id.particle2,
                R.id.particle3, R.id.particle4, R.id.particle5).forEach { id ->
                findViewById<View?>(id)?.let { particleViews.add(it) }
            }
        }

        ThemeManager.aplicarDrawables(this)
        ImmersiveModeManager.applyRootInsets(findViewById(R.id.layout))
        ImmersiveModeManager.applyBottomMargin(nenxtPlayer)
    }

    // ── Mostrar datos del jugador actual ────────────────────────────────────
    override fun onShowPlayer(index: Int) {
        turnPlayerName.text = listaJugadores[index].nombre
        if (opciones.modoLoco && modoLocoActivo) cargarInformacionModoLoco()
        else cargarInformacionNormal()
        ocultarPalabra()
    }

    // ── Revelar contenido al mantener pulsado ──────────────────────────────
    override fun onRevealContent(index: Int) {
        animarAperturaRevealMantener()
    }

    // ── Ocultar contenido al soltar ─────────────────────────────────────────
    override fun onHideContent() {
        animarCierreRevealMantener()
    }

    // ── Configuracion del listener de toque (sobreescribe la base) ──────────
    @SuppressLint("ClickableViewAccessibility")
    override fun configurarInteraccion() {
        presText.text = getString(R.string.reveal_hold_to_reveal)
        cardViewPrincipal.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    cardViewPrincipal.parent?.requestDisallowInterceptTouchEvent(true)
                    animarAperturaRevealMantener()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    animarCierreRevealMantener()
                    true
                }
                else -> false
            }
        }
        nenxtPlayer.setOnClickListener {
            cancelarRevealMantenerPendiente()
            holdRevealActivo = false
            avanzarJugador()
        }
    }

    // ── Transicion entre jugadores ──────────────────────────────────────────
    // playerInGame ya fue incrementado por avanzarJugador() antes de llamar aqui
    override fun onPlayerTransition(onSwap: () -> Unit) {
        if (isAnimating) return
        isAnimating = true
        nenxtPlayer.isEnabled = false

        slideOutIn(cardViewPrincipal, outExtra = 120f) {
            nenxtPlayer.visibility = View.INVISIBLE
            // Cargar con el nuevo playerInGame (ya incrementado por la base)
            if (opciones.modoLoco && modoLocoActivo) cargarInformacionModoLoco()
            else cargarInformacionNormal()
            ocultarPalabra()
            val esUltimo = (playerInGame == listaJugadores.lastIndex)
            textNextPlayer.text = if (esUltimo) getString(R.string.reveal_empezar)
            else getString(R.string.reveal_siguiente)
            if (!opciones.modoDatosCuriosos) {
                categoryViewModel.deleteWordItem(categoriaInGame.id, wordItemInGame)
                if (esUltimo) categoryViewModel.logItems(categoriaInGame.id)
            }
        }

        cardViewPrincipal.postDelayed({
            nenxtPlayer.isEnabled = true
            isAnimating = false
        }, 420)
    }

    // ── Animacion de entrada carmesi (llamada desde la base en onCreate) ────
    override fun onPostSetup() {
        if (ThemeManager.esCarmesi(this)) {
            prepararEntradaCarmesi()
        }
    }

    // ── Animaciones de reveal al mantener pulsado ───────────────────────────
    private fun cancelarRevealMantenerPendiente() {
        pendingHoldReveal?.let { cardViewPrincipal.removeCallbacks(it) }
        pendingHoldReveal = null
    }

    private fun resetTransientState(vararg views: View) {
        views.forEach { view ->
            view.alpha = 1f
            view.translationX = 0f
            view.translationY = 0f
            view.scaleX = 1f
            view.scaleY = 1f
        }
    }

    private fun idleViewsVisibles(): List<View> =
        listOf(imgWord, imgDedo, txtTwo, presText).filter { it.visibility == View.VISIBLE }

    private fun revealViewsVisibles(): List<View> =
        listOf(detailsPlayer, hintPlayer).filter { it.visibility == View.VISIBLE }

    private fun animarAperturaRevealMantener() {
        if (holdRevealActivo) return
        holdRevealActivo = true
        cancelarRevealMantenerPendiente()

        val fadeOutMs = 300L

        val reposoViews = idleViewsVisibles()
        reposoViews.forEach { it.animate().cancel() }
        revealViewsVisibles().forEach { it.animate().cancel() }

        reposoViews.forEach { view ->
            view.animate()
                .alpha(0f)
                .setDuration(fadeOutMs)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        val revealRunnable = Runnable {
            pendingHoldReveal = null
            if (!holdRevealActivo) return@Runnable

            if (opciones.camaraActiva && imageCapture != null && playerInGame !in selfiesTomados) {
                selfiesTomados.add(playerInGame)
                tomarSelfie(playerInGame)
            }

            if (opciones.modoLoco && modoLocoActivo) mostrarPalabraModoLoco()
            else mostrarPalabraNormal()

            revealViewsVisibles().forEach { view ->
                view.alpha = 0f
                view.animate()
                    .alpha(1f)
                    .setDuration(350L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

            if (nenxtPlayer.visibility != View.VISIBLE) {
                nenxtPlayer.visibility = View.VISIBLE
                nenxtPlayer.alpha = 0f
                nenxtPlayer.animate()
                    .alpha(1f)
                    .setStartDelay(80L)
                    .setDuration(280L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }

            if (ThemeManager.esCarmesi(this)) animarRevelacionCarmesi()
        }

        pendingHoldReveal = revealRunnable
        cardViewPrincipal.postDelayed(revealRunnable, fadeOutMs)
    }

    private fun animarCierreRevealMantener() {
        cancelarRevealMantenerPendiente()
        if (!holdRevealActivo) return
        holdRevealActivo = false

        val fadeOutMs = 250L

        val revealViews = revealViewsVisibles()
        revealViews.forEach { it.animate().cancel() }
        idleViewsVisibles().forEach { it.animate().cancel() }

        revealViews.forEach { view ->
            view.animate()
                .alpha(0f)
                .setDuration(fadeOutMs)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        val restoreRunnable = Runnable {
            if (holdRevealActivo) return@Runnable
            ocultarPalabra()
            idleViewsVisibles().forEach { view ->
                view.alpha = 0f
                view.animate()
                    .alpha(1f)
                    .setDuration(300L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }

        if (revealViews.isEmpty()) restoreRunnable.run()
        else cardViewPrincipal.postDelayed(restoreRunnable, fadeOutMs)
    }

    // ── Cargar informacion del jugador ──────────────────────────────────────
    private fun cargarInformacionModoLoco() {
        turnPlayerName.text = listaJugadores[playerInGame].nombre
        imageResTurno = PlayerImageManager.getRandom(this)
        resetearTextoReveal()
        detailsPlayer.text = getString(R.string.reveal_you_are_impostor)
        detailsPlayer.setTextColor(getColor(R.color.colorImpostor))

        if (!pistaActivaModoLoco) {
            val wordItemRandom = listaCategorias.randomOrNull()?.items?.randomOrNull()
            if (wordItemRandom != null) {
                palabra = wordItemRandom.name
                pista   = wordItemRandom.hints.randomOrNull() ?: ""
            } else {
                palabra = ""; pista = ""
            }
        }
        aplicarPistaReveal(textoAyudaImpostor())
        hintPlayer.visibility = View.GONE
        pistaActivaModoLoco = true
    }

    private fun cargarInformacionNormal() {
        turnPlayerName.text = listaJugadores[playerInGame].nombre
        imageResTurno = imagenPorJugador[playerInGame]

        val esSenorBlanco = listaJugadores[playerInGame].tipo == TipoJugador.SENOR_BLANCO
        val esImpostor    = playerInGame in indicesImpostores

        when {
            esSenorBlanco -> {
                resetearTextoReveal()
                detailsPlayer.text = getString(R.string.reveal_mr_white_title) + "\n" + getString(R.string.reveal_no_word)
                detailsPlayer.setTextColor(getColor(R.color.colorImpostor))
                hintPlayer.text = ""; hintPlayer.visibility = View.GONE
            }
            esImpostor && opciones.modoMisterioso -> {
                aplicarTextoReveal(pistaMisteriosa)
                detailsPlayer.setTextColor(Color.WHITE)
                hintPlayer.text = ""; hintPlayer.visibility = View.GONE
            }
            esImpostor -> {
                resetearTextoReveal()
                detailsPlayer.text = getString(R.string.reveal_you_are_impostor)
                detailsPlayer.setTextColor(getColor(R.color.colorImpostor))
                val pistaImpostor = if (opciones.modoDatosCuriosos)
                    getString(R.string.datos_inventa_dato)
                else
                    textoAyudaImpostor()
                aplicarPistaReveal(pistaImpostor)
                hintPlayer.visibility = View.GONE
            }
            else -> {
                if (opciones.modoDatosCuriosos) {
                    val texto = datosAsignados[playerInGame]?.let { getTextoDato(it) } ?: ""
                    aplicarTextoReveal(texto)
                } else {
                    aplicarTextoReveal(palabra)
                }
                detailsPlayer.setTextColor(Color.WHITE)
                hintPlayer.text = ""; hintPlayer.visibility = View.GONE
            }
        }
    }

    // ── Estado reposo: ocultar palabra y mostrar imagen ─────────────────────
    private fun ocultarPalabra() {
        resetTransientState(detailsPlayer, hintPlayer, imgWord, imgDedo, txtTwo, presText)
        detailsPlayer.visibility = View.GONE
        hintPlayer.visibility    = View.GONE
        imgWord.visibility       = View.GONE
        txtTwo.visibility        = View.VISIBLE
        presText.visibility      = View.VISIBLE
        mostrarImagenReposo()
    }

    private fun mostrarImagenReposo() {
        val selfie = SelfieManager.getBitmap(listaJugadores[playerInGame].nombre)
        when {
            selfie != null -> {
                imgDedo.visibility = View.GONE
                imgWord.setImageBitmap(selfie)
                imgWord.visibility = View.VISIBLE
            }
            opciones.camaraActiva -> {
                imgDedo.setImageResource(R.drawable.ic_touch_app)
                imgDedo.visibility = View.VISIBLE
                imgWord.visibility = View.GONE
            }
            else -> {
                imgDedo.visibility = View.GONE
                val img = imageResTurno
                if (img != null) {
                    imgWord.setImageBitmap(img)
                    imgWord.visibility = View.VISIBLE
                } else {
                    imgWord.visibility = View.GONE
                }
            }
        }
    }

    private fun mostrarPalabraNormal() {
        cargarInformacionNormal()
        imgDedo.visibility  = View.GONE
        imgWord.visibility  = View.GONE
        txtTwo.visibility   = View.GONE
        presText.visibility = View.GONE

        val esImpostor    = playerInGame in indicesImpostores
        val esSenorBlanco = listaJugadores[playerInGame].tipo == TipoJugador.SENOR_BLANCO

        hintPlayer.visibility = if (esImpostor && !opciones.modoMisterioso && !esSenorBlanco)
            View.VISIBLE else View.GONE

        detailsPlayer.visibility = View.VISIBLE
    }

    private fun mostrarPalabraModoLoco() {
        cargarInformacionModoLoco()
        imgDedo.visibility  = View.GONE
        txtTwo.visibility   = View.GONE
        presText.visibility = View.GONE
        detailsPlayer.visibility = View.VISIBLE
        hintPlayer.visibility    = View.VISIBLE
    }

    // ── Utilidades de texto ─────────────────────────────────────────────────
    private fun resetearTextoReveal() {
        detailsPlayer.setTextSize(TypedValue.COMPLEX_UNIT_SP, detailsPlayerBaseTextSp)
        detailsPlayer.setSingleLine(false)
        detailsPlayer.maxLines = Int.MAX_VALUE
    }

    private fun resetearPistaReveal() {
        hintPlayer.setTextSize(TypedValue.COMPLEX_UNIT_SP, hintPlayerBaseTextSp)
        hintPlayer.setSingleLine(false)
        hintPlayer.maxLines = Int.MAX_VALUE
    }

    private fun aplicarTextoReveal(texto: String) {
        resetearTextoReveal()
        detailsPlayer.applyWordSafeText(
            rawText = texto,
            preferSingleLine = true,
            maxTextSp = detailsPlayerBaseTextSp,
            minTextSp = 14f,
            preferredSingleLineMinSp = 12f,
            preferredWrappedMaxLines = 3
        )
    }

    private fun aplicarPistaReveal(texto: String) {
        resetearPistaReveal()
        hintPlayer.applyWordSafeText(
            rawText = texto,
            preferSingleLine = true,
            maxTextSp = hintPlayerBaseTextSp,
            minTextSp = 14f,
            preferredSingleLineMinSp = 12f,
            preferredWrappedMaxLines = 3
        )
    }

    // ── Animacion de deslizamiento entre jugadores ──────────────────────────
    private fun slideOutIn(card: View, outExtra: Float = 0f, onSwap: () -> Unit) {
        val w = card.width.toFloat().takeIf { it > 0f } ?: return
        card.animate().cancel()
        card.animate()
            .translationX(-(w + outExtra)).alpha(0f).setDuration(180L)
            .withEndAction {
                onSwap()
                card.translationX = (w + outExtra); card.alpha = 0f
                card.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(180L)
                    .withEndAction {
                        if (ThemeManager.esCarmesi(this)) animarCambioJugadorCarmesi()
                    }
                    .start()
            }.start()
    }

    // ── Animaciones especificas del estilo carmesi ──────────────────────────
    private fun prepararEntradaCarmesi() {
        txtEditionReveal?.apply { alpha = 0f; translationY = -18f }
        cardViewPrincipal.apply {
            alpha = 0f; scaleX = 0.92f; scaleY = 0.92f; translationY = 58f
        }
        turnPlayerName.alpha = 0f; turnPlayerName.translationY = 18f
        txtTwo.alpha = 0f; txtTwo.translationY = 14f
        presText.alpha = 0f; presText.translationY = 14f
        imgDedo.alpha = 0f; imgDedo.scaleX = 0.84f; imgDedo.scaleY = 0.84f

        iniciarAnimacionAmbientalCarmesi()

        txtEditionReveal?.animate()
            ?.alpha(1f)?.translationY(0f)?.setDuration(420L)?.setInterpolator(DecelerateInterpolator())?.start()

        cardViewPrincipal.animate()
            .alpha(1f).scaleX(1f).scaleY(1f).translationY(0f)
            .setDuration(620L).setInterpolator(DecelerateInterpolator()).start()

        turnPlayerName.animate()
            .alpha(1f).translationY(0f).setStartDelay(120L).setDuration(420L)
            .setInterpolator(DecelerateInterpolator()).start()

        txtTwo.animate()
            .alpha(1f).translationY(0f).setStartDelay(180L).setDuration(360L)
            .setInterpolator(DecelerateInterpolator()).start()

        presText.animate()
            .alpha(1f).translationY(0f).setStartDelay(220L).setDuration(360L)
            .setInterpolator(DecelerateInterpolator()).start()

        imgDedo.animate()
            .alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(240L).setDuration(420L)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun iniciarAnimacionAmbientalCarmesi() {
        cancelarAnimacionesCarmesi()
        if (!ThemeManager.esCarmesi(this)) return

        ringRevealPrimary?.let { ring ->
            ring.scaleX = 0.98f; ring.scaleY = 0.98f
            val sX = ObjectAnimator.ofFloat(ring, View.SCALE_X, 0.98f, 1.05f).apply {
                duration = 4200L; repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
            }
            val sY = ObjectAnimator.ofFloat(ring, View.SCALE_Y, 0.98f, 1.05f).apply {
                duration = 4200L; repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
            }
            val alpha = ObjectAnimator.ofFloat(ring, View.ALPHA, 0.24f, 0.40f).apply {
                duration = 4200L; repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
            }
            carmesiAnimators += listOf(sX, sY, alpha)
        }

        ringRevealSecondary?.let { ring ->
            ring.scaleX = 0.94f; ring.scaleY = 0.94f
            val sX = ObjectAnimator.ofFloat(ring, View.SCALE_X, 0.94f, 1.02f).apply {
                duration = 5200L; repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
            }
            val sY = ObjectAnimator.ofFloat(ring, View.SCALE_Y, 0.94f, 1.02f).apply {
                duration = 5200L; repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
            }
            val alpha = ObjectAnimator.ofFloat(ring, View.ALPHA, 0.10f, 0.20f).apply {
                duration = 5200L; repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
            }
            carmesiAnimators += listOf(sX, sY, alpha)
        }

        imgLetraSReveal?.let { letra ->
            val alpha = ObjectAnimator.ofFloat(letra, View.ALPHA, 0.08f, 0.15f).apply {
                duration = 5600L; repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
            }
            val rotation = ObjectAnimator.ofFloat(letra, View.ROTATION, -2f, 2f).apply {
                duration = 6400L; repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
            }
            carmesiAnimators += listOf(alpha, rotation)
        }

        // Particulas flotantes
        particleViews.forEachIndexed { i, p ->
            val delay = (i * 600L)
            val dur   = 3000L + (i * 400L)
            val fadeIn = ObjectAnimator.ofFloat(p, View.ALPHA, 0f, 0.4f + (i % 3) * 0.1f).apply {
                duration = dur; startDelay = delay
                repeatCount = ValueAnimator.INFINITE; repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator(); start()
            }
            val floatY = ObjectAnimator.ofFloat(p, View.TRANSLATION_Y, 0f, -(20f + i * 8f)).apply {
                duration = dur; startDelay = delay
                repeatCount = ValueAnimator.INFINITE; repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator(); start()
            }
            val floatX = ObjectAnimator.ofFloat(p, View.TRANSLATION_X, 0f, if (i % 2 == 0) 6f else -6f).apply {
                duration = dur + 800L; startDelay = delay
                repeatCount = ValueAnimator.INFINITE; repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator(); start()
            }
            carmesiAnimators += listOf(fadeIn, floatY, floatX)
        }

        // Dedo flotante
        val floatDedo = ObjectAnimator.ofFloat(imgDedo, View.TRANSLATION_Y, 0f, -10f).apply {
            duration = 1800L; repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE; interpolator = AccelerateDecelerateInterpolator(); start()
        }
        carmesiAnimators += floatDedo
    }

    private fun cancelarAnimacionesCarmesi() {
        carmesiAnimators.forEach { it.cancel() }
        carmesiAnimators.clear()
    }

    private fun animarRevelacionCarmesi() {
        cardViewPrincipal.animate().cancel()
        cardViewPrincipal.animate()
            .scaleX(1.03f).scaleY(1.03f).setDuration(220L).setInterpolator(DecelerateInterpolator())
            .withEndAction {
                cardViewPrincipal.animate()
                    .scaleX(1f).scaleY(1f).setDuration(280L).setInterpolator(DecelerateInterpolator()).start()
            }.start()

        imgLetraSReveal?.animate()
            ?.alpha(0.18f)?.rotation(5f)?.setDuration(280L)
            ?.withEndAction {
                imgLetraSReveal?.animate()
                    ?.alpha(0.12f)?.rotation(0f)?.setDuration(420L)?.setInterpolator(DecelerateInterpolator())?.start()
            }?.start()
    }

    private fun animarCambioJugadorCarmesi() {
        listOf(turnPlayerName, txtTwo, presText).forEachIndexed { index, view ->
            view.alpha = 0f; view.translationY = 14f
            view.animate().alpha(1f).translationY(0f)
                .setStartDelay(index * 60L).setDuration(280L).setInterpolator(DecelerateInterpolator()).start()
        }

        imgDedo.alpha = 0f; imgDedo.scaleX = 0.88f; imgDedo.scaleY = 0.88f
        imgDedo.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setStartDelay(120L).setDuration(320L).setInterpolator(DecelerateInterpolator()).start()
    }

    // ── Ciclo de vida ───────────────────────────────────────────────────────
    override fun onResume() {
        super.onResume()
        if (ThemeManager.esCarmesi(this)) iniciarAnimacionAmbientalCarmesi()
    }

    override fun onPause() {
        cancelarAnimacionesCarmesi()
        super.onPause()
    }

    override fun onDestroy() {
        cancelarAnimacionesCarmesi()
        cancelarRevealMantenerPendiente()
        super.onDestroy()
    }
}
