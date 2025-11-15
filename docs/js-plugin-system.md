## JavaScript Plugin System

### Overview

The JavaScript Plugin System enables IReader to load and execute JavaScript-based plugins from the LNReader ecosystem. This allows users to access hundreds of novel sources through LNReader's plugin format without requiring native Kotlin implementations.

### Architecture

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     IReader Application                      │
│  ┌────────────────────────────────────────────────────────┐ │
│  │           CatalogLoader                                │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │ │
│  │  │   Bundled    │  │   Locally    │  │  SystemWide │ │ │
│  │  │   Catalogs   │  │   Catalogs   │  │   Catalogs  │ │ │
│  │  └──────────────┘  └──────────────┘  └─────────────┘ │ │
│  │                                                        │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │         JSPluginCatalog (NEW)                    │ │ │
│  │  │  - Implements CatalogLocal                       │ │ │
│  │  │  - Wraps JSPluginSource                          │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────┘ │
│                            │                                 │
│                            ▼                                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         JSPluginSource                                 │ │
│  │  - Implements Source interface                         │ │
│  │  - Delegates to JSPluginBridge                         │ │
│  └────────────────────────────────────────────────────────┘ │
│                            │                                 │
│                            ▼                                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         JSPluginBridge                                 │ │
│  │  - Translates Kotlin ↔ JavaScript                     │ │
│  │  - Manages JS Engine lifecycle                         │ │
│  │  - Handles async operations                            │ │
│  └────────────────────────────────────────────────────────┘ │
│                            │                                 │
│                            ▼                                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         JSEngine                                       │ │
│  │  - QuickJS (Android) / GraalVM (Desktop)              │ │
│  │  - Provides require() implementation                   │ │
│  │  - Sandboxed execution environment                     │ │
│  └────────────────────────────────────────────────────────┘ │
│                            │                                 │
│                            ▼                                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         JSLibraryProvider                              │ │
│  │  - Cheerio, dayjs, urlencode                          │ │
│  │  - fetch(), Storage, LocalStorage                      │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

#### Data Flow

1. **Plugin Loading**: `JSPluginLoader` scans the plugins directory for `.js` files
2. **Engine Initialization**: `JSEngine` is created and initialized with required libraries
3. **Code Execution**: Plugin code is evaluated in the sandboxed JavaScript environment
4. **Metadata Extraction**: `JSPluginBridge` extracts plugin metadata (id, name, version, etc.)
5. **Source Creation**: `JSPluginSource` wraps the bridge and implements IReader's `Source` interface
6. **Catalog Integration**: `JSPluginCatalog` wraps the source and integrates with the catalog system

#### Class Relationships

```
CatalogLocal
    ↑
    │ implements
    │
JSPluginCatalog
    │ contains
    ├─→ JSPluginSource (implements Source)
    │       │ uses
    │       └─→ JSPluginBridge
    │               │ uses
    │               ├─→ JSEngine
    │               └─→ JSLibraryProvider
    │
    └─→ PluginMetadata
```

### JSEngine API

The `JSEngine` interface provides a platform-agnostic way to execute JavaScript code.

#### Methods

##### `initialize()`

Initializes the JavaScript engine. Must be called before any other operations.

```kotlin
val engine = JSEngine()
engine.initialize()
```

**Platform Implementations:**
- **Android**: Uses QuickJS via `quickjs-android` library
- **Desktop**: Uses GraalVM JavaScript engine

---

##### `evaluateScript(script: String): Any?`

Evaluates a JavaScript script and returns the result.

```kotlin
val result = engine.evaluateScript("""
    function add(a, b) {
        return a + b;
    }
    add(2, 3);
""")
println(result) // Output: 5
```

**Parameters:**
- `script`: The JavaScript code to execute

**Returns:**
- The result of the script execution (can be any type)

**Throws:**
- `JSException` if execution fails

---

##### `callFunction(name: String, vararg args: Any?): Any?`

Calls a JavaScript function by name with the provided arguments.

