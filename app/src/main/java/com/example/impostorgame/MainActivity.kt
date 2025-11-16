package com.example.impostorgame

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val players = mutableListOf("Ricardo", "Lucia", "Pedro", "Ana", "Mario", "Luis", "Ste")
        val recyclerViewPlayers = findViewById<RecyclerView>(R.id.playersRecyclerView)
        val cardViewModoJuego = findViewById<CardView>(R.id.cardViewModoJuego)
        val layoutModoJuego = findViewById<LinearLayout>(R.id.layoutModoJuego)

        // Definimos cómo se muestran pero solo permite Horizontal o vertical, sacamos la implementacion de google
        //recyclerViewPlayers.layoutManager = LinearLayoutManager(this, LinearLayoutManager.INVALID_OFFSET, false)

        //Solucionamos problema anterior y se muestran de forma correcta en el apartado Modo de Juego
        val layoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW       // Coloca ítems en fila
            flexWrap = FlexWrap.WRAP                // Salta a la siguiente fila cuando no quepan
        }

        recyclerViewPlayers.layoutManager = layoutManager
        recyclerViewPlayers.adapter = PlayerAdapter(players) { index ->
            editarJugadores(index)
        }

        cardViewModoJuego.setOnClickListener { editarJugadores(0) }

    }

    fun editarJugadores(index: Int?){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar jugadores")

        val editText = EditText(this)
        editText.hint = "Escribe un nuevo nombre"

        builder.setView(editText)

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

}