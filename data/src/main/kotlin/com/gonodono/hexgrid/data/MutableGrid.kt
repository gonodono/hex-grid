package com.gonodono.hexgrid.data

/**
 * The regular mutable version of the library's [Grid] structure.
 *
 * [MutableGrid] allows its [Grid.State]s to be changed out, though the grid's
 * basic shape is actually immutable. That shape is defined by its row and
 * column counts, the lines which are inset, and whether edge lines are enabled.
 */
open class MutableGrid(
    final override val size: Grid.Size,
    final override val insetEvenLines: Boolean = false,
    final override val enableEdgeLines: Boolean = false,
    init: (Grid.Address) -> Grid.State? = { null }
) : Grid {

    /**
     * Convenience for the [init] function constructor that takes line count
     * [Int]s rather than a [Grid.Size].
     */
    constructor(
        rowCount: Int,
        columnCount: Int,
        insetEvenLines: Boolean = false,
        enableEdgeLines: Boolean = false,
        init: (Grid.Address) -> Grid.State? = { null }
    ) : this(
        Grid.Size(rowCount, columnCount),
        insetEvenLines,
        enableEdgeLines,
        init
    )

    /**
     * Constructor that takes an [initial] state Map.
     */
    constructor(
        size: Grid.Size,
        insetEvenLines: Boolean = false,
        enableEdgeLines: Boolean = false,
        initial: Map<Grid.Address, Grid.State>
    ) : this(
        size,
        insetEvenLines,
        enableEdgeLines,
        initial::get
    )

    /**
     * Convenience for the [initial] map constructor that takes line count
     * [Int]s rather than a [Grid.Size].
     */
    constructor(
        rowCount: Int,
        columnCount: Int,
        insetEvenLines: Boolean = false,
        enableEdgeLines: Boolean = false,
        initial: Map<Grid.Address, Grid.State>
    ) : this(
        Grid.Size(rowCount, columnCount),
        insetEvenLines,
        enableEdgeLines,
        initial::get
    )

    /**
     * Copy/convert constructor.
     */
    constructor(grid: Grid) : this(
        grid.size,
        grid.insetEvenLines,
        grid.enableEdgeLines,
        grid::get
    )

    final override val totalSize: Grid.Size = when {
        enableEdgeLines -> Grid.Size(size.rowCount + 2, size.columnCount + 2)
        else -> Grid.Size(size.rowCount, size.columnCount)
    }

    final override val cellCount: Int = if (insetEvenLines) {
        totalSize.rowCount.largeHalf * totalSize.columnCount.smallHalf +
                totalSize.rowCount.smallHalf * totalSize.columnCount.largeHalf
    } else {
        totalSize.rowCount.largeHalf * totalSize.columnCount.largeHalf +
                totalSize.rowCount.smallHalf * totalSize.columnCount.smallHalf
    }

    open class MutableCell(
        final override val address: Grid.Address,
        override var state: Grid.State
    ) : Grid.Cell

    protected open fun createMutableCell(
        address: Grid.Address,
        state: Grid.State
    ): MutableCell = MutableCell(address, state)

    protected fun checkAddress(row: Int, column: Int) {
        check(isValidAddress(row, column)) {
            "Invalid Address ($row, $column) for $this"
        }
    }

    private inner class Row(
        private val rowIndex: Int,
        private val startIndex: Int,
        size: Int,
        init: (Grid.Address) -> Grid.State?
    ) : Iterable<MutableCell> {

        private val cells: Array<MutableCell> = Array(size) { column ->
            val address = Grid.Address(rowIndex, 2 * column + startIndex)
            createMutableCell(address, init(address) ?: Grid.State.Default)
        }

        private fun getCell(column: Int) = cells[(column - startIndex) / 2]

        operator fun get(column: Int): Grid.State = getCell(column).state

        operator fun set(column: Int, state: Grid.State) {
            getCell(column).state = state
        }

        fun address(column: Int): Grid.Address = getCell(column).address

        override fun iterator(): Iterator<MutableCell> = cells.iterator()
    }

    private val minimumIndex = if (enableEdgeLines) -1 else 0

    private val rows: Array<Row> = Array(totalSize.rowCount) { index ->
        val rowIndex = minimumIndex + index
        val isInset = isLineInset(rowIndex)
        val startIndex = crossStartIndex(isInset)
        val size = crossCount(isInset, totalSize.columnCount)
        Row(rowIndex, startIndex, size, init)
    }

    private fun getRow(row: Int) = rows[row - minimumIndex]

    final override fun get(address: Grid.Address): Grid.State =
        get(address.row, address.column)

    final override fun get(row: Int, column: Int): Grid.State {
        checkAddress(row, column)
        return getRow(row)[column]
    }

    /**
     * The [Address][Grid.Address]-indexed set operator for [MutableGrid].
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    operator fun set(address: Grid.Address, state: Grid.State) {
        set(address.row, address.column, state)
    }

    /**
     * The [Int]-indexed set operator for [MutableGrid].
     *
     * Invalid addresses will result in Exceptions. See [isValidAddress].
     */
    operator fun set(row: Int, column: Int, state: Grid.State) {
        checkAddress(row, column)
        getRow(row)[column] = state
    }

    final override fun findAddress(row: Int, column: Int): Grid.Address? =
        when {
            isValidAddress(row, column) -> getRow(row).address(column)
            else -> null
        }

    private abstract inner class MutableGridIterator {

        private val major: Iterator<Iterable<MutableCell>> = rows.iterator()

        private var minor: Iterator<MutableCell>? = nextIterator()

        private fun nextIterator(): Iterator<MutableCell>? {
            while (major.hasNext()) {
                val nextIterator = major.next().iterator()
                if (nextIterator.hasNext()) return nextIterator
            }
            return null
        }

        fun hasNext() = minor?.hasNext() == true

        fun nextCell(): MutableCell {
            val iterator = minor ?: error("Iterator is empty.")
            val nextValue = iterator.next()
            if (!iterator.hasNext()) minor = nextIterator()
            return nextValue
        }
    }

    final override val cells: Set<Grid.Cell>
        get() = _cells ?: object : AbstractSet<MutableCell>() {
            override val size: Int get() = this@MutableGrid.cellCount
            override fun iterator(): Iterator<MutableCell> =
                object : MutableGridIterator(), Iterator<MutableCell> {
                    override fun next(): MutableCell = nextCell()
                }
        }.also { _cells = it }

    @kotlin.concurrent.Volatile
    private var _cells: Set<Grid.Cell>? = null

    final override val addresses: Set<Grid.Address>
        get() = _addresses ?: object : AbstractSet<Grid.Address>() {
            override val size: Int get() = this@MutableGrid.cellCount
            override fun iterator(): Iterator<Grid.Address> =
                object : MutableGridIterator(), Iterator<Grid.Address> {
                    override fun next(): Grid.Address = nextCell().address
                }
        }.also { _addresses = it }

    @kotlin.concurrent.Volatile
    private var _addresses: Set<Grid.Address>? = null

    final override val states: Collection<Grid.State>
        get() = _states ?: object : AbstractCollection<Grid.State>() {
            override val size: Int get() = this@MutableGrid.cellCount
            override fun iterator(): Iterator<Grid.State> =
                object : MutableGridIterator(), Iterator<Grid.State> {
                    override fun next(): Grid.State = nextCell().state
                }
        }.also { _states = it }

    @kotlin.concurrent.Volatile
    private var _states: Collection<Grid.State>? = null

    final override fun forEach(action: (Grid.Address, Grid.State) -> Unit) {
        for (row in rows) for (cell in row) action(cell.address, cell.state)
    }

    override fun copy(address: Grid.Address, change: Grid.State): MutableGrid {
        checkAddress(address.row, address.column)
        return when {
            this[address] != change -> {
                MutableGrid(
                    size,
                    insetEvenLines,
                    enableEdgeLines
                ) { initAddress ->
                    when (initAddress) {
                        address -> change
                        else -> this[initAddress]
                    }
                }
            }
            else -> this
        }
    }

    override fun copy(changes: Map<Grid.Address, Grid.State>): MutableGrid {
        var actualChanges: MutableMap<Grid.Address, Grid.State>? = null
        changes.entries.forEach { (address, state) ->
            checkAddress(address.row, address.column)
            if (this[address] != state) {
                val actual = actualChanges
                    ?: mutableMapOf<Grid.Address, Grid.State>()
                        .also { actualChanges = it }
                actual[address] = state
            }
        }
        return when (val actual = actualChanges) {
            null -> this
            else -> {
                MutableGrid(
                    size,
                    insetEvenLines,
                    enableEdgeLines
                ) { initAddress ->
                    actual[initAddress] ?: this[initAddress]
                }
            }
        }
    }

    final override fun isLineInset(index: Int): Boolean =
        (index % 2 == 0) == insetEvenLines

    final override fun isValidAddress(row: Int, column: Int): Boolean =
        isValidLine(row, column, totalSize.rowCount) &&
                isValidLine(column, row, totalSize.columnCount)

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

    private fun crossStartIndex(isInset: Boolean) = when {
        isInset -> if (enableEdgeLines) -1 else 1
        else -> 0
    }

    private fun crossCount(
        isInset: Boolean,
        totalCount: Int
    ) = if (isInset == enableEdgeLines) {
        totalCount.largeHalf
    } else {
        totalCount.smallHalf
    }

    override fun toString(): String =
        "MutableGrid($size,$insetEvenLines,$enableEdgeLines)"
}

private inline val Int.smallHalf: Int
    get() = this / 2

private inline val Int.largeHalf: Int
    get() = if (isEven) smallHalf else smallHalf + 1

private inline val Int.isEven: Boolean
    get() = this % 2 == 0