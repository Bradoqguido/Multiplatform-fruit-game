package com.fruitpuzzle.game.logic

import com.fruitpuzzle.game.model.BoardTile
import com.fruitpuzzle.game.model.FruitType
import kotlin.random.Random

/**
 * Procedural level generator using the "Reverse Play" (backwards generation) algorithm.
 *
 * **How it works:**
 * 1. Start from the solved state (empty board).
 * 2. For each group, pick a random fruit type and place 3 tiles in a cluster.
 * 3. Tiles are layered so some overlap, creating the puzzle's depth.
 * 4. Since every group of 3 was explicitly placed, playing forward always has a solution.
 *
 * **Board grid:**
 * - Default: 6 columns × 8 rows (phone)
 * - Tablet: 8 columns × 10 rows
 *
 * **Layer strategy:**
 * - Max layers scale with difficulty: min(5, 2 + level / 4)
 * - Higher layers placed on top of lower ones at offset positions
 */
object LevelGenerator {

  /** Default board dimensions for phone-sized screens. */
  const val DEFAULT_COLUMNS: Int = 6
  const val DEFAULT_ROWS: Int = 8

  /** Tablet board dimensions. */
  const val TABLET_COLUMNS: Int = 8
  const val TABLET_ROWS: Int = 10

  /**
   * Generates a solvable board for the given level.
   *
   * @param level      Current level number (1-based).
   * @param columns    Board grid width.
   * @param rows       Board grid height.
   * @param seed       Optional random seed for reproducible levels.
   * @return           List of [BoardTile] with unique IDs, positions, and layers.
   */
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

    // Track occupied cells per layer to avoid exact overlaps within the same layer
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

  /**
   * Calculates number of groups (each group = 3 identical tiles).
   * Tile count = 6 + (level - 1) * 6, all divisible by 3.
   */
  internal fun calculateGroupCount(level: Int): Int {
    val tileCount = 6 + (level - 1) * 6
    return tileCount / 3
  }

  /**
   * Calculates maximum layer depth for the given level.
   * Caps at 5 layers for the hardest levels.
   */
  internal fun calculateMaxLayers(level: Int): Int {
    return minOf(5, 2 + level / 4)
  }

  /**
   * Places a group of 3 identical tiles in a cluster pattern.
   * Tiles within a group are placed in adjacent/overlapping positions
   * potentially spanning multiple layers.
   */
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

    // Pick a cluster anchor point (leaving margin for adjacent placement)
    val anchorX = random.nextInt(maxOf(1, columns - 2))
    val anchorY = random.nextInt(maxOf(1, rows - 2))

    // Offsets for the 3 tiles within a cluster
    val offsets = generateClusterOffsets(random)

    for (i in 0 until 3) {
      val gridX = (anchorX + offsets[i].first).coerceIn(0, columns - 1)
      val gridY = (anchorY + offsets[i].second).coerceIn(0, rows - 1)

      // Assign layer: try to spread across layers based on difficulty
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

  /**
   * Generates 3 offset pairs for cluster placement.
   * Creates patterns like L-shapes, lines, or tight squares.
   */
  private fun generateClusterOffsets(random: Random): List<Pair<Int, Int>> {
    val patterns = listOf(
      // Horizontal line
      listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
      // Vertical line
      listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
      // L-shape right
      listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1)),
      // L-shape left
      listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1)),
      // L-shape down
      listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1)),
      // Diagonal step
      listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1)),
      // Tight triangle
      listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1)),
      // Spread horizontal
      listOf(Pair(0, 0), Pair(1, 0), Pair(2, 1)),
    )
    return patterns[random.nextInt(patterns.size)]
  }

  /**
   * Picks a layer for a tile at the given grid position.
   * Prefers placing on a layer where the cell is already occupied by lower layers
   * (creating actual visual overlap). Falls back to layer 0 if all layers are full.
   */
  private fun pickLayer(
    gridX: Int,
    gridY: Int,
    maxLayers: Int,
    occupiedCells: MutableMap<Int, MutableSet<Pair<Int, Int>>>,
    random: Random
  ): Int {
    val cell = Pair(gridX, gridY)

    // 60% chance to try a higher layer if the cell is occupied at a lower layer
    if (random.nextFloat() < 0.6f) {
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

    // Find the lowest available layer for this cell
    for (layer in 0..maxLayers) {
      if (occupiedCells[layer]?.contains(cell) != true) {
        return layer
      }
    }

    // All layers occupied at this cell — use layer 0 (will visually stack)
    return 0
  }

  /**
   * Determines which tiles are clickable (not blocked by tiles on higher layers).
   * A tile is clickable if no other visible tile shares its grid cell at a higher layer.
   *
   * @param board The current board state.
   * @return Set of tile IDs that are clickable.
   */
  fun calculateClickableTiles(board: List<BoardTile>): Set<Int> {
    val visibleTiles = board.filter { it.isVisible }

    // Group by grid position, find the max layer at each position
    val maxLayerByCell = mutableMapOf<Pair<Int, Int>, Int>()
    for (tile in visibleTiles) {
      val cell = Pair(tile.gridX, tile.gridY)
      val currentMax = maxLayerByCell[cell]
      if (currentMax == null || tile.layer > currentMax) {
        maxLayerByCell[cell] = tile.layer
      }
    }

    // A tile is clickable if it's at the max layer for its cell
    return visibleTiles
      .filter { tile ->
        val cell = Pair(tile.gridX, tile.gridY)
        tile.layer == maxLayerByCell[cell]
      }
      .map { it.id }
      .toSet()
  }
}
