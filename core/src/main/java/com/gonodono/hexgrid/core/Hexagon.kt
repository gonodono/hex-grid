package com.gonodono.hexgrid.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.sqrt

class Hexagon(var isHorizontal: Boolean) {

    // Base measures for vertex coordinates
    var side = 0F
        private set

    var halfSide = 0F
        private set

    var minor = 0F
        private set

    var halfMinor = 0F
        private set

    // Determined by orientation
    private val vertices = List(6) { PointF() }

    private val shapePath = Path()

    private val tmpPath = Path()

    fun setSideLength(side: Float) {
        if (this.side == side) return
        this.side = side

        minor = sqrt(3F) * side
        halfSide = side / 2F
        halfMinor = minor / 2F

        // Unordered coordinates, symmetric across y=x
        val coordinatePairs = listOf(
            Pair(0F, halfMinor),
            Pair(halfSide, 0F),
            Pair(3F * halfSide, 0F),
            Pair(2F * side, halfMinor),
            Pair(3F * halfSide, minor),
            Pair(halfSide, minor)
        )
        val v = vertices
        v.forEachIndexed { index, point ->
            val pair = coordinatePairs[index]
            if (isHorizontal) {
                point.set(pair.first, pair.second)
            } else {
                point.set(pair.second, pair.first)
            }
        }
        shapePath.apply {
            rewind()
            moveTo(v[0].x, v[0].y)
            lineTo(v[1].x, v[1].y)
            lineTo(v[2].x, v[2].y)
            lineTo(v[3].x, v[3].y)
            lineTo(v[4].x, v[4].y)
            lineTo(v[5].x, v[5].y)
            lineTo(v[0].x, v[0].y)
            close()
        }
    }

    fun draw(
        canvas: Canvas,
        bounds: RectF,
        paint: Paint,
        strokeWidth: Float,
        strokeColor: Int,
        fillColor: Int
    ) {
        val drawPath = tmpPath.also { path ->
            shapePath.offset(bounds.left, bounds.top, path)
        }
        paint.style = Paint.Style.FILL
        paint.color = fillColor
        canvas.drawPath(drawPath, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor
        canvas.drawPath(drawPath, paint)
    }

    fun getPath(outPath: Path, matrix: Matrix) {
        shapePath.transform(matrix, outPath)
    }

    // I'm assuming that this is cheaper than creating a new
    // Path instance every time, but I've not confirmed that.
    fun getPathBuilder(matrix: Matrix): Path.() -> Unit {
        val v = FloatArray(12)
        vertices.forEachIndexed { index, pointF ->
            v[2 * index] = pointF.x
            v[2 * index + 1] = pointF.y
        }
        matrix.mapPoints(v)
        return {
            moveTo(v[0], v[1])
            lineTo(v[2], v[3])
            lineTo(v[4], v[5])
            lineTo(v[6], v[7])
            lineTo(v[8], v[9])
            lineTo(v[10], v[11])
            lineTo(v[0], v[1])
            close()
        }
    }
}