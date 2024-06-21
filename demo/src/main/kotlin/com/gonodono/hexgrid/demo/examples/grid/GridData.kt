package com.gonodono.hexgrid.demo.examples.grid

import androidx.core.util.component1
import androidx.core.util.component2
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.hexEdgeForLineCount
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.hexSizeForHexEdge
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.lineCountForHexEdge
import kotlin.math.roundToInt

internal data class GridData(
    val rowCount: Int,
    val columnCount: Int,
    val hexWidth: Float,
    val hexHeight: Float,
    val radius: Int,
)

internal enum class FitMode { FitColumns, FitRows, FitHex }

internal fun calculateGridData(
    fitMode: FitMode,
    isHorizontal: Boolean,
    availableWidth: Int,
    availableHeight: Int,
    marginDp: Int,
    density: Float
): GridData {
    val margin = marginDp * density

    val rowCount: Int
    val columnCount: Int
    val hexEdge: Float
    when (fitMode) {
        FitMode.FitHex -> {
            // This can be figured from width/height, if you need
            // to use those instead. This is just for the example.
            hexEdge = HEX_EDGE_DP * density
            columnCount = lineCountForHexEdge(
                hexEdge = hexEdge,
                available = availableWidth.toFloat(),
                margin = margin,
                isMajor = isHorizontal
            )
            rowCount = lineCountForHexEdge(
                hexEdge = hexEdge,
                available = availableHeight.toFloat(),
                margin = margin,
                isMajor = !isHorizontal
            )
        }

        FitMode.FitColumns -> {
            rowCount = ROW_COUNT
            columnCount = COLUMN_COUNT
            hexEdge = hexEdgeForLineCount(
                lineCount = if (isHorizontal) columnCount else 2 * columnCount,
                available = availableWidth.toFloat(),
                margin = margin,
                isMajor = isHorizontal
            )
        }

        else -> {
            rowCount = ROW_COUNT
            columnCount = COLUMN_COUNT
            hexEdge = hexEdgeForLineCount(
                lineCount = if (isHorizontal) 2 * rowCount else rowCount,
                available = availableHeight.toFloat(),
                margin = margin,
                isMajor = !isHorizontal
            )
        }
    }

    val (hexWidth, hexHeight) = hexSizeForHexEdge(hexEdge, isHorizontal)
    val shortSide = if (isHorizontal) hexHeight else hexWidth
    val radius = (shortSide + margin).roundToInt()

    return GridData(rowCount, columnCount, hexWidth, hexHeight, radius)
}

private const val ROW_COUNT = 4

private const val COLUMN_COUNT = 5

private const val HEX_EDGE_DP = 30