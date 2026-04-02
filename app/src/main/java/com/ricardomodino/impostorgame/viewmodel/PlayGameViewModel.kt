package com.ricardomodino.impostorgame.viewmodel

import androidx.lifecycle.ViewModel
import com.ricardomodino.impostorgame.modelos.DatoCurioso
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador

/**
 * ViewModel de la pantalla de partida en curso.
 * Gestiona el estado del juego (jugadores, palabra, opciones) y la lógica
 * de evaluación de votos. Sobrevive rotaciones de pantalla.
 */
class PlayGameViewModel : ViewModel() {

    /** Resultado de evaluar los votos tras una ronda. */
    sealed class ResultadoVoto {
        object CivilesGanan    : ResultadoVoto()
        object ImpostoresGanan : ResultadoVoto()
        object ContinuaPartida : ResultadoVoto()
    }

    // ── Estado del juego ────────────────────────────────────────────────────

    var listaJugadores: List<Jugador>     = emptyList(); private set
    var palabraJugada: String             = "";           private set
    var nombreImpostor: String            = "";           private set
    var nombresSenoresBlancos: String     = "";           private set
    var modoMisterioso: Boolean           = false;        private set
    var modoDatosCuriosos: Boolean        = false;        private set
    var tiempoLimitado: Boolean           = false;        private set
    var minutos: Int                      = 3;            private set
    var datosPartida: List<DatoCurioso>   = emptyList();  private set

    // ── Estado de runtime (sobrevive rotaciones) ───────────────────────────
    var revelado: Boolean            = false
    var tiempoRestanteMs: Long       = -1L
    var timerActivo: Boolean         = false
    var jugadorEmpiezaNombre: String = ""

    private var inicializado    = false
    private var impostorContado = false

    // ── Inicialización ──────────────────────────────────────────────────────

    /**
     * Carga el estado inicial desde los extras del Intent.
     * Es idempotente: si ya fue inicializado (p. ej. tras rotación), no hace nada.
     */
    fun cargarDesdeIntent(
        jugadores: List<Jugador>,
        datosPartida: List<DatoCurioso>,
        palabraJugada: String,
        nombreImpostor: String,
        nombresSenoresBlancos: String,
        modoMisterioso: Boolean,
        modoDatosCuriosos: Boolean,
        tiempoLimitado: Boolean,
        minutos: Int
    ) {
        if (inicializado) return
        inicializado              = true
        listaJugadores            = jugadores
        this.datosPartida         = datosPartida
        this.palabraJugada        = palabraJugada
        this.nombreImpostor       = nombreImpostor
        this.nombresSenoresBlancos = nombresSenoresBlancos
        this.modoMisterioso       = modoMisterioso
        this.modoDatosCuriosos    = modoDatosCuriosos
        this.tiempoLimitado       = tiempoLimitado
        this.minutos              = minutos
    }

    // ── Lógica de negocio ───────────────────────────────────────────────────

    /**
     * Actualiza la lista de jugadores tras una ronda de votos y determina
     * si la partida tiene ganador o debe continuar.
     */
    fun evaluarVotos(jugadoresActualizados: List<Jugador>): ResultadoVoto {
        listaJugadores = jugadoresActualizados
        val noCiviles = listaJugadores.count {
            it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO
        }
        val civiles = listaJugadores.count { it.tipo == TipoJugador.NORMAL }
        return when {
            noCiviles == 0       -> ResultadoVoto.CivilesGanan
            noCiviles >= civiles -> ResultadoVoto.ImpostoresGanan
            else                 -> ResultadoVoto.ContinuaPartida
        }
    }

    /**
     * Devuelve true la primera vez que se llama (para que la Activity
     * incremente el contador de veces-impostor exactamente una vez).
     */
    fun debeContarImpostor(): Boolean {
        if (impostorContado) return false
        impostorContado = true
        return true
    }
}
