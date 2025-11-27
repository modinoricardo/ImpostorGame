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
import android.content.Context
import android.view.inputmethod.InputMethodManager

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

        init {
            iconEdit.expandTouchArea(16)
            iconDelete.expandTouchArea(20)
        }

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
            // 1) Poner el foco en el EditText
            holder.playerName.requestFocus()

            // 2) Colocar el cursor al final del texto
            holder.playerName.setSelection(holder.playerName.text.length)

            // 3) Mostrar el teclado
            val imm = holder.itemView.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(holder.playerName, InputMethodManager.SHOW_IMPLICIT)

            // Si prefieres seleccionar_todo el texto en lugar de ir al final:
            // holder.playerName.selectAll()
        }

    }

    override fun getItemCount(): Int = players.size

    fun updatePlayers(newList: List<String>) {
        // Comprobar si solo ha cambiado el texto o también el tamaño de la lista
        val sizeChanged = newList.size != players.size

        // Actualizamos el modelo interno
        players = newList

        // Si el tamaño NO ha cambiado, es un rename: no hace falta redibujar la lista
        if (!sizeChanged) return

        // Si el tamaño sí ha cambiado (add/remove), entonces sí refrescamos
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        } else {
            notifyDataSetChanged()
        }
    }

}
