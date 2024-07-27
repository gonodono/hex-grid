package com.gonodono.hexgrid.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

internal class Hexagon(var isHorizontal: Boolean) {

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

        val minor = (Sqrt3F * side).also { minor = it }
        val halfMinor = (minor / 2F).also { halfMinor = it }
        val halfSide = (side / 2F).also { halfSide = it }

        val v = vertices
        if (isHorizontal) {
            v[0] = 0F; v[1] = halfMinor
            v[2] = halfSide; v[3] = 0F
            v[4] = 3F * halfSide; v[5] = 0F
            v[6] = 2F * side; v[7] = halfMinor
            v[8] = 3F * halfSide; v[9] = minor
            v[10] = halfSide; v[11] = minor
        } else {
            v[0] = halfMinor; v[1] = 0F
            v[2] = 0F; v[3] = halfSide
            v[4] = 0F; v[5] = 3F * halfSide
            v[6] = halfMinor; v[7] = 2F * side
            v[8] = minor; v[9] = 3F * halfSide
            v[10] = minor; v[11] = halfSide
        }

        path.run { rewind(); getPathBuilder(IDENTITY_MATRIX)() }
    }

    fun draw(
        canvas: Canvas,
        bounds: RectF,
        paint: Paint,
        strokeColor: Int,
        fillColor: Int
    ) {
        val count = canvas.save()
        canvas.translate(bounds.left, bounds.top)

        paint.style = Paint.Style.FILL
        paint.color = fillColor
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.STROKE
        paint.color = strokeColor
        canvas.drawPath(path, paint)

        canvas.restoreToCount(count)
    }

    fun getPath(outPath: Path, matrix: Matrix) {
        path.transform(matrix, outPath)
    }

    fun getPathBuilder(matrix: Matrix): Path.() -> Unit {
        val vf = FloatArray(12)
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
}

private val IDENTITY_MATRIX = Matrix()