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
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.HexOrientation
import com.gonodono.hexgrid.data.Lines
import com.gonodono.hexgrid.data.emptyGrid
import com.gonodono.hexgrid.data.isHorizontal
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

class GridUi {

    private val hexagon = Hexagon(DefaultLayoutSpecs.isHexHorizontal)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val measureData = MeasureData()

    private val drawMatrix = Matrix()

    private val touchMatrix = Matrix()

    private val actualSize = PointF()

    private val stepSize = PointF()

    private val cellSize = PointF()

    private var textOffsetY = 0F

    var grid: Grid = emptyGrid()
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
            paint.strokeWidth = value.strokeWidth
            calculate()
        }

    var strokeColor: Int = Color.BLACK

    var fillColor: Int = Color.TRANSPARENT

    var selectColor: Int = Color.GRAY

    var indexColor: Int = Color.BLACK

    var cellIndices: Lines = Lines.None

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
        val lineThicknessMajor = 2 / Sqrt3F * lineThicknessMinor
        val fitLineThickness: Float
        val crossLineThickness: Float
        if (isFitMajor) {
            fitLineThickness = lineThicknessMajor
            crossLineThickness = lineThicknessMinor
        } else {
            fitLineThickness = lineThicknessMinor
            crossLineThickness = lineThicknessMajor
        }

        val insetsHorizontal = insetLeft + insetRight
        val insetsVertical = insetTop + insetBottom
        val maxWidth = availableWidth - insetsHorizontal
        val maxHeight = availableHeight - insetsVertical

        val fitCount: Int
        val crossCount: Int
        val fitDimension: Float
        if (isFitColumns) {
            fitCount = grid.size.columnCount
            crossCount = grid.size.rowCount
            fitDimension = maxWidth - fitLineThickness
        } else {
            fitCount = grid.size.rowCount
            crossCount = grid.size.columnCount
            fitDimension = maxHeight - fitLineThickness
        }

        val hexagonSide = when {
            isFitMajor -> fitDimension * 2 / (3 * fitCount + 1)
            else -> fitDimension * 2 / Sqrt3F / (fitCount + 1)
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
            isFitMajor -> hexagonSide * Sqrt3F / 2 * (crossCount + 1)
            else -> hexagonSide * (3 * crossCount + 1) / 2
        }

        val strokeInsetLeft: Float
        val strokeInsetTop: Float
        val size = actualSize
        if (isFitColumns) {
            strokeInsetLeft = fitLineThickness / 2
            strokeInsetTop = crossLineThickness / 2
            size.set(
                fitDimension + fitLineThickness,
                crossDimension + crossLineThickness + insetsVertical
            )
        } else {
            strokeInsetLeft = crossLineThickness / 2
            strokeInsetTop = fitLineThickness / 2
            size.set(
                crossDimension + crossLineThickness + insetsHorizontal,
                fitDimension + fitLineThickness
            )
        }

        with(drawMatrix) {
            reset()
            var offsetX = insetLeft + strokeInsetLeft
            var offsetY = insetTop + strokeInsetTop
            if (!isWrapContent) when (layoutSpecs.crossMode) {
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
                CrossMode.AlignStart -> {}  // no-op
            }
            preTranslate(offsetX, offsetY)
            invert(touchMatrix)
        }

        val textSize = cellSize.y / 3
        paint.textSize = textSize
        textOffsetY = textSize / 3  // Cheap approximation
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
        val source = tmpBoundsF
        source.set(0F, 0F, cellSize.x, cellSize.y)
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
        if (layoutSpecs.isHexHorizontal) {
            insetX = 2 / Sqrt3F * inset
            insetY = inset
        } else {
            insetX = inset
            insetY = 2 / Sqrt3F * inset
        }

        val boundsF = tmpBoundsF
        boundsF.setToCellBounds(address)
        drawMatrix.mapRect(boundsF)

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

    private fun RectF.setToCellBounds(address: Grid.Address) {
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
                strokeColor,
                fillColor,
                selectColor,
                indexColor,
                cellIndices,
                textOffsetY,
                tmpBoundsF
            )
        }

    private fun drawGridInternal(
        canvas: Canvas,
        grid: Grid,
        hexagon: Hexagon,
        paint: Paint,
        strokeColor: Int,
        fillColor: Int,
        selectColor: Int,
        indexColor: Int,
        cellIndices: Lines,
        textOffsetY: Float,
        tmpBoundsF: RectF
    ) {
        grid.forEach { address, state ->
            if (state.isVisible) {
                tmpBoundsF.setToCellBounds(address)
                val fill = if (state.isSelected) selectColor else fillColor
                hexagon.draw(canvas, tmpBoundsF, paint, strokeColor, fill)
            }
        }

        val formatIndices: (Grid.Address) -> String = when (cellIndices) {
            Lines.None -> return
            Lines.Both -> { a -> "${a.row},${a.column}" }
            Lines.Rows -> { a -> "${a.row}" }
            Lines.Columns -> { a -> "${a.column}" }
        }
        paint.style = Paint.Style.FILL
        paint.color = indexColor

        grid.forEach { address, _ ->
            tmpBoundsF.setToCellBounds(address)
            canvas.drawText(
                formatIndices(address),
                tmpBoundsF.centerX(),
                tmpBoundsF.centerY() + textOffsetY,
                paint
            )
        }
    }

    fun resolveAddress(x: Float, y: Float): Grid.Address? {
        val point = tmpPoint
        point[0] = x; point[1] = y
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

private val DefaultLayoutSpecs = LayoutSpecs(
    FitMode.FitColumns,
    CrossMode.AlignCenter,
    HexOrientation.Horizontal,
    0F
)

private inline val LayoutSpecs.isHexHorizontal
    get() = hexOrientation.isHorizontal

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

internal val Sqrt3F = sqrt(3F)