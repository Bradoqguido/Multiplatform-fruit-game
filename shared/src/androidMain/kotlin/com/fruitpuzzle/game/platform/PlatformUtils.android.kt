package com.fruitpuzzle.game.platform

import kotlin.system.exitProcess

actual fun exitApp() {
  exitProcess(0)
}

actual val showExitButton: Boolean = true
