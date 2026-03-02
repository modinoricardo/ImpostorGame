package com.example.impostorgame.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.impostorgame.R
import com.example.impostorgame.managers.ThemeManager
import com.example.impostorgame.modelos.Jugador
import com.example.impostorgame.modelos.TipoJugador

class GuessWordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess_word)
        ThemeManager.aplicarDrawables(this)

        val nombreVotado = intent.getStringExtra("NOMBRE_VOTADO") ?: ""
        val tipoVotado   = intent.getStringExtra("TIPO_VOTADO") ?: ""
        val palabra      = intent.getStringExtra("PALABRA") ?: ""
        val jugadores    = intent.getParcelableArrayListExtra<Jugador>("JUGADORES") ?: arrayListOf()

        val txtSubtitle = findViewById<TextView>(R.id.txtGuessSubtitle)
        val editWord    = findViewById<EditText>(R.id.editGuessWord)
        val btnConfirm  = findViewById<Button>(R.id.btnConfirmarPalabra)

        val rol = if (tipoVotado == "IMPOSTOR") "el impostor" else "el señor blanco"
        txtSubtitle.text = "$nombreVotado es $rol.\nAdivinad la palabra para salvar al equipo."

        btnConfirm.setOnClickListener {
            val respuesta = editWord.text.toString().trim()
            if (respuesta.isBlank()) return@setOnClickListener

            if (respuesta.equals(palabra, ignoreCase = true)) {
                // Acierta — victoria impostores, ir a VictoryActivity sin cerrar todo
                val intent = Intent(this, VictoryActivity::class.java).apply {
                    putExtra("IR_A_REVEAL", true)
                    putExtra("GANADOR", "IMPOSTORES")
                    putExtra("MOTIVO", "¡$nombreVotado adivinó la palabra \"$palabra\"!\nLos no civiles ganan.")
                }
                // Flags para limpiar la pila pero sin salir de la app
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                // Falla — jugador eliminado, comprobar si quedan impostores
                val nuevaLista = ArrayList(jugadores.filter { it.nombre != nombreVotado })
                val quedanNoCiviles = nuevaLista.any {
                    it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO
                }

                if (!quedanNoCiviles) {
                    // No quedan impostores — civiles ganan
                    AlertDialog.Builder(this)
                        .setTitle("❌ Palabra incorrecta")
                        .setMessage("La palabra no era esa.\n$nombreVotado ha sido eliminado.")
                        .setCancelable(false)
                        .setPositiveButton("Ver victoria") { _, _ ->
                            val victoryIntent = Intent(this, VictoryActivity::class.java).apply {
                                putExtra("GANADOR", "CIVILES")
                                putExtra("MOTIVO", "¡$nombreVotado era el último impostor!")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(victoryIntent)
                        }
                        .show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("🎉 ¡Palabra correcta!")
                        .setMessage("¡$nombreVotado ha salvado al equipo!")
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->
                            val nuevaLista = ArrayList(jugadores.filter { it.nombre != nombreVotado })

                            val noCiviles = nuevaLista.count {
                                it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO
                            }
                            val civiles = nuevaLista.count { it.tipo == TipoJugador.NORMAL }

                            if (noCiviles >= civiles && noCiviles > 0) {
                                startActivity(Intent(this, VictoryActivity::class.java).apply {
                                    putExtra("GANADOR", "IMPOSTORES")
                                    putExtra("MOTIVO", "Los no civiles superaron a los civiles.")
                                    putExtra("IR_A_REVEAL", true)
                                    putParcelableArrayListExtra("JUGADORES", nuevaLista)
                                    putExtra("PALABRA", palabra)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                            } else {
                                val result = Intent().apply {
                                    putParcelableArrayListExtra("JUGADORES_ACTUALIZADOS", nuevaLista)
                                }
                                setResult(RESULT_OK, result)
                                finish()
                            }
                        }
                        .show()
                }
            }
        }
    }
}