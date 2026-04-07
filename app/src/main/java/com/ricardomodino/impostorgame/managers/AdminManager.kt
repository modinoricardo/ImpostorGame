package com.ricardomodino.impostorgame.managers

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AdminManager {

    private const val PREFS          = "admin_prefs"
    private const val KEY_LOGGED_IN  = "is_admin_logged_in"

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_LOGGED_IN, false)

    fun login(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_LOGGED_IN, true).apply()
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    /** Código válido: "IG" + hora actual (HHmm) invertida. Ej: 13:21 → IG1231 */
    fun isValidCode(input: String): Boolean {
        val time = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())
        return input == "IG${time.reversed()}"
    }
}
