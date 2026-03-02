package com.example.impostorgame.managers

import android.content.Context

object SoundManager {
    private const val PREFS = "impostor_prefs"
    private const val KEY   = "sonido_activo"

    fun isSoundEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true)

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY, enabled).apply()
    }
}