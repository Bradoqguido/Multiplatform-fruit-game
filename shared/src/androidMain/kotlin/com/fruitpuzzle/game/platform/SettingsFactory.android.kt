package com.fruitpuzzle.game.platform

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import android.content.Context

/**
 * Android implementation uses SharedPreferences via multiplatform-settings.
 * Context must be provided before first use.
 */
private var appContext: Context? = null

fun initSettings(context: Context) {
  appContext = context.applicationContext
}

actual fun createSettings(): Settings {
  val context = appContext
    ?: throw IllegalStateException("Call initSettings(context) before createSettings()")
  val prefs = context.getSharedPreferences("fruit_puzzle_prefs", Context.MODE_PRIVATE)
  return SharedPreferencesSettings(prefs)
}
