package com.ricardomodino.impostorgame.bottomsheets

import android.content.Context
import android.os.Build
import android.util.Patterns
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ricardomodino.impostorgame.BuildConfig
import com.ricardomodino.impostorgame.R
import com.ricardomodino.impostorgame.managers.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SugerenciasBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG       = "SugerenciasBottomSheet"
        private const val PREFS     = "sugerencias_prefs"
        private const val KEY_EMAIL = "user_email"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_sugerencias, container, false)

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return
        bottomSheet.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bottomsheet_rounded)
        bottomSheet.post {
            val h = if (bottomSheet.height > 0) bottomSheet.height
                    else bottomSheet.resources.displayMetrics.heightPixels
            bottomSheet.translationY = h.toFloat()
            bottomSheet.alpha = 0f
            bottomSheet.animate().translationY(0f).alpha(1f)
                .setDuration(400L).setInterpolator(DecelerateInterpolator(2f)).start()
        }
        BottomSheetBehavior.from(bottomSheet).apply {
            state       = BottomSheetBehavior.STATE_EXPANDED
            isDraggable = false
            isHideable  = false
            peekHeight  = bottomSheet.resources.displayMetrics.heightPixels
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Aplicar tema ──────────────────────────────────────────────────────
        val accent = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootSugerencias)
            ?.setBackgroundResource(ThemeManager.getBgCard(requireContext()))
        view.findViewById<TextView>(R.id.txtTituloSugerencias)
            ?.setShadowLayer(12f, 0f, 0f, accent)
        view.findViewById<Button>(R.id.btnEnviarSugerencia)
            ?.setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))

        // ── Botón atrás ───────────────────────────────────────────────────────
        view.findViewById<View>(R.id.btnBackSugerencias)?.setOnClickListener { dismiss() }

        // ── Email persistente (SharedPreferences) ─────────────────────────────
        val editEmail = view.findViewById<EditText>(R.id.editEmail)
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        editEmail.setText(prefs.getString(KEY_EMAIL, ""))
        editEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                prefs.edit().putString(KEY_EMAIL, s?.toString().orEmpty()).apply()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ── Botón Enviar ──────────────────────────────────────────────────────
        val editSugerencia = view.findViewById<EditText>(R.id.editSugerencia)
        val btnEnviar      = view.findViewById<Button>(R.id.btnEnviarSugerencia)
        btnEnviar.setOnClickListener {
            val texto = editSugerencia.text.toString().trim()
            val email = editEmail.text.toString().trim()
            if (texto.isEmpty()) {
                editSugerencia.error = "Escribe tu sugerencia primero"
                return@setOnClickListener
            }
            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = "Email no válido"
                return@setOnClickListener
            }
            enviar(texto, email, btnEnviar)
        }
    }

    // ── Flujo principal ───────────────────────────────────────────────────────

    private fun enviar(mensaje: String, emailUsuario: String, btnEnviar: Button) {
        if (mensaje.length < 15) {
            Toast.makeText(requireContext(), "Tu sugerencia es demasiado corta. Añade más detalle.", Toast.LENGTH_LONG).show()
            return
        }
        if (BuildConfig.GMAIL_SENDER.isEmpty() || BuildConfig.GMAIL_PASSWORD.isEmpty()) {
            Toast.makeText(requireContext(), "El envío no está configurado. Contacta al desarrollador.", Toast.LENGTH_LONG).show()
            return
        }

        btnEnviar.isEnabled = false
        btnEnviar.text      = "Enviando…"

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { mandarEmail(mensaje, emailUsuario) }
            } catch (e: Exception) {
                if (isAdded) mostrarError("Error al enviar. Comprueba tu conexión.", btnEnviar)
                return@launch
            }

            if (isAdded) {
                Toast.makeText(requireContext(), "¡Gracias! Tu sugerencia ha sido enviada.", Toast.LENGTH_LONG).show()
                dismiss()
            }
        }
    }

    private fun mostrarError(msg: String, btnEnviar: Button) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        btnEnviar.isEnabled = true
        btnEnviar.text      = "Enviar"
    }

    // ── Gmail SMTP ────────────────────────────────────────────────────────────

    private fun mandarEmail(mensajeUsuario: String, emailUsuario: String) {
        val props = Properties().apply {
            put("mail.smtp.auth",              "true")
            put("mail.smtp.starttls.enable",   "true")
            put("mail.smtp.host",              "smtp.gmail.com")
            put("mail.smtp.port",              "587")
            put("mail.smtp.ssl.trust",         "smtp.gmail.com")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout",           "15000")
        }

        val sender   = BuildConfig.GMAIL_SENDER
        val password = BuildConfig.GMAIL_PASSWORD
        val receiver = BuildConfig.GMAIL_RECEIVER

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(sender, password.replace(" ", ""))
        })

        val appVersion = try {
            val info = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            "${info.versionName} (build ${info.longVersionCode})"
        } catch (_: Exception) { "Desconocida" }

        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())

        val cuerpo = buildString {
            appendLine("--- MENSAJE USUARIO ---")
            appendLine(mensajeUsuario)
            appendLine()
            appendLine("=".repeat(40))
            appendLine("USUARIO    : ${emailUsuario.ifEmpty { "Anónimo" }}")
            appendLine("DISPOSITIVO: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
            appendLine("APP        : ImpostorGame v$appVersion")
            appendLine("FECHA      : $fecha")
            appendLine("=".repeat(40))
            appendLine("(Responde a este email para añadir esta tarea a Notion)")
        }

        val titulo = mensajeUsuario.take(55).trimEnd()

        MimeMessage(session).apply {
            setFrom(InternetAddress(sender))
            setRecipient(Message.RecipientType.TO, InternetAddress(receiver))
            subject = "[ImpostorGame] $titulo"
            setText(cuerpo, "UTF-8")
        }.also { Transport.send(it) }
    }
}
