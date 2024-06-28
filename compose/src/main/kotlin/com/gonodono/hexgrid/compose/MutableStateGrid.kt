package com.gonodono.hexgrid.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.MutableGrid

/**
 * The Compose version of [MutableGrid], [MutableStateGrid] integrates Compose's
 * [State][androidx.compose.runtime.State] triggers.
 */
@Stable
class MutableStateGrid(
    rowCount: Int,
    columnCount: Int,
    insetEvenLines: Boolean = false,
    enableEdgeLines: Boolean = false,
    init: (Grid.Address) -> Grid.State? = { null }
) : MutableGrid(rowCount, columnCount, insetEvenLines, enableEdgeLines, init) {

    /**
     * Map init constructor.
     */
    constructor(
        rowCount: Int,
        columnCount: Int,
        insetEvenLines: Boolean = false,
        enableEdgeLines: Boolean = false,
        initial: Map<Grid.Address, Grid.State>
    ) : this(
        rowCount,
        columnCount,
        insetEvenLines,
        enableEdgeLines,
        initial::get
    )

    /**
     * Copy/convert constructor.
     */
    constructor(grid: Grid) : this(
        grid.rowCount,
        grid.columnCount,
        grid.insetEvenLines,
        grid.enableEdgeLines,
        grid::get
    )

    private class MutableStateCell(
        address: Grid.Address,
        state: Grid.State
    ) : MutableCell(address, state) {
        override var state: Grid.State by mutableStateOf(state)
    }

    override fun createMutableCell(
        address: Grid.Address,
        state: Grid.State
    ): MutableCell = MutableStateCell(address, state)

    override fun copy(
        address: Grid.Address,
        change: Grid.State
    ): MutableStateGrid {
        checkAddress(address.row, address.column)
        return MutableStateGrid(
            rowCount,
            columnCount,
            insetEvenLines,
            enableEdgeLines
        ) { initAddress ->
            when (initAddress) {
                address -> change
                else -> this[initAddress]
            }
        }
    }

    override fun copy(changes: Map<Grid.Address, Grid.State>): MutableStateGrid {
        changes.keys.forEach { address ->
            checkAddress(address.row, address.column)
        }
        return MutableStateGrid(
            rowCount,
            columnCount,
            insetEvenLines,
            enableEdgeLines
        ) { initAddress ->
            changes[initAddress] ?: this[initAddress]
        }
    }

    override fun toString(): String =
        "MutableStateGrid($rowCount,$columnCount,$insetEvenLines,$enableEdgeLines)"
}

/**
 * Creates a congruent [MutableStateGrid] and copies this [MutableGrid]'s
 * [Grid.State]s to it.
 */
fun MutableGrid.toMutableStateGrid(): MutableStateGrid = MutableStateGrid(this)