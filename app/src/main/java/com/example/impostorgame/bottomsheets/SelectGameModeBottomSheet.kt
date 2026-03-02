package com.example.impostorgame.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.impostorgame.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.impostorgame.modelos.GameOptions

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

    private var modoMisteriosoSeleccionado = false

    private lateinit var cardClasico: CardView
    private lateinit var cardMisterioso: CardView
    private lateinit var iconCheckClasico: TextView
    private lateinit var iconCheckMisterioso: TextView
    private lateinit var btnConfirm: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_select_game_mode, container, false)

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
                .setDuration(1500L)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isDraggable = false
        behavior.isHideable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val opcionesActuales = arguments?.getParcelable<GameOptions>(ARG_OPCIONES) ?: GameOptions()
        modoMisteriosoSeleccionado = opcionesActuales.modoMisterioso

        cardClasico = view.findViewById(R.id.cardModoClasico)
        cardMisterioso = view.findViewById(R.id.cardModoMisterioso)
        iconCheckClasico = view.findViewById(R.id.iconCheckClasico)
        iconCheckMisterioso = view.findViewById(R.id.iconCheckMisterioso)
        btnConfirm = view.findViewById(R.id.btnConfirmModo)

        actualizarSeleccion()

        cardClasico.setOnClickListener {
            modoMisteriosoSeleccionado = false
            actualizarSeleccion()
        }

        cardMisterioso.setOnClickListener {
            modoMisteriosoSeleccionado = true
            actualizarSeleccion()
        }

        btnConfirm.setOnClickListener {
            val nuevasOpciones = opcionesActuales.copy(modoMisterioso = modoMisteriosoSeleccionado)
            (activity as? Listener)?.onGameModeConfirmed(nuevasOpciones)
            dismiss()
        }
    }

    private fun actualizarSeleccion() {
        iconCheckClasico.visibility = if (!modoMisteriosoSeleccionado) View.VISIBLE else View.GONE
        iconCheckMisterioso.visibility = if (modoMisteriosoSeleccionado) View.VISIBLE else View.GONE
    }
}