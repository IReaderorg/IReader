# Advanced System Features

This document describes the advanced system features implemented in IReader, including crash handling, deep links, app updates, telemetry, privacy controls, and diagnostics.

## Overview

The advanced system features provide:
- **Crash Handling**: Comprehensive crash reporting and recovery
- **Deep Link System**: Handle external URLs and content links
- **App Updates**: Automatic update checking and installation
- **Telemetry**: Privacy-focused analytics and usage tracking
- **Privacy Controls**: Granular control over data collection
- **System Health Monitoring**: Performance metrics and stability tracking
- **Diagnostic Tools**: Troubleshooting and system information

## Components

### 1. Error Handling

#### IReaderError
Structured error types for better error handling:

```kotlin
sealed class IReaderError : Exception() {
    data class NetworkError(...)
    data class DatabaseError(...)
    data class SourceError(...)
    data class StorageError(...)
    data class AuthError(...)
    data class ParseError(...)
    data class UnknownError(...)
}
```

#### GlobalExceptionHandler
Centralized exception handling:

```kotlin
GlobalExceptionHandler.initialize { throwable ->
    // Handle crash
}

// Create coroutine exception handler
val handler = GlobalExceptionHandler.createCoroutineExceptionHandler("MyContext")
```

#### ErrorBoundary (Compose)
Catch errors in UI components:

```kotlin
ErrorBoundary(
    onError = { error -> /* Handle error */ }
) {
    // Your composable content
}
```

### 2. Crash Reporting

#### CrashReport
Detailed crash information:

```kotlin
val crashReport = CrashReport.create(
    exception = throwable,
    context = "Reading Chapter",
    appVersion = "1.0.0"
)

// Get formatted report
val report = crashReport.toFormattedString()
```

#### CrashScreen (Compose)
Display crash information to users:

```kotlin
CrashScreen(
    crashReport = crashReport,
    onRestart = { /* Restart app */ },
    onShareReport = { report -> /* Share report */ }
)
```

### 3. Deep Link System

#### DeepLinkHandler
Handle various deep link types:

```kotlin
val handler = DeepLinkHandler()

// Register handlers
handler.registerHandler(DeepLinkType.BOOK) { deepLink ->
    // Navigate to book
}

// Handle deep link
handler.handleDeepLink("ireader://book/123")
```

Supported URL schemes:
- `ireader://book/{id}` - Open a specific book
- `ireader://chapter/{id}` - Open a specific chapter
- `ireader://source/{id}` - Open a source
- `ireader://browse` - Open browse screen
- `ireader://library` - Open library
- `ireader://settings/{section}` - Open settings

#### UrlResolver
Resolve external URLs to content:

```kotlin
val resolver = UrlResolver(listOf(GenericUrlResolver()))

val content = resolver.resolve("https://example.com/manga/123")
// Returns ResolvedContent with book information
```

#### DeepLinkScreen (Compose)
Show loading state while processing deep links:

```kotlin
DeepLinkScreen(
    deepLink = deepLink,
    isProcessing = true,
    onRetry = { /* Retry */ },
    onCancel = { /* Cancel */ }
)
```

### 4. App Updates

#### AppUpdateChecker
Check for and download updates:

```kotlin
val updateChecker = AppUpdateChecker(
    currentVersion = "1.0.0",
    updateRepository = myRepository
)

// Check for updates
val result = updateChecker.checkForUpdates()

// Download update
updateChecker.downloadUpdate(updateInfo)

// Install update
updateChecker.installUpdate(filePath)
```

#### UpdateNotificationCard (Compose)
Display update notifications:

```kotlin
UpdateNotificationCard(
    updateState = updateState,
    onDownload = { /* Download */ },
    onInstall = { /* Install */ },
    onDismiss = { /* Dismiss */ }
)
```

### 5. Privacy & Telemetry

#### PrivacyPreferences
Manage user privacy settings:

```kotlin
// Enable/disable features
privacyPreferences.setAnalyticsEnabled(true)
privacyPreferences.setCrashReportingEnabled(true)

// Privacy mode (disable all)
privacyPreferences.enablePrivacyMode()
```

#### TelemetrySystem
Privacy-focused analytics:

```kotlin
val telemetry = TelemetrySystem(
    privacyPreferences = prefs,
    telemetryRepository = repo,
    scope = scope
)

// Track events (only if user consented)
telemetry.trackScreenView("Library")
telemetry.trackUserAction("BookOpened")
telemetry.trackPerformance("LoadChapter", 1500)
telemetry.trackError(exception, "Reading")
```

#### PrivacySettingsScreen (Compose)
UI for privacy controls:

```kotlin
PrivacySettingsScreen(
    analyticsEnabled = true,
    crashReportingEnabled = true,
    onAnalyticsChanged = { enabled -> },
    onEnablePrivacyMode = { },
    // ... other settings
)
```

### 6. System Health Monitoring

#### SystemHealthMonitor
Track performance and stability:

```kotlin
val monitor = SystemHealthMonitor()

// Record performance
monitor.recordPerformance("DatabaseQuery", 150, PerformanceCategory.DATABASE)

// Record memory usage
monitor.recordMemoryUsage()

// Record crash
monitor.recordCrash(throwable)

// Get statistics
val stats = monitor.getAllPerformanceStats()
val report = monitor.getHealthReport()
```

### 7. Diagnostic Tools

#### DiagnosticTools
System information and troubleshooting:

```kotlin
// Collect system info
val systemInfo = DiagnosticTools.collectSystemInfo()

// Generate diagnostic report
val report = DiagnosticTools.generateDiagnosticReport(
    includeSystemInfo = true,
    includeMemoryInfo = true,
    healthMonitor = monitor
)

// Run health check
val healthCheck = DiagnosticTools.runHealthCheck()

// Export diagnostic data
val data = DiagnosticTools.exportDiagnosticData(
    healthMonitor = monitor,
    crashReports = reports
)
```

