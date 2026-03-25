package com.ricardomodino.impostorgame.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ricardomodino.impostorgame.modelos.GameOptions

class SelectGameModeBottomSheet : BottomSheetDialogFragment() {

    interface Listener {
        fun onGameModeConfirmed(opciones: GameOptions)
    }

    companion object {
        const val TAG = "SelectGameModeBottomSheet"
        private const val ARG_OPCIONES = "opciones"

        fun newInstance(opciones: GameOptions): SelectGameModeBottomSheet {
            return SelectGameModeBottomSheet().apply {
                arguments = Bundle().apply { putParcelable(ARG_OPCIONES, opciones) }
            }
        }
    }

    private lateinit var cardClasico: CardView
    private lateinit var cardMisterioso: CardView
    private lateinit var cardDatosCuriosos: CardView
    private lateinit var iconCheckClasico: TextView
    private lateinit var iconCheckMisterioso: TextView
    private lateinit var iconCheckDatosCuriosos: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(
        if (ThemeManager.esFinal(requireContext())) R.layout.bottomsheet_select_game_mode_final
        else R.layout.bottomsheet_select_game_mode,
        container, false
    )

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
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isDraggable = true
        behavior.isHideable = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val opcionesActuales = arguments?.getParcelable<GameOptions>(ARG_OPCIONES) ?: GameOptions()

        cardClasico         = view.findViewById(R.id.cardModoClasico)
        cardMisterioso      = view.findViewById(R.id.cardModoMisterioso)
        cardDatosCuriosos   = view.findViewById(R.id.cardModoDatosCuriosos)
        iconCheckClasico        = view.findViewById(R.id.iconCheckClasico)
        iconCheckMisterioso     = view.findViewById(R.id.iconCheckMisterioso)
        iconCheckDatosCuriosos  = view.findViewById(R.id.iconCheckDatosCuriosos)

        iconCheckClasico.visibility       = if (!opcionesActuales.modoMisterioso && !opcionesActuales.modoDatosCuriosos) View.VISIBLE else View.GONE
        iconCheckMisterioso.visibility    = if (opcionesActuales.modoMisterioso) View.VISIBLE else View.GONE
        iconCheckDatosCuriosos.visibility = if (opcionesActuales.modoDatosCuriosos) View.VISIBLE else View.GONE

        view.findViewById<Button>(R.id.btnConfirmModo)?.visibility = View.GONE

        cardClasico.setOnClickListener {
            (activity as? Listener)?.onGameModeConfirmed(
                opcionesActuales.copy(modoMisterioso = false, modoDatosCuriosos = false)
            )
            dismiss()
        }

        cardMisterioso.setOnClickListener {
            (activity as? Listener)?.onGameModeConfirmed(
                opcionesActuales.copy(modoMisterioso = true, modoDatosCuriosos = false)
            )
            dismiss()
        }

        cardDatosCuriosos.setOnClickListener {
            (activity as? Listener)?.onGameModeConfirmed(
                opcionesActuales.copy(modoMisterioso = false, modoDatosCuriosos = true)
            )
            dismiss()
        }
    }
}