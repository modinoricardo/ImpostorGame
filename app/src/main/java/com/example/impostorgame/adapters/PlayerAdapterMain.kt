package com.example.impostorgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.impostorgame.managers.ThemeManager
import com.example.impostorgame.modelos.Jugador

class PlayerAdapterMain(
    private var players: List<Jugador>
) : RecyclerView.Adapter<PlayerAdapterMain.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.playerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.playerName.text = players[position].nombre
        holder.itemView.setBackgroundResource(ThemeManager.getBgChip(holder.itemView.context))
    }

    override fun getItemCount(): Int = players.size

    fun updatePlayers(newPlayers: List<Jugador>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}