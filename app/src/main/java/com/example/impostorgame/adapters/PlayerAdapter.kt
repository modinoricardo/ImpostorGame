package com.example.impostorgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter(
    private var players: List<String>
    ) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.playerName)
    }

    var useEditLayout: Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {

        val layout = when (useEditLayout) {
            0 -> R.layout.item_player          // layout actual
            1 -> R.layout.item_player_edit     // segundo layout
            else -> R.layout.item_player
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.playerName.text = players[position]

    }

    override fun getItemCount(): Int = players.size

    fun updatePlayers(newList: List<String>) {
        players = newList
        notifyDataSetChanged()
    }


}
