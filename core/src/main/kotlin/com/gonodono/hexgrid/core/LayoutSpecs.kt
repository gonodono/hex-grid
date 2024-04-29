package com.gonodono.hexgrid.core

import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.HexOrientation

data class LayoutSpecs(
    val fitMode: FitMode,
    val crossMode: CrossMode,
    val hexOrientation: HexOrientation,
    val strokeWidth: Float
)