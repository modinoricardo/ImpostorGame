package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel

class EditarCategoriaBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "EditarCategoria"
        private const val ARG_CAT_ID = "cat_id"
        fun newInstance(catId: Long) = EditarCategoriaBottomSheet().apply {
            arguments = Bundle().also { it.putLong(ARG_CAT_ID, catId) }
        }
    }

    override val expandForKeyboard = true
    private lateinit var categoryViewModel: CategoryViewModel
    private var catId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_editar_categoria, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        catId = arguments?.getLong(ARG_CAT_ID) ?: 0L
        categoryViewModel = ViewModelProvider(requireActivity()).get(CategoryViewModel::class.java)

        view.findViewById<View>(R.id.rootEditarCategoria)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())   -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else                                     -> ThemeManager.getBgCard(requireContext())
            }
        )

        // Pre-fill with current data
        val cat = categoryViewModel.categories.value?.firstOrNull { it.id == catId }
        val editNombre = view.findViewById<EditText>(R.id.editEditarNombre)
        val editEmoji  = view.findViewById<EditText>(R.id.editEditarEmoji)
        editNombre.setText(cat?.title ?: "")
        editEmoji.setText(cat?.iconEmoji ?: "")

        view.findViewById<View>(R.id.btnGuardarEdicion).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener {
                val newName  = editNombre.text.toString().trim()
                val newEmoji = editEmoji.text.toString().trim().ifBlank { "📝" }
                if (newName.isBlank()) return@setOnClickListener
                categoryViewModel.editarCategoriaLocal(catId, newName, newEmoji) { dismiss() }
            }
        }
    }
}
