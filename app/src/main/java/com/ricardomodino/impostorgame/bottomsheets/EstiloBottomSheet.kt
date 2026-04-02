package com.ricardomodino.impostorgame.bottomsheets

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager

class EstiloBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "EstiloBottomSheet"
    }

    override val animationDuration: Long = 400L
    override val expandOnStart: Boolean = true

    override fun provideSheetBackground(): Drawable? {
        val res = when {
            ThemeManager.esFinal(requireContext())   -> R.drawable.bottomsheet_rounded_final
            ThemeManager.esCarmesi(requireContext()) -> R.drawable.bottomsheet_rounded_carmesi
            else                                     -> R.drawable.bottomsheet_rounded
        }
        return ContextCompat.getDrawable(requireContext(), res)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutRes = when {
            ThemeManager.esFinal(requireContext()) -> R.layout.bottomsheet_estilo_final
            ThemeManager.esCarmesi(requireContext()) -> R.layout.bottomsheet_estilo_carmesi
            else -> R.layout.bottomsheet_estilo
        }
        return inflater.inflate(layoutRes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBackEstilo)?.setOnClickListener { dismiss() }

        val cardClasico  = view.findViewById<CardView>(R.id.cardTemaClasico)
        val cardCarmesi  = view.findViewById<CardView>(R.id.cardTemaCarmesi)
        val cardJmc      = view.findViewById<CardView>(R.id.cardTemaJmc)
        val cardFinal    = view.findViewById<CardView>(R.id.cardTemaFinal)
        val checkClasico = view.findViewById<TextView>(R.id.checkTemaClasico)
        val checkCarmesi = view.findViewById<TextView>(R.id.checkTemaCarmesi)
        val checkJmc     = view.findViewById<TextView>(R.id.checkTemaJmc)
        val checkFinal   = view.findViewById<TextView>(R.id.checkTemaFinal)

        val temaActual = ThemeManager.getTema(requireContext())

        // JMC sigue soportado internamente, pero queda oculto en el selector por ahora.
        cardJmc.visibility = View.GONE
        checkJmc.visibility = View.GONE

        // Estilo visual del sheet según el tema activo (solo afecta al sheet, nunca a las cards)
        if (ThemeManager.esFinal(requireContext())) {
            view.findViewById<View>(R.id.rootEstilo)?.setBackgroundResource(R.drawable.bg_estilo_final_sheet)
            view.findViewById<TextView>(R.id.txtTituloEstilo)?.apply {
                setTextColor(0xFFFFFFFF.toInt())
                setShadowLayer(18f, 0f, 0f, 0x6600E5FF)
            }
            view.findViewById<ImageView>(R.id.btnBackEstilo)?.setColorFilter(0xFF9CEFFF.toInt())
        } else if (ThemeManager.esCarmesi(requireContext())) {
            view.findViewById<View>(R.id.rootEstilo)?.setBackgroundResource(R.drawable.bg_carmesi_sheet)
            view.findViewById<TextView>(R.id.txtTituloEstilo)?.apply {
                setTextColor(0xFFFFF3F6.toInt())
                setShadowLayer(16f, 0f, 0f, 0x66D92C58)
            }
            view.findViewById<ImageView>(R.id.btnBackEstilo)?.setColorFilter(0xFFF0D0D7.toInt())
        } else {
            val bgCard = ThemeManager.getBgCard(requireContext())
            val accent = ThemeManager.getAccentColor(requireContext())
            view.findViewById<View>(R.id.rootEstilo)?.setBackgroundResource(bgCard)
            view.findViewById<TextView>(R.id.txtTituloEstilo)?.setShadowLayer(12f, 0f, 0f, accent)
        }

        checkClasico.visibility = if (temaActual == ThemeManager.TEMA_CLASICO) View.VISIBLE else View.GONE
        checkCarmesi.visibility = if (temaActual == ThemeManager.TEMA_CARMESI) View.VISIBLE else View.GONE
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
        cardFinal.setOnClickListener {
            if (ThemeManager.getTema(requireContext()) != ThemeManager.TEMA_FINAL) {
                ThemeManager.setTema(requireContext(), ThemeManager.TEMA_FINAL)
                dismiss(); activity?.recreate()
            }
        }
    }
}
