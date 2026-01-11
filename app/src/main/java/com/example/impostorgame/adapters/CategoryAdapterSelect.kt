package com.example.impostorgame

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.impostorgame.modelos.Category

class CategoryAdapterSelect(
    private var categories: List<Category>,
    private val onCategoryClicked: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapterSelect.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardCategory: View = itemView.findViewById(R.id.cardCategory)
        val textEmoji: TextView = itemView.findViewById(R.id.textEmoji)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_select, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categories[position]

        holder.textEmoji.text = item.iconEmoji
        holder.textTitle.text = item.title

        applySelectedStyle(holder, item.isSelected)

        holder.cardCategory.setOnClickListener {
            val adapterPos = holder.adapterPosition
            if (adapterPos == RecyclerView.NO_POSITION) return@setOnClickListener

            val clicked = categories[adapterPos]
            onCategoryClicked(clicked)   // ViewModel.toggleSelection(...)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged()
    }

    private fun applySelectedStyle(holder: CategoryViewHolder, selected: Boolean) {
        if (selected) {
            holder.cardCategory.setBackgroundResource(R.drawable.bg_category_selected)
            holder.textTitle.setTypeface(null, Typeface.BOLD)
        } else {
            holder.cardCategory.setBackgroundResource(R.drawable.bg_category_normal)
            holder.textTitle.setTypeface(null, Typeface.NORMAL)
        }
    }
}

