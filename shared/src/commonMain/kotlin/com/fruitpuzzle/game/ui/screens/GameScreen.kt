package com.fruitpuzzle.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fruitpuzzle.game.model.GamePhase
import com.fruitpuzzle.game.model.GameState
import com.fruitpuzzle.game.ui.components.FlyingOverlay
import com.fruitpuzzle.game.ui.components.GameBoard
import com.fruitpuzzle.game.ui.components.GamePopup
import com.fruitpuzzle.game.ui.components.SlotRack

/**
 * Main game screen with header, slot rack, board, flying overlay, and popups.
 */
@Composable
fun GameScreen(
  gameState: GameState,
  onTileClick: (tileId: Int, fromX: Float, fromY: Float, toX: Float, toY: Float) -> Unit,
  onFlyComplete: () -> Unit,
  onDestroyComplete: () -> Unit,
  onNextLevel: () -> Unit,
  onRetry: () -> Unit,
  onContinueAfterGameOver: () -> Unit,
  onBackToMenu: () -> Unit
) {
  // Track absolute positions of rack slots for fly animation targeting
  val slotPositions = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF0D1B2A),
            Color(0xFF1B2838),
            Color(0xFF0D1B2A)
          )
        )
      )
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // ─── Header ───
      GameHeader(
        level = gameState.currentLevel,
        lives = gameState.lives,
        modifier = Modifier.fillMaxWidth()
      )

      // ─── Slot Rack ───
      SlotRack(
        rack = gameState.rack,
        destroyingIndices = gameState.destroyingIndices,
        isDestroyPhase = gameState.phase == GamePhase.ANIMATING_DESTROY,
        onDestroyComplete = onDestroyComplete,
        onSlotPositioned = { index, x, y ->
          slotPositions[index] = Pair(x, y)
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp)
      )

      // ─── Board ───
      GameBoard(
        board = gameState.board,
        clickableTileIds = gameState.clickableTileIds,
        isInteractive = gameState.phase == GamePhase.PLAYING,
        onTileClick = { tileId, fromX, fromY ->
          // Find the target slot position
          val rackOccupied = gameState.rack.count { it.fruitType != null }
          val targetIndex = rackOccupied.coerceAtMost(GameState.RACK_SIZE - 1)
          val targetPos = slotPositions[targetIndex] ?: Pair(0f, 0f)
          onTileClick(tileId, fromX, fromY, targetPos.first, targetPos.second)
        },
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(8.dp)
      )
    }

    // ─── Flying Overlay (renders above everything) ───
    if (gameState.phase == GamePhase.ANIMATING_FLY && gameState.flyingTile != null) {
      FlyingOverlay(
        flyingTile = gameState.flyingTile,
        onAnimationComplete = onFlyComplete
      )
    }

    // ─── Win Popup ───
    if (gameState.phase == GamePhase.WIN) {
      GamePopup(
        title = "🎉 Congratulations!",
        message = "Level ${gameState.currentLevel} cleared!",
        primaryButtonText = "Next Level",
        onPrimaryClick = onNextLevel,
        secondaryButtonText = "Menu",
        onSecondaryClick = onBackToMenu
      )
    }

    // ─── Loss Popup ───
    if (gameState.phase == GamePhase.LOSS) {
      GamePopup(
        title = "😔 Try Again",
        message = "Rack is full!\nLives remaining: ${gameState.lives}",
        primaryButtonText = "Retry (−1 ❤️)",
        onPrimaryClick = onRetry,
        secondaryButtonText = "Menu",
        onSecondaryClick = onBackToMenu
      )
    }

    // ─── Game Over Popup ───
    if (gameState.phase == GamePhase.GAME_OVER) {
      GamePopup(
        title = "💀 Game Over",
        message = "Lives depleted! Back to Level ${gameState.currentLevel}.\nLives refilled to 3.",
        primaryButtonText = "Continue",
        onPrimaryClick = onContinueAfterGameOver,
        secondaryButtonText = "Menu",
        onSecondaryClick = onBackToMenu
      )
    }
  }
}

/**
 * Header bar showing current level and lives.
 */
@Composable
private fun GameHeader(
  level: Int,
  lives: Int,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .background(Color(0xFF1A1A2E).copy(alpha = 0.8f))
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "Level $level",
      fontSize = 22.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )

    Row {
      repeat(lives) {
        Text(text = "❤️", fontSize = 20.sp)
        if (it < lives - 1) Spacer(modifier = Modifier.width(4.dp))
      }
      repeat(3 - lives) {
        Text(text = "🖤", fontSize = 20.sp)
      }
    }
  }
}
