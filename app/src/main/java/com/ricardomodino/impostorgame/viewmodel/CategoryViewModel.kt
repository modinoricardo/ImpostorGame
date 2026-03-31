package com.ricardomodino.impostorgame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.WordItem
import org.json.JSONArray

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("categories", Application.MODE_PRIVATE)

    private val _categories = MutableLiveData<List<Category>>(initialCategoriesWithSavedSelections())
    val categories: LiveData<List<Category>> = _categories

    private fun initialCategoriesWithSavedSelections(): List<Category> {
        val selectedIds = prefs.getStringSet("selected_ids", emptySet()) ?: emptySet()
        return initialCategories().map { it.copy(isSelected = it.id.toString() in selectedIds) }
    }

    private fun saveSelectedIds() {
        val ids = _categories.value?.filter { it.isSelected }?.map { it.id.toString() }?.toSet() ?: emptySet()
        prefs.edit().putStringSet("selected_ids", ids).apply()
    }

    private fun initialCategories(): List<Category> {
        val base = parseCategoriesFromRaw("game_categories_es")
        if (base.isEmpty()) return emptyList()

        val ctx = getApplication<Application>()
        val language = LocaleManager.getLanguage(ctx)
        if (language == "es") return base

        val overlayName = "game_categories_${language.lowercase().replace('-', '_')}"
        val localized = parseCategoriesFromRaw(overlayName)
        if (localized.isEmpty()) return base

        val localizedById = localized.associateBy { it.id }
        return base.map { category ->
            val override = localizedById[category.id] ?: return@map category
            category.copy(
                title = override.title.ifBlank { category.title },
                iconEmoji = override.iconEmoji.ifBlank { category.iconEmoji },
                items = override.items.ifEmpty { category.items }
            )
        }
    }

    private fun parseCategoriesFromRaw(resourceName: String): List<Category> {
        val ctx = getApplication<Application>()
        val resId = ctx.resources.getIdentifier(resourceName, "raw", ctx.packageName)
        if (resId == 0) return emptyList()

        return try {
            val text = ctx.resources.openRawResource(resId)
                .bufferedReader()
                .use { it.readText() }

            val arr = JSONArray(text)
            List(arr.length()) { i ->
                val cat = arr.getJSONObject(i)
                val itemsArr = cat.getJSONArray("items")
                val items = List(itemsArr.length()) { j ->
                    val item = itemsArr.getJSONObject(j)
                    val hintsArr = item.optJSONArray("hints") ?: JSONArray()
                    val hints = List(hintsArr.length()) { k -> hintsArr.getString(k) }
                    WordItem(
                        name = item.getString("name"),
                        hints = hints
                    )
                }

                Category(
                    id = cat.getLong("id"),
                    title = cat.getString("title"),
                    iconEmoji = cat.optString("iconEmoji", ""),
                    items = items
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun deleteWordItem(categoryId: Long, itemToDelete: WordItem) {
        val current = _categories.value ?: return
        _categories.value = current.map { c ->
            if (c.id != categoryId) c
            else c.copy(items = c.items.filterNot { it == itemToDelete })
        }
    }

    fun itemsVacio(categoryId: Long): Boolean =
        _categories.value?.firstOrNull { it.id == categoryId }?.items?.isEmpty() ?: true

    fun restoreItems(categoryId: Long) {
        val current = _categories.value ?: return
        val initialItems = initialCategories().firstOrNull { it.id == categoryId }?.items ?: return
        _categories.value = current.map { c ->
            if (c.id != categoryId) c else c.copy(items = initialItems.map { it.copy() })
        }
    }

    fun toggleSelection(categoryId: Long) {
        val current = _categories.value ?: return
        _categories.value = current.map { c ->
            if (c.id == categoryId) c.copy(isSelected = !c.isSelected) else c
        }
        saveSelectedIds()
    }

    fun getSelectedCategories(): List<Category> =
        _categories.value?.filter { it.isSelected } ?: emptyList()

    fun logItems(categoryId: Long) {
        val category = _categories.value?.firstOrNull { it.id == categoryId } ?: return
        category.items.forEachIndexed { i, item ->
            android.util.Log.d("CategoryVM", "[$i] ${item.name} - ${item.hints}")
        }
    }

    fun setCategories(list: List<Category>) {
        _categories.value = list
        saveSelectedIds()
    }
}
