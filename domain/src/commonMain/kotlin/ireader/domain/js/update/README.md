# JavaScript Plugin Update System

This package contains the implementation of the plugin update system for JavaScript plugins.

## Components

### JSPluginRepository
Represents a repository containing JavaScript plugins. The default repository points to the official LNReader plugins.

### PluginUpdate
Data class representing an available update with version information and download URL.

### JSPluginUpdateChecker
Core component that:
- Fetches plugin lists from repositories
- Compares versions using semantic versioning
- Downloads plugin updates
- Installs updates with automatic backup
- Supports rollback on failure

### JSPluginUpdateScheduler
Platform-specific scheduler for periodic update checks:
- **Android**: Uses WorkManager for reliable background execution
- **Desktop**: Uses ScheduledExecutorService

### JSPluginUpdateNotifier
Platform-specific notification system:
- **Android**: Uses NotificationManager with notification channels
- **Desktop**: Console logging (can be extended with system tray notifications)

### JSPluginUpdateManager
High-level manager that coordinates all update operations:
- Enable/disable auto-updates
- Manual update checks
- Install individual or all updates
- Rollback support
- Background update checking

## Usage

### Basic Setup

```kotlin
val updateChecker = JSPluginUpdateChecker(
    httpClient = httpClient,
    pluginLoader = pluginLoader,
    repositories = listOf(JSPluginRepository.default())
)

val scheduler = JSPluginUpdateScheduler(context) // Android
// or
val scheduler = JSPluginUpdateScheduler() // Desktop

val notifier = JSPluginUpdateNotifier(context) // Android
// or
val notifier = JSPluginUpdateNotifier() // Desktop

val updateManager = JSPluginUpdateManager(
    updateChecker = updateChecker,
    scheduler = scheduler,
    notifier = notifier,
    scope = coroutineScope
)
```

### Enable Auto-Updates

```kotlin
// Check every 24 hours
updateManager.enableAutoUpdate(intervalHours = 24)
```

### Manual Update Check

```kotlin
val updates = updateManager.checkForUpdates()
updates.forEach { update ->
    println("${update.pluginId}: ${update.currentVersion} → ${update.newVersion}")
}
```

### Install Updates

```kotlin
// Install single update
val success = updateManager.installUpdate(update)

// Install all updates
val results = updateManager.installAllUpdates()
```

### Rollback

```kotlin
val success = updateManager.rollbackUpdate(pluginId)
```

## Repository Format

Plugin repositories should serve a JSON file with the following format:

```json
{
  "plugins": [
    {
      "id": "plugin-id",
      "name": "Plugin Name",
      "version": "1.2.3",
      "url": "https://example.com/plugin.js",
      "changelog": "Bug fixes and improvements"
    }
  ]
}
```

## Version Comparison

The system uses semantic versioning (semver) for version comparison:
- Format: `MAJOR.MINOR.PATCH`
- Example: `1.2.3` < `1.2.4` < `1.3.0` < `2.0.0`

## Error Handling

All operations include comprehensive error handling:
- Network failures are logged and don't crash the app
- Failed updates are automatically rolled back
- Validation ensures downloaded plugins are safe
- Backup files are created before updates

## Performance

- Updates are downloaded and installed in the background
- Compiled code cache is maintained across updates
- Engine pool is preserved during updates
- User data is retained during updates
