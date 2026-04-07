package com.ricardomodino.impostorgame.bottomsheets

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ricardomodino.impostorgame.adapters.PlayerAdapterEdit
import com.ricardomodino.impostorgame.viewmodel.PlayerViewModel
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.activities.MainActivity
import com.ricardomodino.impostorgame.managers.GameDialog
import com.ricardomodino.impostorgame.managers.ThemeManager

class EditPlayersBottomSheet : BaseGameBottomSheet() {

    private lateinit var adapter: PlayerAdapterEdit
    private lateinit var editTextNewPlayer: EditText
    private lateinit var imageViewAniadirJugador: ImageView
    private lateinit var btnConfirm: Button

    override val isDraggableSheet: Boolean = false
    override val isHideableSheet: Boolean = false
    override val expandOnStart: Boolean = true  // necesario para que el teclado suba correctamente

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            when {
                ThemeManager.esFinal(requireContext()) -> R.layout.dialog_edit_players_final
                ThemeManager.esCarmesi(requireContext()) -> R.layout.dialog_edit_players_carmesi
                else -> R.layout.dialog_edit_players
            },
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // â”€â”€ Aplicar tema â”€â”€
        // Cuando el teclado aparece, se añade padding inferior igual a su altura
        // para que el contenido suba en lugar de quedar tapado (compatble con edge-to-edge)
        val rootView = view.findViewById<View>(R.id.rootBottomSheet)
        val basePaddingBottom = rootView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, basePaddingBottom + imeHeight)
            insets
        }

        val bgCard  = ThemeManager.getBgCard(requireContext())
        val btnNeon = ThemeManager.getBtnNeon(requireContext())
        val accent  = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootBottomSheet)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext()) -> R.drawable.bg_final_sheet_surface
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                else -> bgCard
            }
        )
        view.findViewById<LinearLayout>(R.id.inputRow)?.setBackgroundResource(
            when {
                ThemeManager.esFinal(requireContext()) -> R.drawable.bg_final_input
                ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_soft_panel
                else -> bgCard
            }
        )
        view.findViewById<TextView>(R.id.txtTitle)?.setShadowLayer(12f, 0f, 0f, accent)
        view.findViewById<Button>(R.id.btnConfirm)?.setBackgroundResource(btnNeon)

        val viewModel = ViewModelProvider(requireActivity()).get(PlayerViewModel::class.java)

        btnConfirm = view.findViewById(R.id.btnConfirm)
        editTextNewPlayer = view.findViewById(R.id.editTextNewPlayer)
        imageViewAniadirJugador = view.findViewById(R.id.imageViewAniadirJugador)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPlayersDialog)

        val currentPlayers = viewModel.players.value ?: emptyList()

        adapter = PlayerAdapterEdit(
            currentPlayers,
            onDeleteClick = { index ->
                if (viewModel.getPlayerCount() <= 3) {
                    mensajeAlerta(
                        "\u26D4",
                        getString(R.string.edit_players_delete_block_title),
                        getString(R.string.edit_players_delete_block_msg)
                    )
                } else {
                    viewModel.removeAt(index)
                }
            },
            onEditClick = { index, newName ->
                viewModel.renameAt(index, newName)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.players.observe(viewLifecycleOwner) { nuevaLista ->
            adapter.updatePlayers(nuevaLista)
        }

        imageViewAniadirJugador.setOnClickListener {
            val nameNewPlayer = editTextNewPlayer.text.toString()
            if (nameNewPlayer.lowercase() in listOf("ste", "staicy")) {
                mensajeAlerta("\u2764\uFE0F", getString(R.string.edit_players_too_much_love_title), getString(R.string.edit_players_too_much_love_msg))
                aniadirJugador(nameNewPlayer.trim(), viewModel)
            } else if (nameNewPlayer.lowercase() in listOf("frankestein")) {
                mensajeAlerta(
                    "ðŸ”",
                    getString(R.string.edit_players_clue_found_title),
                    getString(R.string.edit_players_clue_found_msg)
                )
            } else if (!nameNewPlayer.isBlank()) {
                aniadirJugador(nameNewPlayer.trim(), viewModel)
            } else {
                mensajeAlerta("\u26A0\uFE0F", getString(R.string.edit_players_add_error_title), getString(R.string.edit_players_add_error_msg))
                return@setOnClickListener
            }
        }

        btnConfirm.setOnClickListener {
            val nameNewPlayer = editTextNewPlayer.text.toString()
            if (nameNewPlayer.isNotBlank()) aniadirJugador(nameNewPlayer.trim(), viewModel)
            dismiss()
        }
    }

    fun mensajeAlerta(icono: String, titulo: String, mensaje: String) {
        GameDialog(requireContext())
            .icon(icono)
            .title(titulo)
            .message(mensaje)
            .positiveButton(getString(R.string.dialog_ok))
            .show()
    }

    private fun aniadirJugador(newPlayer: String, viewModel: PlayerViewModel) {
        viewModel.addPlayer(newPlayer)
        editTextNewPlayer.text.clear()
    }
}
