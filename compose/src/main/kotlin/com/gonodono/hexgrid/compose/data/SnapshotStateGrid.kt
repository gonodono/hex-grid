package com.gonodono.hexgrid.compose.data

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.StateObject
import androidx.compose.runtime.snapshots.StateRecord
import androidx.compose.runtime.snapshots.readable
import androidx.compose.runtime.snapshots.withCurrent
import androidx.compose.runtime.snapshots.writable
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.MutableGrid
import com.gonodono.hexgrid.data.checkAddress

/**
 * An implementation of [Grid] that integrates with Compose's observe and
 * snapshot mechanisms. Instances of this class should be obtained with the
 * [mutableStateGridOf][com.gonodono.hexgrid.compose.mutableStateGridOf] factory
 * functions.
 */
@Stable
class SnapshotStateGrid private constructor(
    override val size: Grid.Size,
    override val insetEvenLines: Boolean,
    override val enableEdgeLines: Boolean,
    init: ((Grid.Address) -> Grid.State?)? = null,
    persistentGrid: PersistentGrid? = null
) : MutableStateGrid, StateObject {

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
        init,
        null
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
        initial::get,
        null
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
        initial::get,
        null
    )

    constructor(grid: Grid) : this(
        grid.size,
        grid.insetEvenLines,
        grid.enableEdgeLines,
        grid::get
    )

    override var firstStateRecord: StateRecord =
        StateGridStateRecord(
            persistentGrid ?: PersistentArrayGrid(
                size,
                insetEvenLines,
                enableEdgeLines,
                init
            )
        )
        private set

    override fun prependStateRecord(value: StateRecord) {
        firstStateRecord = value as StateGridStateRecord
    }

    internal val modification get() = readable.modification

    internal class StateGridStateRecord(
        var grid: PersistentGrid
    ) : StateRecord() {

        var modification = 0

        override fun assign(value: StateRecord) {
            synchronized(sync) {
                grid = (value as StateGridStateRecord).grid
                modification = value.modification
            }
        }

        override fun create(): StateRecord = StateGridStateRecord(grid)
    }

    internal val readable: StateGridStateRecord
        get() = (firstStateRecord as StateGridStateRecord).readable(this)

    private inline fun <R> withCurrent(block: StateGridStateRecord.() -> R): R =
        (firstStateRecord as StateGridStateRecord).withCurrent(block)

    private inline fun <R> writable(block: StateGridStateRecord.() -> R): R =
        (firstStateRecord as StateGridStateRecord).writable(this, block)

    override val totalSize: Grid.Size = readable.grid.totalSize

    override val cellCount: Int = readable.grid.cellCount

    private var _cells: Set<MutableGrid.MutableCell>? = null
    override val cells: Set<MutableGrid.MutableCell>
        get() = _cells ?: SnapshotGridCellSet(this).also { _cells = it }

    private var _addresses: Set<Grid.Address>? = null
    override val addresses: Set<Grid.Address>
        get() = _addresses
            ?: SnapshotGridAddressSet(this).also { _addresses = it }

    private var _states: Collection<Grid.State>? = null
    override val states: Collection<Grid.State>
        get() = _states ?: SnapshotGridStateSet(this).also { _states = it }

    override fun get(address: Grid.Address): Grid.State =
        this[address.row, address.column]

    override fun get(row: Int, column: Int): Grid.State {
        checkAddress(row, column)
        return readable.grid[row, column]
    }

    override fun set(address: Grid.Address, state: Grid.State) {
        set(address.row, address.column, state)
    }

    override fun set(row: Int, column: Int, state: Grid.State) {
        checkAddress(row, column)
        modify { grid -> grid.set(row, column, state) }
    }

    override fun set(states: Map<Grid.Address, Grid.State>) {
        modify { grid -> grid.set(states) }
    }

    private fun modify(block: (PersistentGrid) -> PersistentGrid) {
        while (true) {
            val oldGrid: PersistentGrid
            var currentModification = 0
            synchronized(sync) {
                val current = withCurrent { this }
                currentModification = current.modification
                oldGrid = current.grid
            }
            val newGrid = block(oldGrid)
            if (newGrid == oldGrid) break
            if (writable {
                    synchronized(sync) {
                        if (modification == currentModification) {
                            grid = newGrid
                            modification++
                            true
                        } else false
                    }
                }
            ) break
        }
    }

    override fun isValidAddress(row: Int, column: Int): Boolean =
        readable.grid.isValidAddress(row, column)

    override fun findAddress(row: Int, column: Int): Grid.Address? =
        readable.grid.findAddress(row, column)

    override fun forEach(action: (Grid.Address, Grid.State) -> Unit) =
        readable.grid.forEach(action)

    override fun copy(
        address: Grid.Address,
        change: Grid.State
    ): SnapshotStateGrid {
        val grid = readable.grid
        return when (val copy = grid.copy(address, change)) {
            grid -> this
            else -> SnapshotStateGrid(copy)
        }
    }

    override fun copy(
        changes: Map<Grid.Address, Grid.State>
    ): SnapshotStateGrid {
        val grid = readable.grid
        return when (val copy = grid.copy(changes)) {
            grid -> this
            else -> SnapshotStateGrid(copy)
        }
    }

    override fun toString(): String = readable.grid.toString()

