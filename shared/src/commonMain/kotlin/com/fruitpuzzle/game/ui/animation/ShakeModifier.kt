package com.fruitpuzzle.game.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Applies a horizontal shake animation to the target composable.
 *
 * @param trigger       Key to trigger a single burst shake animation (e.g. rack filled 6th slot, life lost).
 * @param continuous    If true, continuously pulses/shakes (e.g. low life warning when 1 life remains).
 * @param shakeOffsetDp Peak horizontal shake distance.
 */
fun Modifier.shakeAnimation(
  trigger: Any? = null,
  continuous: Boolean = false,
  shakeOffsetDp: Float = 8f
): Modifier = composed {
  val offsetX = remember { Animatable(0f) }

  LaunchedEffect(trigger, continuous) {
    if (continuous) {
      offsetX.animateTo(
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
          animation = keyframes {
            durationMillis = 600
            0f at 0 with LinearEasing
            -shakeOffsetDp at 100 with LinearEasing
            shakeOffsetDp at 200 with LinearEasing
            -shakeOffsetDp / 2f at 300 with LinearEasing
            shakeOffsetDp / 2f at 400 with LinearEasing
            0f at 600 with LinearEasing
          },
          repeatMode = RepeatMode.Restart
        )
      )
    } else if (trigger != null) {
      offsetX.snapTo(0f)
      offsetX.animateTo(
        targetValue = 0f,
        animationSpec = keyframes {
          durationMillis = 400
          0f at 0 with LinearEasing
          -shakeOffsetDp at 80 with LinearEasing
          shakeOffsetDp at 160 with LinearEasing
          -shakeOffsetDp / 2f at 240 with LinearEasing
          shakeOffsetDp / 2f at 320 with LinearEasing
          0f at 400 with LinearEasing
        }
      )
    } else {
      offsetX.snapTo(0f)
    }
  }

  this.graphicsLayer {
    translationX = offsetX.value
  }
}
