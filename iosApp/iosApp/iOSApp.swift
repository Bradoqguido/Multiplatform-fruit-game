import SwiftUI
import shared

/// iOS entry point — launches the shared Compose UI via ComposeView.
@main
struct iOSApp: SwiftUI.App {
  var body: some Scene {
    WindowGroup {
      ContentView()
        .ignoresSafeArea()
    }
  }
}
