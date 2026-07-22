package com.fruitpuzzle.game.platform

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual fun createSettings(): Settings {
  val delegate = Preferences.userRoot().node("com.fruitpuzzle.game")
  return PreferencesSettings(delegate)
}
