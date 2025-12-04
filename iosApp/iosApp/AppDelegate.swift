import UIKit
import SwiftUI
import presentation

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Initialize Koin for dependency injection
        IosEntryPointsKt.doInitKoin()
        
        window = UIWindow(frame: UIScreen.main.bounds)
        
        // Use Compose Multiplatform UI
        let composeVC = IosEntryPointsKt.createMainViewController()
        window?.rootViewController = composeVC
        window?.makeKeyAndVisible()
        
        return true
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(
        _ application: UIApplication,
        didDiscardSceneSessions sceneSessions: Set<UISceneSession>
    ) {
    }
}
