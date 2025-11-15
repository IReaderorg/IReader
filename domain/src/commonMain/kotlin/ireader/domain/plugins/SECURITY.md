# Plugin Security and Sandboxing

This document describes the security architecture for the IReader plugin system.

## Overview

The plugin security system provides:
- **Permission-based access control** - Plugins must declare and request permissions
- **Sandboxed execution** - Plugins run in isolated environments with restricted access
- **Resource monitoring** - Track CPU, memory, and network usage per plugin
- **Automatic throttling/termination** - Plugins exceeding limits are automatically managed

## Components

### 1. PluginSandbox

The `PluginSandbox` class provides the core sandboxing functionality:

```kotlin
val sandbox = PluginSandbox(
    pluginId = "com.example.plugin",
    manifest = pluginManifest,
    permissionManager = permissionManager,
    pluginsBaseDir = File("/plugins")
)

// Check permissions
if (sandbox.checkPermission(PluginPermission.NETWORK)) {
    // Allow network access
}

// Validate file access
if (sandbox.restrictFileAccess("/path/to/file")) {
    // Allow file access
}

// Check resource usage
if (sandbox.hasExceededResourceLimits()) {
    // Terminate plugin
}
```

### 2. PluginPermissionManager

Manages runtime permissions for plugins:

```kotlin
val permissionManager = PluginPermissionManager(database)

// Request permission (may trigger user dialog)
val result = permissionManager.requestPermission(
    pluginId = "com.example.plugin",
    permission = PluginPermission.NETWORK,
    manifest = pluginManifest
)

when (result) {
    is PermissionRequestResult.Granted -> // Permission granted
    is PermissionRequestResult.Pending -> // Waiting for user approval
    is PermissionRequestResult.Denied -> // Permission denied
}

// Grant permission
permissionManager.grantPermission(pluginId, permission)

// Revoke permission
permissionManager.revokePermission(pluginId, permission)
```

### 3. PluginResourceMonitor

Monitors resource usage for plugins:

```kotlin
val monitor = PluginResourceMonitor(
    pluginId = "com.example.plugin",
    limits = PluginResourceLimits(
        maxCpuPercent = 50.0,
        maxMemoryBytes = 100 * 1024 * 1024, // 100 MB
        maxNetworkBytesPerMinute = 10 * 1024 * 1024 // 10 MB/min
    )
)

// Record usage
monitor.recordUsage(
    cpuUsage = 25.0,
    memoryUsage = 50_000_000,
    networkUsage = 1_000_000
)

// Check limits
if (monitor.hasExceededLimits()) {
    // Take action
}

// Get usage statistics
val current = monitor.getCurrentUsage()
val average = monitor.getAverageUsage()
val peak = monitor.getPeakUsage()
```

### 4. SandboxedPluginContext

Provides sandboxed access to app resources:

```kotlin
val context = SandboxedPluginContext(
    pluginId = "com.example.plugin",
    permissions = listOf(PluginPermission.STORAGE),
    sandbox = sandbox,
    preferencesStore = preferencesStore
)

// Access plugin data directory
val dataDir = context.getDataDir()

// Check permissions
if (context.hasPermission(PluginPermission.STORAGE)) {
    // Access storage
}

// Get preferences (requires PREFERENCES permission)
val prefs = context.getPreferences()
prefs.putString("key", "value")
```

### 5. PluginSecurityManager

Central coordinator for all security components:

```kotlin
val securityManager = PluginSecurityManager(
    permissionManager = permissionManager,
    pluginsBaseDir = File("/plugins")
)

// Initialize
securityManager.initialize()

// Create sandboxed context
val context = securityManager.createPluginContext(
    pluginId = "com.example.plugin",
    manifest = pluginManifest,
    preferencesStore = preferencesStore
)

// Request permission
val result = securityManager.requestPermission(
    pluginId = "com.example.plugin",
    permission = PluginPermission.NETWORK,
    manifest = pluginManifest
)

// Monitor resources
securityManager.recordResourceUsage(
    pluginId = "com.example.plugin",
    cpuUsage = 25.0,
    memoryUsage = 50_000_000,
    networkUsage = 1_000_000
)

// Check for excessive plugins
val excessive = securityManager.getPluginsExceedingLimits()
excessive.forEach { pluginId ->
    securityManager.terminatePlugin(pluginId, "Resource limit exceeded")
}
```

