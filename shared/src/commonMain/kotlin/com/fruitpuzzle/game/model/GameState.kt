package com.fruitpuzzle.game.model

/**
 * Snapshot of a single move for the undo feature.
 */
data class MoveRecord(
  val tileId: Int,
  val boardState: List<BoardTile>,
  val rackState: List<RackSlot>,
  val destroyingIndices: List<Int>
)

/**
 * Immutable snapshot of the entire game state.
 * Drives all UI rendering via StateFlow.
 */
data class GameState(
  val currentLevel: Int = 1,
  val lives: Int = 3,
  val undoCount: Int = 3,
  val moveHistory: List<MoveRecord> = emptyList(),
  val board: List<BoardTile> = emptyList(),
  val rack: List<RackSlot> = List(RACK_SIZE) { RackSlot(index = it) },
  val phase: GamePhase = GamePhase.IDLE,
  val flyingTiles: List<FlyingTile> = emptyList(),
  val destroyingIndices: List<Int> = emptyList(),
  val clickableTileIds: Set<Int> = emptySet()
) {
  companion object {
    /** Fixed rack capacity — game is lost when all 7 slots fill without a match. */
    const val RACK_SIZE: Int = 7
    /** Max undo moves allowed per level. */
    const val MAX_UNDOS: Int = 3
  }

  /** Number of occupied slots in the rack. */
  val rackOccupiedCount: Int
    get() = rack.count { !it.isEmpty }

  /** Whether the board has any visible tiles remaining. */
  val boardHasTiles: Boolean
    get() = board.any { it.isVisible }

  /** Whether the game is won (board empty + rack empty + no flying tiles). */
  val isWon: Boolean
    get() = !boardHasTiles && rackOccupiedCount == 0 && flyingTiles.isEmpty()

  /** Whether the rack is full (all 7 slots occupied). */
  val isRackFull: Boolean
    get() = rackOccupiedCount == RACK_SIZE

  /** Whether the player can perform an undo. */
  val canUndo: Boolean
    get() = undoCount > 0 && moveHistory.isNotEmpty() && phase == GamePhase.PLAYING
}

/**
 * Finite state machine for the game flow.
 */
enum class GamePhase {
  /** Game not started or between levels. */
  IDLE,
  /** Player can interact with the board. */
  PLAYING,
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
 * @param id             Unique flight identifier.
 * @param fruitType      The fruit/gem type.
 * @param fromX          Absolute X position (positionInRoot) of the tile on the board.
 * @param fromY          Absolute Y position (positionInRoot) of the tile on the board.
 * @param toX            Absolute X position of the target rack slot.
 * @param toY            Absolute Y position of the target rack slot.
 * @param targetSlotIndex Which rack slot this tile will land in.
 */
data class FlyingTile(
  val id: String,
  val fruitType: FruitType,
  val fromX: Float,
  val fromY: Float,
  val toX: Float,
  val toY: Float,
  val targetSlotIndex: Int
)

