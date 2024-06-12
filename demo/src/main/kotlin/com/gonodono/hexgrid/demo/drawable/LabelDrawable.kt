package com.gonodono.hexgrid.demo.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class LabelDrawable(
    private val title: String,
    private val textSize: Float
) : Drawable() {

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.GRAY }

    var label: String = ""
        set(value) {
            if (field == value) return
            field = value
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) = paint.let { paint ->
        val textBounds = tmpRect

        paint.textSize = textSize
        val margin = textSize / 2
        paint.getTextBounds(title, 0, title.length, textBounds)
        canvas.drawText(
            title,
            margin - textBounds.left,
            margin - textBounds.top,
            paint
        )

        paint.textSize = 0.75F * textSize
        paint.getTextBounds(label, 0, label.length, textBounds)
        canvas.drawText(
            label,
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