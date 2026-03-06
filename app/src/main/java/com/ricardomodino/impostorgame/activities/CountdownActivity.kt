package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador

class CountdownActivity : AppCompatActivity() {

    private lateinit var txtCountdown: TextView
    private var timer: CountDownTimer? = null
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown_fullscreen)

        txtCountdown = findViewById(R.id.txtCountdown)

        val players    = intent.getParcelableArrayListExtra<Jugador>("PLAYERS")
        val categories = intent.getParcelableArrayListExtra<Category>("CATEGORIES")
        val opciones   = intent.getParcelableExtra<GameOptions>("OPCIONES")

        startCountdown(players, categories, opciones)
    }

    private fun startCountdown(
        players: ArrayList<Jugador>?,
        categories: ArrayList<Category>?,
        opciones: GameOptions?
    ) {
        val numbers = listOf("3", "2", "1", "¡Ya!")
        var index = 0

        fun showNext() {
            if (index >= numbers.size) {
                // Ir a ImpostorRevealActivity
                val intent = Intent(this, ImpostorRevealActivity::class.java).apply {
                    putParcelableArrayListExtra("PLAYERS", players)
                    putParcelableArrayListExtra("CATEGORIES", categories)
                    putExtra("OPCIONES", opciones)
                }
                startActivity(intent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return
            }

            val text = numbers[index]
            txtCountdown.text = text

            // Sonido tick
            try { toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150) } catch (_: Exception) {}

            // Animación: scale up desde 0 + fade
            txtCountdown.scaleX = 0f
            txtCountdown.scaleY = 0f
            txtCountdown.alpha  = 0f
            txtCountdown.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(300L)
                .withEndAction {
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
        timer?.cancel()
        try { toneGen.release() } catch (_: Exception) {}
    }
}