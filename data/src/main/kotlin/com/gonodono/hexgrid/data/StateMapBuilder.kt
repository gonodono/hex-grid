package com.gonodono.hexgrid.data

/**
 * Provides a scope in which to build a Map<Grid.Address, Grid.State> using
 * convenience functions.
 *
 * The command functions in [StateMapBuilderScope] are cooperative; i.e., you
 * can both [select][StateMapBuilderScope.select] and
 * [hide][StateMapBuilderScope.hide] the same [Grid.Address].
 */
fun buildStateMap(
    block: StateMapBuilderScope.() -> Unit
): Map<Grid.Address, Grid.State> = StateMapBuilderScopeImpl().apply(block).map

/**
 * Scope for assembling State maps. For use with [buildStateMap].
 *
 * The command functions in this scope are cooperative; i.e., you can both
 * [select] and [hide] the same [Grid.Address]. These commands operate on a
 * shared [Grid.State] for each Address that implicitly starts at
 * [Grid.State.Default].
 */
sealed interface StateMapBuilderScope {

    /**
     * Sets the [Grid.State] at each of the [addresses] to a copy of itself with
     * [isSelected][Grid.State.isSelected] set to `true`.
     */
    fun select(vararg addresses: Grid.Address)

    /**
     * Sets the [Grid.State] at each of the [addresses] to a copy of itself with
     * [isVisible][Grid.State.isVisible] set to `false`.
     */
    fun hide(vararg addresses: Grid.Address)

    /**
     * Creates a [Grid.Address] from [row] and [column].
     */
    fun at(row: Int, column: Int): Grid.Address = Grid.Address(row, column)
}

private class StateMapBuilderScopeImpl : StateMapBuilderScope {

    val map = mutableMapOf<Grid.Address, Grid.State>()

    override fun select(vararg addresses: Grid.Address) {
        addresses.forEach { address ->
            val state = map[address] ?: Grid.State.Default
            map += address to state.copy(isSelected = true)
        }
    }

    override fun hide(vararg addresses: Grid.Address) {
        addresses.forEach { address ->
            val state = map[address] ?: Grid.State.Default
            map += address to state.copy(isVisible = false)
        }
    }
}