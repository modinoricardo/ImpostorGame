package com.example.impostorgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter(
    private var players: MutableMap<String, Boolean>
    ) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

        private var keys = players.keys.toList()

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.playerName)
    }


    var useEditLayout: Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val layout = when (useEditLayout) {
            0 -> R.layout.item_player
            1 -> R.layout.item_player_edit
            else -> R.layout.item_player
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val key = keys[position]                // nombre del jugador
        val value = players[key] ?: false       // boolean del mapa

        holder.playerName.text = key
    }

    override fun getItemCount(): Int = keys.size

    fun updatePlayers(newMap: MutableMap<String, Boolean>) {
        players = newMap
        keys = newMap.keys.toList()
        notifyDataSetChanged()
    }


}
