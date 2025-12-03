# IReader iOS App

This is the iOS application for IReader, built with Kotlin Multiplatform and Compose Multiplatform.

## Requirements

- macOS with Xcode 15.0+
- iOS 15.0+ deployment target
- CocoaPods (optional, for additional iOS dependencies)

## Building

### From Xcode

1. Open `iosApp.xcodeproj` in Xcode
2. Select your target device/simulator
3. Build and run (⌘R)

The Xcode project includes a build phase that automatically compiles the Kotlin framework.

### From Command Line

```bash
# Build the presentation framework first
cd ..
./gradlew :presentation:linkDebugFrameworkIosSimulatorArm64

# Then open Xcode
open iosApp/iosApp.xcodeproj
```

## Project Structure

```
iosApp/
├── iosApp.xcodeproj/     # Xcode project
├── iosApp/
│   ├── AppDelegate.swift # App entry point with Koin initialization
│   ├── SceneDelegate.swift
│   ├── ContentView.swift # Main SwiftUI view (fallback UI)
│   ├── ComposeViewController.swift # Bridge to Compose UI
│   ├── Info.plist        # App configuration
│   └── Assets.xcassets/  # App icons and colors
├── Podfile               # CocoaPods config (optional)
└── README.md
```

## Architecture

The iOS app uses a hybrid approach:
- **SwiftUI** for native iOS UI components and navigation
- **Compose Multiplatform** for shared UI from the presentation module
- **Koin** for dependency injection (initialized in AppDelegate)

## Kotlin Framework Integration

The app can import multiple Kotlin frameworks:

```swift
import iosBuildCheck    // Build verification module
import presentation     // Main presentation layer (when linked)

// Initialize Koin
IosKoinInitKt.initKoin(additionalModules: [])

// Get Compose UI ViewController
let composeVC = IosMainViewControllerKt.MainViewController()
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
