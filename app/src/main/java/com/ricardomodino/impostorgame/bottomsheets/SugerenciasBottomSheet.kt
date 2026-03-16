package com.ricardomodino.impostorgame.bottomsheets

import android.content.Context
import android.os.Build
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
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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

    private data class AnalisisIA(
        val esValido    : Boolean,
        val titulo      : String,
        val descripcion : String,
        val promptIA    : String
    )

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
            if (texto.isEmpty()) {
                editSugerencia.error = "Escribe tu sugerencia primero"
                return@setOnClickListener
            }
            enviar(texto, editEmail.text.toString().trim(), btnEnviar)
        }
    }

    // ── Flujo principal ───────────────────────────────────────────────────────

    private fun enviar(mensaje: String, emailUsuario: String, btnEnviar: Button) {
        btnEnviar.isEnabled = false
        btnEnviar.text      = "Validando…"

        lifecycleScope.launch {
            // Paso 1: Gemini valida — si falla por cuota/red usamos validación local
            val analisis = try {
                withContext(Dispatchers.IO) { llamarGemini(mensaje) }
            } catch (e: Exception) {
                // Gemini no disponible: validación básica local
                if (mensaje.length < 15) {
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Tu sugerencia es demasiado corta. Añade más detalle.",
                            Toast.LENGTH_LONG
                        ).show()
                        btnEnviar.isEnabled = true
                        btnEnviar.text      = "Enviar"
                    }
                    return@launch
                }
                // Mensaje suficientemente largo → lo tratamos como válido
                AnalisisIA(
                    esValido    = true,
                    titulo      = mensaje.take(55).trimEnd(),
                    descripcion = "Sugerencia recibida directamente del usuario (validación IA no disponible).",
                    promptIA    = ""
                )
            }

            if (!analisis.esValido) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Tu mensaje no parece una sugerencia válida. Intenta ser más específico.",
                        Toast.LENGTH_LONG
                    ).show()
                    btnEnviar.isEnabled = true
                    btnEnviar.text      = "Enviar"
                }
                return@launch
            }

            // Paso 2: Email
            if (isAdded) btnEnviar.text = "Enviando…"
            try {
                withContext(Dispatchers.IO) { mandarEmail(analisis, mensaje, emailUsuario) }
            } catch (e: Exception) {
                if (isAdded) mostrarError("Email: ${e.javaClass.simpleName} — ${e.message}", btnEnviar)
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

    // ── Gemini API ────────────────────────────────────────────────────────────

    private fun llamarGemini(mensaje: String): AnalisisIA {
        val url = URL(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash" +
            ":generateContent?key=${BuildConfig.GEMINI_API_KEY}"
        )
        val prompt = """
            Eres un asistente que evalúa sugerencias para una app de juego llamada "El Impostor Game".
            Responde SOLO con JSON válido, sin texto extra ni bloques markdown:
            {
              "esValido": true o false,
              "titulo": "Título corto de la sugerencia (máx 60 caracteres, vacío si no válido)",
              "descripcion": "Qué habría que hacer a nivel conceptual, sin código (vacío si no válido)",
              "promptIA": "Prompt listo para copiar y pegar en una IA para implementar la mejora (vacío si no válido)"
            }
            Es válido si es una sugerencia constructiva para la app.
            No es válido si es spam, insulto o texto sin sentido.
            Mensaje del usuario: "$mensaje"
        """.trimIndent()

        val body = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
        }.toString()

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10_000
            readTimeout    = 15_000
            doOutput       = true
            outputStream.use { it.write(body.toByteArray()) }
        }

        val raw = if (conn.responseCode in 200..299) {
            conn.inputStream.bufferedReader().readText()
        } else {
            val err = conn.errorStream?.bufferedReader()?.readText() ?: "sin cuerpo"
            conn.disconnect()
            throw Exception("HTTP ${conn.responseCode}: $err")
        }
        conn.disconnect()

        val text = JSONObject(raw)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(text)
        return AnalisisIA(
            esValido    = json.getBoolean("esValido"),
            titulo      = json.optString("titulo",      ""),
            descripcion = json.optString("descripcion", ""),
            promptIA    = json.optString("promptIA",    "")
        )
    }

    // ── Gmail SMTP ────────────────────────────────────────────────────────────

    private fun mandarEmail(analisis: AnalisisIA, mensajeUsuario: String, emailUsuario: String) {
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
            override fun getPasswordAuthentication() = PasswordAuthentication(sender, password)
        })

        val cuerpo = buildString {
            appendLine("SUGERENCIA IMPOSTOR GAME")
            appendLine("=".repeat(40))
            appendLine("USUARIO   : ${emailUsuario.ifEmpty { "No proporcionado" }}")
            appendLine("DISPOSITIVO: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
            appendLine()
            appendLine("--- ANALISIS IA ---")
            appendLine(analisis.descripcion)
            appendLine()
            appendLine("--- PROMPT IA ---")
            appendLine(analisis.promptIA)
            appendLine()
            appendLine("--- MENSAJE USUARIO ---")
            appendLine(mensajeUsuario)
            appendLine()
            appendLine("=".repeat(40))
            appendLine("(Responde a este email para añadir esta tarea a Notion)")
        }

        MimeMessage(session).apply {
            setFrom(InternetAddress(sender))
            setRecipient(Message.RecipientType.TO, InternetAddress(receiver))
            subject = "[ImpostorGame] ${analisis.titulo}"
            setText(cuerpo, "UTF-8")
        }.also { Transport.send(it) }
    }
}
