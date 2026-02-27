# Source-API Integration Improvements

## Problem Statement

External developers report that source-api is "dirty" and difficult to integrate into their codebases. The main issues are:

### 1. Tight Platform Coupling
```kotlin
// Android implementation requires Android Context
actual class HttpClients(
    context: Context,  // ❌ Android-specific
    browseEngine: BrowserEngine,
    cookiesStorage: CookiesStorage,
    webViewCookieJar: WebViewCookieJar,
    preferencesStore: PreferenceStore,  // ❌ IReader-specific
    webViewManager: WebViewManger? = null,
    networkConfig: NetworkConfig = NetworkConfig()
)
```

### 2. Complex Dependency Graph
Users need to understand and provide:
- `Context` (Android)
- `BrowserEngine` (platform-specific)
- `CookiesStorage` (Ktor-specific)
- `WebViewCookieJar` (custom implementation)
- `PreferenceStore` (IReader-specific interface)
- `WebViewManager` (optional but needed for Cloudflare)
- `NetworkConfig` (configuration object)

### 3. No Simple Entry Points
```kotlin
// What users want:
val httpClients = HttpClients.create()

// What they currently need:
val context = getAndroidContext()
val prefs = MyPreferenceStoreImpl()
val cookies = AcceptAllCookiesStorage()
val webViewJar = WebViewCookieJar(cookies)
val browser = BrowserEngine(context, webViewJar)
val webViewManager = WebViewManager(context)
val httpClients = HttpClients(context, browser, cookies, webViewJar, prefs, webViewManager)
```

### 4. IReader-Specific Abstractions
- `PreferenceStore` - Custom interface instead of standard key-value store
- `Log` - Uses Kermit but wrapped in custom API
- `Dependencies` - Couples HTTP clients with preferences

### 5. No Standalone Usage
Can't use individual components without the full stack:
- Want just Cloudflare bypass? Need full HttpClients
- Want just rate limiting? Need full HttpClients
- Want just browser engine? Need Context + WebViewManager

---

## Proposed Solutions

### Phase 1: Builder Pattern & Factory Methods

#### 1.1 HttpClients Builder

```kotlin
// New builder API in commonMain
class HttpClientsBuilder {
    private var networkConfig: NetworkConfig = NetworkConfig()
    private var preferenceStore: PreferenceStore? = null
    private var browserEngine: BrowserEngineInterface? = null
    private var cookieStorage: CookiesStorage? = null
    private var enableCloudflare: Boolean = true
    private var enableCaching: Boolean = true
    private var enableCookies: Boolean = true
    
    fun networkConfig(config: NetworkConfig) = apply { 
        this.networkConfig = config 
    }
    
    fun preferenceStore(store: PreferenceStore) = apply { 
        this.preferenceStore = store 
    }
    
    fun browserEngine(engine: BrowserEngineInterface) = apply { 
        this.browserEngine = engine 
    }
    
    fun cookieStorage(storage: CookiesStorage) = apply { 
        this.cookieStorage = storage 
    }
    
    fun enableCloudflare(enabled: Boolean) = apply { 
        this.enableCloudflare = enabled 
    }
    
    fun enableCaching(enabled: Boolean) = apply { 
        this.enableCaching = enabled 
    }
    
    fun enableCookies(enabled: Boolean) = apply { 
        this.enableCookies = enabled 
    }
    
    fun build(): HttpClientsInterface {
        return createHttpClients(
            networkConfig = networkConfig,
            preferenceStore = preferenceStore,
            browserEngine = browserEngine,
            cookieStorage = cookieStorage,
            enableCloudflare = enableCloudflare,
            enableCaching = enableCaching,
            enableCookies = enableCookies
        )
    }
}

// Platform-specific factory
expect fun createHttpClients(
    networkConfig: NetworkConfig,
    preferenceStore: PreferenceStore?,
    browserEngine: BrowserEngineInterface?,
    cookieStorage: CookiesStorage?,
    enableCloudflare: Boolean,
    enableCaching: Boolean,
    enableCookies: Boolean
): HttpClientsInterface
```

