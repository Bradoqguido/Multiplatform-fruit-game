package com.fruitpuzzle.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fruitpuzzle.game.model.FruitType

/**
 * Individual fruit 3D Mahjong brick tile.
 * Renders extruded 3D right & bottom side walls, top face highlights, inner bevel rims, and soft drop shadows.
 */
@Composable
fun FruitTile(
  fruitType: FruitType,
  size: Dp = 48.dp,
  isClickable: Boolean = true,
  isBlocked: Boolean = false,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val baseBgColor = fruitBackgroundColor(fruitType)
  val canClick = isClickable && !isBlocked

  // 3D Extrusion Depth (right & bottom side wall thickness)
  val depthX = (size.value * 0.12f).dp // ~5.8dp for 48dp tile
  val depthY = (size.value * 0.15f).dp // ~7.2dp for 48dp tile

  val mainColor = if (isBlocked) baseBgColor.copy(alpha = 0.65f) else baseBgColor
  val rightWallColor = darkenColor(mainColor, 0.30f)
  val bottomWallColor = darkenColor(mainColor, 0.45f)
  val baseCornerColor = darkenColor(mainColor, 0.60f)

  val brickShape = RoundedCornerShape(14.dp)
  val topFaceShape = RoundedCornerShape(topStart = 14.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 8.dp)

  Box(
    modifier = modifier
      .size(size)
      .shadow(
        elevation = if (canClick) 8.dp else 2.dp,
        shape = brickShape,
        ambientColor = Color.Black.copy(alpha = 0.5f),
        spotColor = Color.Black.copy(alpha = 0.6f)
      )
      .then(if (canClick) Modifier.clickable(onClick = onClick) else Modifier),
    contentAlignment = Alignment.TopStart
  ) {
    // 1. Extruded 3D Side Walls (Canvas drawing trapezoidal side walls for real 3D depth)
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height
      val dx = depthX.toPx()
      val dy = depthY.toPx()
      val cornerR = 10.dp.toPx()

      // Base background fill (dark corner back)
      drawRoundRect(
        color = baseCornerColor,
        size = Size(w, h),
        cornerRadius = CornerRadius(cornerR, cornerR)
      )

      // Right 3D Side Wall
      val rightWall = Path().apply {
        moveTo(w - dx, cornerR)
        lineTo(w - cornerR / 2f, dy)
        lineTo(w - cornerR / 2f, h - cornerR)
        lineTo(w - dx, h - dy)
        close()
      }
      drawPath(rightWall, color = rightWallColor)

      // Bottom 3D Side Wall
      val bottomWall = Path().apply {
        moveTo(cornerR, h - dy)
        lineTo(dx, h - cornerR / 2f)
        lineTo(w - cornerR, h - cornerR / 2f)
        lineTo(w - dx, h - dy)
        close()
      }
      drawPath(bottomWall, color = bottomWallColor)
    }

    // 2. Raised Top Face Plate (Shifted top-left to expose 3D side walls)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(end = depthX, bottom = depthY)
        .clip(topFaceShape)
        .background(
          Brush.verticalGradient(
            colors = listOf(
              mainColor.lighten(0.35f),
              mainColor,
              mainColor.darken(0.18f)
            )
          )
        )
        .border(
          width = 1.5.dp,
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.90f),
              Color.White.copy(alpha = 0.35f),
              Color.Black.copy(alpha = 0.35f)
            )
          ),
          shape = topFaceShape
        ),
      contentAlignment = Alignment.Center
    ) {
      // Inner embossed rim highlight
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(2.5.dp)
          .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.25f),
            shape = RoundedCornerShape(10.dp)
          )
      )

      Text(
        text = fruitType.emoji,
        fontSize = (size.value * 0.46f).sp
      )

      // Dark shading overlay if tile is blocked by higher layers
      if (isBlocked) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.40f))
        )
      }
    }
  }
}

/**
 * Color manipulation helpers for 3D bevel effects.
 */
private fun darkenColor(color: Color, factor: Float = 0.35f): Color {
  return Color(
    red = color.red * (1f - factor),
    green = color.green * (1f - factor),
    blue = color.blue * (1f - factor),
    alpha = color.alpha
  )
}

private fun Color.lighten(factor: Float = 0.15f): Color {
  return Color(
    red = (red + (1f - red) * factor).coerceIn(0f, 1f),
    green = (green + (1f - green) * factor).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
    alpha = alpha
  )
}

private fun Color.darken(factor: Float = 0.15f): Color {
  return darkenColor(this, factor)
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
