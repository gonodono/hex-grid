package com.gonodono.hexgrid.demo.examples.shield

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.databinding.FragmentShieldBinding
import com.gonodono.hexgrid.demo.examples.internal.doOnSizeChanges
import com.gonodono.hexgrid.demo.internal.LabelDrawable

class ShieldFragment : Fragment(R.layout.fragment_shield) {

    private lateinit var ui: FragmentShieldBinding

    private lateinit var isHorizontal: MutableState<Boolean>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ui = FragmentShieldBinding.bind(view).also { ui = it }
        isHorizontal = mutableStateOf(ui.radioHorizontal.isChecked)

        val size = resources.getDimension(R.dimen.label_size)
        ui.constraintLayout.background = LabelDrawable("View", size)
        ui.composeView.background = LabelDrawable("Compose", size)

        ui.groupFramework.setOnCheckedChangeListener { _, checkedId ->
            ui.dualPaneView.displayedChild = when (checkedId) {
                ui.radioCompose.id -> 1
                else -> 0
            }
        }
        ui.groupOrientation.setOnCheckedChangeListener { _, _ -> hexShield() }
        ui.constraintLayout.doOnSizeChanges { hexShield() }

        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent { HexShield(isHorizontal.value) }
        }
    }

    private fun hexShield() = with(ui) {
        constraintLayout.hexShield(radioHorizontal.isChecked)
        isHorizontal.value = radioHorizontal.isChecked
    }
}