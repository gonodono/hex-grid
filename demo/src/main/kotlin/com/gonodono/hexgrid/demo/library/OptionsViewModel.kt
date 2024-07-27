package com.gonodono.hexgrid.demo.library

import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.gonodono.hexgrid.compose.data.MutableStateGrid
import com.gonodono.hexgrid.compose.mutableStateGridOf
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.HexOrientation
import com.gonodono.hexgrid.data.Lines
import com.gonodono.hexgrid.data.buildStateMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class OptionsViewModel : ViewModel() {

    private val _gridState = MutableStateFlow(DefaultGridState)

    val gridState = _gridState.asSharedFlow()

    fun toggleSelected(address: Grid.Address) {
        val grid = _gridState.value.grid
        val current: Grid.State = grid[address]

        // This assignment is enough to trigger HexGrid's recomposition.
        grid[address] = current.copy(isSelected = !current.isSelected)

        // This is used to signal the HexGridView to invalidate.
        val selected = grid.cells.count { it.state.isSelected }
        val copy = _gridState.value.copy(selected = selected)
        _gridState.value = copy
    }

    var rowCount by changeGrid(DefaultGridState.grid.size.rowCount) { grid, value ->
        grid.newGrid(rowCount = value)
    }
    var columnCount by changeGrid(DefaultGridState.grid.size.columnCount) { grid, value ->
        grid.newGrid(columnCount = value)
    }
    var insetEvenLines by changeGrid(DefaultGridState.grid.insetEvenLines) { grid, value ->
        grid.newGrid(insetEvenLines = value)
    }
    var enableEdgeLines by changeGrid(DefaultGridState.grid.enableEdgeLines) { grid, value ->
        grid.newGrid(enableEdgeLines = value)
    }

    var fitMode by changeState(DefaultGridState.fitMode) { state, value ->
        state.copy(fitMode = value)
    }
    var crossMode by changeState(DefaultGridState.crossMode) { state, value ->
        state.copy(crossMode = value)
    }
    var hexOrientation by changeState(DefaultGridState.hexOrientation) { state, value ->
        state.copy(hexOrientation = value)
    }
    var strokeWidth by changeState(DefaultGridState.strokeWidth) { state, value ->
        state.copy(strokeWidth = value)
    }

    var strokeColor by changeState(DefaultGridState.strokeColor) { state, value ->
        state.copy(strokeColor = value)
    }
    var fillColor by changeState(DefaultGridState.fillColor) { state, value ->
        state.copy(fillColor = value)
    }
    var selectColor by changeState(DefaultGridState.selectColor) { state, value ->
        state.copy(selectColor = value)
    }
    var indexColor by changeState(DefaultGridState.indexColor) { state, value ->
        state.copy(indexColor = value)
    }
    var cellIndices by changeState(DefaultGridState.cellIndices) { state, value ->
        state.copy(cellIndices = value)
    }

    private fun <T> changeGrid(
        initialValue: T,
        createGrid: (current: Grid, newValue: T) -> MutableStateGrid
    ) = changeState(initialValue) { current, value ->
        current.copy(grid = createGrid(current.grid, value))
    }

    private fun <T> changeState(
        initialValue: T,
        createState: (current: GridState, newValue: T) -> GridState
    ) = object : ReadWriteProperty<Any?, T> {
        private var value = initialValue
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (this.value == value) return
            this.value = value
            _gridState.value = createState(_gridState.value, value)
        }
    }
}

internal data class GridState(
    val grid: MutableStateGrid,
    val selected: Int,
    val fitMode: FitMode,
    val crossMode: CrossMode,
    val hexOrientation: HexOrientation,
    val strokeWidth: Float,
    val strokeColor: Int,
    val fillColor: Int,
    val selectColor: Int,
    val indexColor: Int,
    val cellIndices: Lines
)

internal val DefaultGridState = GridState(
    grid = mutableStateGridOf(
        rowCount = 5,
        columnCount = 5,
        insetEvenLines = true,
        initial = buildStateMap { select(at(2, 1), at(2, 3)) }
    ),
    selected = 0,
    fitMode = FitMode.FitColumns,
    crossMode = CrossMode.AlignCenter,
    hexOrientation = HexOrientation.Horizontal,
    strokeWidth = 0F,
    strokeColor = Color.BLUE,
    fillColor = Color.CYAN,
    selectColor = Color.MAGENTA,
    indexColor = Color.BLUE,
    cellIndices = Lines.None
)

private fun Grid.newGrid(
    rowCount: Int = this.size.rowCount,
    columnCount: Int = this.size.columnCount,
    insetEvenLines: Boolean = this.insetEvenLines,
    enableEdgeLines: Boolean = this.enableEdgeLines
) = mutableStateGridOf(rowCount, columnCount, insetEvenLines, enableEdgeLines)