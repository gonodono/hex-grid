package com.gonodono.hexgrid.demo.examples.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Interface for the Examples' hexagonal [Shape]s.
 */
sealed interface HexagonShape : Shape

/**
 * Creates and remembers a sized [HexagonShape] for use with [Hexagon]s.
 */
@Composable
fun rememberHexagonShape(isHorizontal: Boolean, size: Size): HexagonShape =
    remember(isHorizontal, size) { SizedHexagonShape(isHorizontal, size) }

/**
 * A [HexagonShape] that pre-computes its [Outline], for use with the [Hexagon]
 * Composable.
 */
@Immutable
private class SizedHexagonShape(
    isHorizontal: Boolean,
    size: Size
) : HexagonShape {

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

/**
 * A general-purpose [HexagonShape] with a major axis aligned horizontally.
 *
 * This does _not_ enforce a regular hexagon. The shape is defined by vertices
 * that are placed along the bounds with the same proportional spacing as a
 * regular hexagon, but the bounds' dimensions are not constrained in any way.
 */
@Suppress("unused")
data object HorizontalHexagonShape : HexagonShape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
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
        return Outline.Generic(path)
    }
}

/**
 * A general-purpose [HexagonShape] with a major axis aligned vertically.
 *
 * This does _not_ enforce a regular hexagon. The shape is defined by vertices
 * that are placed along the bounds with the same proportional spacing as a
 * regular hexagon, but the bounds' dimensions are not constrained in any way.
 */
@Suppress("unused")
data object VerticalHexagonShape : HexagonShape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
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
        return Outline.Generic(path)
    }
}