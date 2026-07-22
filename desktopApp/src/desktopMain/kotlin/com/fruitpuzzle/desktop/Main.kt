package com.fruitpuzzle.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.fruitpuzzle.game.ui.App

/**
 * Desktop entry point — launches a windowed Compose application.
 */
fun main() = application {
  Window(
    title = "Jogo das Frutas - Trinca de Frutas",
    state = rememberWindowState(width = 420.dp, height = 800.dp),
    onCloseRequest = ::exitApplication
  ) {
    App()
  }
}
