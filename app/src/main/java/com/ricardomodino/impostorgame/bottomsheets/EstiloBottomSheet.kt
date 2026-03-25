package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EstiloBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "EstiloBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(
        if (ThemeManager.esFinal(requireContext())) R.layout.bottomsheet_estilo_final
        else R.layout.bottomsheet_estilo,
        container, false
    )

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return
        val backgroundRes = if (ThemeManager.esFinal(requireContext())) {
            R.drawable.bottomsheet_rounded_final
        } else {
            R.drawable.bottomsheet_rounded
        }
        bottomSheet.background = ContextCompat.getDrawable(requireContext(), backgroundRes)
        bottomSheet.post {
            val h = if (bottomSheet.height > 0) bottomSheet.height
            else bottomSheet.resources.displayMetrics.heightPixels
            bottomSheet.translationY = h.toFloat()
            bottomSheet.alpha = 0f
            bottomSheet.animate().translationY(0f).alpha(1f)
                .setDuration(400L).setInterpolator(DecelerateInterpolator(2f)).start()
        }
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = true
        behavior.isHideable = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBackEstilo)?.setOnClickListener { dismiss() }

        val cardClasico = view.findViewById<CardView>(R.id.cardTemaClasico)
        val cardCarmesi = view.findViewById<CardView>(R.id.cardTemaCarmesi)
        val cardJmc     = view.findViewById<CardView>(R.id.cardTemaJmc)
        val cardFinal   = view.findViewById<CardView>(R.id.cardTemaFinal)
        val checkClasico = view.findViewById<TextView>(R.id.checkTemaClasico)
        val checkCarmesi = view.findViewById<TextView>(R.id.checkTemaCarmesi)
        val checkJmc     = view.findViewById<TextView>(R.id.checkTemaJmc)
        val checkFinal   = view.findViewById<TextView>(R.id.checkTemaFinal)

        val temaActual = ThemeManager.getTema(requireContext())
        if (ThemeManager.esFinal(requireContext())) {
            val accentWarm = 0xFFE7C87D.toInt()
            view.findViewById<View>(R.id.rootEstilo)?.setBackgroundResource(R.drawable.bg_estilo_final_sheet)
            view.findViewById<TextView>(R.id.txtTituloEstilo)?.apply {
                setTextColor(0xFFFFF3D8.toInt())
                setShadowLayer(18f, 0f, 0f, 0x66E7C87D)
            }
            view.findViewById<ImageView>(R.id.btnBackEstilo)?.setColorFilter(accentWarm)

            aplicarFondoFinal(cardClasico, temaActual == ThemeManager.TEMA_CLASICO)
            aplicarFondoFinal(cardCarmesi, temaActual == ThemeManager.TEMA_CARMESI)
            aplicarFondoFinal(cardJmc, temaActual == ThemeManager.TEMA_JMC)
            aplicarFondoFinal(cardFinal, temaActual == ThemeManager.TEMA_FINAL, featured = true)
        } else {
            val bgCard = ThemeManager.getBgCard(requireContext())
            val accent = ThemeManager.getAccentColor(requireContext())
            view.findViewById<View>(R.id.rootEstilo)?.setBackgroundResource(bgCard)
            view.findViewById<TextView>(R.id.txtTituloEstilo)?.setShadowLayer(12f, 0f, 0f, accent)
        }

        checkClasico.visibility = if (temaActual == ThemeManager.TEMA_CLASICO) View.VISIBLE else View.GONE
        checkCarmesi.visibility = if (temaActual == ThemeManager.TEMA_CARMESI) View.VISIBLE else View.GONE
        checkJmc.visibility     = if (temaActual == ThemeManager.TEMA_JMC)     View.VISIBLE else View.GONE
        checkFinal.visibility   = if (temaActual == ThemeManager.TEMA_FINAL)   View.VISIBLE else View.GONE

        cardClasico.setOnClickListener {
            if (ThemeManager.getTema(requireContext()) != ThemeManager.TEMA_CLASICO) {
                ThemeManager.setTema(requireContext(), ThemeManager.TEMA_CLASICO)
                dismiss(); activity?.recreate()
            }
        }
        cardCarmesi.setOnClickListener {
            if (ThemeManager.getTema(requireContext()) != ThemeManager.TEMA_CARMESI) {
                ThemeManager.setTema(requireContext(), ThemeManager.TEMA_CARMESI)
                dismiss(); activity?.recreate()
            }
        }
        cardJmc.setOnClickListener {
            if (ThemeManager.getTema(requireContext()) != ThemeManager.TEMA_JMC) {
                ThemeManager.setTema(requireContext(), ThemeManager.TEMA_JMC)
                dismiss(); activity?.recreate()
            }
        }
        cardFinal.setOnClickListener {
            if (ThemeManager.getTema(requireContext()) != ThemeManager.TEMA_FINAL) {
                ThemeManager.setTema(requireContext(), ThemeManager.TEMA_FINAL)
                dismiss(); activity?.recreate()
            }
        }
    }

    private fun aplicarFondoFinal(card: CardView, selected: Boolean, featured: Boolean = false) {
        val child = card.getChildAt(0) ?: return
        val backgroundRes = when {
            selected -> R.drawable.bg_estilo_final_option_selected
            featured -> R.drawable.bg_estilo_final_option_featured
            else -> R.drawable.bg_estilo_final_option
        }
        child.setBackgroundResource(backgroundRes)
    }
}
