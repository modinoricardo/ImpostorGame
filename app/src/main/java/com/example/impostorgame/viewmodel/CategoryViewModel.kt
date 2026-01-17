package com.example.impostorgame

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.impostorgame.modelos.Category
import com.example.impostorgame.modelos.WordItem

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
                    WordItem("Perro", listOf("Mascota", "Ladra", "Paseo")),
                    WordItem("Gato", listOf("Felino", "Maúlla", "Bigotes")),
                    WordItem("Tiburón", listOf("Océano", "Aletas", "Depredador")),
                    WordItem("Aguila", listOf("Altura", "Pico", "Alas")),
                    WordItem("Elefante", listOf("Trompa", "Colmillos", "Gigante")),
                    WordItem("Leon", listOf("Sabana", "Melena", "Ruge")),
                    WordItem("Delfin", listOf("Mar", "Inteligente", "Salta")),
                    WordItem("Lobo", listOf("Aullido", "Manada", "Bosque")),
                    WordItem("Jirafa", listOf("Cuello largo", "Manchas", "Africa")),
                    WordItem("Panda", listOf("Bambu", "Blanco y negro", "China")),
                    WordItem("Canguro", listOf("Salta", "Marsupial", "Australia")),
                    WordItem("Pingüino", listOf("Hielo", "No vuela", "Antartida")),
                    WordItem("Camaleon", listOf("Cambia de color", "Lengua larga", "Reptil")),
                    WordItem("Buho", listOf("Noche", "Ojos grandes", "Ulula")),
                    WordItem("Zorro", listOf("Astuto", "Cola", "Bosque")),
                    WordItem("Tigre", listOf("Rayas", "Selva", "Felino")),
                    WordItem("Hipopotamo", listOf("Rio", "Grande", "Agresivo")),
                    WordItem("Koala", listOf("Eucalipto", "Marsupial", "Duerme mucho")),
                    WordItem("Rinoceronte", listOf("Cuerno", "Pesado", "Sabana")),
                    WordItem("Pulpo", listOf("Tentaculos", "Mar", "Inteligente")),
                    WordItem("Serpiente", listOf("Reptil", "Sin patas", "Veneno")),
                    WordItem("Oso polar", listOf("Artico", "Blanco", "Carnívoro")),
                    WordItem("Mono", listOf("Arboles", "Banana", "Tarzan")),
                    WordItem("Pavo real", listOf("Plumas", "Cola", "Colorido")),
                    WordItem("Murcielago", listOf("Nocturno", "Vuela", "Cueva", "Covid-19"))
                )
            ),
            Category(
                id = 2L,
                title = "Objetos cotidianos",
                iconEmoji = "🏠",
                items = listOf(
                    WordItem("Taza", listOf("cafe", "liquido", "ceramica")),
                    WordItem("Movil", listOf("pantalla", "llamadas", "apps")),
                    WordItem("Llaves", listOf("cerradura", "metal", "bolsillo")),
                    WordItem("Mochila", listOf("libros", "espalda", "cremallera")),
                    WordItem("Sofa", listOf("siesta", "salon", "comodidad")),
                    WordItem("Lampara", listOf("bombilla", "luz", "mesa")),
                    WordItem("Reloj", listOf("minutos", "hora", "muneca")),
                    WordItem("Mesa", listOf("madera", "comer", "patas")),
                    WordItem("Silla", listOf("sentarse", "respaldo", "patas")),
                    WordItem("Cuchara", listOf("sopa", "metal", "comer")),
                    WordItem("Tenedor", listOf("pinchos", "plato", "cubierto")),
                    WordItem("Cuchillo", listOf("cortar", "filo", "cocina")),
                    WordItem("Vaso", listOf("agua", "cristal", "beber")),
                    WordItem("Botella", listOf("tapon", "liquido", "plastico")),
                    WordItem("Television", listOf("pantalla", "canales", "sofa")),
                    WordItem("Ordenador", listOf("teclado", "raton", "pantalla")),
                    WordItem("Portatil", listOf("bateria", "pantalla", "teclado")),
                    WordItem("Cargador", listOf("energia", "cable", "enchufe")),
                    WordItem("Auriculares", listOf("musica", "orejas", "sonido")),
                    WordItem("Ventana", listOf("cristal", "luz", "abrir")),
                    WordItem("Puerta", listOf("abrir", "cerrar", "entrada")),
                    WordItem("Espejo", listOf("reflejo", "cara", "cristal")),
                    WordItem("Cama", listOf("dormir", "colchon", "sabana")),
                    WordItem("Almohada", listOf("cabeza", "dormir", "blanda")),
                    WordItem("Mando", listOf("botones", "television", "pilas")),
                    WordItem("Enchufe", listOf("corriente", "pared", "energia")),
                    WordItem("Alfombra", listOf("suelo", "pisar", "decoracion"))
                )
            ),
            Category(
                id = 3L,
                title = "Personajes famosos",
                iconEmoji = "👤",
                items = listOf(
                    WordItem("Shakira", listOf("Caderas")),
                    WordItem("Messi", listOf("Balón")),
                    WordItem("Taylor Swift", listOf("Conciertos")),
                    WordItem("Leonardo DiCaprio", listOf("Óscar")),
                    WordItem("Hitler", listOf("El que llega más alto con la mano"))
                    )
            ),
            Category(
                id = 4L,
                title = "Superhéroes",
                iconEmoji = "\uD83E\uDDB8\u200D♂\uFE0F",
                items = listOf(
                    WordItem("Superman", listOf("capa", "volar", "fuerza")),
                    WordItem("Batman", listOf("murcielago", "noche", "detective")),
                    WordItem("Spiderman", listOf("telaranas", "trepar", "aracnido")),
                    WordItem("Iron Man", listOf("armadura", "tecnologia", "vuelo")),
                    WordItem("Wonder Woman", listOf("lazo", "amazona", "fuerza")),
                    WordItem("Hulk", listOf("verde", "fuerza", "ira")),
                    WordItem("Thor", listOf("martillo", "trueno", "dios")),
                    WordItem("Capitan America", listOf("escudo", "lider", "soldado")),
                    WordItem("Flash", listOf("velocidad", "rapido", "tiempo")),
                    WordItem("Aquaman", listOf("mar", "tridente", "oceano")),
                    WordItem("Black Widow", listOf("espia", "combate", "sigilo")),
                    WordItem("Doctor Strange", listOf("magia", "hechizos", "tiempo")),
                    WordItem("Pantera Negra", listOf("wakanda", "traje", "rey")),
                    WordItem("Ant Man", listOf("pequeno", "grande", "hormiga")),
                    WordItem("Vision", listOf("mente", "gema", "android")),
                    WordItem("Scarlet Witch", listOf("magia", "caos", "poder")),
                    WordItem("Green Lantern", listOf("anillo", "voluntad", "energia")),
                    WordItem("Deadpool", listOf("humor", "regeneracion", "katana")),
                    WordItem("Wolverine", listOf("garras", "metal", "regeneracion")),
                    WordItem("Capitana Marvel", listOf("energia", "volar", "espacio"))
                )
            ),
            Category(
                id = 5L,
                title = "Comida",
                iconEmoji = "🍕",
                items = listOf(
                    WordItem("Lasaña", listOf("capas", "pasta", "horno")),
                    WordItem("Croqueta", listOf("rellena", "frita", "bechamel")),
                    WordItem("Gazpacho", listOf("tomate", "frio", "verano")),
                    WordItem("Manzana", listOf("roja", "fruta", "morder")),
                    WordItem("Espaguetis", listOf("tenedor", "pasta", "salsa")),
                    WordItem("Hamburguesa", listOf("carne", "pan", "queso")),
                    WordItem("Tortilla", listOf("huevos", "patata", "sarten")),
                    WordItem("Pizza", listOf("queso", "masa", "horno")),
                    WordItem("Paella", listOf("arroz", "sarten", "marisco")),
                    WordItem("Ensalada", listOf("lechuga", "tomate", "aliño")),
                    WordItem("Sopa", listOf("caldo", "cuchara", "caliente")),
                    WordItem("Sandwich", listOf("pan", "relleno", "lonchas")),
                    WordItem("Helado", listOf("frio", "dulce", "cono")),
                    WordItem("Chocolate", listOf("dulce", "cacao", "tableta")),
                    WordItem("Queso", listOf("leche", "curado", "lonchas")),
                    WordItem("Yogur", listOf("cuchara", "frio", "lacteo")),
                    WordItem("Pan", listOf("harina", "horno", "miga")),
                    WordItem("Arroz", listOf("grano", "blanco", "cocer")),
                    WordItem("Pollo", listOf("carne", "asado", "pechuga")),
                    WordItem("Pescado", listOf("mar", "espinas", "plancha")),
                    WordItem("Sushi", listOf("arroz", "pescado", "palillos")),
                    WordItem("Burrito", listOf("tortilla", "relleno", "mexico")),
                    WordItem("Kebab", listOf("pan", "carne", "salsa", "rollo")),
                    WordItem("Empanada", listOf("masa", "relleno", "horno")),
                    WordItem("Churros", listOf("fritos", "azucar", "chocolate")),
                    WordItem("Galletas", listOf("dulce", "horno", "crujiente")),
                    WordItem("Cereales", listOf("leche", "desayuno", "bol")),
                    WordItem("Cafe", listOf("taza", "caliente", "energia"))
                )
            ), Category(
                id = 6L,
                title = "Colores",
                iconEmoji = "\uD83E\uDE85",
                items = listOf(
                    WordItem("Rojo", listOf("sangre", "fuego", "pasión")),
                    WordItem("Verde", listOf("esperanza", "hierba", "naturaleza")),
                    WordItem("Naranja", listOf("mandarina", "atardecer", "fruta")),
                    WordItem("Azul", listOf("cielo", "mar", "frio")),
                    WordItem("Blanco", listOf("nieve", "luz", "puro")),
                    WordItem("Negro", listOf("carbon", "noche", "sombra")),
                    WordItem("Gris", listOf("ceniza", "nublado", "metal")),
                    WordItem("Amarillo", listOf("sol", "limon", "brillante")),
                    WordItem("Rosa", listOf("flor", "romantico", "chicle")),
                    WordItem("Morado", listOf("uva", "misterio", "noche")),
                    WordItem("Violeta", listOf("flor", "intenso", "tono")),
                    WordItem("Marron", listOf("tierra", "madera", "chocolate")),
                    WordItem("Beige", listOf("arena", "suave", "neutral")),
                    WordItem("Turquesa", listOf("mar", "gema", "playa")),
                    WordItem("Cian", listOf("tinta", "azulado", "agua")),
                    WordItem("Magenta", listOf("impresion", "fuerte", "tinta")),
                    WordItem("Fucsia", listOf("vivo", "rosa", "llamativo")),
                    WordItem("Lila", listOf("suave", "flor", "morado")),
                    WordItem("Coral", listOf("mar", "rosado", "arrecife")),
                    WordItem("Salmon", listOf("pescado", "rosado", "tono")),
                    WordItem("Ocre", listOf("tierra", "amarillo", "antiguo")),
                    WordItem("Oliva", listOf("verde", "aceite", "militar")),
                    WordItem("Esmeralda", listOf("gema", "verde", "brillo")),
                    WordItem("Azul marino", listOf("oscuro", "mar", "uniforme")),
                    WordItem("Celeste", listOf("claro", "cielo", "suave")),
                    WordItem("Granate", listOf("vino", "oscuro", "rojo")),
                    WordItem("Plateado", listOf("metal", "brillo", "gris")),
                    WordItem("Dorado", listOf("oro", "lujo", "brillo"))
                )
            ), Category(
                id = 7L,
                title = "Random",
                iconEmoji = "\uD83D\uDE40",
                items = listOf(
                    WordItem(
                        "Jugador más cercano (distancia física)",
                        listOf("Está en tu 'burbuja'", "No tienes que proyectar la voz", "Con un gesto basta")
                    ),
                    WordItem(
                        "Jugador más mayor (edad más alta)",
                        listOf("Tiene referencias que no todos pillan", "Ha visto cosas repetirse", "Se nota en cómo lo cuenta")
                    ),
                    WordItem(
                        "Jugador más alto (mayor estatura)",
                        listOf("Cambia la perspectiva del grupo", "En fotos, tu ojo va ahí", "No pasa desapercibido de pie")
                    ),
                    WordItem(
                        "Jugador más joven (edad más baja)",
                        listOf("Tiene 'primera vez' en muchas cosas", "Le sobra energía a ciertas horas", "Ciertas frases le suenan normales")
                    ),
                    WordItem(
                        "Jugador con el pelo más largo",
                        listOf("Tiene 'rutina' sin decirlo", "A veces estorba y a veces ayuda", "En movimiento se nota más")
                    ),
                    WordItem(
                        "Jugador con la voz más grave",
                        listOf("Su tono llena el espacio", "Da sensación de 'radio'", "Con poco volumen, llega igual")
                    ),
                    WordItem(
                        "Jugador con la voz más aguda",
                        listOf("Destaca sin querer", "Cambia mucho según el momento", "En emoción se delata")
                    ),
                    WordItem(
                        "Jugador más puntual",
                        listOf("Para él/ella, 'a tiempo' no existe", "Prefiere esperar que llegar justo", "Le incomoda improvisar horarios")
                    ),
                    WordItem(
                        "Jugador más despistado",
                        listOf("Va en automático a ratos", "Se le escapan detalles obvios", "Vive en su propio hilo")
                    ),
                    WordItem(
                        "Jugador más ordenado",
                        listOf("Se nota en lo pequeño", "Le molesta el desorden ajeno", "Tiene su manera de hacerlo todo")
                    ),
                    WordItem(
                        "Jugador más dormilón",
                        listOf("Se apaga fácil", "El descanso le llama", "Siempre podría ser 'un ratito más'")
                    ),
                    WordItem(
                        "Jugador más deportista",
                        listOf("Su cuerpo habla por él/ella", "Tiene hábitos que se notan", "El movimiento es su idioma")
                    ),
                    WordItem(
                        "Jugador más competitivo",
                        listOf("Se le encienden los ojos", "No se lo toma 'solo por jugar'", "Necesita saber quién gana")
                    ),
                    WordItem(
                        "Jugador más tranquilo",
                        listOf("Baja la temperatura del ambiente", "No reacciona rápido", "Su presencia calma")
                    ),
                    WordItem(
                        "Jugador más impaciente",
                        listOf("El tiempo le pesa", "Le cuesta el 'luego'", "Quiere el final ya")
                    ),
                    WordItem(
                        "Jugador que más usa el móvil",
                        listOf("Siempre está en dos sitios a la vez", "Tiene reflejos de pantalla", "Nunca está del todo presente")
                    ),
                    WordItem(
                        "Jugador más bromista",
                        listOf("Encuentra huecos para rematar", "Tiene respuestas listas", "Le sale sin pensarlo")
                    ),
                    WordItem(
                        "Jugador más serio",
                        listOf("No regala reacciones", "Su cara no lo cuenta todo", "Va a lo funcional")
                    ),
                    WordItem(
                        "Jugador más miedoso",
                        listOf("Su cerebro imagina finales", "Prefiere lo predecible", "Evita lo raro")
                    ),
                    WordItem(
                        "Jugador más fan de la música",
                        listOf("Se le nota en el ritmo", "Tiene una banda sonora interna", "Reconoce cosas sin esfuerzo")
                    ),
                    WordItem(
                        "Jugador con mejor memoria",
                        listOf("Guarda detalles como si nada", "Conecta cosas antiguas", "Se acuerda cuando tú no")
                    ),
                    WordItem(
                        "Jugador más hablador",
                        listOf("Piensa hablando", "Un tema lleva a otro", "Su silencio dura poco")
                    ),
                    WordItem(
                        "Jugador más callado",
                        listOf("Habla cuando merece la pena", "Observa más de lo que parece", "Responde con lo justo")
                    ),
                    WordItem(
                        "Jugador más fashion (mejor vestido)",
                        listOf("Hay intención en lo que lleva", "Parece 'montado' sin esfuerzo", "Su imagen habla primero")
                    ),
                    WordItem(
                        "¿De los presentes quién es más probable que llegue tarde?",
                        listOf("Siempre se le cruza algo", "Su reloj va diferente", "Aparece cuando ya está en marcha")
                    )
                )

            )
        )
    }

    // Elimina un WordItem concreto de la lista items de una categoría (por id)
    fun deleteWordItem(categoryId: Long, itemToDelete: WordItem) {
        val current = _categories.value ?: return

        _categories.value = current.map { c ->
            if (c.id != categoryId) c
            else c.copy(items = c.items.filterNot { it == itemToDelete })
        }
    }

    // Devuelve true si la lista items de esa categoría está vacía
    fun itemsVacio(categoryId: Long): Boolean {
        return _categories.value!!.first { it.id == categoryId }.items.isEmpty()
    }

    // Restaura la lista items de esa categoría a los valores originales de initialCategories()
    fun restoreItems(categoryId: Long) {
        val current = _categories.value ?: return
        val initialItems = initialCategories().first { it.id == categoryId }.items

        _categories.value = current.map { c ->
            if (c.id != categoryId) c
            else c.copy(items = initialItems.map { it.copy() })
        }
    }

    // Alterna (marca/desmarca) isSelected de una categoría por id
    fun toggleSelection(categoryId: Long) {
        val current = _categories.value ?: return
        _categories.value = current.map { c ->
            if (c.id == categoryId) c.copy(isSelected = !c.isSelected) else c
        }
    }

    // Devuelve la lista de categorías actualmente seleccionadas (isSelected = true)
    fun getSelectedCategories(): List<Category> {
        return _categories.value?.filter { it.isSelected } ?: emptyList()
    }

    //Imprimimos por consola los items de una categoria
    fun logItems(categoryId: Long) {
        Log.d("Catego","------------------------------------------------------")
        val category = _categories.value?.firstOrNull { it.id == categoryId } ?: return

        Log.d("CategoryVM", "Category ${category.id} - ${category.title}:")
        category.items.forEachIndexed { i, item ->
            Log.d("CategoryVM", "Category ${category.id} item[$i] ${item.name} - ${item.hints}")
        }

    }

    fun setCategories(list: List<Category>) {
        _categories.value = list
    }

}
