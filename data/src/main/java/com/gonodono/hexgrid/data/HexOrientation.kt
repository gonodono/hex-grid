package com.gonodono.hexgrid.data


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