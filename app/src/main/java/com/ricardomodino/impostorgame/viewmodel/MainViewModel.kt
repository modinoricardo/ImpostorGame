package com.ricardomodino.impostorgame.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ricardomodino.impostorgame.data.GameOptionsRepository
import com.ricardomodino.impostorgame.modelos.GameOptions
import com.ricardomodino.impostorgame.R

/** Resultado de un ajuste automático de no-civiles al cambiar el número de jugadores. */
data class AjusteNoCiviles(
    val impostoresAntes: Int,
    val impostoresDespues: Int,
    val blancosAntes: Int,
    val blancosDespues: Int,
    val modoMisterioso: Boolean
) {
    val huboCambio: Boolean
        get() = impostoresAntes != impostoresDespues || blancosAntes != blancosDespues
}

/** Eventos únicos que la Activity debe consumir (Toasts, Navegación, etc.) */
sealed class MainEvent {
    data class MostrarAjuste(val ajuste: AjusteNoCiviles) : MainEvent()
}

/**
 * ViewModel de la pantalla principal. Gestiona el estado de [GameOptions] y
 * centraliza toda la lógica de validación y cálculo de roles.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = GameOptionsRepository(application)

    private val _opciones = MutableLiveData(repo.restaurar())
    val opciones: LiveData<GameOptions> = _opciones

    private val _eventos = MutableLiveData<MainEvent?>()
    val eventos: LiveData<MainEvent?> = _eventos

    val opcionesActuales: GameOptions get() = _opciones.value ?: GameOptions()

    fun consumirEvento() { _eventos.value = null }

    // ── Mutaciones ──────────────────────────────────────────────────────────

    fun actualizarOpciones(nuevas: GameOptions) {
        if (nuevas != _opciones.value) _opciones.value = nuevas
    }

    fun confirmarModoJuego(nuevasOpciones: GameOptions, numJugadores: Int) {
        var opts = nuevasOpciones
        if (!opts.modoMisterioso) opts = opts.copy(numSenoresBlancos = 0)
        if (opts.modoMisterioso || opts.modoDatosCuriosos) opts = opts.copy(modoLoco = false)
        if (!esConfiguracionValida(numJugadores, opts)) opts = opts.copy(numImpostores = 1, numSenoresBlancos = 0)
        actualizarOpciones(opts)
    }

    fun incrementarImpostores(numJugadores: Int) {
        val opts = opcionesActuales
        val nuevo = opts.numImpostores + 1
        if (nuevo <= maxImpostoresPermitidos(numJugadores)) {
            actualizarOpciones(opts.copy(numImpostores = nuevo).ajustarALimites(numJugadores))
        } else if (opts.modoMisterioso && opts.numSenoresBlancos > 0) {
            actualizarOpciones(opts.copy(
                numImpostores = nuevo,
                numSenoresBlancos = opts.numSenoresBlancos - 1
            ).ajustarALimites(numJugadores))
        }
    }

    fun decrementarImpostores(numJugadores: Int) {
        val opts = opcionesActuales
        val nuevo = opts.numImpostores - 1
        val minimo = if (opts.modoMisterioso) 0 else 1
        if (nuevo >= minimo) {
            val blancos = if (opts.modoMisterioso && nuevo == 0 && opts.numSenoresBlancos == 0) 1
                          else opts.numSenoresBlancos
            actualizarOpciones(opts.copy(numImpostores = nuevo, numSenoresBlancos = blancos).ajustarALimites(numJugadores))
        }
    }

    fun incrementarBlancos(numJugadores: Int) {
        val opts = opcionesActuales
        val nuevo = opts.numSenoresBlancos + 1
        if (nuevo <= maxBlancosPermitidos(numJugadores)) {
            actualizarOpciones(opts.copy(numSenoresBlancos = nuevo).ajustarALimites(numJugadores))
        } else if (opts.modoMisterioso && opts.numImpostores > 0) {
            actualizarOpciones(opts.copy(
                numImpostores = opts.numImpostores - 1,
                numSenoresBlancos = nuevo
            ).ajustarALimites(numJugadores))
        }
    }

    fun decrementarBlancos(numJugadores: Int) {
        val opts = opcionesActuales
        val nuevo = opts.numSenoresBlancos - 1
        if (nuevo >= 0) {
            val impostores = if (nuevo == 0 && opts.numImpostores == 0) 1 else opts.numImpostores
            actualizarOpciones(opts.copy(numSenoresBlancos = nuevo, numImpostores = impostores).ajustarALimites(numJugadores))
        }
    }

    fun guardar() = repo.guardar(opcionesActuales)

    fun ajustarNoCivilesSiNecesario(numJugadores: Int) {
        val opts = opcionesActuales
        val max = maxNoCiviles(numJugadores)
        val blancosActuales = if (opts.modoMisterioso) opts.numSenoresBlancos else 0
        val impActuales = opts.numImpostores
        val total = impActuales + blancosActuales

        if (total <= max) return

        val exceso = total - max
        val blancosReducir = minOf(exceso, blancosActuales)
        val blancosNuevos = blancosActuales - blancosReducir
        val excesoRestante = exceso - blancosReducir
        val impNuevos = (impActuales - excesoRestante).coerceAtLeast(1)

        val nuevas = opts.copy(
            numImpostores = impNuevos,
            numSenoresBlancos = if (opts.modoMisterioso) blancosNuevos else 0
        )
        actualizarOpciones(nuevas)

        val ajuste = AjusteNoCiviles(
            impostoresAntes = impActuales,
            impostoresDespues = impNuevos,
            blancosAntes = blancosActuales,
            blancosDespues = blancosNuevos,
            modoMisterioso = opts.modoMisterioso
        )
        if (ajuste.huboCambio) {
            _eventos.value = MainEvent.MostrarAjuste(ajuste)
        }
    }

    // ── Cálculos puros ──────────────────────────────────────────────────────

    fun civiles(numJugadores: Int, opts: GameOptions = opcionesActuales): Int =
        numJugadores - opts.numImpostores - if (opts.modoMisterioso) opts.numSenoresBlancos else 0

    fun esConfiguracionValida(numJugadores: Int, opts: GameOptions = opcionesActuales): Boolean {
        val noCiviles = opts.numImpostores + if (opts.modoMisterioso) opts.numSenoresBlancos else 0
        val hayAlMenosUnNoCivil = if (opts.modoMisterioso) (opts.numImpostores + opts.numSenoresBlancos) > 0
                                  else opts.numImpostores > 0
        return hayAlMenosUnNoCivil && noCiviles <= (numJugadores - 1) / 2
    }

    fun maxNoCiviles(numJugadores: Int): Int = (numJugadores - 1) / 2

    fun maxImpostoresPermitidos(numJugadores: Int, opts: GameOptions = opcionesActuales): Int {
        val blancos = if (opts.modoMisterioso) opts.numSenoresBlancos else 0
        return (maxNoCiviles(numJugadores) - blancos).coerceAtLeast(0)
    }

    fun maxBlancosPermitidos(numJugadores: Int, opts: GameOptions = opcionesActuales): Int {
        if (!opts.modoMisterioso) return 0
        return (maxNoCiviles(numJugadores) - opts.numImpostores).coerceAtLeast(0)
    }

    // ── Privado ─────────────────────────────────────────────────────────────

    private fun GameOptions.ajustarALimites(numJugadores: Int): GameOptions {
        val max = maxNoCiviles(numJugadores)
        val blancosActuales = if (modoMisterioso) numSenoresBlancos else 0
        val impMax = (max - blancosActuales).coerceAtLeast(0)
        val blancosMax = if (modoMisterioso) (max - numImpostores).coerceAtLeast(0) else 0
        return copy(
            numImpostores = numImpostores.coerceIn(0, impMax),
            numSenoresBlancos = numSenoresBlancos.coerceIn(0, blancosMax)
        )
    }
}
