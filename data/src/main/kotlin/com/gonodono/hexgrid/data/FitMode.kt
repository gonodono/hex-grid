package com.gonodono.hexgrid.data

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