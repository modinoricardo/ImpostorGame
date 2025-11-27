package com.example.impostorgame

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.graphics.Rect
import android.view.TouchDelegate

class EditPlayersBottomSheet : BottomSheetDialogFragment() {

    private lateinit var adapter: PlayerAdapterEdit

    private lateinit var editTextNewPlayer: EditText
    private lateinit var imageViewAniadirJugador: ImageView
    private lateinit var btnConfirm: Button

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        // Avisar a la Activity
        (activity as? MainActivity)?.onBottomSheetClosed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // Fondo redondeado como antes
        bottomSheet.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bottomsheet_rounded
        )

        // ANIMACIÓN DE ENTRADA EXAGERADA PARA VER SI FUNCIONA
        bottomSheet.post {
            // Altura de referencia: si no tiene, usamos la altura de pantalla
            val h = if (bottomSheet.height > 0) {
                bottomSheet.height
            } else {
                bottomSheet.resources.displayMetrics.heightPixels
            }

            // Empezamos MUY abajo y transparente
            bottomSheet.translationY = h.toFloat()      // fuera de pantalla
            bottomSheet.alpha = 0f

            bottomSheet.animate().translationY(0f)                      // sube hasta su posición
                .alpha(1f)                             // fade in
                .setDuration(1500L)                    // 1.5 segundos para que se note
                .setInterpolator(DecelerateInterpolator(2f)).start()
        }

        val behavior = BottomSheetBehavior.from(bottomSheet)

        behavior.isDraggable = false   // BLOQUEAR el swipe para cerrar
        behavior.isHideable = false    // No permitir que se oculte

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity()).get(PlayerViewModel::class.java)

        btnConfirm = view.findViewById(R.id.btnConfirm)
        editTextNewPlayer = view.findViewById(R.id.editTextNewPlayer)
        imageViewAniadirJugador = view.findViewById(R.id.imageViewAniadirJugador)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPlayersDialog)

        // Tomar lista actual del ViewModel
        val currentPlayers = viewModel.players.value ?: emptyList()

        adapter = PlayerAdapterEdit(
            currentPlayers,
            onDeleteClick = { index ->
                viewModel.removeAt(index)
            },
            onEditClick = { index, newName ->
                viewModel.renameAt(index, newName)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observar cambios en LiveData
        viewModel.players.observe(viewLifecycleOwner) { nuevaLista ->
            adapter.updatePlayers(nuevaLista)
        }

        // Añadir jugador
        imageViewAniadirJugador.setOnClickListener {
            val nameNewPlayer = editTextNewPlayer.text.toString()

            if (nameNewPlayer.lowercase() in listOf("ste", "staicy")) {
                mensajeAlerta(
                    "Demasiado amor",
                    "Este nombre desprende niveles extraordinarios de cariño y ternura. Podría sobrecargar el sistema."
                )
                aniadirJugador(nameNewPlayer.trim(), viewModel)

            } else if (!nameNewPlayer.isBlank()) {
                aniadirJugador(nameNewPlayer.trim(), viewModel)

            } else {
                if (nameNewPlayer.isEmpty()) {
                    mensajeAlerta(
                        "Nombre vacío",
                        "Antes de añadir un jugador, necesitas escribir al menos un nombre."
                    )
                } else {
                    mensajeAlerta(
                        "¿Espacios otra vez?",
                        "No vale añadir jugadores invisibles. Escribe un nombre real."
                    )
                }
            }
        }

        btnConfirm.setOnClickListener { dismiss() }

    }

    fun mensajeAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun aniadirJugador(newPlayer: String, viewModel: PlayerViewModel) {
        viewModel.addPlayer(newPlayer)
        editTextNewPlayer.text.clear()
    }

}
