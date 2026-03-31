package com.ricardomodino.impostorgame.activities

/**
 * Constantes para las claves de Intent usadas entre activities.
 * Centraliza los strings para evitar typos silenciosos y facilitar refactorizaciones.
 */
object IntentKeys {
    // Jugadores y partida
    const val PLAYERS             = "PLAYERS"
    const val CATEGORIES          = "CATEGORIES"
    const val OPCIONES            = "OPCIONES"
    const val LISTA_JUGADORES     = "LISTA_JUGADORES"
    const val LISTA_CATEGORIAS    = "LISTA_CATEGORIAS"
    const val DATOS_PARTIDA       = "DATOS_PARTIDA"
    const val JUGADOR_EMPIEZA     = "JUGADOR_EMPIEZA"

    // Roles
    const val IMPOSTOR            = "IMPOSTOR"
    const val SENORES_BLANCOS     = "SENORES_BLANCOS"

    // Contenido
    const val PALABRA             = "PALABRA"

    // Modos de juego
    const val MODO_MISTERIOSO     = "MODO_MISTERIOSO"
    const val MODO_DATOS_CURIOSOS = "MODO_DATOS_CURIOSOS"
    const val MODO_LOCO           = "MODO_LOCO"

    // Tiempo
    const val TIEMPO_LIMITADO     = "TIEMPO_LIMITADO"
    const val MINUTOS             = "MINUTOS"

    // Victoria
    const val GANADOR             = "GANADOR"
    const val MOTIVO              = "MOTIVO"
    const val IR_A_REVEAL         = "IR_A_REVEAL"
    const val VICTORIA_INMEDIATA  = "VICTORIA_INMEDIATA"

    // Votación / GuessWord
    const val JUGADORES              = "JUGADORES"
    const val NOMBRE_VOTADO          = "NOMBRE_VOTADO"
    const val TIPO_VOTADO            = "TIPO_VOTADO"
    const val JUGADORES_ACTUALIZADOS = "JUGADORES_ACTUALIZADOS"
}
