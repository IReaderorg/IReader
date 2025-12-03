import UIKit
import SwiftUI
import presentation

/// UIViewController that hosts Compose Multiplatform UI
/// This bridges the Kotlin Compose UI to iOS UIKit
class ComposeViewController: UIViewController {
    
    private var composeVC: UIViewController?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Create the Compose UI view controller
        composeVC = IosMainViewControllerKt.MainViewController()
        
        guard let composeVC = composeVC else {
            showFallbackUI()
            return
        }
        
        // Add as child view controller
        addChild(composeVC)
        view.addSubview(composeVC.view)
        composeVC.view.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            composeVC.view.topAnchor.constraint(equalTo: view.topAnchor),
            composeVC.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            composeVC.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            composeVC.view.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])
        
        composeVC.didMove(toParent: self)
    }
    
    private func showFallbackUI() {
        view.backgroundColor = .systemBackground
        
        let label = UILabel()
        label.text = "Failed to load Compose UI"
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
