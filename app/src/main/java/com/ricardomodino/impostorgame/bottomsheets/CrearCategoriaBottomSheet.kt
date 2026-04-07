package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel

class CrearCategoriaBottomSheet : BaseGameBottomSheet() {

    companion object { const val TAG = "CrearCategoria" }

    override val expandForKeyboard = true
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var llPalabras: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_crear_categoria, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.rootCrearCategoria)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())   -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else                                     -> ThemeManager.getBgCard(requireContext())
            }
        )
        val accent = ThemeManager.getAccentColor(requireContext())
        view.findViewById<TextView>(R.id.txtCrearCatTitle)?.setShadowLayer(12f, 0f, 0f, accent)

        categoryViewModel = ViewModelProvider(requireActivity()).get(CategoryViewModel::class.java)

        llPalabras = view.findViewById(R.id.llPalabras)

        view.findViewById<View>(R.id.btnAgregarPalabra).setOnClickListener {
            agregarFilaPalabra()
        }

        view.findViewById<View>(R.id.btnGuardarCategoria).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener { guardar(view) }
        }
    }

    private fun agregarFilaPalabra() {
        val fila = layoutInflater.inflate(R.layout.item_word_input_row, llPalabras, false)
        fila.findViewById<View>(R.id.btnEliminarPalabra).setOnClickListener {
            llPalabras.removeView(fila)
        }
        llPalabras.addView(fila)
    }

    private fun guardar(view: View) {
        val titulo = view.findViewById<EditText>(R.id.editCategoriaNombre).text.toString().trim()
        val emoji  = view.findViewById<EditText>(R.id.editCategoriaEmoji).text.toString().trim().ifBlank { "📝" }

        if (titulo.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.crear_cat_nombre_requerido), Toast.LENGTH_SHORT).show()
            return
        }

        val palabras = mutableListOf<Pair<String, String>>()
        for (i in 0 until llPalabras.childCount) {
            val fila   = llPalabras.getChildAt(i)
            val nombre = fila.findViewById<EditText>(R.id.editPalabraNombre).text.toString().trim()
            val pista  = fila.findViewById<EditText>(R.id.editPalabraPista).text.toString().trim()
            if (nombre.isBlank()) continue
            if (pista.isBlank()) {
                Toast.makeText(requireContext(), getString(R.string.crear_cat_pista_requerida), Toast.LENGTH_SHORT).show()
                return
            }
            val pistasUnidas = pista.split(",").map { it.trim() }.filter { it.isNotBlank() }.joinToString("|")
            palabras.add(nombre to pistasUnidas)
        }

        categoryViewModel.crearCategoriaLocal(titulo, emoji, palabras) {
            dismiss()
        }
    }
}
