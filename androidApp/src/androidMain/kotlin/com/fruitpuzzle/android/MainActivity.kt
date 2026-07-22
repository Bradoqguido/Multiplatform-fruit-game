package com.fruitpuzzle.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fruitpuzzle.game.platform.initSettings
import com.fruitpuzzle.game.ui.App

/**
 * Android entry point — initializes platform settings and delegates entirely
 * to the shared Compose UI defined in [App].
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initSettings(applicationContext)
    setContent {
      App()
    }
  }
}
