package com.fruitpuzzle.game.platform

import com.russhwolf.settings.Settings

/**
 * Creates a platform-specific [Settings] instance for key-value persistence.
 * - Android: SharedPreferences
 * - iOS: NSUserDefaults
 * - Desktop: java.util.prefs.Preferences
 */
expect fun createSettings(): Settings
