package com.gonodono.hexgrid.demo.library

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.unit.Dp
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gonodono.hexgrid.compose.HexGrid
import com.gonodono.hexgrid.compose.HexGridDefaults
import com.gonodono.hexgrid.compose.ShownIndices
import com.gonodono.hexgrid.compose.asImmutableGrid
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.HexOrientation
import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.databinding.FragmentOptionsBinding
import com.gonodono.hexgrid.demo.internal.FlashDrawable
import com.gonodono.hexgrid.demo.internal.LabelDrawable
import com.gonodono.hexgrid.view.HexGridDrawable
import com.gonodono.hexgrid.view.HexGridView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0
import androidx.compose.ui.graphics.Color as ComposeColor

class OptionsFragment : Fragment(R.layout.fragment_options) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ui = FragmentOptionsBinding.bind(view)

        val model: OptionsViewModel by viewModels()

        with(DefaultGridState) {
            ui.editColumns.setText(grid.columnCount.toString())
            ui.editRows.setText(grid.rowCount.toString())
            ui.editStrokeWidth.setText(strokeWidth.toInt().toString())
            ui.switchInsetEvenLines.isChecked = grid.insetEvenLines
            ui.switchEnableEdgeLines.isChecked = grid.enableEdgeLines
            ui.buttonStrokeColor.setSwatchColor(strokeColor)
            ui.buttonFillColor.setSwatchColor(fillColor)
            ui.buttonSelectColor.setSwatchColor(selectColor)
            ui.checkShowColumnIndices.isChecked = showColumnIndices
            ui.checkShowRowIndices.isChecked = showRowIndices
            ui.drawableView.background = HexGridDrawable(grid).also { d ->
                d.fitMode = fitMode
                d.crossMode = crossMode
                d.hexOrientation = hexOrientation
                d.strokeWidth = strokeWidth
                d.strokeColor = strokeColor
                d.fillColor = fillColor
                d.selectColor = selectColor
                d.showColumnIndices = showColumnIndices
                d.showRowIndices = showRowIndices
            }
        }

        val hexGridDrawable = ui.drawableView.background as HexGridDrawable
        val foregroundFlasher = FlashDrawable(0xAAFF00FF.toInt()).also {
            ui.drawableView.foreground = it
        }
        ui.drawableView.setOnClickListener {
            foregroundFlasher.flash()
            Snackbar.make(
                ui.groupFramework,
                "Drawable is not interactive",
                Snackbar.LENGTH_SHORT
            ).setAnchorView(ui.groupFramework).show()
        }

        val countLabel = LabelDrawable(
            null,
            resources.getDimension(R.dimen.label_size)
        ).also { ui.animator.foreground = it }

        val backgroundFlasher = FlashDrawable(Color.LTGRAY).also {
            ui.animator.background = it
        }
        ui.hexGrid.onClickListener =
            object : HexGridView.OnClickListener {
                override fun onGridClick(address: Grid.Address) {
                    model.toggleSelected(address)
                }

                override fun onOutsideClick() {
                    backgroundFlasher.flash()
                }
            }
        ui.editRows.doOnTextChanged { text, _, _, _ ->
            model.rowCount = text?.toString()?.toIntOrNull() ?: 0
        }
        ui.editColumns.doOnTextChanged { text, _, _, _ ->
            model.columnCount = text?.toString()?.toIntOrNull() ?: 0
        }
        ui.switchInsetEvenLines.setOnCheckedChangeListener { _, isChecked ->
            model.insetEvenLines = isChecked
        }
        ui.switchEnableEdgeLines.setOnCheckedChangeListener { _, isChecked ->
            model.enableEdgeLines = isChecked
        }
        ui.buttonFitMode.setOnClickListener {
            showChoiceDialog(FitMode::class.java, model::fitMode)
        }
        ui.buttonCrossMode.setOnClickListener {
            showChoiceDialog(CrossMode::class.java, model::crossMode)
        }
        ui.buttonHexOrientation.setOnClickListener {
            showChoiceDialog(
                HexOrientation::class.java,
                model::hexOrientation
            )
        }
        ui.editStrokeWidth.doOnTextChanged { text, _, _, _ ->
            model.strokeWidth = text?.toString()?.toFloatOrNull() ?: 0F
        }
        ui.buttonStrokeColor.setOnClickListener {
            showColorDialog(model::strokeColor, ui.buttonStrokeColor)
        }
        ui.buttonFillColor.setOnClickListener {
            showColorDialog(model::fillColor, ui.buttonFillColor)
        }
        ui.buttonSelectColor.setOnClickListener {
            showColorDialog(model::selectColor, ui.buttonSelectColor)
        }
        ui.checkShowRowIndices.setOnCheckedChangeListener { _, isChecked ->
            model.showRowIndices = isChecked
        }
        ui.checkShowColumnIndices.setOnCheckedChangeListener { _, isChecked ->
            model.showColumnIndices = isChecked
        }
        ui.groupFramework.setOnCheckedChangeListener { _, checkedId ->
            ui.animator.displayedChild = when (checkedId) {
                ui.radioView.id -> 0
                ui.radioCompose.id -> 1
                else -> 2
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.gridState.collect { state ->
                    ui.hexGrid.apply {
                        grid = state.grid
                        fitMode = state.fitMode
                        crossMode = state.crossMode
                        hexOrientation = state.hexOrientation
                        strokeWidth = state.strokeWidth
                        strokeColor = state.strokeColor
                        fillColor = state.fillColor
                        selectColor = state.selectColor
                        showColumnIndices = state.showColumnIndices
                        showRowIndices = state.showRowIndices
                    }
                    hexGridDrawable.apply {
                        grid = state.grid
                        fitMode = state.fitMode
                        crossMode = state.crossMode
                        hexOrientation = state.hexOrientation
                        strokeWidth = state.strokeWidth
                        strokeColor = state.strokeColor
                        fillColor = state.fillColor
                        selectColor = state.selectColor
                        showColumnIndices = state.showColumnIndices
                        showRowIndices = state.showRowIndices
                    }
                    var selected = 0
                    state.grid.forEach { _, s -> if (s.isSelected) selected++ }
                    countLabel.info = "$selected/${state.grid.size}"
                }
            }
        }

        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                // TODO https://issuetracker.google.com/issues/336842920
                CompositionLocalProvider(
                    androidx.lifecycle.compose.LocalLifecycleOwner provides
                            androidx.compose.ui.platform.LocalLifecycleOwner.current
                ) {
                    GridHexGrid(model, backgroundFlasher::flash)
                }
            }
        }
    }
}

