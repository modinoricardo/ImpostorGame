package com.ricardomodino.impostorgame.data.local

import android.content.Context
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.data.local.entities.CategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactCategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactEntity
import com.ricardomodino.impostorgame.data.local.entities.WordEntity
import org.json.JSONArray

/**
 * Siembra la base de datos Room con el contenido de los JSON bundled.
 * Solo se ejecuta una vez (si no hay categorías globales en Room).
 */
object DatabaseSeeder {

    suspend fun seed(context: Context, db: AppDatabase) {
        seedCategories(context, db)
        seedFactCategories(context, db)
    }

    // ── Categorías de palabras ────────────────────────────────────────────────

    private suspend fun seedCategories(context: Context, db: AppDatabase) {
        // 1. Parsear español (master)
        val esData = parseCategoryJson(context, "game_categories_es") ?: return

        // Mapa: jsonId → roomId asignado tras inserción
        val jsonIdToRoomId = mutableMapOf<Long, Long>()

        // 2. Insertar categorías en español
        for (i in 0 until esData.length()) {
            val cat = esData.getJSONObject(i)
            val jsonId = cat.getLong("id")
            val entity = CategoryEntity(
                id         = jsonId,   // usamos el ID del JSON directamente
                titleEs    = cat.getString("title"),
                iconEmoji  = cat.optString("iconEmoji", ""),
                source     = CategoryEntity.SOURCE_SEED,
                isSelected = false
            )
            val roomId = db.categoryDao().insert(entity)
            // insert con IGNORE: si roomId == -1 significa que ya existía → usar jsonId
            jsonIdToRoomId[jsonId] = if (roomId == -1L) jsonId else roomId

            // 3. Insertar palabras en español para esta categoría
            val items = cat.getJSONArray("items")
            for (j in 0 until items.length()) {
                val item = items.getJSONObject(j)
                val hintsArr = item.optJSONArray("hints") ?: JSONArray()
                val hints = (0 until hintsArr.length()).joinToString("|") { hintsArr.getString(it) }
                db.wordDao().insert(
                    WordEntity(
                        categoryId = jsonIdToRoomId[jsonId]!!,
                        position   = j,
                        nameEs     = item.getString("name"),
                        hintsEs    = hints,
                        source     = CategoryEntity.SOURCE_GLOBAL
                    )
                )
            }
        }

        // 4. Superponer traducciones
        overlayLanguage(context, db, "game_categories_en",      jsonIdToRoomId, "en")
        overlayLanguage(context, db, "game_categories_zh_hans", jsonIdToRoomId, "zh-Hans")
        overlayLanguage(context, db, "game_categories_zh_hant", jsonIdToRoomId, "zh-Hant")
    }

    private suspend fun overlayLanguage(
        context: Context,
        db: AppDatabase,
        resourceName: String,
        jsonIdToRoomId: Map<Long, Long>,
        language: String
    ) {
        val data = parseCategoryJson(context, resourceName) ?: return
        for (i in 0 until data.length()) {
            val cat = data.getJSONObject(i)
            val jsonId  = cat.getLong("id")
            val roomId  = jsonIdToRoomId[jsonId] ?: continue
            val title   = cat.optString("title", "")
            val items   = cat.optJSONArray("items") ?: continue

            // Actualizar título de la categoría
            if (title.isNotBlank()) {
                val existing = db.categoryDao().getById(roomId) ?: continue
                val updated = when (language) {
                    "en"      -> existing.copy(titleEn = title)
                    "zh-Hans" -> existing.copy(titleZhHans = title)
                    "zh-Hant" -> existing.copy(titleZhHant = title)
                    else      -> existing
                }
                db.categoryDao().update(updated)
            }

            // Actualizar palabras por posición
            for (j in 0 until items.length()) {
                val item    = items.getJSONObject(j)
                val name    = item.optString("name", "")
                val hintsArr = item.optJSONArray("hints") ?: JSONArray()
                val hints   = (0 until hintsArr.length()).joinToString("|") { hintsArr.getString(it) }
                if (name.isBlank()) continue
                when (language) {
                    "en"      -> db.wordDao().updateEnglish(roomId, j, name, hints)
                    "zh-Hans" -> db.wordDao().updateZhHans(roomId, j, name, hints)
                    "zh-Hant" -> db.wordDao().updateZhHant(roomId, j, name, hints)
                }
            }
        }
    }

    private fun parseCategoryJson(context: Context, resourceName: String): JSONArray? {
        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resId == 0) return null
        return try {
            val text = context.resources.openRawResource(resId).bufferedReader().readText()
            JSONArray(text)
        } catch (_: Exception) { null }
    }

    // ── Datos curiosos ────────────────────────────────────────────────────────

    private suspend fun seedFactCategories(context: Context, db: AppDatabase) {
        val text = try {
            context.resources.openRawResource(R.raw.datos_curiosos).bufferedReader().readText()
        } catch (_: Exception) { return }

        val arr = try { JSONArray(text) } catch (_: Exception) { return }

        for (i in 0 until arr.length()) {
            val cat = arr.getJSONObject(i)
            val jsonId = cat.getLong("id")
            val entity = FactCategoryEntity(
                id         = jsonId,
                nameEs     = cat.getString("es"),
                nameEn     = cat.getString("en"),
                nameZhHans = cat.getString("zhHans"),
                nameZhHant = cat.getString("zhHant"),
                emoji      = cat.getString("emoji"),
                source     = CategoryEntity.SOURCE_SEED,
                isSelected = false
            )
            val roomId = db.factCategoryDao().insert(entity)
            val effectiveId = if (roomId == -1L) jsonId else roomId

            val datosArr = cat.getJSONArray("datos")
            for (j in 0 until datosArr.length()) {
                val d = datosArr.getJSONObject(j)
                db.factDao().insert(
                    FactEntity(
                        factCategoryId = effectiveId,
                        textEs         = d.getString("es"),
                        textEn         = d.getString("en"),
                        textZhHans     = d.getString("zhHans"),
                        textZhHant     = d.getString("zhHant"),
                        source         = CategoryEntity.SOURCE_GLOBAL
                    )
                )
            }
        }
    }
}
