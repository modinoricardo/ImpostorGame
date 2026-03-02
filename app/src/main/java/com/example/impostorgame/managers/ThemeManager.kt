package com.example.impostorgame.managers

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.impostorgame.R

object ThemeManager {

    private const val PREFS_NAME = "impostor_prefs"
    private const val KEY_THEME = "selected_theme"

    const val TEMA_CLASICO = "clasico"
    const val TEMA_CARMESI = "carmesi"
    const val TEMA_JMC = "jmc"

    fun getTema(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, TEMA_CLASICO) ?: TEMA_CLASICO
    }

    fun setTema(context: Context, tema: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, tema).apply()
    }

    fun aplicarTema(activity: Activity) {
        val themeRes = when (getTema(activity)) {
            TEMA_CARMESI -> R.style.Theme_ImpostorGame_Carmesi
            TEMA_JMC     -> R.style.Theme_ImpostorGame_Jmc
            else -> R.style.Theme_ImpostorGame
        }
        activity.setTheme(themeRes)
    }

    fun esCarmesi(context: Context) = getTema(context) == TEMA_CARMESI
    fun esJmc(context: Context) = getTema(context) == TEMA_JMC

    fun getBgMain(context: Context) = when (getTema(context)) {
        TEMA_CARMESI -> R.drawable.bg_neon_space_red
        TEMA_JMC     -> R.drawable.bg_neon_space_jmc
        else         -> R.drawable.bg_neon_space
    }
    fun getBtnNext(context: Context) = when (getTema(context)) {
        TEMA_CARMESI -> R.drawable.btn_neon_red
        TEMA_JMC     -> R.drawable.btn_neon_jmc
        else         -> R.drawable.btn_neon
    }
    fun getBgCard(context: Context) = when (getTema(context)) {
        TEMA_CARMESI -> R.drawable.bg_card_main_red
        TEMA_JMC     -> R.drawable.bg_card_main_jmc
        else         -> R.drawable.bg_card_main
    }
    fun getBgChip(context: Context) = when (getTema(context)) {
        TEMA_CARMESI -> R.drawable.bg_chip_carmesi
        TEMA_JMC     -> R.drawable.bg_chip_jmc
        else         -> R.drawable.bg_card_main
    }
    fun getBtnNeon(context: Context) = when (getTema(context)) {
        TEMA_CARMESI -> R.drawable.btn_neon_red
        TEMA_JMC     -> R.drawable.btn_neon_jmc
        else         -> R.drawable.btn_neon
    }
    fun getRingNeon(context: Context) = when (getTema(context)) {
        TEMA_CARMESI -> R.drawable.ring_neon_red
        TEMA_JMC     -> R.drawable.ring_neon_jmc
        else         -> R.drawable.ring_neon
    }
    fun getRingSoft(context: Context) = when (getTema(context)) {
        TEMA_CARMESI -> R.drawable.ring_neon_red_soft
        TEMA_JMC     -> R.drawable.ring_neon_jmc_soft
        else         -> R.drawable.ring_neon_soft
    }
    fun getAccentColor(context: Context) = when (getTema(context)) {
        TEMA_CARMESI -> 0xFFFF1744.toInt()
        TEMA_JMC     -> 0xFF00C853.toInt()
        else         -> 0xFF00E5FF.toInt()
    }

    // Aplica el tema a todas las vistas comunes de una Activity
    fun aplicarDrawables(activity: Activity) {
        val bgMain    = getBgMain(activity)
        val bgCard    = getBgCard(activity)
        val btnNeon   = getBtnNeon(activity)
        val ringNeon  = getRingNeon(activity)
        val ringSoft  = getRingSoft(activity)
        val accent    = getAccentColor(activity)

        // Fondo raíz
        activity.findViewById<View>(R.id.main)?.setBackgroundResource(bgMain)

        // Rings
        activity.findViewById<View>(R.id.ring1)?.setBackgroundResource(ringNeon)
        activity.findViewById<View>(R.id.ring2)?.setBackgroundResource(ringSoft)

        // Todos los botones Button con btn_neon
        applyToAllButtons(activity.window.decorView, btnNeon)

        // Todas las cards: cambiar el fondo del primer hijo
        applyToAllCards(activity.window.decorView, bgCard)

        // Sombra del título si existe
        activity.findViewById<TextView>(R.id.titleTextView)
            ?.setShadowLayer(14f, 0f, 0f, accent)
        activity.findViewById<TextView>(R.id.txtTitle)
            ?.setShadowLayer(16f, 0f, 0f, accent)
        activity.findViewById<TextView>(R.id.txtSubtitle)
            ?.setShadowLayer(10f, 0f, 0f, accent and 0x80FFFFFF.toInt())
        activity.findViewById<TextView>(R.id.titleTextView)
            ?.setShadowLayer(14f, 0f, 0f, accent)
        activity.findViewById<TextView>(R.id.txtTitle)
            ?.setShadowLayer(16f, 0f, 0f, accent)
        activity.findViewById<TextView>(R.id.txtSubtitle)
            ?.setShadowLayer(10f, 0f, 0f, accent and 0x80FFFFFF.toInt())
    }

    // Aplica bgCard a todos los TextView/View con bg_card_main (chips de jugadores y categorías)
    fun aplicarBgChip(view: View, context: Context) {
        view.setBackgroundResource(getBgCard(context))
    }

    private fun applyToAllButtons(root: View, btnRes: Int) {
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) applyToAllButtons(root.getChildAt(i), btnRes)
        } else if (root is Button) {
            root.setBackgroundResource(btnRes)
        }
        // También TextViews usados como botones +/-
        if (root is TextView && (root.text == "+" || root.text == "−")) {
            root.setBackgroundResource(btnRes)
        }
    }

    private fun applyToAllCards(root: View, bgCard: Int) {
        if (root is CardView) {
            val child = root.getChildAt(0)
            child?.setBackgroundResource(bgCard)
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) applyToAllCards(root.getChildAt(i), bgCard)
        }
    }
}