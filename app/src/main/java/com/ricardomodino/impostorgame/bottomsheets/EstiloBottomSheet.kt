package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
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
    ): View = inflater.inflate(R.layout.bottomsheet_estilo, container, false)

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return
        bottomSheet.background = ContextCompat.getDrawable(requireContext(), R.drawable.bottomsheet_rounded)
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
        behavior.isDraggable = false
        behavior.isHideable = false
        behavior.peekHeight = bottomSheet.resources.displayMetrics.heightPixels
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBackEstilo)?.setOnClickListener { dismiss() }

        val bgCard = ThemeManager.getBgCard(requireContext())
        val accent = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootEstilo)?.setBackgroundResource(bgCard)
        view.findViewById<TextView>(R.id.txtTituloEstilo)?.setShadowLayer(12f, 0f, 0f, accent)

        val cardClasico = view.findViewById<CardView>(R.id.cardTemaClasico)
        val cardCarmesi = view.findViewById<CardView>(R.id.cardTemaCarmesi)
        val cardJmc     = view.findViewById<CardView>(R.id.cardTemaJmc)
        val checkClasico = view.findViewById<TextView>(R.id.checkTemaClasico)
        val checkCarmesi = view.findViewById<TextView>(R.id.checkTemaCarmesi)
        val checkJmc     = view.findViewById<TextView>(R.id.checkTemaJmc)

        val temaActual = ThemeManager.getTema(requireContext())
        checkClasico.visibility = if (temaActual == ThemeManager.TEMA_CLASICO) View.VISIBLE else View.GONE
        checkCarmesi.visibility = if (temaActual == ThemeManager.TEMA_CARMESI) View.VISIBLE else View.GONE
        checkJmc.visibility     = if (temaActual == ThemeManager.TEMA_JMC)     View.VISIBLE else View.GONE

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
    }
}