#### 1.2 Simple Factory Methods

```kotlin
object HttpClientsFactory {
    
    /**
     * Create minimal HTTP clients with defaults
     * No Cloudflare, no caching, in-memory cookies
     */
    fun createMinimal(): HttpClientsInterface {
        return HttpClientsBuilder().build()
    }
    
    /**
     * Create standard HTTP clients with caching and cookies
     */
    fun createStandard(
        cacheDir: String? = null,
        preferenceStore: PreferenceStore? = null
    ): HttpClientsInterface {
        return HttpClientsBuilder()
            .enableCaching(cacheDir != null)
            .enableCookies(true)
            .preferenceStore(preferenceStore)
            .build()
    }
    
    /**
     * Create full-featured HTTP clients with Cloudflare bypass
     * Requires platform-specific setup
     */
    fun createFull(
        preferenceStore: PreferenceStore,
        browserEngine: BrowserEngineInterface,
        networkConfig: NetworkConfig = NetworkConfig()
    ): HttpClientsInterface {
        return HttpClientsBuilder()
            .networkConfig(networkConfig)
            .preferenceStore(preferenceStore)
            .browserEngine(browserEngine)
            .enableCloudflare(true)
            .enableCaching(true)
            .enableCookies(true)
            .build()
    }
}
```

#### 1.3 Platform-Specific Builders

```kotlin
// Android-specific builder
object AndroidHttpClientsFactory {
    
    /**
     * Create HTTP clients with Android defaults
     */
    fun create(
        context: Context,
        preferenceStore: PreferenceStore? = null,
        enableCloudflare: Boolean = true
    ): HttpClientsInterface {
        val prefs = preferenceStore ?: InMemoryPreferenceStore()
        val cookies = AcceptAllCookiesStorage()
        val webViewJar = WebViewCookieJar(cookies)
        val browser = if (enableCloudflare) {
            BrowserEngine(context, webViewJar)
        } else {
            null
        }
        
        return HttpClientsBuilder()
            .preferenceStore(prefs)
            .browserEngine(browser)
            .cookieStorage(cookies)
            .enableCloudflare(enableCloudflare)
            .build()
    }
    
    /**
     * Create with custom configuration
     */
    fun create(
        context: Context,
        config: AndroidHttpConfig
    ): HttpClientsInterface {
        // Build from config
    }
}

data class AndroidHttpConfig(
    val preferenceStore: PreferenceStore? = null,
    val enableCloudflare: Boolean = true,
    val enableCaching: Boolean = true,
    val cacheSize: Long = 15L * 1024 * 1024,
    val networkConfig: NetworkConfig = NetworkConfig()
)
```

---

### Phase 2: Decouple from IReader-Specific Abstractions

#### 2.1 Standard PreferenceStore Adapter

```kotlin
/**
 * Adapter to use any key-value store as PreferenceStore
 */
class KeyValuePreferenceStore(
    private val getString: (String, String) -> String,
    private val putString: (String, String) -> Unit,
    private val getLong: (String, Long) -> Long,
    private val putLong: (String, Long) -> Unit,
    private val getInt: (String, Int) -> Int,
    private val putInt: (String, Int) -> Unit,
    private val getBoolean: (String, Boolean) -> Boolean,
    private val putBoolean: (String, Boolean) -> Unit
) : PreferenceStore {
    
    override fun getString(key: String, defaultValue: String): Preference<String> {
        return SimplePreference(
            key = key,
            defaultValue = defaultValue,
            get = { getString(key, defaultValue) },
            set = { putString(key, it) }
        )
    }
    
    // ... implement other methods
}

/**
 * In-memory implementation for testing or simple use cases
 */
class InMemoryPreferenceStore : PreferenceStore {
    private val storage = mutableMapOf<String, Any>()
    
    override fun getString(key: String, defaultValue: String): Preference<String> {
        return SimplePreference(
            key = key,
            defaultValue = defaultValue,
            get = { storage[key] as? String ?: defaultValue },
            set = { storage[key] = it }
        )
    }
    
    // ... implement other methods
}

/**
 * Android SharedPreferences adapter
 */
class SharedPreferencesStore(
    private val sharedPreferences: SharedPreferences
) : PreferenceStore {
    override fun getString(key: String, defaultValue: String): Preference<String> {
        return SimplePreference(
            key = key,
            defaultValue = defaultValue,
            get = { sharedPreferences.getString(key, defaultValue) ?: defaultValue },
            set = { sharedPreferences.edit().putString(key, it).apply() }
        )
    }
    
    // ... implement other methods
}
```

