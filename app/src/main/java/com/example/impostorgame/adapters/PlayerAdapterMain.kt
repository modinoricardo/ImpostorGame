package com.example.impostorgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapterMain(
    private var players: MutableMap<String, Boolean>
) : RecyclerView.Adapter<PlayerAdapterMain.PlayerViewHolder>() {

    private var keys = players.keys.toList()

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.playerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val key = keys[position]
        holder.playerName.text = key
    }

    override fun getItemCount(): Int = keys.size

    fun updatePlayers(newPlayers: MutableMap<String, Boolean>) {
        players = newPlayers
        keys = newPlayers.keys.toList()
        notifyDataSetChanged()
    }
}
