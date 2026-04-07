package com.ricardomodino.impostorgame.bottomsheets

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.entities.CategoryEntity
import com.ricardomodino.impostorgame.data.local.entities.FactCategoryEntity
import com.ricardomodino.impostorgame.data.repository.AdminRepository
import com.ricardomodino.impostorgame.managers.AdminManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "AdminBottomSheet"
    }

    override val expandForKeyboard = true

    private lateinit var repo: AdminRepository
    private lateinit var llCats: LinearLayout
    private lateinit var llFactCats: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_admin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.rootAdmin)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())   -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else                                     -> ThemeManager.getBgCard(requireContext())
            }
        )
        val accent = ThemeManager.getAccentColor(requireContext())
        view.findViewById<TextView>(R.id.txtAdminTitle)?.setShadowLayer(12f, 0f, 0f, accent)

        repo     = AdminRepository(AppDatabase.getInstance(requireContext()))
        llCats     = view.findViewById(R.id.llAdminCats)
        llFactCats = view.findViewById(R.id.llAdminFactCats)

        view.findViewById<Button>(R.id.btnAdminNuevaCat).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener { mostrarDialogoCrearCategoria() }
        }
        view.findViewById<Button>(R.id.btnAdminNuevaFactCat).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener { mostrarDialogoCrearFactCategoria() }
        }
        view.findViewById<Button>(R.id.btnAdminPublicarSeed).apply {
            setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))
            setOnClickListener { confirmarPublicarSeed() }
        }

        view.findViewById<Button>(R.id.btnAdminLogout).setOnClickListener {
            AdminManager.logout(requireContext())
            dismiss()
        }

        recargarListas()
    }

    private fun recargarListas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cats     = repo.getCategoriasGlobales()
            val factCats = repo.getFactCategoriasGlobales()
            withContext(Dispatchers.Main) {
                poblarCategorias(cats)
                poblarFactCategorias(factCats)
            }
        }
    }

    private fun poblarCategorias(cats: List<CategoryEntity>) {
        llCats.removeAllViews()
        if (cats.isEmpty()) {
            llCats.addView(textoVacio(getString(R.string.admin_vacio)))
            return
        }
        cats.forEach { cat ->
            val row = layoutInflater.inflate(R.layout.item_admin_cat_row, llCats, false)
            row.findViewById<TextView>(R.id.txtAdminCatNombre).text = "${cat.iconEmoji} ${cat.titleEs}"
            row.findViewById<View>(R.id.btnAdminCatWords).setOnClickListener {
                AdminWordsBottomSheet.newInstance(cat.id, "${cat.iconEmoji} ${cat.titleEs}")
                    .show(parentFragmentManager, AdminWordsBottomSheet.TAG)
            }
            row.findViewById<View>(R.id.btnAdminCatEdit).setOnClickListener {
                mostrarDialogoEditarCategoria(cat)
            }
            row.findViewById<View>(R.id.btnAdminCatDelete).setOnClickListener {
                confirmarBorrar(cat.titleEs) {
                    ejecutar { repo.borrarCategoriaGlobal(cat.id) }
                }
            }
            llCats.addView(row)
        }
    }

    private fun poblarFactCategorias(cats: List<FactCategoryEntity>) {
        llFactCats.removeAllViews()
        if (cats.isEmpty()) {
            llFactCats.addView(textoVacio(getString(R.string.admin_vacio)))
            return
        }
        cats.forEach { cat ->
            val row = layoutInflater.inflate(R.layout.item_admin_cat_row, llFactCats, false)
            row.findViewById<TextView>(R.id.txtAdminCatNombre).text = "${cat.emoji} ${cat.nameEs}"
            row.findViewById<View>(R.id.btnAdminCatWords).setOnClickListener {
                AdminFactsBottomSheet.newInstance(cat.id, "${cat.emoji} ${cat.nameEs}")
                    .show(parentFragmentManager, AdminFactsBottomSheet.TAG)
            }
            row.findViewById<View>(R.id.btnAdminCatEdit).setOnClickListener {
                mostrarDialogoEditarFactCategoria(cat)
            }
            row.findViewById<View>(R.id.btnAdminCatDelete).setOnClickListener {
                confirmarBorrar(cat.nameEs) {
                    ejecutar { repo.borrarFactCategoriaGlobal(cat.id) }
                }
            }
            llFactCats.addView(row)
        }
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────

    private fun mostrarDialogoCrearCategoria() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val editNombre = EditText(requireContext()).apply { hint = getString(R.string.crear_cat_nombre_hint) }
        val editEmoji  = EditText(requireContext()).apply { hint = getString(R.string.crear_cat_emoji_hint) }
        layout.addView(editNombre)
        layout.addView(editEmoji)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_nueva_cat))
            .setView(layout)
            .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                val nombre = editNombre.text.toString().trim()
                val emoji  = editEmoji.text.toString().trim().ifBlank { "📝" }
                if (nombre.isNotBlank()) ejecutar { repo.crearCategoriaGlobal(nombre, emoji) }
            }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }

    private fun mostrarDialogoEditarCategoria(cat: CategoryEntity) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val editNombre = EditText(requireContext()).apply { setText(cat.titleEs) }
        val editEmoji  = EditText(requireContext()).apply { setText(cat.iconEmoji) }
        layout.addView(editNombre)
        layout.addView(editEmoji)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.editar_categoria_titulo))
            .setView(layout)
            .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                val nombre = editNombre.text.toString().trim()
                val emoji  = editEmoji.text.toString().trim().ifBlank { "📝" }
                if (nombre.isNotBlank()) ejecutar { repo.editarCategoriaGlobal(cat.id, nombre, emoji) }
            }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }

    private fun mostrarDialogoCrearFactCategoria() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val editNombre = EditText(requireContext()).apply { hint = getString(R.string.crear_cat_nombre_hint) }
        val editEmoji  = EditText(requireContext()).apply { hint = getString(R.string.crear_cat_emoji_hint) }
        layout.addView(editNombre)
        layout.addView(editEmoji)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_nueva_fact_cat))
            .setView(layout)
            .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                val nombre = editNombre.text.toString().trim()
                val emoji  = editEmoji.text.toString().trim().ifBlank { "📝" }
                if (nombre.isNotBlank()) ejecutar { repo.crearFactCategoriaGlobal(nombre, emoji) }
            }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }

    private fun mostrarDialogoEditarFactCategoria(cat: FactCategoryEntity) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val editNombre = EditText(requireContext()).apply { setText(cat.nameEs) }
        val editEmoji  = EditText(requireContext()).apply { setText(cat.emoji) }
        layout.addView(editNombre)
        layout.addView(editEmoji)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.editar_dato_titulo))
            .setView(layout)
            .setPositiveButton(getString(R.string.guardar)) { _, _ ->
                val nombre = editNombre.text.toString().trim()
                val emoji  = editEmoji.text.toString().trim().ifBlank { "📝" }
                if (nombre.isNotBlank()) ejecutar { repo.editarFactCategoriaGlobal(cat.id, nombre, emoji) }
            }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }

    private fun confirmarPublicarSeed() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_publicar_seed))
            .setMessage(getString(R.string.admin_publicar_seed_confirm))
            .setPositiveButton(getString(R.string.admin_publicar_seed)) { _, _ ->
                val toast = Toast.makeText(requireContext(), getString(R.string.admin_publicando), Toast.LENGTH_LONG)
                toast.show()
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val count = repo.publicarContenidoEnSupabase()
                        withContext(Dispatchers.Main) {
                            toast.cancel()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.admin_publicar_seed_ok, count),
                                Toast.LENGTH_LONG
                            ).show()
                            recargarListas()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminBottomSheet", "Error publicar seed: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            toast.cancel()
                            Toast.makeText(requireContext(), getString(R.string.admin_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }

    private fun confirmarBorrar(nombre: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_confirmar_borrar, nombre))
            .setPositiveButton(getString(R.string.admin_borrar)) { _, _ -> onConfirm() }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun ejecutar(block: suspend () -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                block()
                val catVM  = ViewModelProvider(requireActivity()).get(CategoryViewModel::class.java)
                val datVM  = ViewModelProvider(requireActivity()).get(DatosCuriososViewModel::class.java)
                withContext(Dispatchers.Main) {
                    catVM.recargar()
                    datVM.recargar()
                    recargarListas()
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminBottomSheet", "Error admin: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.admin_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun textoVacio(msg: String) = TextView(requireContext()).apply {
        text = msg
        setTextColor(0x99FFFFFF.toInt())
        textSize = 13f
        setPadding(0, 8, 0, 16)
    }
}
