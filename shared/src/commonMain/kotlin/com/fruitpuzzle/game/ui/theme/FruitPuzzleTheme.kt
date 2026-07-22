package com.fruitpuzzle.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Custom Colors ───────────────────────────────
private val FruitGreen = Color(0xFF2E7D32)
private val FruitGreenLight = Color(0xFF4CAF50)
private val FruitOrange = Color(0xFFFF9800)
private val FruitPink = Color(0xFFE91E63)
private val FruitPurple = Color(0xFF9C27B0)
private val SurfaceDark = Color(0xFF1A1A2E)
private val SurfaceLight = Color(0xFFFFF8E1)
private val OnSurfaceDark = Color(0xFFE0E0E0)
private val BackgroundDark = Color(0xFF0F0F23)
private val BackgroundLight = Color(0xFFFFFDE7)

private val DarkColorScheme = darkColorScheme(
  primary = FruitGreenLight,
  secondary = FruitOrange,
  tertiary = FruitPurple,
  background = BackgroundDark,
  surface = SurfaceDark,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = OnSurfaceDark,
  onSurface = OnSurfaceDark,
  error = FruitPink
)

private val LightColorScheme = lightColorScheme(
  primary = FruitGreen,
  secondary = FruitOrange,
  tertiary = FruitPurple,
  background = BackgroundLight,
  surface = SurfaceLight,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color(0xFF1B1B1B),
  onSurface = Color(0xFF1B1B1B),
  error = FruitPink
)

/**
 * Fruit Puzzle theme wrapping Material 3 with fruit-inspired color palette.
 */
@Composable
fun FruitPuzzleTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    content = content
  )
}
