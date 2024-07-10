package com.gonodono.hexgrid.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * A dummy View that allows [Grid.State][com.gonodono.hexgrid.data.Grid.State]
 * properties to be set through child elements of a [HexGridView] in layout
 * XML, without adding an actual View instance at runtime.
 *
 * ```xml
 * <com.gonodono.hexgrid.view.HexGridView …>
 *
 *     <com.gonodono.hexgrid.view.CellStateView
 *         …
 *         app:layout_rowAndColumn="1,1"
 *         app:layout_cellIsSelected="true" />
 *
 * </com.gonodono.hexgrid.view.HexGridView>
 * ```

 * The XML attributes are used for the initial property values, and the
 * CellStateView object itself is discarded.
 */
class CellStateView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs)