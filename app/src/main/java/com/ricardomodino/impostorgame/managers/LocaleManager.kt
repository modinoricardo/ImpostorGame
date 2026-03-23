package com.ricardomodino.impostorgame.managers

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {

    private const val PREF_NAME = "locale_prefs"
    private const val PREF_KEY  = "app_language"

    /** Idiomas soportados */
    val LANGUAGES = listOf("es", "en", "zh-Hans", "zh-Hant")

    fun getLanguage(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(PREF_KEY, "es") ?: "es"

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(PREF_KEY, language).apply()
    }

    /** Envuelve el Context con el locale guardado. Llamar desde attachBaseContext. */
    fun wrap(base: Context): Context {
        val locale = toLocale(getLanguage(base))
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    fun toLocale(language: String): Locale = when (language) {
        "en"      -> Locale.ENGLISH
        "zh-Hans" -> Locale("zh", "CN")
        "zh-Hant" -> Locale("zh", "TW")
        else      -> Locale("es")
    }

    fun languageLabel(language: String): String = when (language) {
        "en"      -> "🇬🇧  English"
        "zh-Hans" -> "🇨🇳  简体中文"
        "zh-Hant" -> "🇹🇼  繁體中文"
        else      -> "🇪🇸  Español"
    }
}
