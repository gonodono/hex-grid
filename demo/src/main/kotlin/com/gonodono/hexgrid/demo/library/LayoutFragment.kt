package com.gonodono.hexgrid.demo.library

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.gonodono.hexgrid.compose.toImmutableGrid
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.MutableGrid
import com.gonodono.hexgrid.data.toggle
import com.gonodono.hexgrid.data.toggled
import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.databinding.FragmentLayoutBinding
import com.gonodono.hexgrid.demo.databinding.LayoutItemBinding
import com.gonodono.hexgrid.demo.internal.LabelDrawable
import com.gonodono.hexgrid.view.HexGridView
import android.graphics.Color as AndroidColor

class LayoutFragment : Fragment(R.layout.fragment_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ui = FragmentLayoutBinding.bind(view)

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

        val mutableGrid = LayoutGrid.copy()
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

        val size = resources.getDimension(R.dimen.label_size)
        val viewLabel = LabelDrawable("View", size).also {
            ui.hexGrid.background = it
            it.showStats(LayoutGrid)
        }
        val composeLabel = LabelDrawable("Compose", size).also {
            ui.composeView.background = it
            it.showStats(LayoutGrid)
        }

        ui.hexGrid.onClickListener = HexGridView.OnClickListener { address ->
            mutableGrid.toggle(address)
            ui.hexGrid.invalidate()
            viewLabel.showStats(mutableGrid)
        }

        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // TODO https://issuetracker.google.com/issues/336842920
                CompositionLocalProvider(
                    androidx.lifecycle.compose.LocalLifecycleOwner provides
                            androidx.compose.ui.platform.LocalLifecycleOwner.current
                ) {
                    LayoutHexGrid(
                        crossMode,
                        showStroke,
                        showIcons,
                        showBackgrounds
                    ) { composeLabel.showStats(it) }
                }
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
    showStats: (Grid) -> Unit
) {
    var immutableGrid by remember {
        mutableStateOf(LayoutGrid.toImmutableGrid())
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
            showStats(immutableGrid)
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
            shadowElevation = elevation,
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

private fun LabelDrawable.showStats(grid: Grid) {
    var selected = 0
    var visible = 0
    grid.forEach { _, state ->
        if (state.isSelected) selected++
        if (state.isVisible) visible++
    }
    info = "$selected/$visible"
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