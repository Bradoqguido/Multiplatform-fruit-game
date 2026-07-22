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

  fun startLevel(level: Int, resetLives: Boolean = true) {
    val board = LevelGenerator.generate(level)
    val clickable = LevelGenerator.calculateClickableTiles(board)
    val dominant = calculateDominantFruit(board)

    _state.update {
      it.copy(
        currentLevel = level,
        lives = if (resetLives) DEFAULT_LIVES else it.lives,
        board = board,
        rack = List(GameState.RACK_SIZE) { idx -> RackSlot(index = idx) },
        phase = GamePhase.PLAYING,
        flyingTiles = emptyList(),
        destroyingIndices = emptyList(),
        clickableTileIds = clickable,
        dominantFruit = dominant,
        undoCount = GameState.MAX_UNDOS,
        moveHistory = emptyList(),
        isPaused = false
      )
    }
    saveProgress()
    com.fruitpuzzle.game.audio.AudioEngine.startBgMusic(_state.value.bgmVolume, _state.value.isMuted)
  }

  fun startCurrentLevel() {
    startLevel(_state.value.currentLevel)
  }

  fun selectTile(tileId: Int, fromX: Float, fromY: Float, toX: Float, toY: Float) {
    val current = _state.value
    if (current.phase != GamePhase.PLAYING || current.isPaused) return

    val tile = current.board.find { it.id == tileId && it.isVisible } ?: return
    if (tileId !in current.clickableTileIds) return
    if (current.rackOccupiedCount >= GameState.RACK_SIZE) return

    com.fruitpuzzle.game.audio.AudioEngine.playClickSfx(current.sfxVolume, current.isMuted)

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
    val updatedDominant = calculateDominantFruit(updatedBoard)

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

    _state.update {
      it.copy(
        board = updatedBoard,
        rack = updatedRack,
        clickableTileIds = updatedClickable,
        flyingTiles = updatedFlyingTiles,
        dominantFruit = updatedDominant,
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
    val restoredDominant = calculateDominantFruit(lastRecord.boardState)

    _state.update {
      it.copy(
        board = lastRecord.boardState,
        rack = lastRecord.rackState,
        destroyingIndices = lastRecord.destroyingIndices,
        clickableTileIds = restoredClickable,
        flyingTiles = emptyList(),
        dominantFruit = restoredDominant,
        undoCount = current.undoCount - 1,
        moveHistory = updatedHistory,
        phase = GamePhase.PLAYING
      )
    }
  }

  fun onFlyComplete(flightId: String) {
    val current = _state.value
    com.fruitpuzzle.game.audio.AudioEngine.playDropSfx(current.sfxVolume, current.isMuted)

    _state.update { curr ->
      val updatedFlying = curr.flyingTiles.filterNot { it.id == flightId }

      // Check for 3-of-a-kind match ONLY after flying tile lands in slot rack
      val matchIndices = if (updatedFlying.isEmpty()) {
        findMatchIndices(curr.rack)
      } else {
        emptyList()
      }

      if (matchIndices.isNotEmpty()) {
        com.fruitpuzzle.game.audio.AudioEngine.playMatchSfx(curr.sfxVolume, curr.isMuted)
      }

      val updatedDestroying = (curr.destroyingIndices + matchIndices).distinct()

      val boardEmpty = !curr.board.any { it.isVisible }
      val rackEmpty = curr.rack.all { it.isEmpty }
      val isFull = curr.rack.count { !it.isEmpty } >= GameState.RACK_SIZE

      // 7-slot fill verification rule:
      // If 7 slots are full AND no match is formed AND no destroying animation active -> Game Over / Life Lost
      val newPhase = when {
        boardEmpty && rackEmpty && updatedFlying.isEmpty() && updatedDestroying.isEmpty() -> GamePhase.WIN
        isFull && matchIndices.isEmpty() && updatedDestroying.isEmpty() && updatedFlying.isEmpty() -> GamePhase.LOSS
        else -> curr.phase
      }

      curr.copy(
        flyingTiles = updatedFlying,
        destroyingIndices = updatedDestroying,
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

  fun togglePause() {
    _state.update { it.copy(isPaused = !it.isPaused) }
  }

  fun toggleMute(muted: Boolean) {
    _state.update { it.copy(isMuted = muted) }
    com.fruitpuzzle.game.audio.AudioEngine.updateBgmVolume(_state.value.bgmVolume, muted)
  }

  fun setBgmVolume(volume: Float) {
    _state.update { it.copy(bgmVolume = volume) }
    com.fruitpuzzle.game.audio.AudioEngine.updateBgmVolume(volume, _state.value.isMuted)
  }

  fun setSfxVolume(volume: Float) {
    _state.update { it.copy(sfxVolume = volume) }
  }

  fun setUiScale(scale: Float) {
    _state.update { it.copy(uiScale = scale) }
  }

  fun setFontScale(scale: Float) {
    _state.update { it.copy(fontScale = scale) }
  }

  private fun calculateDominantFruit(board: List<BoardTile>): FruitType {
    val visible = board.filter { it.isVisible }
    if (visible.isEmpty()) return FruitType.APPLE
    return visible.groupBy { it.fruitType }
      .maxByOrNull { it.value.size }?.key ?: FruitType.APPLE
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
      startLevel(current.currentLevel, resetLives = false)
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
