package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador
import com.ricardomodino.impostorgame.viewmodel.GuessWordViewModel

class GuessWordActivity : BaseGameActivity() {

    private val guessViewModel: GuessWordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            when {
                ThemeManager.esFinal(this) -> R.layout.activity_guess_word_final
                ThemeManager.esCarmesi(this) -> R.layout.activity_guess_word_carmesi
                else -> R.layout.activity_guess_word
            }
        )
        ImmersiveModeManager.applyActivityContentInsets(this, includeBottomInset = true)
        ThemeManager.aplicarDrawables(this)

        val nombreVotado = intent.getStringExtra(IntentKeys.NOMBRE_VOTADO) ?: ""
        val tipoVotado = intent.getStringExtra(IntentKeys.TIPO_VOTADO) ?: ""
        val palabra = intent.getStringExtra(IntentKeys.PALABRA) ?: ""
        val nombreImpostor = intent.getStringExtra(IntentKeys.IMPOSTOR) ?: ""
        val senoresBlancos = intent.getStringExtra(IntentKeys.SENORES_BLANCOS) ?: ""
        val jugadores = intent.getParcelableArrayListExtra<Jugador>(IntentKeys.JUGADORES) ?: arrayListOf()

        val txtSubtitle = findViewById<TextView>(R.id.txtGuessSubtitle)
        val editWord = findViewById<EditText>(R.id.editGuessWord)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmarPalabra)

        if (ThemeManager.esCarmesi(this)) {
            findViewById<TextView?>(R.id.txtGuessTitleHint)?.text = getString(R.string.staicy_edition)
        }

        // Genero y texto del subtitulo
        val rol = if (tipoVotado == IntentKeys.IMPOSTOR) {
            getString(R.string.guess_role_impostor)
        } else {
            getString(R.string.guess_role_mr_white)
        }
        val otrosMalos = jugadores.count {
            (it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO) &&
                it.nombre != nombreVotado
        }
        val salvar = if (otrosMalos > 0) {
            getString(R.string.guess_save_group)
        } else {
            getString(R.string.guess_save_yourself)
        }
        txtSubtitle.text = getString(R.string.guess_subtitle_reveal, nombreVotado, rol, salvar)

        // Helper local: construye el Intent a VictoryActivity con los datos comunes
        val irAVictoria = { ganador: String, motivo: String, lista: ArrayList<Jugador> ->
            Intent(this@GuessWordActivity, VictoryActivity::class.java).apply {
                putExtra(IntentKeys.GANADOR, ganador)
                putExtra(IntentKeys.MOTIVO, motivo)
                putExtra(IntentKeys.IR_A_REVEAL, true)
                putExtra(IntentKeys.PALABRA, palabra)
                putExtra(IntentKeys.IMPOSTOR, nombreImpostor)
                putExtra(IntentKeys.SENORES_BLANCOS, senoresBlancos)
                putParcelableArrayListExtra(IntentKeys.LISTA_JUGADORES, lista)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        // Procesa la respuesta confirmada: mismo bloque usado tanto en el click
        // como en la restauración tras rotación (si ya había una respuesta guardada).
        fun procesarRespuesta(respuesta: String) {
            if (respuesta.equals(palabra, ignoreCase = true)) {
                // Acierta: victoria impostores
                val motivoAcierto = getString(R.string.guess_correct_reason, nombreVotado)
                startActivity(irAVictoria("IMPOSTORES", motivoAcierto, jugadores))
            } else {
                val nuevaLista = ArrayList(jugadores.filter { it.nombre != nombreVotado })
                val noCiviles = nuevaLista.count {
                    it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO
                }
                val civiles = nuevaLista.count { it.tipo == TipoJugador.NORMAL }

                when {
                    noCiviles == 0 -> {
                        GameDialog(this)
                            .icon("\u274C")
                            .title(getString(R.string.guess_incorrect_title))
                            .message(getString(R.string.guess_message_no_threats, nombreVotado))
                            .cancelable(false)
                            .positiveButton(getString(R.string.guess_action_view_victory)) {
                                val motivo = getString(R.string.guess_last_threat_reason, nombreVotado)
                                startActivity(irAVictoria("CIVILES", motivo, nuevaLista))
                            }.show()
                    }

                    noCiviles >= civiles -> {
                        GameDialog(this)
                            .icon("\u274C")
                            .title(getString(R.string.guess_incorrect_title))
                            .message(getString(R.string.guess_message_impostors_dominate, nombreVotado))
                            .cancelable(false)
                            .positiveButton(getString(R.string.guess_action_view_victory)) {
                                startActivity(
                                    irAVictoria(
                                        "IMPOSTORES",
                                        getString(R.string.guess_reason_impostors_dominate),
                                        nuevaLista
                                    )
                                )
                            }.show()
                    }

                    else -> {
                        GameDialog(this)
                            .icon("\u274C")
                            .title(getString(R.string.guess_incorrect_title))
                            .message(getString(R.string.guess_message_word_wrong, nombreVotado))
                            .cancelable(false)
                            .positiveButton(getString(R.string.dialog_ok)) {
                                setResult(
                                    RESULT_OK,
                                    Intent().apply {
                                        putParcelableArrayListExtra(
                                            IntentKeys.JUGADORES_ACTUALIZADOS,
                                            nuevaLista
                                        )
                                    }
                                )
                                finish()
                            }.show()
                    }
                }
            }
        }

        btnConfirm.setOnClickListener {
            val respuesta = editWord.text.toString().trim()
            if (respuesta.isBlank()) return@setOnClickListener
            guessViewModel.respuesta = respuesta
            procesarRespuesta(respuesta)
        }

        // Restaurar tras rotación: si el usuario ya había confirmado una respuesta,
        // re-lanzar el procesamiento directamente para mostrar el diálogo correcto.
        guessViewModel.respuesta?.let { procesarRespuesta(it) }
    }
}
