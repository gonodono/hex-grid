package com.gonodono.hexgrid.demo.examples.grid

/**
 * The Examples' options for fitting the grid to the available space.
 */
internal sealed class FitMode {

    /**
     * Calculate the hexagon size from the available width and the number of
     * columns.
     */
    data object FitColumns : FitMode()

    /**
     * Calculate the hexagon size from the available height and the number of
     * rows.
     */
    data object FitRows : FitMode()

    /**
     * Calculate the number of rows and columns from the size of a regular
     * hexagon with [side] length.
     */
    data class FitHex(val side: Float) : FitMode()
}