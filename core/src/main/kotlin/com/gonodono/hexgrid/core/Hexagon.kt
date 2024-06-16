package com.gonodono.hexgrid.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.withTranslation
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

    private val path = Path()

    // Determined by orientation
    private val vertices = FloatArray(12)

    fun setSideLength(side: Float) {
        if (this.side == side) return
        this.side = side

        val minor = (sqrt(3F) * side).also { minor = it }
        val halfMinor = (minor / 2F).also { halfMinor = it }
        val halfSide = (side / 2F).also { halfSide = it }

        // Unordered coordinate pairs, symmetric across y=x.
        val coordinatePairs = arrayOf(
            floatArrayOf(0F, halfMinor),
            floatArrayOf(halfSide, 0F),
            floatArrayOf(3F * halfSide, 0F),
            floatArrayOf(2F * side, halfMinor),
            floatArrayOf(3F * halfSide, minor),
            floatArrayOf(halfSide, minor)
        )

        val v = vertices
        coordinatePairs.forEachIndexed { index, (c1, c2) ->
            if (isHorizontal) {
                v[2 * index] = c1
                v[2 * index + 1] = c2
            } else {
                v[2 * index] = c2
                v[2 * index + 1] = c1
            }
        }
        path.run { rewind(); getPathBuilder(IDENTITY_MATRIX)() }
    }

    fun draw(
        canvas: Canvas,
        bounds: RectF,
        paint: Paint,
        strokeWidth: Float,
        strokeColor: Int,
        fillColor: Int
    ) {
        canvas.withTranslation(bounds.left, bounds.top) {
            paint.style = Paint.Style.FILL
            paint.color = fillColor
            drawPath(path, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            paint.color = strokeColor
            drawPath(path, paint)
        }
    }

    fun getPath(outPath: Path, matrix: Matrix) {
        path.transform(matrix, outPath)
    }

    fun getPathBuilder(matrix: Matrix): Path.() -> Unit {
        val vf = tmpArray
        matrix.mapPoints(vf, vertices)
        return {
            moveTo(vf[0], vf[1])
            lineTo(vf[2], vf[3])
            lineTo(vf[4], vf[5])
            lineTo(vf[6], vf[7])
            lineTo(vf[8], vf[9])
            lineTo(vf[10], vf[11])
            lineTo(vf[0], vf[1])
        }
    }

    private val tmpArray = FloatArray(12)
}

private val IDENTITY_MATRIX = Matrix()