```kotlin
engine.evaluateScript("""
    function greet(name) {
        return 'Hello, ' + name + '!';
    }
""")

val greeting = engine.callFunction("greet", "World")
println(greeting) // Output: Hello, World!
```

**Parameters:**
- `name`: The function name
- `args`: Variable number of arguments to pass to the function

**Returns:**
- The function result

**Throws:**
- `JSException` if the call fails

---

##### `setGlobalObject(name: String, value: Any)`

Sets a global object in the JavaScript context.

```kotlin
engine.setGlobalObject("API_KEY", "abc123")
engine.evaluateScript("console.log(API_KEY)") // Output: abc123
```

**Parameters:**
- `name`: The global variable name
- `value`: The value to set

---

##### `getGlobalObject(name: String): Any?`

Gets a global object from the JavaScript context.

```kotlin
engine.evaluateScript("var config = { url: 'https://example.com' };")
val config = engine.getGlobalObject("config")
```

**Parameters:**
- `name`: The global variable name

**Returns:**
- The global object value, or null if not found

---

##### `dispose()`

Disposes the JavaScript engine and releases resources. The engine cannot be used after disposal.

```kotlin
engine.dispose()
```

---

##### `withTimeout(timeoutMillis: Long, block: suspend () -> T): T`

Extension function that executes a JavaScript operation with a timeout.

```kotlin
val result = engine.withTimeout(5000L) {
    engine.callFunction("longRunningOperation")
}
```

**Parameters:**
- `timeoutMillis`: Timeout in milliseconds (default 30 seconds)
- `block`: The operation to execute

**Returns:**
- The result of the operation

**Throws:**
- `kotlinx.coroutines.TimeoutCancellationException` if timeout is exceeded

---

### JSPluginBridge API

The `JSPluginBridge` translates between JavaScript plugin API and Kotlin domain models.

#### Constructor

```kotlin
class JSPluginBridge(
    private val engine: JSEngine,
    private val pluginInstance: JSValue,
    private val httpClient: HttpClient,
    private val pluginId: String
)
```

#### Methods

##### `suspend fun getPluginMetadata(): PluginMetadata`

Extracts plugin metadata from the JavaScript plugin instance.

```kotlin
val metadata = bridge.getPluginMetadata()
println("Plugin: ${metadata.name} v${metadata.version}")
```

**Returns:**
- `PluginMetadata` containing id, name, version, site, icon, lang, filters, etc.

---

##### `suspend fun popularNovels(page: Int, filters: Map<String, Any>): List<JSNovelItem>`

Calls the plugin's `popularNovels()` method to fetch popular novels.

```kotlin
val novels = bridge.popularNovels(page = 1, filters = emptyMap())
novels.forEach { novel ->
    println("${novel.name} - ${novel.path}")
}
```

**Parameters:**
- `page`: The page number (1-indexed)
- `filters`: Map of filter values to apply

**Returns:**
- List of `JSNovelItem` objects

**Throws:**
- `JSPluginError.ExecutionError` if the call fails
- `JSPluginError.TimeoutError` if execution exceeds 30 seconds

---

##### `suspend fun searchNovels(searchTerm: String, page: Int): List<JSNovelItem>`

Calls the plugin's `searchNovels()` method to search for novels.

```kotlin
val results = bridge.searchNovels("fantasy", page = 1)
```

**Parameters:**
- `searchTerm`: The search query
- `page`: The page number (1-indexed)

**Returns:**
- List of `JSNovelItem` objects matching the search

---

##### `suspend fun parseNovel(novelPath: String): JSSourceNovel`

Calls the plugin's `parseNovel()` method to fetch novel details and chapters.

```kotlin
val novel = bridge.parseNovel("/novel/example-novel")
println("Title: ${novel.name}")
println("Chapters: ${novel.chapters.size}")
```

**Parameters:**
- `novelPath`: The novel path (from `JSNovelItem.path`)

**Returns:**
- `JSSourceNovel` containing full novel details and chapter list

---

