package com.gonodono.hexgrid.demo.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class LabelDrawable(
    private val label: String?,
    private val textSize: Float
) : Drawable() {

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY }

    var info: String? = null
        set(value) {
            if (field == value) return
            field = value
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) {
        val paint = paint
        val label = label
        val info = info
        val textBounds = tmpRect

        val margin = textSize / 2

        if (label != null) {
            paint.textSize = textSize
            paint.getTextBounds(label, 0, label.length, textBounds)
            if (info != null) {
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(
                    label,
                    margin - textBounds.left,
                    margin - textBounds.top,
                    paint
                )
            } else {
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(
                    label,
                    bounds.centerX().toFloat(),
                    margin - textBounds.top,
                    paint
                )
            }
        }

        if (info == null) return

        paint.textSize = 0.8F * textSize
        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(info, 0, info.length, textBounds)
        canvas.drawText(
            info,
            bounds.width() - textBounds.width() - margin,
            margin - textBounds.top,
            paint
        )
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    private val tmpRect = Rect()
}