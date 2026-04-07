package com.ricardomodino.impostorgame.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fact_categories")
data class FactCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Global: multilingüe
    val nameEs: String = "",
    val nameEn: String = "",
    val nameZhHans: String = "",
    val nameZhHant: String = "",
    // Local (usuario)
    val nameLocal: String = "",
    val emoji: String = "",
    val source: String,
    val isSelected: Boolean = false
)
