package com.fruitpuzzle.game.logic

import com.fruitpuzzle.game.model.BoardTile
import com.fruitpuzzle.game.model.FlyingTile
import com.fruitpuzzle.game.model.FruitType
import com.fruitpuzzle.game.model.GamePhase
import com.fruitpuzzle.game.model.GameState
import com.fruitpuzzle.game.model.RackSlot
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Central game state manager. Owns the single source of truth [GameState]
 * and exposes it as an immutable [StateFlow].
 *
 * Handles:
 * - Level loading / generation
 * - Tile selection → rack insertion → grouping → matching
 * - Win / Loss / Game Over transitions
 * - Persistence of currentLevel and lives via [Settings]
 */
class GameRepository(private val settings: Settings) {

  private val _state = MutableStateFlow(GameState())
  val state: StateFlow<GameState> = _state.asStateFlow()

  companion object {
    private const val KEY_CURRENT_LEVEL = "currentLevel"
    private const val KEY_LIVES = "livesCounter"
    private const val DEFAULT_LIVES = 3
  }

  // ──────────────────────────────────────────
  // Persistence
  // ──────────────────────────────────────────

  /**
   * Loads saved progress from local storage and initializes the game state.
   * Call once on app launch.
   */
  fun loadSavedProgress() {
    val savedLevel = settings.getInt(KEY_CURRENT_LEVEL, 1)
    val savedLives = settings.getInt(KEY_LIVES, DEFAULT_LIVES)
    _state.update { it.copy(currentLevel = savedLevel, lives = savedLives) }
  }

  private fun saveProgress() {
    val current = _state.value
    settings.putInt(KEY_CURRENT_LEVEL, current.currentLevel)
    settings.putInt(KEY_LIVES, current.lives)
  }

  // ──────────────────────────────────────────
  // Level Management
  // ──────────────────────────────────────────

  /**
   * Generates and starts a new level.
   * Resets the rack and generates a fresh board using [LevelGenerator].
   */
  fun startLevel(level: Int) {
    val board = LevelGenerator.generate(level)
    val clickable = LevelGenerator.calculateClickableTiles(board)

    _state.update {
      it.copy(
        currentLevel = level,
        board = board,
        rack = List(GameState.RACK_SIZE) { idx -> RackSlot(index = idx) },
        phase = GamePhase.PLAYING,
        flyingTile = null,
        destroyingIndices = emptyList(),
        clickableTileIds = clickable
      )
    }
    saveProgress()
  }

  /**
   * Starts the current level (reloads from state).
   */
  fun startCurrentLevel() {
    startLevel(_state.value.currentLevel)
  }

  // ──────────────────────────────────────────
  // Tile Selection
  // ──────────────────────────────────────────

  /**
   * Called when the player taps a tile on the board.
   * Removes the tile from the board and prepares the fly animation.
   *
   * @param tileId  The ID of the tapped tile.
   * @param fromX   Absolute screen X of the tile (positionInRoot).
   * @param fromY   Absolute screen Y of the tile (positionInRoot).
   * @param toX     Absolute screen X of the target rack slot.
   * @param toY     Absolute screen Y of the target rack slot.
   */
  fun selectTile(tileId: Int, fromX: Float, fromY: Float, toX: Float, toY: Float) {
    val current = _state.value
    if (current.phase != GamePhase.PLAYING) return

    val tile = current.board.find { it.id == tileId && it.isVisible } ?: return
    if (tileId !in current.clickableTileIds) return

    // Find the target slot index (where this fruit will land)
    val targetSlotIndex = findInsertionIndex(current.rack, tile.fruitType)

    // Remove tile from board (mark invisible)
    val updatedBoard = current.board.map { t ->
      if (t.id == tileId) t.copy(isVisible = false) else t
    }
    val updatedClickable = LevelGenerator.calculateClickableTiles(updatedBoard)

    _state.update {
      it.copy(
        board = updatedBoard,
        clickableTileIds = updatedClickable,
        phase = GamePhase.ANIMATING_FLY,
        flyingTile = FlyingTile(
          tile = tile,
          fromX = fromX,
          fromY = fromY,
          toX = toX,
          toY = toY,
          targetSlotIndex = targetSlotIndex
        )
      )
    }
  }

  /**
   * Called when the fly animation completes.
   * Inserts the fruit into the rack with grouping logic, then checks for matches.
   */
  fun onFlyComplete() {
    val current = _state.value
    val flying = current.flyingTile ?: return

    // Insert fruit into rack with grouping
    val newRack = insertIntoRack(current.rack, flying.tile.fruitType)

    // Check for 3-match
    val matchIndices = findMatchIndices(newRack)

    if (matchIndices.isNotEmpty()) {
      // Trigger destruction animation
      _state.update {
        it.copy(
          rack = newRack,
          flyingTile = null,
          phase = GamePhase.ANIMATING_DESTROY,
          destroyingIndices = matchIndices
        )
      }
    } else {
      // No match — check if rack is full (loss)
      val occupiedCount = newRack.count { it.fruitType != null }
      val newPhase = if (occupiedCount >= GameState.RACK_SIZE) {
        GamePhase.LOSS
      } else {
        GamePhase.PLAYING
      }

      _state.update {
        it.copy(
          rack = newRack,
          flyingTile = null,
          phase = newPhase,
          destroyingIndices = emptyList()
        )
      }
    }
  }

