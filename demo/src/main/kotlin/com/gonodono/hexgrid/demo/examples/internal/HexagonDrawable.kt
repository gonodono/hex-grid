package com.gonodono.hexgrid.demo.examples.internal

import android.content.res.ColorStateList
import android.content.res.Resources
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
import android.util.AttributeSet
import com.gonodono.hexgrid.demo.R
import org.xmlpull.v1.XmlPullParser

/**
 * [HexagonDrawable] uses only basic arithmetic, but it doesn't enforce regular
 * hexagons. It's left up to the user to set properly proportioned bounds.
 */
class HexagonDrawable(isHorizontal: Boolean = true) : Drawable() {

    private val path = Path()

    /**
     * Determines whether the hexagon has a major axis aligned horizontally or
     * vertically.
     */
    var isHorizontal: Boolean = isHorizontal
        set(value) {
            if (field == value) return
            field = value
            onBoundsChange(bounds)
            invalidateSelf()
        }

    /**
     * The [Paint] is public for these examples in order to allow simple
     * external modifications without additional setters.
     */
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * [HexagonDrawable] isn't set up for shared state currently, so we just
     * directly assign the property here.
     *
     * Only the orientation is handled for now, but it should be obvious how to
     * add any other attributes that might be required. Refer to
     * `res/drawable/hexagon_vertical.xml` for an example.
     */
    override fun inflate(
        r: Resources,
        parser: XmlPullParser,
        attrs: AttributeSet,
        theme: Resources.Theme?
    ) {
        val a = obtainAttributes(r, attrs, theme, R.styleable.HexagonDrawable)
        isHorizontal =
            a.getInt(R.styleable.HexagonDrawable_hexOrientation, 0) == 0
        a.recycle()
    }

    /**
     * This routine simply places the vertices along the edges with the same
     * spacing as regular hexagons.
     *
     * Since this class doesn't enforce regular hexagons, the real dimensions
     * may not even be in the right relation to each other; i.e., the normally
     * shorter dimension may actually be the longer one.
     *
     * + The "short" sides each have one vertex right in the middle.
     *
     * + The "long" sides each have one vertex at a quarter length, and another
     *   at three-quarters.
     *
     * Knowing that, it's just a matter of divvying up the respective lengths,
     * and then connecting the dots.
     */
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

        with(path) {
            rewind()
            if (isHorizontal) {
                moveTo(0F, halfMinor)
                lineTo(quarterMajor, 0F)
                lineTo(3 * quarterMajor, 0F)
                lineTo(major, halfMinor)
                lineTo(3 * quarterMajor, minor)
                lineTo(quarterMajor, minor)
                lineTo(0F, halfMinor)
            } else {
                moveTo(halfMinor, 0F)
                lineTo(0F, quarterMajor)
                lineTo(0F, 3 * quarterMajor)
                lineTo(halfMinor, major)
                lineTo(minor, 3 * quarterMajor)
                lineTo(minor, quarterMajor)
                lineTo(halfMinor, 0F)
            }
        }
    }

    /**
     * This is currently geared toward a fill style, as the draw does not
     * account for possible stroke widths at the bounds (which for the "pointy"
     * ends are 2/√3 times the actual width, by the way).
     */
    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    /**
     * This allows the `android:backgroundTint` attribute to be applied. This is
     * very simplistic, currently, and it does not account for state.
     */
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