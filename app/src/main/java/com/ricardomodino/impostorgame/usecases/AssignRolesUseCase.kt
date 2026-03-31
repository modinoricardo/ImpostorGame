package com.ricardomodino.impostorgame.usecases

import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador

/**
 * Resultado de la asignación de roles para una partida.
 */
data class RoleAssignmentResult(
    val jugadores: List<Jugador>,            // lista con TipoJugador.SENOR_BLANCO marcados
    val indicesImpostores: Set<Int>,
    val indicesSenoresBlancos: Set<Int>,
    val nombresImpostores: List<String>
)

/**
 * Caso de uso puro (sin dependencias de Android) que asigna roles a los jugadores.
 *
 * Orden de asignación:
 *  1. Resetear todos a NORMAL
 *  2. Elegir Señores Blancos (solo modo misterioso)
 *  3. Elegir Impostores excluyendo a los Señores Blancos
 *  4. Calcular índices finales y nombres
 *
 * Recibe [pickImpostorIndices] como función inyectada para mantener la lógica
 * de aleatoriedad ponderada en [PlayerViewModel] sin acoplar este use case al ViewModel.
 */
class AssignRolesUseCase {

    fun execute(
        jugadores: List<Jugador>,
        opciones: GameOptions,
        pickImpostorIndices: (jugadores: List<Jugador>, num: Int, excluir: Set<Int>) -> Set<Int>
    ): RoleAssignmentResult {

        // 1. Resetear todos los roles a NORMAL
        var lista = jugadores.map { it.copy(tipo = TipoJugador.NORMAL) }

        // 2. Asignar Señores Blancos (solo en modo misterioso)
        if (opciones.modoMisterioso) {
            lista = asignarSenoresBlancos(lista, opciones)
        }

        // 3. Elegir impostores excluyendo los Señores Blancos ya asignados
        val indicesBlancos = lista.indices.filter { lista[it].tipo == TipoJugador.SENOR_BLANCO }.toSet()
        val indicesImpostores = pickImpostorIndices(lista, opciones.numImpostores, indicesBlancos)

        // 4. Índices finales de Señores Blancos y nombres de impostores
        val indicesSenoresBlancos = lista.indices
            .filter { lista[it].tipo == TipoJugador.SENOR_BLANCO }.toSet()
        val nombresImpostores = indicesImpostores.map { lista[it].nombre }

        return RoleAssignmentResult(lista, indicesImpostores, indicesSenoresBlancos, nombresImpostores)
    }

    private fun asignarSenoresBlancos(jugadores: List<Jugador>, opts: GameOptions): List<Jugador> {
        val elegibles = jugadores.indices.toList()
        val num = opts.numSenoresBlancos.coerceIn(0, (elegibles.size - 1).coerceAtLeast(0))
        val indicesBlancos = elegibles.shuffled().take(num).toSet()
        return jugadores.mapIndexed { i, j ->
            j.copy(tipo = if (i in indicesBlancos) TipoJugador.SENOR_BLANCO else TipoJugador.NORMAL)
        }
    }
}
