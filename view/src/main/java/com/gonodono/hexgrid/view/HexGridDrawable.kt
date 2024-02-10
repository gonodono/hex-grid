package com.gonodono.hexgrid.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.withClip
import com.gonodono.hexgrid.core.GridUi
import com.gonodono.hexgrid.data.EmptyGrid
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.LayoutSpecs
import kotlin.reflect.KMutableProperty0

/**
 * The drawable version of the library's hex grid.
 *
 * Similar in operation to the other versions, but this one doesn't support cell
 * content, and it's not inherently interactive.
 */
class HexGridDrawable(grid: Grid? = null) : Drawable() {

    private val gridUi = GridUi()

    /**
     * The HexGridDrawable's [Grid].
     */
    var grid: Grid = EmptyGrid
        set(value) {
            if (field == value) return
            field = value
            gridUi.grid = value
            invalidateSelf()
        }

    /**
     * The HexGridDrawable's current [LayoutSpecs].
     */
    var layoutSpecs: LayoutSpecs
        get() = gridUi.layoutSpecs
        set(value) {
            if (gridUi.layoutSpecs == value) return
            gridUi.layoutSpecs = value
            invalidateSelf()
        }

    /**
     * Color of the cells' outlines.
     */
    @get:ColorInt
    @setparam:ColorInt
    var strokeColor: Int by invalidating(gridUi::strokeColor)

    /**
     * Color of the cells' interior normally.
     */
    @get:ColorInt
    @setparam:ColorInt
    var fillColor: Int by invalidating(gridUi::fillColor)

    /**
     * Color of the cells' interior when its selected state is true.
     */
    @get:ColorInt
    @setparam:ColorInt
    var selectColor: Int by invalidating(gridUi::selectColor)

    /**
     * Whether to show each cell's row index.
     */
    var showRowIndices: Boolean by invalidating(gridUi::showRowIndices)

    /**
     * Whether to show each cell's column index.
     */
    var showColumnIndices: Boolean by invalidating(gridUi::showColumnIndices)

    /**
     * Whether to clip the grid, or let it draw out of bounds, if it's big
     * enough to do so.
     */
    var clipToBounds: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            invalidateSelf()
        }

    init {
        if (grid != null) this.grid = grid
    }

    override fun onBoundsChange(bounds: Rect) {
        gridUi.calculateSize(
            hasBoundedWidth = true,
            hasBoundedHeight = true,
            hasFixedWidth = true,
            hasFixedHeight = true,
            availableWidth = bounds.width(),
            availableHeight = bounds.height()
        )
    }

    override fun draw(canvas: Canvas) {
        if (clipToBounds) {
            canvas.withClip(bounds) { gridUi.drawGrid(canvas) }
        } else {
            gridUi.drawGrid(canvas)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    private fun <T> invalidating(wrapped: KMutableProperty0<T>) =
        relayChange(wrapped, ::invalidateSelf)
}