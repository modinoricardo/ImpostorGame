package com.example.impostorgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectCategoriesBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onCategoriesConfirmed(selected: List<Category>)
    }

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapterSelect

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_select_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerCategories)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmCategories)

        categoryViewModel = ViewModelProvider(requireActivity()).get(CategoryViewModel::class.java)

        adapter = CategoryAdapterSelect(emptyList()) { category ->
            // Al pulsar una categoría, pedimos al ViewModel que cambie su selección
            categoryViewModel.toggleSelection(category.id)
        }

        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = adapter

        // Observar las categorías del ViewModel
        categoryViewModel.categories.observe(viewLifecycleOwner) { list ->
            adapter.updateCategories(list)
            //btnConfirm.isEnabled = list.any { it.isSelected }
        }

        btnConfirm.setOnClickListener {
            val selected = categoryViewModel.getSelectedCategories()
            (activity as? Listener)?.onCategoriesConfirmed(selected)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // tu código para bloquear el swipe, si lo tenías
    }

    companion object {
        const val TAG = "SelectCategories"
    }
}

