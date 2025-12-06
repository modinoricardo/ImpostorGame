package com.example.impostorgame.activities

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.impostorgame.Category
import com.example.impostorgame.R

@Suppress("DEPRECATION")
class ImpostorRevealActivity : AppCompatActivity() {
    //Lista donde guardamos los jugadores
    private lateinit var listaJugadores: List<String>;
    private lateinit var listaCategorias: List<Category>;
    private lateinit var detailsPlayer: TextView;
    private lateinit var textNextPlayer: TextView;
    private lateinit var layout: LinearLayout;
    private lateinit var cardViewPrincipal: CardView;
    private lateinit var imgDedo: ImageView;
    private lateinit var txtTwo: TextView;
    private lateinit var nenxtPlayer: CardView;
    private lateinit var presText: TextView;
    private lateinit var turnPlayerName: TextView;
    private var playerInGame: Int = 0;
    private var indiceImpostor: Int = 0;
    private var indicePalabra: Int = 0;
//    private lateinit var categoriaInGame: Category;
    private lateinit var palabra: String;
    private lateinit var pista: String;
    private lateinit var nameImpostorInGame: String;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_impostor_reveal)

        //Hacemos todas las asignaciones
        detailsPlayer = findViewById(R.id.detailsPlayer);
        layout = findViewById(R.id.layout);
        cardViewPrincipal = findViewById(R.id.cardViewPrincipal);
        imgDedo = findViewById(R.id.imgDedo);
        txtTwo = findViewById(R.id.txtTwo);
        nenxtPlayer = findViewById(R.id.nenxtPlayer);
        presText = findViewById(R.id.presText);
        turnPlayerName = findViewById(R.id.turnPlayerName);
        textNextPlayer = findViewById(R.id.textNextPlayer);

        //Recuperamos la lista que viene en el Intent
        listaJugadores = intent.getStringArrayListExtra("PLAYERS")?.toList() ?: emptyList();
        //Recuperamos la lista de categorias
        listaCategorias =
            intent.getParcelableArrayListExtra<Category>("CATEGORIES")?.toList() ?: emptyList()

        //Activamos la tracsaccion del Layout mas suave
        layout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        //Dejamos todos los eventos lanzados
        onEventos();

        //Hacemos un aleatorio para decidir al impostor
        indiceImpostor = listaJugadores.indices.random()
        //nombre del impostor
        nameImpostorInGame = listaJugadores[indiceImpostor]

        //Hacemos un aleatorio para elegir el indice de la
        indicePalabra = listaCategorias.indices.random()
        // Categoría a jugar
        val categoriaInGame = listaCategorias[indicePalabra]
        // Índice aleatorio dentro de items
        val indiceListaItems = categoriaInGame.items.indices.random()
        // Item elegido
        val wordItemInGame = categoriaInGame.items[indiceListaItems]
        // Palabra a jugar
        palabra = wordItemInGame.name
        //Pista a jugar
        pista = wordItemInGame.hint

        //Empezamos desde el primer jugador
        playerInGame = 0;
        //Cargamos la tarjeta del impostor o de la palabra
        cargarInformacion();

    }

    private fun cargarInformacion() {
        if (indiceImpostor == playerInGame) {
            turnPlayerName.text = listaJugadores[playerInGame]
            detailsPlayer.text = "IMPOSTOR"
        }else{
            turnPlayerName.text = listaJugadores[playerInGame]
            detailsPlayer.text = palabra
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onEventos() {
        cardViewPrincipal.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mostrarPalabra()
                    nenxtPlayer.visibility = View.VISIBLE
                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    ocultarPalabra()
                    true
                }

                else -> false
            }
        }
        nenxtPlayer.setOnClickListener {
            btnNextPlayer()
        }
    }


    private fun ocultarPalabra() {

        detailsPlayer.visibility = View.GONE
        imgDedo.visibility = View.VISIBLE
        txtTwo.visibility = View.VISIBLE
        presText.visibility = View.VISIBLE

    }

    private fun mostrarPalabra() {

        cargarInformacion();

        detailsPlayer.visibility = View.VISIBLE
        imgDedo.visibility = View.GONE
        txtTwo.visibility = View.GONE
        presText.visibility = View.GONE
    }

    private fun btnNextPlayer() {
        nenxtPlayer.visibility = View.INVISIBLE
        val lastIndex = listaJugadores.lastIndex

        if (playerInGame < lastIndex) {
            playerInGame++
            cargarInformacion()
        }else{

            val intent = Intent(this, PlayGameActivity::class.java).apply {}
            intent.putExtra("PALABRA", palabra)
            intent.putExtra("IMPOSTOR", nameImpostorInGame)
//
//            // Lanzar la nueva Activity
            startActivity(intent)
        }

        textNextPlayer.text =
            if (playerInGame == lastIndex) "¡EMPEZAR PARTIDA!" else "⏭ SIGUIENTE JUGADOR"
    }

}