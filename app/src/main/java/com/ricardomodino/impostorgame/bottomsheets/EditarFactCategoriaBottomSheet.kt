package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel

class EditarFactCategoriaBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "EditarFactCategoria"
        private const val ARG_CAT_ID = "cat_id"
        fun newInstance(catId: Long) = EditarFactCategoriaBottomSheet().apply {
            arguments = Bundle().also { it.putLong(ARG_CAT_ID, catId) }
        }
    }

    override val expandForKeyboard = true
    private lateinit var datosViewModel: DatosCuriososViewModel
    private var catId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_editar_fact_categoria, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        catId = arguments?.getLong(ARG_CAT_ID) ?: 0L
        datosViewModel = ViewModelProvider(requireActivity()).get(DatosCuriososViewModel::class.java)

        view.findViewById<View>(R.id.rootEditarFact)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())   -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else                                     -> ThemeManager.getBgCard(requireContext())
            }
        )

        val language = LocaleManager.getLanguage(requireContext())
        val cat = datosViewModel.categorias.value?.firstOrNull { it.id == catId }
        view.findViewById<EditText>(R.id.editEditarFactNombre).setText(cat?.nombre(language) ?: "")
        view.findViewById<EditText>(R.id.editEditarFactEmoji).setText(cat?.emoji ?: "")

        view.findViewById<View>(R.id.btnGuardarEdicionFact).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener {
                val newName  = view.findViewById<EditText>(R.id.editEditarFactNombre).text.toString().trim()
                val newEmoji = view.findViewById<EditText>(R.id.editEditarFactEmoji).text.toString().trim().ifBlank { "📝" }
                if (newName.isBlank()) return@setOnClickListener
                datosViewModel.editarFactCategoriaLocal(catId, newName, newEmoji) { dismiss() }
            }
        }
    }
}
