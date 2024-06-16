package com.gonodono.hexgrid.demo.examples.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope

@Composable
internal fun ConstraintLayoutScope.Hexagon(
    ref: ConstrainedLayoutReference,
    size: DpSize,
    modifier: Modifier.(Shape) -> Modifier,
    isHorizontal: Boolean = true,
    text: String? = null,
    constrainBlock: ConstrainScope.() -> Unit
) {
    val shape = GenericShape(hexagonBuilder(isHorizontal))
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .modifier(shape)
            .constrainAs(ref, constrainBlock)
    ) {
        text?.let { Text(text = it, fontWeight = FontWeight.Bold) }
    }
}

private fun hexagonBuilder(
    isHorizontal: Boolean
): Path.(size: Size, layoutDirection: LayoutDirection) -> Unit = { size, _ ->

    val major = if (isHorizontal) size.width else size.height
    val quarterMajor = major / 4F
    val minor = if (isHorizontal) size.height else size.width
    val halfMinor = minor / 2F

    val coordinatePairs = arrayOf(
        floatArrayOf(0F, halfMinor),
        floatArrayOf(quarterMajor, 0F),
        floatArrayOf(3 * quarterMajor, 0F),
        floatArrayOf(major, halfMinor),
        floatArrayOf(3 * quarterMajor, minor),
        floatArrayOf(quarterMajor, minor)
    )

    coordinatePairs.forEachIndexed { index, (c1, c2) ->
        if (isHorizontal) when (index) {
            0 -> moveTo(c1, c2)
            else -> lineTo(c1, c2)
        } else when (index) {
            0 -> moveTo(c2, c1)
            else -> lineTo(c2, c1)
        }
    }
    close()
}