package com.gonodono.hexgrid.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.core.graphics.toRectF
import androidx.core.graphics.withMatrix
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.EmptyGrid
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.HexOrientation
import com.gonodono.hexgrid.data.isHorizontal
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

class GridUi {

    private val hexagon = Hexagon(DefaultLayoutSpecs.isHexHorizontal)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val measureData = MeasureData()

    private val drawMatrix = Matrix()

    private val touchMatrix = Matrix()

    private val actualSize = PointF()

    private val stepSize = PointF()

    private val cellSize = PointF()

    var grid: Grid = EmptyGrid
        set(value) {
            if (field == value) return
            field = value
            calculate()
        }

    var layoutSpecs: LayoutSpecs = DefaultLayoutSpecs
        set(value) {
            if (field == value) return
            field = value
            hexagon.isHorizontal = value.isHexHorizontal
            calculate()
        }

    var strokeColor: Int = Color.BLACK

    var fillColor: Int = Color.TRANSPARENT

    var selectColor: Int = Color.GRAY

    var showRowIndices: Boolean = false

    var showColumnIndices: Boolean = false

    fun calculateSize(
        hasBoundedWidth: Boolean,
        hasBoundedHeight: Boolean,
        hasFixedWidth: Boolean,
        hasFixedHeight: Boolean,
        availableWidth: Int,
        availableHeight: Int,
        insetLeft: Int = 0,
        insetTop: Int = 0,
        insetRight: Int = 0,
        insetBottom: Int = 0,
    ): Size {
        val isFitColumns = layoutSpecs.fitMode == FitMode.FitColumns

        check(
            isFitColumns && hasBoundedWidth ||
                    !isFitColumns && hasBoundedHeight
        ) { "Cannot fit unspecified dimension" }
        check(
            layoutSpecs.crossMode != CrossMode.ScaleToFit ||
                    isFitColumns && hasBoundedHeight ||
                    !isFitColumns && hasBoundedWidth
        ) { "Cannot scale to unspecified dimension" }

        val isWrapContent = isFitColumns && !hasFixedHeight ||
                !isFitColumns && !hasFixedWidth

        with(measureData) {
            this.isFitColumns = isFitColumns
            this.isWrapContent = isWrapContent
            this.availableWidth = availableWidth
            this.availableHeight = availableHeight
            this.insetLeft = insetLeft
            this.insetTop = insetTop
            this.insetRight = insetRight
            this.insetBottom = insetBottom
        }

        calculate()

        return if (isFitColumns && !hasFixedHeight) {
            Size(availableWidth, actualSize.y.roundToInt())
        } else if (!isFitColumns && !hasFixedWidth) {
            Size(actualSize.x.roundToInt(), availableHeight)
        } else {
            Size(availableWidth, availableHeight)
        }
    }

