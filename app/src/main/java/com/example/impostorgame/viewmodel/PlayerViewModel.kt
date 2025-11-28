package com.example.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {

    private val _players = MutableLiveData<List<String>>(
        listOf("Jugador 1", "Jugador 2", "Jugador 3")
    )

    val players: LiveData<List<String>> = _players

    // Añadir jugador (al final de la lista)
    fun addPlayer(name: String) {
        val current = _players.value ?: emptyList()
        _players.value = current + name
    }

    // Eliminar jugador por posición
    fun removeAt(index: Int): Boolean {
        val current = _players.value?.toMutableList() ?: return false

        // Si ya hay 3 o menos, NO borramos y devolvemos false
        if (current.size <= 3) {
            return false
        }

        if (index in current.indices) {
            current.removeAt(index)
            _players.value = current
            return true     // borrado OK
        }

        return false        // índice inválido
    }


    // Renombrar un jugador concreto, por su posición
    fun renameAt(index: Int, newName: String) {
        val current = _players.value?.toMutableList() ?: return
        if (index in current.indices) {
            current[index] = newName
            _players.value = current
        }
    }

    // Sobrescribir la lista entera (opcional)
    fun updatePlayers(newList: List<String>) {
        _players.value = newList
    }

    fun getPlayerCount(): Int {
        return _players.value?.size ?: 0
    }
}
