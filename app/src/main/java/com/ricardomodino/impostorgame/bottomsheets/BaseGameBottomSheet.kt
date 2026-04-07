package com.ricardomodino.impostorgame.bottomsheets

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import com.ricardomodino.impostorgame.managers.ThemeManager

/**
 * Clase base para todos los BottomSheetDialogFragment del juego.
 * Centraliza la configuración de inmersión, fondo, animación de entrada y behavior.
 * Las subclases solo sobreescriben las propiedades que difieran del comportamiento estándar.
 */
abstract class BaseGameBottomSheet : BottomSheetDialogFragment() {

    /** Duración de la animación de entrada en milisegundos. */
    protected open val animationDuration: Long = 550L

    /** Si el panel puede arrastrarse para cerrarse (default: true). */
    protected open val isDraggableSheet: Boolean = true

    /** Si el panel puede ocultarse deslizando hacia abajo (default: true). */
    protected open val isHideableSheet: Boolean = true

    /** Si el panel debe expandirse completamente al iniciarse (default: false). */
    protected open val expandOnStart: Boolean = false

    /** Si el panel contiene campos de texto y necesita subir con el teclado (default: false). */
    protected open val expandForKeyboard: Boolean = false

    /**
     * Drawable de fondo del panel.
     * Por defecto usa bottomsheet_rounded. Sobreescribir para fondo dinámico por tema.
     */
    protected open fun provideSheetBackground(): Drawable? =
        ContextCompat.getDrawable(
            requireContext(),
            if (ThemeManager.esCarmesi(requireContext())) R.drawable.bottomsheet_rounded_carmesi
            else R.drawable.bottomsheet_rounded
        )

    /**
     * Hook llamado justo antes de la animación de entrada, con el behavior ya configurado.
     * Úsalo para ajustes adicionales como peekHeight o STATE_EXPANDED condicional.
     */
    protected open fun onSheetReady(behavior: BottomSheetBehavior<View>) {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).also { dialog ->
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

    override fun getTheme(): Int = R.style.Theme_EditPlayersBottomSheet

    override fun onStart() {
        super.onStart()
        ImmersiveModeManager.apply(dialog?.window)

        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val behavior = ImmersiveModeManager.prepareBottomSheet(bottomSheet, provideSheetBackground())
        behavior.isDraggable = isDraggableSheet
        behavior.isHideable  = isHideableSheet
        if (expandOnStart) {
            behavior.state         = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
        onSheetReady(behavior)

        // Cuando el teclado aparece, añadir padding inferior igual a su altura
        // para que el contenido suba y el campo enfocado quede visible
        if (expandForKeyboard) {
            val contentView = (bottomSheet as? android.view.ViewGroup)?.getChildAt(0) ?: return
            val basePaddingBottom = contentView.paddingBottom
            ViewCompat.setOnApplyWindowInsetsListener(contentView) { v, insets ->
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, basePaddingBottom + imeHeight)
                insets
            }
        }

        bottomSheet.post {
            val h = if (bottomSheet.height > 0) bottomSheet.height
                    else bottomSheet.resources.displayMetrics.heightPixels
            bottomSheet.translationY = h.toFloat()
            bottomSheet.alpha = 0f
            bottomSheet.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(animationDuration)
                .setInterpolator(DecelerateInterpolator(2f))
                .start()
        }
    }
}
