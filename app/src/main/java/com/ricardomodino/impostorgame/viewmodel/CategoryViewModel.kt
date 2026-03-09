package com.ricardomodino.impostorgame

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.WordItem

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
        return listOf(
            Category(
                id = 1L, title = "Animales", iconEmoji = "🦁",
                items = listOf(
                    WordItem("Perro", listOf("Mascota", "Ladra", "Paseo")),
                    WordItem("Gato", listOf("Felino", "Maúlla", "Bigotes")),
                    WordItem("Tiburón", listOf("Océano", "Aletas", "Depredador")),
                    WordItem("Águila", listOf("Altura", "Pico", "Alas")),
                    WordItem("Elefante", listOf("Trompa", "Colmillos", "Gigante")),
                    WordItem("León", listOf("Sabana", "Melena", "Ruge")),
                    WordItem("Delfín", listOf("Mar", "Inteligente", "Salta")),
                    WordItem("Lobo", listOf("Aullido", "Manada", "Bosque")),
                    WordItem("Jirafa", listOf("Cuello largo", "Manchas", "África")),
                    WordItem("Panda", listOf("Bambú", "Blanco y negro", "China")),
                    WordItem("Canguro", listOf("Salta", "Marsupial", "Australia")),
                    WordItem("Pingüino", listOf("Hielo", "Antártida")),
                    WordItem("Camaleón", listOf("Cambia de color", "Lengua larga", "Reptil")),
                    WordItem("Búho", listOf("Noche", "Ojos grandes", "Ulula")),
                    WordItem("Zorro", listOf("Astuto", "Cola", "Bosque")),
                    WordItem("Tigre", listOf("Rayas", "Selva", "Felino")),
                    WordItem("Hipopótamo", listOf("Río", "Grande", "Agresivo")),
                    WordItem("Koala", listOf("Eucalipto", "Marsupial", "Duerme mucho")),
                    WordItem("Rinoceronte", listOf("Cuerno", "Pesado", "Sabana")),
                    WordItem("Pulpo", listOf("Tentáculos", "Mar", "Inteligente")),
                    WordItem("Serpiente", listOf("Reptil", "Sin patas", "Veneno")),
                    WordItem("Oso polar", listOf("Ártico", "Blanco", "Carnívoro")),
                    WordItem("Mono", listOf("Árboles", "Banana", "Tarzán")),
                    WordItem("Pavo real", listOf("Plumas", "Cola", "Colorido")),
                    WordItem("Murciélago", listOf("Nocturno", "Vuela", "Cueva")),
                    WordItem("Flamenco", listOf("Rosa", "Una pata", "Laguna")),
                    WordItem("Tortuga", listOf("Lenta", "Caparazón", "Larga vida")),
                    WordItem("Cocodrilo", listOf("Río", "Dientes", "Reptil")),
                    WordItem("Ballena", listOf("Mar", "Canto", "Enorme")),
                    WordItem("Caballo", listOf("Galopa", "Crin", "Herradura")),
                    WordItem("Cerdo", listOf("Granja", "Rosa", "Barro")),
                    WordItem("Vaca", listOf("Leche", "Manchas", "Muge")),
                    WordItem("Oveja", listOf("Lana", "Balido", "Rebaño")),
                    WordItem("Gorila", listOf("Pecho", "Selva", "Fuerza")),
                    WordItem("Leopardo", listOf("Puntos", "Rápido", "Árbol")),
                    WordItem("Camello", listOf("Joroba", "Desierto", "Agua")),
                    WordItem("Cebra", listOf("Rayas", "África", "Caballo")),
                    WordItem("Ñu", listOf("Migración", "África", "Manada")),
                    WordItem("Piraña", listOf("Dientes", "Río", "Banco")),
                    WordItem("Medusa", listOf("Transparente", "Pica", "Mar"))
                )
            ),
            Category(
                id = 2L, title = "Objetos cotidianos", iconEmoji = "🏠",
                items = listOf(
                    WordItem("Taza", listOf("Café", "Líquido", "Cerámica")),
                    WordItem("Móvil", listOf("Pantalla", "Llamadas", "Apps")),
                    WordItem("Llaves", listOf("Cerradura", "Metal", "Bolsillo")),
                    WordItem("Mochila", listOf("Libros", "Espalda", "Cremallera")),
                    WordItem("Sofá", listOf("Siesta", "Salón", "Comodidad")),
                    WordItem("Lámpara", listOf("Bombilla", "Luz", "Mesa")),
                    WordItem("Reloj", listOf("Minutos", "Hora", "Muñeca")),
                    WordItem("Mesa", listOf("Madera", "Comer", "Patas")),
                    WordItem("Silla", listOf("Sentarse", "Respaldo", "Patas")),
                    WordItem("Cuchara", listOf("Sopa", "Metal", "Comer")),
                    WordItem("Tenedor", listOf("Pinchos", "Plato", "Cubierto")),
                    WordItem("Cuchillo", listOf("Cortar", "Filo", "Cocina")),
                    WordItem("Vaso", listOf("Agua", "Cristal", "Beber")),
                    WordItem("Botella", listOf("Tapón", "Líquido", "Plástico")),
                    WordItem("Televisión", listOf("Pantalla", "Canales", "Sofá")),
                    WordItem("Ordenador", listOf("Teclado", "Ratón", "Pantalla")),
                    WordItem("Portátil", listOf("Batería", "Pantalla", "Teclado")),
                    WordItem("Cargador", listOf("Energía", "Cable", "Enchufe")),
                    WordItem("Auriculares", listOf("Música", "Orejas", "Sonido")),
                    WordItem("Ventana", listOf("Cristal", "Luz", "Abrir")),
                    WordItem("Puerta", listOf("Abrir", "Cerrar", "Entrada")),
                    WordItem("Espejo", listOf("Reflejo", "Cara", "Cristal")),
                    WordItem("Cama", listOf("Dormir", "Colchón", "Sábana")),
                    WordItem("Almohada", listOf("Cabeza", "Dormir", "Blanda")),
                    WordItem("Mando", listOf("Botones", "Televisión", "Pilas")),
                    WordItem("Enchufe", listOf("Corriente", "Pared", "Energía")),
                    WordItem("Alfombra", listOf("Suelo", "Pisar", "Decoración")),
                    WordItem("Paraguas", listOf("Lluvia", "Abrir", "Mojarse")),
                    WordItem("Tijeras", listOf("Cortar", "Dos hojas", "Papel")),
                    WordItem("Bolígrafo", listOf("Escribir", "Tinta", "Capuchón")),
                    WordItem("Cuaderno", listOf("Hojas", "Escribir", "Espiral")),
                    WordItem("Nevera", listOf("Frío", "Comida", "Cocina")),
                    WordItem("Microondas", listOf("Calentar", "Girar", "Pitido")),
                    WordItem("Lavadora", listOf("Ropa", "Centrifugar", "Detergente")),
                    WordItem("Aspiradora", listOf("Polvo", "Ruido", "Suelo")),
                    WordItem("Plancha", listOf("Ropa", "Vapor", "Calor")),
                    WordItem("Escoba", listOf("Barrer", "Bruja", "Suelo")),
                    WordItem("Toalla", listOf("Secarse", "Baño", "Suave")),
                    WordItem("Jabón", listOf("Espuma", "Lavar", "Olor")),
                    WordItem("Cepillo de dientes", listOf("Pasta", "Mañana", "Noche"))
                )
            ),
            Category(
                id = 3L, title = "Personajes famosos", iconEmoji = "👤",
                items = listOf(
                    WordItem("Shakira", listOf("Caderas", "Colombia", "Waka Waka")),
                    WordItem("Messi", listOf("Balón", "Argentina", "Pulga")),
                    WordItem("Taylor Swift", listOf("Conciertos", "Rubia", "Eras Tour")),
                    WordItem("Leonardo DiCaprio", listOf("Óscar", "Titanic", "Actor")),
                    WordItem("Hitler", listOf("El que llega más alto con la mano", "Bigote", "Austria")),
                    WordItem("Cristiano Ronaldo", listOf("Siuu", "Portugal", "Goles")),
                    WordItem("Michael Jackson", listOf("Moonwalk", "Pop", "Guante")),
                    WordItem("Elon Musk", listOf("Tesla", "Cohetes", "Twitter")),
                    WordItem("Madonna", listOf("Reina del pop", "Conos", "Material Girl")),
                    WordItem("Freddie Mercury", listOf("Queen", "Bigote", "Bohemian Rhapsody")),
                    WordItem("Albert Einstein", listOf("Lengua", "Física", "Relatividad")),
                    WordItem("Napoleón", listOf("Pequeño", "Francia", "Exilio")),
                    WordItem("Cleopatra", listOf("Egipto", "Serpiente", "Reina")),
                    WordItem("Steve Jobs", listOf("Manzana", "Negro", "iPhone")),
                    WordItem("Bill Gates", listOf("Windows", "Vacunas", "Multimillonario")),
                    WordItem("Beyoncé", listOf("Lemonade", "Diva", "Jay-Z")),
                    WordItem("Eminem", listOf("Detroit", "Rap", "8 Mile")),
                    WordItem("Pablo Picasso", listOf("Cubismo", "Pintor", "España")),
                    WordItem("Walt Disney", listOf("Mickey", "Cuentos", "Parques")),
                    WordItem("Bruce Lee", listOf("Artes marciales", "Rápido", "Hong Kong")),
                    WordItem("Elvis Presley", listOf("Rock and roll", "Pelvis", "Vegas")),
                    WordItem("Marilyn Monroe", listOf("Vestido", "Rubia", "Kennedy")),
                    WordItem("Charles Darwin", listOf("Evolución", "Tortugas", "Barco")),
                    WordItem("Stephen Hawking", listOf("Silla de ruedas", "Universo", "Agujeros negros")),
                    WordItem("Rihanna", listOf("Barbados", "Paraguas", "Fenty")),
                    WordItem("Adele", listOf("Hello", "Voz", "Londres")),
                    WordItem("Jeff Bezos", listOf("Amazon", "Calvo", "Paquetes")),
                    WordItem("Mark Zuckerberg", listOf("Facebook", "Robot", "Datos")),
                    WordItem("Lady Gaga", listOf("Carne", "Extravagante", "Poker Face")),
                    WordItem("Drake", listOf("Toronto", "Rap", "Champagne Papi")),
                    WordItem("Kanye West", listOf("Ego", "Kim", "Yeezy")),
                    WordItem("Roger Federer", listOf("Tenis", "Elegancia", "Suiza")),
                    WordItem("Usain Bolt", listOf("Rayo", "Jamaica", "100 metros")),
                    WordItem("Muhammad Ali", listOf("Mariposa", "Boxeo", "Flota")),
                    WordItem("Michael Jordan", listOf("Jumpman", "Bulls", "Anillos")),
                    WordItem("Leonardo da Vinci", listOf("Mona Lisa", "Inventos", "Italia")),
                    WordItem("Shakespeare", listOf("Romeo", "Teatro", "Inglaterra")),
                    WordItem("Mozart", listOf("Niño prodigio", "Piano", "Austria")),
                    WordItem("Beethoven", listOf("Sordo", "Sinfonía", "Alemania")),
                    WordItem("Nelson Mandela", listOf("Prisión", "Sudáfrica", "Libertad")),
                    WordItem("Gandhi", listOf("Paz", "India", "Sábana")),
                    WordItem("Martin Luther King", listOf("Sueño", "Discurso", "Derechos")),
                    WordItem("Barack Obama", listOf("Yes we can", "USA", "Nobel")),
                    WordItem("Penélope Cruz", listOf("España", "Óscar", "Actriz")),
                    WordItem("Pedro Almodóvar", listOf("Manchuela", "Colores", "Director")),
                    WordItem("Rafael Nadal", listOf("Tenis", "Mallorca", "Arcilla")),
                    WordItem("Lewis Hamilton", listOf("F1", "Negro", "Mercedes")),
                    WordItem("Serena Williams", listOf("Tenis", "Fuerza", "USA")),
                    WordItem("Kobe Bryant", listOf("Mamba", "Lakers", "24")),
                    WordItem("Tom Hanks", listOf("Forrest", "Náufrago", "Simpático")),
                    WordItem("Brad Pitt", listOf("Guapo", "Fight Club", "Actor")),
                    WordItem("Angelina Jolie", listOf("Labios", "Hijos", "Lara Croft")),
                    WordItem("Johnny Depp", listOf("Pirata", "Sombrero", "Burton")),
                    WordItem("Scarlett Johansson", listOf("Viuda Negra", "Rubia", "Marvel")),
                    WordItem("Robert Downey Jr", listOf("Iron Man", "Sarcasmo", "Marvel")),
                    WordItem("Dwayne Johnson", listOf("La Roca", "Músculos", "WWE")),
                    WordItem("Will Smith", listOf("Bofetón", "Men in Black", "Rapero")),
                    WordItem("Jim Carrey", listOf("Caras", "Comedia", "Máscara")),
                    WordItem("Keanu Reeves", listOf("Matrix", "Moto", "Neo")),
                    WordItem("Morgan Freeman", listOf("Voz", "Dios", "Narrador")),
                    WordItem("Meryl Streep", listOf("Diablo viste de Prada", "Óscar", "Actriz")),
                    WordItem("Audrey Hepburn", listOf("Elegancia", "Desayuno con diamantes", "Clásica")),
                    WordItem("Pablo Escobar", listOf("Colombia", "Plata o plomo", "Narco")),
                    WordItem("Che Guevara", listOf("Boina", "Revolución", "Cuba")),
                    WordItem("Marco Polo", listOf("China", "Viajes", "Venecia")),
                    WordItem("Alejandro Magno", listOf("Conquista", "Caballo", "Macedonia")),
                    WordItem("Julio César", listOf("Et tu Brute", "Roma", "Togas")),
                    WordItem("Isaac Newton", listOf("Manzana", "Gravedad", "Física")),
                    WordItem("Nikola Tesla", listOf("Electricidad", "Corriente", "Inventor")),
                    WordItem("Salvador Dalí", listOf("Bigote", "Relojes", "Surrealismo")),
                    WordItem("Vincent van Gogh", listOf("Oreja", "Girasoles", "Pintor")),
                    WordItem("Frida Kahlo", listOf("Ceja", "México", "Autorretratos")),
                    WordItem("Bruce Willis", listOf("Calvo", "Die Hard", "Acción")),
                    WordItem("Arnold Schwarzenegger", listOf("Terminator", "Músculo", "Gobernador")),
                    WordItem("Sylvester Stallone", listOf("Rocky", "Puños", "Rambo")),
                    WordItem("Robin Williams", listOf("Comedia", "Genio", "Mrs. Doubtfire")),
                    WordItem("Charlie Chaplin", listOf("Bigote", "Mudo", "Bombín")),
                    WordItem("Oprah Winfrey", listOf("Coche", "Entrevistas", "USA")),
                    WordItem("Ellen DeGeneres", listOf("Bailar", "Presentadora", "Simpática")),
                    WordItem("Gordon Ramsay", listOf("Cocina", "Grita", "Chef")),
                    WordItem("David Beckham", listOf("Fútbol", "Tatuajes", "Victoria")),
                    WordItem("Ronaldinho", listOf("Sonrisa", "Brasil", "Goles")),
                    WordItem("Zinedine Zidane", listOf("Cabezazo", "Francia", "Balón de Oro")),
                    WordItem("Pelé", listOf("Brasil", "Goles", "Rey")),
                    WordItem("Maradona", listOf("Mano de Dios", "Argentina", "Crack")),
                    WordItem("Tiger Woods", listOf("Golf", "Verde", "Escándalo")),
                    WordItem("Mike Tyson", listOf("Oreja", "Boxeo", "Tatuaje")),
                    WordItem("Conor McGregor", listOf("Whisky", "MMA", "Irlandés")),
                    WordItem("LeBron James", listOf("NBA", "Calvo", "Rey")),
                    WordItem("Neymar", listOf("Brasil", "Regate", "Llora")),
                    WordItem("Ibai Llanos", listOf("Twitch", "Bilbao", "Porcino")),
                    WordItem("Ibrahimović", listOf("Ego", "Acrobático", "Suecia")),
                    WordItem("Alejandro Sanz", listOf("España", "Balada", "Voz")),
                    WordItem("Enrique Iglesias", listOf("España", "Lunar", "Héroe")),
                    WordItem("Rosalía", listOf("Flamenco", "Barcelona", "Motomami")),
                    WordItem("Bad Bunny", listOf("Puerto Rico", "Reggaeton", "Conejo")),
                    WordItem("J Balvin", listOf("Colombia", "Colores", "Reggaeton")),
                    WordItem("Daddy Yankee", listOf("Gasolina", "Puerto Rico", "Reggaeton")),
                    WordItem("Celia Cruz", listOf("Azúcar", "Cuba", "Salsa")),
                    WordItem("Marc Anthony", listOf("Salsa", "Jennifer Lopez", "Nueva York")),
                    WordItem("Placido Domingo", listOf("Ópera", "España", "Tenor")),
                    WordItem("Paco de Lucía", listOf("Guitarra", "Flamenco", "España"))
                )
            ),
            Category(
                id = 4L, title = "Superhéroes", iconEmoji = "🦸‍♂️",
                items = listOf(
                    WordItem("Superman", listOf("Capa", "Volar", "Fuerza")),
                    WordItem("Batman", listOf("Murciélago", "Noche", "Detective")),
                    WordItem("Spiderman", listOf("Telarañas", "Trepar", "Arácnido")),
                    WordItem("Iron Man", listOf("Armadura", "Tecnología", "Vuelo")),
                    WordItem("Wonder Woman", listOf("Lazo", "Amazona", "Fuerza")),
                    WordItem("Hulk", listOf("Verde", "Fuerza", "Ira")),
                    WordItem("Thor", listOf("Martillo", "Trueno", "Dios")),
                    WordItem("Capitán América", listOf("Escudo", "Líder", "Soldado")),
                    WordItem("Flash", listOf("Velocidad", "Rápido", "Tiempo")),
                    WordItem("Aquaman", listOf("Mar", "Tridente", "Océano")),
                    WordItem("Black Widow", listOf("Espía", "Combate", "Sigilo")),
                    WordItem("Doctor Strange", listOf("Magia", "Hechizos", "Tiempo")),
                    WordItem("Pantera Negra", listOf("Wakanda", "Traje", "Rey")),
                    WordItem("Ant Man", listOf("Pequeño", "Grande", "Hormiga")),
                    WordItem("Visión", listOf("Mente", "Gema", "Android")),
                    WordItem("Scarlet Witch", listOf("Magia", "Caos", "Poder")),
                    WordItem("Green Lantern", listOf("Anillo", "Voluntad", "Energía")),
                    WordItem("Deadpool", listOf("Humor", "Regeneración", "Katana")),
                    WordItem("Wolverine", listOf("Garras", "Metal", "Regeneración")),
                    WordItem("Capitana Marvel", listOf("Energía", "Volar", "Espacio")),
                    WordItem("Daredevil", listOf("Ciego", "Bastón", "Abogado")),
                    WordItem("Hawkeye", listOf("Arco", "Flecha", "Puntería")),
                    WordItem("Black Panther", listOf("Garras", "Vibranio", "África")),
                    WordItem("Star Lord", listOf("Walkman", "Espacio", "Mix")),
                    WordItem("Gamora", listOf("Verde", "Espada", "Thanos")),
                    WordItem("Rocket", listOf("Mapache", "Armas", "Guardianes")),
                    WordItem("Groot", listOf("Árbol", "I am Groot", "Guardianes")),
                    WordItem("Thanos", listOf("Guantelete", "Chasquido", "Morado")),
                    WordItem("Loki", listOf("Engaño", "Asgard", "Cuernos")),
                    WordItem("Venom", listOf("Lengua", "Negro", "Simbionte"))
                )
            ),
            Category(
                id = 5L, title = "Comidas", iconEmoji = "🍕",
                items = listOf(
                    WordItem("Lasaña", listOf("Capas", "Pasta", "Horno")),
                    WordItem("Croqueta", listOf("Rellena", "Frita", "Bechamel")),
                    WordItem("Gazpacho", listOf("Tomate", "Frío", "Verano")),
                    WordItem("Manzana", listOf("Roja", "Fruta", "Morder")),
                    WordItem("Espaguetis", listOf("Tenedor", "Pasta", "Salsa")),
                    WordItem("Hamburguesa", listOf("Carne", "Pan", "Queso")),
                    WordItem("Tortilla", listOf("Huevos", "Patata", "Sartén")),
                    WordItem("Pizza", listOf("Queso", "Masa", "Horno")),
                    WordItem("Paella", listOf("Arroz", "Sartén", "Marisco")),
                    WordItem("Ensalada", listOf("Lechuga", "Tomate", "Aliño")),
                    WordItem("Sopa", listOf("Caldo", "Cuchara", "Caliente")),
                    WordItem("Sandwich", listOf("Pan", "Relleno", "Lonchas")),
                    WordItem("Helado", listOf("Frío", "Dulce", "Cono")),
                    WordItem("Chocolate", listOf("Dulce", "Cacao", "Tableta")),
                    WordItem("Queso", listOf("Leche", "Curado", "Lonchas")),
                    WordItem("Yogur", listOf("Cuchara", "Frío", "Lácteo")),
                    WordItem("Pan", listOf("Harina", "Horno", "Miga")),
                    WordItem("Arroz", listOf("Grano", "Blanco", "Cocer")),
                    WordItem("Pollo", listOf("Carne", "Asado", "Pechuga")),
                    WordItem("Pescado", listOf("Mar", "Espinas", "Plancha")),
                    WordItem("Sushi", listOf("Arroz", "Pescado", "Palillos")),
                    WordItem("Burrito", listOf("Tortilla", "Relleno", "México")),
                    WordItem("Kebab", listOf("Pan", "Carne", "Salsa")),
                    WordItem("Empanada", listOf("Masa", "Relleno", "Horno")),
                    WordItem("Churros", listOf("Fritos", "Azúcar", "Chocolate")),
                    WordItem("Galletas", listOf("Dulce", "Horno", "Crujiente")),
                    WordItem("Cereales", listOf("Leche", "Desayuno", "Bol")),
                    WordItem("Café", listOf("Taza", "Caliente", "Energía")),
                    WordItem("Croissant", listOf("Mantequilla", "Hojaldrado", "Desayuno")),
                    WordItem("Donut", listOf("Agujero", "Glaseado", "Dulce")),
                    WordItem("Brownie", listOf("Chocolate", "Nueces", "Horno")),
                    WordItem("Tarta", listOf("Cumpleaños", "Velas", "Dulce")),
                    WordItem("Flan", listOf("Huevo", "Caramelo", "Tembla")),
                    WordItem("Natillas", listOf("Crema", "Galleta", "Dulce")),
                    WordItem("Arroz con leche", listOf("Canela", "Dulce", "Postre")),
                    WordItem("Fabada", listOf("Asturiana", "Alubias", "Chorizo")),
                    WordItem("Cocido madrileño", listOf("Garbanzos", "Madrid", "Caldo")),
                    WordItem("Pulpo a la gallega", listOf("Pimentón", "Galicia", "Tentáculos")),
                    WordItem("Crêpe", listOf("Fino", "Relleno", "Francia")),
                    WordItem("Waffles", listOf("Cuadrícula", "Mantequilla", "Sirope")),
                    WordItem("Tostada", listOf("Desayuno", "Pan", "Crujiente")),
                    WordItem("Macarrones", listOf("Pasta", "Salsa", "Horno")),
                    WordItem("Tarta de queso", listOf("Postre", "Cremosa", "Horno")),
                    WordItem("Tacos", listOf("México", "Tortilla", "Relleno")),
                    WordItem("Arepa", listOf("Maíz", "Rellena", "Venezuela")),
                    WordItem("Hot dog", listOf("Pan", "Salchicha", "Mostaza")),
                    WordItem("Empanadillas", listOf("Relleno", "Fritas", "Masa")),
                    WordItem("Patatas fritas", listOf("Crujientes", "Aceite", "Sal")),
                    WordItem("Hummus", listOf("Garbanzos", "Crema", "Untar")),
                    WordItem("Tiramisú", listOf("Café", "Cacao", "Postre")),
                    WordItem("Fajitas", listOf("Tortilla", "Pollo", "Verduras")),
                    WordItem("Nuggets", listOf("Pollo", "Crujientes", "Fritos")),
                    WordItem("Salmorejo", listOf("Tomate", "Pan", "Frío")),
                    WordItem("Puré de patata", listOf("Suave", "Acompañamiento", "Crema")),
                    WordItem("Calamares", listOf("Anillas", "Fritos", "Mar")),
                    WordItem("Tortitas", listOf("Desayuno", "Dulce", "Sirope")),
                    WordItem("Muffin", listOf("Dulce", "Horno", "Esponjoso")),
                    WordItem("Batido", listOf("Frío", "Líquido", "Dulce")),
                    WordItem("Canelones", listOf("Pasta", "Relleno", "Horno")),
                    WordItem("Pisto", listOf("Verduras", "Sartén", "España")),
                    WordItem("Merluza", listOf("Pescado", "Blanco", "Plancha")),
                    WordItem("Tortellini", listOf("Pasta", "Rellenos", "Italia")),
                    WordItem("Magdalena", listOf("Dulce", "Esponjosa", "Desayuno")),
                    WordItem("Bizcocho", listOf("Horno", "Esponjoso", "Dulce")),
                    WordItem("Alitas de pollo", listOf("Fritas", "Salsa", "Crujientes")),
                    WordItem("Patatas bravas", listOf("Salsa", "Tapa", "España")),
                    WordItem("Crema de verduras", listOf("Caliente", "Suave", "Cuchara")),
                    WordItem("Tallarines", listOf("Pasta", "Largos", "Salsa")),
                    WordItem("Melón", listOf("Fruta", "Dulce", "Verano")),
                    WordItem("Torrijas", listOf("Pan", "Dulce", "Semana Santa"))

                )
            ),
            Category(
                id = 6L, title = "Colores", iconEmoji = "🪅",
                items = listOf(
                    WordItem("Rojo", listOf("Sangre", "Fuego", "Pasión")),
                    WordItem("Verde", listOf("Esperanza", "Hierba", "Naturaleza")),
                    WordItem("Naranja", listOf("Mandarina", "Atardecer", "Fruta")),
                    WordItem("Azul", listOf("Cielo", "Mar", "Frío")),
                    WordItem("Blanco", listOf("Nieve", "Luz", "Puro")),
                    WordItem("Negro", listOf("Carbón", "Noche", "Sombra")),
                    WordItem("Gris", listOf("Ceniza", "Nublado", "Metal")),
                    WordItem("Amarillo", listOf("Sol", "Limón", "Brillante")),
                    WordItem("Rosa", listOf("Flor", "Romántico", "Chicle")),
                    WordItem("Morado", listOf("Uva", "Misterio", "Noche")),
                    WordItem("Violeta", listOf("Flor", "Intenso", "Tono")),
                    WordItem("Marrón", listOf("Tierra", "Madera", "Chocolate")),
                    WordItem("Beige", listOf("Arena", "Suave", "Neutral")),
                    WordItem("Turquesa", listOf("Mar", "Gema", "Playa")),
                    WordItem("Cian", listOf("Tinta", "Azulado", "Agua")),
                    WordItem("Magenta", listOf("Impresión", "Fuerte", "Tinta")),
                    WordItem("Fucsia", listOf("Vivo", "Rosa", "Llamativo")),
                    WordItem("Lila", listOf("Suave", "Flor", "Morado")),
                    WordItem("Coral", listOf("Mar", "Rosado", "Arrecife")),
                    WordItem("Salmón", listOf("Pescado", "Rosado", "Tono")),
                    WordItem("Ocre", listOf("Tierra", "Amarillo", "Antiguo")),
                    WordItem("Oliva", listOf("Verde", "Aceite", "Militar")),
                    WordItem("Esmeralda", listOf("Gema", "Verde", "Brillo")),
                    WordItem("Azul marino", listOf("Oscuro", "Mar", "Uniforme")),
                    WordItem("Celeste", listOf("Claro", "Cielo", "Suave")),
                    WordItem("Granate", listOf("Vino", "Oscuro", "Rojo")),
                    WordItem("Plateado", listOf("Metal", "Brillo", "Gris")),
                    WordItem("Dorado", listOf("Oro", "Lujo", "Brillo"))
                )
            ),
            Category(
                id = 7L, title = "Random", iconEmoji = "🙃",
                items = listOf(
                    WordItem("Jugador más cercano (distancia física)", listOf("Está en tu burbuja", "No tienes que proyectar la voz", "Con un gesto basta")),
                    WordItem("Jugador más mayor (edad más alta)", listOf("Tiene referencias que no todos pillan", "Ha visto cosas repetirse", "Se nota en cómo lo cuenta")),
                    WordItem("Jugador más alto (mayor estatura)", listOf("Cambia la perspectiva del grupo", "En fotos tu ojo va ahí", "No pasa desapercibido de pie")),
                    WordItem("Jugador más joven (edad más baja)", listOf("Tiene primera vez en muchas cosas", "Le sobra energía a ciertas horas", "Ciertas frases le suenan normales")),
                    WordItem("Jugador con el pelo más largo", listOf("Tiene rutina sin decirlo", "A veces estorba y a veces ayuda", "En movimiento se nota más")),
                    WordItem("Jugador con la voz más grave", listOf("Su tono llena el espacio", "Da sensación de radio", "Con poco volumen llega igual")),
                    WordItem("Jugador con la voz más aguda", listOf("Destaca sin querer", "Cambia mucho según el momento", "En emoción se delata")),
                    WordItem("Jugador más puntual", listOf("Para él a tiempo no existe", "Prefiere esperar que llegar justo", "Le incomoda improvisar horarios")),
                    WordItem("Jugador más despistado", listOf("Va en automático a ratos", "Se le escapan detalles obvios", "Vive en su propio hilo")),
                    WordItem("Jugador más ordenado", listOf("Se nota en lo pequeño", "Le molesta el desorden ajeno", "Tiene su manera de hacerlo todo")),
                    WordItem("Jugador más dormilón", listOf("Se apaga fácil", "El descanso le llama", "Siempre podría ser un ratito más")),
                    WordItem("Jugador más deportista", listOf("Su cuerpo habla por él", "Tiene hábitos que se notan", "El movimiento es su idioma")),
                    WordItem("Jugador más competitivo", listOf("Se le encienden los ojos", "No se lo toma solo por jugar", "Necesita saber quién gana")),
                    WordItem("Jugador más tranquilo", listOf("Baja la temperatura del ambiente", "No reacciona rápido", "Su presencia calma")),
                    WordItem("Jugador más impaciente", listOf("El tiempo le pesa", "Le cuesta el luego", "Quiere el final ya")),
                    WordItem("Jugador que más usa el móvil", listOf("Siempre está en dos sitios a la vez", "Tiene reflejos de pantalla", "Nunca está del todo presente")),
                    WordItem("Jugador más bromista", listOf("Encuentra huecos para rematar", "Tiene respuestas listas", "Le sale sin pensarlo")),
                    WordItem("Jugador más serio", listOf("No regala reacciones", "Su cara no lo cuenta todo", "Va a lo funcional")),
                    WordItem("Jugador más miedoso", listOf("Su cerebro imagina finales", "Prefiere lo predecible", "Evita lo raro")),
                    WordItem("Jugador más fan de la música", listOf("Se le nota en el ritmo", "Tiene una banda sonora interna", "Reconoce cosas sin esfuerzo")),
                    WordItem("Jugador con mejor memoria", listOf("Guarda detalles como si nada", "Conecta cosas antiguas", "Se acuerda cuando tú no")),
                    WordItem("Jugador más hablador", listOf("Piensa hablando", "Un tema lleva a otro", "Su silencio dura poco")),
                    WordItem("Jugador más callado", listOf("Habla cuando merece la pena", "Observa más de lo que parece", "Responde con lo justo")),
                    WordItem("Jugador más fashion", listOf("Hay intención en lo que lleva", "Parece montado sin esfuerzo", "Su imagen habla primero")),
                    WordItem("¿Quién tiene más probabilidad de llegar tarde?", listOf("Siempre se le cruza algo", "Su reloj va diferente", "Aparece cuando ya está en marcha")),
                    WordItem("¿Quién se reiría primero si nadie puede reír?", listOf("No puede con la presión", "Le entra la risa floja", "Basta una mirada")),
                    WordItem("¿Quién mentiría mejor en una mentira piadosa?", listOf("Tiene cara de póker", "Convence sin esfuerzo", "Lo suaviza todo")),
                    WordItem("¿Quién sobreviviría más tiempo sin móvil?", listOf("Tiene vida más allá", "No depende de la pantalla", "Le cuesta menos desconectar")),
                    WordItem("¿Quién organizaría mejor una escapada?", listOf("Piensa en los detalles", "Ya tiene el plan en la cabeza", "Le gusta controlar el itinerario")),
                    WordItem("¿Quién sería el primero en rendirse en un reto físico?", listOf("El cuerpo manda", "No es lo suyo", "Busca la salida antes")),
                    WordItem("¿Quién cantaría en el karaoke sin que le pidan?", listOf("El micro le llama", "No necesita excusa", "Le da igual el nivel"))
                )
            ),
            Category(
                id = 8L, title = "Deportes", iconEmoji = "🎾",
                items = listOf(
                    WordItem("Waterpolo", listOf("Agua", "Pelota", "Bañador")),
                    WordItem("Cricket", listOf("Bate", "Pelota", "Inglés")),
                    WordItem("Golf", listOf("Hoyo", "Césped", "Palos")),
                    WordItem("Baloncesto", listOf("Alto", "Aro", "Balón")),
                    WordItem("Fútbol", listOf("Estadio", "Balón", "Gol")),
                    WordItem("Tenis", listOf("Raqueta", "Red", "Set")),
                    WordItem("Fórmula 1", listOf("Rueda", "Velocidad", "Bandera")),
                    WordItem("Moto GP", listOf("Casco", "Curva", "Asfalto")),
                    WordItem("Natación", listOf("Piscina", "Braza", "Gafas")),
                    WordItem("Atletismo", listOf("Pista", "Velocidad", "Salto")),
                    WordItem("Ciclismo", listOf("Bicicleta", "Maillot", "Montaña")),
                    WordItem("Boxeo", listOf("Guantes", "Ring", "Nocaut")),
                    WordItem("Rugby", listOf("Oval", "Melé", "Placaje")),
                    WordItem("Béisbol", listOf("Bate", "Guante", "Home run")),
                    WordItem("Hockey sobre hielo", listOf("Puck", "Patines", "Palo")),
                    WordItem("Esquí", listOf("Nieve", "Cuesta", "Bastones")),
                    WordItem("Surf", listOf("Ola", "Tabla", "Playa")),
                    WordItem("Escalada", listOf("Roca", "Cuerda", "Pies")),
                    WordItem("Esgrima", listOf("Espada", "Máscara", "Touché")),
                    WordItem("Judo", listOf("Tatami", "Kimono", "Ippon")),
                    WordItem("Kárate", listOf("Cinturón", "Kata", "Kiai")),
                    WordItem("Balonmano", listOf("Portería", "Salto", "Siete metros")),
                    WordItem("Voleibol", listOf("Red", "Saque", "Remate")),
                    WordItem("Padel", listOf("Pared", "Pista", "Cristal")),
                    WordItem("Ping pong", listOf("Mesa", "Pelotita", "Paleta")),
                    WordItem("Dardos", listOf("Diana", "Punta", "Pub")),
                    WordItem("Billar", listOf("Taco", "Bola", "Mesa verde")),
                    WordItem("Ajedrez", listOf("Tablero", "Rey", "Jaque mate")),
                    WordItem("Lucha libre", listOf("Máscara", "Ring", "Llave"))
                )
            )
        )
    }

    fun deleteWordItem(categoryId: Long, itemToDelete: WordItem) {
        val current = _categories.value ?: return
        _categories.value = current.map { c ->
            if (c.id != categoryId) c
            else c.copy(items = c.items.filterNot { it == itemToDelete })
        }
    }

    fun itemsVacio(categoryId: Long): Boolean =
        _categories.value!!.first { it.id == categoryId }.items.isEmpty()

    fun restoreItems(categoryId: Long) {
        val current = _categories.value ?: return
        val initialItems = initialCategories().first { it.id == categoryId }.items
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