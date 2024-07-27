package com.gonodono.hexgrid.view.internal

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

    override fun getValue(thisRef: Any?, property: KProperty<*>) = wrapped.get()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (wrapped.get() == value) return
        wrapped.set(value)
        onChange()
    }
}