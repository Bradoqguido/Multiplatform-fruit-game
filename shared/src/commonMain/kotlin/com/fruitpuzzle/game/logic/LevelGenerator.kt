package com.fruitpuzzle.game.logic

import com.fruitpuzzle.game.model.BoardTile
import com.fruitpuzzle.game.model.FruitType
import kotlin.random.Random

/**
 * Procedural level generator using the "Reverse Play" algorithm.
 *
 * Board grid:
 * - Default: 6 columns × 8 rows (phone)
 * - Tablet: 8 columns × 10 rows
 */
object LevelGenerator {

  const val DEFAULT_COLUMNS: Int = 6
  const val DEFAULT_ROWS: Int = 8

  const val TABLET_COLUMNS: Int = 8
  const val TABLET_ROWS: Int = 10

  fun generate(
    level: Int,
    columns: Int = DEFAULT_COLUMNS,
    rows: Int = DEFAULT_ROWS,
    seed: Long? = null
  ): List<BoardTile> {
    val random = if (seed != null) Random(seed) else Random
    val availableTypes = FruitType.availableForLevel(level)
    val groupCount = calculateGroupCount(level)
    val maxLayers = calculateMaxLayers(level)

    val tiles = mutableListOf<BoardTile>()
    var nextId = 0

    val occupiedCells = mutableMapOf<Int, MutableSet<Pair<Int, Int>>>()
    for (layer in 0..maxLayers) {
      occupiedCells[layer] = mutableSetOf()
    }

    for (groupIndex in 0 until groupCount) {
      val fruitType = availableTypes[random.nextInt(availableTypes.size)]
      val groupTiles = placeGroup(
        fruitType = fruitType,
        startId = nextId,
        columns = columns,
        rows = rows,
        maxLayers = maxLayers,
        occupiedCells = occupiedCells,
        random = random
      )
      tiles.addAll(groupTiles)
      nextId += groupTiles.size
    }

    return tiles
  }

  internal fun calculateGroupCount(level: Int): Int {
    val tileCount = 6 + (level - 1) * 6
    return tileCount / 3
  }

  internal fun calculateMaxLayers(level: Int): Int {
    return minOf(5, 2 + level / 4)
  }

  private fun placeGroup(
    fruitType: FruitType,
    startId: Int,
    columns: Int,
    rows: Int,
    maxLayers: Int,
    occupiedCells: MutableMap<Int, MutableSet<Pair<Int, Int>>>,
    random: Random
  ): List<BoardTile> {
    val result = mutableListOf<BoardTile>()

    val minX = if (columns >= 6) 1 else 0
    val maxX = if (columns >= 6) columns - 3 else maxOf(0, columns - 2)

    val minY = if (rows >= 8) 1 else 0
    val maxY = if (rows >= 8) rows - 3 else maxOf(0, rows - 2)

    val anchorX = random.nextInt(minX, maxOf(minX + 1, maxX + 1))
    val anchorY = random.nextInt(minY, maxOf(minY + 1, maxY + 1))

    val offsets = generateClusterOffsets(random)

    for (i in 0 until 3) {
      val gridX = (anchorX + offsets[i].first).coerceIn(0, columns - 1)
      val gridY = (anchorY + offsets[i].second).coerceIn(0, rows - 1)

      val layer = pickLayer(gridX, gridY, maxLayers, occupiedCells, random)

      val cell = Pair(gridX, gridY)
      occupiedCells.getOrPut(layer) { mutableSetOf() }.add(cell)

      result.add(
        BoardTile(
          id = startId + i,
          fruitType = fruitType,
          gridX = gridX,
          gridY = gridY,
          layer = layer,
          isVisible = true
        )
      )
    }

    return result
  }

  private fun generateClusterOffsets(random: Random): List<Pair<Int, Int>> {
    val patterns = listOf(
      listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1)),
      listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1)),
      listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1)),
      listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
      listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
      listOf(Pair(0, 0), Pair(1, 1), Pair(0, 1))
    )
    return patterns[random.nextInt(patterns.size)]
  }

  private fun pickLayer(
    gridX: Int,
    gridY: Int,
    maxLayers: Int,
    occupiedCells: MutableMap<Int, MutableSet<Pair<Int, Int>>>,
    random: Random
  ): Int {
    val cell = Pair(gridX, gridY)

    if (random.nextFloat() < 0.7f) {
      for (layer in 1..maxLayers) {
        val belowOccupied = (0 until layer).any { l ->
          occupiedCells[l]?.contains(cell) == true
        }
        val currentFree = occupiedCells[layer]?.contains(cell) != true
        if (belowOccupied && currentFree) {
          return layer
        }
      }
    }

    for (layer in 0..maxLayers) {
      if (occupiedCells[layer]?.contains(cell) != true) {
        return layer
      }
    }

    return 0
  }

  fun calculateClickableTiles(board: List<BoardTile>): Set<Int> {
    val visibleTiles = board.filter { it.isVisible }
    val layerOffset = 0.15f
    val tileWidth = 1.0f
    val tileHeight = 1.0f

    val clickableIds = mutableSetOf<Int>()

    for (tileA in visibleTiles) {
      val xA = tileA.gridX + tileA.layer * layerOffset
      val yA = tileA.gridY + tileA.layer * layerOffset

      var maxCoveredArea = 0.0f

      val higherTiles = visibleTiles.filter { it.layer > tileA.layer }
      for (tileB in higherTiles) {
        val xB = tileB.gridX + tileB.layer * layerOffset
        val yB = tileB.gridY + tileB.layer * layerOffset

        val overlapX = maxOf(0.0f, minOf(xA + tileWidth, xB + tileWidth) - maxOf(xA, xB))
        val overlapY = maxOf(0.0f, minOf(yA + tileHeight, yB + tileHeight) - maxOf(yA, yB))
        val overlapArea = overlapX * overlapY
        maxCoveredArea = maxOf(maxCoveredArea, overlapArea)
      }

      // Tile is clickable if at least 90% exposed (covered area <= 10% or 0.10f)
      if (maxCoveredArea <= 0.10f) {
        clickableIds.add(tileA.id)
      }
    }

    return clickableIds
  }
}
