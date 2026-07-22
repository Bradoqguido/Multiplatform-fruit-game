package com.fruitpuzzle.game.logic

import com.fruitpuzzle.game.model.BoardTile
import com.fruitpuzzle.game.model.FlyingTile
import com.fruitpuzzle.game.model.FruitType
import com.fruitpuzzle.game.model.GamePhase
import com.fruitpuzzle.game.model.GameState
import com.fruitpuzzle.game.model.MoveRecord
import com.fruitpuzzle.game.model.RackSlot
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameRepository(private val settings: Settings) {

  private val _state = MutableStateFlow(GameState())
  val state: StateFlow<GameState> = _state.asStateFlow()

  companion object {
    private const val KEY_CURRENT_LEVEL = "currentLevel"
    private const val KEY_LIVES = "livesCounter"
    private const val DEFAULT_LIVES = 3
  }

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

  fun startLevel(level: Int) {
    val board = LevelGenerator.generate(level)
    val clickable = LevelGenerator.calculateClickableTiles(board)

    _state.update {
      it.copy(
        currentLevel = level,
        lives = DEFAULT_LIVES,
        board = board,
        rack = List(GameState.RACK_SIZE) { idx -> RackSlot(index = idx) },
        phase = GamePhase.PLAYING,
        flyingTiles = emptyList(),
        destroyingIndices = emptyList(),
        clickableTileIds = clickable,
        undoCount = GameState.MAX_UNDOS,
        moveHistory = emptyList()
      )
    }
    saveProgress()
  }

  fun startCurrentLevel() {
    startLevel(_state.value.currentLevel)
  }

  fun selectTile(tileId: Int, fromX: Float, fromY: Float, toX: Float, toY: Float) {
    val current = _state.value
    if (current.phase != GamePhase.PLAYING) return

    val tile = current.board.find { it.id == tileId && it.isVisible } ?: return
    if (tileId !in current.clickableTileIds) return

    if (current.rackOccupiedCount >= GameState.RACK_SIZE) return

    val record = MoveRecord(
      tileId = tileId,
      boardState = current.board,
      rackState = current.rack,
      destroyingIndices = current.destroyingIndices
    )
    val updatedHistory = current.moveHistory + record

    val updatedBoard = current.board.map { t ->
      if (t.id == tileId) t.copy(isVisible = false) else t
    }
    val updatedClickable = LevelGenerator.calculateClickableTiles(updatedBoard)

    val targetSlotIndex = findInsertionIndex(current.rack, tile.fruitType)
    val updatedRack = insertIntoRack(current.rack, tile.fruitType)

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

    val matchIndices = findMatchIndices(updatedRack)
    val updatedDestroying = (current.destroyingIndices + matchIndices).distinct()

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
        phase = newPhase,
        moveHistory = updatedHistory
      )
    }
  }

  fun undoMove() {
    val current = _state.value
    if (!current.canUndo) return

    val lastRecord = current.moveHistory.last()
    val updatedHistory = current.moveHistory.dropLast(1)
    val restoredClickable = LevelGenerator.calculateClickableTiles(lastRecord.boardState)

    _state.update {
      it.copy(
        board = lastRecord.boardState,
        rack = lastRecord.rackState,
        destroyingIndices = lastRecord.destroyingIndices,
        clickableTileIds = restoredClickable,
        flyingTiles = emptyList(),
        undoCount = current.undoCount - 1,
        moveHistory = updatedHistory,
        phase = GamePhase.PLAYING
      )
    }
  }

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

  fun onDestroyComplete() {
    _state.update { current ->
      val indices = current.destroyingIndices.toSet()
      if (indices.isEmpty()) return@update current

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

  fun advanceLevel() {
    val nextLevel = _state.value.currentLevel + 1
    _state.update { it.copy(currentLevel = nextLevel) }
    saveProgress()
    startLevel(nextLevel)
  }

  fun retryLevel() {
    val current = _state.value
    val newLives = current.lives - 1

    if (newLives <= 0) {
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

  fun continueAfterGameOver() {
    startLevel(_state.value.currentLevel)
  }

  private fun findInsertionIndex(rack: List<RackSlot>, fruitType: FruitType): Int {
    val lastMatchIndex = rack.indexOfLast { it.fruitType == fruitType }

    return if (lastMatchIndex >= 0) {
      lastMatchIndex + 1
    } else {
      rack.indexOfFirst { it.isEmpty }.takeIf { it >= 0 } ?: (GameState.RACK_SIZE - 1)
    }
  }

  internal fun insertIntoRack(rack: List<RackSlot>, fruitType: FruitType): List<RackSlot> {
    val fruits = rack.mapNotNull { it.fruitType }.toMutableList()

    val lastMatchIndex = fruits.indexOfLast { it == fruitType }
    val insertAt = if (lastMatchIndex >= 0) lastMatchIndex + 1 else fruits.size

    fruits.add(insertAt, fruitType)

    return List(GameState.RACK_SIZE) { idx ->
      RackSlot(
        index = idx,
        fruitType = if (idx < fruits.size) fruits[idx] else null
      )
    }
  }

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
