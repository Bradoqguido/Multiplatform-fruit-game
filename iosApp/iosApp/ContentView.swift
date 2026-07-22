import SwiftUI
import shared

/// Wraps the shared Compose UI in a SwiftUI view using UIKit interop.
struct ContentView: View {
  var body: some View {
    ComposeViewControllerRepresentable()
      .ignoresSafeArea()
  }
}

/// UIViewControllerRepresentable wrapper for the shared ComposeViewController.
struct ComposeViewControllerRepresentable: UIViewControllerRepresentable {
  func makeUIViewController(context: Context) -> UIViewController {
    MainViewControllerKt.MainViewController()
  }

  func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    // No updates needed
  }
}
