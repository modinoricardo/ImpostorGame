package com.ricardomodino.impostorgame.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.LocaleManager
import com.ricardomodino.impostorgame.managers.ThemeManager

/**
 * Activity base de la que heredan todas las pantallas del juego.
 *
 * Centraliza dos comportamientos que antes estaban copiados en cada Activity:
 *  - Envuelve el contexto con el idioma seleccionado por el usuario [LocaleManager.wrap].
 *  - Aplica el tema activo ANTES de que AppCompatActivity infle el layout [ThemeManager.aplicarTema],
 *    requisito obligatorio para que setTheme() tenga efecto.
 *
 * Las subclases NO deben sobreescribir [attachBaseContext] ni llamar a
 * [ThemeManager.aplicarTema] manualmente; esta clase ya lo gestiona.
 *
 * Si una Activity necesita ejecutar algo antes de [super.onCreate] (ej: window flags),
 * puede hacerlo directamente en su propio [onCreate] antes de llamar a super:
 *
 * ```kotlin
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     window.setFlags(...)   // se ejecuta primero
 *     super.onCreate(...)    // → aplica tema → AppCompatActivity.onCreate
 * }
 * ```
 */
abstract class BaseGameActivity : AppCompatActivity() {

    /**
     * Registra el comportamiento del botón atrás mostrando un diálogo de confirmación.
     * @param onConfirm Acción a ejecutar cuando el usuario confirma salir (por defecto: finish()).
     */
    protected fun configurarBackPressed(onConfirm: () -> Unit = { finish() }) {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                GameDialog(this@BaseGameActivity)
                    .icon("\uD83D\uDEAA")
                    .title(getString(R.string.dialog_salir_titulo))
                    .message(getString(R.string.dialog_salir_msg))
                    .cancelable(true)
                    .positiveButton(getString(R.string.dialog_salir_si)) { onConfirm() }
                    .negativeButton(getString(R.string.dialog_salir_no))
                    .show()
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
    }
}
