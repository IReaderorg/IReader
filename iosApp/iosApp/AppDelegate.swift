import UIKit
import SwiftUI
import iosBuildCheck

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Initialize Koin for dependency injection
        // Note: When presentation framework is linked, uncomment:
        // IosKoinInitKt.initKoin(additionalModules: [])
        
        window = UIWindow(frame: UIScreen.main.bounds)
        
        // Use SwiftUI ContentView for now
        // When Compose UI is ready, switch to:
        // let composeVC = IosMainViewControllerKt.MainViewController()
        // window?.rootViewController = composeVC
        
        let contentView = ContentView()
        window?.rootViewController = UIHostingController(rootView: contentView)
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
