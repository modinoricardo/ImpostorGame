package com.ricardomodino.impostorgame.data.repository

import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.entities.CategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactCategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactEntity
import com.ricardomodino.impostorgame.data.local.entities.WordEntity
import com.ricardomodino.impostorgame.data.remote.SupabaseClient
import org.json.JSONArray
import org.json.JSONObject

class AdminRepository(private val db: AppDatabase) {

    // ── Categorías de palabras ────────────────────────────────────────────────

    suspend fun crearCategoriaGlobal(titleEs: String, emoji: String): Long {
        val id = System.currentTimeMillis()
        val body = JSONObject().apply {
            put("id", id)
            put("title_es", titleEs)
            put("icon_emoji", emoji)
        }.toString()
        SupabaseClient.post("categories", body)
        db.categoryDao().insert(
            CategoryEntity(id = id, titleEs = titleEs, iconEmoji = emoji, source = CategoryEntity.SOURCE_GLOBAL)
        )
        return id
    }

    suspend fun editarCategoriaGlobal(id: Long, titleEs: String, emoji: String) {
        val body = JSONObject().apply {
            put("title_es", titleEs)
            put("icon_emoji", emoji)
        }.toString()
        SupabaseClient.patch("categories", id, body)
        db.categoryDao().getById(id)?.let {
            db.categoryDao().update(it.copy(titleEs = titleEs, iconEmoji = emoji))
        }
    }

    suspend fun borrarCategoriaGlobal(id: Long) {
        SupabaseClient.delete("categories", id)
        db.categoryDao().getById(id)?.let { db.categoryDao().delete(it) }
    }

    suspend fun getCategoriasGlobales() =
        db.categoryDao().getBySource(CategoryEntity.SOURCE_GLOBAL)

    // ── Palabras ──────────────────────────────────────────────────────────────

    suspend fun crearPalabraGlobal(categoryId: Long, nameEs: String, hintsRaw: String): Long {
        val position = db.wordDao().getByCategory(categoryId).size
        val hints = hintsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString("|")
        val body = JSONObject().apply {
            put("category_id", categoryId)
            put("position", position)
            put("name_es", nameEs)
            put("hints_es", hints)
        }.toString()
        val result = SupabaseClient.post("words", body)
        val wordId = result?.getLong("id") ?: System.currentTimeMillis()
        db.wordDao().insert(
            WordEntity(id = wordId, categoryId = categoryId, position = position,
                nameEs = nameEs, hintsEs = hints, source = CategoryEntity.SOURCE_GLOBAL)
        )
        return wordId
    }

    suspend fun borrarPalabraGlobal(word: WordEntity) {
        SupabaseClient.delete("words", word.id)
        db.wordDao().delete(word)
    }

    suspend fun getPalabrasDe(categoryId: Long) =
        db.wordDao().getByCategory(categoryId).filter { it.source == CategoryEntity.SOURCE_GLOBAL }

    // ── Categorías de datos curiosos ──────────────────────────────────────────

    suspend fun crearFactCategoriaGlobal(nameEs: String, emoji: String): Long {
        val id = System.currentTimeMillis()
        val body = JSONObject().apply {
            put("id", id)
            put("name_es", nameEs)
            put("emoji", emoji)
        }.toString()
        SupabaseClient.post("fact_categories", body)
        db.factCategoryDao().insert(
            FactCategoryEntity(id = id, nameEs = nameEs, emoji = emoji, source = CategoryEntity.SOURCE_GLOBAL)
        )
        return id
    }

    suspend fun editarFactCategoriaGlobal(id: Long, nameEs: String, emoji: String) {
        val body = JSONObject().apply {
            put("name_es", nameEs)
            put("emoji", emoji)
        }.toString()
        SupabaseClient.patch("fact_categories", id, body)
        db.factCategoryDao().getById(id)?.let {
            db.factCategoryDao().update(it.copy(nameEs = nameEs, emoji = emoji))
        }
    }

    suspend fun borrarFactCategoriaGlobal(id: Long) {
        SupabaseClient.delete("fact_categories", id)
        db.factCategoryDao().getById(id)?.let { db.factCategoryDao().delete(it) }
    }

