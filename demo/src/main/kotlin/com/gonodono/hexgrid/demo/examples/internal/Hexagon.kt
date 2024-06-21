package com.gonodono.hexgrid.demo.examples.internal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope

/**
 * This is the Compose version's analog of [HexagonDrawable].
 */
@Composable
internal fun ConstraintLayoutScope.Hexagon(
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

@Composable
internal fun rememberHexagonShape(
    isHorizontal: Boolean,
    size: Size
): HexagonShape = remember(isHorizontal, size) {
    HexagonShape(isHorizontal, size)
}

/**
 * The [Shape] used for the [Hexagon] Composable that can pre-compute its
 * [Outline].
 */
@Immutable
internal class HexagonShape(isHorizontal: Boolean, size: Size) : Shape {

    private val outline = when {
        isHorizontal -> {
            val major = size.width
            val quarterMajor = major / 4F
            val minor = size.height
            val halfMinor = minor / 2F
            val path = Path().apply {
                moveTo(0F, halfMinor)
                lineTo(quarterMajor, 0F)
                lineTo(3 * quarterMajor, 0F)
                lineTo(major, halfMinor)
                lineTo(3 * quarterMajor, minor)
                lineTo(quarterMajor, minor)
                lineTo(0F, halfMinor)
            }
            Outline.Generic(path)
        }
        else -> {
            val major = size.height
            val quarterMajor = major / 4F
            val minor = size.width
            val halfMinor = minor / 2F
            val path = Path().apply {
                moveTo(halfMinor, 0F)
                lineTo(0F, quarterMajor)
                lineTo(0F, 3 * quarterMajor)
                lineTo(halfMinor, major)
                lineTo(minor, 3 * quarterMajor)
                lineTo(minor, quarterMajor)
                lineTo(halfMinor, 0F)
            }
            Outline.Generic(path)
        }
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = outline
}