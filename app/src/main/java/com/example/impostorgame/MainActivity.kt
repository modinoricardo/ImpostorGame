package com.example.impostorgame

import android.annotation.SuppressLint
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

class MainActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")

    private lateinit var recyclerViewPlayers: RecyclerView
    private lateinit var cardViewModoJuego: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerViewPlayers = findViewById<RecyclerView>(R.id.playersRecyclerView)
        cardViewModoJuego = findViewById<CardView>(R.id.cardViewModoJuego)

        // ViewModel
        val viewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)

        // RecyclerView con Flexbox
        val layoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        recyclerViewPlayers.layoutManager = layoutManager

        // Adapter vacío (se llenará con LiveData)
        val adapter = PlayerAdapter(emptyList())
        recyclerViewPlayers.adapter = adapter

        // Observamos los jugadores del ViewModel
        viewModel.players.observe(this) { lista ->
            adapter.updatePlayers(lista)
        }

        cardViewModoJuego.setOnTouchListener { v, event ->
                    clickEditarJugadores(event)
            true
        }


        recyclerViewPlayers.setOnTouchListener { _, event ->
                clickEditarJugadores(event)
            true // Consumimos el evento para que no siga a los hijos
        }


    }

    fun clickEditarJugadores(event: MotionEvent){
        //mensajeAlerta("Alerta", "Esta opcion aun no esta implementada en la app")

        when(event.action){
            MotionEvent.ACTION_DOWN ->{

                cardViewModoJuego.setCardBackgroundColor(
                    getColor(R.color.button_pressed)
                )

                recyclerViewPlayers.setBackgroundColor(
                    getColor(R.color.button_pressed)
                )

            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                cardViewModoJuego.setCardBackgroundColor(
                    getColor(android.R.color.white)
                )

                recyclerViewPlayers.setBackgroundColor(
                    getColor(android.R.color.white)
                )

                editarJugadores()
            }
        }

    }

    fun editarJugadores() {



    }

    fun mensajeAlerta(titulo: String, mensaje: String){
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}