#### 2.2 Optional Logging

```kotlin
/**
 * Pluggable logging interface
 */
interface Logger {
    fun verbose(message: String)
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * No-op logger for users who don't want logging
 */
object NoOpLogger : Logger {
    override fun verbose(message: String) {}
    override fun debug(message: String) {}
    override fun info(message: String) {}
    override fun warn(message: String) {}
    override fun error(message: String, throwable: Throwable?) {}
}

/**
 * Console logger for simple use cases
 */
object ConsoleLogger : Logger {
    override fun verbose(message: String) = println("[VERBOSE] $message")
    override fun debug(message: String) = println("[DEBUG] $message")
    override fun info(message: String) = println("[INFO] $message")
    override fun warn(message: String) = println("[WARN] $message")
    override fun error(message: String, throwable: Throwable?) {
        println("[ERROR] $message")
        throwable?.printStackTrace()
    }
}

/**
 * Adapter for existing Log object
 */
object KermitLoggerAdapter : Logger {
    override fun verbose(message: String) = Log.verbose(message)
    override fun debug(message: String) = Log.debug(message)
    override fun info(message: String) = Log.info(message)
    override fun warn(message: String) = Log.warn(message)
    override fun error(message: String, throwable: Throwable?) {
        if (throwable != null) Log.error(throwable, message)
        else Log.error(message)
    }
}

// Global logger configuration
object SourceApiConfig {
    var logger: Logger = NoOpLogger
    
    fun setLogger(logger: Logger) {
        this.logger = logger
    }
}
```

#### 2.3 Simplified Dependencies

```kotlin
/**
 * Minimal dependencies for source creation
 */
interface SourceDependencies {
    val httpClients: HttpClientsInterface
}

/**
 * Standard implementation
 */
class StandardSourceDependencies(
    override val httpClients: HttpClientsInterface
) : SourceDependencies

/**
 * Full implementation with preferences (backward compatible)
 */
class FullSourceDependencies(
    override val httpClients: HttpClientsInterface,
    val preferences: PreferenceStore
) : SourceDependencies

// Update Dependencies class to be backward compatible
class Dependencies(
    override val httpClients: HttpClientsInterface,
    val preferences: PreferenceStore
) : SourceDependencies
```

---

### Phase 3: Standalone Component Usage

#### 3.1 Standalone Cloudflare Bypass

```kotlin
/**
 * Standalone Cloudflare bypass without full HttpClients
 */
object CloudflareBypass {
    
    /**
     * Create a bypass manager with minimal dependencies
     */
    fun create(
        cookieStore: CloudflareCookieStore? = null,
        browserEngine: BrowserEngineInterface? = null
    ): CloudflareBypassManager {
        val store = cookieStore ?: InMemoryCloudfareCookieStore()
        val strategies = buildList {
            add(CookieReplayStrategy(store))
            if (browserEngine != null && browserEngine.isAvailable()) {
                add(WebViewBypassStrategy(browserEngine, InMemoryFingerprintManager()))
            }
        }
        
        return CloudflareBypassManager(
            strategies = strategies,
            cookieStore = store
        )
    }
    
    /**
     * Quick bypass for a single URL
     */
    suspend fun bypass(
        url: String,
        httpClient: HttpClient,
        browserEngine: BrowserEngineInterface? = null
    ): BypassResult {
        val manager = create(browserEngine = browserEngine)
        val response = httpClient.get(url)
        
        if (CloudflareDetector.isChallengeLikely(response)) {
            return manager.bypass(url, response)
        }
        
        return BypassResult.NotNeeded
    }
}

// Usage:
val result = CloudflareBypass.bypass(url, httpClient, browserEngine)
when (result) {
    is BypassResult.Success -> {
        // Use result.cookie
    }
    is BypassResult.Failed -> {
        // Handle failure
    }
    else -> {}
}
```

