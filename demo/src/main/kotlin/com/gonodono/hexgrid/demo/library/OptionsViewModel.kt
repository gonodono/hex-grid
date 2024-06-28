package com.gonodono.hexgrid.demo.library

import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.HexOrientation
import com.gonodono.hexgrid.data.MutableGrid
import com.gonodono.hexgrid.data.buildStateMap
import com.gonodono.hexgrid.data.toggled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class OptionsViewModel : ViewModel() {

    private val _gridState = MutableStateFlow(DefaultGridState)

    val gridState = _gridState.asSharedFlow()

    fun toggleSelected(address: Grid.Address) {
        val current = _gridState.value.grid
        val next = current.toggled(address)
        _gridState.value = _gridState.value.copy(grid = next)
    }

    var rowCount by changeGrid(DefaultGridState.grid.rowCount) { grid, value ->
        grid.newGrid(rowCount = value)
    }
    var columnCount by changeGrid(DefaultGridState.grid.columnCount) { grid, value ->
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
    var showRowIndices by changeState(DefaultGridState.showRowIndices) { state, value ->
        state.copy(showRowIndices = value)
    }
    var showColumnIndices by changeState(DefaultGridState.showColumnIndices) { state, value ->
        state.copy(showColumnIndices = value)
    }

    private fun <T> changeGrid(
        initialValue: T,
        createGrid: (current: MutableGrid, newValue: T) -> MutableGrid
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
    val grid: MutableGrid,
    val fitMode: FitMode,
    val crossMode: CrossMode,
    val hexOrientation: HexOrientation,
    val strokeWidth: Float,
    val strokeColor: Int,
    val fillColor: Int,
    val selectColor: Int,
    val indexColor: Int,
    val showRowIndices: Boolean,
    val showColumnIndices: Boolean
)

internal val DefaultGridState = GridState(
    grid = MutableGrid(
        rowCount = 5,
        columnCount = 5,
        insetEvenLines = true,
        initial = buildStateMap { select(at(2, 1), at(2, 3)) }
    ),
    fitMode = FitMode.FitColumns,
    crossMode = CrossMode.AlignCenter,
    hexOrientation = HexOrientation.Horizontal,
    strokeWidth = 0F,
    strokeColor = Color.BLUE,
    fillColor = Color.CYAN,
    selectColor = Color.MAGENTA,
    indexColor = Color.BLUE,
    showRowIndices = false,
    showColumnIndices = false
)

private fun MutableGrid.newGrid(
    rowCount: Int = this.rowCount,
    columnCount: Int = this.columnCount,
    insetEvenLines: Boolean = this.insetEvenLines,
    enableEdgeLines: Boolean = this.enableEdgeLines
) = MutableGrid(rowCount, columnCount, insetEvenLines, enableEdgeLines)