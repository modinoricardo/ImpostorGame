package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel

class CrearFactCategoriaBottomSheet : BaseGameBottomSheet() {

    companion object { const val TAG = "CrearFactCategoria" }

    override val expandForKeyboard = true
    private lateinit var datosViewModel: DatosCuriososViewModel
    private lateinit var llDatos: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_crear_fact_categoria, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.rootCrearFact)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())   -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else                                     -> ThemeManager.getBgCard(requireContext())
            }
        )
        val accent = ThemeManager.getAccentColor(requireContext())
        view.findViewById<TextView>(R.id.txtCrearFactTitle)?.setShadowLayer(12f, 0f, 0f, accent)

        datosViewModel = ViewModelProvider(requireActivity()).get(DatosCuriososViewModel::class.java)
        llDatos = view.findViewById(R.id.llDatos)

        view.findViewById<View>(R.id.btnAgregarDato).setOnClickListener {
            agregarFilaDato()
        }

        view.findViewById<View>(R.id.btnGuardarFactCategoria).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener { guardar(view) }
        }
    }

    private fun agregarFilaDato() {
        val fila = layoutInflater.inflate(R.layout.item_fact_input_row, llDatos, false)
        fila.findViewById<View>(R.id.btnEliminarDato).setOnClickListener {
            llDatos.removeView(fila)
        }
        llDatos.addView(fila)
    }

    private fun guardar(view: View) {
        val nombre = view.findViewById<EditText>(R.id.editFactCategoriaNombre).text.toString().trim()
        val emoji  = view.findViewById<EditText>(R.id.editFactCategoriaEmoji).text.toString().trim().ifBlank { "📝" }

        if (nombre.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.crear_cat_nombre_requerido), Toast.LENGTH_SHORT).show()
            return
        }

        val datos = mutableListOf<String>()
        for (i in 0 until llDatos.childCount) {
            val fila = llDatos.getChildAt(i)
            val texto = fila.findViewById<EditText>(R.id.editDatoTexto).text.toString().trim()
            if (texto.isNotBlank()) datos.add(texto)
        }

        datosViewModel.crearFactCategoriaLocal(nombre, emoji, datos) {
            dismiss()
        }
    }
}
