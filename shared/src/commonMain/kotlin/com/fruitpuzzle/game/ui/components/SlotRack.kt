package com.fruitpuzzle.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.fruitpuzzle.game.model.FruitType
import com.fruitpuzzle.game.model.RackSlot
import kotlinx.coroutines.delay

import com.fruitpuzzle.game.ui.animation.shakeAnimation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.Dp

/**
 * The 7-slot rack at the top of the game screen.
 * Shows occupied slots with fruit tiles and empty slots as bordered placeholders.
 * Handles destruction animation when 3 identical fruits match and 6th-slot warning shake.
 */
@Composable
fun SlotRack(
  rack: List<RackSlot>,
  destroyingIndices: List<Int>,
  isDestroyPhase: Boolean,
  onDestroyComplete: () -> Unit,
  onSlotPositioned: (index: Int, x: Float, y: Float) -> Unit,
  uiScale: Float = 1.0f,
  modifier: Modifier = Modifier
) {
  val occupiedCount = rack.count { !it.isEmpty }

  // Trigger destroy callback after full 3-piece match group is displayed in the rack
  if (isDestroyPhase) {
    LaunchedEffect(destroyingIndices) {
      delay(300) // 300ms delay allows user to visually see the complete 3-piece group formed in slots
      onDestroyComplete()
    }
  }

  BoxWithConstraints(modifier = modifier) {
    val maxSlotWidth = (maxWidth - 32.dp) / 7.2f
    val baseSlotSize = 46.dp * uiScale
    val slotContainerSize = minOf(baseSlotSize, maxSlotWidth).coerceAtLeast(32.dp)
    val tileInsideSize = slotContainerSize - 4.dp

    Row(
      modifier = Modifier
        .shakeAnimation(
          trigger = if (occupiedCount == 6 && !isDestroyPhase) occupiedCount else null,
          shakeOffsetDp = 10f
        )
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF2A2A4A).copy(alpha = 0.7f))
        .padding(horizontal = 8.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      for (slot in rack) {
        val isDestroying = slot.index in destroyingIndices

        SlotContent(
          fruitType = slot.fruitType,
          slotIndex = slot.index,
          isDestroying = isDestroying,
          slotSize = slotContainerSize,
          tileSize = tileInsideSize,
          onSlotPositioned = onSlotPositioned
        )
      }
    }
  }
}

/**
 * Individual slot content — extracted to break the RowScope chain so
 * top-level [AnimatedVisibility] resolves correctly.
 */
@Composable
private fun SlotContent(
  fruitType: FruitType?,
  slotIndex: Int,
  isDestroying: Boolean,
  slotSize: Dp,
  tileSize: Dp,
  onSlotPositioned: (index: Int, x: Float, y: Float) -> Unit
) {
  Box(
    modifier = Modifier
      .size(slotSize)
      .onGloballyPositioned { coordinates ->
        val pos = coordinates.positionInRoot()
        onSlotPositioned(slotIndex, pos.x, pos.y)
      },
    contentAlignment = Alignment.Center
  ) {
    if (fruitType != null) {
      AnimatedVisibility(
        visible = !isDestroying,
        exit = scaleOut() + fadeOut()
      ) {
        FruitTile(
          fruitType = fruitType,
          size = tileSize,
          isClickable = false
        )
      }
    } else {
      // Empty slot placeholder
      Box(
        modifier = Modifier
          .size(tileSize)
          .clip(RoundedCornerShape(10.dp))
          .border(
            width = 1.5.dp,
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp)
          )
          .background(Color.White.copy(alpha = 0.05f))
      )
    }
  }
}
