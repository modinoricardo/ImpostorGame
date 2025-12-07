package com.example.impostorgame.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.impostorgame.R
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog


class PlayGameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_game)

        //Cuando el usuario de hacia atras en la barra de navegacion
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@PlayGameActivity)
                    .setTitle("Salir")
                    .setMessage("¿Quieres salir de la partida?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Salir") { _, _ ->
                        // Volver atrás a la MainActivity que ya está debajo
                        finish()
                    }
                    .show()
            }
        })

        val root = findViewById<View>(R.id.main)
        val btnRow = findViewById<View>(R.id.btnRow)

        // Arriba (status bar + notch) y laterales
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val topInsets = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val sideInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(left = sideInsets.left, top = topInsets.top, right = sideInsets.right)
            insets
        }

        // Abajo (navigation bar) sumado al padding que ya tengas
        val basePaddingBottom = btnRow.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(btnRow) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = basePaddingBottom + nav.bottom + dpToPx(22))
            insets
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}
