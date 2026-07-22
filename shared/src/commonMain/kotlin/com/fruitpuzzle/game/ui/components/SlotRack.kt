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

/**
 * The 7-slot rack at the top of the game screen.
 * Shows occupied slots with fruit tiles and empty slots as bordered placeholders.
 * Handles destruction animation when 3 identical fruits match.
 */
@Composable
fun SlotRack(
  rack: List<RackSlot>,
  destroyingIndices: List<Int>,
  isDestroyPhase: Boolean,
  onDestroyComplete: () -> Unit,
  onSlotPositioned: (index: Int, x: Float, y: Float) -> Unit,
  modifier: Modifier = Modifier
) {
  // Trigger destroy callback after animation completes
  if (isDestroyPhase) {
    LaunchedEffect(destroyingIndices) {
      delay(500) // match animation duration
      onDestroyComplete()
    }
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFF2A2A4A).copy(alpha = 0.7f))
      .padding(horizontal = 8.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (slot in rack) {
      val isDestroying = slot.index in destroyingIndices

      // Each slot rendered via standalone composable to avoid RowScope receiver conflict
      SlotContent(
        fruitType = slot.fruitType,
        slotIndex = slot.index,
        isDestroying = isDestroying,
        onSlotPositioned = onSlotPositioned
      )
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
  onSlotPositioned: (index: Int, x: Float, y: Float) -> Unit
) {
  Box(
    modifier = Modifier
      .size(46.dp)
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
          size = 42.dp,
          isClickable = false
        )
      }
    } else {
      // Empty slot placeholder
      Box(
        modifier = Modifier
          .size(42.dp)
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
