package com.ricardomodino.impostorgame.data.repository

import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.entities.*
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.DatoCategoria
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.WordItem

class ContentRepository(private val db: AppDatabase) {

    // ── Categories ───────────────────────────────────────────────────────────

    suspend fun getCategoriesWithWords(language: String): List<Category> {
        return db.categoryDao().getAll().mapNotNull { entity ->
            val title = entity.getDisplayTitle(language) ?: return@mapNotNull null
            val words = db.wordDao().getByCategory(entity.id)
            val wordItems = words.mapNotNull { it.toWordItem(language) }
            Category(
                id = entity.id,
                title = title,
                iconEmoji = entity.iconEmoji,
                isSelected = entity.isSelected,
                items = wordItems,
                source = entity.source
            )
        }
    }

    suspend fun getWordsForCategory(categoryId: Long, language: String): List<WordItem> =
        db.wordDao().getByCategory(categoryId).mapNotNull { it.toWordItem(language) }

    suspend fun insertCategory(entity: CategoryEntity): Long =
        db.categoryDao().insert(entity)

    suspend fun updateCategory(entity: CategoryEntity) =
        db.categoryDao().update(entity)

    suspend fun deleteCategory(entity: CategoryEntity) =
        db.categoryDao().delete(entity)

    suspend fun getCategoryById(id: Long): CategoryEntity? =
        db.categoryDao().getById(id)

    suspend fun toggleCategorySelection(id: Long, currentlySelected: Boolean) =
        db.categoryDao().updateSelection(id, !currentlySelected)

    suspend fun insertWord(entity: WordEntity): Long =
        db.wordDao().insert(entity)

    suspend fun updateWord(entity: WordEntity) =
        db.wordDao().update(entity)

    suspend fun deleteWord(entity: WordEntity) =
        db.wordDao().delete(entity)

    suspend fun getWordsByCategory(categoryId: Long): List<WordEntity> =
        db.wordDao().getByCategory(categoryId)

    // ── Fact Categories ───────────────────────────────────────────────────────

    suspend fun getFactCategoriesWithFacts(language: String): List<DatoCategoria> {
        return db.factCategoryDao().getAll().mapNotNull { entity ->
            val name = entity.getDisplayName(language) ?: return@mapNotNull null
            val facts = db.factDao().getByCategory(entity.id)
            val datosCuriosos = facts.mapNotNull { it.toDatoCurioso(language) }
            val nameLocal = if (entity.source == CategoryEntity.SOURCE_LOCAL) entity.nameLocal else ""
            DatoCategoria(
                id = entity.id,
                es = if (entity.source == CategoryEntity.SOURCE_LOCAL) nameLocal else entity.nameEs,
                en = if (entity.source == CategoryEntity.SOURCE_LOCAL) nameLocal else entity.nameEn,
                zhHans = if (entity.source == CategoryEntity.SOURCE_LOCAL) nameLocal else entity.nameZhHans,
                zhHant = if (entity.source == CategoryEntity.SOURCE_LOCAL) nameLocal else entity.nameZhHant,
                emoji = entity.emoji,
                isSelected = entity.isSelected,
                datos = datosCuriosos,
                source = entity.source
            )
        }
    }

    suspend fun insertFactCategory(entity: FactCategoryEntity): Long =
        db.factCategoryDao().insert(entity)

    suspend fun updateFactCategory(entity: FactCategoryEntity) =
        db.factCategoryDao().update(entity)

    suspend fun deleteFactCategory(entity: FactCategoryEntity) =
        db.factCategoryDao().delete(entity)

    suspend fun getFactCategoryById(id: Long): FactCategoryEntity? =
        db.factCategoryDao().getById(id)

    suspend fun toggleFactCategorySelection(id: Long, currentlySelected: Boolean) =
        db.factCategoryDao().updateSelection(id, !currentlySelected)

    suspend fun insertFact(entity: FactEntity): Long =
        db.factDao().insert(entity)

    suspend fun updateFact(entity: FactEntity) =
        db.factDao().update(entity)

    suspend fun deleteFact(entity: FactEntity) =
        db.factDao().delete(entity)

    suspend fun getFactsByCategory(categoryId: Long): List<FactEntity> =
        db.factDao().getByCategory(categoryId)

    // ── Seed check ────────────────────────────────────────────────────────────

    suspend fun isSeeded(): Boolean =
        db.categoryDao().countSeed() > 0
}

// ── Extension functions ───────────────────────────────────────────────────────

fun CategoryEntity.getDisplayTitle(language: String): String? {
    return if (source == CategoryEntity.SOURCE_LOCAL) {
        titleLocal.ifBlank { null }
    } else {
        // Global: always show, fallback to Spanish
        when (language) {
            "en"      -> titleEn.ifBlank { titleEs }
            "zh-Hans" -> titleZhHans.ifBlank { titleEs }
            "zh-Hant" -> titleZhHant.ifBlank { titleEs }
            else      -> titleEs
        }.ifBlank { null }
    }
}

fun WordEntity.toWordItem(language: String): WordItem? {
    val name = if (source == CategoryEntity.SOURCE_LOCAL) {
        nameLocal.ifBlank { return null }
    } else {
        // Global: Option B — si no hay traducción, no mostrar la palabra
        when (language) {
            "en"      -> nameEn.ifBlank { return null }
            "zh-Hans" -> nameZhHans.ifBlank { return null }
            "zh-Hant" -> nameZhHant.ifBlank { return null }
            else      -> nameEs.ifBlank { return null }
        }
    }
    val hintsRaw = if (source == CategoryEntity.SOURCE_LOCAL) {
        hintsLocal
    } else {
        when (language) {
            "en"      -> hintsEn
            "zh-Hans" -> hintsZhHans
            "zh-Hant" -> hintsZhHant
            else      -> hintsEs
        }
    }
    val hints = if (hintsRaw.isBlank()) emptyList() else hintsRaw.split("|")
    return WordItem(name = name, hints = hints)
}

fun FactCategoryEntity.getDisplayName(language: String): String? {
    return if (source == CategoryEntity.SOURCE_LOCAL) {
        nameLocal.ifBlank { null }
    } else {
        when (language) {
            "en"      -> nameEn.ifBlank { nameEs }
            "zh-Hans" -> nameZhHans.ifBlank { nameEs }
            "zh-Hant" -> nameZhHant.ifBlank { nameEs }
            else      -> nameEs
        }.ifBlank { null }
    }
}

fun FactEntity.toDatoCurioso(language: String): DatoCurioso? {
    return if (source == CategoryEntity.SOURCE_LOCAL) {
        if (textLocal.isBlank()) return null
        DatoCurioso(
            id = id,
            es = textLocal,
            en = textLocal,
            zhHans = textLocal,
            zhHant = textLocal
        )
    } else {
        // Global: Option B — si no hay traducción, no mostrar
        val text = when (language) {
            "en"      -> textEn.ifBlank { return null }
            "zh-Hans" -> textZhHans.ifBlank { return null }
            "zh-Hant" -> textZhHant.ifBlank { return null }
            else      -> textEs.ifBlank { return null }
        }
        DatoCurioso(
            id = id,
            es = textEs,
            en = textEn,
            zhHans = textZhHans,
            zhHant = textZhHant
        )
    }
}
