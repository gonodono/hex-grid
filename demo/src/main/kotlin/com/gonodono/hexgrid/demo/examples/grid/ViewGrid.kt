package com.gonodono.hexgrid.demo.examples.grid

import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.Gravity
import android.view.View.LAYOUT_DIRECTION_LTR
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.naturalRowStartAngle
import com.gonodono.hexgrid.demo.examples.internal.HexGridCalculator.naturalRowTailAngle
import com.gonodono.hexgrid.demo.examples.internal.HexagonDrawable
import com.gonodono.hexgrid.demo.examples.internal.MARGIN_DP
import kotlin.math.roundToInt

internal fun ConstraintLayout.hexGrid(
    fitMode: FitMode,
    isHorizontal: Boolean,
    availableWidth: Int,
    availableHeight: Int
) {
    if (availableWidth <= 0 || availableHeight <= 0) return

    val density = context.resources.displayMetrics.density
    val data = calculateGridData(
        fitMode,
        isHorizontal,
        availableWidth,
        availableHeight,
        MARGIN_DP,
        density
    )

    buildGrid(
        data.rowCount,
        data.columnCount,
        data.hexWidth,
        data.hexHeight,
        data.radius,
        isHorizontal
    )
}

private fun ConstraintLayout.buildGrid(
    rowCount: Int,
    columnCount: Int,
    hexWidth: Float,
    hexHeight: Float,
    radius: Int,
    isHorizontal: Boolean
) {
    val density = context.resources.displayMetrics.density
    val isLtr = resources.configuration.layoutDirection == LAYOUT_DIRECTION_LTR

    // Workaround for an apparent bug in ConstraintLayout that causes layout to
    // flake with wrap_content in RTL mode upon removing/adding Views after the
    // initial layout. You might not need it if you're not doing that.
    val p = if (!isLtr) parent as? ViewGroup else null
    p?.removeView(this)

    removeAllViews()
    for (row in 0..<rowCount) {
        for (column in 0..<columnCount) {
            val viewId = row * columnCount + column + 1 // View.id should be > 0
            val params = ConstraintLayout.LayoutParams(
                hexWidth.roundToInt(),
                hexHeight.roundToInt()
            )
            when {
                row == 0 && column == 0 -> {
                    params.startToStart = PARENT_ID
                    params.topToTop = PARENT_ID
                }

                column == 0 -> {
                    params.circleConstraint = viewId - columnCount
                    params.circleRadius = radius
                    params.circleAngle =
                        naturalRowStartAngle(isHorizontal, isLtr, row)
                }

                else -> {
                    params.circleConstraint = viewId - 1
                    params.circleRadius = radius
                    params.circleAngle =
                        naturalRowTailAngle(isHorizontal, isLtr, column)
                }
            }
            val view = TextView(context).apply {
                id = viewId
                val color = evaluator.evaluate(
                    (viewId - 1).toFloat() / (rowCount * columnCount),
                    Color.BLUE, Color.MAGENTA
                ) as Int
                background = HexagonDrawable(isHorizontal).apply {
                    setTintList(ColorStateList.valueOf(color))
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2 * density
                }
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                text = viewId.toString()
            }
            addView(view, params)
        }
    }

    p?.addView(this)
}

private val evaluator = ArgbEvaluator()