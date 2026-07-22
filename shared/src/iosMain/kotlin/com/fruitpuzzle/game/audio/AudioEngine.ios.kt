package com.fruitpuzzle.game.audio

actual object AudioEngine {
  actual fun playClickSfx(volume: Float, isMuted: Boolean) {}
  actual fun playDropSfx(volume: Float, isMuted: Boolean) {}
  actual fun playMatchSfx(volume: Float, isMuted: Boolean) {}
  actual fun startBgMusic(volume: Float, isMuted: Boolean) {}
  actual fun stopBgMusic() {}
  actual fun updateBgmVolume(volume: Float, isMuted: Boolean) {}
}
