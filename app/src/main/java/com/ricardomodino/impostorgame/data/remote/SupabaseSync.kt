package com.ricardomodino.impostorgame.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.entities.CategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactCategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactEntity
import com.ricardomodino.impostorgame.data.local.entities.WordEntity

object SupabaseSync {

    suspend fun sync(context: Context, db: AppDatabase) {
        if (!isConnected(context)) return
        try {
            syncCategories(db)
            syncFactCategories(db)
        } catch (_: Exception) {
            // Error de red o parsing → Room intacto, la app sigue funcionando
        }
    }

    private suspend fun syncCategories(db: AppDatabase) {
        val json = SupabaseClient.get("categories?select=*,words(*)")

        // Si Supabase está vacío, no tocar Room (evita borrar el contenido seed)
        if (json.length() == 0) return

        // Borrar entradas SOURCE_SEED que tengan el mismo ID que las de Supabase
        // (Room usa IGNORE al insertar → sin esto el SOURCE_GLOBAL se descartaría)
        for (i in 0 until json.length()) {
            val catId = json.getJSONObject(i).getLong("id")
            db.categoryDao().getById(catId)?.let { existing ->
                if (existing.source == CategoryEntity.SOURCE_SEED) {
                    db.categoryDao().delete(existing) // CASCADE elimina sus palabras seed
                }
            }
        }

        // Preservar selección actual antes de borrar
        val selectedIds = db.categoryDao()
            .getBySource(CategoryEntity.SOURCE_GLOBAL)
            .filter { it.isSelected }
            .map { it.id }
            .toSet()

        // Borrar todo el contenido global (CASCADE borra las palabras)
        db.categoryDao()
            .getBySource(CategoryEntity.SOURCE_GLOBAL)
            .forEach { db.categoryDao().delete(it) }

        // Insertar contenido fresco desde Supabase
        for (i in 0 until json.length()) {
            val cat   = json.getJSONObject(i)
            val catId = cat.getLong("id")

            db.categoryDao().insert(
                CategoryEntity(
                    id          = catId,
                    titleEs     = cat.optString("title_es"),
                    titleEn     = cat.optString("title_en"),
                    titleZhHans = cat.optString("title_zh_hans"),
                    titleZhHant = cat.optString("title_zh_hant"),
                    iconEmoji   = cat.optString("icon_emoji"),
                    source      = CategoryEntity.SOURCE_GLOBAL,
                    isSelected  = catId in selectedIds
                )
            )

            val words = cat.optJSONArray("words") ?: continue
            for (j in 0 until words.length()) {
                val w = words.getJSONObject(j)
                db.wordDao().insert(
                    WordEntity(
                        categoryId  = catId,
                        position    = w.optInt("position", j),
                        nameEs      = w.optString("name_es"),
                        nameEn      = w.optString("name_en"),
                        nameZhHans  = w.optString("name_zh_hans"),
                        nameZhHant  = w.optString("name_zh_hant"),
                        hintsEs     = w.optString("hints_es"),
                        hintsEn     = w.optString("hints_en"),
                        hintsZhHans = w.optString("hints_zh_hans"),
                        hintsZhHant = w.optString("hints_zh_hant"),
                        source      = CategoryEntity.SOURCE_GLOBAL
                    )
                )
            }
        }
    }

    private suspend fun syncFactCategories(db: AppDatabase) {
        val json = SupabaseClient.get("fact_categories?select=*,facts(*)")

        // Si Supabase está vacío, no tocar Room
        if (json.length() == 0) return

        // Borrar entradas SOURCE_SEED con el mismo ID que las de Supabase
        for (i in 0 until json.length()) {
            val catId = json.getJSONObject(i).getLong("id")
            db.factCategoryDao().getById(catId)?.let { existing ->
                if (existing.source == CategoryEntity.SOURCE_SEED) {
                    db.factCategoryDao().delete(existing) // CASCADE elimina sus facts seed
                }
            }
        }

        // Preservar selección actual
        val selectedIds = db.factCategoryDao()
            .getAll()
            .filter { it.source == CategoryEntity.SOURCE_GLOBAL && it.isSelected }
            .map { it.id }
            .toSet()

        // Borrar todo el contenido global (CASCADE borra los facts)
        db.factCategoryDao()
            .getAll()
            .filter { it.source == CategoryEntity.SOURCE_GLOBAL }
            .forEach { db.factCategoryDao().delete(it) }

        // Insertar contenido fresco desde Supabase
        for (i in 0 until json.length()) {
            val cat   = json.getJSONObject(i)
            val catId = cat.getLong("id")

            db.factCategoryDao().insert(
                FactCategoryEntity(
                    id         = catId,
                    nameEs     = cat.optString("name_es"),
                    nameEn     = cat.optString("name_en"),
                    nameZhHans = cat.optString("name_zh_hans"),
                    nameZhHant = cat.optString("name_zh_hant"),
                    emoji      = cat.optString("emoji"),
                    source     = CategoryEntity.SOURCE_GLOBAL,
                    isSelected = catId in selectedIds
                )
            )

            val facts = cat.optJSONArray("facts") ?: continue
            for (j in 0 until facts.length()) {
                val f = facts.getJSONObject(j)
                db.factDao().insert(
                    FactEntity(
                        factCategoryId = catId,
                        textEs         = f.optString("text_es"),
                        textEn         = f.optString("text_en"),
                        textZhHans     = f.optString("text_zh_hans"),
                        textZhHant     = f.optString("text_zh_hant"),
                        source         = CategoryEntity.SOURCE_GLOBAL
                    )
                )
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps    = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
