package com.example.impostorgame.activities

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.impostorgame.R
import com.example.impostorgame.ThemeManager
import com.example.impostorgame.modelos.Jugador
import com.example.impostorgame.views.VictoryParticleView

class VictoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)

        val ganador   = intent.getStringExtra("GANADOR") ?: "IMPOSTORES"
        val motivo    = intent.getStringExtra("MOTIVO") ?: ""
        val irAReveal = intent.getBooleanExtra("IR_A_REVEAL", false)

        val particleView = findViewById<VictoryParticleView>(R.id.particleView)
        val txtTrophy    = findViewById<TextView>(R.id.txtTrophy)
        val txtTitle     = findViewById<TextView>(R.id.txtVictoryTitle)
        val txtSubtitle  = findViewById<TextView>(R.id.txtVictorySubtitle)
        val txtMotivo    = findViewById<TextView>(R.id.txtVictoryMotivo)
        val btnNewGame   = findViewById<Button>(R.id.btnVictoryNewGame)

        particleView.setGanador(ganador)

        if (ganador == "IMPOSTORES") {
            txtTrophy.text   = "😈"
            txtTitle.text    = "¡LOS IMPOSTORES GANAN!"
            // Subtítulo dinámico según el motivo
            txtSubtitle.text = if (motivo.contains("adivinó"))
                "¡Descubiertos pero no derrotados!"
            else
                "Los civiles nunca tuvieron una oportunidad"
            txtTitle.setShadowLayer(30f, 0f, 0f, 0xFFFF1744.toInt())
        } else {
            txtTrophy.text = "🎉"
            // Título según número de impostores
            val hayVarios = motivo.contains("impostores")
            txtTitle.text = if (hayVarios) "¡LOS IMPOSTORES HAN SIDO DESCUBIERTOS!"
            else "¡EL IMPOSTOR HA SIDO DESCUBIERTO!"
            // Mensajes aleatorios para civiles
            val mensajesCiviles = listOf(
                "La verdad siempre sale a la luz",
                "El engaño tiene los días contados",
                "Juntos son imparables",
                "Nadie puede esconderse para siempre",
                "La justicia ha triunfado"
            )
            txtSubtitle.text = mensajesCiviles.random()
            txtTitle.setShadowLayer(30f, 0f, 0f, 0xFF00E5FF.toInt())
        }

        txtMotivo.text = motivo

        // Si hay que ir al reveal, cambiar texto del botón
        btnNewGame.text = if (irAReveal) "🔍 DESVELAR" else "NUEVA PARTIDA"

        // Animación entrada
        listOf(txtTrophy, txtTitle, txtSubtitle, txtMotivo).forEachIndexed { i, v ->
            v.alpha = 0f; v.translationY = 80f
            v.animate().alpha(1f).translationY(0f)
                .setStartDelay(i * 150L).setDuration(500L)
                .setInterpolator(OvershootInterpolator(1.5f)).start()
        }

        // Rebote emoji
// Animación continua del emoji
        txtTrophy.postDelayed({
            fun animar() {
                if (ganador == "IMPOSTORES") {
                    // Demonio: rotación de guiño continua
                    txtTrophy.animate()
                        .rotationY(360f).setDuration(800L)
                        .withEndAction {
                            txtTrophy.rotationY = 0f
                            txtTrophy.postDelayed({ animar() }, 1500L)
                        }.start()
                } else {
                    // Trofeo: sube y baja continuamente
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
            if (irAReveal) {
                val jugadores = intent.getParcelableArrayListExtra<Jugador>("LISTA_JUGADORES")
                val nextIntent = Intent(this, PlayGameActivity::class.java).apply {
                    putParcelableArrayListExtra("LISTA_JUGADORES", jugadores)
                    putExtra("PALABRA", intent.getStringExtra("PALABRA"))
                    putExtra("IMPOSTOR", intent.getStringExtra("IMPOSTOR"))
                    putExtra("SENORES_BLANCOS", intent.getStringExtra("SENORES_BLANCOS"))
                    putExtra("MODO_MISTERIOSO", intent.getBooleanExtra("MODO_MISTERIOSO", false))
                    putExtra("TIEMPO_LIMITADO", false)
                    putExtra("VICTORIA_INMEDIATA", true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(nextIntent)
            } else {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }
}