package com.ricardomodino.impostorgame.managers

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ricardomodino.impostorgame.R
import kotlin.math.max

object ImmersiveModeManager {

    fun apply(activity: Activity) {
        apply(activity.window)
        if (ThemeManager.esCarmesi(activity)) {
            activity.window.setBackgroundDrawableResource(R.drawable.bg_neon_space_red)
        }
    }

    fun apply(window: Window?) {
        val safeWindow = window ?: return

        WindowCompat.setDecorFitsSystemWindows(safeWindow, false)
        safeWindow.statusBarColor = Color.TRANSPARENT
        safeWindow.navigationBarColor = Color.TRANSPARENT
        safeWindow.setBackgroundDrawableResource(android.R.color.transparent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            safeWindow.isStatusBarContrastEnforced = false
            safeWindow.isNavigationBarContrastEnforced = false
        }

        WindowInsetsControllerCompat(safeWindow, safeWindow.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun applyActivityContentInsets(
        activity: Activity,
        includeBottomInset: Boolean = false,
        extraTop: Int = 0,
        extraBottom: Int = 0
    ) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
        val root = content.getChildAt(0) ?: return
        applyRootInsets(root, includeBottomInset = includeBottomInset, extraTop = extraTop, extraBottom = extraBottom)
    }

    fun applyRootInsets(
        root: View,
        includeBottomInset: Boolean = false,
        includeTopInset: Boolean = true,
        extraTop: Int = 0,
        extraBottom: Int = 0
    ) {
        val baseLeft = root.paddingLeft
        val baseTop = root.paddingTop
        val baseRight = root.paddingRight
        val baseBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val sideInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.displayCutout()
            )
            val topInset = if (includeTopInset) {
                max(
                    insets.getInsets(WindowInsetsCompat.Type.displayCutout()).top,
                    insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                )
            } else {
                0
            }
            val bottomInset = if (includeBottomInset) {
                insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures()).bottom
            } else {
                0
            }

            view.updatePadding(
                left = baseLeft + sideInsets.left,
                top = baseTop + topInset + extraTop,
                right = baseRight + sideInsets.right,
                bottom = baseBottom + bottomInset + extraBottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    fun applyBottomMargin(view: View, extraBottom: Int = 0) {
        val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        val baseBottom = layoutParams.bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(view) { currentView, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures()).bottom
            (currentView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                params.bottomMargin = baseBottom + bottomInset + extraBottom
                currentView.layoutParams = params
            }
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    fun prepareBottomSheet(bottomSheet: View, background: Drawable?): BottomSheetBehavior<View> {
        bottomSheet.background = background
        bottomSheet.backgroundTintList = null
        bottomSheet.setPadding(0, 0, 0, 0)
        (bottomSheet.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)

        return BottomSheetBehavior.from(bottomSheet).apply {
            isDraggable = true
            isHideable = true
            isGestureInsetBottomIgnored = true
        }
    }
}
