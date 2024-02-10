package com.gonodono.hexgrid.data


/**
 * Convenience to concisely check an Address against individual indices.
 */
fun Grid.Address.isAt(row: Int, column: Int): Boolean =
    this.row == row && this.column == column

/**
 * Updates the [Grid.State] at the given [Grid.Address].
 *
 * Invalid Addresses will result in Exceptions. See [Grid.isValidAddress].
 */
fun MutableGrid.change(
    address: Grid.Address,
    isVisible: Boolean = this[address].isVisible,
    isSelected: Boolean = this[address].isSelected
) {
    this[address] = Grid.State(isVisible, isSelected)
}

/**
 * Toggles the [Grid.State.isSelected] value at the given [Grid.Address].
 *
 * Invalid Addresses will result in Exceptions. See [Grid.isValidAddress].
 */
fun MutableGrid.toggle(address: Grid.Address) {
    change(address, isSelected = !this[address].isSelected)
}

/**
 * Returns a modified copy of the [Grid] with the specified [Grid.State] update
 * at the given [Grid.Address].
 *
 * If the State does not change, the same Grid instance is returned.
 *
 * Invalid Addresses will result in Exceptions. See [Grid.isValidAddress].
 */
inline fun <reified T : Grid> T.changed(
    address: Grid.Address,
    isVisible: Boolean = this[address].isVisible,
    isSelected: Boolean = this[address].isSelected
): T = copy(address, Grid.State(isVisible, isSelected)) as T

/**
 * Returns a modified copy of the [Grid] with the [Grid.State.isSelected] value
 * toggled at the given [Grid.Address].
 *
 * Invalid Addresses will result in Exceptions. See [Grid.isValidAddress].
 */
inline fun <reified T : Grid> T.toggled(address: Grid.Address): T =
    changed(address, isSelected = !this[address].isSelected)