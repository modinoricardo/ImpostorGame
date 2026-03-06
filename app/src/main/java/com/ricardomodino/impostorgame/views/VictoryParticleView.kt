package com.ricardomodino.impostorgame.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class VictoryParticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isImpostor = true

    private val colorsPalette by lazy {
        if (isImpostor)
            listOf(Color.parseColor("#FF1744"), Color.parseColor("#FF6D00"), Color.parseColor("#FF9100"), Color.WHITE)
        else
            listOf(Color.parseColor("#00E5FF"), Color.parseColor("#00BFA5"), Color.parseColor("#64DD17"), Color.WHITE)
    }

    fun setGanador(ganador: String) {
        isImpostor = ganador == "IMPOSTORES"
    }

    data class Particle(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        var radius: Float, var color: Int,
        var alpha: Float, var decay: Float
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        repeat(120) { spawnParticle(w.toFloat(), h.toFloat()) }
    }

    private fun spawnParticle(w: Float, h: Float) {
        val angle = Random.nextFloat() * 360f
        val speed = Random.nextFloat() * 8f + 2f
        particles.add(Particle(
            x = w / 2f, y = h / 2f,
            vx = cos(Math.toRadians(angle.toDouble())).toFloat() * speed,
            vy = sin(Math.toRadians(angle.toDouble())).toFloat() * speed,
            radius = Random.nextFloat() * 12f + 4f,
            color = colorsPalette.random(),
            alpha = 1f,
            decay = Random.nextFloat() * 0.01f + 0.005f
        ))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val toRemove = mutableListOf<Particle>()
        for (p in particles) {
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.1f // gravedad
            p.alpha -= p.decay
            if (p.alpha <= 0) { toRemove.add(p); continue }
            paint.color = p.color
            paint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x, p.y, p.radius, paint)
        }
        particles.removeAll(toRemove)
        if (particles.size < 60) {
            repeat(5) { spawnParticle(width.toFloat(), height.toFloat()) }
        }
        invalidate()
    }
}