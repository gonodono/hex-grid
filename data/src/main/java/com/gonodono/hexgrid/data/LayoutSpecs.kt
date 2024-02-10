package com.gonodono.hexgrid.data


/**
 * Specifications relevant to the cells' layout.
 */
data class LayoutSpecs(
    val fitMode: FitMode,
    val crossMode: CrossMode,
    val hexOrientation: HexOrientation,
    val strokeWidth: Float
)

/**
 * Default layout values.
 */
val DefaultLayoutSpecs = LayoutSpecs(
    FitMode.FitColumns,
    CrossMode.AlignCenter,
    HexOrientation.Horizontal,
    0F // Hairline
)

/**
 * Possible options for determining the hex cell size.
 */
enum class FitMode {

    /**
     * Indicates that the regular cell size should be calculated from the
     * available width and the number of columns.
     */
    FitColumns,

    /**
     * Indicates that the regular cell size should be calculated from the
     * available height and the number of rows.
     */
    FitRows
}

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

/**
 * Possible options for the orientation of the cell hexagons.
 */
enum class HexOrientation {

    /**
     * Indicates that a major axis should be horizontal; "flat top" orientation.
     */
    Horizontal,

    /**
     * Indicates that a major axis should be vertical; "pointy top" orientation.
     */
    Vertical
}

/**
 * Convenience for concisely checking the hex orientation.
 */
inline val HexOrientation.isHorizontal: Boolean
    get() = this == HexOrientation.Horizontal