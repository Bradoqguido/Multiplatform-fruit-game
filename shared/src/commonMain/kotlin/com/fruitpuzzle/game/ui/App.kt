package com.fruitpuzzle.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fruitpuzzle.game.logic.GameRepository
import com.fruitpuzzle.game.platform.createSettings
import com.fruitpuzzle.game.ui.screens.GameScreen
import com.fruitpuzzle.game.ui.screens.StartScreen
import com.fruitpuzzle.game.ui.theme.FruitPuzzleTheme

/**
 * Screens in the app navigation.
 */
enum class Screen {
  START,
  GAME
}

/**
 * Root composable for the entire application.
 * Manages navigation between Start and Game screens.
 * Shared across all platforms (Android, iOS, Desktop).
 */
@Composable
fun App() {
  val repository = remember {
    val settings = createSettings()
    GameRepository(settings).also { it.loadSavedProgress() }
  }

  var currentScreen by remember { mutableStateOf(Screen.START) }
  val gameState by repository.state.collectAsState()

  FruitPuzzleTheme {
    when (currentScreen) {
      Screen.START -> StartScreen(
        onStartClick = {
          repository.startCurrentLevel()
          currentScreen = Screen.GAME
        }
      )

      Screen.GAME -> GameScreen(
        gameState = gameState,
        onTileClick = { tileId, fromX, fromY, toX, toY ->
          repository.selectTile(tileId, fromX, fromY, toX, toY)
        },
        onFlyComplete = { flightId -> repository.onFlyComplete(flightId) },
        onDestroyComplete = { repository.onDestroyComplete() },
        onUndoClick = { repository.undoMove() },
        onNextLevel = { repository.advanceLevel() },
        onRetry = { repository.retryLevel() },
        onContinueAfterGameOver = { repository.continueAfterGameOver() },
        onBackToMenu = { currentScreen = Screen.START },
        onTogglePause = { repository.togglePause() },
        onToggleMute = { muted -> repository.toggleMute(muted) },
        onBgmVolumeChange = { vol -> repository.setBgmVolume(vol) },
        onSfxVolumeChange = { vol -> repository.setSfxVolume(vol) },
        onUiScaleChange = { scale -> repository.setUiScale(scale) },
        onFontScaleChange = { scale -> repository.setFontScale(scale) }
      )
    }
  }
}
