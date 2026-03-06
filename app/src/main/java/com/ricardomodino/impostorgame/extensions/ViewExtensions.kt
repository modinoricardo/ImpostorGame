package com.ricardomodino.impostorgame.extensions

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View

// extraDp es el margen extra en dp que quieres alrededor del botón
fun View.expandTouchArea(extraDp: Int) {
    val parentView = parent as? View ?: return

    parentView.post {
        val rect = Rect()
        // Rect original del botón
        getHitRect(rect)

        // Convertir dp a px
        val extraPx = (extraDp * resources.displayMetrics.density).toInt()

        // Ampliar el rectángulo alrededor
        rect.top -= extraPx
        rect.bottom += extraPx
        rect.left -= extraPx
        rect.right += extraPx

        // Asignar el delegado de toque ampliado
        parentView.touchDelegate = TouchDelegate(rect, this)
    }
}
