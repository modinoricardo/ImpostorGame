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
                iconEmoji = "🦁",
                items = listOf(
                    WordItem("perro", "doméstico"),
                    WordItem("gato", "felino")
                )
            ),
            Category(
                id = 2L,
                title = "Objetos cotidianos",
                iconEmoji = "🏠",
                items = listOf(
                    WordItem("taza", "sirve para beber"),
                    WordItem("móvil", "lo llevas siempre encima")
                )
            ),
            Category(
                id = 3L,
                title = "Personajes famosos",
                iconEmoji = "👤",
                items = listOf(
                    WordItem("Shakira", "cantante colombiana"),
                    WordItem("Messi", "futbolista argentino")
                )


            ),
            Category(
                id = 4L,
                title = "Superheroes",
                iconEmoji = "",
                items = listOf(
                    WordItem("Spider-Man", "Vecino"),
                    WordItem("Superman", "Esperanza"),
                    WordItem("Wolverine", "Regeneración"),
                    WordItem("MoonKnight", "Lunas")


                ),
            ),
            Category(
                id = 5L,
                title = "Comidas",
                iconEmoji = "b",
                items = listOf(WordItem("Lentejas", "abuela"))
            ),
        )
    }


    // --- NUEVO: gestionar selección desde el ViewModel ---

    fun toggleSelection(categoryId: Long) {
        val current = _categories.value ?: return
        _categories.value = current.map { c ->
            if (c.id == categoryId) c.copy(isSelected = !c.isSelected) else c
        }
    }

    fun getSelectedCategories(): List<Category> {
        return _categories.value?.filter { it.isSelected } ?: emptyList()
    }

    fun clearSelection() {
        val current = _categories.value ?: return
        _categories.value = current.map { it.copy(isSelected = false) }
    }
}
