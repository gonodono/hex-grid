package com.gonodono.hexgrid.data

internal class GridIterator<T>(
    private val mainIterator: Iterator<Iterable<T>>
) : Iterator<T> {

    private var minorIterator = nextIterator()

    private fun nextIterator(): Iterator<T>? {
        while (mainIterator.hasNext()) {
            val nextIterator = mainIterator.next().iterator()
            if (nextIterator.hasNext()) return nextIterator
        }
        return null
    }

    override fun hasNext() = minorIterator?.hasNext() == true

    override fun next(): T {
        val iterator = minorIterator ?: error("Iterator empty")
        val nextValue = iterator.next()
        if (!iterator.hasNext()) minorIterator = nextIterator()
        return nextValue
    }
}