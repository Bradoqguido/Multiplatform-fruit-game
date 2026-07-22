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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import com.fruitpuzzle.game.model.FruitType
import com.fruitpuzzle.game.ui.animation.shakeAnimation
import com.fruitpuzzle.game.ui.components.MatchParticleOverlay
import com.fruitpuzzle.game.ui.components.PauseMenuModal

/**
 * Main game screen with header, slot rack, board, flying overlay, pause menu, and popups.
 */
@Composable
fun GameScreen(
  gameState: GameState,
  onTileClick: (tileId: Int, fromX: Float, fromY: Float, toX: Float, toY: Float) -> Unit,
  onFlyComplete: (flightId: String) -> Unit,
  onDestroyComplete: () -> Unit,
  onUndoClick: () -> Unit,
  onNextLevel: () -> Unit,
  onRetry: () -> Unit,
  onContinueAfterGameOver: () -> Unit,
  onBackToMenu: () -> Unit,
  onTogglePause: () -> Unit = {},
  onToggleMute: (Boolean) -> Unit = {},
  onBgmVolumeChange: (Float) -> Unit = {},
  onSfxVolumeChange: (Float) -> Unit = {},
  onUiScaleChange: (Float) -> Unit = {},
  onFontScaleChange: (Float) -> Unit = {}
) {
  // Track absolute positions of rack slots for fly animation targeting
  val slotPositions = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(fruitThemeGradient(gameState.dominantFruit))
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // ─── Header ───
      GameHeader(
        level = gameState.currentLevel,
        lives = gameState.lives,
        fontScale = gameState.fontScale,
        onPauseClick = onTogglePause,
        modifier = Modifier.fillMaxWidth()
      )

      // ─── Slot Rack ───
      SlotRack(
        rack = gameState.rack,
        destroyingIndices = gameState.destroyingIndices,
        isDestroyPhase = gameState.destroyingIndices.isNotEmpty(),
        onDestroyComplete = onDestroyComplete,
        onSlotPositioned = { index, x, y ->
          slotPositions[index] = Pair(x, y)
        },
        uiScale = gameState.uiScale,
        fontScale = gameState.fontScale,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 8.dp)
      )

      // ─── Controls / Action Bar (Menu & Undo) ───
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Button(
          onClick = onBackToMenu,
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF37474F)
          )
        ) {
          Text(
            text = "🏠 Menu",
            fontSize = 14.sp * gameState.fontScale,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        Button(
          onClick = onUndoClick,
          enabled = gameState.canUndo,
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3F51B5),
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
          )
        ) {
          Text(
            text = "↩️ Desfazer (${gameState.undoCount}/3)",
            fontSize = 14.sp * gameState.fontScale,
            fontWeight = FontWeight.Bold,
            color = if (gameState.canUndo) Color.White else Color.White.copy(alpha = 0.4f)
          )
        }
      }

      // ─── Board ───
      GameBoard(
        board = gameState.board,
        clickableTileIds = gameState.clickableTileIds,
        isInteractive = gameState.phase == GamePhase.PLAYING && !gameState.isRackFull && !gameState.isPaused,
        uiScale = gameState.uiScale,
        onTileClick = { tileId, fromX, fromY ->
          val rackOccupied = gameState.rack.count { !it.isEmpty }
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

    // ─── Match Triumph Particle Burst Overlay ───
    MatchParticleOverlay(
      destroyingIndices = gameState.destroyingIndices,
      slotPositions = slotPositions
    )

    // ─── Flying Overlay (renders above board) ───
    FlyingOverlay(
      flyingTiles = gameState.flyingTiles,
      onFlyComplete = onFlyComplete
    )

    // ─── Pause Menu Modal ───
    if (gameState.isPaused) {
      PauseMenuModal(
        isMuted = gameState.isMuted,
        bgmVolume = gameState.bgmVolume,
        sfxVolume = gameState.sfxVolume,
        uiScale = gameState.uiScale,
        fontScale = gameState.fontScale,
        onResume = onTogglePause,
        onMuteToggle = onToggleMute,
        onBgmVolumeChange = onBgmVolumeChange,
        onSfxVolumeChange = onSfxVolumeChange,
        onUiScaleChange = onUiScaleChange,
        onFontScaleChange = onFontScaleChange,
        onRestart = onRetry,
        onBackToMenu = onBackToMenu
      )
    }

    // ─── Win Popup ───
    if (gameState.phase == GamePhase.WIN) {
      GamePopup(
        title = "🎉 Parabéns!",
        message = "Nível ${gameState.currentLevel} concluído com sucesso!",
        primaryButtonText = "Próximo Nível",
        onPrimaryClick = onNextLevel,
        secondaryButtonText = "Menu Inicial",
        onSecondaryClick = onBackToMenu
      )
    }

    // ─── Loss Popup ───
    if (gameState.phase == GamePhase.LOSS) {
      GamePopup(
        title = "😔 Tente Novamente",
        message = "As vagas estão cheias!\nVidas restantes: ${gameState.lives}",
        primaryButtonText = "Tentar Novamente (−1 ❤️)",
        onPrimaryClick = onRetry,
        secondaryButtonText = "Menu Inicial",
        onSecondaryClick = onBackToMenu
      )
    }

    // ─── Game Over Popup ───
    if (gameState.phase == GamePhase.GAME_OVER) {
      GamePopup(
        title = "💀 Fim de Jogo",
        message = "Suas vidas acabaram!\nRetornando ao Nível ${gameState.currentLevel}.\nVidas recarregadas para 3.",
        primaryButtonText = "Continuar",
        onPrimaryClick = onContinueAfterGameOver,
        secondaryButtonText = "Menu Inicial",
        onSecondaryClick = onBackToMenu
      )
    }
  }
}

