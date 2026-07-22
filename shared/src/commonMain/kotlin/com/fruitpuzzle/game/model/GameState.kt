package com.fruitpuzzle.game.model

/**
 * Immutable snapshot of the entire game state.
 * Drives all UI rendering via StateFlow.
 */
data class GameState(
  val currentLevel: Int = 1,
  val lives: Int = 3,
  val board: List<BoardTile> = emptyList(),
  val rack: List<RackSlot> = List(RACK_SIZE) { RackSlot(index = it) },
  val phase: GamePhase = GamePhase.IDLE,
  val flyingTile: FlyingTile? = null,
  val destroyingIndices: List<Int> = emptyList(),
  val clickableTileIds: Set<Int> = emptySet()
) {
  companion object {
    /** Fixed rack capacity — game is lost when all 7 slots fill without a match. */
    const val RACK_SIZE: Int = 7
  }

  /** Number of occupied slots in the rack. */
  val rackOccupiedCount: Int
    get() = rack.count { !it.isEmpty }

  /** Whether the board has any visible tiles remaining. */
  val boardHasTiles: Boolean
    get() = board.any { it.isVisible }

  /** Whether the game is won (board empty + rack empty). */
  val isWon: Boolean
    get() = !boardHasTiles && rackOccupiedCount == 0

  /** Whether the rack is full (all 7 slots occupied). */
  val isRackFull: Boolean
    get() = rackOccupiedCount == RACK_SIZE
}

/**
 * Finite state machine for the game flow.
 */
enum class GamePhase {
  /** Game not started or between levels. */
  IDLE,
  /** Player can interact with the board. */
  PLAYING,
  /** A tile is flying from the board to the rack. */
  ANIMATING_FLY,
  /** 3 matched tiles are being destroyed in the rack. */
  ANIMATING_DESTROY,
  /** Player cleared the board and rack. */
  WIN,
  /** Rack is full with no match — player must retry. */
  LOSS,
  /** Player has no lives left — punished back a level. */
  GAME_OVER
}

/**
 * Describes a tile currently animating from the board to the rack.
 * Used by the Flying Overlay composable.
 *
 * @param tile           The board tile being animated.
 * @param fromX          Absolute X position (positionInRoot) of the tile on the board.
 * @param fromY          Absolute Y position (positionInRoot) of the tile on the board.
 * @param toX            Absolute X position of the target rack slot.
 * @param toY            Absolute Y position of the target rack slot.
 * @param targetSlotIndex Which rack slot this tile will land in.
 */
data class FlyingTile(
  val tile: BoardTile,
  val fromX: Float,
  val fromY: Float,
  val toX: Float,
  val toY: Float,
  val targetSlotIndex: Int
)
