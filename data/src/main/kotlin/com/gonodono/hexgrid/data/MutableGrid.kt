package com.gonodono.hexgrid.data

/**
 * The mutable version of the library's [Grid] structure.
 *
 * MutableGrid allows its [Grid.State]s to be changed out, though the Grid's
 * basic shape is actually immutable. That shape is defined by its row and
 * column counts, the lines which are inset, and whether edge lines are enabled.
 */
class MutableGrid private constructor(
    override val rowCount: Int,
    override val columnCount: Int,
    override val insetEvenLines: Boolean,
    override val enableEdgeLines: Boolean,
    initial: Map<Grid.Address, Grid.State>?,
    addresses: Addresses?,
    states: States?
) : Grid {

    constructor(
        rowCount: Int,
        columnCount: Int,
        insetEvenLines: Boolean = false,
        enableEdgeLines: Boolean = false,
        initial: Map<Grid.Address, Grid.State>? = null
    ) : this(
        rowCount,
        columnCount,
        insetEvenLines,
        enableEdgeLines,
        initial,
        null,
        null
    )

    internal val totalRowCount = when {
        enableEdgeLines -> rowCount + 2
        else -> rowCount
    }

    internal val totalColumnCount = when {
        enableEdgeLines -> columnCount + 2
        else -> columnCount
    }

    override val size: Int = when {
        insetEvenLines -> totalRowCount.largeHalf * totalColumnCount.smallHalf +
                totalRowCount.smallHalf * totalColumnCount.largeHalf

        else -> totalRowCount.largeHalf * totalColumnCount.largeHalf +
                totalRowCount.smallHalf * totalColumnCount.smallHalf
    }

    private val addresses = addresses ?: Addresses(this)

    private val states = states ?: States(this, initial)

    init {
        check(size == this.addresses.size && size == this.states.size) {
            "Bad internal state: $size,${this.addresses.size},${this.states.size}"
        }
    }

    override fun get(address: Grid.Address): Grid.State {
        checkAddress(address.row, address.column)
        return states[address]
    }

    override fun get(row: Int, column: Int): Grid.State {
        checkAddress(row, column)
        return states[row, column]
    }

    /**
     * The Address-indexed set operator for MutableGrid.
     *
     * This is direct assignment, and it will result in Exceptions for invalid
     * addresses. See [isValidAddress].
     */
    operator fun set(address: Grid.Address, state: Grid.State) {
        checkAddress(address.row, address.column)
        states[address] = state
    }

    /**
     * The Int-indexed set operator for MutableGrid.
     *
     * This is direct assignment, and it will result in Exceptions for invalid
     * addresses. See [isValidAddress].
     */
    operator fun set(row: Int, column: Int, state: Grid.State) {
        checkAddress(row, column)
        states[row, column] = state
    }

    private fun checkAddress(row: Int, column: Int) {
        check(isValidAddress(row, column)) {
            "Invalid Address ($row, $column) for $this"
        }
    }

    override fun isLineInset(index: Int): Boolean =
        insetEvenLines == (index % 2 == 0)

    override fun isValidAddress(row: Int, column: Int): Boolean =
        isValidLine(row, column, totalRowCount) &&
                isValidLine(column, row, totalColumnCount)

    override fun findAddress(row: Int, column: Int): Grid.Address? =
        if (isValidAddress(row, column)) addresses[row][column] else null

    override fun copy(changes: Map<Grid.Address, Grid.State>): MutableGrid {
        var statesCopy: States?
        if (changes.isEmpty()) {
            statesCopy = states.copyOf()
        } else {
            statesCopy = null
            changes.forEach { (address, state) ->
                checkAddress(address.row, address.column)
                if (this[address] != state) {
                    val copy = statesCopy
                        ?: states.copyOf().also { statesCopy = it }
                    copy[address] = state
                }
            }
        }
        return when {
            statesCopy != null -> MutableGrid(
                rowCount,
                columnCount,
                insetEvenLines,
                enableEdgeLines,
                null,
                addresses,
                statesCopy
            )

            else -> this
        }
    }

    override fun copy(
        address: Grid.Address,
        change: Grid.State
    ): MutableGrid {
        checkAddress(address.row, address.column)
        return when {
            this[address] != change -> {
                val statesCopy = states.copyOf()
                statesCopy[address] = change
                MutableGrid(
                    rowCount,
                    columnCount,
                    insetEvenLines,
                    enableEdgeLines,
                    null,
                    addresses,
                    statesCopy
                )
            }

            else -> this
        }
    }

    override fun forEach(action: (Grid.Address, Grid.State) -> Unit) {
        addresses.forEach { address -> action(address, states[address]) }
    }

    private fun isValidLine(
        line: Int,
        cross: Int,
        totalCount: Int
    ): Boolean {
        val isCrossInset = isLineInset(cross)
        val crossStart = crossStartIndex(isCrossInset)
        val offset = line - crossStart
        if (offset % 2 != 0 || offset < 0) return false
        val index = offset / 2
        return index < crossCount(isCrossInset, totalCount)
    }

    internal fun crossStartIndex(isInset: Boolean) = when {
        isInset -> if (enableEdgeLines) -1 else 1
        else -> 0
    }

    internal fun crossCount(
        isInset: Boolean,
        totalCount: Int
    ) = if (isInset == enableEdgeLines) {
        totalCount.largeHalf
    } else {
        totalCount.smallHalf
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MutableGrid
        if (rowCount != other.rowCount) return false
        if (columnCount != other.columnCount) return false
        if (insetEvenLines != other.insetEvenLines) return false
        if (enableEdgeLines != other.enableEdgeLines) return false
        return states.contentEquals(other.states)
    }

    override fun hashCode(): Int {
        var result = rowCount
        result = 31 * result + columnCount
        result = 31 * result + insetEvenLines.hashCode()
        result = 31 * result + enableEdgeLines.hashCode()
        result = 31 * result + states.contentHashCode()
        return result
    }

    override fun toString(): String =
        "MutableGrid($rowCount,$columnCount,$insetEvenLines,$enableEdgeLines)"
}

