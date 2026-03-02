package com.example.impostorgame.bottomsheets

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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.impostorgame.PlayerAdapterEdit
import com.example.impostorgame.PlayerViewModel
import com.example.impostorgame.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.impostorgame.activities.MainActivity
import com.example.impostorgame.managers.ThemeManager

class EditPlayersBottomSheet : BottomSheetDialogFragment() {

    private lateinit var adapter: PlayerAdapterEdit
    private lateinit var editTextNewPlayer: EditText
    private lateinit var imageViewAniadirJugador: ImageView
    private lateinit var btnConfirm: Button

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? MainActivity)?.onBottomSheetClosed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_players, container, false)
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        bottomSheet.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bottomsheet_rounded
        )

        bottomSheet.post {
            val h = if (bottomSheet.height > 0) bottomSheet.height
            else bottomSheet.resources.displayMetrics.heightPixels
            bottomSheet.translationY = h.toFloat()
            bottomSheet.alpha = 0f
            bottomSheet.animate()
                .translationY(0f).alpha(1f)
                .setDuration(1500L)
                .setInterpolator(DecelerateInterpolator(2f)).start()
        }

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isDraggable = false
        behavior.isHideable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Aplicar tema ──
        val bgCard  = ThemeManager.getBgCard(requireContext())
        val btnNeon = ThemeManager.getBtnNeon(requireContext())
        val accent  = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootBottomSheet)?.setBackgroundResource(bgCard)
        view.findViewById<LinearLayout>(R.id.inputRow)?.setBackgroundResource(bgCard)
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
                        "No se puede borrar",
                        "Para poder jugar hacen falta al menos 3 jugadores. En lugar de borrar, puedes editar uno de los que ya tienes."
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
                mensajeAlerta("Demasiado amor", "Este nombre desprende niveles extraordinarios de cariño y ternura. Podría sobrecargar el sistema.")
                aniadirJugador(nameNewPlayer.trim(), viewModel)
            } else if (nameNewPlayer.lowercase() in listOf("frankestein")) {
                mensajeAlerta("PISTA ENCONTRADA",
                    "TE L♥ HEYE TK UOTPOWWO.\n" +
                            "♥V WVX L♥ ULZVLG O, ULAE ♥VHKYKLTPL.\n" +
                            "UOAO BE♥PAOA, ÑLML YET♥VBKA♥L.\n" +
                            "OUOJOÑO TE ♥KACL.\n" +
                            "TE ÑO YOWEA VPKW.\n" +
                            "ÑO AL♥UVL♥PO♥.\n" +
                            "LTYKLTÑLWO.")
            } else if (!nameNewPlayer.isBlank()) {
                aniadirJugador(nameNewPlayer.trim(), viewModel)
            } else {
                mensajeAlerta("Error al añadir jugador", "No se pudo añadir el jugador. Escribe un nombre válido y vuelve a intentarlo.")
                return@setOnClickListener
            }
        }

        btnConfirm.setOnClickListener {
            val nameNewPlayer = editTextNewPlayer.text.toString()
            if (!nameNewPlayer.isEmpty() || !nameNewPlayer.isBlank()) aniadirJugador(nameNewPlayer.trim(), viewModel)
            dismiss()
        }
    }

    fun mensajeAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(requireContext()).setTitle(titulo).setMessage(mensaje).setPositiveButton("OK", null).show()
    }

    private fun aniadirJugador(newPlayer: String, viewModel: PlayerViewModel) {
        viewModel.addPlayer(newPlayer)
        editTextNewPlayer.text.clear()
    }
}