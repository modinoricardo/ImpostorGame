package com.ricardomodino.impostorgame.data.remote

import com.ricardomodino.impostorgame.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SupabaseClient {

    private val baseUrl = BuildConfig.SUPABASE_URL
    private val apiKey  = BuildConfig.SUPABASE_KEY

    fun get(endpoint: String): JSONArray {
        val url  = URL("$baseUrl/rest/v1/$endpoint")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "GET"
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 10_000
            readTimeout    = 15_000
        }
        return try {
            val text = conn.inputStream.bufferedReader().readText()
            JSONArray(text)
        } finally {
            conn.disconnect()
        }
    }

    fun post(table: String, body: String): JSONObject? {
        val url  = URL("$baseUrl/rest/v1/$table")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Prefer", "return=representation")
            doOutput = true
            connectTimeout = 10_000
            readTimeout    = 15_000
        }
        return try {
            conn.outputStream.use { it.write(body.toByteArray()) }
            val text = conn.inputStream.bufferedReader().readText()
            JSONArray(text).optJSONObject(0)
        } finally {
            conn.disconnect()
        }
    }

    // Upsert: POST con Prefer merge-duplicates — PostgREST lo trata como UPDATE si la fila ya existe.
    // body debe incluir el campo "id".
    fun patch(table: String, id: Long, body: String) {
        // Inyectar "id" en el body para que el upsert funcione
        val json = org.json.JSONObject(body).apply { put("id", id) }
        val url  = URL("$baseUrl/rest/v1/$table")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
            doOutput = true
            connectTimeout = 10_000
            readTimeout    = 15_000
        }
        try {
            conn.outputStream.use { it.write(json.toString().toByteArray()) }
            val code = conn.responseCode
            if (code >= 400) {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
                throw RuntimeException("PATCH $table failed: HTTP $code – $error")
            }
        } finally {
            conn.disconnect()
        }
    }

    // Inserta ignorando duplicados (útil para publicar seed sin error si ya existe)
    fun postOrIgnore(table: String, body: String) {
        val url  = URL("$baseUrl/rest/v1/$table")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "POST"
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Prefer", "resolution=ignore-duplicates,return=minimal")
            doOutput = true
            connectTimeout = 10_000
            readTimeout    = 15_000
        }
        try {
            conn.outputStream.use { it.write(body.toByteArray()) }
            conn.responseCode // consume respuesta
        } finally {
            conn.disconnect()
        }
    }

    fun delete(table: String, id: Long) {
        val url  = URL("$baseUrl/rest/v1/$table?id=eq.$id")
        val conn = url.openConnection() as HttpURLConnection
        conn.apply {
            requestMethod = "DELETE"
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Authorization", "Bearer $apiKey")
            connectTimeout = 10_000
            readTimeout    = 15_000
        }
        try {
            conn.responseCode
        } finally {
            conn.disconnect()
        }
    }
}
