package com.fruitpuzzle.game.model

/**
 * All available fruit and gem types for the puzzle.
 * Level difficulty scales by unlocking more types progressively.
 * Uses universal Unicode 6.0 emojis for guaranteed cross-platform rendering.
 */
enum class FruitType(val emoji: String, val label: String) {
  // --- Frutas ---
  APPLE("🍎", "Maçã"),
  GREEN_APPLE("🍏", "Maçã Verde"),
  ORANGE("🍊", "Laranja"),
  LEMON("🍋", "Limão"),
  GRAPE("🍇", "Uva"),
  STRAWBERRY("🍓", "Morango"),
  MELON("🍈", "Melão"),
  PEACH("🍑", "Pêssego"),
  CHERRY("🍒", "Cereja"),
  BANANA("🍌", "Banana"),
  WATERMELON("🍉", "Melancia"),
  PINEAPPLE("🍍", "Abacaxi"),
  PEAR("🍐", "Pêra"),

  // --- Gemas ---
  DIAMOND("💎", "Diamante"),
  RUBY("🔴", "Rubi"),
  SAPPHIRE("🔵", "Safira"),
  EMERALD("❇️", "Esmeralda"),
  AMETHYST("🔮", "Ametista"),
  TOPAZ("🌟", "Topázio"),
  BLUE_GEM("💠", "Gema Azul"),
  AMBER("🔶", "Âmbar");

  companion object {
    /** Total count of all available types. */
    val TOTAL_COUNT: Int = entries.size

    /**
     * Returns the subset of fruit types available for a given level.
     * Difficulty scales by unlocking more types.
     */
    fun availableForLevel(level: Int): List<FruitType> {
      // Level 1 starts with 4 types, adding +1 variation per level up to max (21 types at level 18)
      val count = (3 + level).coerceAtMost(TOTAL_COUNT)
      return entries.take(count)
    }
  }
}

