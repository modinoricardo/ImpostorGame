package com.example.impostorgame

data class WordItem(
    val name: String,    // palabra que ven todos los no impostores
    val hint: String     // pista para el impostor
)

data class Category(
    val id: Long,                // identificador único
    val title: String,           // "Animales", "Famosos", etc.
    val items: List<WordItem>    // lista de pares (nombre, pista)
)

val animalesCategory = Category(
    id = 1L,
    title = "Animales",
    items = listOf(
        WordItem("perro", "doméstico"),
        WordItem("gato", "felino"),
        WordItem("tiburón", "vive en el mar"),
    )
)

