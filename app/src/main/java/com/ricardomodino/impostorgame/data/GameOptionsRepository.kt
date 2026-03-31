package com.ricardomodino.impostorgame.data

import android.content.Context
import com.ricardomodino.impostorgame.modelos.GameOptions

/**
 * Repositorio para persistir y recuperar las opciones de partida en SharedPreferences.
 * Centraliza todas las claves y la lógica de serialización de [GameOptions].
 */
class GameOptionsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun guardar(opciones: GameOptions) {
        prefs.edit().apply {
            putBoolean(KEY_MODO_LOCO, opciones.modoLoco)
            putString(KEY_TIPO_PISTA, opciones.tipoPista)
            putBoolean(KEY_TIEMPO_LIMITADO, opciones.tiempoLimitado)
            putBoolean(KEY_CAMARA_ACTIVA, opciones.camaraActiva)
            putBoolean(KEY_MODO_MISTERIOSO, opciones.modoMisterioso)
            putBoolean(KEY_MODO_DATOS_CURIOSOS, opciones.modoDatosCuriosos)
            putInt(KEY_NUM_IMPOSTORES, opciones.numImpostores)
            putInt(KEY_NUM_SENORES_BLANCOS, opciones.numSenoresBlancos)
            putInt(KEY_MINUTOS, opciones.minutos)
            apply()
        }
    }

    fun restaurar(default: GameOptions = GameOptions()): GameOptions = default.copy(
        modoLoco          = prefs.getBoolean(KEY_MODO_LOCO, false),
        tipoPista         = prefs.getString(KEY_TIPO_PISTA, GameOptions.PISTA_COMPLETA) ?: GameOptions.PISTA_COMPLETA,
        tiempoLimitado    = prefs.getBoolean(KEY_TIEMPO_LIMITADO, false),
        camaraActiva      = prefs.getBoolean(KEY_CAMARA_ACTIVA, false),
        modoMisterioso    = prefs.getBoolean(KEY_MODO_MISTERIOSO, false),
        modoDatosCuriosos = prefs.getBoolean(KEY_MODO_DATOS_CURIOSOS, false),
        numImpostores     = prefs.getInt(KEY_NUM_IMPOSTORES, 1),
        numSenoresBlancos = prefs.getInt(KEY_NUM_SENORES_BLANCOS, 0),
        minutos           = prefs.getInt(KEY_MINUTOS, 3)
    )

    companion object {
        private const val PREFS_NAME             = "opciones"
        private const val KEY_MODO_LOCO          = "modoLoco"
        private const val KEY_TIPO_PISTA         = "tipoPista"
        private const val KEY_TIEMPO_LIMITADO    = "tiempoLimitado"
        private const val KEY_CAMARA_ACTIVA      = "camaraActiva"
        private const val KEY_MODO_MISTERIOSO    = "modoMisterioso"
        private const val KEY_MODO_DATOS_CURIOSOS = "modoDatosCuriosos"
        private const val KEY_NUM_IMPOSTORES     = "numImpostores"
        private const val KEY_NUM_SENORES_BLANCOS = "numSenoresBlancos"
        private const val KEY_MINUTOS            = "minutos"
    }
}
