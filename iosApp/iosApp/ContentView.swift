import SwiftUI
import UIKit
import presentation

/// SwiftUI wrapper for the Compose Multiplatform UI
/// This is used as a fallback or for SwiftUI integration scenarios
struct ContentView: View {
    var body: some View {
        ComposeViewControllerRepresentable()
            .ignoresSafeArea()
    }
}

/// UIViewControllerRepresentable wrapper for the Compose UI
struct ComposeViewControllerRepresentable: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return IosMainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // No updates needed
    }
}

// MARK: - Preview
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
