package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador

class GuessWordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess_word)
        ThemeManager.aplicarDrawables(this)

        val nombreVotado   = intent.getStringExtra("NOMBRE_VOTADO") ?: ""
        val tipoVotado     = intent.getStringExtra("TIPO_VOTADO") ?: ""
        val palabra        = intent.getStringExtra("PALABRA") ?: ""
        val nombreImpostor = intent.getStringExtra("IMPOSTOR") ?: ""
        val senoresBlancos = intent.getStringExtra("SENORES_BLANCOS") ?: ""
        val jugadores      = intent.getParcelableArrayListExtra<Jugador>("JUGADORES") ?: arrayListOf()

        val txtSubtitle = findViewById<TextView>(R.id.txtGuessSubtitle)
        val editWord    = findViewById<EditText>(R.id.editGuessWord)
        val btnConfirm  = findViewById<Button>(R.id.btnConfirmarPalabra)

        // ── Género y texto del subtítulo ──
        val femenino = esFemenino(nombreVotado)
        val rol = if (tipoVotado == "IMPOSTOR") {
            if (femenino) "la impostora" else "el impostor"
        } else {
            if (femenino) "la señora blanca" else "el señor blanco"
        }
        val otrosMalos = jugadores.count {
            (it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO) && it.nombre != nombreVotado
        }
        val salvar = if (otrosMalos > 0) "salvar al grupo" else "salvarte"
        txtSubtitle.text = "$nombreVotado es $rol.\nAdivina la palabra para $salvar."

        btnConfirm.setOnClickListener {
            val respuesta = editWord.text.toString().trim()
            if (respuesta.isBlank()) return@setOnClickListener

            if (respuesta.equals(palabra, ignoreCase = true)) {
                // Acierta — victoria impostores
                startActivity(Intent(this, VictoryActivity::class.java).apply {
                    putExtra("GANADOR", "IMPOSTORES")
                    putExtra("MOTIVO", "¡$nombreVotado adivinó la palabra!")
                    putExtra("IR_A_REVEAL", true)
                    putExtra("PALABRA", palabra)
                    putExtra("IMPOSTOR", nombreImpostor)
                    putExtra("SENORES_BLANCOS", senoresBlancos)
                    putParcelableArrayListExtra("LISTA_JUGADORES", jugadores)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } else {
                val nuevaLista = ArrayList(jugadores.filter { it.nombre != nombreVotado })
                val noCiviles  = nuevaLista.count { it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO }
                val civiles    = nuevaLista.count { it.tipo == TipoJugador.NORMAL }
                val eliminado  = if (femenino) "eliminada" else "eliminado"

                when {
                    noCiviles == 0 -> {
                        val motivo = if (femenino)
                            "¡$nombreVotado era la última impostora!"
                        else
                            "¡$nombreVotado era el último impostor!"
                        GameDialog(this)
                            .icon("❌")
                            .title("Palabra incorrecta")
                            .message("$nombreVotado ha sido $eliminado.\n¡No quedan impostores!")
                            .cancelable(false)
                            .positiveButton("Ver victoria") {
                                startActivity(Intent(this, VictoryActivity::class.java).apply {
                                    putExtra("GANADOR", "CIVILES")
                                    putExtra("MOTIVO", motivo)
                                    putExtra("IR_A_REVEAL", true)
                                    putExtra("PALABRA", palabra)
                                    putExtra("IMPOSTOR", nombreImpostor)
                                    putExtra("SENORES_BLANCOS", senoresBlancos)
                                    putParcelableArrayListExtra("LISTA_JUGADORES", nuevaLista)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                            }.show()
                    }
                    noCiviles >= civiles -> {
                        GameDialog(this)
                            .icon("❌")
                            .title("Palabra incorrecta")
                            .message("$nombreVotado ha sido $eliminado.\n¡Los impostores dominan!")
                            .cancelable(false)
                            .positiveButton("Ver victoria") {
                                startActivity(Intent(this, VictoryActivity::class.java).apply {
                                    putExtra("GANADOR", "IMPOSTORES")
                                    putExtra("MOTIVO", "Los impostores superaron a los civiles.")
                                    putExtra("IR_A_REVEAL", true)
                                    putExtra("PALABRA", palabra)
                                    putExtra("IMPOSTOR", nombreImpostor)
                                    putExtra("SENORES_BLANCOS", senoresBlancos)
                                    putParcelableArrayListExtra("LISTA_JUGADORES", nuevaLista)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                            }.show()
                    }
                    else -> {
                        GameDialog(this)
                            .icon("❌")
                            .title("Palabra incorrecta")
                            .message("La palabra no era esa.\n$nombreVotado ha sido $eliminado.")
                            .cancelable(false)
                            .positiveButton("OK") {
                                setResult(RESULT_OK, Intent().apply {
                                    putParcelableArrayListExtra("JUGADORES_ACTUALIZADOS", nuevaLista)
                                })
                                finish()
                            }.show()
                    }
                }
            }
        }
    }

    private fun esFemenino(nombre: String): Boolean = nombre.trim().lowercase().endsWith("a")
}