#### 3.2 Standalone Rate Limiter

```kotlin
/**
 * Standalone rate limiter without full HttpClients
 */
object RateLimiting {
    
    /**
     * Create a rate limiter with default config
     */
    fun create(
        requestsPerSecond: Double = 2.0,
        burstSize: Int = 5
    ): RateLimiter {
        return AdaptiveRateLimiter(
            defaultConfig = RateLimitConfig(
                requestsPerSecond = requestsPerSecond,
                burstSize = burstSize
            )
        )
    }
    
    /**
     * Wrap an HttpClient with rate limiting
     */
    fun HttpClient.withRateLimiting(
        requestsPerSecond: Double = 2.0
    ): HttpClient {
        val limiter = create(requestsPerSecond)
        
        return HttpClient(this.engine) {
            install(RateLimitPlugin) {
                rateLimiter = limiter
            }
        }
    }
}

// Usage:
val rateLimitedClient = httpClient.withRateLimiting(requestsPerSecond = 1.0)
```

#### 3.3 Standalone Browser Engine

```kotlin
/**
 * Standalone browser engine creation
 */
object BrowserEngines {
    
    /**
     * Create browser engine for Android
     */
    fun createAndroid(
        context: Context,
        cookieJar: WebViewCookieJar? = null
    ): BrowserEngineInterface {
        val jar = cookieJar ?: WebViewCookieJar(AcceptAllCookiesStorage())
        return BrowserEngine(context, jar)
    }
    
    /**
     * Create stub browser engine for platforms without WebView
     */
    fun createStub(): BrowserEngineInterface {
        return StubBrowserEngine()
    }
    
    /**
     * Check if browser engine is available on current platform
     */
    fun isAvailable(): Boolean {
        return when (Platform.current) {
            Platform.ANDROID -> true
            else -> false
        }
    }
}
```

---

### Phase 4: Documentation & Examples

#### 4.1 Quick Start Guide

```kotlin
/**
 * QUICK START EXAMPLES
 */

// Example 1: Minimal setup (no Cloudflare, no caching)
fun minimalSetup() {
    val httpClients = HttpClientsFactory.createMinimal()
    val deps = StandardSourceDependencies(httpClients)
    val source = MySource(deps)
}

// Example 2: Standard setup (with caching and cookies)
fun standardSetup() {
    val prefs = InMemoryPreferenceStore()
    val httpClients = HttpClientsFactory.createStandard(
        cacheDir = "/path/to/cache",
        preferenceStore = prefs
    )
    val deps = StandardSourceDependencies(httpClients)
    val source = MySource(deps)
}

// Example 3: Android with Cloudflare bypass
fun androidSetup(context: Context) {
    val prefs = SharedPreferencesStore(
        context.getSharedPreferences("source_api", Context.MODE_PRIVATE)
    )
    val httpClients = AndroidHttpClientsFactory.create(
        context = context,
        preferenceStore = prefs,
        enableCloudflare = true
    )
    val deps = StandardSourceDependencies(httpClients)
    val source = MySource(deps)
}

// Example 4: Custom configuration
fun customSetup() {
    val httpClients = HttpClientsBuilder()
        .networkConfig(NetworkConfig(
            connectTimeoutSeconds = 60,
            readTimeoutMinutes = 10
        ))
        .enableCaching(true)
        .enableCookies(true)
        .build()
    
    val deps = StandardSourceDependencies(httpClients)
    val source = MySource(deps)
}

// Example 5: Using standalone components
suspend fun standaloneComponents(context: Context) {
    // Just rate limiting
    val httpClient = HttpClient(OkHttp)
    val rateLimitedClient = httpClient.withRateLimiting(requestsPerSecond = 1.0)
    
    // Just Cloudflare bypass
    val browserEngine = BrowserEngines.createAndroid(context)
    val bypassResult = CloudflareBypass.bypass(
        url = "https://example.com",
        httpClient = httpClient,
        browserEngine = browserEngine
    )
}
```

