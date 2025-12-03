import UIKit
import SwiftUI
import iosBuildCheck

/// UIViewController that hosts Compose Multiplatform UI
/// This bridges the Kotlin Compose UI to iOS UIKit
class ComposeViewController: UIViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // The Compose UI will be rendered here when the presentation module
        // is properly linked. For now, show a placeholder.
        view.backgroundColor = .systemBackground
        
        let label = UILabel()
        label.text = "Compose UI Loading..."
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(label)
        NSLayoutConstraint.activate([
            label.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            label.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
    }
}

/// SwiftUI wrapper for ComposeViewController
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> ComposeViewController {
        return ComposeViewController()
    }
    
    func updateUIViewController(_ uiViewController: ComposeViewController, context: Context) {
        // Update the view controller if needed
    }
}

/// Extension to create a SwiftUI view from Compose
extension View {
    func composeOverlay() -> some View {
        self.overlay(
            ComposeView()
                .ignoresSafeArea()
        )
    }
}
