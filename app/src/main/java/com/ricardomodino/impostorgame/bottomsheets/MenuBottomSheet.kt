package com.ricardomodino.impostorgame.bottomsheets

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.google.android.material.switchmaterial.SwitchMaterial
import com.ricardomodino.impostorgame.managers.SoundManager
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "MenuBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(
        if (ThemeManager.esFinal(requireContext())) R.layout.bottomsheet_menu_final
        else R.layout.bottomsheet_menu,
        container, false
    )

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
        behavior.isDraggable = true
        behavior.isHideable = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Aplicar tema ──
        val bgCard  = ThemeManager.getBgCard(requireContext())
        val accent  = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootBottomSheet)?.setBackgroundResource(bgCard)
        // Título "Menú"
        view.findViewById<TextView>(R.id.txtMenuTitle)?.setShadowLayer(12f, 0f, 0f, accent)
        // Cards internas
        listOf(R.id.cardMenuEstilo, R.id.cardMenuSonido, R.id.cardMenuAcercaDe, R.id.cardMenuSugerencias, R.id.cardMenuIdioma).forEach { cardId ->
            view.findViewById<CardView>(cardId)?.getChildAt(0)?.setBackgroundResource(bgCard)
        }

        // Mostrar idioma actual
        val idiomaActual = LocaleManager.getLanguage(requireContext())
        view.findViewById<TextView>(R.id.txtIdiomaActual)?.text = LocaleManager.languageLabel(idiomaActual)

        val switchSonido = view.findViewById<SwitchMaterial>(R.id.switchSonidoMenu)
        switchSonido?.isChecked = SoundManager.isSoundEnabled(requireContext())
        switchSonido?.setOnCheckedChangeListener { _, checked ->
            SoundManager.setSoundEnabled(requireContext(), checked)
        }

        view.findViewById<CardView>(R.id.cardMenuEstilo).setOnClickListener {
            dismiss()
            EstiloBottomSheet().show(parentFragmentManager, EstiloBottomSheet.TAG)
        }
        view.findViewById<CardView>(R.id.cardMenuAcercaDe).setOnClickListener {
            dismiss()
            AcercaDeBottomSheet().show(parentFragmentManager, AcercaDeBottomSheet.TAG)
        }
        view.findViewById<CardView>(R.id.cardMenuSugerencias).setOnClickListener {
            dismiss()
            SugerenciasBottomSheet().show(parentFragmentManager, SugerenciasBottomSheet.TAG)
        }

        view.findViewById<CardView>(R.id.cardMenuIdioma).setOnClickListener {
            mostrarSelectorIdioma()
        }
    }

    private fun mostrarSelectorIdioma() {
        val idiomas = LocaleManager.LANGUAGES
        val etiquetas = idiomas.map { LocaleManager.languageLabel(it) }.toTypedArray()
        val actual = LocaleManager.getLanguage(requireContext())
        val seleccionado = idiomas.indexOf(actual).coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.menu_idioma))
            .setSingleChoiceItems(etiquetas, seleccionado) { dialog, which ->
                val nuevoIdioma = idiomas[which]
                if (nuevoIdioma != actual) {
                    LocaleManager.setLanguage(requireContext(), nuevoIdioma)
                    dialog.dismiss()
                    dismiss()
                    // Reiniciar la actividad para aplicar el nuevo idioma
                    requireActivity().let { act ->
                        val intent = act.intent
                        act.finish()
                        act.startActivity(intent)
                        act.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                } else {
                    dialog.dismiss()
                }
            }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }
}