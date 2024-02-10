package com.gonodono.hexgrid.demo.page

import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.gonodono.hexgrid.data.DefaultLayoutSpecs
import com.gonodono.hexgrid.data.Grid.Address
import com.gonodono.hexgrid.data.Grid.State
import com.gonodono.hexgrid.data.LayoutSpecs
import com.gonodono.hexgrid.data.MutableGrid
import com.gonodono.hexgrid.data.toggled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


internal class GridViewModel : ViewModel() {

    private val _gridState = MutableStateFlow(DefaultGridState)

    val gridState = _gridState.asSharedFlow()

    fun toggleSelected(address: Address) {
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

    var fitMode by changeSpecs(DefaultGridState.layoutSpecs.fitMode) { specs, value ->
        specs.copy(fitMode = value)
    }
    var crossMode by changeSpecs(DefaultGridState.layoutSpecs.crossMode) { specs, value ->
        specs.copy(crossMode = value)
    }
    var hexOrientation by changeSpecs(DefaultGridState.layoutSpecs.hexOrientation) { specs, value ->
        specs.copy(hexOrientation = value)
    }
    var strokeWidth by changeSpecs(DefaultGridState.layoutSpecs.strokeWidth) { specs, value ->
        specs.copy(strokeWidth = value)
    }

    var strokeColor by doOnChange(DefaultGridState.strokeColor) { value ->
        _gridState.value = _gridState.value.copy(strokeColor = value)
    }
    var fillColor by doOnChange(DefaultGridState.fillColor) { value ->
        _gridState.value = _gridState.value.copy(fillColor = value)
    }
    var selectColor by doOnChange(DefaultGridState.selectColor) { value ->
        _gridState.value = _gridState.value.copy(selectColor = value)
    }
    var showRowIndices by doOnChange(DefaultGridState.showRowIndices) { value ->
        _gridState.value = _gridState.value.copy(showRowIndices = value)
    }
    var showColumnIndices by doOnChange(DefaultGridState.showColumnIndices) { value ->
        _gridState.value = _gridState.value.copy(showColumnIndices = value)
    }

    private fun <T> changeGrid(
        initialValue: T,
        createGrid: (MutableGrid, newValue: T) -> MutableGrid
    ) = doOnChange(initialValue) { value ->
        val currentState = _gridState.value
        _gridState.value =
            currentState.copy(grid = createGrid(currentState.grid, value))
    }

    private fun <T> changeSpecs(
        initialValue: T,
        createSpecs: (LayoutSpecs, newValue: T) -> LayoutSpecs
    ) = doOnChange(initialValue) { value ->
        val currentState = _gridState.value
        _gridState.value = currentState.copy(
            layoutSpecs = createSpecs(currentState.layoutSpecs, value)
        )
    }

    private fun <T> doOnChange(
        initialValue: T,
        onChange: (newValue: T) -> Unit
    ) = object : ReadWriteProperty<Any?, T> {
        private var value = initialValue
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            if (this.value == value) return
            this.value = value
            onChange(value)
        }
    }
}

internal data class GridState(
    val grid: MutableGrid,
    val layoutSpecs: LayoutSpecs,
    val strokeColor: Int,
    val fillColor: Int,
    val selectColor: Int,
    val showRowIndices: Boolean,
    val showColumnIndices: Boolean
)

internal val DefaultGridState = GridState(
    grid = MutableGrid(
        rowCount = 5,
        columnCount = 5,
        insetEvenLines = true,
        initial = mapOf(
            Address(2, 1) to State(isSelected = true),
            Address(2, 3) to State(isSelected = true),
        )
    ),
    layoutSpecs = DefaultLayoutSpecs,
    strokeColor = Color.BLUE,
    fillColor = Color.CYAN,
    selectColor = Color.MAGENTA,
    showRowIndices = false,
    showColumnIndices = false
)

private fun MutableGrid.newGrid(
    rowCount: Int = this.rowCount,
    columnCount: Int = this.columnCount,
    insetEvenLines: Boolean = this.insetEvenLines,
    enableEdgeLines: Boolean = this.enableEdgeLines
) = MutableGrid(rowCount, columnCount, insetEvenLines, enableEdgeLines)