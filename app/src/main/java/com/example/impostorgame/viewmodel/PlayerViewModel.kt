package com.example.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.impostorgame.modelos.Jugador
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

    // Añadir jugador (al final de la lista)
    fun addPlayer(name: String) {
        val current = _players.value ?: emptyList()
        _players.value = current + Jugador(nombre = name, vecesImpostor = 0)
    }

    // Eliminar jugador por posición
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

    // Renombrar un jugador concreto, por su posición (mantiene vecesImpostor)
    fun renameAt(index: Int, newName: String) {
        val current = _players.value?.toMutableList() ?: return
        if (index in current.indices) {
            val old = current[index]
            current[index] = old.copy(nombre = newName)
            _players.value = current
        }
    }

    // Sobrescribir la lista entera
    fun updatePlayers(newList: List<Jugador>) {
        _players.value = newList
    }

    fun getPlayerCount(): Int {
        return _players.value?.size ?: 0
    }

    // Extra: sumar 1 cuando salga impostor (por nombre)
    fun incrementImpostorByName(name: String) {
        val current = _players.value ?: return
        _players.value = current.map {
            if (it.nombre == name) it.copy(vecesImpostor = it.vecesImpostor + 1) else it
        }
    }

    fun pickImpostorIndex(players: List<Jugador>): Int {
        val weights = players.map { 1.0 / (1 + it.vecesImpostor) }
        val total = weights.sum()
        val r = Random.nextDouble() * total
        var acc = 0.0
        for (i in players.indices) {
            acc += weights[i]
            if (r <= acc) return i
        }
        return players.lastIndex
    }

}

