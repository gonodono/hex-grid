package com.gonodono.hexgrid.demo.examples.grid

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.databinding.FragmentGridBinding
import com.gonodono.hexgrid.demo.examples.internal.doOnSizeChanges
import com.gonodono.hexgrid.demo.internal.LabelDrawable

class GridFragment : Fragment(R.layout.fragment_grid) {

    private lateinit var ui: FragmentGridBinding

    private lateinit var fitMode: MutableState<FitMode>

    private lateinit var isHorizontal: MutableState<Boolean>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ui = FragmentGridBinding.bind(view).also { ui = it }

        fitMode = mutableStateOf(fitMode())
        isHorizontal = mutableStateOf(ui.radioHorizontal.isChecked)

        val size = resources.getDimension(R.dimen.label_size)
        ui.constraintContainer.background = LabelDrawable("View", size)
        ui.composeView.background = LabelDrawable("Compose", size)

        ui.groupFramework.setOnCheckedChangeListener { _, checkedId ->
            ui.dualPaneView.displayedChild = when (checkedId) {
                ui.radioCompose.id -> 1
                else -> 0
            }
        }
        ui.groupOrientation.setOnCheckedChangeListener { _, _ -> hexGrid() }
        ui.groupFitMode.setOnCheckedChangeListener { _, _ -> hexGrid() }
        ui.constraintContainer.doOnSizeChanges { hexGrid() }

        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent { HexGrid(fitMode.value, isHorizontal.value) }
        }
    }

    private fun hexGrid() = with(ui) {
        // The target size is coming from the container, so it's passed in.
        constraintLayout.hexGrid(
            fitMode(),
            radioHorizontal.isChecked,
            with(constraintContainer) { width - paddingLeft - paddingRight },
            with(constraintContainer) { height - paddingTop - paddingBottom },
        )
        fitMode.value = fitMode()
        isHorizontal.value = radioHorizontal.isChecked
    }

    private fun fitMode() = when {
        ui.radioFitHex.isChecked -> FitMode.FitHex
        ui.radioFitRows.isChecked -> FitMode.FitRows
        else -> FitMode.FitColumns
    }
}