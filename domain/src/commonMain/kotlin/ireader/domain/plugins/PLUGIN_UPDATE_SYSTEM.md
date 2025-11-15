# Plugin Update System

This document describes the plugin update system implementation for IReader.

## Overview

The plugin update system provides automatic and manual update checking, downloading, and installation of plugin updates. It includes rollback functionality, update history tracking, and error handling.

## Components

### PluginUpdateChecker

The main service that manages plugin updates.

**Key Features:**
- Periodic update checking based on user preferences
- Automatic updates when enabled
- Manual update triggering
- Download progress tracking
- Update status monitoring
- Rollback to previous versions
- Update history tracking

### PluginMarketplaceClient

Interface for communicating with the plugin marketplace to fetch version information and download plugins.

**Implementations:**
- `MockPluginMarketplaceClient`: Mock implementation for testing
- Production implementation should use REST API

### PluginUpdateHistoryRepository

Repository for storing and retrieving plugin update history.

**Implementations:**
- `InMemoryPluginUpdateHistoryRepository`: In-memory implementation for testing
- Production implementation should use database storage

## Usage Examples

### Basic Setup

```kotlin
// Create dependencies
val pluginManager = PluginManager(...)
val pluginRegistry = PluginRegistry(...)
val pluginLoader = PluginLoader(...)
val pluginDatabase = PluginDatabase(...)
val preferences = PluginPreferences(...)
val marketplaceClient = MockPluginMarketplaceClient() // or production implementation
val updateHistoryRepository = InMemoryPluginUpdateHistoryRepository()

// Create update checker
val updateChecker = PluginUpdateChecker(
    pluginManager = pluginManager,
    pluginRegistry = pluginRegistry,
    pluginLoader = pluginLoader,
    pluginDatabase = pluginDatabase,
    preferences = preferences,
    marketplaceClient = marketplaceClient,
    updateHistoryRepository = updateHistoryRepository
)
```

### Start Periodic Update Checking

```kotlin
// Start checking for updates every 24 hours (or based on user preference)
updateChecker.startPeriodicUpdateChecking()

// Stop periodic checking
updateChecker.stopPeriodicUpdateChecking()
```

### Manual Update Check

```kotlin
// Check for updates manually
val result = updateChecker.checkForUpdates()
result.onSuccess { updates ->
    println("Found ${updates.size} updates")
    updates.forEach { update ->
        println("${update.pluginId}: ${update.currentVersion} -> ${update.latestVersion}")
    }
}
```

### Observe Available Updates

```kotlin
// Collect available updates
updateChecker.availableUpdates.collect { updates ->
    println("Available updates: ${updates.size}")
    
    // Show notification if updates are available
    if (updates.isNotEmpty()) {
        val notification = updateChecker.createUpdateNotification()
        showNotification(notification.getSummaryMessage())
    }
}
```

### Update a Plugin

```kotlin
// Update a specific plugin
val result = updateChecker.updatePlugin("com.example.plugin")
result.onSuccess {
    println("Plugin updated successfully")
}.onFailure { error ->
    println("Update failed: ${error.message}")
}
```

### Monitor Update Status

```kotlin
// Observe update status for all plugins
updateChecker.updateStatus.collect { statusMap ->
    statusMap.forEach { (pluginId, status) ->
        when (status) {
            is UpdateStatus.Downloading -> {
                println("$pluginId: Downloading ${status.progress}%")
            }
            is UpdateStatus.Installing -> {
                println("$pluginId: Installing...")
            }
            is UpdateStatus.Completed -> {
                println("$pluginId: Update completed")
            }
            is UpdateStatus.Failed -> {
                println("$pluginId: Update failed - ${status.message}")
            }
            else -> {}
        }
    }
}
```

### Rollback a Plugin

```kotlin
// Rollback to a previous version
val result = updateChecker.rollbackPlugin(
    pluginId = "com.example.plugin",
    targetVersionCode = 100
)
result.onSuccess {
    println("Rollback successful")
}.onFailure { error ->
    println("Rollback failed: ${error.message}")
}
```

### View Update History

```kotlin
// Get update history for a plugin
val history = updateChecker.getUpdateHistory("com.example.plugin")
history.forEach { record ->
    println("${record.fromVersion} -> ${record.toVersion}")
    println("Date: ${record.updateDate}")
    println("Success: ${record.success}")
}

// Get all update history
val allHistory = updateChecker.getAllUpdateHistory()
```

### Retry Failed Update

