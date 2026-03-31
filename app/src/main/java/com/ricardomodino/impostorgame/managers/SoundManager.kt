package com.ricardomodino.impostorgame.managers

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

object SoundManager {
    private const val PREFS = "impostor_prefs"
    private const val KEY   = "sonido_activo"

    fun isSoundEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true)

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY, enabled).apply()
    }

    /**
     * Sintetiza y reproduce un tono PCM de forma asíncrona.
     * Usado para la cuenta atrás (3-2-1) en [CountdownActivity] y [VoteActivity].
     *
     * @param context     Contexto para comprobar si el sonido está activo.
     * @param frequencyHz Frecuencia del tono en Hz (392 = Sol4, 494 = Si4, 659 = Mi5, 880 = La5).
     * @param durationMs  Duración del tono en milisegundos (por defecto 140 ms).
     */
    fun playCountdownTone(context: Context, frequencyHz: Float, durationMs: Int = 140) {
        if (!isSoundEnabled(context)) return
        playTonePcm(frequencyHz, durationMs, volume = 0.7, fadeRatio = 0.015)
    }

    /**
     * Sintetiza y reproduce el tono de revelación (528 Hz, 180 ms).
     * Usado en [CoverRevealActivity].
     *
     * @param context Contexto para comprobar si el sonido está activo.
     */
    fun playRevealTone(context: Context) {
        if (!isSoundEnabled(context)) return
        playTonePcm(frequencyHz = 528f, durationMs = 180, volume = 0.6, fadeRatio = 0.02)
    }

    // ── Núcleo de síntesis PCM ──────────────────────────────────────────────
    private fun playTonePcm(
        frequencyHz: Float,
        durationMs: Int,
        volume: Double,
        fadeRatio: Double
    ) {
        try {
            val sampleRate = 44100
            val numSamples = sampleRate * durationMs / 1000
            val fadeLen    = (sampleRate * fadeRatio).toInt()
            val samples    = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val env = when {
                    i < fadeLen              -> i.toDouble() / fadeLen
                    i > numSamples - fadeLen -> (numSamples - i).toDouble() / fadeLen
                    else                     -> 1.0
                }
                samples[i] = (env * volume * Short.MAX_VALUE *
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
}