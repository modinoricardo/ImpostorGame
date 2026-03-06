package com.ricardomodino.impostorgame.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Gestiona las fotos selfie de cada jugador en memoria + disco.
 * Las fotos se guardan en el cache dir con nombre basado en el jugador.
 */
object SelfieManager {

    private val cache = mutableMapOf<String, Bitmap>()
    private var cacheDir: File? = null

    fun init(dir: File) { cacheDir = dir }

    fun saveBitmap(playerName: String, bmp: Bitmap) {
        cache[playerName] = bmp
        val file = fileFor(playerName) ?: return
        try {
            file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        } catch (_: Exception) {}
    }

    fun getBitmap(playerName: String): Bitmap? {
        cache[playerName]?.let { return it }
        val file = fileFor(playerName) ?: return null
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath).also { cache[playerName] = it }
        } catch (_: Exception) { null }
    }

    fun clear() {
        cache.clear()
        cacheDir?.listFiles()?.forEach { if (it.name.startsWith("selfie_")) it.delete() }
    }

    private fun fileFor(name: String): File? {
        val dir = cacheDir ?: return null
        val safe = name.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return File(dir, "selfie_$safe.jpg")
    }
}