package com.example.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {

    // Lista interna modificable (inicializada vacía)
    private val _players = MutableLiveData<MutableMap<String, Boolean>>(mutableMapOf())

    // LiveData pública
    val players: LiveData<MutableMap<String, Boolean>> = _players

    // Añadir jugador
    fun addPlayer(name: String) {
        val mapa = _players.value ?: mutableMapOf()

        mapa[name] = false
        _players.value = mapa
    }

    // Editar jugador
    fun editPlayer(oldName: String, newName: String) {
        val mapa = _players.value ?: mutableMapOf()

        // Construir un nuevo mapa manteniendo el orden
        val nuevoMapa = linkedMapOf<String, Boolean>()

        for ((key, value) in mapa) {
            if (key == oldName) {
                nuevoMapa[newName] = value
            } else {
                nuevoMapa[key] = value
            }
        }

        _players.value = nuevoMapa
    }


    // Eliminar jugador
    fun removePlayer(name: String) {
        val mapa = _players.value ?: mutableMapOf()
        mapa.remove(name)
        _players.value = mapa
    }
}
