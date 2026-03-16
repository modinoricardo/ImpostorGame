package com.ricardomodino.impostorgame.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager

/**
 * Pantalla de intro cinemática que sustituye la antigua pantalla negra del splash.
 *
 * Fases de animación:
 *  - Fase 1 (    0 ms): los 6 personajes caen desde arriba de forma escalonada.
 *  - Fase 2 (  700 ms): las flechas de acusación aparecen deslizándose hacia el impostor.
 *  - Fase 3 ( 1300 ms): el impostor escala hacia arriba y aparece "¡IMPOSTOR!".
 *  - Barra  (    0 ms): la barra de carga llena progresivamente durante toda la intro.
 *  - Fin    ( 2600 ms): transición con fade a MainActivity.
 *
 *  android:noHistory="true" en el manifest evita que al pulsar atrás desde
 *  MainActivity se vuelva a esta pantalla.
 */
class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var navigated = false

    // Índice del impostor dentro de la fila de personajes (0-based)
    private val impostorIndex = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        // Pantalla completa antes de inflar el layout para evitar parpadeos
        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val characters = listOf(
            findViewById<ImageView>(R.id.char0),
            findViewById(R.id.char1),
            findViewById(R.id.char2),
            findViewById(R.id.char3),  // impostor
            findViewById(R.id.char4),
            findViewById(R.id.char5)
        )

        // Lista de flechas; null en la posición del impostor (no lo acusa a sí mismo)
        val arrows: List<ImageView?> = listOf(
            findViewById(R.id.arrow0),
            findViewById(R.id.arrow1),
            findViewById(R.id.arrow2),
            null,
            findViewById(R.id.arrow4),
            findViewById(R.id.arrow5)
        )

        val tvImpostor  = findViewById<TextView>(R.id.tvImpostor)
        val loadingFill = findViewById<View>(R.id.loadingFill)
        val loadingIcon = findViewById<ImageView>(R.id.loadingIcon)

        // Fase 1 — personajes caen desde arriba de forma escalonada
        characters.forEachIndexed { i, view ->
            view.alpha = 0f
            view.translationY = -130f
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, "translationY", -130f, 0f),
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                )
                duration    = 450
                startDelay  = i * 75L
                interpolator = FastOutSlowInInterpolator()
                start()
            }
        }

        // Fase 2 — flechas de acusación deslizándose hacia el centro
        handler.postDelayed({
            var arrowDelay = 0L
            arrows.forEachIndexed { i, arrow ->
                arrow ?: return@forEachIndexed
                arrow.visibility = View.VISIBLE
                arrow.alpha = 0f
                // Las flechas izquierdas (i<3) arrancan 20px a su izquierda y se deslizan a 0.
                // Las flechas derechas (i>3) arrancan 20px a su derecha y se deslizan a 0.
                val startX = if (i < impostorIndex) -20f else 20f
                arrow.translationX = startX
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(arrow, "alpha", 0f, 1f),
                        ObjectAnimator.ofFloat(arrow, "translationX", startX, 0f)
                    )
                    duration     = 350
                    startDelay   = arrowDelay
                    interpolator = FastOutSlowInInterpolator()
                    start()
                }
                arrowDelay += 60L
            }
        }, 700)

        // Fase 3 — impostor revelado: escala + texto "¡IMPOSTOR!"
        handler.postDelayed({
            val impostor = characters[impostorIndex]

            // El impostor escala hacia arriba para destacar
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(impostor, "scaleX", 1f, 1.3f),
                    ObjectAnimator.ofFloat(impostor, "scaleY", 1f, 1.3f)
                )
                duration     = 280
                interpolator = FastOutSlowInInterpolator()
                start()
            }

            // Texto "¡IMPOSTOR!" aparece con escala + fade
            tvImpostor.visibility = View.VISIBLE
            tvImpostor.alpha  = 0f
            tvImpostor.scaleX = 0.3f
            tvImpostor.scaleY = 0.3f
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(tvImpostor, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(tvImpostor, "scaleX", 0.3f, 1f),
                    ObjectAnimator.ofFloat(tvImpostor, "scaleY", 0.3f, 1f)
                )
                duration     = 420
                interpolator = FastOutSlowInInterpolator()
                start()
            }
        }, 1300)

        // Barra de carga — se llena a lo largo de toda la intro
        // Se usa scaleX (0→1) con pivotX=0 para crecer desde la izquierda sin
        // necesidad de modificar layoutParams en cada frame (más eficiente).
        loadingFill.pivotX = 0f
        loadingFill.scaleX = 0f

        loadingFill.post {
            val fillWidth  = loadingFill.width.toFloat()
            val iconWidth  = loadingIcon.width.toFloat()

            ValueAnimator.ofFloat(0f, 1f).apply {
                duration     = TOTAL_DURATION - 350
                interpolator = LinearInterpolator()
                addUpdateListener { anim ->
                    val progress = anim.animatedValue as Float
                    loadingFill.scaleX        = progress
                    // El icono avanza con el borde derecho del relleno
                    loadingIcon.translationX  = (fillWidth - iconWidth) * progress
                }
                start()
            }

            // El icono también hace un pequeño rebote vertical (efecto "caminando")
            ObjectAnimator.ofFloat(loadingIcon, "translationY", 0f, -6f, 0f).apply {
                duration     = 380
                repeatCount  = ValueAnimator.INFINITE
                repeatMode   = ValueAnimator.RESTART
                interpolator = FastOutSlowInInterpolator()
                start()
            }
        }

        // Navegación — al finalizar la intro se pasa a MainActivity con fade
        handler.postDelayed({ navigateToMain() }, TOTAL_DURATION)
    }

    private fun navigateToMain() {
        if (navigated) return
        navigated = true
        startActivity(Intent(this, MainActivity::class.java))
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelar callbacks pendientes para evitar fugas si el usuario
        // cierra la app durante la intro
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        /** Duración total de la cinemática en milisegundos */
        private const val TOTAL_DURATION = 2600L
    }
}
