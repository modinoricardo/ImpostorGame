package com.example.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {

    // Lista interna modificable
    private val _players = MutableLiveData<MutableMap<String, Boolean>>(mutableMapOf())

    // LiveData pública que la vista observa
    val players: LiveData<MutableMap<String, Boolean>> = _players

    // Añadir jugador
    fun addPlayer(name: String) {
        val mapaActual = _players.value?: mutableMapOf()
        mapaActual[name] = false
        _players.value = mapaActual
    }

    // Editar jugador
    fun editPlayer(oldName: String, newName: String) {
        val mapa = _players.value?: mutableMapOf()

        if(mapa.containsKey(oldName)){
//            val value = mapa[oldName] ?: false
            mapa.remove(oldName)
            mapa[newName] = false
            _players.value = mapa
        }

    }

    // Eliminar jugador
    fun removePlayer(name: String) {
        val mapa = _players.value?: mutableMapOf()
        mapa.remove(name)
        _players.value = mapa
    }
}
