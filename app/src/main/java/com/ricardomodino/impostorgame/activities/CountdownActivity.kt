package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.SoundManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador
import kotlin.math.PI
import kotlin.math.sin

class CountdownActivity : AppCompatActivity() {

    private lateinit var txtCountdown: TextView

    // Flag para detener los callbacks de animación cuando el usuario pulsa atrás.
    // Sin este flag, el withEndAction sigue ejecutándose aunque la Activity ya se esté cerrando,
    // lo que lanzaba ImpostorRevealActivity desde una Activity destruida (Activity fantasma).
    private var isCancelled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown_fullscreen)

        txtCountdown = findViewById(R.id.txtCountdown)

        // Pulsar atrás durante la cuenta cancela la partida y vuelve al menú principal.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelarCuentaAtras()
            }
        })

        val players    = intent.getParcelableArrayListExtra<Jugador>("PLAYERS")
        val categories = intent.getParcelableArrayListExtra<Category>("CATEGORIES")
        val opciones   = intent.getParcelableExtra<GameOptions>("OPCIONES")
        val esDatosCuriosos = opciones?.modoDatosCuriosos == true

        startCountdown(players, categories, opciones, esDatosCuriosos)
    }

    private fun cancelarCuentaAtras() {
        isCancelled = true
        txtCountdown.animate().cancel()
        finish()
    }

    private fun playCountdownTone(frequencyHz: Float, durationMs: Int = 140) {
        if (!SoundManager.isSoundEnabled(this)) return
        try {
            val sampleRate = 44100
            val numSamples = sampleRate * durationMs / 1000
            val fadeLen    = (sampleRate * 0.015).toInt() // 15 ms fade in/out
            val samples    = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val env = when {
                    i < fadeLen              -> i.toDouble() / fadeLen
                    i > numSamples - fadeLen -> (numSamples - i).toDouble() / fadeLen
                    else                     -> 1.0
                }
                samples[i] = (env * 0.7 * Short.MAX_VALUE *
                        sin(2.0 * PI * frequencyHz * i / sampleRate)).toInt().toShort()
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack(
                AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuf, numSamples * 2), AudioTrack.MODE_STATIC
            )
            track.write(samples, 0, numSamples)
            track.setNotificationMarkerPosition(numSamples)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) { t.release() }
                override fun onPeriodicNotification(t: AudioTrack) {}
            })
            track.play()
        } catch (_: Exception) {}
    }

    private fun startCountdown(
        players: ArrayList<Jugador>?,
        categories: ArrayList<Category>?,
        opciones: GameOptions?,
        esDatosCuriosos: Boolean = false
    ) {
        val toneFreqs = mapOf("3" to 392f, "2" to 494f, "1" to 659f, "¡Ya!" to 880f)
        val numbers   = listOf("3", "2", "1", "¡Ya!")
        var index     = 0

        fun showNext() {
            // Si el usuario pulsó atrás o la Activity ya está cerrándose, no continuar.
            if (isCancelled || isFinishing || isDestroyed) return

            if (index >= numbers.size) {
                val destino = if (esDatosCuriosos) DatosCuriososRevealActivity::class.java
                              else ImpostorRevealActivity::class.java
                val intent = Intent(this, destino).apply {
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

            val freq     = toneFreqs[text] ?: 440f
            val duration = if (text == "¡Ya!") 220 else 140
            playCountdownTone(freq, duration)

            txtCountdown.scaleX = 0f
            txtCountdown.scaleY = 0f
            txtCountdown.alpha  = 0f
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
