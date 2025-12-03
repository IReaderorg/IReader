# IReader iOS Setup Guide

This guide explains how to build and run the IReader iOS app.

## Prerequisites

- macOS 13.0+ (Ventura or later)
- Xcode 15.0+
- iOS 15.0+ deployment target
- JDK 17+
- Gradle 8.0+

## Project Structure

```
iosApp/                          # iOS app module
├── iosApp.xcodeproj/           # Xcode project
├── iosApp/
│   ├── AppDelegate.swift       # App entry point
│   ├── SceneDelegate.swift     # Scene lifecycle
│   ├── ContentView.swift       # Main SwiftUI view
│   ├── ComposeViewController.swift  # Compose UI bridge
│   ├── Info.plist              # App configuration
│   └── Assets.xcassets/        # App icons and assets
├── Podfile                     # CocoaPods dependencies
└── README.md

presentation/src/iosMain/       # iOS-specific Kotlin code
├── kotlin/ireader/presentation/
│   ├── IosMainViewController.kt    # Compose UI entry point
│   ├── IosKoinInit.kt              # Koin initialization
│   └── ...                         # Platform implementations

presentation-core/src/iosMain/  # iOS-specific core code
```

## Building the iOS Frameworks

### 1. Build Kotlin Frameworks

```bash
# Build for iOS Simulator (Apple Silicon Mac)
./gradlew :presentation:linkDebugFrameworkIosSimulatorArm64
./gradlew :presentation-core:linkDebugFrameworkIosSimulatorArm64

# Build for iOS Simulator (Intel Mac)
./gradlew :presentation:linkDebugFrameworkIosX64
./gradlew :presentation-core:linkDebugFrameworkIosX64

# Build for physical iOS devices
./gradlew :presentation:linkDebugFrameworkIosArm64
./gradlew :presentation-core:linkDebugFrameworkIosArm64

# Build release versions
./gradlew :presentation:linkReleaseFrameworkIosArm64
```

Or use the convenience script:
```bash
./scripts/build-ios-framework.sh
```

### 2. Open Xcode Project

```bash
open iosApp/iosApp.xcodeproj
```

### 3. Configure Framework Search Paths

In Xcode, go to your target's Build Settings and add:

**Framework Search Paths:**
```
$(SRCROOT)/../presentation/build/bin/iosSimulatorArm64/debugFramework
$(SRCROOT)/../presentation-core/build/bin/iosSimulatorArm64/debugFramework
$(SRCROOT)/../ios-build-check/build/bin/iosSimulatorArm64/debugFramework
```

### 4. Link Frameworks

In Xcode, go to your target's General tab and add the frameworks:
- `presentation.framework`
- `presentation_core.framework`
- `iosBuildCheck.framework`

### 5. Build and Run

Select your target device/simulator and press ⌘R.

## Using Compose UI from Swift

### Initialize Koin

In `AppDelegate.swift`:
```swift
import presentation

func application(_ application: UIApplication, 
                 didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    // Initialize Koin
    IosKoinInitKt.initKoin(additionalModules: [])
    return true
}
```

### Display Compose UI

```swift
import presentation

// Get the Compose UI ViewController
let composeVC = IosMainViewControllerKt.MainViewController()

// Present it
window?.rootViewController = composeVC
```

### SwiftUI Integration

```swift
import SwiftUI
import presentation

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return IosMainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// Use in SwiftUI
struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
```

## Troubleshooting

### Framework Not Found

1. Ensure you've built the frameworks with Gradle
2. Check Framework Search Paths in Xcode
3. Clean build folder (⇧⌘K) and rebuild

### Linker Errors

1. Ensure all frameworks are linked in General > Frameworks
2. Check that the framework architectures match your target

### Runtime Crashes

1. Ensure Koin is initialized before using any Kotlin code
2. Check that all expect/actual declarations have iOS implementations

## Architecture Notes

The iOS app uses a hybrid architecture:

1. **SwiftUI** - Native iOS UI for platform-specific features
2. **Compose Multiplatform** - Shared UI from the presentation module
3. **Koin** - Dependency injection (shared with Android/Desktop)
4. **Kotlin/Native** - Shared business logic

The `ContentView.swift` provides a fallback SwiftUI interface that mirrors the Compose UI structure, allowing the app to function even without the Compose framework linked.

## App Icons

App icons are located in `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`. The icons were generated from the source files in `ios-icon/`.

## Permissions

The app requests the following permissions (configured in Info.plist):
- **NSPhotoLibraryUsageDescription** - For character art selection
- **NSPhotoLibraryAddUsageDescription** - For saving images
- **NSCameraUsageDescription** - For capturing images
- **UIFileSharingEnabled** - For file access via Files app
- **UISupportsDocumentBrowser** - For document picker
