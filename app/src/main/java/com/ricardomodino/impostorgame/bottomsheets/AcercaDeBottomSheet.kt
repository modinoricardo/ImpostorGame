package com.ricardomodino.impostorgame.bottomsheets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager

class AcercaDeBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "AcercaDeBottomSheet"
    }

    override val animationDuration: Long = 400L
    override val isDraggableSheet: Boolean = false
    override val isHideableSheet: Boolean = false
    override val expandOnStart: Boolean = true

    override fun onSheetReady(behavior: BottomSheetBehavior<View>) {
        behavior.peekHeight = resources.displayMetrics.heightPixels
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(
        if (ThemeManager.esCarmesi(requireContext())) R.layout.bottomsheet_acerca_de_carmesi
        else if (ThemeManager.esFinal(requireContext())) R.layout.bottomsheet_acerca_de_final
        else R.layout.bottomsheet_acerca_de,
        container, false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Aplicar tema ──
        val bgCard = ThemeManager.getBgCard(requireContext())
        val accent = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootAcercaDe)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext()) -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else -> bgCard
            }
        )
        // Título
        view.findViewById<TextView>(R.id.txtTituloAcercaDe)?.setShadowLayer(12f, 0f, 0f, accent)
        // Avatar inicial "R"
        view.findViewById<TextView>(R.id.txtAvatar)?.apply {
            setTextColor(accent)
            setBackgroundResource(
                when {
                    ThemeManager.esFinal(requireContext()) -> R.drawable.bg_final_avatar
                    ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_badge
                    else -> bgCard
                }
            )
        }
        // Cards de contacto
        listOf(R.id.layoutInstagram, R.id.layoutEmail, R.id.layoutGithub).forEach { id ->
            view.findViewById<LinearLayout>(id)?.setBackgroundResource(
                when {
                    ThemeManager.esFinal(requireContext()) -> R.drawable.bg_final_sheet_option
                    ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_soft_panel
                    else -> bgCard
                }
            )
        }

        view.findViewById<View>(R.id.btnBack)?.setOnClickListener { dismiss() }

        view.findViewById<LinearLayout>(R.id.layoutInstagram).setOnClickListener {
            abrirUrl("https://www.instagram.com/elrichi27")
        }
        view.findViewById<LinearLayout>(R.id.layoutEmail).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:modinoricardo@gmail.com")
            }
            startActivity(Intent.createChooser(intent, getString(R.string.acerca_send_email)))
        }
        view.findViewById<LinearLayout>(R.id.layoutGithub).setOnClickListener {
            abrirUrl("https://github.com/modinoricardo")
        }
    }

    private fun abrirUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
