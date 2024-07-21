package com.gonodono.hexgrid.demo.examples.internal

import android.util.SizeF
import kotlin.math.sqrt

// NB: The sides of a hexagon are called edges here, to prevent confusion
// between Side and Size, and between the hexagon sides and the rectangular
// cell sides.

/**
 * Returns the size of the rectangular bounds of a regular hexagon in a grid
 * that has an [available] measure in the constrained dimension, a [margin]
 * between all cells, and a [lineCount] number of cross lines. [isMajor]
 * specifies whether the measure is along the hexagon's major or minor axis.
 *
 * This function defines lines as collinear cells, not necessarily contiguous
 * ones. Please see the README's Library section for details.
 */
internal fun hexSizeForLineCount(
    lineCount: Int,
    isHorizontal: Boolean,
    available: Float,
    margin: Float,
    isMajor: Boolean
): SizeF = hexSizeForHexEdge(
    hexEdgeForLineCount(lineCount, available, margin, isMajor),
    isHorizontal
)

/**
 * Calculates the length of a regular hexagon's edge in a grid that has an
 * [available] measure in the constrained dimension, a [margin] between all
 * cells, and a [lineCount] number of cross lines. [isMajor] specifies whether
 * the measure is along the hexagon's major or minor axis.
 *
 * This function defines lines as collinear cells, not necessarily contiguous
 * ones. Please see the README's Library section for details.
 */
internal fun hexEdgeForLineCount(
    lineCount: Int,
    available: Float,
    margin: Float,
    isMajor: Boolean
): Float {
    val d = available - when {
        isMajor -> (lineCount - 1) * margin
        else -> (lineCount - 1) / 2F * margin
    }
    return when {
        isMajor -> 2 * d / (3 * lineCount + 1)
        else -> 2 / Sqrt3F * d / (lineCount + 1)
    }
}

/**
 * Returns the line count for a grid of regular hexagons that have [hexEdge]
 * long edges, constrained by [available] length, with [margin] between all the
 * cells.
 *
 * This function defines lines as collinear cells, not necessarily contiguous
 * ones. Please see the README's Library section for details.
 */
internal fun lineCountForHexEdge(
    hexEdge: Float,
    available: Float,
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

/**
 * Returns the rectangular size of a hexagon with edge length [hexEdge], and
 * orientation determined by [isHorizontal].
 */
internal fun hexSizeForHexEdge(
    hexEdge: Float,
    isHorizontal: Boolean
): SizeF = when {
    isHorizontal -> SizeF(2 * hexEdge, Sqrt3F * hexEdge)
    else -> SizeF(Sqrt3F * hexEdge, 2 * hexEdge)
}

/**
 * Returns the length of a regular hexagon's major axis if its edge has length
 * [hexEdge].
 *
 * I don't know that you'll ever need this; it's included mainly as info.
 */
@Suppress("unused")
internal fun hexMajorForHexEdge(hexEdge: Float): Float = 2 * hexEdge

/**
 * Returns the length of a regular hexagon's minor axis if its edge has length
 * of [hexEdge].
 *
 * I don't know that you'll ever need this; it's included mainly as info.
 */
@Suppress("unused")
internal fun hexMinorForHexEdge(hexEdge: Float): Float = Sqrt3F * hexEdge

/**
 * For the start-to-end and top-to-bottom build sequence used in the these
 * examples, when beginning a row, this returns the proper angle relative to
 * the start hexagon in the previous row to start the current one.
 *
 * To explain, that sequence constrains the first hexagon to the top-start
 * corner, then fills in that row to the end by linking each new hexagon to
 * the previous one. A new row is started by linking its first hexagon to
 * the first one in the row above, then its neighbors are filled out to the
 * end as before.
 *
 * A "natural" row comprises contiguous cells, rather than collinear ones
 * that some other functions require. These grids are built in this fashion
 * in order to keep the examples relatively simple, but there's no way to
 * simplify certain calculations.
 *
 * [isHorizontal] specifies the hexagon's orientation, [isLtr] corresponds
 * to [LAYOUT_DIRECTION_LTR][android.view.View.LAYOUT_DIRECTION_LTR], and
 * [row] is the index of the current row.
 */
internal fun naturalRowStartAngle(
    isHorizontal: Boolean,
    isLtr: Boolean,
    row: Int
): Float = when {
    isHorizontal -> 180F
    else -> when {
        isLtr -> if (row.isEven) 210F else 150F
        else -> if (row.isEven) 150F else 210F
    }
}

/**
 * For the start-to-end and top-to-bottom build sequence used in the these
 * examples, when building out a row, this returns the proper angle relative
 * to the previous hexagon in the current row to place the current hexagon.
 *
 * To explain, that sequence constrains the first hexagon to the top-start
 * corner, then fills in that row to the end by linking each new hexagon to
 * the previous one. A new row is started by linking its first hexagon to
 * the first one in the row above, then its neighbors are filled out to the
 * end as before.
 *
 * A "natural" row comprises contiguous cells, rather than collinear ones
 * that some other functions require. These grids are built in this fashion
 * in order to keep the examples relatively simple, but there's no way to
 * simplify certain calculations.
 *
 * [isHorizontal] specifies the hexagon's orientation, [isLtr] corresponds
 * to [LAYOUT_DIRECTION_LTR][android.view.View.LAYOUT_DIRECTION_LTR], and
 * [column] is the index of the current column.
 */
internal fun naturalRowTailAngle(
    isHorizontal: Boolean,
    isLtr: Boolean,
    column: Int
): Float = when {
    isHorizontal -> when {
        isLtr -> if (column.isEven) 60F else 120F
        else -> if (column.isEven) 300F else 240F
    }
    else -> if (isLtr) 90F else 270F
}

private inline val Int.isEven: Boolean get() = this % 2 == 0

private val Sqrt3F = sqrt(3F)