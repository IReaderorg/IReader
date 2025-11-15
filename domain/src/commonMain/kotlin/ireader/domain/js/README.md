# JavaScript Plugin Engine Infrastructure

This package provides the core infrastructure for executing JavaScript-based plugins in IReader.

## Package Structure

```
ireader/domain/js/
├── engine/          # JavaScript engine implementations
│   ├── JSEngine.kt          # Expect/actual interface for JS execution
│   ├── JSValue.kt           # Wrapper for JavaScript values
│   ├── JSException.kt       # JavaScript execution exceptions
│   └── JSEnginePool.kt      # Engine pooling and lifecycle management
│
├── library/         # JavaScript library providers
│   ├── JSLibraryProvider.kt # Main library setup and require() implementation
│   ├── JSFetchApi.kt        # Fetch API implementation
│   └── JSStorage.kt         # Storage API implementation
│
├── models/          # Data models (to be implemented in Task 2)
├── bridge/          # Plugin bridge (to be implemented in Task 2)
└── README.md        # This file
```

## Components

### JSEngine

Platform-specific JavaScript engine implementation:
- **Android**: Uses QuickJS (lightweight, fast)
- **Desktop**: Uses GraalVM JavaScript (full ES2022 support)

**Key Features**:
- Script evaluation
- Function calls
- Global object management
- Memory limits (64MB per engine)
- Execution timeout (30 seconds via coroutines)
- Automatic resource cleanup

**Usage**:
```kotlin
val engine = JSEngine()
engine.initialize()

// Evaluate script
val result = engine.evaluateScript("1 + 1")

// Call function
engine.evaluateScript("function add(a, b) { return a + b; }")
val sum = engine.callFunction("add", 5, 3)

// Set global object
engine.setGlobalObject("myVar", "Hello")

// Cleanup
engine.dispose()
```

### JSEnginePool

Manages a pool of JavaScript engines for efficient resource usage:
- LRU eviction when pool is full (max 10 engines)
- Idle timeout (5 minutes)
- Thread-safe operations
- Automatic cleanup

**Usage**:
```kotlin
val pool = JSEnginePool()
val engine = pool.getOrCreate("plugin-id")
// Use engine...
pool.returnEngine("plugin-id")
```

### JSLibraryProvider

Provides JavaScript libraries and APIs to plugins:
- `require()` function for loading libraries
- Bundled libraries: cheerio, dayjs, urlencode
- Fetch API for HTTP requests
- Storage API for persistence
- LocalStorage and SessionStorage

**Usage**:
```kotlin
val provider = JSLibraryProvider(engine, pluginId, httpClient, preferenceStore)
provider.setupRequireFunction()

// Now JavaScript can use:
// - require('cheerio')
// - require('dayjs')
// - require('urlencode')
// - fetch(url, options)
// - storage.set/get/delete
// - localStorage.setItem/getItem
// - sessionStorage.setItem/getItem
```

### JSFetchApi

Implements the Fetch API using Ktor HttpClient:
- GET and POST requests
- Custom headers
- Request body support
- Response handling

### JSStorage

Persistent storage for plugins:
- Key-value storage scoped to plugin ID
- Optional expiration times
- JSON serialization
- Backed by PreferenceStore

## Platform Implementations

### Android (QuickJS)

**Dependencies**:
```gradle
implementation("app.cash.quickjs:quickjs-android:0.9.2")
```

**Features**:
- Lightweight (~1MB)
- Fast execution
- Good ES6+ support
- Native Android integration

### Desktop (GraalVM)

**Dependencies**:
```gradle
implementation("org.graalvm.polyglot:polyglot:23.1.0")
implementation("org.graalvm.polyglot:js:23.1.0")
```

**Features**:
- Full ES2022 support
- Resource limits
- Host access control
- Excellent performance

## Bundled JavaScript Libraries

Located in `domain/src/commonMain/resources/js/`:

1. **cheerio.min.js** - HTML parsing and DOM manipulation
2. **dayjs.min.js** - Date manipulation
3. **urlencode.min.js** - URL encoding utilities

**Note**: Current files are placeholders. Download actual minified libraries from CDN:
- Cheerio: https://cdn.jsdelivr.net/npm/cheerio@1.0.0-rc.12/dist/browser/cheerio.min.js
- Dayjs: https://cdn.jsdelivr.net/npm/dayjs@1.11.10/dayjs.min.js
- Urlencode: https://cdn.jsdelivr.net/npm/urlencode@1.1.0/urlencode.min.js

## Security Considerations

1. **Sandboxing**: JavaScript code runs in isolated environment
2. **Memory Limits**: 64MB per engine instance
3. **Execution Timeout**: 30 seconds per operation
4. **No Host Access**: Desktop implementation disables host class lookup
5. **Storage Isolation**: Each plugin has isolated storage

## Error Handling

All JavaScript errors are wrapped in `JSException` with:
- Error message
- JavaScript stack trace
- Original cause

Example:
```kotlin
try {
    engine.evaluateScript("invalid javascript")
} catch (e: JSException) {
    println("Error: ${e.message}")
    println("JS Stack: ${e.jsStackTrace}")
}
```

## Performance

- **Engine Pooling**: Reuse engines across invocations
- **Lazy Loading**: Load libraries on demand
- **Compiled Code Cache**: Cache parsed JavaScript
- **Concurrent Execution**: Support multiple plugins simultaneously

## Next Steps

Task 2 will implement:
- Plugin data models (PluginMetadata, JSNovelItem, etc.)
- Plugin bridge for Kotlin ↔ JavaScript translation
- JSPluginSource implementing IReader's Source interface

## Requirements Covered

This implementation covers the following requirements:
- 1.1: JavaScript engine integration on all platforms
- 1.2: Sandboxed execution environment
- 1.3: ES6+ JavaScript syntax support
- 1.4: require() function with approved libraries
- 1.5: Memory limits per plugin execution
- 4.1-4.5: Network request handling via fetch()
- 5.1-5.5: HTML parsing support via cheerio
- 8.1-8.5: Storage and persistence
- 10.1-10.5: Performance and resource management
