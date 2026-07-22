package com.fruitpuzzle.game.platform

import platform.posix.exit

actual fun exitApp() {
  exit(0)
}

actual val showExitButton: Boolean = true