    private fun calculate() = with(measureData) {
        val isHorizontal = hexagon.isHorizontal
        val isFitMajor = isFitColumns == isHorizontal

        val lineThicknessMinor = layoutSpecs.strokeWidth.coerceAtLeast(1F)
        val lineThicknessMajor = 2 / sqrt(3F) * lineThicknessMinor
        val fitLineThickness: Float
        val crossLineThickness: Float
        if (isFitMajor) {
            fitLineThickness = lineThicknessMajor
            crossLineThickness = lineThicknessMinor
        } else {
            fitLineThickness = lineThicknessMinor
            crossLineThickness = lineThicknessMajor
        }

        val maxWidth = availableWidth - insetLeft - insetRight
        val maxHeight = availableHeight - insetTop - insetBottom
        val fitCount: Int
        val crossCount: Int
        val fitDimension: Float
        if (isFitColumns) {
            fitCount = grid.columnCount
            crossCount = grid.rowCount
            fitDimension = maxWidth - fitLineThickness
        } else {
            fitCount = grid.rowCount
            crossCount = grid.columnCount
            fitDimension = maxHeight - fitLineThickness
        }

        val hexagonSide = when {
            isFitMajor -> fitDimension * 2 / (3 * fitCount + 1)
            else -> fitDimension * 2 / sqrt(3F) / (fitCount + 1)
        }
        with(hexagon) {
            setSideLength(hexagonSide)
            if (isHorizontal) {
                cellSize.set(2 * side, minor)
                stepSize.set(3 * halfSide, halfMinor)
            } else {
                cellSize.set(minor, 2 * side)
                stepSize.set(halfMinor, 3 * halfSide)
            }
        }
        val crossDimension = when {
            isFitMajor -> hexagonSide * sqrt(3F) / 2 * (crossCount + 1)
            else -> hexagonSide * (3 * crossCount + 1) / 2
        }

        val marginHorizontal: Float
        val marginVertical: Float
        val size = actualSize
        if (isFitColumns) {
            marginHorizontal = fitLineThickness / 2
            marginVertical = crossLineThickness / 2
            size.set(
                fitDimension + fitLineThickness,
                crossDimension + crossLineThickness
            )
        } else {
            marginHorizontal = crossLineThickness / 2
            marginVertical = fitLineThickness / 2
            size.set(
                crossDimension + crossLineThickness,
                fitDimension + fitLineThickness
            )
        }

        with(drawMatrix) {
            reset()
            var offsetX = insetLeft + marginHorizontal
            var offsetY = insetTop + marginVertical
            if (!isWrapContent) when (layoutSpecs.crossMode) {

                CrossMode.AlignStart -> {}  // no-op

                CrossMode.AlignCenter -> {
                    offsetX += (maxWidth - size.x) / 2
                    offsetY += (maxHeight - size.y) / 2
                }

                CrossMode.AlignEnd -> {
                    offsetX += maxWidth - size.x
                    offsetY += maxHeight - size.y
                }

                CrossMode.ScaleToFit -> setScale(
                    maxWidth / size.x,
                    maxHeight / size.y
                )
            }
            preTranslate(offsetX, offsetY)
            invert(touchMatrix)
        }
    }

    fun getHexagonPath(outPath: Path, bounds: Rect) {
        val matrix = getCellToBoundsMatrix(bounds)
        hexagon.getPath(outPath, matrix)
    }

    fun getHexagonPathBuilder(bounds: Rect): Path.() -> Unit {
        val matrix = getCellToBoundsMatrix(bounds)
        return hexagon.getPathBuilder(matrix)
    }

    private fun getCellToBoundsMatrix(bounds: Rect): Matrix {
        val source = tmpBoundsF.apply {
            set(0F, 0F, cellSize.x, cellSize.y)
        }
        return tmpMatrix.apply {
            setRectToRect(
                source,
                bounds.toRectF(),
                Matrix.ScaleToFit.FILL
            )
        }
    }

    fun getCellItemBounds(
        address: Grid.Address,
        inset: Float,
        outBounds: Rect
    ) {
        val insetX: Float
        val insetY: Float
        if (layoutSpecs.hexOrientation.isHorizontal) {
            insetX = 2 / sqrt(3F) * inset
            insetY = inset
        } else {
            insetX = inset
            insetY = 2 / sqrt(3F) * inset
        }
        val boundsF = tmpBoundsF.also { tmp ->
            getCellBounds(address, tmp)
            drawMatrix.mapRect(tmp)
        }
        val bounds = tmpBounds
        boundsF.roundOut(bounds)
        bounds.set(
            floor(bounds.left + insetX).toInt(),
            floor(bounds.top + insetY).toInt(),
            ceil(bounds.right - insetX).toInt(),
            ceil(bounds.bottom - insetY).toInt()
        )
        outBounds.set(bounds)
    }

    private fun getCellBounds(
        address: Grid.Address,
        outBounds: RectF
    ) = outBounds.run {
        set(0F, 0F, cellSize.x, cellSize.y)
        offsetTo(address.column * stepSize.x, address.row * stepSize.y)
    }

    fun drawGrid(canvas: Canvas) =
        canvas.withMatrix(drawMatrix) {
            drawGridInternal(
                canvas,
                grid,
                hexagon,
                paint,
                layoutSpecs.strokeWidth,
                strokeColor,
                fillColor,
                selectColor,
                showRowIndices,
                showColumnIndices,
                tmpBoundsF
            )
        }

