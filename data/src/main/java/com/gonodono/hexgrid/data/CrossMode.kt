package com.gonodono.hexgrid.data

/**
 * Possible options for how to lay out the grid in the dimension perpendicular
 * to the one that determines the cell size.
 */
enum class CrossMode {

    /**
     * This value centers the grid along the cross dimension.
     */
    AlignCenter,

    /**
     * This value aligns the grid against the left or top side, depending on the
     * [FitMode].
     */
    AlignStart,

    /**
     * This value aligns the grid against the right or bottom side, depending on
     * the [FitMode].
     */
    AlignEnd,

    /**
     * This value will stretch or shrink the grid along the cross dimension to
     * match its parent.
     */
    ScaleToFit
}