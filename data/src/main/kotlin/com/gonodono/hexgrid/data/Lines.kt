package com.gonodono.hexgrid.data

/**
 * Option set for properties and parameters that apply to rows and/or columns
 * together.
 */
enum class Lines {

    /**
     * Neither rows nor columns.
     */
    None,

    /**
     * Both rows and columns.
     */
    Both,

    /**
     * Rows only.
     */
    Rows,

    /**
     * Columns only.
     */
    Columns
}