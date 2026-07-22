import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.compose.multiplatform)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain by getting {
      dependencies {
        implementation(project(":shared"))
        implementation(compose.desktop.currentOs)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "com.fruitpuzzle.desktop.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

      packageName = "FruitPuzzle"
      packageVersion = "1.0.0"
      description = "Triple Match Fruit Puzzle Game"
      vendor = "FruitPuzzle"

      macOS {
        bundleID = "com.fruitpuzzle.desktop"
        appCategory = "public.app-category.puzzle-games"
      }

      windows {
        menuGroup = "FruitPuzzle"
        upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
      }

      linux {
        packageName = "fruit-puzzle"
        debMaintainer = "dev@fruitpuzzle.com"
        appCategory = "Game"
      }

      // Strip unused Java modules for smaller binary
      modules("java.base", "java.desktop", "java.logging", "java.prefs")
    }
  }
}
