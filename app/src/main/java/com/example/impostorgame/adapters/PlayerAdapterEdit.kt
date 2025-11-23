package com.example.impostorgame

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.text.TextWatcher
import android.text.Editable

class PlayerAdapterEdit(
    private var players: MutableMap<String, Boolean>,
    private val onDeleteClick: (String) -> Unit,
    private val onEditClick: (oldName: String, newName: String) -> Unit
) : RecyclerView.Adapter<PlayerAdapterEdit.PlayerViewHolder>() {

    private var keys = players.keys.toList()

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: EditText = itemView.findViewById(R.id.playerName)
        val iconDelete: ImageView = itemView.findViewById(R.id.iconDelete)
        val iconEdit: ImageView = itemView.findViewById(R.id.iconEdit)

        // Guardamos el último watcher para poder eliminarlo
        var currentWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_edit, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val oldName = keys[position]

        val currentText = holder.playerName.text.toString()

        if (currentText != oldName) {
            val cursorPos = holder.playerName.selectionStart
            holder.playerName.setText(oldName)

            // restaurar posición segura del cursor
            val nuevaPos = minOf(cursorPos, oldName.length)
            holder.playerName.setSelection(nuevaPos)
        }


        // ------ ELIMINAR WATCHER ANTERIOR ------
        holder.currentWatcher?.let {
            holder.playerName.removeTextChangedListener(it)
        }

        // ------ CREAR NUEVO WATCHER ------
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nuevo = s.toString().trim()
                if (nuevo.isNotEmpty() && nuevo != oldName) {
                    onEditClick(oldName, nuevo)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        holder.playerName.addTextChangedListener(watcher)
        holder.currentWatcher = watcher

        // ---- BORRAR ----
        holder.iconDelete.setOnClickListener {
            onDeleteClick(oldName)
        }

        // ---- EDITAR CON ICONO ----
        holder.iconEdit.setOnClickListener {
            val nuevo = holder.playerName.text.toString().trim()
            if (nuevo.isNotEmpty() && nuevo != oldName) {
                onEditClick(oldName, nuevo)
            }
        }
    }

    override fun getItemCount(): Int = keys.size

    fun updatePlayers(newPlayers: MutableMap<String, Boolean>) {
        players = newPlayers
        keys = newPlayers.keys.toList()

        // Ejecutar la actualización en la cola del hilo principal
        // y evitar el crash "Cannot call this while RecyclerView is computing a layout"
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        } else {
            notifyDataSetChanged()
        }
    }

}