##### `suspend fun parseChapter(chapterPath: String): String`

Calls the plugin's `parseChapter()` method to fetch chapter content.

```kotlin
val htmlContent = bridge.parseChapter("/chapter/example-chapter-1")
```

**Parameters:**
- `chapterPath`: The chapter path (from `JSChapterItem.path`)

**Returns:**
- HTML content of the chapter as a String

---

##### `suspend fun getFilters(): Map<String, FilterDefinition>`

Extracts filter definitions from the plugin.

```kotlin
val filters = bridge.getFilters()
filters.forEach { (key, definition) ->
    when (definition) {
        is FilterDefinition.Picker -> println("Picker: ${definition.label}")
        is FilterDefinition.TextInput -> println("TextInput: ${definition.label}")
        // ...
    }
}
```

**Returns:**
- Map of filter key to `FilterDefinition`

---

### Data Conversion Rules

#### JavaScript to Kotlin

| JavaScript Type | Kotlin Type | Notes |
|----------------|-------------|-------|
| `string` | `String` | Direct conversion |
| `number` | `Int`, `Long`, `Float`, `Double` | Depends on value |
| `boolean` | `Boolean` | Direct conversion |
| `null` | `null` | Direct conversion |
| `undefined` | `null` | Converted to null |
| `Array` | `List<Any?>` | Recursive conversion |
| `Object` | `Map<String, Any?>` | Recursive conversion |
| `Promise` | Awaited result | Automatically awaited |

#### Kotlin to JavaScript

| Kotlin Type | JavaScript Type | Notes |
|------------|-----------------|-------|
| `String` | `string` | Direct conversion |
| `Int`, `Long` | `number` | Direct conversion |
| `Float`, `Double` | `number` | Direct conversion |
| `Boolean` | `boolean` | Direct conversion |
| `null` | `null` | Direct conversion |
| `List<*>` | `Array` | Recursive conversion |
| `Map<String, *>` | `Object` | Recursive conversion |

---

### Plugin Format

#### Required Methods

Every LNReader plugin must implement the following methods:

```javascript
// Plugin metadata (properties)
const id = 'example-plugin';
const name = 'Example Plugin';
const version = '1.0.0';
const site = 'https://example.com';
const lang = 'en';
const icon = 'https://example.com/icon.png';

// Fetch popular novels
async function popularNovels(page, { filters }) {
    // Return array of NovelItem objects
    return [
        {
            name: 'Novel Title',
            path: '/novel/novel-slug',
            cover: 'https://example.com/cover.jpg'
        }
    ];
}

// Search for novels
async function searchNovels(searchTerm, page) {
    // Return array of NovelItem objects
    return [];
}

// Parse novel details
async function parseNovel(novelPath) {
    // Return SourceNovel object
    return {
        name: 'Novel Title',
        path: novelPath,
        cover: 'https://example.com/cover.jpg',
        summary: 'Novel description',
        author: 'Author Name',
        genres: 'Action, Fantasy',
        status: 'Ongoing',
        chapters: [
            {
                name: 'Chapter 1',
                path: '/chapter/1',
                releaseTime: '2024-01-01'
            }
        ]
    };
}

// Parse chapter content
async function parseChapter(chapterPath) {
    // Return HTML content as string
    return '<p>Chapter content...</p>';
}
```

#### Metadata Structure

```javascript
{
    id: 'plugin-id',              // Required: lowercase, hyphens only
    name: 'Plugin Name',          // Required: display name
    version: '1.0.0',             // Required: semantic versioning
    site: 'https://example.com',  // Required: source website
    lang: 'en',                   // Required: language code
    icon: 'https://...',          // Required: icon URL or data URI
    imageRequestInit: {           // Optional: custom headers for images
        headers: {
            'Referer': 'https://example.com'
        }
    },
    filters: {                    // Optional: filter definitions
        status: {
            type: 'Picker',
            label: 'Status',
            options: [
                { label: 'All', value: 'all' },
                { label: 'Ongoing', value: 'ongoing' }
            ],
            defaultValue: 'all'
        }
    }
}
```

