package com.fruitpuzzle.game.model

/**
 * All available fruit and gem types for the puzzle.
 * Level difficulty scales by unlocking more types progressively.
 */
enum class FruitType(val emoji: String, val label: String) {
  // --- Fruits ---
  APPLE("🍎", "Apple"),
  ORANGE("🍊", "Orange"),
  LEMON("🍋", "Lemon"),
  GRAPE("🍇", "Grape"),
  STRAWBERRY("🍓", "Strawberry"),
  BLUEBERRY("🫐", "Blueberry"),
  PEACH("🍑", "Peach"),
  CHERRY("🍒", "Cherry"),
  BANANA("🍌", "Banana"),
  WATERMELON("🍉", "Watermelon"),
  KIWI("🥝", "Kiwi"),
  PINEAPPLE("🍍", "Pineapple"),
  MANGO("🥭", "Mango"),
  PEAR("🍐", "Pear"),
  COCONUT("🥥", "Coconut"),

  // --- Gems ---
  DIAMOND("💎", "Diamond"),
  BLUE_GEM("💠", "Blue Gem"),
  RUBY("❤️‍🔥", "Ruby"),
  EMERALD("🟢", "Emerald"),
  AMETHYST("🟣", "Amethyst"),
  TOPAZ("🟡", "Topaz");

  companion object {
    /** Total count of all available types. */
    val TOTAL_COUNT: Int = entries.size

    /**
     * Returns the subset of fruit types available for a given level.
     * Difficulty scales by unlocking more types.
     *
     * - Level 1-2:   4 types
     * - Level 3-4:   6 types
     * - Level 5-6:   8 types
     * - Level 7-8:  10 types
     * - Level 9-10: 13 types
     * - Level 11-14: 16 types
     * - Level 15+:  all 21 types
     */
    fun availableForLevel(level: Int): List<FruitType> {
      val count = when {
        level <= 2 -> 4
        level <= 4 -> 6
        level <= 6 -> 8
        level <= 8 -> 10
        level <= 10 -> 13
        level <= 14 -> 16
        else -> TOTAL_COUNT
      }
      return entries.take(count)
    }
  }
}