```kotlin
// Retry a failed update
val result = updateChecker.retryUpdate("com.example.plugin")
result.onSuccess {
    println("Retry successful")
}.onFailure { error ->
    println("Retry failed: ${error.message}")
}
```

### Display Changelog

```kotlin
// Format and display changelog
val update = updateChecker.availableUpdates.value.first()
val formattedChangelog = PluginChangelogFormatter.format(update.changelog)
println(formattedChangelog)

// Extract summary for list view
val summary = PluginChangelogFormatter.extractSummary(update.changelog)
println(summary)
```

## Update Flow

### Automatic Update Flow

1. Periodic check runs based on `pluginUpdateCheckInterval` preference
2. `checkForUpdates()` compares installed versions with marketplace versions
3. If `autoUpdatePlugins` is enabled, updates are downloaded and installed automatically
4. Update status is tracked and can be observed via `updateStatus` flow
5. Update history is recorded in the repository

### Manual Update Flow

1. User triggers update check or update for specific plugin
2. Plugin package is downloaded with progress tracking
3. Current plugin is disabled
4. New plugin is loaded and validated
5. Plugin is registered and database is updated
6. Plugin is re-enabled if it was enabled before
7. Update history is recorded

### Rollback Flow

1. User selects a previous version from update history
2. Old version is downloaded from marketplace
3. Current plugin is disabled
4. Old version is installed using the same flow as updates
5. Plugin is re-enabled if it was enabled before

## Error Handling

All update operations return `Result<T>` types for proper error handling:

```kotlin
updateChecker.updatePlugin(pluginId).fold(
    onSuccess = { 
        // Handle success
    },
    onFailure = { error ->
        // Handle error
        when (error) {
            is NetworkException -> showNetworkError()
            is ValidationException -> showValidationError()
            else -> showGenericError(error.message)
        }
    }
)
```

## Update Status Types

- `UpdateStatus.Idle`: No update operation in progress
- `UpdateStatus.Downloading(progress)`: Downloading update package
- `UpdateStatus.Downloaded`: Download completed
- `UpdateStatus.Installing`: Installing update
- `UpdateStatus.RollingBack`: Rolling back to previous version
- `UpdateStatus.Completed`: Operation completed successfully
- `UpdateStatus.Failed(message)`: Operation failed with error message

## Preferences

The update system uses the following preferences:

- `autoUpdatePlugins`: Boolean - Enable/disable automatic updates (default: true)
- `pluginUpdateCheckInterval`: Long - Interval between update checks in milliseconds (default: 24 hours)

## Requirements Mapping

- **12.1**: Periodic update checking - `startPeriodicUpdateChecking()`, `checkForUpdates()`
- **12.2**: Update notifications - `createUpdateNotification()`, `getAvailableUpdatesCount()`
- **12.3**: Auto-update functionality - Automatic updates in `checkForUpdates()` when preference enabled
- **12.4**: Rollback functionality - `rollbackPlugin()`
- **12.5**: Update history tracking - `PluginUpdateHistoryRepository`, `getUpdateHistory()`

## Testing

Use `MockPluginMarketplaceClient` for testing:

```kotlin
val mockClient = MockPluginMarketplaceClient()

// Add mock version data
mockClient.addMockVersion(
    pluginId = "com.example.plugin",
    versionInfo = PluginVersionInfo(
        pluginId = "com.example.plugin",
        version = "2.0.0",
        versionCode = 200,
        changelog = "- New feature\n- Bug fixes",
        downloadUrl = "https://example.com/plugin-2.0.0.iplugin",
        releaseDate = System.currentTimeMillis(),
        minIReaderVersion = "1.0.0",
        fileSize = 1024000
    )
)

// Use in tests
val updateChecker = PluginUpdateChecker(
    // ... other dependencies
    marketplaceClient = mockClient,
    // ...
)
```

## Integration with UI

The update system is designed to integrate with UI components:

1. **Update Badge**: Use `getAvailableUpdatesCount()` for notification badges
2. **Update List**: Observe `availableUpdates` flow to display available updates
3. **Progress Indicators**: Observe `updateStatus` flow to show download/install progress
4. **Changelog Display**: Use `PluginChangelogFormatter` to format changelogs
5. **Update History**: Use `getUpdateHistory()` to display version history

## Future Enhancements

- Delta updates (only download changed files)
- Update scheduling (update at specific times)
- Update channels (stable, beta, alpha)
- Automatic rollback on plugin errors
- Update size optimization
- Bandwidth throttling for downloads
