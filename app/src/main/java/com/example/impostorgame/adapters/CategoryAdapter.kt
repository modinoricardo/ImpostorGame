package com.example.impostorgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private var categories: List<String>
): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // 1) ViewHolder: representa un item de la lista
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
    }

    // 2) Crear (inflar) la vista del item
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    // 3) Vincular datos a la vista
    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {
        val item = categories[position]
        holder.categoryName.text = item
    }

    // 4) Tamaño de la lista
    override fun getItemCount(): Int = categories.size

}