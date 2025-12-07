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
                    WordItem("Perro", "Mascota"),
                    WordItem("Gato", "Felino"),
                    WordItem("Tiburón", "Océano"),
                    WordItem("Águila", "Altura"),
                    WordItem("Elefante", "Trompa")
                )
            ),
            Category(
                id = 2L,
                title = "Objetos cotidianos",
                iconEmoji = "🏠",
                items = listOf(
                    WordItem("Taza", "Café"),
                    WordItem("Móvil", "Pantalla"),
                    WordItem("Llaves", "Cerradura"),
                    WordItem("Mochila", "Libros"),
                    WordItem("Sofá", "Siesta"),
                    WordItem("Lámpara", "Bombilla"),
                    WordItem("Reloj", "Minutos")
                )
            ),
            Category(
                id = 3L,
                title = "Personajes famosos",
                iconEmoji = "👤",
                items = listOf(
                    WordItem("Shakira", "Caderas"),
                    WordItem("Messi", "Balón"),
                    WordItem("Taylor Swift", "Conciertos"),
                    WordItem("Leonardo DiCaprio", "Óscar"),
                    WordItem("Rihanna", "Isla")
                )
            ),
            Category(
                id = 4L,
                title = "Superhéroes",
                iconEmoji = "\uD83E\uDDB8\u200D♂\uFE0F",
                items = listOf(
                    WordItem("Superman", "Capa"),
                    WordItem("Batman", "Murciélago"),
                    WordItem("Spiderman", "Telarañas"),
                    WordItem("Iron Man", "Tecnología"),
                    WordItem("Wonder Woman", "Lazo")
                )
            ),
            Category(
                id = 5L,
                title = "Comida",
                iconEmoji = "🍕",
                items = listOf(
                    WordItem("Lasaña", "Capas"),
                    WordItem("Croqueta", "Rellena"),
                    WordItem("Gazpacho", "Tomate"),
                    WordItem("Manzana", "Roja"),
                    WordItem("Espaguetis", "Tenedor"),
                    WordItem("Hamburguesa", "Carne"),
                    WordItem("Tortilla", "Huevos")
                )
            )
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
