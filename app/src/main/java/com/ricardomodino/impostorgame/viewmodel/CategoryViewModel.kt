package com.ricardomodino.impostorgame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.entities.CategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.WordEntity
import com.ricardomodino.impostorgame.data.repository.ContentRepository
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.WordItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ContentRepository(db)
    private val language = LocaleManager.getLanguage(application)

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    init {
        cargarCategorias()
    }

    fun recargar() = cargarCategorias()

    private fun cargarCategorias() {
        viewModelScope.launch(Dispatchers.IO) {
            val cats = repository.getCategoriesWithWords(language)
            _categories.postValue(cats)
        }
    }

    fun toggleSelection(categoryId: Long) {
        val current = _categories.value ?: return
        val cat = current.firstOrNull { it.id == categoryId } ?: return
        val newSelected = !cat.isSelected
        _categories.value = current.map { c ->
            if (c.id == categoryId) c.copy(isSelected = newSelected) else c
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.categoryDao().updateSelection(categoryId, newSelected)
        }
    }

    fun getSelectedCategories(): List<Category> =
        _categories.value?.filter { it.isSelected } ?: emptyList()

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
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.getWordsForCategory(categoryId, language)
            val current = _categories.value ?: return@launch
            _categories.postValue(current.map { c ->
                if (c.id != categoryId) c else c.copy(items = items)
            })
        }
    }

    fun setCategories(list: List<Category>) {
        _categories.value = list
        viewModelScope.launch(Dispatchers.IO) {
            list.forEach { cat ->
                db.categoryDao().updateSelection(cat.id, cat.isSelected)
            }
        }
    }

    fun logItems(categoryId: Long) {
        val category = _categories.value?.firstOrNull { it.id == categoryId } ?: return
        category.items.forEachIndexed { i, item ->
            android.util.Log.d("CategoryVM", "[$i] ${item.name} - ${item.hints}")
        }
    }

    // ── Gestión de contenido del usuario ─────────────────────────────────────

    fun crearCategoriaLocal(title: String, emoji: String, words: List<Pair<String, String>>, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val catEntity = CategoryEntity(
                titleLocal = title,
                iconEmoji  = emoji,
                source     = CategoryEntity.SOURCE_LOCAL,
                isSelected = false
            )
            val catId = repository.insertCategory(catEntity)
            words.forEachIndexed { index, (name, hint) ->
                repository.insertWord(
                    WordEntity(
                        categoryId = catId,
                        position   = index,
                        nameLocal  = name,
                        hintsLocal = hint,
                        source     = CategoryEntity.SOURCE_LOCAL
                    )
                )
            }
            cargarCategorias()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun editarCategoriaLocal(catId: Long, newTitle: String, newEmoji: String, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = db.categoryDao().getById(catId) ?: return@launch
            db.categoryDao().update(entity.copy(titleLocal = newTitle, iconEmoji = newEmoji))
            cargarCategorias()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun borrarCategoriaLocal(catId: Long, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = db.categoryDao().getById(catId) ?: return@launch
            db.categoryDao().delete(entity)
            cargarCategorias()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun agregarPalabraLocal(catId: Long, name: String, hint: String, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = db.wordDao().getByCategory(catId)
            val nextPos = existing.size
            repository.insertWord(
                WordEntity(
                    categoryId = catId,
                    position   = nextPos,
                    nameLocal  = name,
                    hintsLocal = hint,
                    source     = CategoryEntity.SOURCE_LOCAL
                )
            )
            cargarCategorias()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun borrarPalabraLocal(catId: Long, wordName: String, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val words = db.wordDao().getByCategory(catId)
            val toDelete = words.firstOrNull { it.nameLocal == wordName && it.source == CategoryEntity.SOURCE_LOCAL }
            if (toDelete != null) {
                db.wordDao().delete(toDelete)
                cargarCategorias()
            }
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun getCategoriasLocales(): List<Category> =
        _categories.value?.filter { it.source == CategoryEntity.SOURCE_LOCAL } ?: emptyList()
}
