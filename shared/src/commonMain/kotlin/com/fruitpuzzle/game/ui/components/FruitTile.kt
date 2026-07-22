package com.fruitpuzzle.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fruitpuzzle.game.model.FruitType

/**
 * Individual fruit tile rendered on the game board or in the rack.
 *
 * @param fruitType   The fruit/gem to display.
 * @param size        Tile size in dp.
 * @param isClickable Whether the tile responds to clicks.
 * @param onClick     Callback when tile is tapped.
 * @param modifier    Additional modifiers.
 */
@Composable
fun FruitTile(
  fruitType: FruitType,
  size: Dp = 48.dp,
  isClickable: Boolean = true,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val bgColor = fruitBackgroundColor(fruitType)

  Box(
    modifier = modifier
      .size(size)
      .shadow(
        elevation = if (isClickable) 4.dp else 1.dp,
        shape = RoundedCornerShape(12.dp),
        ambientColor = bgColor.copy(alpha = 0.3f)
      )
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .then(
        if (isClickable) Modifier.clickable(onClick = onClick) else Modifier
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = fruitType.emoji,
      fontSize = (size.value * 0.55f).sp
    )
  }
}

/**
 * Maps each fruit type to a distinct background color for visual differentiation.
 */
private fun fruitBackgroundColor(fruitType: FruitType): Color {
  return when (fruitType) {
    FruitType.APPLE -> Color(0xFFFFCDD2)
    FruitType.GREEN_APPLE -> Color(0xFFDCEDC8)
    FruitType.ORANGE -> Color(0xFFFFE0B2)
    FruitType.LEMON -> Color(0xFFFFF9C4)
    FruitType.GRAPE -> Color(0xFFE1BEE7)
    FruitType.STRAWBERRY -> Color(0xFFF8BBD0)
    FruitType.MELON -> Color(0xFFC8E6C9)
    FruitType.PEACH -> Color(0xFFFFCCBC)
    FruitType.CHERRY -> Color(0xFFEF9A9A)
    FruitType.BANANA -> Color(0xFFFFF59D)
    FruitType.WATERMELON -> Color(0xFFFFCDD2)
    FruitType.PINEAPPLE -> Color(0xFFFFECB3)
    FruitType.PEAR -> Color(0xFFE8F5E9)
    FruitType.DIAMOND -> Color(0xFFB3E5FC)
    FruitType.RUBY -> Color(0xFFFF8A80)
    FruitType.SAPPHIRE -> Color(0xFF90CAF9)
    FruitType.EMERALD -> Color(0xFFA5D6A7)
    FruitType.AMETHYST -> Color(0xFFCE93D8)
    FruitType.TOPAZ -> Color(0xFFFFD54F)
    FruitType.BLUE_GEM -> Color(0xFF80DEEA)
    FruitType.AMBER -> Color(0xFFFFCC80)
  }
}
