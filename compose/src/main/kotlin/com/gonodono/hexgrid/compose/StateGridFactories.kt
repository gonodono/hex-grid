package com.gonodono.hexgrid.compose

import androidx.compose.runtime.snapshots.StateFactoryMarker
import com.gonodono.hexgrid.compose.data.SnapshotStateGrid
import com.gonodono.hexgrid.data.Grid

/**
 * Creates an instance of [Grid] that integrates with Compose's observe and
 * snapshot mechanisms.
 */
@StateFactoryMarker
fun mutableStateGridOf(
    size: Grid.Size,
    insetEvenLines: Boolean = false,
    enableEdgeLines: Boolean = false,
    init: ((Grid.Address) -> Grid.State?)? = null
) = SnapshotStateGrid(size, insetEvenLines, enableEdgeLines, init)

/**
 * Creates an instance of [Grid] that integrates with Compose's observe and
 * snapshot mechanisms.
 */
@StateFactoryMarker
fun mutableStateGridOf(
    rowCount: Int,
    columnCount: Int,
    insetEvenLines: Boolean = false,
    enableEdgeLines: Boolean = false,
    init: ((Grid.Address) -> Grid.State?)? = null
) = SnapshotStateGrid(
    rowCount,
    columnCount,
    insetEvenLines,
    enableEdgeLines,
    init
)

/**
 * Creates an instance of [Grid] that integrates with Compose's observe and
 * snapshot mechanisms.
 */
@StateFactoryMarker
fun mutableStateGridOf(
    size: Grid.Size,
    insetEvenLines: Boolean = false,
    enableEdgeLines: Boolean = false,
    initial: Map<Grid.Address, Grid.State>
) = SnapshotStateGrid(size, insetEvenLines, enableEdgeLines, initial)

/**
 * Creates an instance of [Grid] that integrates with Compose's observe and
 * snapshot mechanisms.
 */
@StateFactoryMarker
fun mutableStateGridOf(
    rowCount: Int,
    columnCount: Int,
    insetEvenLines: Boolean = false,
    enableEdgeLines: Boolean = false,
    initial: Map<Grid.Address, Grid.State>
) = SnapshotStateGrid(
    rowCount,
    columnCount,
    insetEvenLines,
    enableEdgeLines,
    initial
)

/**
 * Creates an instance of [Grid] that integrates with Compose's observe and
 * snapshot mechanisms.
 */
@StateFactoryMarker
fun mutableStateGridOf(grid: Grid) = SnapshotStateGrid(grid)