package com.example.impostorgame

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditPlayersBottomSheet : BottomSheetDialogFragment() {

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        // Avisar a la Activity
        (activity as? MainActivity)?.onBottomSheetClosed()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // El tema ya no es relevante para la animación, pero puedes dejarlo si quieres
        // setStyle(STYLE_NORMAL, R.style.Theme_EditPlayersBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_players, container, false)
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        // Fondo redondeado como antes
        bottomSheet.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.bottomsheet_rounded
        )

        // ANIMACIÓN DE ENTRADA EXAGERADA PARA VER SI FUNCIONA
        bottomSheet.post {
            // Altura de referencia: si no tiene, usamos la altura de pantalla
            val h = if (bottomSheet.height > 0) {
                bottomSheet.height
            } else {
                bottomSheet.resources.displayMetrics.heightPixels
            }

            // Empezamos MUY abajo y transparente
            bottomSheet.translationY = h.toFloat()      // fuera de pantalla
            bottomSheet.alpha = 0f

            bottomSheet.animate()
                .translationY(0f)                      // sube hasta su posición
                .alpha(1f)                             // fade in
                .setDuration(1500L)                    // 1.5 segundos para que se note
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
    }
}
