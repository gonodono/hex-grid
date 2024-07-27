package com.gonodono.hexgrid.demo.examples.grid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import com.gonodono.hexgrid.demo.examples.internal.Hexagon
import com.gonodono.hexgrid.demo.examples.internal.MARGIN_DP
import com.gonodono.hexgrid.demo.examples.internal.naturalRowStartAngle
import com.gonodono.hexgrid.demo.examples.internal.naturalRowTailAngle
import com.gonodono.hexgrid.demo.examples.internal.rememberHexagonShape

@Preview(showBackground = true)
@Composable
fun HexGrid(
    fitMode: FitMode = FitMode.FitHex(40F),
    isHorizontal: Boolean = false
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val density = LocalDensity.current
        val data = remember(fitMode, isHorizontal, width, height, density) {
            calculateExampleData(
                fitMode = fitMode,
                isHorizontal = isHorizontal,
                availableWidth = width,
                availableHeight = height,
                marginDp = MARGIN_DP,
                density = density.density
            )
        }

        val size = Size(data.hexWidth, data.hexHeight)
        val dpSize = with(density) { size.toDpSize() }
        val shape = rememberHexagonShape(isHorizontal, size)
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val radius = with(density) { data.radius.toDp() }

        ConstraintLayout {
            val refs = mutableListOf<ConstrainedLayoutReference>()
            for (row in 0..<data.rowCount) {
                for (column in 0..<data.columnCount) {
                    val index = row * data.columnCount + column
                    val ref = createRef().also { refs += it }
                    val total = data.rowCount * data.columnCount
                    val color = lerp(
                        start = Color.Blue,
                        stop = Color.Magenta,
                        fraction = index.toFloat() / total
                    )
                    Hexagon(
                        ref = ref,
                        size = dpSize,
                        shape = shape,
                        borderStroke = BorderStroke(2.dp, color),
                        content = { Text(text = (index + 1).toString()) }
                    ) {
                        when {
                            row == 0 && column == 0 -> {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                            }
                            column == 0 -> {
                                val angle = naturalRowStartAngle(
                                    isHorizontal = isHorizontal,
                                    isLtr = isLtr,
                                    row = row
                                )
                                circular(
                                    other = refs[index - data.columnCount],
                                    angle = angle,
                                    distance = radius
                                )
                            }
                            else -> {
                                val angle = naturalRowTailAngle(
                                    isHorizontal = isHorizontal,
                                    isLtr = isLtr,
                                    column = column
                                )
                                circular(
                                    other = refs[index - 1],
                                    angle = angle,
                                    distance = radius
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}