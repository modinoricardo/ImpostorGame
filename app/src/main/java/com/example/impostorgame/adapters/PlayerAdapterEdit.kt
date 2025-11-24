package com.example.impostorgame

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapterEdit(
    private var players: List<String>,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (position: Int, newName: String) -> Unit
) : RecyclerView.Adapter<PlayerAdapterEdit.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: EditText = itemView.findViewById(R.id.playerName)
        val iconDelete: ImageView = itemView.findViewById(R.id.iconDelete)
        val iconEdit: ImageView = itemView.findViewById(R.id.iconEdit)
        var currentWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_edit, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {

        val originalName = players[position]

        // Quitar watcher viejo antes de poner texto nuevo
        holder.currentWatcher?.let {
            holder.playerName.removeTextChangedListener(it)
        }

        // Asegurar que el EditText muestra el texto correcto sin mover el cursor
        if (holder.playerName.text.toString() != originalName) {
            holder.playerName.setText(originalName)
            holder.playerName.setSelection(originalName.length)
        }

        // Nuevo text watcher
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val nuevo = s.toString().trim()
                if (nuevo.isNotEmpty() && nuevo != originalName) {
                    onEditClick(holder.adapterPosition, nuevo)
                }
            }
        }

        holder.playerName.addTextChangedListener(watcher)
        holder.currentWatcher = watcher

        // Borrar jugador por posición
        holder.iconDelete.setOnClickListener {
            onDeleteClick(holder.adapterPosition)
        }

        // Icono editar explícito
        holder.iconEdit.setOnClickListener {
            val nuevo = holder.playerName.text.toString().trim()
            if (nuevo.isNotEmpty() && nuevo != originalName) {
                onEditClick(holder.adapterPosition, nuevo)
            }
        }
    }

    override fun getItemCount(): Int = players.size

    fun updatePlayers(newList: List<String>) {
        players = newList

        // Evita crash si RecyclerView está haciendo layout
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        } else {
            notifyDataSetChanged()
        }
    }
}