private class Addresses(grid: MutableGrid) : Iterable<Grid.Address> {

    var size: Int = 0
        private set

    private val minimumIndex = if (grid.enableEdgeLines) -1 else 0

    private val rows = Array(grid.totalRowCount) { r ->
        val row = minimumIndex + r
        val isInset = grid.isLineInset(row)
        val start = grid.crossStartIndex(isInset)
        val size = grid.crossCount(isInset, grid.totalColumnCount)
        this@Addresses.size += size
        Row(start, size) { column -> Grid.Address(row, column) }
    }

    operator fun get(row: Int) = rows[row - minimumIndex]

    override fun iterator(): Iterator<Grid.Address> =
        GridIterator(rows.iterator())

    inner class Row(
        private val start: Int,
        size: Int,
        init: (Int) -> Grid.Address
    ) : Iterable<Grid.Address> {

        private val addresses = Array(size) { c -> init(start + 2 * c) }

        operator fun get(column: Int) = addresses[(column - start) / 2]

        override fun iterator() = addresses.iterator()
    }
}

private class States private constructor(
    private val grid: MutableGrid,
    initial: Map<Grid.Address, Grid.State>?,
    rows: Array<Row>?
) {
    constructor(
        grid: MutableGrid,
        initial: Map<Grid.Address, Grid.State>?
    ) : this(grid, initial, null)

    var size: Int = 0
        private set

    private val minimumIndex = if (grid.enableEdgeLines) -1 else 0

    private val rows: Array<Row> = rows ?: with(grid) {
        val newRows = Array(totalRowCount) { index ->
            val row = minimumIndex + index
            val isInset = isLineInset(row)
            val start = crossStartIndex(isInset)
            val size = crossCount(isInset, totalColumnCount)
            this@States.size += size
            Row(start, size)
        }
        initial?.forEach { (address, state) ->
            if (isValidAddress(address.row, address.column)) {
                newRows[address.row][address.column] = state
            }
        }
        newRows
    }

    operator fun get(address: Grid.Address): Grid.State =
        rows[address.row - minimumIndex][address.column]

    operator fun get(row: Int, column: Int): Grid.State =
        rows[row - minimumIndex][column]

    operator fun set(address: Grid.Address, state: Grid.State) {
        rows[address.row - minimumIndex][address.column] = state
    }

    operator fun set(row: Int, column: Int, state: Grid.State) {
        rows[row - minimumIndex][column] = state
    }

    fun contentEquals(other: States) = rows.contentEquals(other.rows)

    fun contentHashCode() = rows.contentHashCode()

    fun copyOf(): States {
        val rowsCopy = Array(grid.totalRowCount) { rows[it].copyOf() }
        return States(grid, null, rowsCopy).also { it.size = this@States.size }
    }

    private inner class Row(
        val startIndex: Int,
        val size: Int,
        cells: Array<Grid.State>?
    ) {
        constructor(
            startIndex: Int,
            size: Int,
        ) : this(startIndex, size, null)

        private val cells = cells ?: Array(size) { Grid.State.Default }

        operator fun get(index: Int) = cells[adjustedIndex(index)]

        operator fun set(index: Int, state: Grid.State) {
            cells[adjustedIndex(index)] = state
        }

        private fun adjustedIndex(index: Int) = (index - startIndex) / 2

        fun copyOf() = Row(startIndex, size, cells.copyOf())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (other !is Row) return false
            if (size != other.size) return false
            if (startIndex != other.startIndex) return false
            return cells.contentEquals(other.cells)
        }

        override fun hashCode(): Int {
            var result = size
            result = 31 * result + startIndex
            result = 31 * result + cells.contentHashCode()
            return result
        }
    }
}

private inline val Int.smallHalf: Int
    get() = this / 2

private inline val Int.largeHalf: Int
    get() = if (isEven) smallHalf else smallHalf + 1

private inline val Int.isEven: Boolean
    get() = this % 2 == 0