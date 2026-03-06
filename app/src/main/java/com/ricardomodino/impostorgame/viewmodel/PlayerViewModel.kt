package com.ricardomodino.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador
import kotlin.random.Random

class PlayerViewModel : ViewModel() {

    private val _players = MutableLiveData<List<Jugador>>(
        listOf(
            Jugador("Jugador 1", 0),
            Jugador("Jugador 2", 0),
            Jugador("Jugador 3", 0)
        )
    )
    val players: LiveData<List<Jugador>> = _players

    // Índice del último jugador que empezó una partida (para no repetir)
    private var ultimoIndiceQueEmpezó: Int = -1

    fun addPlayer(name: String) {
        val current = _players.value ?: emptyList()
        _players.value = current + Jugador(nombre = name, vecesImpostor = 0)
    }

    fun removeAt(index: Int): Boolean {
        val current = _players.value?.toMutableList() ?: return false
        if (current.size <= 3) return false
        if (index in current.indices) {
            current.removeAt(index)
            _players.value = current
            return true
        }
        return false
    }

    fun renameAt(index: Int, newName: String) {
        val current = _players.value?.toMutableList() ?: return
        if (index in current.indices) {
            val old = current[index]
            current[index] = old.copy(nombre = newName)
            _players.value = current
        }
    }

    fun updatePlayers(newList: List<Jugador>) {
        _players.value = newList
    }

    fun getPlayerCount(): Int = _players.value?.size ?: 0

    fun incrementImpostorByName(name: String) {
        val current = _players.value ?: return
        _players.value = current.map {
            if (it.nombre == name) it.copy(vecesImpostor = it.vecesImpostor + 1) else it
        }
    }

    // Elige un jugador que empiece la ronda, sin repetir el de la ronda anterior
    fun pickJugadorQueEmpieza(jugadores: List<Jugador>): Jugador? {
        if (jugadores.isEmpty()) return null
        if (jugadores.size == 1) return jugadores[0]

        val candidatos = jugadores.indices
            .filter { it != ultimoIndiceQueEmpezó }
            .map { jugadores[it] }

        val elegido = candidatos.random()
        ultimoIndiceQueEmpezó = jugadores.indexOfFirst { it.nombre == elegido.nombre }
        return elegido
    }

    // Elige UN impostor (compatibilidad hacia atrás)
    fun pickImpostorIndex(players: List<Jugador>): Int =
        pickImpostorIndices(players, 1).firstOrNull() ?: 0

    // Elige N impostores ponderados por vecesImpostor, solo entre jugadores NORMAL
    // Excluye además los índices pasados en 'excluir' (señores blancos ya asignados)
    fun pickImpostorIndices(
        players: List<Jugador>,
        numImpostores: Int,
        excluir: Set<Int> = emptySet()
    ): Set<Int> {
        val elegibles = players.indices.filter {
            players[it].tipo == TipoJugador.NORMAL && it !in excluir
        }
        if (elegibles.isEmpty()) return emptySet()

        val resultado = mutableSetOf<Int>()
        val disponibles = elegibles.toMutableList()
        val n = numImpostores.coerceAtMost(disponibles.size)

        repeat(n) {
            val weights = disponibles.map { 1.0 / (1 + players[it].vecesImpostor) }
            val total = weights.sum()
            val r = Random.nextDouble() * total
            var acc = 0.0
            for (i in disponibles.indices) {
                acc += weights[i]
                if (r < acc) {  // fix: < en vez de <=
                    resultado.add(disponibles[i])
                    disponibles.removeAt(i)
                    return@repeat
                }
            }
            // fallback por precisión flotante
            resultado.add(disponibles.last())
            disponibles.removeAt(disponibles.lastIndex)
        }

        return resultado
    }
}