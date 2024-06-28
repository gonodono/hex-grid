package com.gonodono.hexgrid.demo.examples.grid

import androidx.core.util.component1
import androidx.core.util.component2
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.hexEdgeForLineCount
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.hexSizeForHexEdge
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.lineCountForHexEdge
import kotlin.math.roundToInt

internal data class ExampleData(
    val rowCount: Int,
    val columnCount: Int,
    val hexWidth: Float,
    val hexHeight: Float,
    val radius: Int,
)

internal fun calculateExampleData(
    fitMode: FitMode,
    isHorizontal: Boolean,
    availableWidth: Int,
    availableHeight: Int,
    marginDp: Int,
    density: Float
): ExampleData {
    val margin = marginDp * density

    val rowCount: Int
    val columnCount: Int
    val hexEdge: Float
    when (fitMode) {
        // This Example mode fits "natural" rows and columns – i.e., lines with
        // contiguous cells – so the demo grid might look like another line
        // of hexagons can be squeezed in on one side, but that directions'
        // lines are the wonky ones, and another full natural line won't fit.
        is FitMode.FitHex -> {
            // This can be figured from width/height, if you need
            // to use those instead. This is just for the example.
            hexEdge = fitMode.side * density
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

    return ExampleData(rowCount, columnCount, hexWidth, hexHeight, radius)
}

private const val ROW_COUNT = 4

private const val COLUMN_COUNT = 5