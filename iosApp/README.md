# IReader iOS App

This is the iOS application for IReader, built with Kotlin Multiplatform and Compose Multiplatform.

## Requirements

- macOS with Xcode 15.0+
- iOS 15.0+ deployment target
- CocoaPods (optional, for additional iOS dependencies)

## Building

### From Xcode

1. Build the Kotlin framework first:
   ```bash
   cd ..
   ./gradlew :presentation:linkDebugFrameworkIosSimulatorArm64
   # Or for device:
   ./gradlew :presentation:linkDebugFrameworkIosArm64
   ```

2. Open `iosApp.xcodeproj` in Xcode
3. Select your target device/simulator
4. Build and run (⌘R)

### From Command Line

```bash
# Build the presentation framework
./gradlew :presentation:linkDebugFrameworkIosSimulatorArm64

# Open Xcode
open iosApp/iosApp.xcodeproj
```

## Project Structure

```
iosApp/
├── iosApp.xcodeproj/     # Xcode project
├── iosApp/
│   ├── AppDelegate.swift # App entry point with Koin initialization
│   ├── SceneDelegate.swift
│   ├── ContentView.swift # SwiftUI wrapper for Compose UI
│   ├── ComposeViewController.swift # UIKit bridge to Compose UI
│   ├── Info.plist        # App configuration
│   └── Assets.xcassets/  # App icons and colors
├── Podfile               # CocoaPods config (optional)
└── README.md
```

## Architecture

The iOS app uses Compose Multiplatform for the entire UI:

1. **AppDelegate** initializes Koin and sets up the root view controller
2. **IosKoinInit.kt** configures all dependency injection modules
3. **IosMainViewController.kt** creates the Compose UI entry point
4. **CommonNavHost** provides shared navigation across all platforms

## Kotlin Framework Integration

```swift
import presentation

// Initialize Koin (call once at app startup)
IosKoinInitKt.initKoin(additionalModules: [])

// Get Compose UI ViewController
let composeVC = IosMainViewControllerKt.MainViewController()
window?.rootViewController = composeVC
```

## Features

- Library management with grid/list views
- Book reader with customizable settings
- Source browsing and extension management
- Download management
- Backup and restore
- Theme customization
- TTS (Text-to-Speech) using iOS AVSpeechSynthesizer

## Permissions

The app requests the following permissions:
- Photo Library (for character art)
- Camera (optional, for capturing images)
- Files (for backup/restore and EPUB import)

## Troubleshooting

### Framework not found
Make sure to build the Kotlin framework before opening Xcode:
```bash
./gradlew :presentation:linkDebugFrameworkIosSimulatorArm64
```

### Koin not initialized
Ensure `IosKoinInitKt.initKoin()` is called in AppDelegate before any Compose UI is created.

### Compose UI not showing
Check that the presentation framework is properly linked in Xcode:
1. Go to Build Phases
2. Verify "Embed Frameworks" includes the presentation framework
