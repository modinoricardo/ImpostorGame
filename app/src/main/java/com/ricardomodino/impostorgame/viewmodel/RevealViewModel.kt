package com.ricardomodino.impostorgame.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.ricardomodino.impostorgame.modelos.Category
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.WordItem

/**
 * ViewModel que preserva el estado completo del reveal entre cambios de configuración.
 *
 * Una vez que [initialized] es true, el estado de juego (roles, palabra, imágenes,
 * modoLocoActivo…) no vuelve a generarse aleatoriamente aunque la Activity se recree
 * por rotación u otro cambio de configuración.
 */
class RevealViewModel : ViewModel() {

    /** true en cuanto inicializarJuego() completa en el primer onCreate. */
    var initialized: Boolean = false

    // ── Datos de entrada (del Intent) ────────────────────────────────────────
    var listaJugadores: List<Jugador> = emptyList()
    var listaCategorias: List<Category> = emptyList()
    var opciones: GameOptions = GameOptions()

    // ── Estado inmutable generado por inicializarJuego ───────────────────────
    var indicesImpostores: Set<Int> = emptySet()
    var indicesSenoresBlancos: Set<Int> = emptySet()
    var nombresImpostores: List<String> = emptyList()
    var datosAsignados: Map<Int, DatoCurioso> = emptyMap()
    var palabra: String = ""
    var pista: String = ""
    var pistaMisteriosa: String = ""
    /** null en modo datos curiosos (nunca se inicializa en ese flujo). */
    var categoriaInGame: Category? = null
    /** null en modo datos curiosos. */
    var wordItemInGame: WordItem? = null
    var modoLocoActivo: Boolean = false
    var imagenPorJugador: Array<Bitmap?> = emptyArray()
    var imageResTurno: Bitmap? = null
}
