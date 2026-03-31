package com.ricardomodino.impostorgame.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.extensions.applyWordSafeText
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.TipoJugador

/**
 * Pantalla de reveal para los estilos con cubierta (Final y Datos Curiosos).
 * Hereda de BaseRevealActivity que gestiona toda la logica de juego.
 *
 * Mecanica: mantener pulsado la capa cubierta para ver el contenido; soltar para ocultarlo.
 * Al primer soltar se marca como revelado y aparece el boton siguiente.
 */
class CoverRevealActivity : BaseRevealActivity() {

    // ── Vistas propias ──────────────────────────────────────────────────────
    private lateinit var txtContador: TextView
    private lateinit var txtNombre: TextView
    private lateinit var lineaAcento: View
    private lateinit var cardReveal: FrameLayout
    private lateinit var capaCubierta: FrameLayout
    private lateinit var imgJugador: ImageView
    private lateinit var layoutHintTap: LinearLayout
    private lateinit var layoutContenido: LinearLayout
    private lateinit var layoutImpostor: LinearLayout
    private lateinit var layoutDato: LinearLayout
    private lateinit var txtDatoCurioso: TextView
    private lateinit var txtImpostorEmoji: TextView
    private lateinit var txtImpostorTitulo: TextView
    private lateinit var txtImpostorSubtitulo: TextView
    private lateinit var txtLabelContenido: TextView
    private lateinit var btnSiguienteJugador: CardView
    private lateinit var txtBtnSiguienteView: TextView

    // Estado de la cubierta
    private var cubiertaReveladaLocal = false
    private var revealMantenerActivo = false

    // ── BaseRevealActivity: layout segun tema ───────────────────────────────
    override fun provideLayoutRes(): Int =
        if (ThemeManager.esFinal(this)) R.layout.activity_datos_curiosos_reveal_final
        else R.layout.activity_datos_curiosos_reveal

    // ── Vistas de interaccion que usa la base ───────────────────────────────
    override val touchTarget: View get() = capaCubierta
    override val btnSiguiente: View get() = btnSiguienteJugador
    override val txtBtnSiguiente: TextView get() = txtBtnSiguienteView

    // ── Bindeo de vistas ────────────────────────────────────────────────────
    override fun onBindViews() {
        txtContador          = findViewById(R.id.txtContadorJugador)
        txtNombre            = findViewById(R.id.txtNombreJugador)
        lineaAcento          = findViewById(R.id.lineaAcento)
        cardReveal           = findViewById(R.id.cardReveal)
        capaCubierta         = findViewById(R.id.capaCubierta)
        imgJugador           = findViewById(R.id.imgJugador)
        layoutHintTap        = findViewById(R.id.layoutHintTap)
        layoutContenido      = findViewById(R.id.layoutContenido)
        layoutImpostor       = findViewById(R.id.layoutImpostor)
        layoutDato           = findViewById(R.id.layoutDato)
        txtDatoCurioso       = findViewById(R.id.txtDatoCurioso)
        txtImpostorEmoji     = findViewById(R.id.txtImpostorEmoji)
        txtImpostorTitulo    = findViewById(R.id.txtImpostorTitulo)
        txtImpostorSubtitulo = findViewById(R.id.txtImpostorSubtitulo)
        txtLabelContenido    = findViewById(R.id.txtLabelContenido)
        btnSiguienteJugador  = findViewById(R.id.btnSiguienteJugador)
        txtBtnSiguienteView  = findViewById(R.id.txtBtnSiguiente)

        val accent = ThemeManager.getAccentColor(this)
        lineaAcento.setBackgroundColor(accent)
        ThemeManager.aplicarDrawables(this)

        ImmersiveModeManager.applyRootInsets(
            findViewById(R.id.layoutDatosCuriosos),
            includeBottomInset = true,
            extraTop = 24,
            extraBottom = 24
        )
    }

    // ── Mostrar datos del jugador actual ────────────────────────────────────
    override fun onShowPlayer(index: Int) {
        cubiertaReveladaLocal = false
        cubiertaRevelada      = false

        val jugador = listaJugadores[index]
        val total   = listaJugadores.size

        txtContador.text = getString(R.string.reveal_player_progress, index + 1, total)
        txtNombre.text   = jugador.nombre.uppercase()

        val selfie = SelfieManager.getBitmap(jugador.nombre)
        if (selfie != null) imgJugador.setImageBitmap(selfie)
        else imagenPorJugador[index]?.let { imgJugador.setImageBitmap(it) }

        layoutImpostor.visibility = View.GONE
        layoutDato.visibility     = View.GONE
        restablecerEstadoCubierta()

        txtBtnSiguienteView.text = if (index == total - 1)
            getString(R.string.datos_empezar) else getString(R.string.datos_siguiente)

        // Animacion de entrada del nombre
        txtNombre.translationX = 60f
        txtNombre.alpha = 0f
        txtNombre.animate().translationX(0f).alpha(1f).setDuration(300L).start()

        prepararContenido()
    }