#### Filter Definitions

##### Picker Filter

Dropdown selection with predefined options.

```javascript
{
    type: 'Picker',
    label: 'Status',
    options: [
        { label: 'All', value: 'all' },
        { label: 'Ongoing', value: 'ongoing' },
        { label: 'Completed', value: 'completed' }
    ],
    defaultValue: 'all'
}
```

##### TextInput Filter

Free-text input field.

```javascript
{
    type: 'TextInput',
    label: 'Author Name',
    defaultValue: ''
}
```

##### CheckboxGroup Filter

Multiple selection checkboxes.

```javascript
{
    type: 'CheckboxGroup',
    label: 'Genres',
    options: [
        { label: 'Action', value: 'action' },
        { label: 'Romance', value: 'romance' },
        { label: 'Fantasy', value: 'fantasy' }
    ],
    defaultValues: ['action']
}
```

##### ExcludableCheckboxGroup Filter

Tri-state checkboxes (include/exclude/none).

```javascript
{
    type: 'ExcludableCheckboxGroup',
    label: 'Genres',
    options: [
        { label: 'Action', value: 'action' },
        { label: 'Horror', value: 'horror' }
    ],
    included: ['action'],
    excluded: ['horror']
}
```

---

### Available Libraries

Plugins have access to the following JavaScript libraries:

#### cheerio

HTML parsing and DOM manipulation (similar to jQuery).

```javascript
const cheerio = require('cheerio');

async function parseNovel(novelPath) {
    const html = await fetch(novelPath).then(res => res.text());
    const $ = cheerio.load(html);
    
    const title = $('h1.title').text();
    const cover = $('img.cover').attr('src');
    
    return { name: title, cover, /* ... */ };
}
```

#### dayjs

Date manipulation and formatting.

```javascript
const dayjs = require('dayjs');

const releaseTime = dayjs('2024-01-01').format('YYYY-MM-DD');
```

#### urlencode

URL encoding utilities.

```javascript
const urlencode = require('urlencode');

const encoded = urlencode('search query with spaces');
// Output: search%20query%20with%20spaces
```

#### fetch()

HTTP requests (Fetch API compatible).

```javascript
const response = await fetch('https://example.com/api/novels', {
    method: 'GET',
    headers: {
        'User-Agent': 'IReader/1.0'
    }
});

const data = await response.json();
```

#### Storage

Persistent key-value storage.

```javascript
// Set value with optional expiration (in milliseconds)
await storage.set('cache-key', { data: 'value' }, 3600000); // 1 hour

// Get value
const cached = await storage.get('cache-key');

// Delete value
await storage.delete('cache-key');

// Clear all storage
await storage.clearAll();

// Get all keys
const keys = await storage.getAllKeys();
```

---

### Security Considerations

#### Sandboxing

- Plugins run in an isolated JavaScript environment
- No access to file system (except plugin-specific storage)
- No access to native APIs or system commands
- Network requests go through IReader's HTTP client

#### Forbidden Patterns

The following patterns are rejected during plugin validation:

- `eval()`
- `Function()` constructor
- `require('fs')`
- `require('child_process')`
- `require('process')`
- `__dirname`, `__filename`
- `process.exit()`, `process.env`

#### Permission Model

Plugins declare required permissions in metadata:

```javascript
const permissions = ['NETWORK', 'STORAGE'];
```

Available permissions:
- `NETWORK`: Make HTTP requests
- `STORAGE`: Persist data
- `WEBVIEW`: Use WebView for authentication (future)

---

### Performance Optimization

#### Engine Pooling

JS engines are pooled and reused across plugin invocations to minimize initialization overhead.

```kotlin
val pool = JSEnginePool(maxPoolSize = 10, idleTimeout = 5.minutes)
val engine = pool.getOrCreate(pluginId)
```

#### Code Caching

Compiled JavaScript code is cached to avoid re-parsing:

```kotlin
val cache = JSCodeCache(maxSize = 50.MB)
val compiled = cache.getOrCompile(pluginId, code)
```

