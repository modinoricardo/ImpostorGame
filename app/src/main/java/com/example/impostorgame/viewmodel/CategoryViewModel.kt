package com.example.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CategoryViewModel : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>(initialCategories())
    val categories: LiveData<List<Category>> = _categories

    private fun initialCategories(): List<Category> {
        return listOf(
            Category(
                id = 1L,
                title = "Animales",
                items = listOf(
                    WordItem("perro", "doméstico"),
                    WordItem("gato", "felino")
                )
            ),
            Category(
                id = 2L,
                title = "Objetos cotidianos",
                items = listOf(
                    WordItem("taza", "sirve para beber"),
                    WordItem("móvil", "lo llevas siempre encima")
                )
            ),
            Category(
                id = 3L,
                title = "Personajes famosos",
                items = listOf(
                    WordItem("Shakira", "cantante colombiana"),
                    WordItem("Messi", "futbolista argentino")
                )
            )
        )
    }

    fun addCategory(title: String) {
        val current = _categories.value ?: emptyList()
        val newId = (current.maxOfOrNull { it.id } ?: 0L) + 1
        _categories.value = current + Category(
            id = newId,
            title = title,
            items = emptyList()
        )
    }

    fun addWordToCategory(categoryId: Long, name: String, hint: String) {
        val current = _categories.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.id == categoryId }
        if (index == -1) return

        val cat = current[index]
        val updatedItems = cat.items + WordItem(name, hint)
        current[index] = cat.copy(items = updatedItems)
        _categories.value = current
    }

    fun removeWordFromCategory(categoryId: Long, wordIndex: Int) {
        val current = _categories.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.id == categoryId }
        if (index == -1) return

        val cat = current[index]
        if (wordIndex !in cat.items.indices) return

        val updatedItems = cat.items.toMutableList().apply { removeAt(wordIndex) }
        current[index] = cat.copy(items = updatedItems)
        _categories.value = current
    }
}
