package com.gonodono.hexgrid.demo.examples.internal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope

/**
 * This is the Compose version's analog of [HexagonDrawable].
 */
@Composable
fun ConstraintLayoutScope.Hexagon(
    ref: ConstrainedLayoutReference,
    size: DpSize,
    shape: HexagonShape,
    backgroundColor: Color = Color.Unspecified,
    borderStroke: BorderStroke? = null,
    content: (@Composable () -> Unit)? = null,
    constrainBlock: ConstrainScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .background(backgroundColor, shape)
            .run { borderStroke?.let { border(it, shape) } ?: this }
            .constrainAs(ref, constrainBlock)
    ) {
        content?.invoke()
    }
}