## Permissions

### Available Permissions

| Permission | Description | Risk Level | Auto-Grant |
|------------|-------------|------------|------------|
| `NETWORK` | Access to network | HIGH | No |
| `STORAGE` | Access to local storage | HIGH | No |
| `READER_CONTEXT` | Access to reading context | LOW | Yes |
| `LIBRARY_ACCESS` | Access to user's library | MEDIUM | No |
| `PREFERENCES` | Access to app preferences | MEDIUM | No |
| `NOTIFICATIONS` | Show notifications | LOW | Yes |

### Permission Flow

1. Plugin declares permissions in manifest
2. Plugin requests permission at runtime
3. For sensitive permissions (HIGH/MEDIUM risk):
   - User is prompted for approval
   - Request is pending until user responds
4. For non-sensitive permissions (LOW risk):
   - Auto-granted if in manifest
5. Granted permissions are persisted in database
6. Permissions can be revoked by user at any time

## Resource Limits

### Default Limits

```kotlin
PluginResourceLimits(
    maxCpuPercent = 50.0,              // 50% CPU
    maxMemoryBytes = 100 * 1024 * 1024, // 100 MB
    maxNetworkBytesPerMinute = 10 * 1024 * 1024 // 10 MB/min
)
```

### Monitoring

Resource usage is monitored continuously:
- Measurements recorded every few seconds
- History of last 60 measurements kept
- Average and peak usage calculated

### Throttling and Termination

- **Throttling**: When usage exceeds 80% of limit
  - Plugin operations may be slowed down
  - Warning shown to user
  
- **Termination**: When usage exceeds 100% of limit
  - Plugin is automatically disabled
  - User is notified
  - Plugin can be re-enabled manually

## File Access Restrictions

Plugins can only access files within their data directory:

```
/plugins/data/{pluginId}/
```

Attempts to access files outside this directory are blocked:

```kotlin
// Allowed
sandbox.restrictFileAccess("/plugins/data/com.example.plugin/config.json") // true

// Blocked
sandbox.restrictFileAccess("/system/important.file") // false
sandbox.restrictFileAccess("../other-plugin/data.json") // false
```

## Network Access Restrictions

Plugins must have `NETWORK` permission to access the network:

```kotlin
// Check before making network request
if (sandbox.restrictNetworkAccess("https://api.example.com")) {
    // Make request
} else {
    // Access denied
}
```

Additional URL filtering can be implemented to block specific domains or protocols.

## Integration with PluginManager

The `PluginManager` integrates security automatically:

```kotlin
val pluginManager = PluginManager(
    loader = loader,
    registry = registry,
    preferences = preferences,
    monetization = monetization,
    database = database,
    securityManager = securityManager
)

// Load plugins (security initialized automatically)
pluginManager.loadPlugins()

// Enable plugin (sandboxed context created automatically)
pluginManager.enablePlugin("com.example.plugin")

// Check resource usage
val usage = pluginManager.getPluginResourceUsage("com.example.plugin")

// Terminate excessive plugins
pluginManager.checkAndTerminateExcessivePlugins()
```

## Best Practices

### For Plugin Developers

1. **Declare all permissions** in manifest
2. **Request permissions** only when needed
3. **Handle permission denials** gracefully
4. **Minimize resource usage** to avoid throttling
5. **Use plugin data directory** for all file operations
6. **Respect permission boundaries** - don't try to bypass security

### For App Developers

1. **Initialize security manager** before loading plugins
2. **Monitor resource usage** periodically
3. **Handle permission requests** in UI
4. **Provide clear permission descriptions** to users
5. **Log security violations** for debugging
6. **Test with resource limits** to ensure proper handling

## Security Considerations

### Threat Model

The security system protects against:
- **Malicious plugins** accessing sensitive data
- **Buggy plugins** consuming excessive resources
- **Unauthorized file access** outside plugin directory
- **Unauthorized network access** without permission
- **Privilege escalation** attempts

### Limitations

The security system does NOT protect against:
- **Social engineering** (user granting permissions)
- **Vulnerabilities in plugin API** itself
- **Side-channel attacks**
- **Timing attacks**

### Future Enhancements

Potential improvements:
- Code signing and verification
- Plugin reputation system
- Network traffic inspection
- Sandboxed process isolation (OS-level)
- Encrypted plugin storage
- Audit logging of security events
