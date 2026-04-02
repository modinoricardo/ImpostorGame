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
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

class SugerenciasBottomSheet : BaseGameBottomSheet() {

    companion object {
        const val TAG       = "SugerenciasBottomSheet"
        private const val PREFS     = "sugerencias_prefs"
        private const val KEY_EMAIL = "user_email"
    }

    override val animationDuration: Long = 400L
    override val isDraggableSheet: Boolean = false
    override val isHideableSheet: Boolean = false
    override val expandOnStart: Boolean = true

    override fun onSheetReady(behavior: BottomSheetBehavior<View>) {
        behavior.peekHeight = resources.displayMetrics.heightPixels
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(
        if (ThemeManager.esCarmesi(requireContext())) R.layout.bottomsheet_sugerencias_carmesi
        else if (ThemeManager.esFinal(requireContext())) R.layout.bottomsheet_sugerencias_final
        else R.layout.bottomsheet_sugerencias,
        container, false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Teclado: subir contenido cuando aparece el IME ──
        val rootView = view.findViewById<View>(R.id.rootSugerencias)
        val basePaddingBottom = rootView?.paddingBottom ?: 0
        rootView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, basePaddingBottom + imeHeight)
                insets
            }
        }

        // â”€â”€ Aplicar tema â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        val accent = ThemeManager.getAccentColor(requireContext())
        view.findViewById<View>(R.id.rootSugerencias)
            ?.setBackgroundResource(
                when {
                    ThemeManager.esFinal(requireContext()) -> R.drawable.bg_final_sheet_surface
                    ThemeManager.esCarmesi(requireContext()) -> R.drawable.bg_carmesi_sheet
                    else -> ThemeManager.getBgCard(requireContext())
                }
            )
        view.findViewById<TextView>(R.id.txtTituloSugerencias)
            ?.setShadowLayer(12f, 0f, 0f, accent)
        view.findViewById<Button>(R.id.btnEnviarSugerencia)
            ?.setBackgroundResource(ThemeManager.getBtnNeon(requireContext()))

        // â”€â”€ BotÃ³n atrÃ¡s â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        view.findViewById<View>(R.id.btnBackSugerencias)?.setOnClickListener { dismiss() }

        // â”€â”€ Email persistente (SharedPreferences) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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

        // â”€â”€ BotÃ³n Enviar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        val editSugerencia = view.findViewById<EditText>(R.id.editSugerencia)
        val btnEnviar      = view.findViewById<Button>(R.id.btnEnviarSugerencia)
        btnEnviar.setOnClickListener {
            val texto = editSugerencia.text.toString().trim()
            val email = editEmail.text.toString().trim()
            if (texto.isEmpty()) {
                editSugerencia.error = getString(R.string.sug_write_first)
                return@setOnClickListener
            }
            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = getString(R.string.sug_invalid_email)
                return@setOnClickListener
            }
            enviar(texto, email, btnEnviar)
        }
    }

    // â”€â”€ Flujo principal â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private fun enviar(mensaje: String, emailUsuario: String, btnEnviar: Button) {
        if (mensaje.length < 15) {
            Toast.makeText(requireContext(), getString(R.string.sug_too_short), Toast.LENGTH_LONG).show()
            return
        }
        if (BuildConfig.GMAIL_SENDER.isEmpty() || BuildConfig.GMAIL_PASSWORD.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.sug_not_configured), Toast.LENGTH_LONG).show()
            return
        }

        btnEnviar.isEnabled = false
        btnEnviar.text      = getString(R.string.sug_enviando)

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { mandarEmail(mensaje, emailUsuario) }
            } catch (e: Exception) {
                if (isAdded) mostrarError(getString(R.string.sug_send_error), btnEnviar)
                return@launch
            }

            if (isAdded) {
                Toast.makeText(requireContext(), getString(R.string.sug_sent_success), Toast.LENGTH_LONG).show()
                dismiss()
            }
        }
    }

    private fun mostrarError(msg: String, btnEnviar: Button) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        btnEnviar.isEnabled = true
        btnEnviar.text      = getString(R.string.sug_enviar)
    }

    // â”€â”€ Gmail SMTP â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
            "${info.versionName} (build ${PackageInfoCompat.getLongVersionCode(info)})"
        } catch (_: Exception) { "Desconocida" }

        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())

        val cuerpo = buildString {
            appendLine("--- MENSAJE USUARIO ---")
            appendLine(mensajeUsuario)
            appendLine()
            appendLine("=".repeat(40))
            appendLine("USUARIO    : ${emailUsuario.ifEmpty { "AnÃ³nimo" }}")
            appendLine("DISPOSITIVO: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
            appendLine("APP        : ImpostorGame v$appVersion")
            appendLine("FECHA      : $fecha")
            appendLine("=".repeat(40))
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

