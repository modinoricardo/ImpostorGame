package com.example.impostorgame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel : ViewModel() {

    // Lista interna modificable
    private val _players = MutableLiveData<List<String>>(
        listOf("Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste",
            "Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste"
            )
    )

    // LiveData pública que la vista observa
    val players: LiveData<List<String>> = _players

    // Añadir jugador
    fun addPlayer(name: String) {
        val currentList = _players.value?.toMutableList() ?: mutableListOf()
        currentList.add(name)
        _players.value = currentList
    }

    // Editar jugador
    fun editPlayer(index: Int, newName: String) {
        val currentList = _players.value?.toMutableList() ?: return
        if (index in currentList.indices) {
            currentList[index] = newName
            _players.value = currentList
        }
    }

    // Eliminar jugador
    fun removePlayer(index: Int) {
        val currentList = _players.value?.toMutableList() ?: return
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _players.value = currentList
        }
    }
}
