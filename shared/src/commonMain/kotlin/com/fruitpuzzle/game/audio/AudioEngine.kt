package com.fruitpuzzle.game.audio

/**
 * Cross-platform Audio Engine interface.
 * Handles click SFX, drop SFX, match-3 triumph SFX, and ambient background music (BGM).
 * Guaranteed 100% offline, lightweight, and zero UI thread latency.
 */
expect object AudioEngine {
  fun playClickSfx(volume: Float = 0.8f, isMuted: Boolean = false)
  fun playDropSfx(volume: Float = 0.8f, isMuted: Boolean = false)
  fun playMatchSfx(volume: Float = 0.8f, isMuted: Boolean = false)
  fun startBgMusic(volume: Float = 0.5f, isMuted: Boolean = false)
  fun stopBgMusic()
  fun updateBgmVolume(volume: Float = 0.5f, isMuted: Boolean = false)
}
