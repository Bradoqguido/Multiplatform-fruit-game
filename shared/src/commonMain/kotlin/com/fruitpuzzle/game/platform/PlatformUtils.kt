package com.fruitpuzzle.game.platform

/**
 * Exits the application.
 * Desktop: calls System.exit(0).
 * Mobile: no-op (OS handles lifecycle).
 */
expect fun exitApp()

/**
 * Whether to show the "Exit" button on the Start Screen.
 * true on Desktop, false on mobile (back-stack handles exit).
 */
expect val showExitButton: Boolean
