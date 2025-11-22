package com.example.impostorgame

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
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
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate


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

        //Desactivamos el modo noche
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

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
            true // Consumimos el evento para que no siga a los hijos
        }


        recyclerViewPlayers.setOnTouchListener { _, event ->
                clickEditarJugadores(event)
            true // Consumimos el evento para que no siga a los hijos
        }


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
                recyclerViewPlayers.setBackgroundColor(pressedColor)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                cardViewModoJuego.setCardBackgroundColor(originalColor)
                recyclerViewPlayers.setBackgroundColor(originalColor)

                editarJugadores()
            }
        }
    }

    fun editarJugadores() {

        val intent = Intent(this, EditPlayersActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.no_animation)


    }

    fun closeEditarJugadores(){
        finish()
        overridePendingTransition(0, R.anim.slide_out_bottom)
    }

    fun mensajeAlerta(titulo: String, mensaje: String){
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}