#### Concurrent Execution Limits

Maximum concurrent plugin executions are limited to prevent resource exhaustion:

```kotlin
val semaphore = Semaphore(maxConcurrentExecutions = 5)
```

#### Memory Limits

Each plugin execution is limited to 64MB of memory:

```kotlin
engine.setMemoryLimit(64 * 1024 * 1024L)
```

---

### Error Handling

#### Error Types

```kotlin
sealed class JSPluginError : Exception() {
    data class LoadError(val pluginId: String, override val cause: Throwable?)
    data class ExecutionError(val pluginId: String, val method: String, override val cause: Throwable?)
    data class TimeoutError(val pluginId: String, val method: String)
    data class ValidationError(val pluginId: String, val reason: String)
    data class NetworkError(val pluginId: String, val url: String, override val cause: Throwable?)
}
```

#### Error Messages

User-friendly error messages are generated using `toUserMessage()`:

```kotlin
try {
    val novels = bridge.popularNovels(1, emptyMap())
} catch (e: JSPluginError) {
    showError(e.toUserMessage())
}
```

---

### Testing

#### Unit Tests

Located in `domain/src/commonTest/kotlin/ireader/domain/js/`:

- `JSEngineTest.kt`: Tests script execution, error handling, timeouts
- `JSLibraryProviderTest.kt`: Tests require(), fetch(), Storage
- `JSPluginBridgeTest.kt`: Tests method invocation, data conversion
- `JSPluginSourceTest.kt`: Tests Source interface implementation
- `JSFilterConverterTest.kt`: Tests filter parsing and conversion
- `JSPluginValidatorTest.kt`: Tests code and metadata validation

#### Integration Tests

Located in `domain/src/androidTest/kotlin/ireader/domain/js/`:

- `JSPluginIntegrationTest.kt`: Tests complete flow from loading to reading

#### Running Tests

```bash
# Run all tests
./gradlew domain:test

# Run specific test
./gradlew domain:test --tests JSPluginValidatorTest

# Run with coverage
./gradlew domain:koverHtmlReport
```

---

### Debugging

#### Debug Mode

Enable debug mode in settings to see detailed plugin execution logs:

```kotlin
val settings = JSPluginSettings(debugMode = true)
```

#### Logging

All plugin operations are logged with the `[JSPlugin]` prefix:

```
[JSPlugin] Loading plugin: example-plugin
[JSPlugin] Calling popularNovels(page=1) on example-plugin
[JSPlugin] popularNovels completed in 1234ms
```

#### Performance Metrics

View plugin performance metrics in settings:

```kotlin
data class PluginPerformanceMetrics(
    val pluginId: String,
    val loadTime: Long,
    val avgExecutionTime: Long,
    val maxExecutionTime: Long,
    val errorRate: Float,
    val memoryUsage: Long
)
```

---

### Platform Differences

#### Android

- **Engine**: QuickJS (lightweight, fast)
- **Plugins Directory**: `/data/data/com.ireader/files/js-plugins/`
- **Storage**: SharedPreferences
- **Memory Limit**: 64MB per plugin

#### Desktop

- **Engine**: GraalVM JavaScript (full ES6+ support)
- **Plugins Directory**: `~/.ireader/js-plugins/`
- **Storage**: File-based
- **Memory Limit**: 128MB per plugin

---

### Future Enhancements

- Plugin marketplace for browsing and installing plugins
- In-app plugin editor and debugger
- WebView integration for authentication
- Plugin sharing and import/export
- Multi-source plugins (aggregate multiple sources)
- Plugin ratings and reviews

---

### References

- [LNReader Plugin Specification](https://github.com/LNReader/lnreader-plugins)
- [QuickJS Documentation](https://bellard.org/quickjs/)
- [GraalVM JavaScript](https://www.graalvm.org/javascript/)
- [Cheerio Documentation](https://cheerio.js.org/)
- [Day.js Documentation](https://day.js.org/)
