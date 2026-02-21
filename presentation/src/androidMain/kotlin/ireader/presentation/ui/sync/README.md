# Android Sync Foreground Service

## Overview

The `SyncForegroundService` is an Android foreground service that keeps sync operations running in the background while displaying a persistent notification to the user.

## Components

### 1. SyncForegroundService
The main Android Service class that:
- Runs as a foreground service (required for Android 8.0+)
- Creates and manages a notification channel
- Shows a persistent notification during sync
- Updates the notification with progress
- Stops when sync completes or is cancelled

### 2. SyncServiceController
Platform-agnostic interface for controlling the service:
- **Common**: Defines the expect interface
- **Android**: Actual implementation that controls SyncForegroundService
- **Desktop/iOS**: No-op implementations (not needed on these platforms)

### 3. SyncServiceManager
Common state manager for tracking service state across platforms.

## Integration with SyncViewModel

The `SyncViewModel` automatically manages the service lifecycle:

1. **Sync Starts**: When `SyncStatus.Syncing` is emitted, the service starts
2. **Progress Updates**: As sync progresses, the notification updates
3. **Sync Completes**: When `SyncStatus.Completed` or `SyncStatus.Failed` is emitted, the service stops
4. **Sync Cancelled**: When user cancels, the service stops immediately

## Permissions

The following permissions are declared in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Service Declaration

```xml
<service
    android:name="ireader.presentation.ui.sync.SyncForegroundService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="dataSync" />
```

## Usage

### From ViewModel (Automatic)

The service is automatically managed by `SyncViewModel` when you inject `SyncServiceController`:

```kotlin
// In Koin module
factory { 
    SyncViewModel(
        // ... other dependencies
        serviceController = SyncServiceController(androidContext())
    )
}
```

### Manual Control (Advanced)

If you need to control the service manually:

```kotlin
// Start sync
SyncForegroundService.startSync(context, "Device Name")

// Update progress
SyncForegroundService.updateProgress(context, 50, "Book.epub")

// Stop sync
SyncForegroundService.stopSync(context)

// Cancel sync
SyncForegroundService.cancelSync(context)
```

## Testing

Tests are located in:
- `presentation/src/commonTest/kotlin/ireader/presentation/ui/sync/SyncServiceManagerTest.kt`

The tests follow TDD methodology and were written before implementation.

## Android Version Requirements

- **Minimum**: Android 7.0 (API 24)
- **Foreground Service**: Android 8.0+ (API 26)
- **Notification Channel**: Android 8.0+ (API 26)
- **Foreground Service Type**: Android 10+ (API 29)

## Notification

The notification shows:
- **Title**: "WiFi Sync in Progress"
- **Content**: "Syncing with [Device Name]..." or "Syncing: [Item] (X%)"
- **Icon**: System sync icon (android.R.drawable.stat_notify_sync)
- **Progress Bar**: Shows sync progress (0-100%)
- **Priority**: Low (doesn't interrupt user)
- **Category**: Service
- **Ongoing**: Cannot be dismissed while sync is active

## Future Enhancements

1. Add notification action to cancel sync
2. Add notification action to open sync screen
3. Show completion notification with summary
4. Add error notification for failed syncs
5. Support for multiple concurrent syncs (if needed)
