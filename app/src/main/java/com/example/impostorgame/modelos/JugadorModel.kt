package com.example.impostorgame.modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class TipoJugador { NORMAL, IMPOSTOR, SENOR_BLANCO }

@Parcelize
data class Jugador(
    val nombre: String,
    val vecesImpostor: Int = 0,
    val tipo: TipoJugador = TipoJugador.NORMAL
) : Parcelable