    private fun drawGridInternal(
        canvas: Canvas,
        grid: Grid,
        hexagon: Hexagon,
        paint: Paint,
        strokeWidth: Float,
        strokeColor: Int,
        fillColor: Int,
        selectColor: Int,
        showRowIndices: Boolean,
        showColumnIndices: Boolean,
        tmpBounds: RectF
    ) {
        grid.forEach { address, state ->
            getCellBounds(address, tmpBounds)
            if (state.isVisible) {
                hexagon.draw(
                    canvas,
                    tmpBounds,
                    paint,
                    strokeWidth,
                    strokeColor,
                    if (state.isSelected) selectColor else fillColor
                )
            }
            if (showColumnIndices || showRowIndices) {
                drawIndices(
                    canvas,
                    paint.apply { color = strokeColor },
                    tmpBounds,
                    address,
                    showRowIndices,
                    showColumnIndices
                )
            }
        }
    }

    private fun drawIndices(
        canvas: Canvas,
        paint: Paint,
        bounds: RectF,
        address: Grid.Address,
        showRows: Boolean,
        showColumns: Boolean
    ) {
        val text = with(address) {
            when {
                showRows && showColumns -> "$row,$column"
                showRows -> "$row"
                else -> "$column"
            }
        }
        paint.color = strokeColor
        paint.style = Paint.Style.FILL
        paint.textSize = bounds.height() / 3
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            text,
            bounds.centerX(),
            bounds.centerY() + paint.textSize / 3,
            paint
        )
    }

    fun resolveAddress(x: Float, y: Float): Grid.Address? {
        val point = tmpPoint.also { pt -> pt[0] = x; pt[1] = y }
        touchMatrix.mapPoints(point)
        with(hexagon) {
            // Flip to horizontal if needed, to unify calculations.
            // Also, adding a step in each direction because of edge lines, to
            // keep numbers positive and the slope calculation short and simple.
            // The resulting indices are each decremented one at the end.
            val pointMajor =
                3 * halfSide + if (isHorizontal) point[0] else point[1]
            val pointMinor =
                halfMinor + if (isHorizontal) point[1] else point[0]

            // Determine major index.
            var indexMajor = floor(pointMajor / (3 * halfSide)).toInt()
            // Figure major coordinate within left 3/4-width of touched cell.
            val coordMajor = pointMajor % (3 * halfSide)

            // Account for alternate line insets.
            val isInset = grid.isLineInset(indexMajor)
            val insetMinor = pointMinor + if (isInset) halfMinor else 0F
            // Determine minor index.
            val startIndex = if (isInset) 1 else 0
            var indexMinor = 2 * floor(insetMinor / minor).toInt() - startIndex
            // Figure minor coordinate within touched cell.
            val coordMinor = insetMinor % minor

            // If within 1st quarter-width, adjust indices if touch is outside.
            if (coordMajor < halfSide) {
                val onTop = coordMinor < halfMinor
                val slope = halfMinor / halfSide * if (onTop) -1 else 1
                val above = coordMinor - slope * coordMajor - halfMinor < 0F
                if (onTop && above) {
                    // Touch is above the top-left edge.
                    indexMajor -= 1
                    indexMinor -= 1
                } else if (!onTop && !above) {
                    // Touch is below the bottom-left edge.
                    indexMajor -= 1
                    indexMinor += 1
                }
            }

            // Account for the "padding" added to keep things non-negative.
            indexMajor--
            indexMinor--

            // Flip back if necessary.
            val row = if (isHorizontal) indexMinor else indexMajor
            val column = if (isHorizontal) indexMajor else indexMinor
            return grid.findAddress(row, column)
        }
    }

    private val tmpBounds = Rect()

    private val tmpBoundsF = RectF()

    private val tmpMatrix = Matrix()

    private val tmpPoint = FloatArray(2)
}

private inline val LayoutSpecs.isHexHorizontal
    get() = hexOrientation.isHorizontal

private val DefaultLayoutSpecs = LayoutSpecs(
    FitMode.FitColumns,
    CrossMode.AlignCenter,
    HexOrientation.Horizontal,
    0F
)

private class MeasureData {
    var isFitColumns: Boolean = false
    var isWrapContent: Boolean = false
    var availableWidth: Int = 0
    var availableHeight: Int = 0
    var insetLeft: Int = 0
    var insetTop: Int = 0
    var insetRight: Int = 0
    var insetBottom: Int = 0
}