#### 4.2 Migration Guide

```kotlin
/**
 * MIGRATION FROM OLD API TO NEW API
 */

// OLD WAY (IReader-specific)
fun oldWay(context: Context) {
    val prefs = MyIReaderPreferenceStore()
    val cookies = AcceptAllCookiesStorage()
    val webViewJar = WebViewCookieJar(cookies)
    val browser = BrowserEngine(context, webViewJar)
    val webViewManager = WebViewManager(context)
    
    val httpClients = HttpClients(
        context = context,
        browseEngine = browser,
        cookiesStorage = cookies,
        webViewCookieJar = webViewJar,
        preferencesStore = prefs,
        webViewManager = webViewManager
    )
    
    val deps = Dependencies(httpClients, prefs)
}

// NEW WAY (Clean, simple)
fun newWay(context: Context) {
    val prefs = SharedPreferencesStore(
        context.getSharedPreferences("app", Context.MODE_PRIVATE)
    )
    
    val httpClients = AndroidHttpClientsFactory.create(
        context = context,
        preferenceStore = prefs
    )
    
    val deps = StandardSourceDependencies(httpClients)
}

// EVEN SIMPLER (Defaults)
fun simplestWay(context: Context) {
    val httpClients = AndroidHttpClientsFactory.create(context)
    val deps = StandardSourceDependencies(httpClients)
}
```

---

### Phase 5: Backward Compatibility

#### 5.1 Deprecation Strategy

```kotlin
/**
 * Keep old API but mark as deprecated
 */
@Deprecated(
    message = "Use HttpClientsFactory.create() or AndroidHttpClientsFactory.create() instead",
    replaceWith = ReplaceWith(
        "AndroidHttpClientsFactory.create(context, preferenceStore)",
        "ireader.core.http.AndroidHttpClientsFactory"
    ),
    level = DeprecationLevel.WARNING
)
actual class HttpClients(
    context: Context,
    browseEngine: BrowserEngine,
    cookiesStorage: CookiesStorage,
    webViewCookieJar: WebViewCookieJar,
    preferencesStore: PreferenceStore,
    webViewManager: WebViewManger? = null,
    networkConfig: NetworkConfig = NetworkConfig()
) : HttpClientsInterface {
    // Keep implementation for backward compatibility
}

/**
 * Keep old Dependencies but mark as deprecated
 */
@Deprecated(
    message = "Use StandardSourceDependencies or FullSourceDependencies instead",
    replaceWith = ReplaceWith(
        "FullSourceDependencies(httpClients, preferences)",
        "ireader.core.source.FullSourceDependencies"
    ),
    level = DeprecationLevel.WARNING
)
class Dependencies(
    override val httpClients: HttpClientsInterface,
    val preferences: PreferenceStore
) : SourceDependencies
```

#### 5.2 Compatibility Layer

```kotlin
/**
 * Extension functions for smooth migration
 */

// Convert old Dependencies to new SourceDependencies
fun Dependencies.toSourceDependencies(): SourceDependencies {
    return FullSourceDependencies(httpClients, preferences)
}

// Create Dependencies from SourceDependencies (for legacy code)
fun SourceDependencies.toDependencies(
    preferences: PreferenceStore
): Dependencies {
    return Dependencies(httpClients, preferences)
}
```