    // ── Revelar contenido al mantener pulsado ──────────────────────────────
    override fun onRevealContent(index: Int) {
        // Tomar selfie al revelar (primera vez)
        if (opciones.camaraActiva && imageCapture != null && index !in selfiesTomados) {
            selfiesTomados.add(index)
            tomarSelfie(index)
        }
        animarAperturaCubiertaMantener()
    }

    // ── Ocultar contenido al soltar ─────────────────────────────────────────
    override fun onHideContent() {
        animarCierreCubiertaMantener()
        if (!cubiertaReveladaLocal) {
            cubiertaReveladaLocal = true
            cubiertaRevelada      = true
            mostrarBotonSiguienteAnimado()
        }
    }

    // ── Configuracion del listener de toque (sobreescribe la base) ──────────
    @SuppressLint("ClickableViewAccessibility")
    override fun configurarInteraccion() {
        capaCubierta.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    capaCubierta.parent?.requestDisallowInterceptTouchEvent(true)
                    if (opciones.camaraActiva && imageCapture != null && playerInGame !in selfiesTomados) {
                        selfiesTomados.add(playerInGame)
                        tomarSelfie(playerInGame)
                    }
                    animarAperturaCubiertaMantener()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    animarCierreCubiertaMantener()
                    if (!cubiertaReveladaLocal) {
                        cubiertaReveladaLocal = true
                        cubiertaRevelada      = true
                        mostrarBotonSiguienteAnimado()
                    }
                    true
                }
                else -> false
            }
        }

        btnSiguienteJugador.setOnClickListener {
            if (!cubiertaReveladaLocal) return@setOnClickListener
            avanzarJugador()
        }
    }

    // ── Transicion entre jugadores ──────────────────────────────────────────
    // playerInGame ya fue incrementado por avanzarJugador() antes de llamar aqui
    override fun onPlayerTransition(onSwap: () -> Unit) {
        // Animacion simple del nombre y carga del nuevo jugador
        txtNombre.animate().alpha(0f).setDuration(120L).withEndAction {
            onShowPlayer(playerInGame)
        }.start()
    }

    // ── Preparar contenido del jugador actual ───────────────────────────────
    private fun prepararContenido() {
        val index         = playerInGame
        val esImpostor    = index in indicesImpostores
        val esSenorBlanco = listaJugadores[index].tipo == TipoJugador.SENOR_BLANCO
        configurarTextoPrincipal(esPalabra = false)

        when {
            esSenorBlanco -> {
                txtImpostorEmoji.text     = "\u26AA"
                txtImpostorTitulo.text    = getString(R.string.reveal_mr_white_title)
                txtImpostorTitulo.setTextColor(Color.WHITE)
                txtImpostorSubtitulo.text = getString(R.string.reveal_no_word)
                layoutImpostor.visibility = View.VISIBLE
                layoutDato.visibility     = View.GONE
            }
            esImpostor -> {
                when {
                    opciones.modoMisterioso -> {
                        // Impostor modo misterioso: ve su pista en la tarjeta de contenido
                        configurarTextoPrincipal(esPalabra = true)
                        aplicarTextoContenido(pistaMisteriosa, esPalabra = true)
                        txtLabelContenido.text = getString(R.string.reveal_your_hint)
                        layoutDato.visibility     = View.VISIBLE
                        layoutImpostor.visibility = View.GONE
                    }
                    else -> {
                        txtImpostorSubtitulo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                        txtImpostorSubtitulo.setSingleLine(false)
                        txtImpostorSubtitulo.maxLines = Int.MAX_VALUE
                        txtImpostorEmoji.text  = "\uD83D\uDD75\uFE0F"
                        txtImpostorTitulo.text = getString(R.string.datos_eres_impostor)
                        txtImpostorTitulo.setTextColor(Color.parseColor("#FF4444"))
                        txtImpostorSubtitulo.text = if (opciones.modoDatosCuriosos) {
                            getString(R.string.datos_inventa_dato)
                        } else {
                            textoAyudaImpostor().also {
                                txtImpostorSubtitulo.applyWordSafeText(
                                    rawText = it,
                                    preferSingleLine = true,
                                    maxTextSp = 15f,
                                    minTextSp = 12f,
                                    preferredSingleLineMinSp = 11f,
                                    preferredWrappedMaxLines = 3
                                )
                            }
                        }
                        layoutImpostor.visibility = View.VISIBLE
                        layoutDato.visibility     = View.GONE
                    }
                }
            }
            else -> {
                // Civil
                if (opciones.modoDatosCuriosos) {
                    aplicarTextoContenido(
                        datosAsignados[index]?.let { getTextoDato(it) } ?: "",
                        esPalabra = false
                    )
                    txtLabelContenido.text = getString(R.string.datos_label_dato)
                } else {
                    configurarTextoPrincipal(esPalabra = true)
                    aplicarTextoContenido(palabra, esPalabra = true)
                    txtLabelContenido.text = getString(R.string.reveal_your_word)
                }
                layoutDato.visibility     = View.VISIBLE
                layoutImpostor.visibility = View.GONE
            }
        }
    }

    // ── Configuracion del texto principal ───────────────────────────────────
    private fun configurarTextoPrincipal(esPalabra: Boolean) {
        txtDatoCurioso.includeFontPadding = false

        val layoutDatoParams = layoutDato.layoutParams as LinearLayout.LayoutParams
        val textoParams = txtDatoCurioso.layoutParams as LinearLayout.LayoutParams

        if (esPalabra) {
            layoutDatoParams.height = LinearLayout.LayoutParams.MATCH_PARENT
            layoutDato.layoutParams = layoutDatoParams
            layoutDato.gravity = Gravity.CENTER_HORIZONTAL

            textoParams.width  = LinearLayout.LayoutParams.MATCH_PARENT
            textoParams.height = 0
            textoParams.weight = 1f
            textoParams.bottomMargin = dp(24)
            txtDatoCurioso.layoutParams = textoParams

            txtDatoCurioso.maxLines = Int.MAX_VALUE
            txtDatoCurioso.gravity  = Gravity.CENTER
            txtDatoCurioso.setLineSpacing(0f, 0.95f)
            txtDatoCurioso.setTypeface(txtDatoCurioso.typeface, Typeface.BOLD)
            txtDatoCurioso.setTextSize(TypedValue.COMPLEX_UNIT_SP, 92f)
        } else {
            layoutDatoParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutDato.layoutParams = layoutDatoParams
            layoutDato.gravity = Gravity.CENTER

            textoParams.width  = LinearLayout.LayoutParams.MATCH_PARENT
            textoParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            textoParams.weight = 0f
            textoParams.bottomMargin = dp(20)
            txtDatoCurioso.layoutParams = textoParams

            txtDatoCurioso.maxLines = Int.MAX_VALUE
            txtDatoCurioso.gravity  = Gravity.CENTER
            txtDatoCurioso.setLineSpacing(0f, 1.5f)
            txtDatoCurioso.setTypeface(txtDatoCurioso.typeface, Typeface.NORMAL)
            txtDatoCurioso.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
        }
    }

    private fun aplicarTextoContenido(texto: String, esPalabra: Boolean) {
        txtDatoCurioso.applyWordSafeText(
            rawText = texto,
            preferSingleLine = esPalabra,
            maxTextSp = if (esPalabra) 92f else 19f,
            minTextSp = if (esPalabra) 16f else 14f,
            preferredSingleLineMinSp = if (esPalabra) 14f else 14f,
            preferredWrappedMaxLines = if (esPalabra) 3 else 6
        )
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()

    // ── Animaciones de cubierta ─────────────────────────────────────────────
    private fun restablecerEstadoCubierta() {
        revealMantenerActivo = false
        cardReveal.animate().cancel()
        capaCubierta.animate().cancel()
        layoutHintTap.animate().cancel()
        layoutContenido.animate().cancel()
        lineaAcento.animate().cancel()
        btnSiguienteJugador.animate().cancel()

        cardReveal.scaleX = 1f; cardReveal.scaleY = 1f; cardReveal.translationY = 0f

        capaCubierta.visibility    = View.VISIBLE
        capaCubierta.alpha         = 1f
        capaCubierta.translationY  = 0f
        capaCubierta.scaleX        = 1f; capaCubierta.scaleY = 1f

        layoutHintTap.visibility = View.VISIBLE
        layoutHintTap.alpha      = 1f; layoutHintTap.translationY = 0f
        layoutHintTap.scaleX     = 1f; layoutHintTap.scaleY = 1f

        layoutContenido.alpha      = 1f
        layoutContenido.translationY = 0f
        layoutContenido.scaleX     = 1f; layoutContenido.scaleY = 1f

        lineaAcento.alpha  = 1f; lineaAcento.scaleX = 1f

        btnSiguienteJugador.visibility  = View.GONE
        btnSiguienteJugador.alpha       = 1f; btnSiguienteJugador.translationY = 0f
        btnSiguienteJugador.scaleX      = 1f; btnSiguienteJugador.scaleY = 1f
    }

    private fun animarAperturaCubiertaMantener() {
        if (revealMantenerActivo) return
        revealMantenerActivo = true

        capaCubierta.animate().cancel()

        // Slide suave hacia arriba + fade — el contenido queda visible debajo
        val slideY = capaCubierta.height.takeIf { it > 0 }?.toFloat()
            ?: resources.displayMetrics.heightPixels * 0.6f

        capaCubierta.animate()
            .translationY(-slideY)
            .alpha(0f)
            .setDuration(500L)
            .setInterpolator(DecelerateInterpolator(1.8f))
            .start()
    }

    private fun animarCierreCubiertaMantener() {
        if (!revealMantenerActivo) return
        revealMantenerActivo = false

        capaCubierta.animate().cancel()

        capaCubierta.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400L)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .start()
    }

    private fun mostrarBotonSiguienteAnimado() {
        btnSiguienteJugador.visibility = View.VISIBLE
        btnSiguienteJugador.alpha = 0f
        btnSiguienteJugador.translationY = 6f
        btnSiguienteJugador.animate()
            .alpha(1f).translationY(0f)
            .setStartDelay(35L).setDuration(180L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}
