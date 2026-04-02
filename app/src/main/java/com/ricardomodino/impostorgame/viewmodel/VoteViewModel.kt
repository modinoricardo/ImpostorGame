package com.ricardomodino.impostorgame.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Preserva el estado de votación ante rotaciones de pantalla.
 * - [selectedIndex]: jugador seleccionado actualmente (-1 = ninguno).
 * - [votoEnProceso]: true desde que el usuario confirma el voto hasta
 *   que la Activity termina; evita re-lanzar el countdown y garantiza
 *   que procesarVoto() se ejecuta exactamente una vez por confirmación.
 */
class VoteViewModel : ViewModel() {
    var selectedIndex: Int    = -1
    var votoEnProceso: Boolean = false
}
