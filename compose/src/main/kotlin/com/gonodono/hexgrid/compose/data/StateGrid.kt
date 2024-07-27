package com.gonodono.hexgrid.compose.data

import androidx.compose.runtime.Stable
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.MutableGrid

/**
 * The Compose version of [Grid], [StateGrid] simply extends it to add the
 * [Stable] annotation.
 */
@Stable
sealed interface StateGrid : Grid

/**
 * The Compose version of [MutableGrid], [MutableStateGrid] simply extends it
 * to add the [Stable] annotation.
 */
@Stable
sealed interface MutableStateGrid : StateGrid, MutableGrid