  /**
   * Called when the destruction animation completes.
   * Removes the matched 3 from the rack, shifts remaining left, checks for win.
   */
  fun onDestroyComplete() {
    val current = _state.value
    val indices = current.destroyingIndices.toSet()

    // Remove destroyed slots and shift left
    val remaining = current.rack
      .filter { it.index !in indices }
      .mapIndexed { idx, slot -> slot.copy(index = idx) }

    // Pad with empty slots to maintain RACK_SIZE
    val newRack = remaining + List(GameState.RACK_SIZE - remaining.size) { idx ->
      RackSlot(index = remaining.size + idx)
    }

    // Check win condition: board empty AND rack empty
    val boardEmpty = !current.board.any { it.isVisible }
    val rackEmpty = newRack.all { it.fruitType == null }

    val newPhase = when {
      boardEmpty && rackEmpty -> GamePhase.WIN
      else -> GamePhase.PLAYING
    }

    _state.update {
      it.copy(
        rack = newRack,
        phase = newPhase,
        destroyingIndices = emptyList()
      )
    }
  }

  // ──────────────────────────────────────────
  // Win / Loss / Retry
  // ──────────────────────────────────────────

  /**
   * Advances to the next level after a win.
   */
  fun advanceLevel() {
    val nextLevel = _state.value.currentLevel + 1
    _state.update { it.copy(currentLevel = nextLevel) }
    saveProgress()
    startLevel(nextLevel)
  }

  /**
   * Retries the current level after a loss.
   * Costs 1 life. If lives reach 0, refills to 3 and drops back 1 level.
   */
  fun retryLevel() {
    val current = _state.value
    val newLives = current.lives - 1

    if (newLives <= 0) {
      // Punishment: refill lives, drop back 1 level (min 1)
      val dropLevel = maxOf(1, current.currentLevel - 1)
      _state.update {
        it.copy(
          lives = DEFAULT_LIVES,
          currentLevel = dropLevel,
          phase = GamePhase.GAME_OVER
        )
      }
      saveProgress()
    } else {
      _state.update {
        it.copy(
          lives = newLives,
          phase = GamePhase.IDLE
        )
      }
      saveProgress()
      startLevel(current.currentLevel)
    }
  }

  /**
   * Continues after a GAME_OVER (lives depleted) — starts the dropped level.
   */
  fun continueAfterGameOver() {
    startLevel(_state.value.currentLevel)
  }

  // ──────────────────────────────────────────
  // Rack Logic (Grouping + Matching)
  // ──────────────────────────────────────────

  /**
   * Finds the insertion index for a fruit in the rack.
   * Groups identical fruits together by inserting next to existing matches.
   */
  private fun findInsertionIndex(rack: List<RackSlot>, fruitType: FruitType): Int {
    // Find the last slot with the same fruit type
    val lastMatchIndex = rack.indexOfLast { it.fruitType == fruitType }

    return if (lastMatchIndex >= 0) {
      // Insert right after the last matching fruit
      lastMatchIndex + 1
    } else {
      // No matching fruit — insert at the first empty slot
      rack.indexOfFirst { it.isEmpty }.takeIf { it >= 0 } ?: (GameState.RACK_SIZE - 1)
    }
  }

  /**
   * Inserts a fruit into the rack at the correct grouped position.
   * Shifts other fruits to the right as needed.
   */
  internal fun insertIntoRack(rack: List<RackSlot>, fruitType: FruitType): List<RackSlot> {
    // Extract occupied fruits in order
    val fruits = rack.mapNotNull { it.fruitType }.toMutableList()

    // Find insertion point: after the last occurrence of this fruit type
    val lastMatchIndex = fruits.indexOfLast { it == fruitType }
    val insertAt = if (lastMatchIndex >= 0) lastMatchIndex + 1 else fruits.size

    // Insert the new fruit
    fruits.add(insertAt, fruitType)

    // Rebuild rack slots (cap at RACK_SIZE)
    return List(GameState.RACK_SIZE) { idx ->
      RackSlot(
        index = idx,
        fruitType = if (idx < fruits.size) fruits[idx] else null
      )
    }
  }

  /**
   * Finds indices of 3 consecutive identical fruits in the rack.
   * Returns the indices of the matched group, or empty list if no match.
   */
  internal fun findMatchIndices(rack: List<RackSlot>): List<Int> {
    val fruits = rack.map { it.fruitType }

    for (i in 0..fruits.size - 3) {
      val a = fruits[i]
      val b = fruits[i + 1]
      val c = fruits[i + 2]
      if (a != null && a == b && b == c) {
        return listOf(i, i + 1, i + 2)
      }
    }
    return emptyList()
  }
}
