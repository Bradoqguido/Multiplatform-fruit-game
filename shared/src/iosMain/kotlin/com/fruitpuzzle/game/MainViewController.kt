package com.fruitpuzzle.game

import androidx.compose.ui.window.ComposeUIViewController
import com.fruitpuzzle.game.ui.App

/**
 * iOS factory function called from Swift to create the ComposeUIViewController.
 */
fun MainViewController() = ComposeUIViewController { App() }
