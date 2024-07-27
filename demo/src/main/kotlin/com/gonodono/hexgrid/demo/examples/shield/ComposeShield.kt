package com.gonodono.hexgrid.demo.examples.shield

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.util.component1
import androidx.core.util.component2
import com.gonodono.hexgrid.demo.examples.internal.Hexagon
import com.gonodono.hexgrid.demo.examples.internal.MARGIN_DP
import com.gonodono.hexgrid.demo.examples.internal.hexSizeForLineCount
import com.gonodono.hexgrid.demo.examples.internal.rememberHexagonShape

@Preview(showBackground = true)
@Composable
fun HexShield(isHorizontal: Boolean = true) {
    var availableSize by remember { mutableStateOf(IntSize.Zero) }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { availableSize = it }
    ) {
        val margin = MARGIN_DP * LocalDensity.current.density
        val available = availableSize.width.toFloat()
        val (hexWidth, hexHeight) = hexSizeForLineCount(
            lineCount = if (isHorizontal) 3 else 5,
            isHorizontal = isHorizontal,
            available = available,
            margin = margin,
            isMajor = isHorizontal
        )

        val size = Size(hexWidth, hexHeight)
        val dpSize = with(LocalDensity.current) { size.toDpSize() }
        val shape = rememberHexagonShape(isHorizontal, size)

        val center = createRef()
        Hexagon(
            ref = center,
            size = dpSize,
            shape = shape,
            backgroundColor = Color.LightGray
        ) {
            start.linkTo(parent.start)
            top.linkTo(parent.top)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
        }

        val shortSide = if (isHorizontal) dpSize.height else dpSize.width
        val radius = shortSide + MARGIN_DP.dp
        repeat(6) { index ->
            Hexagon(
                ref = createRef(),
                size = dpSize,
                shape = shape,
                backgroundColor = outerColors[index]
            ) {
                val angle = (60F * index) + if (isHorizontal) 0F else 30F
                circular(other = center, angle = angle, distance = radius)
            }
        }
    }
}

private val outerColors = arrayOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta
)