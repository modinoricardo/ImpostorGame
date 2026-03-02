package com.example.impostorgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.impostorgame.managers.ThemeManager
import com.example.impostorgame.modelos.Category

class CategoryAdapterMain(
    private var categories: List<Category>
) : RecyclerView.Adapter<CategoryAdapterMain.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.textName.text = "${category.iconEmoji} ${category.title}"
        holder.itemView.setBackgroundResource(ThemeManager.getBgChip(holder.itemView.context))
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged()
    }
}