---

## Implementation Priority

### High Priority (Phase 1-2)
1. ✅ Create `HttpClientsBuilder` with fluent API
2. ✅ Create `HttpClientsFactory` with simple factory methods
3. ✅ Create `AndroidHttpClientsFactory` for Android-specific setup
4. ✅ Create `InMemoryPreferenceStore` for testing
5. ✅ Create `KeyValuePreferenceStore` adapter
6. ✅ Create `SharedPreferencesStore` for Android

### Medium Priority (Phase 3)
7. ✅ Create standalone `CloudflareBypass` utility
8. ✅ Create standalone `RateLimiting` utility
9. ✅ Create `BrowserEngines` factory
10. ✅ Create `SourceDependencies` interface
11. ✅ Create `StandardSourceDependencies` implementation

### Lower Priority (Phase 4-5)
12. ⏳ Write comprehensive documentation
13. ⏳ Write migration guide
14. ⏳ Add deprecation warnings to old API
15. ⏳ Create example projects for different platforms

---

## Benefits

### For External Developers

1. **Simple Entry Point**
   ```kotlin
   // One line to get started
   val httpClients = HttpClientsFactory.createMinimal()
   ```

2. **No IReader Dependencies**
   ```kotlin
   // Use standard Android SharedPreferences
   val prefs = SharedPreferencesStore(sharedPreferences)
   ```

3. **Gradual Feature Adoption**
   ```kotlin
   // Start simple, add features as needed
   val basic = HttpClientsFactory.createMinimal()
   val withCache = HttpClientsFactory.createStandard(cacheDir)
   val withCloudflare = AndroidHttpClientsFactory.create(context)
   ```

4. **Standalone Components**
   ```kotlin
   // Use just what you need
   val rateLimiter = RateLimiting.create()
   val bypass = CloudflareBypass.create()
   ```

### For IReader

1. **Backward Compatible** - Existing code continues to work
2. **Cleaner API** - New code can use simpler API
3. **Better Testing** - In-memory implementations for tests
4. **More Adoption** - Easier for others to use = more contributors

---

## Example: External Project Integration

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.ireader:source-api:1.0.0")
}

// MyApp.kt
class MyApp : Application() {
    lateinit var httpClients: HttpClientsInterface
    
    override fun onCreate() {
        super.onCreate()
        
        // Simple setup - just works!
        httpClients = AndroidHttpClientsFactory.create(
            context = this,
            config = AndroidHttpConfig(
                enableCloudflare = true,
                cacheSize = 50L * 1024 * 1024 // 50MB
            )
        )
        
        // Optional: Configure logging
        SourceApiConfig.setLogger(MyCustomLogger())
    }
}

// MySource.kt
class MyNovelSource : HttpSource {
    override val id = 12345L
    override val name = "My Novel Site"
    override val baseUrl = "https://example.com"
    
    // Simple constructor - just needs HTTP clients
    constructor(httpClients: HttpClientsInterface) : super(
        StandardSourceDependencies(httpClients)
    )
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        val response = client.get("$baseUrl/novels?page=$page")
        return parseList(response.asJsoup())
    }
}

// Usage
val source = MyNovelSource(app.httpClients)
val novels = source.getMangaList(page = 1)
```

---

## Next Steps

1. Review this proposal with the team
2. Get feedback from external developers
3. Create feature branch for implementation
4. Implement Phase 1 (Builder + Factory)
5. Write tests for new API
6. Update documentation
7. Release as minor version (backward compatible)
8. Deprecate old API in next major version

---

## Questions for Discussion

1. Should we keep `Dependencies` class or fully migrate to `SourceDependencies`?
2. Should `PreferenceStore` be replaced with a standard interface (like `Map<String, Any>`)?
3. Should we provide a Ktor plugin for rate limiting?
4. Should we create separate artifacts for standalone components?
5. What's the timeline for deprecating the old API?
