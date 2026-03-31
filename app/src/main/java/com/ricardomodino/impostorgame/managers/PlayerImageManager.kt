package com.ricardomodino.impostorgame.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Carga y cachea las imágenes de jugadores desde assets/imagenesJugadoresV2/.
 * Si la carpeta V2 está vacía, usa V1 como fallback.
 */
object PlayerImageManager {

    private const val FOLDER_V2 = "imagenesJugadoresV2"
    private const val FOLDER_V1 = "imagenesJugadoresV1"

    private var cachedBitmaps: List<Bitmap>? = null
    private var activeFolder: String? = null

    /** Devuelve la lista de bitmaps disponibles (cacheada). */
    fun getImages(context: Context): List<Bitmap> {
        cachedBitmaps?.let { return it }

        val assets = context.assets
        var folder = FOLDER_V2
        var files = assets.list(folder)?.filter { isImage(it) }?.sorted() ?: emptyList()

        if (files.isEmpty()) {
            folder = FOLDER_V1
            files = assets.list(folder)?.filter { isImage(it) }?.sorted() ?: emptyList()
        }

        activeFolder = folder
        val bitmaps = files.mapNotNull { fileName ->
            try {
                assets.open("$folder/$fileName").use { BitmapFactory.decodeStream(it) }
            } catch (_: Exception) {
                null
            }
        }

        cachedBitmaps = bitmaps
        return bitmaps
    }

    /** Devuelve una lista shuffled del tamaño pedido, reciclando si hace falta. */
    fun getShuffledPool(context: Context, size: Int): List<Bitmap> {
        val images = getImages(context)
        if (images.isEmpty()) return emptyList()

        val pool = mutableListOf<Bitmap>()
        while (pool.size < size) {
            pool.addAll(images.shuffled())
        }
        return pool.take(size)
    }

    /** Devuelve una imagen aleatoria. */
    fun getRandom(context: Context): Bitmap? {
        val images = getImages(context)
        return if (images.isNotEmpty()) images.random() else null
    }

    /** Limpia la caché (llamar si se cambian las imágenes en caliente). */
    fun clearCache() {
        cachedBitmaps = null
        activeFolder = null
    }

    private fun isImage(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp")
    }
}
