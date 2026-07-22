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
        flyingTiles = emptyList(),
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
  // Tile Selection (Instant & Non-Blocking)
  // ──────────────────────────────────────────

  /**
   * Called when the player taps a tile on the board.
   * Instantly removes the tile from the board, reserves its rack slot,
   * checks for 3-matches, and launches a concurrent flying animation overlay.
   *
   * Does NOT block the UI — player can immediately tap another tile!
   */
  fun selectTile(tileId: Int, fromX: Float, fromY: Float, toX: Float, toY: Float) {
    val current = _state.value
    if (current.phase != GamePhase.PLAYING) return

    val tile = current.board.find { it.id == tileId && it.isVisible } ?: return
    if (tileId !in current.clickableTileIds) return

    // Ensure rack has space (less than 7 occupied)
    if (current.rackOccupiedCount >= GameState.RACK_SIZE) return

    // 1. Remove tile from board immediately & recalculate clickables
    val updatedBoard = current.board.map { t ->
      if (t.id == tileId) t.copy(isVisible = false) else t
    }
    val updatedClickable = LevelGenerator.calculateClickableTiles(updatedBoard)

    // 2. Insert into rack immediately with grouping
    val targetSlotIndex = findInsertionIndex(current.rack, tile.fruitType)
    val updatedRack = insertIntoRack(current.rack, tile.fruitType)

    // 3. Create concurrent flying tile animation
    val flightId = "${tileId}_${tile.fruitType.name}_${updatedBoard.count { !it.isVisible }}"
    val newFlyingTile = FlyingTile(
      id = flightId,
      fruitType = tile.fruitType,
      fromX = fromX,
      fromY = fromY,
      toX = toX,
      toY = toY,
      targetSlotIndex = targetSlotIndex
    )
    val updatedFlyingTiles = current.flyingTiles + newFlyingTile

    // 4. Check for 3-matches immediately
    val matchIndices = findMatchIndices(updatedRack)
    val updatedDestroying = (current.destroyingIndices + matchIndices).distinct()

    // 5. Determine new game phase
    val boardEmpty = !updatedBoard.any { it.isVisible }
    val rackEmpty = updatedRack.all { it.isEmpty }
    val isFull = updatedRack.count { !it.isEmpty } >= GameState.RACK_SIZE

    val newPhase = when {
      boardEmpty && rackEmpty && updatedFlyingTiles.isEmpty() -> GamePhase.WIN
      isFull && matchIndices.isEmpty() && updatedDestroying.isEmpty() -> GamePhase.LOSS
      else -> GamePhase.PLAYING
    }

    _state.update {
      it.copy(
        board = updatedBoard,
        rack = updatedRack,
        clickableTileIds = updatedClickable,
        flyingTiles = updatedFlyingTiles,
        destroyingIndices = updatedDestroying,
        phase = newPhase
      )
    }
  }

  /**
   * Called when an individual flying tile animation finishes in the overlay.
   */
  fun onFlyComplete(flightId: String) {
    _state.update { current ->
      val updatedFlying = current.flyingTiles.filterNot { it.id == flightId }
      val boardEmpty = !current.board.any { it.isVisible }
      val rackEmpty = current.rack.all { it.isEmpty }

      val newPhase = when {
        boardEmpty && rackEmpty && updatedFlying.isEmpty() -> GamePhase.WIN
        else -> current.phase
      }

      current.copy(
        flyingTiles = updatedFlying,
        phase = newPhase
      )
    }
  }

  /**
   * Called when the destruction animation completes for matched tiles.
   * Removes destroyed slots and shifts remaining slots left.
   */
  fun onDestroyComplete() {
    _state.update { current ->
      val indices = current.destroyingIndices.toSet()
      if (indices.isEmpty()) return@update current

      // Remove destroyed slots and shift left
      val remaining = current.rack
        .filter { it.index !in indices }
        .mapIndexed { idx, slot -> slot.copy(index = idx) }

      val newRack = remaining + List(GameState.RACK_SIZE - remaining.size) { idx ->
        RackSlot(index = remaining.size + idx)
      }

      val boardEmpty = !current.board.any { it.isVisible }
      val rackEmpty = newRack.all { it.isEmpty }

      val newPhase = when {
        boardEmpty && rackEmpty && current.flyingTiles.isEmpty() -> GamePhase.WIN
        newRack.count { !it.isEmpty } >= GameState.RACK_SIZE -> GamePhase.LOSS
        else -> GamePhase.PLAYING
      }

      current.copy(
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
