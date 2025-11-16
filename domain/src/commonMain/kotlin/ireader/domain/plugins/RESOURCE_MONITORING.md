# Plugin Resource Monitoring System

## Overview

The Plugin Resource Monitoring System provides comprehensive tracking and enforcement of resource usage for plugins. It monitors CPU, memory, and network usage, enforces limits, and provides notifications when plugins exceed their allocated resources.

## Requirements Implemented

- **4.1**: Track plugin memory usage using platform-specific APIs
- **4.2**: Monitor CPU usage percentage for plugins
- **4.3**: Track network bytes transferred by plugins
- **4.4**: Enforce resource limits (50% CPU, 64MB memory, 10MB/min network)
- **4.5**: Implement throttling logic when limits are approached (>80%)
- **4.6**: Implement suspension logic when limits are exceeded
- **4.7**: Update statistics every 5 seconds in background coroutine
- **4.8**: Provide resource usage history (last hour)
- **4.9**: Create user notification system for limit violations
- **4.10**: Display real-time resource usage in plugin details screens

## Architecture

### Core Components

1. **ResourceMonitor** (Interface)
   - Platform-agnostic interface for resource monitoring
   - Implementations: `AndroidResourceMonitor`, `DesktopResourceMonitor`
   - Tracks CPU, memory, and network usage per plugin

2. **ResourceTracker**
   - Aggregates usage statistics
   - Updates every 5 seconds in background coroutine
   - Provides StateFlow for reactive UI updates
   - Maintains usage history (last 60 measurements = 5 minutes)

3. **ResourceLimiter**
   - Enforces resource limits
   - Implements throttling (>80% of limit)
   - Implements suspension (>100% of limit)
   - Emits violation events

4. **PluginResourceMonitor**
   - Per-plugin monitoring
   - Tracks current, average, and peak usage
   - Calculates usage percentages relative to limits

5. **ResourceViolationNotifier**
   - Observes violation events
   - Handles notifications
   - Logs violations

## Platform-Specific Implementations

### Android

**AndroidResourceMonitor** uses:
- `ActivityManager.getProcessMemoryInfo()` for memory tracking
- `/proc/stat` for CPU usage tracking
- Manual tracking for network usage

### Desktop

**DesktopResourceMonitor** uses:
- `ManagementFactory.getMemoryMXBean()` for memory tracking
- `OperatingSystemMXBean` for CPU usage tracking
- Manual tracking for network usage

## Usage

### Basic Setup

```kotlin
// Create platform-specific resource monitor
val resourceMonitor = createResourceMonitor() // Desktop
// or
val resourceMonitor = createResourceMonitor(context) // Android

// Create resource tracker
val resourceTracker = ResourceTracker(resourceMonitor, scope)

// Create resource limiter
val resourceLimiter = ResourceLimiter(resourceTracker, scope)

// Start monitoring a plugin
resourceTracker.startTracking("plugin-id", PluginResourceLimits())

// Start enforcement
resourceLimiter.startEnforcement()
```

### Recording Network Usage

```kotlin
// When plugin makes a network request
resourceTracker.recordNetworkUsage("plugin-id", bytesTransferred)
```

### Observing Usage Updates

```kotlin
resourceTracker.usageFlow
    .onEach { usageMap ->
        usageMap.forEach { (pluginId, usage) ->
            println("$pluginId: CPU=${usage.cpuUsagePercent}%, Memory=${usage.memoryUsageMB}MB")
        }
    }
    .launchIn(scope)
```

### Handling Violations

```kotlin
resourceLimiter.violationEvents
    .onEach { violation ->
        when (violation.type) {
            ViolationType.APPROACHING_LIMIT -> showWarning(violation.message)
            ViolationType.LIMIT_EXCEEDED -> showError(violation.message)
            ViolationType.THROTTLED -> notifyThrottled(violation.pluginId)
            ViolationType.SUSPENDED -> notifySuspended(violation.pluginId)
            ViolationType.RESUMED -> notifyResumed(violation.pluginId)
        }
    }
    .launchIn(scope)
```

### UI Integration

```kotlin
// In ViewModel
val resourceUsage = resourceTracker.getCurrentUsage(pluginId)
val monitor = resourceTracker.getMonitor(pluginId)
val percentages = monitor?.getUsagePercentages()
val history = resourceTracker.getUsageHistory(pluginId)

// Update state
_state.value = _state.value.copy(
    resourceUsage = resourceUsage,
    resourcePercentages = percentages,
    resourceHistory = history
)
```

```kotlin
// In Composable
ResourceUsageSection(
    usage = state.resourceUsage,
    percentages = state.resourcePercentages,
    modifier = Modifier.padding(16.dp)
)

ResourceUsageHistoryGraph(
    history = state.resourceHistory,
    modifier = Modifier.padding(16.dp)
)
```

## Resource Limits

Default limits (configurable):
- **CPU**: 50% maximum
- **Memory**: 64 MB maximum
- **Network**: 10 MB per minute maximum

### Throttling

Plugins are throttled when usage exceeds 80% of any limit:
- Reduces plugin operation frequency
- Shows warning to user
- Plugin continues to function

### Suspension

Plugins are suspended when usage exceeds 100% of any limit:
- Stops all plugin operations
- Shows error to user
- Requires manual resume or automatic recovery

## Testing

See `ResourceMonitoringExample.kt` for a complete example of how to use the system.

### Manual Testing

1. Install a plugin
2. Monitor resource usage in plugin details screen
3. Trigger high resource usage (e.g., large network requests)
4. Verify throttling occurs at 80%
5. Verify suspension occurs at 100%
6. Verify notifications are shown

### Verification

```kotlin
// Check if plugin is throttled
val isThrottled = resourceLimiter.isThrottled(pluginId)

// Check if plugin is suspended
val isSuspended = resourceLimiter.isSuspended(pluginId)

// Get current usage
val usage = resourceTracker.getCurrentUsage(pluginId)

// Get usage history
val history = resourceTracker.getUsageHistory(pluginId)
```

## Cleanup

Always cleanup resources when done:

```kotlin
resourceTracker.shutdown()
resourceLimiter.shutdown()
```

## Future Enhancements

1. **Persistent History**: Store usage history in database
2. **Advanced Graphs**: Implement proper line/area charts for history
3. **Per-Plugin Limits**: Allow custom limits per plugin
4. **Automatic Recovery**: Implement automatic resume after cooldown period
5. **Usage Predictions**: Predict future usage based on history
6. **Detailed Breakdown**: Track usage per plugin operation
7. **Export Reports**: Export usage reports for debugging

## Notes

- CPU tracking is approximate and platform-dependent
- Memory tracking measures heap usage, not total process memory
- Network tracking requires manual recording of bytes transferred
- Background tracking runs every 5 seconds to balance accuracy and performance
- History is limited to last 60 measurements (5 minutes) to conserve memory
