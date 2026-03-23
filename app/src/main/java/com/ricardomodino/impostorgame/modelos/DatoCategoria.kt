package com.ricardomodino.impostorgame.modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DatoCurioso(
    val id: Long,
    val es: String,
    val en: String,
    val zhHans: String,
    val zhHant: String
) : Parcelable

@Parcelize
data class DatoCategoria(
    val id: Long,
    val es: String,
    val en: String,
    val zhHans: String,
    val zhHant: String,
    val emoji: String,
    var isSelected: Boolean = false,
    val datos: List<DatoCurioso>
) : Parcelable {
    fun nombre(idioma: String = "es"): String = when (idioma) {
        "en"      -> en
        "zh-Hans" -> zhHans
        "zh-Hant" -> zhHant
        else      -> es
    }
}
