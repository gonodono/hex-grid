package com.gonodono.hexgrid.demo.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper

class FlashDrawable(private val flashColor: Int) : Drawable() {

    private val handler = Handler(Looper.getMainLooper())

    private var flashing = false
        set(value) {
            if (field == value) return
            field = value
            invalidateSelf()
        }

    private val paint = Paint()

    private val stopFlashing = Runnable {
        flashing = false
    }

    fun flash() {
        flashing = true
        handler.apply {
            removeCallbacks(stopFlashing)
            postDelayed(stopFlashing, 250)
        }
    }

    override fun draw(canvas: Canvas) {
        if (flashing) paint.also {
            it.style = Paint.Style.FILL
            it.color = flashColor
        } else paint.also {
            it.style = Paint.Style.STROKE
            it.color = Color.BLACK
        }
        canvas.drawRect(bounds, paint)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}