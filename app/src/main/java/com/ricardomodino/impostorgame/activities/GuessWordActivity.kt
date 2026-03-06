package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador

class GuessWordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess_word)
        ThemeManager.aplicarDrawables(this)

        val nombreVotado      = intent.getStringExtra("NOMBRE_VOTADO") ?: ""
        val tipoVotado        = intent.getStringExtra("TIPO_VOTADO") ?: ""
        val palabra           = intent.getStringExtra("PALABRA") ?: ""
        val nombreImpostor    = intent.getStringExtra("IMPOSTOR") ?: ""
        val senoresBlancos    = intent.getStringExtra("SENORES_BLANCOS") ?: ""
        val jugadores         = intent.getParcelableArrayListExtra<Jugador>("JUGADORES") ?: arrayListOf()

        val txtSubtitle = findViewById<TextView>(R.id.txtGuessSubtitle)
        val editWord    = findViewById<EditText>(R.id.editGuessWord)
        val btnConfirm  = findViewById<Button>(R.id.btnConfirmarPalabra)

        val rol = if (tipoVotado == "IMPOSTOR") "el impostor" else "el señor blanco"
        txtSubtitle.text = "$nombreVotado es $rol.\nAdivinad la palabra para salvar al equipo."

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
                val noCiviles = nuevaLista.count {
                    it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO
                }
                val civiles = nuevaLista.count { it.tipo == TipoJugador.NORMAL }

                when {
                    noCiviles == 0 -> {
                        // No quedan impostores — civiles ganan
                        AlertDialog.Builder(this)
                            .setTitle("❌ Palabra incorrecta")
                            .setMessage("$nombreVotado ha sido eliminado.\n¡No quedan impostores!")
                            .setCancelable(false)
                            .setPositiveButton("Ver victoria") { _, _ ->
                                startActivity(Intent(this, VictoryActivity::class.java).apply {
                                    putExtra("GANADOR", "CIVILES")
                                    putExtra("MOTIVO", "¡$nombreVotado era el último impostor!")
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
                        // Impostores igualan o superan civiles — impostores ganan
                        AlertDialog.Builder(this)
                            .setTitle("❌ Palabra incorrecta")
                            .setMessage("$nombreVotado ha sido eliminado.\n¡Los impostores dominan!")
                            .setCancelable(false)
                            .setPositiveButton("Ver victoria") { _, _ ->
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
                        // La partida continúa
                        AlertDialog.Builder(this)
                            .setTitle("❌ Palabra incorrecta")
                            .setMessage("La palabra no era esa.\n$nombreVotado ha sido eliminado.")
                            .setCancelable(false)
                            .setPositiveButton("OK") { _, _ ->
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
}