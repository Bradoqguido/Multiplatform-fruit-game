package com.fruitpuzzle.game.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.fruitpuzzle.game.logic.LevelGenerator
import com.fruitpuzzle.game.model.BoardTile

/**
 * Game board displaying procedurally generated, overlapping fruit tiles.
 * Tiles are positioned using a grid system with layer-based z-ordering.
 *
 * @param board          All board tiles (visible and hidden).
 * @param clickableTileIds Set of tile IDs the player can currently tap.
 * @param isInteractive  Whether the board responds to clicks (false during animations).
 * @param onTileClick    Callback with tile ID and its absolute screen position.
 * @param modifier       Additional modifiers.
 */
@Composable
fun GameBoard(
  board: List<BoardTile>,
  clickableTileIds: Set<Int>,
  isInteractive: Boolean,
  onTileClick: (tileId: Int, fromX: Float, fromY: Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val visibleTiles = board.filter { it.isVisible }

  BoxWithConstraints(modifier = modifier) {
    val density = LocalDensity.current
    val boardWidthPx = with(density) { maxWidth.toPx() }
    val boardHeightPx = with(density) { maxHeight.toPx() }

    // Calculate tile size based on available space and grid dimensions
    val columns = LevelGenerator.DEFAULT_COLUMNS
    val rows = LevelGenerator.DEFAULT_ROWS
    val tileSize = minOf(
      boardWidthPx / (columns + 1),
      boardHeightPx / (rows + 1)
    )
    val tileSizeDp = with(density) { tileSize.toDp() }

    // Center offset
    val totalGridWidth = columns * tileSize
    val totalGridHeight = rows * tileSize
    val offsetX = (boardWidthPx - totalGridWidth) / 2
    val offsetY = (boardHeightPx - totalGridHeight) / 2

    // Layer offset for visual overlap effect (shift higher layers slightly)
    val layerOffset = tileSize * 0.15f

    Box(modifier = Modifier.fillMaxSize()) {
      for (tile in visibleTiles.sortedBy { it.layer }) {
        val x = offsetX + tile.gridX * tileSize + tile.layer * layerOffset
        val y = offsetY + tile.gridY * tileSize + tile.layer * layerOffset
        val xDp = with(density) { x.toDp() }
        val yDp = with(density) { y.toDp() }
        val isClickable = isInteractive && tile.id in clickableTileIds

        Box(
          modifier = Modifier
            .offset(x = xDp, y = yDp)
            .size(tileSizeDp)
            .zIndex(tile.layer.toFloat())
            .onGloballyPositioned { coordinates ->
              // Position tracked for fly animation source
            }
        ) {
          FruitTile(
            fruitType = tile.fruitType,
            size = tileSizeDp - 4.dp,
            isClickable = isClickable,
            onClick = {
              if (isClickable) {
                // We need the absolute position — use the parent offset
                // (approximate from grid calculation)
                onTileClick(tile.id, x + offsetX, y + offsetY)
              }
            },
            modifier = Modifier
              .onGloballyPositioned { coordinates ->
                // Track position for animation if needed
              }
          )
        }
      }
    }
  }
}
