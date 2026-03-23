package com.ricardomodino.impostorgame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.modelos.DatoCategoria
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import org.json.JSONArray

class DatosCuriososViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("datos_curiosos", Application.MODE_PRIVATE)

    private val _categorias = MutableLiveData<List<DatoCategoria>>(cargarCategorias())
    val categorias: LiveData<List<DatoCategoria>> = _categorias

    // ── Carga el JSON de res/raw y aplica selecciones guardadas ──
    private fun cargarCategorias(): List<DatoCategoria> {
        val selectedIds = prefs.getStringSet("selected_ids", emptySet()) ?: emptySet()
        return parsearJson().map { it.copy(isSelected = it.id.toString() in selectedIds) }
    }

    private fun parsearJson(): List<DatoCategoria> {
        return try {
            val ctx = getApplication<Application>()
            val text = ctx.resources.openRawResource(R.raw.datos_curiosos)
                .bufferedReader().readText()
            val arr = JSONArray(text)
            List(arr.length()) { i ->
                val cat = arr.getJSONObject(i)
                val datosArr = cat.getJSONArray("datos")
                val datos = List(datosArr.length()) { j ->
                    val d = datosArr.getJSONObject(j)
                    DatoCurioso(
                        id       = d.getLong("id"),
                        es       = d.getString("es"),
                        en       = d.getString("en"),
                        zhHans   = d.getString("zhHans"),
                        zhHant   = d.getString("zhHant")
                    )
                }
                DatoCategoria(
                    id    = cat.getLong("id"),
                    es    = cat.getString("es"),
                    en    = cat.getString("en"),
                    zhHans = cat.getString("zhHans"),
                    zhHant = cat.getString("zhHant"),
                    emoji = cat.getString("emoji"),
                    datos = datos
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    // ── Devuelve las categorías seleccionadas (o todas si ninguna está seleccionada) ──
    fun getCategoriasActivas(): List<DatoCategoria> {
        val lista = _categorias.value ?: emptyList()
        val seleccionadas = lista.filter { it.isSelected }
        return seleccionadas.ifEmpty { lista }
    }

    fun toggleSelection(id: Long) {
        val lista = _categorias.value?.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        } ?: return
        _categorias.value = lista
        guardarSeleccion(lista)
    }

    fun setAllSelected(selected: Boolean) {
        val lista = _categorias.value?.map { it.copy(isSelected = selected) } ?: return
        _categorias.value = lista
        guardarSeleccion(lista)
    }

    private fun guardarSeleccion(lista: List<DatoCategoria>) {
        val ids = lista.filter { it.isSelected }.map { it.id.toString() }.toSet()
        prefs.edit().putStringSet("selected_ids", ids).apply()
    }
}
