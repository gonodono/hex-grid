package com.gonodono.hexgrid.demo.examples.internal

import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View

const val MARGIN_DP = 5

inline fun View.doOnSizeChanges(crossinline action: () -> Unit) {
    addOnLayoutChangeListener { _, l, t, r, b, ol, ot, or, ob ->
        val w = r - l
        val h = b - t
        val ow = or - ol
        val oh = ob - ot
        if (w != ow || h != oh) action()
    }
}

fun obtainAttributes(
    r: Resources,
    attrs: AttributeSet?,
    theme: Resources.Theme?,
    styleable: IntArray
): TypedArray = theme?.obtainStyledAttributes(attrs, styleable, 0, 0)
    ?: r.obtainAttributes(attrs, styleable)