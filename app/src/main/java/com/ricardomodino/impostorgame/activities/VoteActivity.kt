package com.ricardomodino.impostorgame.activities

import android.content.Intent
import android.graphics.Bitmap
import com.ricardomodino.impostorgame.managers.SoundManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import com.ricardomodino.impostorgame.managers.PlayerImageManager
import com.ricardomodino.impostorgame.managers.ThemeManager
import com.ricardomodino.impostorgame.modelos.Jugador
import com.ricardomodino.impostorgame.modelos.TipoJugador

class VoteActivity : BaseGameActivity() {

    private lateinit var recyclerVotos: RecyclerView
    private lateinit var btnConfirmar: Button
    private var selectedIndex: Int = -1
    private var jugadores: MutableList<Jugador> = mutableListOf()
    private lateinit var palabra: String
    private var modoDatosCuriosos: Boolean = false
    private lateinit var adapter: VoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            when {
                ThemeManager.esFinal(this) -> R.layout.activity_vote_final
                ThemeManager.esCarmesi(this) -> R.layout.activity_vote_carmesi
                else -> R.layout.activity_vote
            }
        )
        ImmersiveModeManager.applyActivityContentInsets(this, includeBottomInset = true)
        ThemeManager.aplicarDrawables(this)

        jugadores         = intent.getParcelableArrayListExtra<Jugador>(IntentKeys.JUGADORES)?.toMutableList() ?: mutableListOf()
        palabra           = intent.getStringExtra(IntentKeys.PALABRA) ?: ""
        modoDatosCuriosos = intent.getBooleanExtra(IntentKeys.MODO_DATOS_CURIOSOS, false)

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

    private fun playCountdownTone(frequencyHz: Float, durationMs: Int = 140) =
        SoundManager.playCountdownTone(this, frequencyHz, durationMs)

    private fun mostrarCountdownVoto() {
        val overlay = layoutInflater.inflate(
            if (ThemeManager.esCarmesi(this)) R.layout.activity_countdown_fullscreen_carmesi
            else R.layout.activity_countdown_fullscreen,
            null
        )
        val txt = overlay.findViewById<TextView>(R.id.txtCountdown)
        val txtAccent = overlay.findViewById<TextView?>(R.id.txtCountdownAccent)
        val decorView = window.decorView as ViewGroup
        overlay.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        decorView.addView(overlay)

        val esCarmesi = ThemeManager.esCarmesi(this)
        val numbers = if (esCarmesi) listOf("3", "2", "1", "\u2665") else listOf("3", "2", "1")
        val toneFreqs = if (esCarmesi) {
            mapOf("3" to 392f, "2" to 494f, "1" to 659f, "\u2665" to 880f)
        } else {
            mapOf("3" to 392f, "2" to 494f, "1" to 659f)
        }
        var i = 0

        if (esCarmesi) {
            txtAccent?.alpha = 0.34f
        }

        fun next() {
            if (i >= numbers.size) {
                decorView.removeView(overlay)
                procesarVoto()
                return
            }
            txt.text = numbers[i]
            if (esCarmesi && numbers[i] == "\u2665") {
                txtAccent?.animate()?.scaleX(1.12f)?.scaleY(1.12f)?.alpha(1f)?.setDuration(260L)?.start()
            }
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
                val impostoresVivos = jugadores.count { it.tipo == TipoJugador.IMPOSTOR }
                val rol = when (votado.tipo) {
                    TipoJugador.IMPOSTOR -> {
                        if (impostoresVivos > 1) {
                            getString(R.string.vote_role_impostor_one_of_many)
                        } else {
                            getString(R.string.vote_role_impostor)
                        }
                    }
                    else -> getString(R.string.vote_role_mr_white)
                }
                if (modoDatosCuriosos) {
                    GameDialog(this)
                        .icon("\u26A0\uFE0F")
                        .title(getString(R.string.vote_caught_title))
                        .message(getString(R.string.vote_message_revealed_continue, votado.nombre, rol))
                        .cancelable(false)
                        .positiveButton(getString(R.string.dialog_ok)) { eliminarJugadorYVolver(votado) }
                        .show()
                } else {
                    GameDialog(this)
                        .icon("\u26A0\uFE0F")
                        .title(getString(R.string.vote_caught_title))
                        .message(getString(R.string.vote_message_revealed_try, votado.nombre, rol))
                        .cancelable(false)
                        .positiveButton(getString(R.string.dialog_try)) { abrirPantallaAdivinar(votado) }
                        .show()
                }
            }
        }
    }

    private fun mostrarMensajeCivil(votado: Jugador) {
        val nuevaLista = ArrayList(jugadores.filter { it.nombre != votado.nombre })
        val noCiviles = nuevaLista.count { it.tipo == TipoJugador.IMPOSTOR || it.tipo == TipoJugador.SENOR_BLANCO }
        val civiles = nuevaLista.count { it.tipo == TipoJugador.NORMAL }

        if (noCiviles >= civiles && noCiviles > 0) {
            GameDialog(this)
                .icon("\uD83D\uDE22")
                .title(getString(R.string.vote_oops_title))
                .message(getString(R.string.vote_message_innocent_too_many, votado.nombre))
                .cancelable(false)
                .positiveButton(getString(R.string.dialog_ok)) {
                    setResult(RESULT_OK, Intent().apply {
                        putParcelableArrayListExtra(IntentKeys.JUGADORES_ACTUALIZADOS, nuevaLista)
                    })
                    finish()
                }
                .show()
        } else {
            GameDialog(this)
                .icon("\uD83D\uDE22")
                .title(getString(R.string.vote_oops_title))
                .message(getString(R.string.vote_message_innocent_continue, votado.nombre))
                .cancelable(false)
                .positiveButton(getString(R.string.dialog_ok)) { eliminarJugadorYVolver(votado) }
                .show()
        }
    }

    private fun esFemenino(nombre: String): Boolean = nombre.trim().lowercase().endsWith("a")

    private fun abrirPantallaAdivinar(votado: Jugador) {
        val intent = Intent(this, GuessWordActivity::class.java).apply {
            putExtra(IntentKeys.NOMBRE_VOTADO, votado.nombre)
            putExtra(IntentKeys.TIPO_VOTADO, votado.tipo.name)
            putExtra(IntentKeys.PALABRA, palabra)
            putExtra(IntentKeys.IMPOSTOR, intent.getStringExtra(IntentKeys.IMPOSTOR) ?: "")
            putExtra(IntentKeys.SENORES_BLANCOS, intent.getStringExtra(IntentKeys.SENORES_BLANCOS) ?: "")
            putParcelableArrayListExtra(IntentKeys.JUGADORES, ArrayList(jugadores))
        }
        startActivityForResult(intent, REQUEST_GUESS)
    }

    private fun eliminarJugadorYVolver(votado: Jugador) {
        val nuevaLista = ArrayList(jugadores.filter { it.nombre != votado.nombre })
        setResult(RESULT_OK, Intent().apply {
            putParcelableArrayListExtra(IntentKeys.JUGADORES_ACTUALIZADOS, nuevaLista)
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

        private val civilImages: List<Bitmap> = PlayerImageManager.getShuffledPool(
            this@VoteActivity, list.size
        )

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val img: ImageView  = v.findViewById(R.id.imgPlayerAvatar)
            val name: TextView  = v.findViewById(R.id.txtPlayerNameVote)
            val overlay: View   = v.findViewById(R.id.overlaySelected)
            val check: TextView = v.findViewById(R.id.txtCheck)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(
                LayoutInflater.from(parent.context).inflate(
                    if (ThemeManager.esCarmesi(parent.context)) R.layout.item_vote_player_carmesi
                    else R.layout.item_vote_player,
                    parent,
                    false
                )
            )

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val jugador = list[position]
            holder.name.text = jugador.nombre

            val selfie = SelfieManager.getBitmap(jugador.nombre)
            if (selfie != null) {
                holder.img.setImageBitmap(selfie)
            } else {
                if (position < civilImages.size) holder.img.setImageBitmap(civilImages[position])
                else PlayerImageManager.getRandom(this@VoteActivity)?.let { holder.img.setImageBitmap(it) }
            }

            val sel = selected == position
            holder.overlay.visibility = if (sel) View.VISIBLE else View.GONE
            holder.check.visibility   = if (sel) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = selected
                selected = currentPosition
                if (prev >= 0) notifyItemChanged(prev)
                notifyItemChanged(currentPosition)
                onSelected(currentPosition)
            }
        }
    }
}

