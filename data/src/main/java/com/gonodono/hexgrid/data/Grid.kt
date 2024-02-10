package com.gonodono.hexgrid.data


/**
 * The library's specialized state collection that allows indexing by the
 * particular address system used here.
 */
interface Grid {
    /**
     * The number of linear rows in the [Grid].
     *
     * A linear row comprises strictly collinear cells, not necessarily
     * contiguous ones.
     */
    val rowCount: Int

    /**
     * The number of linear columns in the [Grid].
     *
     * A linear column comprises strictly collinear cells, not necessarily
     * contiguous ones.
     */
    val columnCount: Int

    /**
     * Whether the even rows and columns are inset, or the odd ones.
     *
     * A hex grid is essentially a staggered grid, so the setting is the same
     * for both directions. See [isLineInset].
     */
    val insetEvenLines: Boolean

    /**
     * Whether edge lines are enabled.
     *
     * When true, one extra line of cells is added to the grid on all sides.
     * This is meant to be used with grids that fill their components and need
     * active cells covering the "holes" at the edges, though using it with any
     * other mode is not prohibited.
     *
     * The extra lines result in the minimum row/column index being -1
     * instead of 0, and the maximum row/column index being exactly the
     * [rowCount]/[columnCount] instead of 1 less than those.
     */
    val enableEdgeLines: Boolean

    /**
     * The total cell count, named [size] for consistency with other collections.
     */
    val size: Int

    /**
     * The indexed get operator for Grid.
     *
     * This is a direct accessor, and it will result in Exceptions for invalid
     * addresses. See [isValidAddress] and [findAddress].
     */
    operator fun get(address: Address): State

    /**
     * Returns whether the given line – row or column – is inset, per the
     * [insetEvenLines] property.
     *
     * Due to the inherent symmetry of staggered grids, insetting even rows is
     * the same as insetting even columns, hence the one function for both.
     */
    fun isLineInset(index: Int): Boolean

    /**
     *  Returns true if [row] and [column] are valid coordinates for the current
     *  grid specifications.
     */
    fun isValidAddress(row: Int, column: Int): Boolean

    /**
     * Returns the appropriate [Grid.Address] if [row] and [column] are valid
     * coordinates.
     *
     * Returns null otherwise.
     */
    fun findAddress(row: Int, column: Int): Address?

    /**
     * Iterator function for Grid.
     */
    fun forEach(action: (Address, State) -> Unit)

    /**
     * Returns a new, modified instance of this [Grid] if [changes] actually
     * causes any changes.
     *
     * If the list is non-empty and causes no changes at all, the same instance
     * is returned. If the list is empty, an exact copy is returned as a new
     * instance.
     *
     * Invalid Addresses will cause Exceptions. See [isValidAddress] and
     * [findAddress].
     */
    fun copy(changes: Map<Address, State>): Grid

    /**
     * Returns a modified copy of this [Grid] if [change] actually causes a
     * change.
     *
     * If it does not cause a change, the same instance is returned.
     *
     * Invalid Addresses will cause Exceptions. See [isValidAddress] and
     * [findAddress].
     */
    fun copy(address: Address, change: State): Grid

    /**
     * The index structure for Grid.
     */
    data class Address(val row: Int, val column: Int) {

        override fun toString(): String = "Grid.Address($row,$column)"

        companion object {
            /**
             * The [Address] of the origin cell.
             */
            val Zero: Address = Address(0, 0)
        }
    }

    /**
     * The state class for grid cells.
     */
    data class State(
        val isVisible: Boolean = true,
        val isSelected: Boolean = false
    ) {
        override fun toString(): String = "Grid.State(v%s,s%s)".format(
            if (isVisible) "+" else "-",
            if (isSelected) "+" else "-"
        )

        companion object {
            /**
             * The default [State] for all cells.
             */
            val Default: State = State()
        }
    }
}

/**
 * An empty Grid implementation for use as an initial value, a reset value, etc.
 *
 * This would be more appropriate as a named object, but then it would be far
 * too easy to inadvertently import its members elsewhere.
 */
val EmptyGrid: Grid = object : Grid {

    override val rowCount: Int = 0

    override val columnCount: Int = 0

    override val insetEvenLines: Boolean = false

    override val enableEdgeLines: Boolean = false

    override val size: Int = 0

    override fun get(address: Grid.Address): Grid.State {
        error("EmptyGrid is empty")
    }

    override fun isLineInset(index: Int): Boolean = false

    override fun isValidAddress(row: Int, column: Int): Boolean = false

    override fun findAddress(row: Int, column: Int): Grid.Address? = null

    override fun forEach(action: (Grid.Address, Grid.State) -> Unit) {}

    override fun copy(changes: Map<Grid.Address, Grid.State>): Grid = this

    override fun copy(address: Grid.Address, change: Grid.State): Grid = this

    override fun toString(): String = "EmptyGrid"
}