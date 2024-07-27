package com.gonodono.hexgrid.data

class ArrayGrid private constructor(
    override val size: Grid.Size,
    override val insetEvenLines: Boolean = false,
    override val enableEdgeLines: Boolean = false,
    init: ((Grid.Address) -> Grid.State?)? = null,
    rows: Array<Row>? = null
) : MutableGrid {

    constructor(
        size: Grid.Size,
        insetEvenLines: Boolean,
        enableEdgeLines: Boolean,
        init: ((Grid.Address) -> Grid.State?)? = null
    ) : this(size, insetEvenLines, enableEdgeLines, init, null)

    constructor(
        rowCount: Int,
        columnCount: Int,
        insetEvenLines: Boolean = false,
        enableEdgeLines: Boolean = false,
        init: ((Grid.Address) -> Grid.State?)? = null
    ) : this(
        Grid.Size(rowCount, columnCount),
        insetEvenLines,
        enableEdgeLines,
        init
    )

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

    constructor(grid: Grid) : this(
        grid.size,
        grid.insetEvenLines,
        grid.enableEdgeLines,
        grid::get
    )

    override val totalSize: Grid.Size = when {
        enableEdgeLines -> Grid.Size(size.rowCount + 2, size.columnCount + 2)
        else -> Grid.Size(size.rowCount, size.columnCount)
    }

    override val cellCount: Int = if (insetEvenLines) {
        totalSize.rowCount.largeHalf * totalSize.columnCount.smallHalf +
                totalSize.rowCount.smallHalf * totalSize.columnCount.largeHalf
    } else {
        totalSize.rowCount.largeHalf * totalSize.columnCount.largeHalf +
                totalSize.rowCount.smallHalf * totalSize.columnCount.smallHalf
    }

    private data class ArrayCell(
        override val address: Grid.Address,
        override var state: Grid.State
    ) : MutableGrid.MutableCell {
        override fun toString(): String = "$address=$state"
    }

    private inner class Row private constructor(
        private val startIndex: Int,
        private val cells: Array<MutableGrid.MutableCell>
    ) : Iterable<MutableGrid.MutableCell> {

        constructor(
            rowIndex: Int,
            startIndex: Int,
            size: Int,
            init: ((Grid.Address) -> Grid.State?)? = null
        ) : this(
            startIndex,
            Array(size) { c ->
                val address = Grid.Address(rowIndex, 2 * c + startIndex)
                val state = init?.invoke(address) ?: Grid.State.Default
                ArrayCell(address, state)
            }
        )

        private fun getCell(column: Int) = cells[(column - startIndex) / 2]

        fun getAddress(column: Int) = getCell(column).address

        fun getState(column: Int) = getCell(column).state

        fun setState(column: Int, state: Grid.State) {
            getCell(column).state = state
        }

        fun forEach(action: (Grid.Address, Grid.State) -> Unit) {
            cells.forEach { cell -> action(cell.address, cell.state) }
        }

        fun copyOf(): Row = Row(startIndex, cells.copyOf())

        override fun iterator() = cells.iterator()
    }

    private val minimumIndex = if (enableEdgeLines) -1 else 0

    private val rows: Array<Row> = rows ?: Array(totalSize.rowCount) { index ->
        val rowIndex = minimumIndex + index
        val isInset = isLineInset(rowIndex)
        val startIndex = crossStartIndex(isInset)
        val size = crossCount(isInset, totalSize.columnCount)
        Row(rowIndex, startIndex, size, init)
    }

    private fun getRowIndex(row: Int) = row - minimumIndex

    private fun getRow(row: Int) = rows[getRowIndex(row)]

    override val cells: Set<MutableGrid.MutableCell>
        get() = _cells ?: object : AbstractSet<MutableGrid.MutableCell>() {
            override val size: Int get() = cellCount
            override fun iterator(): Iterator<MutableGrid.MutableCell> =
                object : GridIterator<MutableGrid.MutableCell>(rows.iterator()),
                    Iterator<MutableGrid.MutableCell> {
                    override fun next(): MutableGrid.MutableCell = nextValue()
                }
        }.also { _cells = it }
    private var _cells: Set<MutableGrid.MutableCell>? = null

    override val addresses: Set<Grid.Address>
        get() = _addresses ?: object : AbstractSet<Grid.Address>() {
            override val size: Int get() = cellCount
            override fun iterator(): Iterator<Grid.Address> =
                object : GridIterator<MutableGrid.MutableCell>(rows.iterator()),
                    Iterator<Grid.Address> {
                    override fun next(): Grid.Address = nextValue().address
                }
        }.also { _addresses = it }
    private var _addresses: Set<Grid.Address>? = null

    override val states: Set<Grid.State>
        get() = _states ?: object : AbstractSet<Grid.State>() {
            override val size: Int get() = cellCount
            override fun iterator(): Iterator<Grid.State> =
                object : GridIterator<MutableGrid.MutableCell>(rows.iterator()),
                    Iterator<Grid.State> {
                    override fun next(): Grid.State = nextValue().state
                }
        }.also { _states = it }
    private var _states: Set<Grid.State>? = null

    override fun get(address: Grid.Address): Grid.State =
        get(address.row, address.column)

    override fun get(row: Int, column: Int): Grid.State {
        checkAddress(row, column)
        return getRow(row).getState(column)
    }

    override fun set(address: Grid.Address, state: Grid.State) {
        set(address.row, address.column, state)
    }

    override fun set(row: Int, column: Int, state: Grid.State) {
        checkAddress(row, column)
        getRow(row).setState(column, state)
    }

    override fun set(states: Map<Grid.Address, Grid.State>) {
        states.forEach { (address, state) ->
            set(address.row, address.column, state)
        }
    }

    override fun isValidAddress(row: Int, column: Int): Boolean =
        isValidLine(row, column, totalSize.rowCount) &&
                isValidLine(column, row, totalSize.columnCount)

    override fun findAddress(row: Int, column: Int): Grid.Address? = when {
        isValidAddress(row, column) -> getRow(row).getAddress(column)
        else -> null
    }

    override fun forEach(action: (Grid.Address, Grid.State) -> Unit) {
        rows.forEach { row -> row.forEach(action) }
    }

    override fun copy(address: Grid.Address, change: Grid.State): ArrayGrid {
        checkAddress(address.row, address.column)
        return when (this[address]) {
            change -> this
            else -> {
                val rowIndex = getRowIndex(address.row)
                val rowCopy = rows[rowIndex].copyOf()
                rowCopy.setState(address.column, change)
                val rowsCopy = Array(rows.size) { r ->
                    if (r == rowIndex) rowCopy else rows[r]
                }
                ArrayGrid(
                    size,
                    insetEvenLines,
                    enableEdgeLines,
                    rows = rowsCopy
                )
            }
        }
    }

    override fun copy(changes: Map<Grid.Address, Grid.State>): ArrayGrid {
        var actualChanges: Array<Row?>? = null
        changes.forEach { (address, state) ->
            checkAddress(address.row, address.column)
            if (this[address] != state) {
                val actual = actualChanges
                    ?: arrayOfNulls<Row>(rows.size).also { actualChanges = it }
                val rowIndex = getRowIndex(address.row)
                val row = actual[rowIndex]
                    ?: rows[rowIndex].copyOf().also { actual[rowIndex] = it }
                row.setState(address.column, state)
            }
        }
        return when (val actual = actualChanges) {
            null -> this
            else -> {
                val rowsCopy = Array(rows.size) { index ->
                    val rowIndex = getRowIndex(index)
                    actual[rowIndex] ?: rows[rowIndex]
                }
                ArrayGrid(
                    size,
                    insetEvenLines,
                    enableEdgeLines,
                    rows = rowsCopy
                )
            }
        }
    }

    override fun toString(): String = when {
        cellCount > 0 -> buildString {
            append('{')
            this@ArrayGrid.forEach { address, state ->
                append(address).append('=').append(state).append(", ")
            }
            delete(length - 2, length)
            append('}')
        }
        else -> "{}"
    }
}

private abstract class GridIterator<T>(
    private val major: Iterator<Iterable<T>>
) {
    private var minor: Iterator<T>? = nextIterator()

    private fun nextIterator(): Iterator<T>? {
        while (major.hasNext()) {
            val nextIterator = major.next().iterator()
            if (nextIterator.hasNext()) return nextIterator
        }
        return null
    }

    fun hasNext(): Boolean = minor?.hasNext() == true

    protected fun nextValue(): T {
        val iterator = minor ?: throw IllegalStateException()
        val next = iterator.next()
        if (!iterator.hasNext()) minor = nextIterator()
        return next
    }
}