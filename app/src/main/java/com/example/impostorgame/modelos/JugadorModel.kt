package com.example.impostorgame.modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Jugador(
    val nombre: String,
    val vecesImpostor: Int = 0
) : Parcelable
