package com.gonodono.hexgrid.demo.examples.shield

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.hexSizeForLineCount
import com.gonodono.hexgrid.demo.examples.internal.HexagonDrawable
import com.gonodono.hexgrid.demo.examples.internal.MARGIN_DP
import kotlin.math.roundToInt

fun ConstraintLayout.hexShield(isHorizontal: Boolean) {
    val available = width.toFloat() - paddingLeft - paddingRight
    val margin = MARGIN_DP * resources.displayMetrics.density
    val (hexWidth, hexHeight) = hexSizeForLineCount(
        lineCount = if (isHorizontal) 3 else 5,
        available = available,
        margin = margin,
        isMajor = isHorizontal,
        isHorizontal = isHorizontal
    )
    children.forEach { child ->
        (child.background as HexagonDrawable).isHorizontal = isHorizontal
        child.updateLayoutParams<ConstraintLayout.LayoutParams> {
            width = hexWidth.roundToInt()
            height = hexHeight.roundToInt()
            if (child.id == R.id.center) return@forEach
            val name = resources.getResourceEntryName(child.id)
            val index = name.split("_")[1].toIntOrNull() ?: return@forEach
            circleAngle = (60F * index) + if (isHorizontal) 0F else 30F
            val dimension = if (isHorizontal) hexHeight else hexWidth
            circleRadius = (dimension + margin).roundToInt()
        }
    }
}