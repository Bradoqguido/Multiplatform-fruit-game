package com.fruitpuzzle.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
 * Full-screen overlay that renders a tile flying from the board to the rack.
 * Uses spring physics for a natural, bouncy trajectory.
 * Rendered at the root level so it's never clipped by parent containers.
 *
 * @param flyingTile        Data about the tile being animated (source, destination, fruit type).
 * @param onAnimationComplete Called when the spring animation settles at the destination.
 */
@Composable
fun FlyingOverlay(
  flyingTile: FlyingTile,
  onAnimationComplete: () -> Unit
) {
  val density = LocalDensity.current
  val tileSize = 42.dp

  val animatable = remember(flyingTile) {
    Animatable(
      initialValue = Offset(flyingTile.fromX, flyingTile.fromY),
      typeConverter = Offset.VectorConverter
    )
  }

  LaunchedEffect(flyingTile) {
    animatable.animateTo(
      targetValue = Offset(flyingTile.toX, flyingTile.toY),
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
      )
    )
    onAnimationComplete()
  }

  val currentOffset = animatable.value
  val xDp = with(density) { currentOffset.x.toDp() }
  val yDp = with(density) { currentOffset.y.toDp() }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .zIndex(100f)
  ) {
    FruitTile(
      fruitType = flyingTile.tile.fruitType,
      size = tileSize,
      isClickable = false,
      modifier = Modifier
        .offset(x = xDp, y = yDp)
        .graphicsLayer {
          // Slight scale-up during flight for visual emphasis
          scaleX = 1.2f
          scaleY = 1.2f
          shadowElevation = 12f
        }
    )
  }
}
