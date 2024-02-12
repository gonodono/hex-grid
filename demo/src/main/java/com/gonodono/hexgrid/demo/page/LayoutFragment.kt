package com.gonodono.hexgrid.demo.page

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.gonodono.hexgrid.compose.HexGrid
import com.gonodono.hexgrid.compose.HexGridDefaults
import com.gonodono.hexgrid.compose.toImmutable
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.MutableGrid
import com.gonodono.hexgrid.data.toggle
import com.gonodono.hexgrid.data.toggled
import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.databinding.FragmentLayoutBinding
import com.gonodono.hexgrid.demo.databinding.LayoutItemBinding
import com.gonodono.hexgrid.demo.drawable.LabelDrawable
import com.gonodono.hexgrid.view.HexGridView
import com.google.android.material.snackbar.Snackbar
import android.graphics.Color as AndroidColor


class LayoutFragment : Fragment(R.layout.fragment_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ui = FragmentLayoutBinding.bind(view)

        ui.hexGrid.background = LabelDrawable("Views")
        ui.composeView.background = LabelDrawable("Compose")

        var showStroke by mutableStateOf(true)
        var showIcons by mutableStateOf(true)
        var showBackgrounds by mutableStateOf(true)
        var crossMode by mutableStateOf(CrossMode.AlignCenter)

        ui.switchStroke.setOnCheckedChangeListener { _, isChecked ->
            showStroke = isChecked
            ui.hexGrid.strokeColor = when {
                isChecked -> AndroidColor.BLACK
                else -> AndroidColor.TRANSPARENT
            }
        }
        ui.switchIcons.setOnCheckedChangeListener { _, isChecked ->
            showIcons = isChecked
            ui.hexGrid.notifyViewsInvalidated()
        }
        ui.switchBackgrounds.setOnCheckedChangeListener { _, isChecked ->
            showBackgrounds = isChecked
            ui.hexGrid.notifyViewsInvalidated()
        }
        ui.groupCrossMode.setOnCheckedChangeListener { _, checkedId ->
            ui.hexGrid.crossMode = when (checkedId) {
                ui.radioCenter.id -> CrossMode.AlignCenter
                ui.radioStart.id -> CrossMode.AlignStart
                ui.radioEnd.id -> CrossMode.AlignEnd
                else -> CrossMode.ScaleToFit
            }.also { crossMode = it }
        }

        val mutableGrid = LayoutGrid.copy(emptyMap())
        ui.hexGrid.grid = mutableGrid
        ui.hexGrid.viewProvider = HexGridView.ViewProvider { address, current ->
            val item = when {
                current != null -> LayoutItemBinding.bind(current)
                else -> LayoutItemBinding.inflate(
                    layoutInflater,
                    ui.hexGrid,
                    false
                )
            }
            if (showBackgrounds) {
                ui.hexGrid.applyHexBackground(
                    item.root,
                    colorFor(address).toArgb(),
                    resources.getDimension(R.dimen.hex_inset)
                )
            } else {
                ui.hexGrid.removeHexBackground(item.root)
            }
            item.image.isVisible = showIcons
            item.root
        }

        fun toast(grid: Grid, address: Grid.Address, anchor: View? = null) {
            val (selected, visible) = grid.stats()
            Snackbar.make(
                ui.root,
                "$address – Selected: $selected/$visible",
                Snackbar.LENGTH_SHORT
            ).apply {
                if (anchor != null) anchorView = anchor
                show()
            }
        }

        ui.hexGrid.onClickListener = HexGridView.OnClickListener { address ->
            mutableGrid.toggle(address)
            ui.hexGrid.invalidate()
            toast(mutableGrid, address, ui.groupCrossMode)
        }

        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LayoutHexGrid(
                    crossMode,
                    showStroke,
                    showIcons,
                    showBackgrounds,
                    ::toast
                )
            }
        }
    }
}

@Composable
private fun LayoutHexGrid(
    crossMode: CrossMode,
    showStroke: Boolean,
    showIcons: Boolean,
    showBackgrounds: Boolean,
    toast: (Grid, Grid.Address) -> Unit
) {
    var immutableGrid by remember {
        mutableStateOf(LayoutGrid.toImmutable())
    }
    val strokeColor = when {
        showStroke -> Color.Black
        else -> Color.Transparent
    }
    HexGrid(
        grid = immutableGrid,
        crossMode = crossMode,
        colors = HexGridDefaults.colors(strokeColor = strokeColor),
        onGridTap = { address ->
            immutableGrid = immutableGrid.toggled(address)
            toast(immutableGrid, address)
        }
    ) { address ->
        val color = when {
            showBackgrounds -> colorFor(address)
            else -> Color.Transparent
        }
        val elevation = when {
            showBackgrounds -> dimensionResource(R.dimen.hex_elevation)
            else -> 0.dp
        }
        val shape = when {
            showBackgrounds -> getHexShape(dimensionResource(R.dimen.hex_inset))
            else -> RectangleShape
        }
        Surface(
            color = color,
            elevation = elevation,
            shape = shape,
            modifier = Modifier.fillMaxSize()
        ) {
            if (showIcons) Image(
                painter = painterResource(R.drawable.ic_android),
                contentDescription = "Icon",
                alignment = Alignment.Center
            )
        }
    }
}

private val LayoutGrid = MutableGrid(3, 5, insetEvenLines = true)

private fun Grid.stats(): Pair<Int, Int> {
    var selected = 0
    var visible = 0
    forEach { _, state ->
        if (state.isSelected) selected++
        if (state.isVisible) visible++
    }
    return selected to visible
}

private fun colorFor(address: Grid.Address) =
    Colors[address] ?: Color.Transparent

private val Colors = mapOf(
    Grid.Address(0, 1) to Color.Cyan,
    Grid.Address(0, 3) to Color.Yellow,
    Grid.Address(1, 0) to Color.Red,
    Grid.Address(1, 2) to Color.LightGray,
    Grid.Address(1, 4) to Color.Blue,
    Grid.Address(2, 1) to Color.Green,
    Grid.Address(2, 3) to Color.Magenta
)