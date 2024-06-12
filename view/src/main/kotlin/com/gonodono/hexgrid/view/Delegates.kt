package com.gonodono.hexgrid.view

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

internal fun <T> onChange(
    initialValue: T,
    onChange: (newValue: T) -> Unit
): ReadWriteProperty<Any?, T> = OnChangeProperty(initialValue, onChange)

private class OnChangeProperty<T>(
    initialValue: T,
    private val onChange: (newValue: T) -> Unit
) : ReadWriteProperty<Any?, T> {

    private var value: T = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>) = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this.value == value) return
        this.value = value
        onChange(value)
    }
}

internal fun <T> relayChange(
    wrapped: KMutableProperty0<T>,
    onChange: () -> Unit
): ReadWriteProperty<Any?, T> = RelayChangeProperty(wrapped, onChange)

private class RelayChangeProperty<T>(
    private val wrapped: KMutableProperty0<T>,
    private val onChange: () -> Unit
) : ReadWriteProperty<Any?, T> {

    private fun getWrapped() = wrapped.get()

    override fun getValue(thisRef: Any?, property: KProperty<*>) = getWrapped()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (getWrapped() == value) return
        wrapped.set(value)
        onChange()
    }
}