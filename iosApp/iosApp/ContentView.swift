import SwiftUI
import UIKit
import presentation

/// SwiftUI wrapper for the Compose Multiplatform UI
struct ContentView: View {
    var body: some View {
        ComposeViewControllerRepresentable()
            .ignoresSafeArea()
    }
}

/// UIViewControllerRepresentable wrapper for the Compose UI
struct ComposeViewControllerRepresentable: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed
    }
}
