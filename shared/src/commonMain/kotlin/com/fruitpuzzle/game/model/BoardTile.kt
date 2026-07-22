package com.fruitpuzzle.game.model

/**
 * A single tile on the game board.
 *
 * @param id         Unique identifier for this tile instance.
 * @param fruitType  The type of fruit/gem displayed.
 * @param gridX      Column position on the board grid (0-based).
 * @param gridY      Row position on the board grid (0-based).
 * @param layer      Overlap layer: 0 = bottom, higher = on top of others.
 * @param isVisible  Whether the tile is still on the board (false after selection).
 */
data class BoardTile(
  val id: Int,
  val fruitType: FruitType,
  val gridX: Int,
  val gridY: Int,
  val layer: Int,
  val isVisible: Boolean = true
) {
  /**
   * A tile is clickable when it is visible and no other visible tile
   * occupies the same grid cell at a higher layer.
   * This check is performed externally by [GameRepository] using the full board list.
   */
  val isOnTopLayer: Boolean
    get() = isVisible
}
