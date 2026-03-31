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
import com.ricardomodino.impostorgame.modelos.GameOptions

class SelectGameModeBottomSheet : BaseGameBottomSheet() {

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
        if (ThemeManager.esCarmesi(requireContext())) R.layout.bottomsheet_select_game_mode_carmesi
        else if (ThemeManager.esFinal(requireContext())) R.layout.bottomsheet_select_game_mode_final
        else R.layout.bottomsheet_select_game_mode,
        container, false
    )

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
