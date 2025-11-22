package com.example.impostorgame

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditPlayersActivity : AppCompatActivity() {

    private lateinit var editTextNewPlayer: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_players)

        var imageViewAniadirJugador = findViewById<ImageView>(R.id.imageViewAniadirJugador)
        var btnConfirm = findViewById<Button>(R.id.btnConfirm)
        editTextNewPlayer = findViewById<EditText>(R.id.editTextNewPlayer)

        // Obtenemos una instancia del ViewModel.
        // El ViewModel mantiene la lista de jugadores y su estado.
        val viewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlayersDialog)

        // Le decimos cómo debe organizar los elementos: en una columna vertical.
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Creamos el adapter de la lista, inicialmente vacío.
        // El contenido real vendrá del LiveData dentro del ViewModel.
        val adapter = PlayerAdapter(emptyList())

        // Asignamos el adapter al RecyclerView para que pueda mostrar datos.
        recyclerView.adapter = adapter

        // Observamos la lista del ViewModel (LiveData).
        // Cada vez que cambie la lista en el ViewModel, esta lambda se ejecuta
        // y actualiza el adapter con los nuevos nombres.
        viewModel.players.observe(this) { lista ->
            adapter.updatePlayers(lista)
        }


        // Indicamos al adapter que queremos usar el layout de tipo 1
        // (por ejemplo: modo edición, item con iconos, etc.)
        adapter.useEditLayout = 1

        // Le decimos al RecyclerView que vuelva a dibujar TODOS los elementos,
        // porque el layout ha cambiado (modo 1 → modo edición).
        adapter.notifyDataSetChanged()

        btnConfirm.setOnClickListener { finish() }
        imageViewAniadirJugador.setOnClickListener { aniadirJugador() }

    }

    private fun aniadirJugador() {

        val newPlayer = editTextNewPlayer.text.toString()

        if (!newPlayer.isBlank()) {

            

        } else {

            if (newPlayer.isEmpty()) {
                mensajeAlerta(
                    "Debe escribir un jugador",
                    "Para poder añadir un jugador a la partida primero tienes que escribir su nombre"
                )
            } else {
                mensajeAlerta(
                    "Si solo pones espacios no voy a poder guardar el nombre",
                    "Para poder añadir un jugador a la partida primero tienes que escribir su nombre"
                )
            }

        }

    }

    fun mensajeAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

}