package com.example.impostorgame

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
data class WordItem(
    val name: String,    // palabra que ven los civiles
    val hint: String     // pista para el impostor
) : Parcelable

@Serializable
@Parcelize
data class Category(
    val id: Long,                    // identificador único
    val title: String,               // "Animales", "Famosos", etc.
    val iconEmoji: String,           // "🏠", "🦁", "🍕" ...
    var isSelected: Boolean = false, // solo para la UI (seleccionada en la lista)
    val items: List<WordItem>        // lista de (nombre, pista)
) : Parcelable


