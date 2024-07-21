package com.gonodono.hexgrid.data

import android.util.SizeF
import kotlin.math.sqrt

internal fun gridSizeForHexSide(
    available: SizeF,
    hexSide: Float,
    margin: Float,
    isHorizontal: Boolean
) = Grid.Size(
    lineCount(available.height, hexSide, margin, !isHorizontal),
    lineCount(available.width, hexSide, margin, isHorizontal)
)

private fun lineCount(
    available: Float,
    hexEdge: Float,
    margin: Float,
    isMajor: Boolean
): Int = when {
    isMajor -> {
        val num = 2 * available + 2 * margin - hexEdge
        val den = 2 * margin + 3 * hexEdge
        (num / den).toInt()
    }
    else -> {
        val num = Sqrt3F * available + Sqrt3F * margin - 3 * hexEdge
        val den = Sqrt3F * margin + 3 * hexEdge
        (num / den).toInt()
    }
}

private val Sqrt3F = sqrt(3F)