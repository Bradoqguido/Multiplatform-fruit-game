package com.fruitpuzzle.game.platform

actual fun exitApp() {
  // No-op on iOS — OS handles app lifecycle via back-stack
}

actual val showExitButton: Boolean = false
