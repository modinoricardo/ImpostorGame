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
import com.example.impostorgame.extensions.expandTouchArea
import com.example.impostorgame.modelos.Jugador

class PlayerAdapterEdit(
    private var players: List<Jugador>,
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

    // Muestra/edita el nombre del jugador en el EditText y comunica cambios al ViewModel
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {

        // Nombre actual del jugador (antes era String)
        val originalName = players[position].nombre

        // Quitar watcher viejo antes de poner texto nuevo
        holder.currentWatcher?.let {
            holder.playerName.removeTextChangedListener(it)
        }

        // Asegurar que el EditText muestra el texto correcto sin mover el cursor
        if (holder.playerName.text.toString() != originalName) {
            holder.playerName.setText(originalName)
            holder.playerName.setSelection(originalName.length)
        }

        // Nuevo text watcher: cuando cambia el texto, avisamos al VM para renombrar
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

        // Borrar jugador por posicion
        holder.iconDelete.setOnClickListener {
            onDeleteClick(holder.adapterPosition)
        }

        // Icono editar: pone foco y abre teclado
        holder.iconEdit.setOnClickListener {
            holder.playerName.requestFocus()
            holder.playerName.setSelection(holder.playerName.text.length)

            val imm = holder.itemView.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(holder.playerName, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun getItemCount(): Int = players.size

    // Actualiza la lista del adapter (si solo cambian nombres y no el tamaño, evita notifyDataSetChanged)
    fun updatePlayers(newList: List<Jugador>) {
        val sizeChanged = newList.size != players.size
        players = newList
        if (!sizeChanged) return

        if (Looper.myLooper() == Looper.getMainLooper()) {
            Handler(Looper.getMainLooper()).post { notifyDataSetChanged() }
        } else {
            notifyDataSetChanged()
        }
    }
}
