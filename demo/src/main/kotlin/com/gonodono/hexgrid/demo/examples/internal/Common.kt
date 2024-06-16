package com.gonodono.hexgrid.demo.examples.internal

import android.view.View

internal const val MARGIN_DP = 5

internal inline fun View.doOnSizeChanges(crossinline action: () -> Unit) {
    addOnLayoutChangeListener { _, l, t, r, b, ol, ot, or, ob ->
        val w = r - l
        val h = b - t
        val ow = or - ol
        val oh = ob - ot
        if (w != ow || h != oh) action()
    }
}