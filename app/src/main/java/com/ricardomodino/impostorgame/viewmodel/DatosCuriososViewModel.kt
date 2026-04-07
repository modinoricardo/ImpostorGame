package com.ricardomodino.impostorgame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.entities.CategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactCategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactEntity
import com.ricardomodino.impostorgame.data.repository.ContentRepository
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.modelos.DatoCategoria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatosCuriososViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ContentRepository(db)
    private val language = LocaleManager.getLanguage(application)

    private val _categorias = MutableLiveData<List<DatoCategoria>>(emptyList())
    val categorias: LiveData<List<DatoCategoria>> = _categorias

    init {
        cargarCategorias()
    }

    fun recargar() = cargarCategorias()

    private fun cargarCategorias() {
        viewModelScope.launch(Dispatchers.IO) {
            val cats = repository.getFactCategoriesWithFacts(language)
            _categorias.postValue(cats)
        }
    }

    fun getCategoriasActivas(): List<DatoCategoria> {
        val lista = _categorias.value ?: emptyList()
        val seleccionadas = lista.filter { it.isSelected }
        return seleccionadas.ifEmpty { lista }
    }

    fun toggleSelection(id: Long) {
        val lista = _categorias.value ?: return
        val cat = lista.firstOrNull { it.id == id } ?: return
        val newSelected = !cat.isSelected
        _categorias.value = lista.map {
            if (it.id == id) it.copy(isSelected = newSelected) else it
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.factCategoryDao().updateSelection(id, newSelected)
        }
    }

    fun setAllSelected(selected: Boolean) {
        val lista = _categorias.value?.map { it.copy(isSelected = selected) } ?: return
        _categorias.value = lista
        viewModelScope.launch(Dispatchers.IO) {
            lista.forEach { db.factCategoryDao().updateSelection(it.id, selected) }
        }
    }

    // ── Gestión de contenido del usuario ─────────────────────────────────────

    fun crearFactCategoriaLocal(name: String, emoji: String, facts: List<String>, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = FactCategoryEntity(
                nameLocal  = name,
                emoji      = emoji,
                source     = CategoryEntity.SOURCE_LOCAL,
                isSelected = false
            )
            val catId = db.factCategoryDao().insert(entity)
            facts.forEach { text ->
                db.factDao().insert(
                    FactEntity(
                        factCategoryId = catId,
                        textLocal      = text,
                        source         = CategoryEntity.SOURCE_LOCAL
                    )
                )
            }
            cargarCategorias()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun editarFactCategoriaLocal(catId: Long, newName: String, newEmoji: String, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = db.factCategoryDao().getById(catId) ?: return@launch
            db.factCategoryDao().update(entity.copy(nameLocal = newName, emoji = newEmoji))
            cargarCategorias()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun borrarFactCategoriaLocal(catId: Long, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = db.factCategoryDao().getById(catId) ?: return@launch
            db.factCategoryDao().delete(entity)
            cargarCategorias()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun getCategoriasLocales(): List<DatoCategoria> =
        _categorias.value?.filter { it.source == CategoryEntity.SOURCE_LOCAL } ?: emptyList()
}
