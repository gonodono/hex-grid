package com.gonodono.hexgrid.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.withClip
import com.gonodono.hexgrid.core.GridUi
import com.gonodono.hexgrid.core.LayoutSpecs
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.EmptyGrid
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.HexOrientation
import kotlin.reflect.KMutableProperty0

/**
 * The Drawable version of the library's hex grid.
 *
 * Similar in operation to the other versions, except:
 * + This one doesn't support cell Views, because it's a [Drawable].
 * + It's not inherently interactive; i.e., it has no click listener interface.
 * + It can't wrap its content; it simply uses the bounds and mode as set.
 */
class HexGridDrawable(grid: Grid? = null) : Drawable() {

    private val gridUi = GridUi()

    /**
     * The HexGridDrawable's current [Grid].
     */
    var grid: Grid = EmptyGrid
        set(value) {
            if (field == value) return
            field = value
            gridUi.grid = value
            invalidateSelf()
        }

    /**
     * The HexGridDrawable's current [FitMode].
     */
    var fitMode: FitMode by changeSpecs(gridUi.layoutSpecs.fitMode) { specs, value ->
        specs.copy(fitMode = value)
    }

    /**
     * The HexGridDrawable's current [CrossMode].
     */
    var crossMode: CrossMode by changeSpecs(gridUi.layoutSpecs.crossMode) { specs, value ->
        specs.copy(crossMode = value)
    }

    /**
     * The HexGridDrawable's current [HexOrientation].
     */
    var hexOrientation: HexOrientation by changeSpecs(gridUi.layoutSpecs.hexOrientation) { specs, value ->
        specs.copy(hexOrientation = value)
    }

    /**
     * The HexGridDrawable's current stroke width for the cells' outline.
     */
    var strokeWidth: Float by changeSpecs(gridUi.layoutSpecs.strokeWidth) { specs, value ->
        specs.copy(strokeWidth = value)
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
     * Color of the cells' indices, if shown.
     */
    @get:ColorInt
    @setparam:ColorInt
    var indexColor: Int by invalidating(gridUi::indexColor)

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

    private fun <T> changeSpecs(
        initialValue: T,
        createSpecs: (current: LayoutSpecs, newValue: T) -> LayoutSpecs
    ) = onChange(initialValue) { newValue ->
        gridUi.layoutSpecs = createSpecs(gridUi.layoutSpecs, newValue)
        invalidateSelf()
    }

    private fun <T> invalidating(wrapped: KMutableProperty0<T>) =
        relayChange(wrapped, ::invalidateSelf)
}