package com.gonodono.hexgrid.compose

import androidx.compose.runtime.Immutable
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.MutableGrid

/**
 * The immutable version of [Grid].
 *
 * This class simply wraps a [MutableGrid] and hides its mutators.
 */
@Immutable
class ImmutableGrid internal constructor(
    private val grid: MutableGrid
) : Grid by grid {

    constructor(
        rowCount: Int,
        columnCount: Int,
        insetEvenLines: Boolean = false,
        enableEdgeLines: Boolean = false,
        initial: Map<Grid.Address, Grid.State>? = null
    ) : this(
        MutableGrid(
            rowCount,
            columnCount,
            insetEvenLines,
            enableEdgeLines,
            initial
        )
    )

    override fun copy(changes: Map<Grid.Address, Grid.State>): ImmutableGrid {
        val copy = grid.copy(changes)
        return if (copy !== grid) ImmutableGrid(copy) else this
    }

    override fun copy(
        address: Grid.Address,
        change: Grid.State
    ): ImmutableGrid {
        val copy = grid.copy(address, change)
        return if (copy !== grid) ImmutableGrid(copy) else this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ImmutableGrid
        return grid == other.grid
    }

    override fun hashCode(): Int {
        return grid.hashCode()
    }

    override fun toString(): String =
        "ImmutableGrid($rowCount,$columnCount,$insetEvenLines,$enableEdgeLines)"
}

/**
 * Copies this [MutableGrid] and wraps the new instance in an [ImmutableGrid].
 */
fun MutableGrid.toImmutable(): ImmutableGrid = ImmutableGrid(this.copy())

/**
 * Wraps this [MutableGrid] in an [ImmutableGrid].
 *
 * Any changes made to the MutableGrid _will_ reflect in the ImmutableGrid, so
 * don't do that. This functionality is useful when one is certain that the Grid
 * will never be modified afterward, as it allows the internal state generation
 * and verification to be skipped.
 */
fun MutableGrid.asImmutable(): ImmutableGrid = ImmutableGrid(this)