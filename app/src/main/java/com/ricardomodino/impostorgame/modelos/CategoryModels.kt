package com.ricardomodino.impostorgame.modelos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
data class WordItem(
    val name: String,
    val hints: List<String> = emptyList()
) : Parcelable

@Serializable
@Parcelize
data class Category(
    val id: Long,
    val title: String,
    val iconEmoji: String,
    var isSelected: Boolean = false,
    val items: List<WordItem>,
    val source: String = "global"   // "global" | "local"
) : Parcelable