#### DiagnosticsScreen (Compose)
UI for diagnostics:

```kotlin
DiagnosticsScreen(
    systemInfo = systemInfo,
    healthCheckResult = healthCheck,
    diagnosticReport = report,
    onRunHealthCheck = { },
    onExportDiagnostics = { },
    onClearCache = { }
)
```

### 8. System Manager

#### SystemManager
Central coordinator for all features:

```kotlin
val systemManager = SystemManager(
    deepLinkHandler = handler,
    urlResolver = resolver,
    appUpdateChecker = updateChecker,
    telemetrySystem = telemetry,
    privacyPreferences = prefs,
    systemHealthMonitor = monitor,
    scope = scope
)

// Initialize
systemManager.initialize()

// Use features
systemManager.handleDeepLink(url)
systemManager.checkForUpdates()
systemManager.trackScreenView("Library")
systemManager.trackPerformance("Operation", 1000)

// Get diagnostics
val diagnostics = systemManager.getSystemDiagnostics()
val healthCheck = systemManager.runHealthCheck()

// Privacy
systemManager.enablePrivacyMode()

// Shutdown
systemManager.shutdown()
```

## Usage Examples

### Example 1: Handle App Crash

```kotlin
// Initialize global exception handler
GlobalExceptionHandler.initialize { throwable ->
    val crashReport = CrashReport.create(throwable)
    
    // Show crash screen
    navController.navigate("crash/${crashReport.id}")
    
    // Log crash
    IReaderLog.error("App crashed", throwable)
}
```

### Example 2: Process Deep Link

```kotlin
// Register deep link handlers
deepLinkHandler.registerHandler(DeepLinkType.BOOK) { deepLink ->
    deepLink.bookId?.let { bookId ->
        navController.navigate("book/$bookId")
    }
}

// Handle incoming deep link
val handled = deepLinkHandler.handleDeepLink(intent.data.toString())
```

### Example 3: Check for Updates

```kotlin
// Check for updates on app start
lifecycleScope.launch {
    val result = appUpdateChecker.checkForUpdates()
    
    when (result) {
        is UpdateCheckResult.UpdateAvailable -> {
            // Show update notification
            showUpdateNotification(result.updateInfo)
        }
        is UpdateCheckResult.UpToDate -> {
            // App is up to date
        }
        is UpdateCheckResult.Error -> {
            // Handle error
        }
    }
}
```

### Example 4: Track User Actions

```kotlin
// Track screen views
systemManager.trackScreenView("Library")

// Track user actions
systemManager.trackUserAction("BookOpened", mapOf(
    "book_id" to bookId,
    "source" to sourceName
))

// Track performance
val startTime = System.currentTimeMillis()
loadChapter()
val duration = System.currentTimeMillis() - startTime
systemManager.trackPerformance("LoadChapter", duration)
```

### Example 5: Privacy Controls

```kotlin
// Show privacy settings
PrivacySettingsScreen(
    analyticsEnabled = privacyPrefs.analyticsEnabled().collectAsState().value,
    onAnalyticsChanged = { enabled ->
        lifecycleScope.launch {
            privacyPrefs.setAnalyticsEnabled(enabled)
        }
    },
    onEnablePrivacyMode = {
        lifecycleScope.launch {
            systemManager.enablePrivacyMode()
        }
    }
)
```

## Best Practices

1. **Error Handling**
   - Always use ErrorBoundary for UI components
   - Convert exceptions to IReaderError for structured handling
   - Log errors with appropriate context

2. **Deep Links**
   - Register all handlers during app initialization
   - Validate deep link data before navigation
   - Handle invalid deep links gracefully

3. **Updates**
   - Check for updates on app start (non-forced)
   - Show clear update notifications
   - Allow users to dismiss non-required updates

4. **Privacy**
   - Always check user consent before collecting data
   - Provide clear privacy controls
   - Default to privacy-friendly settings

5. **Telemetry**
   - Only track essential events
   - Avoid collecting PII
   - Flush telemetry data periodically

6. **Performance**
   - Monitor critical operations
   - Set appropriate thresholds
   - Log slow operations for investigation

7. **Diagnostics**
   - Provide easy access to diagnostic tools
   - Allow users to export diagnostic data
   - Include diagnostics in bug reports

## Testing

### Unit Tests

```kotlin
@Test
fun testDeepLinkParsing() {
    val handler = DeepLinkHandler()
    val handled = handler.handleDeepLink("ireader://book/123")
    assertTrue(handled)
}

@Test
fun testCrashReportCreation() {
    val exception = RuntimeException("Test error")
    val report = CrashReport.create(exception)
    assertEquals("Test error", report.exception.message)
}
```

### Integration Tests

```kotlin
@Test
fun testSystemManagerInitialization() {
    val systemManager = createSystemManager()
    systemManager.initialize()
    
    // Verify initialization
    assertTrue(systemManager.isInitialized)
}
```

## Requirements Mapping

This implementation addresses the following requirements:

- **20.1**: Deep link system with DeepLinkScreen
- **20.2**: Crash handling with CrashScreen and GlobalExceptionHandler
- **20.4**: App update system with AppUpdateChecker
- **20.5**: Telemetry with privacy-focused TelemetrySystem
- **20.6**: Privacy preferences with granular controls
- **6.1-6.4**: Error handling with ErrorBoundary and structured errors

## Future Enhancements

- Add more source-specific URL resolvers
- Implement crash report upload to server
- Add A/B testing framework
- Implement feature flags system
- Add remote configuration
- Enhance diagnostic tools with more metrics
