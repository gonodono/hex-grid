package com.gonodono.hexgrid.compose

import android.graphics.Rect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gonodono.hexgrid.compose.data.StateGrid
import com.gonodono.hexgrid.core.GridUi
import com.gonodono.hexgrid.core.LayoutSpecs
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.HexOrientation
import com.gonodono.hexgrid.data.Lines

/**
 * The Compose version of the library's hex grid.
 *
 * @param grid The HexGrid's data set
 * @param modifier The Modifier to be applied to the grid
 * @param hexOrientation Orientation of a cell hexagon's major axis
 * @param fitMode Whether rows or columns determine the cells' size
 * @param crossMode Behavior for the other direction, rows or columns
 * @param strokeWidth Stroke width of the cell outline
 * @param colors The grid's three colors: line, fill, and select
 * @param clipToBounds Whether to clip the grid draw to the Composable's bounds
 * @param cellIndices Design/debug option to show each cell's row and/or column
 * @param onGridTap Called with the Grid.Address when a tap hits successfully
 * @param onOutsideTap Called when the Composable is clicked outside of any cell
 * @param cellItems Each cell is allowed one (centered) Composable for content
 */
@Composable
fun HexGrid(
    grid: StateGrid,
    modifier: Modifier = Modifier,
    fitMode: FitMode = FitMode.FitColumns,
    crossMode: CrossMode = CrossMode.AlignCenter,
    hexOrientation: HexOrientation = HexOrientation.Horizontal,
    strokeWidth: Dp = Dp.Hairline,
    colors: HexGridColors = HexGridDefaults.colors(),
    clipToBounds: Boolean = true,
    cellIndices: Lines = Lines.None,
    onGridTap: ((Grid.Address) -> Unit)? = null,
    onOutsideTap: (() -> Unit)? = null,
    cellItems: @Composable (HexGridItemScope.(Grid.Address, Grid.State) -> Unit)? = null
) {
    val density = LocalDensity.current
    val layoutSpecs =
        remember(density, fitMode, crossMode, hexOrientation, strokeWidth) {
            val width = with(density) { strokeWidth.toPx() }
            LayoutSpecs(fitMode, crossMode, hexOrientation, width)
        }

    val gridUi = remember { GridUi() }
    gridUi.apply {
        this.grid = grid
        this.layoutSpecs = layoutSpecs
        this.strokeColor = colors.strokeColor.toArgb()
        this.fillColor = colors.fillColor.toArgb()
        this.selectColor = colors.selectColor.toArgb()
        this.indexColor = colors.indexColor.toArgb()
        this.cellIndices = cellIndices
    }

    val scope = remember(density) { HexGridItemScopeImpl(gridUi, density) }

    SubcomposeLayout(
        modifier
            .drawBehind {
                if (clipToBounds) clipRect {
                    gridUi.drawGrid(drawContext.canvas.nativeCanvas)
                } else {
                    gridUi.drawGrid(drawContext.canvas.nativeCanvas)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val address = gridUi.resolveAddress(offset.x, offset.y)
                    when (address) {
                        null -> onOutsideTap?.invoke()
                        else -> onGridTap?.invoke(address)
                    }
                }
            }
    ) { constraints ->

        val uiSize = gridUi.calculateSize(
            constraints.hasBoundedWidth,
            constraints.hasBoundedHeight,
            constraints.hasFixedWidth,
            constraints.hasFixedHeight,
            constraints.maxWidth,
            constraints.maxHeight
        )

        if (cellItems != null) {
            val items = mutableListOf<Pair<Placeable, Rect>>()
            grid.forEach { address, state ->
                val item = subcompose(address) {
                    scope.prepare(address)
                    scope.cellItems(address, state)
                }.firstOrNull() ?: return@forEach
                val bounds = scope.copyBounds()
                val placeable = item.measure(
                    Constraints(
                        maxWidth = bounds.width(),
                        maxHeight = bounds.height()
                    )
                )
                items += placeable to bounds
            }
            layout(uiSize.width, uiSize.height) {
                items.forEach { (item, bounds) ->
                    val x = bounds.left + (bounds.width() - item.width) / 2
                    val y = bounds.top + (bounds.height() - item.height) / 2
                    item.place(x, y)
                }
            }
        } else {
            layout(uiSize.width, uiSize.height) {}
        }
    }
}

/**
 * Child Composable scope for [HexGrid].
 *
 * The scope exposes the [getHexShape] function for (optionally) shaping the
 * children.
 */
interface HexGridItemScope {

    /**
     * Returns a [Shape] identical to the grid's current cell hexagon, shrunk
     * by [inset] on each side.
     *
     * This may be called at most once per scope invocation. Any further calls
     * will result in an Exception.
     */
    fun getHexShape(inset: Dp = 0.dp): Shape
}

/**
 * Default values used by [HexGrid].
 */
object HexGridDefaults {

    /**
     * The grid's various color values.
     *
     * Colors aren't stateful yet, so they're handled as a simple data class for
     * now.
     *
     * @param strokeColor The cell outline color
     * @param fillColor The normal cell fill color
     * @param selectColor The fill color when the cell is selected
     * @param indexColor The color of the cells' indices, if shown
     */
    fun colors(
        strokeColor: Color = Color.Black,
        fillColor: Color = Color.Transparent,
        selectColor: Color = Color.Gray,
        indexColor: Color = strokeColor
    ): HexGridColors = HexGridColors(
        strokeColor = strokeColor,
        fillColor = fillColor,
        selectColor = selectColor,
        indexColor = indexColor
    )
}

/**
 * The various colors used by [HexGrid].
 *
 * @see HexGridDefaults.colors
 */
@Immutable
data class HexGridColors internal constructor(
    internal val strokeColor: Color,
    internal val fillColor: Color,
    internal val selectColor: Color,
    internal val indexColor: Color
)

private class HexGridItemScopeImpl(
    private val gridUi: GridUi,
    density: Density
) : HexGridItemScope, Density by density {

    private var address = Grid.Address.Origin

    private val itemBounds = Rect()

    private var boundsEmpty = true

    fun prepare(address: Grid.Address) {
        this.address = address
        itemBounds.setEmpty()
        boundsEmpty = true
    }

    override fun getHexShape(inset: Dp): Shape {
        check(boundsEmpty) {
            "getHexShape() may be called only once per scope invocation."
        }
        val build = with(gridUi) {
            val bounds = tmpBounds
            getCellItemBounds(address, inset.toPx(), bounds)
            itemBounds.set(bounds)
            boundsEmpty = false
            bounds.offsetTo(0, 0)
            getHexagonPathBuilder(bounds)
        }
        return GenericShape { _, _ -> asAndroidPath().build() }
    }

    fun copyBounds(): Rect = itemBounds.let { bounds ->
        if (boundsEmpty) gridUi.getCellItemBounds(address, 0F, bounds)
        Rect(bounds)
    }

    private val tmpBounds = Rect()
}