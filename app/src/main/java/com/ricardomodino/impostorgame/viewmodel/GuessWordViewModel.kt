package com.ricardomodino.impostorgame.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Preserva el estado del intento de adivinanza ante rotaciones de pantalla.
 * - [respuesta]: texto confirmado por el usuario (null = aún no confirmado).
 *   Si no es null al recrear la Activity, el procesamiento se re-lanza
 *   directamente con este valor para restaurar el diálogo sin que el usuario
 *   tenga que pulsar confirmar de nuevo.
 */
class GuessWordViewModel : ViewModel() {
    var respuesta: String? = null
}