    suspend fun getFactCategoriasGlobales() =
        db.factCategoryDao().getAll().filter { it.source == CategoryEntity.SOURCE_GLOBAL }

    // ── Datos curiosos ────────────────────────────────────────────────────────

    suspend fun crearFactGlobal(categoryId: Long, textEs: String): Long {
        val body = JSONObject().apply {
            put("fact_category_id", categoryId)
            put("text_es", textEs)
        }.toString()
        val result = SupabaseClient.post("facts", body)
        val factId = result?.getLong("id") ?: System.currentTimeMillis()
        db.factDao().insert(
            FactEntity(id = factId, factCategoryId = categoryId, textEs = textEs, source = CategoryEntity.SOURCE_GLOBAL)
        )
        return factId
    }

    suspend fun borrarFactGlobal(fact: FactEntity) {
        SupabaseClient.delete("facts", fact.id)
        db.factDao().delete(fact)
    }

    suspend fun getFactsDe(categoryId: Long) =
        db.factDao().getByCategory(categoryId).filter { it.source == CategoryEntity.SOURCE_GLOBAL }

    // ── Publicar contenido base en Supabase ───────────────────────────────────
    // Sube todas las categorías globales de Room (seed) a Supabase.
    // Usa postOrIgnore para no fallar si ya existen.
    // Devuelve el número de categorías subidas.

    // Sube todo el seed a Supabase en 4 peticiones bulk (una por tabla)
    suspend fun publicarContenidoEnSupabase(): Int {
        val cats = db.categoryDao().getBySource(CategoryEntity.SOURCE_SEED)
        val factCats = db.factCategoryDao().getAll().filter { it.source == CategoryEntity.SOURCE_SEED }

        // 1. Categorías de palabras
        val catsArray = JSONArray()
        cats.forEach { cat ->
            catsArray.put(JSONObject().apply {
                put("id", cat.id)
                put("title_es", cat.titleEs)
                put("title_en", cat.titleEn)
                put("title_zh_hans", cat.titleZhHans)
                put("title_zh_hant", cat.titleZhHant)
                put("icon_emoji", cat.iconEmoji)
            })
        }
        if (catsArray.length() > 0) SupabaseClient.postOrIgnore("categories", catsArray.toString())

        // 2. Palabras (todas de golpe)
        val wordsArray = JSONArray()
        cats.forEach { cat ->
            db.wordDao().getByCategory(cat.id).forEach { word ->
                wordsArray.put(JSONObject().apply {
                    put("category_id", cat.id)
                    put("position", word.position)
                    put("name_es", word.nameEs)
                    put("name_en", word.nameEn)
                    put("name_zh_hans", word.nameZhHans)
                    put("name_zh_hant", word.nameZhHant)
                    put("hints_es", word.hintsEs)
                    put("hints_en", word.hintsEn)
                    put("hints_zh_hans", word.hintsZhHans)
                    put("hints_zh_hant", word.hintsZhHant)
                })
            }
        }
        if (wordsArray.length() > 0) SupabaseClient.postOrIgnore("words", wordsArray.toString())

        // 3. Categorías de datos curiosos
        val factCatsArray = JSONArray()
        factCats.forEach { cat ->
            factCatsArray.put(JSONObject().apply {
                put("id", cat.id)
                put("name_es", cat.nameEs)
                put("name_en", cat.nameEn)
                put("name_zh_hans", cat.nameZhHans)
                put("name_zh_hant", cat.nameZhHant)
                put("emoji", cat.emoji)
            })
        }
        if (factCatsArray.length() > 0) SupabaseClient.postOrIgnore("fact_categories", factCatsArray.toString())

        // 4. Datos curiosos (todos de golpe)
        val factsArray = JSONArray()
        factCats.forEach { cat ->
            db.factDao().getByCategory(cat.id).forEach { fact ->
                factsArray.put(JSONObject().apply {
                    put("fact_category_id", cat.id)
                    put("text_es", fact.textEs)
                    put("text_en", fact.textEn)
                    put("text_zh_hans", fact.textZhHans)
                    put("text_zh_hant", fact.textZhHant)
                })
            }
        }
        if (factsArray.length() > 0) SupabaseClient.postOrIgnore("facts", factsArray.toString())

        return cats.size
    }
}
