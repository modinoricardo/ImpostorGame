package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.adapters.DatosCategoriesAdapter
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.viewmodel.DatosCuriososViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectDatosCategoriesBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onDatosCategoriesConfirmed()
    }

    private lateinit var datosViewModel: DatosCuriososViewModel
    private lateinit var adapter: DatosCategoriesAdapter

    override fun getTheme(): Int = R.style.Theme_EditPlayersBottomSheet

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout = if (ThemeManager.esFinal(requireContext()))
            R.layout.bottomsheet_select_datos_categories_final
        else if (ThemeManager.esCarmesi(requireContext()))
            R.layout.bottomsheet_select_datos_categories_carmesi
        else
            R.layout.bottomsheet_select_datos_categories
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bgCard  = ThemeManager.getBgCard(requireContext())
        val btnNeon = ThemeManager.getBtnNeon(requireContext())
        val accent  = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootCategories)?.setBackgroundResource(
            if (ThemeManager.esCarmesi(requireContext())) R.drawable.bg_carmesi_sheet else bgCard
        )
        view.findViewById<TextView>(R.id.textTitle)?.setShadowLayer(10f, 0f, 0f, accent)
        view.findViewById<Button>(R.id.btnConfirmCategories)?.setBackgroundResource(btnNeon)

        val recycler   = view.findViewById<RecyclerView>(R.id.recyclerCategories)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmCategories)

        datosViewModel = ViewModelProvider(requireActivity()).get(DatosCuriososViewModel::class.java)

        adapter = DatosCategoriesAdapter(emptyList()) { category ->
            datosViewModel.toggleSelection(category.id)
        }

        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter

        datosViewModel.categorias.observe(viewLifecycleOwner) { list ->
            adapter.updateCategories(list)
        }

        btnConfirm.setOnClickListener {
            (activity as? Listener)?.onDatosCategoriesConfirmed()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        ImmersiveModeManager.apply(dialog?.window)
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val rounded = when {
            ThemeManager.esFinal(requireContext()) -> R.drawable.bottomsheet_rounded_final
            ThemeManager.esCarmesi(requireContext()) -> R.drawable.bottomsheet_rounded_carmesi
            else -> R.drawable.bottomsheet_rounded
        }
        val behavior = ImmersiveModeManager.prepareBottomSheet(
            bottomSheet,
            ContextCompat.getDrawable(requireContext(), rounded)
        )

        bottomSheet.post {
            val h = if (bottomSheet.height > 0) bottomSheet.height
            else bottomSheet.resources.displayMetrics.heightPixels
            bottomSheet.translationY = h.toFloat()
            bottomSheet.alpha = 0f
            bottomSheet.animate()
                .translationY(0f).alpha(1f)
                .setDuration(550L)
                .setInterpolator(DecelerateInterpolator(2f)).start()
        }

        behavior.isDraggable = false
        behavior.isHideable = false
    }

    companion object {
        const val TAG = "SelectDatosCategorias"
    }
}
