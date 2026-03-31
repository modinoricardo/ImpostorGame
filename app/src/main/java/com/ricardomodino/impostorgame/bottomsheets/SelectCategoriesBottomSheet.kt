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
import com.ricardomodino.impostorgame.adapters.CategoryAdapterSelect
import com.ricardomodino.impostorgame.viewmodel.CategoryViewModel
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Category

class SelectCategoriesBottomSheet : BaseGameBottomSheet() {

    interface Listener {
        fun onCategoriesConfirmed(selected: List<Category>)
    }

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapterSelect

    override val isDraggableSheet: Boolean = false
    override val isHideableSheet: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            if (ThemeManager.esCarmesi(requireContext())) R.layout.bottomsheet_select_categories_carmesi
            else if (ThemeManager.esFinal(requireContext())) R.layout.bottomsheet_select_categories_final
            else R.layout.bottomsheet_select_categories,
            container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Aplicar tema ──
        val bgCard  = ThemeManager.getBgCard(requireContext())
        val btnNeon = ThemeManager.getBtnNeon(requireContext())
        val accent  = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootCategories)?.setBackgroundResource(
            if (ThemeManager.esCarmesi(requireContext())) R.drawable.bg_carmesi_sheet else bgCard
        )
        view.findViewById<TextView>(R.id.textTitle)?.setShadowLayer(10f, 0f, 0f, accent)
        view.findViewById<Button>(R.id.btnConfirmCategories)?.setBackgroundResource(btnNeon)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerCategories)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmCategories)

        categoryViewModel = ViewModelProvider(requireActivity()).get(CategoryViewModel::class.java)

        adapter = CategoryAdapterSelect(emptyList()) { category ->
            categoryViewModel.toggleSelection(category.id)
        }

        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter

        categoryViewModel.categories.observe(viewLifecycleOwner) { list ->
            adapter.updateCategories(list)
        }

        btnConfirm.setOnClickListener {
            val selected = categoryViewModel.getSelectedCategories()
            (activity as? Listener)?.onCategoriesConfirmed(selected)
            dismiss()
        }
    }

    companion object {
        const val TAG = "SelectCategories"
    }
}
