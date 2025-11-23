package com.example.impostorgame

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")

    private lateinit var cardViewModoJuego: CardView
    private lateinit var main: FrameLayout
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var overlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Desactivamos el modo noche
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //Declaracion de todos los elementos del xml
        playersRecyclerView = findViewById<RecyclerView>(R.id.playersRecyclerView)
        cardViewModoJuego = findViewById<CardView>(R.id.cardViewModoJuego)
        main = findViewById<FrameLayout>(R.id.main)
        relativeLayout = findViewById<RelativeLayout>(R.id.relativeLayout)
        overlay = findViewById<View>(R.id.darkOverlay)

        // ViewModel
        val viewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)

        // RecyclerView con Flexbox
        val layoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        playersRecyclerView.layoutManager = layoutManager

        // Adapter vacío (se llenará con LiveData)
        val adapter = PlayerAdapterMain(viewModel.players.value ?: mutableMapOf())
        playersRecyclerView.adapter = adapter

        viewModel.players.observe(this) {
            adapter.updatePlayers(it)
        }

        // Observamos los jugadores del ViewModel
        viewModel.players.observe(this) { lista ->
            adapter.updatePlayers(lista)
        }

        cardViewModoJuego.setOnTouchListener { v, event ->
                    clickEditarJugadores(event)
            true // Consumimos el evento para que no siga a los hijos
        }

        playersRecyclerView.setOnTouchListener { _, event ->
                clickEditarJugadores(event)
            true // Consumimos el evento para que no siga a los hijos
        }

    }

    //Detecta cuando vuelve a primer plano
    override fun onResume() {
        super.onResume()
        overlay.visibility = View.GONE
    }

    fun onBottomSheetClosed() {
        overlay.visibility = View.GONE
    }


    private var originalColor: Int = 0
    private var originalColorsSaved = false
    fun clickEditarJugadores(event: MotionEvent){
        //mensajeAlerta("Alerta", "Esta opcion aun no esta implementada en la app")

        when(event.action){

            MotionEvent.ACTION_DOWN -> {

                if (!originalColorsSaved) {
                    originalColor = cardViewModoJuego.cardBackgroundColor.defaultColor
                    originalColorsSaved = true
                }

                val pressedColor = getColor(R.color.button_pressed)
                cardViewModoJuego.setCardBackgroundColor(pressedColor)
                playersRecyclerView.setBackgroundColor(pressedColor)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                cardViewModoJuego.setCardBackgroundColor(originalColor)
                playersRecyclerView.setBackgroundColor(originalColor)

                editarJugadores()
            }
        }
    }

    fun editarJugadores() {
        overlay.visibility = View.VISIBLE
        EditPlayersBottomSheet().show(supportFragmentManager, "EditPlayers")
    }

    fun mensajeAlerta(titulo: String, mensaje: String){
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}
