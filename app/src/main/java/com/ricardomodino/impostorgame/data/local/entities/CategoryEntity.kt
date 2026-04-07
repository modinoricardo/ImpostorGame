package com.ricardomodino.impostorgame.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Contenido global: campos multilingüe
    val titleEs: String = "",
    val titleEn: String = "",
    val titleZhHans: String = "",
    val titleZhHant: String = "",
    // Contenido local (usuario): idioma del dispositivo
    val titleLocal: String = "",
    val iconEmoji: String = "",
    val source: String,          // "global" | "local"
    val isSelected: Boolean = false
) {
    companion object {
        const val SOURCE_GLOBAL = "global"
        const val SOURCE_LOCAL  = "local"
        const val SOURCE_SEED   = "seed"   // contenido bundled, nunca reemplazado por el sync
    }
}
