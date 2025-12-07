package com.example.impostorgame

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameOptions(
    val pista: Boolean = false,
    val modoLoco: Boolean = false
) : Parcelable
