package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel

class MiContenidoBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "MiContenido"
    }

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var datosViewModel: DatosCuriososViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_mi_contenido, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.rootMiContenido)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())   -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else                                     -> ThemeManager.getBgCard(requireContext())
            }
        )
        val accent = ThemeManager.getAccentColor(requireContext())
        view.findViewById<TextView>(R.id.txtMiContenidoTitle)?.setShadowLayer(12f, 0f, 0f, accent)

        categoryViewModel = ViewModelProvider(requireActivity()).get(CategoryViewModel::class.java)
        datosViewModel    = ViewModelProvider(requireActivity()).get(DatosCuriososViewModel::class.java)

        view.findViewById<View>(R.id.btnCrearCategoria).setOnClickListener {
            CrearCategoriaBottomSheet().show(parentFragmentManager, CrearCategoriaBottomSheet.TAG)
        }

        view.findViewById<View>(R.id.btnCrearFactCategoria).setOnClickListener {
            CrearFactCategoriaBottomSheet().show(parentFragmentManager, CrearFactCategoriaBottomSheet.TAG)
        }

        actualizarListaLocal(view)

        categoryViewModel.categories.observe(viewLifecycleOwner) { actualizarListaLocal(view) }
        datosViewModel.categorias.observe(viewLifecycleOwner) { actualizarListaLocal(view) }
    }

    private fun actualizarListaLocal(view: View) {
        val llCategorias = view.findViewById<LinearLayout>(R.id.llCategoriasLocales) ?: return
        val llDatos      = view.findViewById<LinearLayout>(R.id.llDatosLocales)      ?: return

        llCategorias.removeAllViews()
        val catLocales = categoryViewModel.getCategoriasLocales()
        if (catLocales.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = getString(R.string.mi_contenido_vacio)
                setTextColor(0x99FFFFFF.toInt())
                textSize = 13f
                setPadding(0, 8, 0, 8)
            }
            llCategorias.addView(tv)
        } else {
            catLocales.forEach { cat ->
                val row = layoutInflater.inflate(R.layout.item_mi_contenido_row, llCategorias, false)
                row.findViewById<TextView>(R.id.txtRowName).text = "${cat.iconEmoji} ${cat.title}"
                row.findViewById<View>(R.id.btnRowEdit).setOnClickListener {
                    EditarCategoriaBottomSheet.newInstance(cat.id)
                        .show(parentFragmentManager, EditarCategoriaBottomSheet.TAG)
                }
                row.findViewById<View>(R.id.btnRowDelete).setOnClickListener {
                    categoryViewModel.borrarCategoriaLocal(cat.id) {}
                }
                llCategorias.addView(row)
            }
        }

        llDatos.removeAllViews()
        val datosLocales = datosViewModel.getCategoriasLocales()
        if (datosLocales.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = getString(R.string.mi_contenido_vacio)
                setTextColor(0x99FFFFFF.toInt())
                textSize = 13f
                setPadding(0, 8, 0, 8)
            }
            llDatos.addView(tv)
        } else {
            datosLocales.forEach { cat ->
                val row = layoutInflater.inflate(R.layout.item_mi_contenido_row, llDatos, false)
                row.findViewById<TextView>(R.id.txtRowName).text = "${cat.emoji} ${cat.nombre(LocaleManager.getLanguage(requireContext()))}"
                row.findViewById<View>(R.id.btnRowEdit).setOnClickListener {
                    EditarFactCategoriaBottomSheet.newInstance(cat.id)
                        .show(parentFragmentManager, EditarFactCategoriaBottomSheet.TAG)
                }
                row.findViewById<View>(R.id.btnRowDelete).setOnClickListener {
                    datosViewModel.borrarFactCategoriaLocal(cat.id) {}
                }
                llDatos.addView(row)
            }
        }
    }
}
