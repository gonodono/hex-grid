package com.gonodono.hexgrid.demo.library

import com.gonodono.hexgrid.demo.R
import com.gonodono.hexgrid.demo.internal.BaseActivity

class LibraryActivity : BaseActivity(
    LibraryPages,
    R.string.welcome_library_header,
    R.string.welcome_library
)

private val LibraryPages = listOf(
    Pair(LayoutFragment::class.java, "Layout"),
    Pair(OptionsFragment::class.java, "Options")
)