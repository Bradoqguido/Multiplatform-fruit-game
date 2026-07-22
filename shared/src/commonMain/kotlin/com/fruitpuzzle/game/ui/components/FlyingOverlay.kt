package com.fruitpuzzle.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.fruitpuzzle.game.model.FlyingTile

/**
 * Full-screen overlay that renders tiles flying concurrently from the board to the rack.
 * Uses snappy spring physics for fast, natural flight trajectories (~200ms).
 * Rendered at the root level so it is never clipped by parent containers.
 *
 * @param flyingTiles        List of active flying tiles.
 * @param onFlyComplete      Callback when an individual tile finishes flying.
 */
@Composable
fun FlyingOverlay(
  flyingTiles: List<FlyingTile>,
  onFlyComplete: (flightId: String) -> Unit
) {
  if (flyingTiles.isEmpty()) return

  Box(
    modifier = Modifier
      .fillMaxSize()
      .zIndex(100f)
  ) {
    for (flyingTile in flyingTiles) {
      SingleFlyingItem(
        flyingTile = flyingTile,
        onAnimationComplete = { onFlyComplete(flyingTile.id) }
      )
    }
  }
}

@Composable
private fun SingleFlyingItem(
  flyingTile: FlyingTile,
  onAnimationComplete: () -> Unit
) {
  val density = LocalDensity.current
  val tileSize = 42.dp

  val animatable = remember(flyingTile.id) {
    Animatable(
      initialValue = Offset(flyingTile.fromX, flyingTile.fromY),
      typeConverter = Offset.VectorConverter
    )
  }

  LaunchedEffect(flyingTile.id) {
    animatable.animateTo(
      targetValue = Offset(flyingTile.toX, flyingTile.toY),
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
      )
    )
    onAnimationComplete()
  }

  val currentOffset = animatable.value
  val xDp = with(density) { currentOffset.x.toDp() }
  val yDp = with(density) { currentOffset.y.toDp() }

  FruitTile(
    fruitType = flyingTile.fruitType,
    size = tileSize,
    isClickable = false,
    modifier = Modifier
      .offset(x = xDp, y = yDp)
      .graphicsLayer {
        scaleX = 1.15f
        scaleY = 1.15f
        shadowElevation = 8f
      }
  )
}

