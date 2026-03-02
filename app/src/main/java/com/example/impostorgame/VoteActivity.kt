package com.example.impostorgame.activities

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.impostorgame.R
import com.example.impostorgame.ThemeManager
import com.example.impostorgame.modelos.Jugador
import com.example.impostorgame.modelos.TipoJugador

class VoteActivity : AppCompatActivity() {

    private lateinit var recyclerVotos: RecyclerView
    private lateinit var btnConfirmar: Button
    private var selectedIndex: Int = -1
    private var jugadores: MutableList<Jugador> = mutableListOf()
    private lateinit var palabra: String
    private lateinit var adapter: VoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.aplicarTema(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote)
        ThemeManager.aplicarDrawables(this)

        jugadores = intent.getParcelableArrayListExtra<Jugador>("JUGADORES")?.toMutableList() ?: mutableListOf()
        palabra   = intent.getStringExtra("PALABRA") ?: ""

        recyclerVotos = findViewById(R.id.recyclerVotos)
        btnConfirmar  = findViewById(R.id.btnConfirmarVoto)

        findViewById<ImageView>(R.id.btnBackVote).setOnClickListener { finish() }

        adapter = VoteAdapter(jugadores) { index ->
            selectedIndex = index
            btnConfirmar.isEnabled = true
            btnConfirmar.alpha = 1f
        }
        recyclerVotos.layoutManager = GridLayoutManager(this, 2)
        recyclerVotos.adapter = adapter

        btnConfirmar.setOnClickListener {
            if (selectedIndex < 0 || selectedIndex >= jugadores.size) return@setOnClickListener
            mostrarCountdownVoto()
        }
    }

    // ── Countdown fullscreen sobre el decorView ──
    private fun mostrarCountdownVoto() {
        val overlay = layoutInflater.inflate(R.layout.activity_countdown_fullscreen, null)
        val txt = overlay.findViewById<TextView>(R.id.txtCountdown)
        val decorView = window.decorView as ViewGroup
        overlay.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        decorView.addView(overlay)

        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        val numbers = listOf("3", "2", "1")
        var i = 0

        fun next() {
            if (i >= numbers.size) {
                decorView.removeView(overlay)
                try { toneGen.release() } catch (_: Exception) {}
                procesarVoto()
                return
            }
            txt.text = numbers[i]
            try { toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200) } catch (_: Exception) {}
            txt.scaleX = 0.2f; txt.scaleY = 0.2f; txt.alpha = 0f
            txt.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(350L)
                .withEndAction {
                    txt.animate().scaleX(1.4f).scaleY(1.4f).alpha(0f).setDuration(550L)
                        .withEndAction { i++; next() }.start()
                }.start()
        }
        next()
    }

    private fun procesarVoto() {
        val votado = jugadores[selectedIndex]
        when (votado.tipo) {
            TipoJugador.NORMAL -> mostrarMensajeCivil(votado)
            TipoJugador.IMPOSTOR, TipoJugador.SENOR_BLANCO -> {
                val rol = if (votado.tipo == TipoJugador.IMPOSTOR) "el impostor" else "el señor blanco"
                AlertDialog.Builder(this)
                    .setTitle("⚠️ ¡Te han pillado!")
                    .setMessage("${votado.nombre} era $rol.\n\n¡Pero intenta salvarte!")
                    .setCancelable(false)
                    .setPositiveButton("Intentar") { _, _ -> abrirPantallaAdivinar(votado) }
                    .show()
            }
        }
    }

    private fun mostrarMensajeCivil(votado: Jugador) {
        val nuevaLista = ArrayList(jugadores.filter { it.nombre != votado.nombre })
        val noCiviles = nuevaLista.count { it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO }
        val civiles = nuevaLista.count { it.tipo == TipoJugador.NORMAL }

        if (noCiviles >= civiles && noCiviles > 0) {
            AlertDialog.Builder(this)
                .setTitle("😢 ¡Error!")
                .setMessage("${votado.nombre} era un pobre civil inocente.\n¡Hay demasiados impostores en la partida!")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    setResult(RESULT_OK, Intent().apply {
                        putParcelableArrayListExtra("JUGADORES_ACTUALIZADOS", nuevaLista)
                    })
                    finish()
                }
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("😢 ¡Error!")
                .setMessage("${votado.nombre} era un pobre civil inocente.\nLa partida continúa.")
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ -> eliminarJugadorYVolver(votado) }
                .show()
        }
    }

    private fun abrirPantallaAdivinar(votado: Jugador) {
        val intent = Intent(this, GuessWordActivity::class.java).apply {
            putExtra("NOMBRE_VOTADO", votado.nombre)
            putExtra("TIPO_VOTADO", votado.tipo.name)
            putExtra("PALABRA", palabra)
            putParcelableArrayListExtra("JUGADORES", ArrayList(jugadores))
        }
        startActivityForResult(intent, REQUEST_GUESS)
    }

    private fun eliminarJugadorYVolver(votado: Jugador) {
        val nuevaLista = ArrayList(jugadores.filter { it.nombre != votado.nombre })
        setResult(RESULT_OK, Intent().apply {
            putParcelableArrayListExtra("JUGADORES_ACTUALIZADOS", nuevaLista)
        })
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GUESS) {
            setResult(resultCode, data)
            finish()
        }
    }

    companion object { const val REQUEST_GUESS = 1001 }

    // ── Adapter: imagen diferente por jugador ──
    inner class VoteAdapter(
        private val list: MutableList<Jugador>,
        private val onSelected: (Int) -> Unit
    ) : RecyclerView.Adapter<VoteAdapter.VH>() {

        private var selected = -1

        // Imágenes distintas por jugador (civil1..10 shuffled)
        private val civilImages = listOf(
            R.drawable.civil1, R.drawable.civil2, R.drawable.civil3,
            R.drawable.civil4, R.drawable.civil5, R.drawable.civil6,
            R.drawable.civil7, R.drawable.civil8, R.drawable.civil9,
            R.drawable.civil10
        ).shuffled()

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val img: ImageView  = v.findViewById(R.id.imgPlayerAvatar)
            val name: TextView  = v.findViewById(R.id.txtPlayerNameVote)
            val overlay: View   = v.findViewById(R.id.overlaySelected)
            val check: TextView = v.findViewById(R.id.txtCheck)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_vote_player, parent, false))

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val jugador = list[position]
            holder.name.text = jugador.nombre

            // 1. Selfie si existe
            val selfie = SelfieManager.getBitmap(jugador.nombre)
            if (selfie != null) {
                holder.img.setImageBitmap(selfie)
            } else {
                // 2. Imagen distinta para cada jugador según su posición
                val imgRes = if (position < civilImages.size) civilImages[position] else R.drawable.civil1
                holder.img.setImageResource(imgRes)
            }

            val sel = selected == position
            holder.overlay.visibility = if (sel) View.VISIBLE else View.GONE
            holder.check.visibility   = if (sel) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                val prev = selected
                selected = position
                if (prev >= 0) notifyItemChanged(prev)
                notifyItemChanged(position)
                onSelected(position)
            }
        }
    }
}