@Composable
private fun GridHexGrid(
    model: OptionsViewModel,
    onOutsideTap: () -> Unit
) {
    val state by model.gridState.collectAsStateWithLifecycle(DefaultGridState)
    val grid = state.grid.asImmutableGrid()
    val dp = state.strokeWidth /
            LocalContext.current.resources.displayMetrics.density
    HexGrid(
        grid = grid,
        fitMode = state.fitMode,
        crossMode = state.crossMode,
        hexOrientation = state.hexOrientation,
        strokeWidth = Dp(dp),
        colors = HexGridDefaults.colors(
            ComposeColor(state.strokeColor),
            ComposeColor(state.fillColor),
            ComposeColor(state.selectColor)
        ),
        shownIndices = when {
            state.showRowIndices && state.showColumnIndices -> ShownIndices.Both
            state.showColumnIndices -> ShownIndices.Columns
            state.showRowIndices -> ShownIndices.Rows
            else -> ShownIndices.None
        },
        onGridTap = { model.toggleSelected(it) },
        onOutsideTap = onOutsideTap
    )
}

private fun <T : Enum<T>> Fragment.showChoiceDialog(
    enum: Class<T>,
    property: KMutableProperty0<T>
) {
    val values = enum.enumConstants!!
    AlertDialog.Builder(requireContext())
        .setSingleChoiceItems(
            values.map { it.name }.toTypedArray<String>(),
            values.indexOf(property.get())
        ) { dialog, which ->
            property.set(values[which])
            dialog.dismiss()
        }
        .show()
}

@SuppressLint("DefaultLocale")
private fun Fragment.showColorDialog(
    property: KMutableProperty0<Int>,
    button: Button
) {
    val fields = Color::class.java.fields.sortedBy { it.name }
    val values = fields.map { it.get(null) as Int }.toTypedArray()
    @Suppress("DEPRECATION") val names =
        fields.map { it.name.lowercase().capitalize() }
    AlertDialog.Builder(requireContext())
        .setSingleChoiceItems(
            names.toTypedArray<String>(),
            values.indexOf(property.get())
        ) { dialog, which ->
            property.set(values[which])
            button.setSwatchColor(values[which])
            dialog.dismiss()
        }
        .show()
}

private fun Button.setSwatchColor(color: Int) {
    compoundDrawablesRelative[2]?.setTint(color)
}