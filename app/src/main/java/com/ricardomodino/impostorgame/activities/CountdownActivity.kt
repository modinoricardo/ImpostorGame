package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import com.ricardomodino.impostorgame.managers.SoundManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador

class CountdownActivity : BaseGameActivity() {

    private lateinit var txtCountdown: TextView

    // Flag para detener los callbacks de animación cuando el usuario pulsa atrás.
    // Sin este flag, el withEndAction sigue ejecutándose aunque la Activity ya se esté cerrando,
    // lo que lanzaba ClassicRevealActivity desde una Activity destruida (Activity fantasma).
    private var isCancelled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ThemeManager.esCarmesi(this)) {
            enableEdgeToEdge()
        }
        setContentView(
            when {
                ThemeManager.esFinal(this) -> R.layout.activity_countdown_fullscreen_final
                ThemeManager.esCarmesi(this) -> R.layout.activity_countdown_fullscreen_carmesi
                else -> R.layout.activity_countdown_fullscreen
            }
        )

        txtCountdown = findViewById(R.id.txtCountdown)

        if (ThemeManager.esCarmesi(this)) {
            val countdownRoot = findViewById<android.view.View>(R.id.countdownRoot)
            val density = resources.displayMetrics.density
            val extraTop = (18 * density).toInt()
            val extraBottom = (22 * density).toInt()
            ImmersiveModeManager.applyRootInsets(
                countdownRoot,
                includeBottomInset = true,
                extraTop = extraTop,
                extraBottom = extraBottom
            )
        }

        // Pulsar atrás durante la cuenta cancela la partida y vuelve al menú principal.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelarCuentaAtras()
            }
        })

        val players    = intent.getParcelableArrayListExtra<Jugador>(IntentKeys.PLAYERS)
        val categories = intent.getParcelableArrayListExtra<Category>(IntentKeys.CATEGORIES)
        val opciones   = intent.getParcelableExtra<GameOptions>(IntentKeys.OPCIONES)
        val usarEstiloFinal = ThemeManager.esFinal(this)

        startCountdown(players, categories, opciones, usarEstiloFinal)
    }

    private fun cancelarCuentaAtras() {
        isCancelled = true
        txtCountdown.animate().cancel()
        finish()
    }

    private fun playCountdownTone(frequencyHz: Float, durationMs: Int = 140) =
        SoundManager.playCountdownTone(this, frequencyHz, durationMs)

    private fun startCountdown(
        players: ArrayList<Jugador>?,
        categories: ArrayList<Category>?,
        opciones: GameOptions?,
        esDatosCuriosos: Boolean = false
    ) {
        val txtCountdownAccent = findViewById<TextView?>(R.id.txtCountdownAccent)
        val esCarmesi = ThemeManager.esCarmesi(this)
        val finalCue = if (esCarmesi) "\u2665" else "¡Ya!"
        val toneFreqs = mapOf("3" to 392f, "2" to 494f, "1" to 659f, finalCue to 880f)
        val numbers = listOf("3", "2", "1", finalCue)
        var index = 0

        if (esCarmesi) {
            txtCountdownAccent?.alpha = 0.34f
        }

        fun showNext() {
            if (isCancelled || isFinishing || isDestroyed) return

            if (index >= numbers.size) {
                val destino = if (esDatosCuriosos) CoverRevealActivity::class.java
                else ClassicRevealActivity::class.java
                val intent = Intent(this, destino).apply {
                    putParcelableArrayListExtra(IntentKeys.PLAYERS, players)
                    putParcelableArrayListExtra(IntentKeys.CATEGORIES, categories)
                    putExtra(IntentKeys.OPCIONES, opciones)
                }
                startActivity(intent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return
            }

            val text = numbers[index]
            txtCountdown.text = text

            if (esCarmesi && text == finalCue) {
                txtCountdownAccent?.animate()
                    ?.scaleX(1.12f)
                    ?.scaleY(1.12f)
                    ?.alpha(1f)
                    ?.setDuration(280L)
                    ?.start()
            }

            val freq = toneFreqs[text] ?: 440f
            val duration = if (text == finalCue) 220 else 140
            playCountdownTone(freq, duration)

            txtCountdown.scaleX = 0f
            txtCountdown.scaleY = 0f
            txtCountdown.alpha = 0f
            txtCountdown.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(300L)
                .withEndAction {
                    if (isCancelled) return@withEndAction
                    txtCountdown.animate()
                        .scaleX(1.3f).scaleY(1.3f).alpha(0f)
                        .setDuration(500L)
                        .withEndAction {
                            index++
                            showNext()
                        }.start()
                }.start()
        }

        showNext()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Marcamos cancelado y cancelamos cualquier animación pendiente para evitar
        // que los withEndAction continúen ejecutándose tras destruir la Activity.
        isCancelled = true
        txtCountdown.animate().cancel()
    }
}
