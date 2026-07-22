package com.fruitpuzzle.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
  val originX: Float,
  val originY: Float,
  val angle: Float,
  val speed: Float,
  val color: Color,
  val radius: Float
)

/**
 * Renders a bursting sparkle particle overlay when 3 fruits match in the rack.
 */
@Composable
fun MatchParticleOverlay(
  destroyingIndices: List<Int>,
  slotPositions: Map<Int, Pair<Float, Float>>
) {
  if (destroyingIndices.isEmpty()) return

  val progress = remember(destroyingIndices) { Animatable(0f) }
  val particles = remember(destroyingIndices) {
    val list = mutableListOf<Particle>()
    val colors = listOf(
      Color(0xFFFFD54F),
      Color(0xFFFF8A80),
      Color(0xFF80DEEA),
      Color(0xFFA5D6A7),
      Color(0xFFFFCC80)
    )
    val random = Random(destroyingIndices.hashCode())

    for (index in destroyingIndices) {
      val pos = slotPositions[index] ?: Pair(0f, 0f)
      val cx = pos.first + 40f
      val cy = pos.second + 40f

      repeat(12) {
        val angle = (random.nextFloat() * 2f * Math.PI).toFloat()
        val speed = 40f + random.nextFloat() * 80f
        val color = colors[random.nextInt(colors.size)]
        val radius = 3f + random.nextFloat() * 5f
        list.add(Particle(cx, cy, angle, speed, color, radius))
      }
    }
    list
  }

  LaunchedEffect(destroyingIndices) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 350, easing = LinearEasing)
    )
  }

  val currentProgress = progress.value

  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .zIndex(90f)
  ) {
    val alpha = (1f - currentProgress).coerceIn(0f, 1f)
    for (p in particles) {
      val dist = p.speed * currentProgress
      val x = p.originX + cos(p.angle) * dist
      val y = p.originY + sin(p.angle) * dist

      drawCircle(
        color = p.color.copy(alpha = alpha),
        radius = p.radius * (1f - currentProgress * 0.5f),
        center = Offset(x, y)
      )
    }
  }
}
