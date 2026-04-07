package com.ricardomodino.impostorgame.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "facts",
    foreignKeys = [ForeignKey(
        entity = FactCategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["factCategoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("factCategoryId")]
)
data class FactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val factCategoryId: Long,
    // Global: multilingüe
    val textEs: String = "",
    val textEn: String = "",
    val textZhHans: String = "",
    val textZhHant: String = "",
    // Local (usuario)
    val textLocal: String = "",
    val source: String
)
