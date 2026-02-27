package com.example.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.impostorgame.modelos.Jugador
import com.example.impostorgame.modelos.TipoJugador
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

    // Elige UN impostor (compatibilidad hacia atrás)
    fun pickImpostorIndex(players: List<Jugador>): Int =
        pickImpostorIndices(players, 1).firstOrNull() ?: 0

    // Elige N impostores ponderados por vecesImpostor, solo entre jugadores NORMAL
    fun pickImpostorIndices(players: List<Jugador>, numImpostores: Int): Set<Int> {
        val elegibles = players.indices.filter { players[it].tipo == TipoJugador.NORMAL }
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
                if (r <= acc) {
                    resultado.add(disponibles[i])
                    disponibles.removeAt(i)
                    return@repeat
                }
            }
            // fallback
            resultado.add(disponibles.last())
            disponibles.removeAt(disponibles.lastIndex)
        }

        return resultado
    }
}