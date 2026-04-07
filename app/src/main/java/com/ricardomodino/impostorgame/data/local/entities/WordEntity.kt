package com.ricardomodino.impostorgame.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("categoryId")]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val position: Int = 0,
    // Global: campos multilingüe
    val nameEs: String = "",
    val nameEn: String = "",
    val nameZhHans: String = "",
    val nameZhHant: String = "",
    // Pistas separadas por "|" — ej: "Mascota|Ladra|Paseo"
    val hintsEs: String = "",
    val hintsEn: String = "",
    val hintsZhHans: String = "",
    val hintsZhHant: String = "",
    // Local (usuario): un solo idioma
    val nameLocal: String = "",
    val hintsLocal: String = "",
    val source: String
)
