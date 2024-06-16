package com.gonodono.hexgrid.demo.examples

import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.examples.grid.GridFragment
import com.gonodono.hexgrid.demo.examples.shield.ShieldFragment
import com.gonodono.hexgrid.demo.internal.BaseActivity

class ExamplesActivity : BaseActivity(
    ExamplesPages,
    R.string.welcome_examples_header,
    R.string.welcome_examples
)

private val ExamplesPages = listOf(
    Pair(ShieldFragment::class.java, "Shield"),
    Pair(GridFragment::class.java, "Grid")
)