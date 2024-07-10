package com.gonodono.hexgrid.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.annotation.ColorInt
import androidx.core.graphics.withClip
import androidx.core.view.children
import com.gonodono.hexgrid.core.GridUi
import com.gonodono.hexgrid.core.LayoutSpecs
import com.gonodono.hexgrid.data.CrossMode
import com.gonodono.hexgrid.data.FitMode
import com.gonodono.hexgrid.data.Grid
import com.gonodono.hexgrid.data.HexOrientation
import com.gonodono.hexgrid.data.Lines
import com.gonodono.hexgrid.data.MutableGrid
import com.gonodono.hexgrid.view.HexGridView.OnClickListener
import com.gonodono.hexgrid.view.HexGridView.ViewProvider
import kotlin.collections.set
import kotlin.reflect.KMutableProperty0

/**
 * The View version of the library's hex grid.
 *
 * HexGridView is a ViewGroup that can hold one child View for each cell. The
 * group recognizes special layout attributes on its children to allow cell
 * assignment and initialization in the layout XML. For example:
 *
 * ```xml
 * <com.gonodono.hexgrid.view.HexGridView …>
 *
 *     <Button
 *         …
 *         app:layout_rowAndColumn="1,1"
 *         app:layout_cellIsSelected="true" />
 *
 * </com.gonodono.hexgrid.view.HexGridView>
 * ```
 *
 * If the library's [CellStateView] is used instead of a regular View, no
 * actual View object is added to the HexGridView, and the element is used
 * only for the attribute settings.
 *
 * The class also offers the [ViewProvider] interface as a way to assign Views
 * in code instead. This method always overrides anything set in the layout XML
 * for a given cell.
 *
 * HexGridView offers its own [OnClickListener] interface and corresponding
 * property for click callbacks, and the drawn grid's cosmetic values are all
 * available as direct properties; e.g., [strokeColor] and [cellIndices].
 *
 * A HexGridView's Grid is handled similarly to ListView's and
 * RecyclerView's data sets: if the user modifies the Grid, the HexGridView
 * needs to be notified to update itself. The only thing needed is a call to
 * [invalidate], so that's left to the user, rather than having a pointless
 * alias function that does nothing else. That is, if you change a State in the
 * Grid, you need to call invalidate() on the HexGridView afterward.
 */
class HexGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    /**
     * Callback for click events.
     */
    fun interface OnClickListener {

        /**
         * Called with the [Grid.Address] when a click hits successfully.
         */
        fun onGridClick(address: Grid.Address)

        /**
         * Called when the [HexGridView] is clicked outside of the grid.
         */
        fun onOutsideClick() {}
    }

    /**
     * Interface through which to provide Views to be placed in each cell.
     */
    fun interface ViewProvider {

        /**
         * Called for each [Grid.Address] during layout.
         *
         * Returning a non-null instance will cause that View to be added to the
         * group, and measured and laid out to the cell's inset bounds during
         * layout. Returning null will remove any View that might already be set
         * for the given Address.
         *
         * If a ViewProvider is set, it replaces any and everything that
         * might have been set or added manually or through layout XML.
         */
        fun getView(address: Grid.Address, current: View?): View?
    }

    private val views = mutableMapOf<Grid.Address, View?>()

    private val gridUi = GridUi()

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.HexGridView)) {
            registerDebugData(context, attrs, this)

            if (hasValue(R.styleable.HexGridView_rowCount) &&
                hasValue(R.styleable.HexGridView_columnCount)
            ) {
                val grid = MutableGrid(
                    getInt(R.styleable.HexGridView_rowCount, 0),
                    getInt(R.styleable.HexGridView_columnCount, 0),
                    getBoolean(R.styleable.HexGridView_insetEvenLines, false),
                    getBoolean(R.styleable.HexGridView_enableEdgeLines, false)
                )
                gridUi.apply {
                    this.grid = grid
                    layoutSpecs = LayoutSpecs(
                        FitMode.entries[
                            getInt(R.styleable.HexGridView_fitMode, 0)
                        ],
                        CrossMode.entries[
                            getInt(R.styleable.HexGridView_crossMode, 0)
                        ],
                        HexOrientation.entries[
                            getInt(R.styleable.HexGridView_hexOrientation, 0)
                        ],
                        getDimension(
                            R.styleable.HexGridView_strokeWidth,
                            0F
                        )
                    )
                    strokeColor = getColor(
                        R.styleable.HexGridView_strokeColor,
                        Color.BLACK
                    )
                    fillColor = getColor(
                        R.styleable.HexGridView_fillColor,
                        Color.TRANSPARENT
                    )
                    selectColor = getColor(
                        R.styleable.HexGridView_selectColor,
                        Color.GRAY
                    )
                    indexColor = getColor(
                        R.styleable.HexGridView_indexColor,
                        strokeColor
                    )
                    cellIndices =
                        when (getInt(R.styleable.HexGridView_cellIndices, 0)) {
                            1 -> Lines.Both
                            2 -> Lines.Rows
                            3 -> Lines.Columns
                            else -> Lines.None
                        }
                }
            }

            recycle()
        }

        setWillNotDraw(false)
        isClickable = true
    }

    /**
     * The HexGridView's current [Grid], which defaults to an empty Grid.
     *
     * If the View has been inflated from an XML element specifying both
     * `app:rowCount` and `app:columnCount`, the Grid will be
     * initialized with the appropriate attribute values.
     *
     * Setting a new Grid instance with the same shape as the current one will
     * only update [Grid.State]s and invalidate the View. If a Grid with a
     * different shape is set, the child Views will be reset and laid out
     * again.
     */
    var grid: Grid = gridUi.grid
        set(value) {
            if (field == value) return
            if (field.isDifferentShape(value)) {
                removeAllViewsInLayout()
                views.clear()
                requestLayout()
            }
            field = value
            gridUi.grid = value
            invalidate()
        }

    /**
     * The HexGridView's current [FitMode].
     */
    var fitMode: FitMode by changeSpecs(gridUi.layoutSpecs.fitMode) { specs, value ->
        specs.copy(fitMode = value)
    }

    /**
     * The HexGridView's current [CrossMode].
     */
    var crossMode: CrossMode by changeSpecs(gridUi.layoutSpecs.crossMode) { specs, value ->
        specs.copy(crossMode = value)
    }

    /**
     * The HexGridView's current [HexOrientation].
     */
    var hexOrientation: HexOrientation by changeSpecs(gridUi.layoutSpecs.hexOrientation) { specs, value ->
        specs.copy(hexOrientation = value)
    }

    /**
     * The HexGridView's current stroke width.
     */
    var strokeWidth: Float by changeSpecs(gridUi.layoutSpecs.strokeWidth) { specs, value ->
        specs.copy(strokeWidth = value)
    }

    /**
     * Color of the cells' outlines.
     */
    @get:ColorInt
    @setparam:ColorInt
    var strokeColor: Int by invalidating(gridUi::strokeColor)

    /**
     * Color of the cells' interior normally.
     */
    @get:ColorInt
    @setparam:ColorInt
    var fillColor: Int by invalidating(gridUi::fillColor)

    /**
     * Color of the cells' interior when its selected state is true.
     */
    @get:ColorInt
    @setparam:ColorInt
    var selectColor: Int by invalidating(gridUi::selectColor)

    /**
     * Color of the cells' indices, if shown.
     */
    @get:ColorInt
    @setparam:ColorInt
    var indexColor: Int by invalidating(gridUi::indexColor)

    /**
     * Design/debug option to show the cells' row and/or column.
     */
    var cellIndices: Lines by invalidating(gridUi::cellIndices)

    /**
     * The HexGridView's current [OnClickListener].
     */
    var onClickListener: OnClickListener? = null

    /**
     * The HexGridView's current [ViewProvider].
     */
    var viewProvider: ViewProvider? = null
        set(value) {
            if (field == value) return
            field = value
            notifyViewsInvalidated()
        }

    /**
     * A [Map] of the current child [View]s.
     */
    val cellViews: Map<Grid.Address, View?> = views

    /**
     * Modifies the passed View's [LayoutParams] to cause a [HexagonDrawable]
     * background to be added or updated during layout. If [color] is
     * [Color.TRANSPARENT], the background is removed.
     *
     * See [removeHexBackground].
     */
    fun applyHexBackground(
        view: View,
        @ColorInt color: Int,
        inset: Float
    ) {
        val current = view.layoutParams
        val next = if (current is LayoutParams) {
            if (current.hexBackgroundColor == color &&
                current.hexBackgroundInset == inset
            ) return
            current
        } else {
            generateDefaultLayoutParams()
        }
        next.hexBackgroundColor = color
        next.hexBackgroundInset = inset
        view.layoutParams = next
    }

    /**
     * Modifies the passed View's [LayoutParams] to remove any
     * [HexagonDrawable] background that may be present.
     *
     * See [applyHexBackground].
     */
    fun removeHexBackground(view: View) {
        val params = view.layoutParams as? LayoutParams ?: return
        params.hexBackgroundColor = Color.TRANSPARENT
        view.layoutParams = params
    }

    /**
     * Causes the HexGridView to re-layout and redraw.
     *
     * This is used similarly to the notify methods in ListView's and
     * RecyclerView's Adapters, to refresh the cell Views obtained from the
     * [ViewProvider].
     */
    fun notifyViewsInvalidated() {
        requestLayout()
        invalidate()
    }

    override fun addView(
        child: View?,
        index: Int,
        params: ViewGroup.LayoutParams?
    ) {
        if (child == null) {
            // Let ViewGroup throw
            super.addView(null, index, params)
            return
        }
        if (params !is LayoutParams) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Child must have HexGridView.LayoutParams")
            }
            return
        }
        val address = params.address
        if (!grid.isValidAddress(address.row, address.column)) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Child LayoutParams must have a valid Grid.Address")
            }
            return
        }
        applyLayoutParamsState(params)
        if (child !is CellStateView) {
            if (setCellView(address, child, params)) notifyViewsInvalidated()
        }
    }

    private fun applyLayoutParamsState(params: LayoutParams) {
        val grid = grid as? MutableGrid ?: return
        val address = params.address
        if (grid.isValidAddress(address.row, address.column)) {
            grid[address] = params.state
        }
    }

    private fun setCellView(
        address: Grid.Address,
        view: View?,
        params: LayoutParams?
    ): Boolean {
        val current = views[address]
        if (current == view) return false
        current?.let { removeViewInLayout(it) }
        views[address] = view
        if (view != null && params != null) {
            params.address = address
            addViewInLayout(view, -1, params, true)
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val uiSize = gridUi.calculateSize(
            widthMode != MeasureSpec.UNSPECIFIED,
            heightMode != MeasureSpec.UNSPECIFIED,
            widthMode == MeasureSpec.EXACTLY,
            heightMode == MeasureSpec.EXACTLY,
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec),
            paddingLeft,
            paddingTop,
            paddingRight,
            paddingBottom
        ).also { size = it }
        setMeasuredDimension(uiSize.width, uiSize.height)
    }

    var size = Size(0, 0)
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        views.values.removeAll { view -> view == null || view.parent != this }
        viewProvider?.let { provider ->
            grid.forEach { address, state ->
                val next = provider.getView(address, views[address])
                val nextParams = next?.layoutParams
                val params = when {
                    next == null -> null
                    nextParams is LayoutParams -> nextParams
                    else -> generateDefaultLayoutParams().also { params ->
                        params.address = address
                        params.state = state
                    }
                }
                setCellView(address, next, params)
            }
        }
        views.values.forEach { view ->
            val params = view?.layoutParams as? LayoutParams ?: return@forEach
            if (params.hexBackgroundColor != Color.TRANSPARENT) {
                val background = view.background as? HexagonDrawable
                    ?: generateHexagonDrawable().also { view.background = it }
                background.fillColor = params.hexBackgroundColor
            } else {
                view.background = null
            }
        }
        children.forEach { child ->
            val params = child.layoutParams as? LayoutParams ?: return@forEach
            val bounds = tmpBounds
            val inset = when (params.hexBackgroundColor) {
                Color.TRANSPARENT -> 0F
                else -> params.hexBackgroundInset
            }
            gridUi.getCellItemBounds(params.address, inset, bounds)
            val childWidth = childMeasureSpec(params.width, bounds.width())
            val childHeight = childMeasureSpec(params.height, bounds.height())
            child.measure(childWidth, childHeight)
            val left = bounds.left + (bounds.width() - child.measuredWidth) / 2
            val top = bounds.top + (bounds.height() - child.measuredHeight) / 2
            val right = left + child.measuredWidth
            val bottom = top + child.measuredHeight
            child.layout(left, top, right, bottom)
        }
    }

    private fun generateHexagonDrawable() = HexagonDrawable(this)

    internal fun getHexagonPath(outPath: Path, bounds: Rect) {
        gridUi.getHexagonPath(outPath, bounds)
    }

    // Stripped-down ViewGroup.getChildMeasureSpec()
    private fun childMeasureSpec(child: Int, max: Int): Int {
        var resultSize = 0
        var resultMode = 0
        if (child >= max || child == ViewGroup.LayoutParams.MATCH_PARENT) {
            resultSize = max
            resultMode = MeasureSpec.EXACTLY
        } else if (child >= 0) {
            resultSize = child
            resultMode = MeasureSpec.EXACTLY
        } else if (child == ViewGroup.LayoutParams.WRAP_CONTENT) {
            resultSize = max
            resultMode = MeasureSpec.AT_MOST
        }
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode)
    }

    override fun onDraw(canvas: Canvas) {
        when {
            clipToPadding -> canvas.withClip(
                paddingLeft,
                paddingTop,
                width - paddingRight,
                height - paddingBottom
            ) { gridUi.drawGrid(canvas) }

            else -> gridUi.drawGrid(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // TODO: Proper touch handling.
        if (event.action == MotionEvent.ACTION_UP) {
            val address = gridUi.resolveAddress(event.x, event.y)
            if (address != null) {
                onClickListener?.onGridClick(address)
            } else {
                onClickListener?.onOutsideClick()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun generateDefaultLayoutParams() = LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    override fun generateLayoutParams(attrs: AttributeSet?) =
        LayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?) =
        LayoutParams(p)

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?) =
        p is LayoutParams

    class LayoutParams : ViewGroup.LayoutParams {

        internal var address = Grid.Address(-2, -2)
        internal var state = Grid.State.Default
        internal var hexBackgroundColor: Int = Color.WHITE
        internal var hexBackgroundInset: Float = 0.0F

        constructor(
            context: Context,
            attrs: AttributeSet?
        ) : super(context, attrs) {
            val array = context.obtainStyledAttributes(
                attrs,
                R.styleable.HexGridView_Layout
            )
            parseRowAndColumn(
                array.getString(
                    R.styleable.HexGridView_Layout_layout_rowAndColumn
                )
            )
            state = Grid.State(
                isVisible = array.getBoolean(
                    R.styleable.HexGridView_Layout_layout_cellIsVisible,
                    state.isVisible
                ),
                isSelected = array.getBoolean(
                    R.styleable.HexGridView_Layout_layout_cellIsSelected,
                    state.isSelected
                )
            )
            hexBackgroundColor = array.getColor(
                R.styleable.HexGridView_Layout_layout_hexBackgroundColor,
                hexBackgroundColor
            )
            hexBackgroundInset = array.getDimension(
                R.styleable.HexGridView_Layout_layout_hexBackgroundInset,
                hexBackgroundInset
            )
            array.recycle()
        }

        private fun parseRowAndColumn(rc: String?) {
            if (rc == null || !rc.contains(",")) return
            rc.split(",").also { pair ->
                if (pair.size != 2) return
                address = Grid.Address(
                    row = pair[0].toIntOrNull() ?: return,
                    column = pair[1].toIntOrNull() ?: return
                )
            }
        }

        constructor(source: LayoutParams) : super(source) {
            this.address = source.address
            this.state = source.state
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams) : super(source)
        constructor(source: MarginLayoutParams) : super(source)
    }

    private fun <T> changeSpecs(
        initialValue: T,
        createSpecs: (current: LayoutSpecs, newValue: T) -> LayoutSpecs
    ) = onChange(initialValue) { newValue ->
        gridUi.layoutSpecs = createSpecs(gridUi.layoutSpecs, newValue)
        requestLayout()
        invalidate()
    }

    private fun <T> invalidating(wrapped: KMutableProperty0<T>) =
        relayChange(wrapped, ::invalidate)

    private fun registerDebugData(
        context: Context,
        attrs: AttributeSet?,
        array: TypedArray
    ) {
        if (Build.VERSION.SDK_INT >= 29) {
            saveAttributeDataForStyleable(
                context,
                R.styleable.HexGridView,
                attrs,
                array,
                0,
                0
            )
        }
    }

    private val tmpBounds = Rect()
}

private fun Grid.isDifferentShape(other: Grid): Boolean =
    rowCount != other.rowCount ||
            columnCount != other.columnCount ||
            insetEvenLines != other.insetEvenLines ||
            enableEdgeLines != other.enableEdgeLines

private const val TAG = "HexGridView"