//    private fun checkAddress(row: Int, column: Int) {
//        check(isValidAddress(row, column)) {
//            "Invalid Address ($row, $column) for $this"
//        }
//    }
}

private class SnapshotGridCellSet(
    private val grid: SnapshotStateGrid
) : AbstractSet<MutableGrid.MutableCell>() {

    override val size: Int = grid.cellCount

    override fun iterator(): Iterator<MutableGrid.MutableCell> =
        object : Iterator<MutableGrid.MutableCell>, StateGridIterator(
            grid,
            grid.readable.grid.cells.iterator()
        ) {
            override fun next(): MutableGrid.MutableCell =
                SnapshotCell(nextCell())

            private inner class SnapshotCell(
                current: Grid.Cell?
            ) : MutableGrid.MutableCell {

                override val address: Grid.Address = current!!.address

                override var state: Grid.State = current!!.state
                    set(value) {
                        if (grid.modification != modification) {
                            throw ConcurrentModificationException()
                        }
                        if (field == value) return
                        field = value
                        grid[address] = value
                        modification = grid.modification
                    }

                override fun toString(): String = "$address=$state"
            }
        }
}

private class SnapshotGridAddressSet(
    private val grid: SnapshotStateGrid
) : AbstractSet<Grid.Address>() {

    override val size: Int = grid.cellCount

    override fun iterator(): Iterator<Grid.Address> =
        object : Iterator<Grid.Address>, StateGridIterator(
            grid,
            grid.readable.grid.cells.iterator()
        ) {
            override fun next(): Grid.Address = nextCell().address
        }
}

private class SnapshotGridStateSet(
    private val grid: SnapshotStateGrid
) : AbstractSet<Grid.State>() {

    override val size: Int = grid.cellCount

    override fun iterator(): Iterator<Grid.State> =
        object : Iterator<Grid.State>, StateGridIterator(
            grid,
            grid.readable.grid.cells.iterator()
        ) {
            override fun next(): Grid.State = nextCell().state
        }
}

private val sync = Any()

private abstract class StateGridIterator(
    grid: SnapshotStateGrid,
    private val iterator: Iterator<Grid.Cell>
) {
    protected var modification = grid.modification

    protected var cell: Grid.Cell? = cell()

    private fun cell(): Grid.Cell? = when {
        iterator.hasNext() -> iterator.next()
        else -> null
    }

    fun hasNext(): Boolean = cell != null

    protected fun nextCell(): Grid.Cell {
        val next = cell ?: throw IllegalStateException()
        cell = cell()
        return next
    }
}