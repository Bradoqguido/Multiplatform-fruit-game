package com.fruitpuzzle.game.model

/**
 * Represents a single slot in the 7-slot rack at the top of the game screen.
 *
 * @param index     Position in the rack (0..6).
 * @param fruitType The fruit occupying this slot, or null if empty.
 */
data class RackSlot(
  val index: Int,
  val fruitType: FruitType? = null
) {
  /** Whether this slot is currently empty. */
  val isEmpty: Boolean
    get() = fruitType == null
}
