package com.fruitpuzzle.game.platform

actual fun exitApp() {
  // No-op on Android — OS handles back-stack lifecycle
}

actual val showExitButton: Boolean = false
