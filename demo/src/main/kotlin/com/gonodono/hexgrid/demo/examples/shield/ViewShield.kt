package com.gonodono.hexgrid.demo.examples.shield

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.examples.internal.HexagonDrawable
import com.gonodono.hexgrid.demo.examples.internal.MARGIN_DP
import com.gonodono.hexgrid.demo.examples.internal.hexSizeForLineCount
import kotlin.math.roundToInt

fun ConstraintLayout.hexShield(isHorizontal: Boolean) {
    val available = width.toFloat() - paddingLeft - paddingRight
    val margin = MARGIN_DP * resources.displayMetrics.density
    val (hexWidth, hexHeight) = hexSizeForLineCount(
        lineCount = if (isHorizontal) 3 else 5,
        isHorizontal = isHorizontal,
        available = available,
        margin = margin,
        isMajor = isHorizontal
    )

    val childWidth = hexWidth.roundToInt()
    val childHeight = hexHeight.roundToInt()
    val shortSide = if (isHorizontal) hexHeight else hexWidth
    val radius = (shortSide + margin).roundToInt()

    children.forEach { child ->
        (child.background as HexagonDrawable).isHorizontal = isHorizontal
        child.updateLayoutParams<ConstraintLayout.LayoutParams> {
            width = childWidth
            height = childHeight
            val index = outerIds.indexOf(child.id)
            if (index < 0) return@updateLayoutParams
            circleAngle = (60F * index) + if (isHorizontal) 0F else 30F
            circleRadius = radius
        }
    }
}

private val outerIds = arrayOf(
    R.id.outer_red,
    R.id.outer_yellow,
    R.id.outer_green,
    R.id.outer_cyan,
    R.id.outer_blue,
    R.id.outer_magenta
)