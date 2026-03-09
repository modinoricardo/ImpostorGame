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
import com.ricardomodino.impostorgame.CategoryAdapterSelect
import com.ricardomodino.impostorgame.CategoryViewModel
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Category
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectCategoriesBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onCategoriesConfirmed(selected: List<Category>)
    }

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapterSelect

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_select_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Aplicar tema ──
        val bgCard  = ThemeManager.getBgCard(requireContext())
        val btnNeon = ThemeManager.getBtnNeon(requireContext())
        val accent  = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootCategories)?.setBackgroundResource(bgCard)
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

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        bottomSheet.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bottomsheet_rounded
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

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isDraggable = false
        behavior.isHideable = false
    }

    companion object {
        const val TAG = "SelectCategories"
    }
}