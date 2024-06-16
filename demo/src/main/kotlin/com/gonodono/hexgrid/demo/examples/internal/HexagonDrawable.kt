package com.gonodono.hexgrid.demo.examples.internal

import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build

class HexagonDrawable(isHorizontal: Boolean = true) : Drawable() {

    private val hexagon = Path()

    var isHorizontal: Boolean = isHorizontal
        set(value) {
            if (field == value) return
            field = value
            onBoundsChange(bounds)
            invalidateSelf()
        }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onBoundsChange(bounds: Rect) {
        val major = when {
            isHorizontal -> bounds.width().toFloat()
            else -> bounds.height().toFloat()
        }
        val quarterMajor = major / 4F
        val minor = when {
            isHorizontal -> bounds.height().toFloat()
            else -> bounds.width().toFloat()
        }
        val halfMinor = minor / 2F

        val coordinatePairs = arrayOf(
            floatArrayOf(0F, halfMinor),
            floatArrayOf(quarterMajor, 0F),
            floatArrayOf(3 * quarterMajor, 0F),
            floatArrayOf(major, halfMinor),
            floatArrayOf(3 * quarterMajor, minor),
            floatArrayOf(quarterMajor, minor)
        )

        val path = hexagon.apply { rewind() }
        coordinatePairs.forEachIndexed { index, (c1, c2) ->
            if (isHorizontal) when (index) {
                0 -> path.moveTo(c1, c2)
                else -> path.lineTo(c1, c2)
            } else when (index) {
                0 -> path.moveTo(c2, c1)
                else -> path.lineTo(c2, c1)
            }
        }
        path.close()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(hexagon, paint)
    }

    override fun setTintList(tint: ColorStateList?) {
        paint.colorFilter = if (tint == null) {
            null
        } else if (Build.VERSION.SDK_INT >= 29) {
            BlendModeColorFilter(tint.defaultColor, BlendMode.SRC_IN)
        } else {
            PorterDuffColorFilter(tint.defaultColor, PorterDuff.Mode.SRC_IN)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}