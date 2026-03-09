package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.ricardomodino.impostorgame.managers.SoundManager
import kotlin.math.PI
import kotlin.math.sin
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador

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

    private fun playCountdownTone(frequencyHz: Float, durationMs: Int = 140) {
        if (!SoundManager.isSoundEnabled(this)) return
        try {
            val sampleRate = 44100
            val numSamples = sampleRate * durationMs / 1000
            val fadeLen    = (sampleRate * 0.015).toInt()
            val samples    = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val env = when {
                    i < fadeLen              -> i.toDouble() / fadeLen
                    i > numSamples - fadeLen -> (numSamples - i).toDouble() / fadeLen
                    else                     -> 1.0
                }
                samples[i] = (env * 0.7 * Short.MAX_VALUE *
                        sin(2.0 * PI * frequencyHz * i / sampleRate)).toInt().toShort()
            }
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack(
                AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuf, numSamples * 2), AudioTrack.MODE_STATIC
            )
            track.write(samples, 0, numSamples)
            track.setNotificationMarkerPosition(numSamples)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) { t.release() }
                override fun onPeriodicNotification(t: AudioTrack) {}
            })
            track.play()
        } catch (_: Exception) {}
    }

    private fun mostrarCountdownVoto() {
        val overlay = layoutInflater.inflate(R.layout.activity_countdown_fullscreen, null)
        val txt = overlay.findViewById<TextView>(R.id.txtCountdown)
        val decorView = window.decorView as ViewGroup
        overlay.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        decorView.addView(overlay)

        val toneFreqs = mapOf("3" to 392f, "2" to 494f, "1" to 659f)
        val numbers = listOf("3", "2", "1")
        var i = 0

        fun next() {
            if (i >= numbers.size) {
                decorView.removeView(overlay)
                procesarVoto()
                return
            }
            txt.text = numbers[i]
            playCountdownTone(toneFreqs[numbers[i]] ?: 440f)
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
                val femenino = esFemenino(votado.nombre)
                val rol = when (votado.tipo) {
                    TipoJugador.IMPOSTOR -> if (femenino) "la impostora" else "el impostor"
                    else -> if (femenino) "la señora blanca" else "el señor blanco"
                }
                GameDialog(this)
                    .icon("⚠️")
                    .title("¡Te han pillado!")
                    .message("${votado.nombre} era $rol.\n\n¡Pero intenta salvarte!")
                    .cancelable(false)
                    .positiveButton("Intentar") { abrirPantallaAdivinar(votado) }
                    .show()
            }
        }
    }

    private fun mostrarMensajeCivil(votado: Jugador) {
        val nuevaLista = ArrayList(jugadores.filter { it.nombre != votado.nombre })
        val noCiviles = nuevaLista.count { it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO }
        val civiles = nuevaLista.count { it.tipo == TipoJugador.NORMAL }
        val art = if (esFemenino(votado.nombre)) "una" else "un"

        if (noCiviles >= civiles && noCiviles > 0) {
            GameDialog(this)
                .icon("😢")
                .title("¡Ups!")
                .message("${votado.nombre} era $art pobre civil inocente.\n¡Hay demasiados impostores en la partida!")
                .cancelable(false)
                .positiveButton("OK") {
                    setResult(RESULT_OK, Intent().apply {
                        putParcelableArrayListExtra("JUGADORES_ACTUALIZADOS", nuevaLista)
                    })
                    finish()
                }
                .show()
        } else {
            GameDialog(this)
                .icon("😢")
                .title("¡Ups!")
                .message("${votado.nombre} era $art pobre civil inocente.\nLa partida continúa.")
                .cancelable(false)
                .positiveButton("OK") { eliminarJugadorYVolver(votado) }
                .show()
        }
    }

    private fun esFemenino(nombre: String): Boolean = nombre.trim().lowercase().endsWith("a")

    private fun abrirPantallaAdivinar(votado: Jugador) {
        val intent = Intent(this, GuessWordActivity::class.java).apply {
            putExtra("NOMBRE_VOTADO", votado.nombre)
            putExtra("TIPO_VOTADO", votado.tipo.name)
            putExtra("PALABRA", palabra)
            putExtra("IMPOSTOR", intent.getStringExtra("IMPOSTOR") ?: "")
            putExtra("SENORES_BLANCOS", intent.getStringExtra("SENORES_BLANCOS") ?: "")
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

    inner class VoteAdapter(
        private val list: MutableList<Jugador>,
        private val onSelected: (Int) -> Unit
    ) : RecyclerView.Adapter<VoteAdapter.VH>() {

        private var selected = -1

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

            val selfie = SelfieManager.getBitmap(jugador.nombre)
            if (selfie != null) {
                holder.img.setImageBitmap(selfie)
            } else {
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
