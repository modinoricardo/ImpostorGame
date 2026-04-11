package com.ricardomodino.impostorgame.bottomsheets

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.app.AlertDialog
import android.widget.EditText
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.AdminManager
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager

class MenuBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG = "MenuBottomSheet"
    }

    override val animationDuration: Long = 400L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(
        when {
            ThemeManager.esCarmesi(requireContext())      -> R.layout.bottomsheet_menu_carmesi
            ThemeManager.esFinal(requireContext())        -> R.layout.bottomsheet_menu_final
            ThemeManager.esDeepTerminal(requireContext()) -> R.layout.bottomsheet_menu_deep_terminal
            else -> R.layout.bottomsheet_menu
        },
        container, false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Aplicar tema ──
        val bgCard  = ThemeManager.getBgCard(requireContext())
        val accent  = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootBottomSheet)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext())        -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext())      -> R.drawable.bg_carmesi_sheet
                ThemeManager.esDeepTerminal(requireContext()) -> R.drawable.bg_deep_terminal_sheet
                else -> bgCard
            }
        )
        // Título "Menú"
        view.findViewById<TextView>(R.id.txtMenuTitle)?.setShadowLayer(12f, 0f, 0f, accent)
        // Cards internas
        listOf(R.id.cardMenuEstilo, R.id.cardMenuSonido, R.id.cardMenuAcercaDe, R.id.cardMenuSugerencias, R.id.cardMenuIdioma, R.id.cardMenuContenido, R.id.cardMenuAdmin).forEach { cardId ->
            view.findViewById<CardView>(cardId)?.getChildAt(0)?.setBackgroundResource(
                if (ThemeManager.esFinal(requireContext())) R.drawable.bg_final_sheet_option else bgCard
            )
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

        view.findViewById<CardView>(R.id.cardMenuContenido)?.setOnClickListener {
            dismiss()
            MiContenidoBottomSheet().show(parentFragmentManager, MiContenidoBottomSheet.TAG)
        }

        view.findViewById<CardView>(R.id.cardMenuAdmin)?.setOnClickListener {
            if (AdminManager.isLoggedIn(requireContext())) {
                dismiss()
                AdminBottomSheet().show(parentFragmentManager, AdminBottomSheet.TAG)
            } else {
                mostrarDialogoCodigo()
            }
        }
    }

    private fun mostrarDialogoCodigo() {
        val editCodigo = EditText(requireContext()).apply {
            hint = getString(R.string.admin_codigo_hint)
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setPadding(48, 32, 48, 16)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_codigo_titulo))
            .setView(editCodigo)
            .setPositiveButton(getString(R.string.admin_acceder)) { _, _ ->
                val codigo = editCodigo.text.toString().trim()
                if (AdminManager.isValidCode(codigo)) {
                    AdminManager.login(requireContext())
                    dismiss()
                    AdminBottomSheet().show(parentFragmentManager, AdminBottomSheet.TAG)
                } else {
                    android.widget.Toast.makeText(
                        requireContext(), getString(R.string.admin_codigo_incorrecto), android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_salir_no), null)
            .show()
    }

    private fun mostrarSelectorIdioma() {
        val idiomas = LocaleManager.LANGUAGES
        val etiquetas = idiomas.map { LocaleManager.languageLabel(it) }.toTypedArray()
        val actual = LocaleManager.getLanguage(requireContext())
        val seleccionado = idiomas.indexOf(actual).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
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
