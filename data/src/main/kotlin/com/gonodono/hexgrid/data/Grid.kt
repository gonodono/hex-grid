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
     * If [enableEdgeLines] is `false`, this will be equal to [rowCount].
     *
     * If [enableEdgeLines] is `true`, this will be equal to [rowCount] + 2.
     */
    val totalRowCount: Int

    /**
     * If [enableEdgeLines] is `false`, this will be equal to [columnCount].
     *
     * If [enableEdgeLines] is `true`, this will be equal to [columnCount] + 2.
     */
    val totalColumnCount: Int

    /**
     * The total cell count, named [size] for consistency with other collections.
     */
    val size: Int

    /**
     * A [Set] view of the [Grid]'s mappings.
     *
     * Analogous to [Map]'s [entries][Map.entries].
     */
    val cells: Set<Cell>

    /**
     * A [Set] view of the [Grid]'s [Address]es.
     *
     * Analogous to [Map]'s [keys][Map.keys].
     */
    val addresses: Set<Address>

    /**
     * A [Collection] view of the [Grid]'s [State]s.
     *
     * Analogous to [Map]'s [values][Map.values].
     */
    val states: Collection<State>

    /**
     * Provides faster iteration than the standard views by skipping [Iterator]s.
     */
    fun fastForEach(action: (Address, State) -> Unit)

    /**
     * The [Address]-indexed get operator for [Grid].
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    operator fun get(address: Address): State

    /**
     * The Int-indexed get operator for [Grid].
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    operator fun get(row: Int, column: Int): State

    /**
     * Returns whether the given line – row or column – is inset, per the
     * [insetEvenLines] property.
     *
     * Due to the inherent symmetry of staggered grids, insetting even rows is
     * the same as insetting even columns, hence the one function for both.
     */
    fun isLineInset(index: Int): Boolean

    /**
     *  Returns true if [row] and [column] are valid indices for the current
     *  grid specifications.
     */
    fun isValidAddress(row: Int, column: Int): Boolean

    /**
     * Returns the appropriate [Grid.Address] if [row] and [column] are valid
     * indices; i.e., if `isValidAddress(row, column)` would return `true`.
     *
     * Returns null otherwise.
     */
    fun findAddress(row: Int, column: Int): Address?

    /**
     * Returns a new, modified instance of this [Grid] if [changes] actually
     * causes any changes.
     *
     * If the map is non-empty and causes no changes at all, the same Grid
     * instance is returned. If the map is empty, an exact copy is returned as a
     * new instance.
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    fun copy(changes: Map<Address, State> = emptyMap()): Grid

    /**
     * Returns a modified copy of this [Grid] if [change] actually causes a
     * change.
     *
     * If it does not cause a change, the same instance is returned.
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    fun copy(address: Address, change: State): Grid

    /**
     * The index structure for [Grid].
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
     * The state class for [Grid] cells.
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

    interface Cell {
        val address: Address
        val state: State
        operator fun component1() = address
        operator fun component2() = state
    }
}

/**
 * An empty [Grid] instance for use as an initial value, a reset value, etc.
 */
val EmptyGrid: Grid = object : Grid {

    override val rowCount: Int = 0

    override val columnCount: Int = 0

    override val insetEvenLines: Boolean = false

    override val enableEdgeLines: Boolean = false

    override val totalRowCount: Int = 0

    override val totalColumnCount: Int = 0

    override val size: Int = 0

    override val cells: Set<Grid.Cell> = emptySet()

    override val addresses: Set<Grid.Address> = emptySet()

    override val states: Collection<Grid.State> = emptyList()

    override fun get(address: Grid.Address): Grid.State {
        throw NoSuchElementException()
    }

    override fun get(row: Int, column: Int): Grid.State {
        throw NoSuchElementException()
    }

    override fun findAddress(row: Int, column: Int): Grid.Address? = null

    override fun copy(changes: Map<Grid.Address, Grid.State>): Grid = this

    override fun copy(address: Grid.Address, change: Grid.State): Grid = this

    override fun fastForEach(action: (Grid.Address, Grid.State) -> Unit) {}

    override fun isLineInset(index: Int): Boolean = false

    override fun isValidAddress(row: Int, column: Int): Boolean = false

    override fun toString(): String = "EmptyGrid"
}