package com.example.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {

    private val _players = MutableLiveData<List<String>>(emptyList())
    val players: LiveData<List<String>> = _players

    // Añadir jugador (al final de la lista)
    fun addPlayer(name: String) {
        val current = _players.value ?: emptyList()
        _players.value = current + name
    }

    // Eliminar jugador por posición
    fun removeAt(index: Int) {
        val current = _players.value?.toMutableList() ?: return
        if (index in current.indices) {
            current.removeAt(index)
            _players.value = current
        }
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
}
