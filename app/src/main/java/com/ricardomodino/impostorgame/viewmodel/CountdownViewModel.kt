package com.ricardomodino.impostorgame.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Preserva el progreso de la cuenta atrás ante rotaciones de pantalla.
 * - [paso]: índice actual en la secuencia ["3","2","1","¡Ya!"] (0–3).
 * - [navegado]: true si ya se lanzó la Activity de revelación; evita
 *   doble navegación si la recreación ocurre justo en ese instante.
 */
class CountdownViewModel : ViewModel() {
    var paso: Int      = 0
    var navegado: Boolean = false
}
