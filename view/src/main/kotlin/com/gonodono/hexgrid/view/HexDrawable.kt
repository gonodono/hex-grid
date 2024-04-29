package com.gonodono.hexgrid.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt

/**
 * Optional background [Drawable] for children of a [HexGridView] that gives
 * them the same shape as the View's grid hexagon.
 *
 * For normal usage, you don't need to deal with this class at all, directly.
 * The `app:layout_hexBackground*` attributes are available to apply this
 * feature from layout XML, and the [HexGridView.applyHexBackground] function
 * offers the same options in code. If you extend this class, you'll need to
 * instantiate and set the drawable manually, but you still must use
 * applyHexBackground to modify the inset or color.
 *
 * The current implementation simply fills its area with a solid color, and
 * sets its [Outline] to the grid View's hexagon shape, so that it can cast
 * material shadows. The inset is accounted for during child layout, so this
 * class is expected to draw right up to its bounds.
 *
 * It is open to allow further customizations, like possibly using a
 * [Shader][android.graphics.Shader] with the [Paint][android.graphics.Paint]
 * instead of a solid color.
 */
open class HexDrawable(
    private val hexGridView: HexGridView,
    @ColorInt fillColor: Int = Color.WHITE
) : Drawable() {

    /**
     * This holds the hexagon shape set as the drawable's Outline.
     *
     * You should not modify this.
     */
    protected val path = Path()

    /**
     * The local Paint object.
     *
     * By default, this is used only to paint the fill color.
     */
    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
    }

    /**
     * The color used to fill the drawable's area.
     */
    @get:ColorInt
    @setparam:ColorInt
    var fillColor: Int
        get() = paint.color
        set(value) {
            if (paint.color == value) return
            paint.color = value
            invalidateSelf()
        }

    @CallSuper
    override fun onBoundsChange(bounds: Rect) {
        hexGridView.getHexagonPath(path, bounds)
    }

    @CallSuper
    override fun getOutline(outline: Outline) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            outline.setPath(path)
        } else {
            @Suppress("DEPRECATION")
            outline.setConvexPath(path)
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}