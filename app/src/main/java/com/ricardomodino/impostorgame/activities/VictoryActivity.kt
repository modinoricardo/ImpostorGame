package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.*
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.views.VictoryParticleView

class VictoryActivity : BaseGameActivity() {

    private lateinit var txtSubtitle: TextView

    companion object {
        private const val KEY_SUBTITLE = "victory_subtitle"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::txtSubtitle.isInitialized) outState.putString(KEY_SUBTITLE, txtSubtitle.text.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            when {
                ThemeManager.esFinal(this) -> R.layout.activity_victory_final
                ThemeManager.esCarmesi(this) -> R.layout.activity_victory_carmesi
                else -> R.layout.activity_victory
            }
        )
        ImmersiveModeManager.applyActivityContentInsets(this, includeBottomInset = true)

        SelfieManager.clear()

        val ganador   = intent.getStringExtra(IntentKeys.GANADOR) ?: "IMPOSTORES"
        val motivo    = intent.getStringExtra(IntentKeys.MOTIVO) ?: ""
        val irAReveal = intent.getBooleanExtra(IntentKeys.IR_A_REVEAL, false)

        val particleView = findViewById<VictoryParticleView>(R.id.particleView)
        val txtTrophy    = findViewById<TextView>(R.id.txtTrophy)
        val txtTitle     = findViewById<TextView>(R.id.txtVictoryTitle)
        txtSubtitle      = findViewById(R.id.txtVictorySubtitle)
        val txtMotivo    = findViewById<TextView>(R.id.txtVictoryMotivo)
        val btnNewGame   = findViewById<Button>(R.id.btnVictoryNewGame)
        val mensajesCiviles = listOf(
            getString(R.string.victory_civilians_subtitle_1),
            getString(R.string.victory_civilians_subtitle_2),
            getString(R.string.victory_civilians_subtitle_3),
            getString(R.string.victory_civilians_subtitle_4),
            getString(R.string.victory_civilians_subtitle_5)
        )

        particleView.setGanador(ganador)

        if (ganador == "IMPOSTORES") {
            txtTrophy.text   = "\uD83D\uDE08"
            txtTitle.text    = getString(R.string.victory_impostors_title)
            txtSubtitle.text = getString(R.string.victory_impostors_subtitle)
            val sombra = if (ThemeManager.esCarmesi(this)) 0x66D92C58.toInt() else 0xFFFF1744.toInt()
            txtTitle.setShadowLayer(30f, 0f, 0f, sombra)
        } else {
            txtTrophy.text = "\uD83C\uDF89"
            txtTitle.text = getString(R.string.victory_civilians_title)
            txtSubtitle.text = savedInstanceState?.getString(KEY_SUBTITLE) ?: mensajesCiviles.random()
            val sombra = if (ThemeManager.esCarmesi(this)) 0x66F0D0D7.toInt() else 0xFF00E5FF.toInt()
            txtTitle.setShadowLayer(30f, 0f, 0f, sombra)
        }

        txtMotivo.text = motivo

        // Si hay que ir al reveal, cambiar texto del botón
        btnNewGame.text = getString(R.string.victory_reveal_button)

        // Animación entrada
        listOf(txtTrophy, txtTitle, txtSubtitle, txtMotivo).forEachIndexed { i, v ->
            v.alpha = 0f; v.translationY = 80f
            v.animate().alpha(1f).translationY(0f)
                .setStartDelay(i * 150L).setDuration(500L)
                .setInterpolator(OvershootInterpolator(1.5f)).start()
        }

        // Rebote emoji
        txtTrophy.postDelayed({
            fun animar() {
                if (ganador == "IMPOSTORES") {
                    txtTrophy.animate()
                        .rotationY(360f).setDuration(800L)
                        .withEndAction {
                            txtTrophy.rotationY = 0f
                            txtTrophy.postDelayed({ animar() }, 1500L)
                        }.start()
                } else {
                    txtTrophy.animate()
                        .translationY(-20f).setDuration(400L)
                        .withEndAction {
                            txtTrophy.animate()
                                .translationY(0f).setDuration(400L)
                                .setInterpolator(BounceInterpolator())
                                .withEndAction { txtTrophy.postDelayed({ animar() }, 800L) }
                                .start()
                        }.start()
                }
            }
            animar()
        }, 700L)

        // Sonido
        try {
            val tone = if (ganador == "IMPOSTORES") ToneGenerator.TONE_PROP_NACK
            else ToneGenerator.TONE_PROP_ACK
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(tone, 800)
            txtTrophy.postDelayed({ try { toneGen.release() } catch (_: Exception) {} }, 1000L)
        } catch (_: Exception) {}

        btnNewGame.setOnClickListener {
                val jugadores = intent.getParcelableArrayListExtra<Jugador>(IntentKeys.LISTA_JUGADORES)
                val nextIntent = Intent(this, PlayGameActivity::class.java).apply {
                    putParcelableArrayListExtra(IntentKeys.LISTA_JUGADORES, jugadores)
                    putParcelableArrayListExtra(IntentKeys.DATOS_PARTIDA, intent.getParcelableArrayListExtra<DatoCurioso>(IntentKeys.DATOS_PARTIDA))
                    putExtra(IntentKeys.PALABRA, intent.getStringExtra(IntentKeys.PALABRA))
                    putExtra(IntentKeys.IMPOSTOR, intent.getStringExtra(IntentKeys.IMPOSTOR))
                    putExtra(IntentKeys.SENORES_BLANCOS, intent.getStringExtra(IntentKeys.SENORES_BLANCOS))
                    putExtra(IntentKeys.MODO_MISTERIOSO, intent.getBooleanExtra(IntentKeys.MODO_MISTERIOSO, false))
                    putExtra(IntentKeys.MODO_DATOS_CURIOSOS, intent.getBooleanExtra(IntentKeys.MODO_DATOS_CURIOSOS, false))
                    putExtra(IntentKeys.TIEMPO_LIMITADO, false)
                    putExtra(IntentKeys.VICTORIA_INMEDIATA, true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(nextIntent)
        }
    }
}
