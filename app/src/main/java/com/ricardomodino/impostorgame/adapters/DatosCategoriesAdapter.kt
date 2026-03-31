package com.ricardomodino.impostorgame.adapters

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.DatoCategoria

class DatosCategoriesAdapter(
    private var categories: List<DatoCategoria>,
    private val onCategoryClicked: (DatoCategoria) -> Unit
) : RecyclerView.Adapter<DatosCategoriesAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardCategory: View   = itemView.findViewById(R.id.cardCategory)
        val textEmoji: TextView  = itemView.findViewById(R.id.textEmoji)
        val textTitle: TextView  = itemView.findViewById(R.id.textTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(
            LayoutInflater.from(parent.context).inflate(
                if (ThemeManager.esCarmesi(parent.context)) R.layout.item_category_select_carmesi else R.layout.item_category_select,
                parent,
                false
            )
        )

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = categories[position]
        val idioma = LocaleManager.getLanguage(holder.itemView.context)
        holder.textEmoji.text = item.emoji
        holder.textTitle.text = item.nombre(idioma)
        applySelectedStyle(holder, item.isSelected)

        holder.cardCategory.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            onCategoryClicked(categories[pos])
        }
    }

    fun updateCategories(newList: List<DatoCategoria>) {
        categories = newList
        notifyDataSetChanged()
    }

    private fun applySelectedStyle(holder: VH, selected: Boolean) {
        val ctx = holder.itemView.context
        when {
            ThemeManager.esFinal(ctx) -> {
                holder.textEmoji.alpha = if (selected) 1f else 0.82f
                if (selected) {
                    holder.cardCategory.setBackgroundResource(R.drawable.bg_category_selected_final)
                    holder.textTitle.setTypeface(null, Typeface.BOLD)
                    holder.textTitle.setTextColor(Color.parseColor("#FFF4DF"))
                } else {
                    holder.cardCategory.setBackgroundResource(R.drawable.bg_category_normal_final)
                    holder.textTitle.setTypeface(null, Typeface.NORMAL)
                    holder.textTitle.setTextColor(Color.parseColor("#E1D5C7"))
                }
            }
            ThemeManager.esCarmesi(ctx) -> {
                if (selected) {
                    holder.cardCategory.setBackgroundResource(R.drawable.bg_category_selected_carmesi)
                    holder.textTitle.setTypeface(null, Typeface.BOLD)
                    holder.textTitle.setTextColor(ContextCompat.getColor(ctx, R.color.carmesi_text_primary))
                } else {
                    holder.cardCategory.setBackgroundResource(R.drawable.bg_category_normal_carmesi)
                    holder.textTitle.setTypeface(null, Typeface.NORMAL)
                    holder.textTitle.setTextColor(ContextCompat.getColor(ctx, R.color.carmesi_text_secondary))
                }
            }
            ThemeManager.esJmc(ctx) -> {
                if (selected) {
                    holder.cardCategory.setBackgroundResource(R.drawable.bg_category_selected_jmc)
                    holder.textTitle.setTypeface(null, Typeface.BOLD)
                    holder.textTitle.setTextColor(Color.WHITE)
                } else {
                    holder.cardCategory.setBackgroundResource(R.drawable.bg_category_normal_jmc)
                    holder.textTitle.setTypeface(null, Typeface.NORMAL)
                    holder.textTitle.setTextColor(Color.parseColor("#CCFFFFFF"))
                }
            }
            else -> {
                if (selected) {
                    holder.cardCategory.setBackgroundResource(R.drawable.bg_category_selected)
                    holder.textTitle.setTypeface(null, Typeface.BOLD)
                } else {
                    holder.cardCategory.setBackgroundResource(R.drawable.bg_category_normal)
                    holder.textTitle.setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        val targetTextSp = if (selected) 18f else 16f
        val targetEmojiSp = if (selected) 26f else 22f
        val prevSelected = holder.cardCategory.tag as? Boolean ?: false
        holder.cardCategory.tag = selected

        if (prevSelected != selected) {
            animateTextSize(holder.textTitle, holder.textTitle.textSize, targetTextSp)
            animateTextSize(holder.textEmoji, holder.textEmoji.textSize, targetEmojiSp)
        } else {
            holder.textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, targetTextSp)
            holder.textEmoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, targetEmojiSp)
        }
    }

    private fun animateTextSize(view: TextView, fromPx: Float, toSp: Float) {
        val toPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, toSp, view.resources.displayMetrics)
        ValueAnimator.ofFloat(fromPx, toPx).apply {
            duration = 200
            addUpdateListener { view.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.animatedValue as Float) }
            start()
        }
    }
}
