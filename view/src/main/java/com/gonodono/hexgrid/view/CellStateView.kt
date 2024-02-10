package com.gonodono.hexgrid.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * A dummy View that allows Grid.State properties to be set through child
 * <View>s of a <HexGridView> without adding an actual View instance at runtime.
 *
 * The <View>'s XML attributes are used for the initial property values, and the
 * View object itself is discarded.
 *
 * Only usable through layout XML, as it requires a non-null [AttributeSet].
 */
class CellStateView(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs)