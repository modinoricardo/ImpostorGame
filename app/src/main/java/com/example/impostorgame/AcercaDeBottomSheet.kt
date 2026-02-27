package com.example.impostorgame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AcercaDeBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "AcercaDeBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_acerca_de, container, false)

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        bottomSheet.background = ContextCompat.getDrawable(requireContext(), R.drawable.bottomsheet_rounded)

        // Expandir al máximo para que el scroll funcione bien
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = true
        behavior.isHideable = true

        bottomSheet.post {
            val h = if (bottomSheet.height > 0) bottomSheet.height
            else bottomSheet.resources.displayMetrics.heightPixels
            bottomSheet.translationY = h.toFloat()
            bottomSheet.alpha = 0f
            bottomSheet.animate()
                .translationY(0f).alpha(1f)
                .setDuration(400L)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instagram
        view.findViewById<LinearLayout>(R.id.layoutInstagram).setOnClickListener {
            abrirUrl("https://www.instagram.com/elrichi27")
        }

        // Email
        view.findViewById<LinearLayout>(R.id.layoutEmail).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:modinoricardo@gmail.com")
            }
            startActivity(Intent.createChooser(intent, "Enviar email"))
        }

        // GitHub
        view.findViewById<LinearLayout>(R.id.layoutGithub).setOnClickListener {
            abrirUrl("https://github.com/modinoricardo")
        }
    }

    private fun abrirUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}