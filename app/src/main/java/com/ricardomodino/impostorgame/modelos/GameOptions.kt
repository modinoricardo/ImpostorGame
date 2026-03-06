package com.ricardomodino.impostorgame.modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameOptions(
    val pista: Boolean = false,
    val modoLoco: Boolean = false,
    val modoMisterioso: Boolean = false,
    val numImpostores: Int = 1,
    val numSenoresBlancos: Int = 0,
    val tiempoLimitado: Boolean = false,
    val minutos: Int = 3,
    val camaraActiva: Boolean = false
) : Parcelable