@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.fruitpuzzle.game.platform

import kotlin.system.exitProcess

actual fun exitApp() {
  exitProcess(0)
}

actual val showExitButton: Boolean = true
