package com.gonodono.hexgrid.data

inline val Int.smallHalf: Int
    get() = this / 2

inline val Int.largeHalf: Int
    get() = if (isEven) smallHalf else smallHalf + 1

inline val Int.isEven: Boolean
    get() = this % 2 == 0

fun Grid.checkAddress(row: Int, column: Int) {
    check(isValidAddress(row, column)) { "Invalid address: ($row, $column)" }
}

fun Grid.isValidLine(
    line: Int,
    cross: Int,
    totalCount: Int
): Boolean {
    val isCrossInset = isLineInset(cross)
    val crossStart = crossStartIndex(isCrossInset)
    val offset = line - crossStart
    if (offset % 2 != 0 || offset < 0) return false
    val index = offset / 2
    return index < crossCount(isCrossInset, totalCount)
}

fun Grid.isLineInset(index: Int): Boolean = index.isEven == insetEvenLines

fun Grid.crossStartIndex(isInset: Boolean) = when {
    isInset -> if (enableEdgeLines) -1 else 1
    else -> 0
}

fun Grid.crossCount(
    isInset: Boolean,
    totalCount: Int
) = if (isInset == enableEdgeLines) {
    totalCount.largeHalf
} else {
    totalCount.smallHalf
}