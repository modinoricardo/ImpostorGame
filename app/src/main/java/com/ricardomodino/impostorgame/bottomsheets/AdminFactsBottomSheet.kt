package com.ricardomodino.impostorgame.bottomsheets

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.entities.FactEntity
import com.ricardomodino.impostorgame.data.repository.AdminRepository
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminFactsBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "AdminFactsBS"
        private const val ARG_CAT_ID    = "cat_id"
        private const val ARG_CAT_NOMBRE = "cat_nombre"

        fun newInstance(catId: Long, catNombre: String) =
            AdminFactsBottomSheet().apply {
                arguments = Bundle().also {
                    it.putLong(ARG_CAT_ID, catId)
                    it.putString(ARG_CAT_NOMBRE, catNombre)
                }
            }
    }

    override val expandForKeyboard = true

    private lateinit var repo: AdminRepository
    private lateinit var llFacts: LinearLayout
    private var catId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_admin_facts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.rootAdminFacts)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())   -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else                                     -> ThemeManager.getBgCard(requireContext())
            }
        )

        catId = arguments?.getLong(ARG_CAT_ID) ?: 0L
        val catNombre = arguments?.getString(ARG_CAT_NOMBRE) ?: ""

        repo    = AdminRepository(AppDatabase.getInstance(requireContext()))
        llFacts = view.findViewById(R.id.llAdminFacts)

        view.findViewById<TextView>(R.id.txtAdminFactsCatNombre).text = catNombre

        view.findViewById<Button>(R.id.btnAdminAddFact).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener { mostrarDialogoAddFact() }
        }

        recargar()
    }

    private fun recargar() {
        lifecycleScope.launch(Dispatchers.IO) {
            val facts = repo.getFactsDe(catId)
            withContext(Dispatchers.Main) { poblarFacts(facts) }
        }
    }

    private fun poblarFacts(facts: List<FactEntity>) {
        llFacts.removeAllViews()
        if (facts.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = getString(R.string.admin_vacio)
                setTextColor(0x99FFFFFF.toInt())
                textSize = 13f
                setPadding(0, 8, 0, 8)
            }
            llFacts.addView(tv)
            return
        }
        facts.forEach { fact ->
            val row = layoutInflater.inflate(R.layout.item_admin_word_row, llFacts, false)
            row.findViewById<TextView>(R.id.txtAdminWordNombre).text = fact.textEs
            row.findViewById<TextView>(R.id.txtAdminWordPistas).visibility = View.GONE
            row.findViewById<View>(R.id.btnAdminWordDelete).setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.admin_confirmar_borrar_dato))
                    .setPositiveButton(getString(R.string.admin_borrar)) { _, _ -> borrarFact(fact) }
                    .setNegativeButton(getString(R.string.dialog_salir_no), null)
                    .show()
            }
            llFacts.addView(row)
        }
    }

    private fun mostrarDialogoAddFact() {
        val editTexto = EditText(requireContext()).apply {
            hint = getString(R.string.crear_fact_dato_hint)
            minLines = 2
            setPadding(48, 32, 48, 16)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_add_fact))
            .setView(editTexto)
            .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                val texto = editTexto.text.toString().trim()
                if (texto.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            repo.crearFactGlobal(catId, texto)
                            ViewModelProvider(requireActivity())
                                .get(DatosCuriososViewModel::class.java).recargar()
                            withContext(Dispatchers.Main) { recargar() }
                        } catch (_: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), getString(R.string.admin_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }

    private fun borrarFact(fact: FactEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repo.borrarFactGlobal(fact)
                ViewModelProvider(requireActivity())
                    .get(DatosCuriososViewModel::class.java).recargar()
                withContext(Dispatchers.Main) { recargar() }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.admin_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