/**
 * Header bar showing level, pause button, and animated life bar.
 */
@Composable
private fun GameHeader(
  level: Int,
  lives: Int,
  fontScale: Float,
  onPauseClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .background(Color(0xFF1A1A2E).copy(alpha = 0.8f))
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Button(
        onClick = onPauseClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
      ) {
        Text(text = "⏸️", fontSize = 16.sp * fontScale, color = Color.White)
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = "Nível $level",
        fontSize = 22.sp * fontScale,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    Row(
      modifier = Modifier.shakeAnimation(
        trigger = lives,
        continuous = (lives == 1),
        shakeOffsetDp = 10f
      )
    ) {
      repeat(lives) {
        Text(text = "❤️", fontSize = 20.sp * fontScale)
        if (it < lives - 1) Spacer(modifier = Modifier.width(4.dp))
      }
      repeat(3 - lives) {
        Text(text = "🖤", fontSize = 20.sp * fontScale)
      }
    }
  }
}

/**
 * Dynamic theme background gradient mapping based on dominant fruit.
 */
private fun fruitThemeGradient(fruitType: FruitType): Brush {
  val baseColor = when (fruitType) {
    FruitType.APPLE, FruitType.STRAWBERRY, FruitType.CHERRY, FruitType.RUBY, FruitType.WATERMELON -> Color(0xFF3E101D)
    FruitType.GREEN_APPLE, FruitType.MELON, FruitType.PEAR, FruitType.EMERALD -> Color(0xFF132A13)
    FruitType.ORANGE, FruitType.PEACH, FruitType.AMBER -> Color(0xFF3A1C08)
    FruitType.LEMON, FruitType.BANANA, FruitType.PINEAPPLE, FruitType.TOPAZ -> Color(0xFF332D06)
    FruitType.GRAPE, FruitType.AMETHYST -> Color(0xFF25103E)
    FruitType.DIAMOND, FruitType.SAPPHIRE, FruitType.BLUE_GEM -> Color(0xFF0C2340)
  }
  return Brush.verticalGradient(
    colors = listOf(
      baseColor,
      Color(0xFF0D1B2A),
      baseColor
    )
  )
}
