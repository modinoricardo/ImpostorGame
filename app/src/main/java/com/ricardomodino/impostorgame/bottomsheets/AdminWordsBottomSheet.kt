package com.ricardomodino.impostorgame.bottomsheets

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.entities.WordEntity
import com.ricardomodino.impostorgame.data.repository.AdminRepository
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminWordsBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "AdminWordsBS"
        private const val ARG_CAT_ID    = "cat_id"
        private const val ARG_CAT_NOMBRE = "cat_nombre"

        fun newInstance(catId: Long, catNombre: String) =
            AdminWordsBottomSheet().apply {
                arguments = Bundle().also {
                    it.putLong(ARG_CAT_ID, catId)
                    it.putString(ARG_CAT_NOMBRE, catNombre)
                }
            }
    }

    override val expandForKeyboard = true

    private lateinit var repo: AdminRepository
    private lateinit var llWords: LinearLayout
    private var catId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_admin_words, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.rootAdminWords)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())   -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else                                     -> ThemeManager.getBgCard(requireContext())
            }
        )

        catId = arguments?.getLong(ARG_CAT_ID) ?: 0L
        val catNombre = arguments?.getString(ARG_CAT_NOMBRE) ?: ""

        repo    = AdminRepository(AppDatabase.getInstance(requireContext()))
        llWords = view.findViewById(R.id.llAdminWords)

        view.findViewById<TextView>(R.id.txtAdminWordsCatNombre).text = catNombre

        view.findViewById<Button>(R.id.btnAdminAddWord).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener { mostrarDialogoAddWord() }
        }

        recargar()
    }

    private fun recargar() {
        lifecycleScope.launch(Dispatchers.IO) {
            val words = repo.getPalabrasDe(catId)
            withContext(Dispatchers.Main) { poblarWords(words) }
        }
    }

    private fun poblarWords(words: List<WordEntity>) {
        llWords.removeAllViews()
        if (words.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = getString(R.string.admin_vacio)
                setTextColor(0x99FFFFFF.toInt())
                textSize = 13f
                setPadding(0, 8, 0, 8)
            }
            llWords.addView(tv)
            return
        }
        words.forEach { word ->
            val row = layoutInflater.inflate(R.layout.item_admin_word_row, llWords, false)
            row.findViewById<TextView>(R.id.txtAdminWordNombre).text = word.nameEs
            row.findViewById<TextView>(R.id.txtAdminWordPistas).text =
                word.hintsEs.replace("|", ", ").ifBlank { "—" }
            row.findViewById<View>(R.id.btnAdminWordDelete).setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.admin_confirmar_borrar, word.nameEs))
                    .setPositiveButton(getString(R.string.admin_borrar)) { _, _ -> borrarWord(word) }
                    .setNegativeButton(getString(R.string.dialog_salir_no), null)
                    .show()
            }
            llWords.addView(row)
        }
    }

    private fun mostrarDialogoAddWord() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val editNombre = EditText(requireContext()).apply { hint = getString(R.string.crear_cat_palabra_hint) }
        val editPistas = EditText(requireContext()).apply { hint = getString(R.string.crear_cat_pista_hint) }
        layout.addView(editNombre)
        layout.addView(editPistas)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_add_word))
            .setView(layout)
            .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                val nombre = editNombre.text.toString().trim()
                val pistas = editPistas.text.toString().trim()
                if (nombre.isNotBlank() && pistas.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            repo.crearPalabraGlobal(catId, nombre, pistas)
                            ViewModelProvider(requireActivity())
                                .get(CategoryViewModel::class.java).recargar()
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

    private fun borrarWord(word: WordEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repo.borrarPalabraGlobal(word)
                ViewModelProvider(requireActivity())
                    .get(CategoryViewModel::class.java).recargar()
                withContext(Dispatchers.Main) { recargar() }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.admin_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
