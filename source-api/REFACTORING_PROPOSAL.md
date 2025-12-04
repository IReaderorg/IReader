# Source-API Refactoring Proposal

## Executive Summary

This document outlines a comprehensive refactoring plan for the `source-api` module, focusing on:
1. Enhanced Cloudflare bypass mechanisms
2. Improved HTTP client architecture
3. Better cross-platform support
4. Modern anti-bot detection evasion
5. Performance optimizations

---

## Current State Analysis

### Strengths
- Clean KMP architecture with expect/actual pattern
- Good separation of concerns (HTTP, Source, Parsing)
- Comprehensive error handling with `FetchResult` sealed class
- Retry strategies with exponential backoff
- Cookie synchronization between WebView and HTTP clients

### Weaknesses Identified

#### 1. Cloudflare Bypass Limitations
- **Android-only**: Full bypass only works on Android via WebView
- **Desktop stub**: No real Cloudflare bypass on desktop
- **iOS missing**: No iOS implementation
- **Outdated detection**: Current detection only checks for 403/503 + "cloudflare" header

#### 2. Browser Fingerprinting Vulnerabilities
- Basic user agent spoofing only
- No canvas/WebGL fingerprint randomization
- No navigator property spoofing
- Missing timezone/language consistency

#### 3. Rate Limiting Issues
- Simple retry logic without intelligent backoff
- No request queuing or throttling
- Missing per-domain rate limiting

#### 4. Missing Modern Anti-Bot Evasion
- No TLS fingerprint manipulation
- No HTTP/2 fingerprint handling
- Missing challenge solver integration
- No headless browser detection evasion

---

## Proposed Improvements

### Phase 1: Enhanced Cloudflare Detection & Bypass

#### 1.1 Improved Challenge Detection

```kotlin
// New CloudflareDetector class
sealed class CloudflareChallenge {
    object None : CloudflareChallenge()
    data class JSChallenge(val rayId: String?) : CloudflareChallenge()
    data class CaptchaChallenge(val siteKey: String, val rayId: String?) : CloudflareChallenge()
    data class TurnstileChallenge(val siteKey: String, val rayId: String?) : CloudflareChallenge()
    data class ManagedChallenge(val rayId: String?) : CloudflareChallenge()
    data class BlockedIP(val rayId: String?) : CloudflareChallenge()
}
```

#### 1.2 Multi-Strategy Bypass System

```kotlin
interface CloudflareBypassStrategy {
    val priority: Int
    val name: String
    suspend fun canHandle(challenge: CloudflareChallenge): Boolean
    suspend fun bypass(
        url: String,
        challenge: CloudflareChallenge,
        config: BypassConfig
    ): BypassResult
}

// Strategies to implement:
// 1. WebViewStrategy (Android) - Current approach, enhanced
// 2. HeadlessBrowserStrategy (Desktop) - Using Playwright/Puppeteer via JNI
// 3. CookieReplayStrategy - Reuse valid cf_clearance cookies
// 4. FlareSolverrStrategy - External solver service integration
// 5. TurnstileSolverStrategy - For Turnstile challenges
```

#### 1.3 Cookie Persistence & Sharing

```kotlin
interface CloudflareCookieStore {
    suspend fun getClearanceCookie(domain: String): ClearanceCookie?
    suspend fun saveClearanceCookie(domain: String, cookie: ClearanceCookie)
    suspend fun isValid(cookie: ClearanceCookie): Boolean
    suspend fun invalidate(domain: String)
    suspend fun getAll(): List<ClearanceCookie>
}

data class ClearanceCookie(
    val cfClearance: String,
    val cfBm: String?,
    val userAgent: String,
    val timestamp: Long,
    val expiresAt: Long,
    val domain: String
)
```

---

### Phase 2: Browser Fingerprint Evasion

#### 2.1 Fingerprint Profile System

```kotlin
data class BrowserFingerprint(
    val userAgent: String,
    val platform: String,
    val vendor: String,
    val language: String,
    val languages: List<String>,
    val timezone: String,
    val screenResolution: ScreenResolution,
    val colorDepth: Int,
    val hardwareConcurrency: Int,
    val deviceMemory: Int,
    val webglVendor: String,
    val webglRenderer: String,
    val canvasNoise: Float,
    val audioContextNoise: Float
)

object FingerprintGenerator {
    fun generateConsistentProfile(seed: Long): BrowserFingerprint
    fun getChromeMobileProfile(): BrowserFingerprint
    fun getChromeDesktopProfile(): BrowserFingerprint
    fun getFirefoxProfile(): BrowserFingerprint
}
```

#### 2.2 JavaScript Injection for Evasion

```kotlin
object FingerprintEvasionScripts {
    // Inject before page load to spoof navigator properties
    val navigatorSpoof = """
        Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
        Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });
        Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });
        // ... more spoofing
    """
    
    // Canvas fingerprint randomization
    val canvasNoise = """
        const originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
        HTMLCanvasElement.prototype.toDataURL = function(type) {
            // Add subtle noise to canvas output
            // ...
        };
    """
}
```

---

### Phase 3: TLS/HTTP Fingerprint Handling

#### 3.1 TLS Fingerprint Randomization

```kotlin
interface TLSFingerprintConfig {
    val cipherSuites: List<String>
    val extensions: List<TLSExtension>
    val supportedVersions: List<String>
    val signatureAlgorithms: List<String>
    val supportedGroups: List<String>
}

// Predefined profiles matching real browsers
object TLSProfiles {
    val CHROME_120: TLSFingerprintConfig
    val FIREFOX_121: TLSFingerprintConfig
    val SAFARI_17: TLSFingerprintConfig
    val EDGE_120: TLSFingerprintConfig
}
```

#### 3.2 HTTP/2 Fingerprint Matching

```kotlin
data class Http2Fingerprint(
    val settingsOrder: List<Http2Setting>,
    val windowUpdateIncrement: Int,
    val headerOrder: List<String>,
    val pseudoHeaderOrder: List<String>,
    val priorityFrames: Boolean
)

// Match Chrome's HTTP/2 fingerprint
object Http2Profiles {
    val CHROME = Http2Fingerprint(
        settingsOrder = listOf(
            Http2Setting.HEADER_TABLE_SIZE,
            Http2Setting.ENABLE_PUSH,
            Http2Setting.MAX_CONCURRENT_STREAMS,
            Http2Setting.INITIAL_WINDOW_SIZE,
            Http2Setting.MAX_FRAME_SIZE,
            Http2Setting.MAX_HEADER_LIST_SIZE
        ),
        windowUpdateIncrement = 15663105,
        headerOrder = listOf(":method", ":authority", ":scheme", ":path"),
        pseudoHeaderOrder = listOf(":method", ":authority", ":scheme", ":path"),
        priorityFrames = true
    )
}
```

---

### Phase 4: Intelligent Rate Limiting & Request Management

#### 4.1 Per-Domain Rate Limiter

```kotlin
interface RateLimiter {
    suspend fun acquire(domain: String, weight: Int = 1)
    fun tryAcquire(domain: String, weight: Int = 1): Boolean
    fun setLimit(domain: String, config: RateLimitConfig)
    fun getStats(domain: String): RateLimitStats
}

data class RateLimitConfig(
    val requestsPerSecond: Double = 2.0,
    val burstSize: Int = 5,
    val minDelayMs: Long = 100,
    val maxDelayMs: Long = 5000,
    val adaptiveEnabled: Boolean = true
)

// Adaptive rate limiting based on server responses
class AdaptiveRateLimiter : RateLimiter {
    // Automatically adjusts rate based on:
    // - 429 responses
    // - Response times
    // - Retry-After headers
    // - Cloudflare challenges
}
```

#### 4.2 Request Queue System

```kotlin
interface RequestQueue {
    suspend fun <T> enqueue(
        domain: String,
        priority: Priority = Priority.NORMAL,
        request: suspend () -> T
    ): T
    
    fun cancel(domain: String)
    fun cancelAll()
    fun getQueueSize(domain: String): Int
}

enum class Priority { LOW, NORMAL, HIGH, CRITICAL }
```

---

### Phase 5: Desktop Cloudflare Bypass

#### 5.1 FlareSolverr Integration

```kotlin
/**
 * Integration with FlareSolverr service for desktop Cloudflare bypass
 * https://github.com/FlareSolverr/FlareSolverr
 */
interface FlareSolverrClient {
    suspend fun solve(request: FlareSolverrRequest): FlareSolverrResponse
    suspend fun createSession(sessionId: String): Boolean
    suspend fun destroySession(sessionId: String): Boolean
    fun isAvailable(): Boolean
}

data class FlareSolverrRequest(
    val cmd: String = "request.get",
    val url: String,
    val maxTimeout: Int = 60000,
    val session: String? = null,
    val cookies: List<Cookie>? = null,
    val returnOnlyCookies: Boolean = false,
    val proxy: ProxyConfig? = null
)

data class FlareSolverrResponse(
    val status: String,
    val message: String,
    val solution: Solution?
) {
    data class Solution(
        val url: String,
        val status: Int,
        val headers: Map<String, String>,
        val response: String,
        val cookies: List<Cookie>,
        val userAgent: String
    )
}
```

#### 5.2 Playwright/Puppeteer Bridge (Desktop)

```kotlin
/**
 * Bridge to headless browser for desktop platforms
 * Uses native process to run Playwright/Puppeteer
 */
interface HeadlessBrowserBridge {
    suspend fun launch(config: BrowserConfig): BrowserSession
    suspend fun close(session: BrowserSession)
    fun isInstalled(): Boolean
    suspend fun install(): Boolean
}

data class BrowserConfig(
    val headless: Boolean = true,
    val proxy: ProxyConfig? = null,
    val userAgent: String? = null,
    val viewport: Viewport = Viewport(1920, 1080),
    val timeout: Long = 30000
)
```

---

### Phase 6: Refactored HTTP Client Architecture

#### 6.1 New HttpClients Interface

```kotlin
interface HttpClientsInterface {
    // Existing
    val browser: BrowserEngine
    val default: HttpClient
    val cloudflareClient: HttpClient
    val config: NetworkConfig
    val sslConfig: SSLConfiguration
    val cookieSynchronizer: CookieSynchronizer
    
    // New additions
    val rateLimiter: RateLimiter
    val requestQueue: RequestQueue
    val cloudflareBypass: CloudflareBypassManager
    val fingerprintManager: FingerprintManager
    val cookieStore: CloudflareCookieStore
    
    // Convenience methods
    suspend fun fetchWithBypass(url: String, config: FetchConfig = FetchConfig()): FetchResult<String>
    suspend fun fetchJson<T>(url: String, deserializer: DeserializationStrategy<T>): FetchResult<T>
}

data class FetchConfig(
    val timeout: Long = 30000,
    val retries: Int = 3,
    val bypassCloudflare: Boolean = true,
    val useRateLimiter: Boolean = true,
    val priority: Priority = Priority.NORMAL,
    val headers: Map<String, String> = emptyMap(),
    val fingerprint: BrowserFingerprint? = null
)
```

#### 6.2 CloudflareBypassManager

```kotlin
class CloudflareBypassManager(
    private val strategies: List<CloudflareBypassStrategy>,
    private val cookieStore: CloudflareCookieStore,
    private val detector: CloudflareDetector
) {
    suspend fun bypass(url: String, response: HttpResponse): BypassResult {
        // 1. Check if we have valid cached cookies
        val cachedCookie = cookieStore.getClearanceCookie(url.extractDomain())
        if (cachedCookie != null && cookieStore.isValid(cachedCookie)) {
            return BypassResult.CachedCookie(cachedCookie)
        }
        
        // 2. Detect challenge type
        val challenge = detector.detect(response)
        if (challenge is CloudflareChallenge.None) {
            return BypassResult.NotNeeded
        }
        
        // 3. Try strategies in priority order
        for (strategy in strategies.sortedByDescending { it.priority }) {
            if (strategy.canHandle(challenge)) {
                val result = strategy.bypass(url, challenge, config)
                if (result is BypassResult.Success) {
                    cookieStore.saveClearanceCookie(url.extractDomain(), result.cookie)
                    return result
                }
            }
        }
        
        return BypassResult.Failed("All bypass strategies failed")
    }
}
```

---

### Phase 7: Enhanced Parsing & Content Extraction

#### 7.1 Intelligent Content Detector

```kotlin
interface ContentDetector {
    fun detectContentType(document: Document): ContentType
    fun findMainContent(document: Document): Element?
    fun findChapterContent(document: Document): List<String>
    fun detectEncoding(bytes: ByteArray): Charset
    fun detectLanguage(text: String): String
}

sealed class ContentType {
    object ChapterContent : ContentType()
    object BookDetails : ContentType()
    object ChapterList : ContentType()
    object SearchResults : ContentType()
    object ErrorPage : ContentType()
    object CloudflarePage : ContentType()
    data class Unknown(val hints: List<String>) : ContentType()
}
```

#### 7.2 Universal Content Extractor

```kotlin
class UniversalContentExtractor(
    private val detector: ContentDetector,
    private val cleaners: List<ContentCleaner>
) {
    fun extract(document: Document, hints: ExtractionHints? = null): ExtractionResult {
        // 1. Detect content type
        val contentType = detector.detectContentType(document)
        
        // 2. Find main content area
        val mainContent = detector.findMainContent(document)
        
        // 3. Apply cleaners
        val cleaned = cleaners.fold(mainContent) { content, cleaner ->
            cleaner.clean(content)
        }
        
        // 4. Extract text with formatting
        return ExtractionResult(
            content = extractFormattedText(cleaned),
            metadata = extractMetadata(document),
            confidence = calculateConfidence(cleaned)
        )
    }
}

interface ContentCleaner {
    fun clean(element: Element?): Element?
}

// Built-in cleaners
object AdRemover : ContentCleaner
object ScriptRemover : ContentCleaner
object NavigationRemover : ContentCleaner
object CommentRemover : ContentCleaner
object EmptyElementRemover : ContentCleaner
```

---

## Implementation Priority

### High Priority (Phase 1-2)
1. **CloudflareDetector** - Better challenge detection
2. **CloudflareCookieStore** - Cookie persistence and reuse
3. **FingerprintEvasionScripts** - Basic fingerprint spoofing
4. **AdaptiveRateLimiter** - Prevent getting blocked

### Medium Priority (Phase 3-4)
5. **FlareSolverrClient** - Desktop bypass solution
6. **TLS Fingerprint Profiles** - Match real browsers
7. **RequestQueue** - Better request management
8. **UniversalContentExtractor** - Improved parsing

### Lower Priority (Phase 5-7)
9. **HeadlessBrowserBridge** - Native browser integration
10. **HTTP/2 Fingerprinting** - Advanced evasion
11. **Full fingerprint randomization** - Canvas, WebGL, Audio

---

## File Structure Changes

```
source-api/src/commonMain/kotlin/ireader/core/
├── http/
│   ├── BrowserEngine.kt (existing, enhanced)
│   ├── HttpClients.kt (existing, enhanced)
│   ├── cloudflare/
│   │   ├── CloudflareDetector.kt (NEW)
│   │   ├── CloudflareChallenge.kt (NEW)
│   │   ├── CloudflareBypassManager.kt (NEW)
│   │   ├── CloudflareCookieStore.kt (NEW)
│   │   └── strategies/
│   │       ├── CloudflareBypassStrategy.kt (NEW)
│   │       ├── WebViewStrategy.kt (NEW)
│   │       ├── CookieReplayStrategy.kt (NEW)
│   │       └── FlareSolverrStrategy.kt (NEW)
│   ├── fingerprint/
│   │   ├── BrowserFingerprint.kt (NEW)
│   │   ├── FingerprintGenerator.kt (NEW)
│   │   ├── FingerprintEvasionScripts.kt (NEW)
│   │   └── TLSProfiles.kt (NEW)
│   ├── ratelimit/
│   │   ├── RateLimiter.kt (NEW)
│   │   ├── AdaptiveRateLimiter.kt (NEW)
│   │   └── RequestQueue.kt (NEW)
│   └── ... (existing files)
├── source/
│   ├── parsing/
│   │   ├── ContentDetector.kt (NEW)
│   │   ├── UniversalContentExtractor.kt (NEW)
│   │   └── cleaners/
│   │       ├── ContentCleaner.kt (NEW)
│   │       ├── AdRemover.kt (NEW)
│   │       └── ... (NEW)
│   └── ... (existing files)
```

---

## Cloudflare Bypass Deep Dive

### Current Cloudflare Protection Types

| Type | Detection | Current Support | Proposed Solution |
|------|-----------|-----------------|-------------------|
| JS Challenge | 503 + challenge page | ✅ Android WebView | Enhanced detection + all platforms |
| Managed Challenge | Interactive verification | ⚠️ Partial | Turnstile solver integration |
| Turnstile CAPTCHA | Widget on page | ❌ None | External solver API |
| IP Block | 403 + block page | ❌ None | Proxy rotation |
| Rate Limit | 429 | ⚠️ Basic retry | Adaptive rate limiting |
| Bot Detection | Various signals | ⚠️ Basic UA | Full fingerprint evasion |

### Cloudflare Detection Improvements

```kotlin
object CloudflareDetector {
    
    private val CF_CHALLENGE_PATTERNS = listOf(
        // JS Challenge
        Regex("""<title>Just a moment\.\.\.</title>"""),
        Regex("""Checking your browser before accessing"""),
        Regex("""cf-browser-verification"""),
        Regex("""_cf_chl_opt"""),
        
        // Turnstile
        Regex("""challenges\.cloudflare\.com/turnstile"""),
        Regex("""cf-turnstile"""),
        
        // Managed Challenge
        Regex("""managed_checking_msg"""),
        Regex("""cf-please-wait"""),
        
        // Block
        Regex("""cf-error-details"""),
        Regex("""Access denied"""),
        Regex("""Sorry, you have been blocked""")
    )
    
    private val CF_HEADERS = listOf(
        "cf-ray",
        "cf-cache-status", 
        "cf-request-id",
        "server" to "cloudflare"
    )
    
    fun detect(response: HttpResponse, body: String): CloudflareChallenge {
        // Check headers first (fast)
        val hasCloudflareHeaders = CF_HEADERS.any { header ->
            when (header) {
                is String -> response.headers[header] != null
                is Pair<*, *> -> response.headers[header.first as String]
                    ?.contains(header.second as String, ignoreCase = true) == true
                else -> false
            }
        }
        
        if (!hasCloudflareHeaders) return CloudflareChallenge.None
        
        // Check status code
        val statusCode = response.status.value
        
        // Check body patterns
        return when {
            statusCode == 403 && body.contains("Access denied") -> 
                CloudflareChallenge.BlockedIP(extractRayId(response))
            
            statusCode == 429 ->
                CloudflareChallenge.RateLimited(extractRetryAfter(response))
            
            body.contains("cf-turnstile") ->
                CloudflareChallenge.TurnstileChallenge(
                    extractTurnstileSiteKey(body),
                    extractRayId(response)
                )
            
            body.contains("_cf_chl_opt") || body.contains("cf-browser-verification") ->
                CloudflareChallenge.JSChallenge(extractRayId(response))
            
            body.contains("managed_checking_msg") ->
                CloudflareChallenge.ManagedChallenge(extractRayId(response))
            
            else -> CloudflareChallenge.None
        }
    }
    
    private fun extractRayId(response: HttpResponse): String? =
        response.headers["cf-ray"]
    
    private fun extractTurnstileSiteKey(body: String): String? {
        val regex = Regex("""sitekey['":\s]+['"]([^'"]+)['"]""")
        return regex.find(body)?.groupValues?.get(1)
    }
    
    private fun extractRetryAfter(response: HttpResponse): Long? =
        response.headers["retry-after"]?.toLongOrNull()
}
```

### WebView Bypass Enhancements (Android)

```kotlin
class EnhancedWebViewBypass(
    private val context: Context,
    private val fingerprintManager: FingerprintManager
) : CloudflareBypassStrategy {
    
    override val priority = 100
    override val name = "WebView"
    
    override suspend fun canHandle(challenge: CloudflareChallenge): Boolean {
        return challenge is CloudflareChallenge.JSChallenge ||
               challenge is CloudflareChallenge.ManagedChallenge
    }
    
    override suspend fun bypass(
        url: String,
        challenge: CloudflareChallenge,
        config: BypassConfig
    ): BypassResult = withContext(Dispatchers.Main) {
        
        val fingerprint = fingerprintManager.getOrCreateProfile(url.extractDomain())
        
        val webView = createStealthWebView(context, fingerprint)
        
        try {
            // Inject evasion scripts BEFORE page load
            webView.evaluateJavascript(FingerprintEvasionScripts.fullEvasion, null)
            
            // Load with consistent headers
            val headers = buildHeaders(fingerprint)
            webView.loadUrl(url, headers)
            
            // Wait for challenge resolution
            val result = waitForClearance(webView, config.timeout)
            
            if (result.success) {
                val cookies = extractCookies(url)
                BypassResult.Success(
                    ClearanceCookie(
                        cfClearance = cookies.cfClearance,
                        cfBm = cookies.cfBm,
                        userAgent = fingerprint.userAgent,
                        timestamp = currentTimeMillis(),
                        expiresAt = cookies.expiresAt,
                        domain = url.extractDomain()
                    )
                )
            } else {
                BypassResult.Failed(result.error ?: "Unknown error")
            }
        } finally {
            webView.destroy()
        }
    }
    
    private fun createStealthWebView(
        context: Context, 
        fingerprint: BrowserFingerprint
    ): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                userAgentString = fingerprint.userAgent
                
                // Stealth settings
                mediaPlaybackRequiresUserGesture = false
                allowFileAccess = false
                allowContentAccess = false
            }
            
            // Disable WebView detection
            removeJavascriptInterface("accessibility")
            removeJavascriptInterface("accessibilityTraversal")
        }
    }
}
```

### Fingerprint Evasion Scripts

```kotlin
object FingerprintEvasionScripts {
    
    val webdriverEvasion = """
        // Remove webdriver flag
        Object.defineProperty(navigator, 'webdriver', {
            get: () => undefined,
            configurable: true
        });
        
        // Remove automation flags
        delete window.cdc_adoQpoasnfa76pfcZLmcfl_Array;
        delete window.cdc_adoQpoasnfa76pfcZLmcfl_Promise;
        delete window.cdc_adoQpoasnfa76pfcZLmcfl_Symbol;
    """.trimIndent()
    
    val navigatorEvasion = """
        // Spoof plugins
        Object.defineProperty(navigator, 'plugins', {
            get: () => {
                const plugins = [
                    { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer' },
                    { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai' },
                    { name: 'Native Client', filename: 'internal-nacl-plugin' }
                ];
                plugins.length = 3;
                return plugins;
            }
        });
        
        // Spoof languages
        Object.defineProperty(navigator, 'languages', {
            get: () => ['en-US', 'en']
        });
        
        // Spoof platform
        Object.defineProperty(navigator, 'platform', {
            get: () => 'Linux armv81'
        });
        
        // Spoof hardware concurrency
        Object.defineProperty(navigator, 'hardwareConcurrency', {
            get: () => 8
        });
        
        // Spoof device memory
        Object.defineProperty(navigator, 'deviceMemory', {
            get: () => 8
        });
    """.trimIndent()
    
    val canvasEvasion = """
        // Add noise to canvas fingerprint
        const originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
        const originalGetImageData = CanvasRenderingContext2D.prototype.getImageData;
        
        HTMLCanvasElement.prototype.toDataURL = function(type) {
            if (type === 'image/png' || type === undefined) {
                const context = this.getContext('2d');
                if (context) {
                    const imageData = originalGetImageData.call(context, 0, 0, this.width, this.height);
                    for (let i = 0; i < imageData.data.length; i += 4) {
                        // Add subtle noise
                        imageData.data[i] = imageData.data[i] ^ (Math.random() > 0.5 ? 1 : 0);
                    }
                    context.putImageData(imageData, 0, 0);
                }
            }
            return originalToDataURL.apply(this, arguments);
        };
    """.trimIndent()
    
    val webglEvasion = """
        // Spoof WebGL vendor and renderer
        const getParameterProxyHandler = {
            apply: function(target, thisArg, args) {
                const param = args[0];
                const gl = thisArg;
                
                // UNMASKED_VENDOR_WEBGL
                if (param === 37445) {
                    return 'Google Inc. (Qualcomm)';
                }
                // UNMASKED_RENDERER_WEBGL
                if (param === 37446) {
                    return 'ANGLE (Qualcomm, Adreno (TM) 650, OpenGL ES 3.2)';
                }
                
                return Reflect.apply(target, thisArg, args);
            }
        };
        
        WebGLRenderingContext.prototype.getParameter = new Proxy(
            WebGLRenderingContext.prototype.getParameter,
            getParameterProxyHandler
        );
        WebGL2RenderingContext.prototype.getParameter = new Proxy(
            WebGL2RenderingContext.prototype.getParameter,
            getParameterProxyHandler
        );
    """.trimIndent()
    
    val fullEvasion = listOf(
        webdriverEvasion,
        navigatorEvasion,
        canvasEvasion,
        webglEvasion
    ).joinToString("\n\n")
}
```

---

## Desktop Cloudflare Solutions

### Option 1: FlareSolverr Integration (Recommended)

FlareSolverr is a proxy server that solves Cloudflare challenges using a real browser.

```kotlin
class FlareSolverrStrategy(
    private val client: FlareSolverrClient
) : CloudflareBypassStrategy {
    
    override val priority = 80
    override val name = "FlareSolverr"
    
    override suspend fun canHandle(challenge: CloudflareChallenge): Boolean {
        return client.isAvailable() && challenge !is CloudflareChallenge.BlockedIP
    }
    
    override suspend fun bypass(
        url: String,
        challenge: CloudflareChallenge,
        config: BypassConfig
    ): BypassResult {
        val request = FlareSolverrRequest(
            cmd = "request.get",
            url = url,
            maxTimeout = config.timeout.toInt(),
            returnOnlyCookies = true
        )
        
        return when (val response = client.solve(request)) {
            is FlareSolverrResponse.Success -> {
                val solution = response.solution
                BypassResult.Success(
                    ClearanceCookie(
                        cfClearance = solution.cookies.find { it.name == "cf_clearance" }?.value ?: "",
                        cfBm = solution.cookies.find { it.name == "__cf_bm" }?.value,
                        userAgent = solution.userAgent,
                        timestamp = currentTimeMillis(),
                        expiresAt = currentTimeMillis() + 30 * 60 * 1000, // 30 min default
                        domain = url.extractDomain()
                    )
                )
            }
            is FlareSolverrResponse.Error -> {
                BypassResult.Failed(response.message)
            }
        }
    }
}

// FlareSolverr client implementation
class FlareSolverrClientImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://localhost:8191"
) : FlareSolverrClient {
    
    override suspend fun solve(request: FlareSolverrRequest): FlareSolverrResponse {
        return try {
            val response = httpClient.post("$baseUrl/v1") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<FlareSolverrResponse>()
        } catch (e: Exception) {
            FlareSolverrResponse.Error("FlareSolverr request failed: ${e.message}")
        }
    }
    
    override fun isAvailable(): Boolean {
        return runBlocking {
            try {
                val response = httpClient.get("$baseUrl/health")
                response.status.isSuccess()
            } catch (e: Exception) {
                false
            }
        }
    }
}
```

### Option 2: Embedded Chromium (JCEF)

For desktop apps that need built-in browser capabilities:

```kotlin
/**
 * JCEF (Java Chromium Embedded Framework) integration
 * Provides full browser capabilities on desktop
 */
class JCEFBrowserEngine : BrowserEngineInterface {
    
    private var cefApp: CefApp? = null
    private var cefClient: CefClient? = null
    
    override fun isAvailable(): Boolean {
        return try {
            Class.forName("org.cef.CefApp")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
    
    override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String
    ): BrowserResult = withContext(Dispatchers.IO) {
        // Initialize JCEF if needed
        if (cefApp == null) {
            initializeCEF()
        }
        
        // Create offscreen browser
        val browser = createOffscreenBrowser(userAgent)
        
        try {
            // Load URL and wait for content
            browser.loadURL(url)
            waitForPageLoad(browser, selector, timeout)
            
            // Extract HTML
            val html = extractHTML(browser)
            val cookies = extractCookies(url)
            
            BrowserResult(
                responseBody = html,
                cookies = cookies,
                statusCode = 200
            )
        } finally {
            browser.close(true)
        }
    }
}
```

---

## Rate Limiting Implementation

```kotlin
class AdaptiveRateLimiter : RateLimiter {
    
    private val domainConfigs = ConcurrentHashMap<String, RateLimitState>()
    
    data class RateLimitState(
        var config: RateLimitConfig,
        var tokens: Double,
        var lastRefill: Long,
        var consecutiveErrors: Int = 0,
        var lastErrorTime: Long = 0
    )
    
    override suspend fun acquire(domain: String, weight: Int) {
        val state = getOrCreateState(domain)
        
        while (true) {
            refillTokens(state)
            
            if (state.tokens >= weight) {
                state.tokens -= weight
                return
            }
            
            // Calculate wait time
            val tokensNeeded = weight - state.tokens
            val waitTime = (tokensNeeded / state.config.requestsPerSecond * 1000).toLong()
            
            delay(minOf(waitTime, state.config.maxDelayMs))
        }
    }
    
    fun onResponse(domain: String, statusCode: Int, retryAfter: Long? = null) {
        val state = domainConfigs[domain] ?: return
        
        when (statusCode) {
            429 -> {
                // Rate limited - back off significantly
                state.consecutiveErrors++
                state.lastErrorTime = currentTimeMillis()
                
                // Reduce rate
                val newRate = state.config.requestsPerSecond * 0.5
                state.config = state.config.copy(
                    requestsPerSecond = maxOf(newRate, 0.1)
                )
                
                // If retry-after header present, respect it
                if (retryAfter != null) {
                    state.tokens = -retryAfter.toDouble()
                }
            }
            in 200..299 -> {
                // Success - gradually increase rate
                if (state.consecutiveErrors > 0) {
                    state.consecutiveErrors = 0
                    
                    // Slowly recover rate
                    val newRate = state.config.requestsPerSecond * 1.1
                    state.config = state.config.copy(
                        requestsPerSecond = minOf(newRate, 10.0)
                    )
                }
            }
            503 -> {
                // Server overloaded - moderate backoff
                state.consecutiveErrors++
                state.config = state.config.copy(
                    requestsPerSecond = state.config.requestsPerSecond * 0.7
                )
            }
        }
    }
    
    private fun refillTokens(state: RateLimitState) {
        val now = currentTimeMillis()
        val elapsed = now - state.lastRefill
        val tokensToAdd = elapsed * state.config.requestsPerSecond / 1000.0
        
        state.tokens = minOf(
            state.tokens + tokensToAdd,
            state.config.burstSize.toDouble()
        )
        state.lastRefill = now
    }
    
    private fun getOrCreateState(domain: String): RateLimitState {
        return domainConfigs.getOrPut(domain) {
            RateLimitState(
                config = RateLimitConfig(),
                tokens = RateLimitConfig().burstSize.toDouble(),
                lastRefill = currentTimeMillis()
            )
        }
    }
}
```

---

## Migration Guide

### For Source Developers

The refactoring maintains backward compatibility. Existing sources will continue to work.

#### Using New Features (Optional)

```kotlin
// Before (still works)
class MySource(deps: Dependencies) : HttpSource(deps) {
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        val response = client.get(baseUrl)
        // ...
    }
}

// After (with new features)
class MySource(deps: Dependencies) : HttpSource(deps) {
    
    // Use Cloudflare bypass automatically
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        val result = deps.httpClients.fetchWithBypass(
            url = "$baseUrl/novels?page=$page",
            config = FetchConfig(
                bypassCloudflare = true,
                useRateLimiter = true
            )
        )
        
        return when (result) {
            is FetchResult.Success -> parseList(result.data.asJsoup())
            is FetchResult.Error -> throw Exception(result.error.getUserMessage())
        }
    }
    
    // Or use the cloudflare client directly
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        val response = deps.httpClients.cloudflareClient.get(manga.key)
        // ...
    }
}
```

### For App Developers

#### Koin Module Updates

```kotlin
// Android httpModule
val httpModule = module {
    single { NetworkConfig() }
    single { AcceptAllCookiesStorage() }
    single { WebViewCookieJar(get()) }
    single { WebViewManger(androidContext()) }
    single { BrowserEngine(get(), get()) }
    
    // New components
    single<CloudflareCookieStore> { CloudflareCookieStoreImpl(get()) }
    single<RateLimiter> { AdaptiveRateLimiter() }
    single<CloudflareDetector> { CloudflareDetectorImpl() }
    single<FingerprintManager> { FingerprintManagerImpl() }
    
    single<CloudflareBypassManager> {
        CloudflareBypassManager(
            strategies = listOf(
                WebViewStrategy(androidContext(), get()),
                CookieReplayStrategy(get())
            ),
            cookieStore = get(),
            detector = get()
        )
    }
    
    single {
        HttpClients(
            context = androidContext(),
            browseEngine = get(),
            cookiesStorage = get(),
            webViewCookieJar = get(),
            preferencesStore = get(),
            webViewManager = get(),
            networkConfig = get(),
            // New parameters
            rateLimiter = get(),
            cloudflareBypass = get(),
            fingerprintManager = get()
        )
    }
}

// Desktop httpModule
val httpModule = module {
    single { NetworkConfig() }
    single<CloudflareCookieStore> { CloudflareCookieStoreImpl(get()) }
    single<RateLimiter> { AdaptiveRateLimiter() }
    
    // FlareSolverr for desktop
    single<FlareSolverrClient> { 
        FlareSolverrClientImpl(
            httpClient = get(),
            baseUrl = get<PreferenceStore>().getString("flaresolverr_url", "http://localhost:8191")
        )
    }
    
    single<CloudflareBypassManager> {
        CloudflareBypassManager(
            strategies = listOf(
                FlareSolverrStrategy(get()),
                CookieReplayStrategy(get())
            ),
            cookieStore = get(),
            detector = get()
        )
    }
    
    single {
        HttpClients(
            store = get(),
            networkConfig = get(),
            rateLimiter = get(),
            cloudflareBypass = get()
        )
    }
}
```

---

## Testing Strategy

### Unit Tests

```kotlin
class CloudflareDetectorTest {
    @Test
    fun `detect JS challenge from response`() {
        val body = """
            <title>Just a moment...</title>
            <div id="cf-browser-verification">
        """.trimIndent()
        
        val challenge = CloudflareDetector.detect(
            mockResponse(503, mapOf("server" to "cloudflare")),
            body
        )
        
        assertTrue(challenge is CloudflareChallenge.JSChallenge)
    }
    
    @Test
    fun `detect Turnstile challenge`() {
        val body = """
            <div class="cf-turnstile" data-sitekey="0x4AAAAAAA..."></div>
        """.trimIndent()
        
        val challenge = CloudflareDetector.detect(
            mockResponse(403, mapOf("server" to "cloudflare")),
            body
        )
        
        assertTrue(challenge is CloudflareChallenge.TurnstileChallenge)
        assertEquals("0x4AAAAAAA...", (challenge as CloudflareChallenge.TurnstileChallenge).siteKey)
    }
}

class AdaptiveRateLimiterTest {
    @Test
    fun `rate limiter backs off on 429`() = runTest {
        val limiter = AdaptiveRateLimiter()
        val domain = "example.com"
        
        val initialRate = limiter.getStats(domain).requestsPerSecond
        
        limiter.onResponse(domain, 429)
        
        val newRate = limiter.getStats(domain).requestsPerSecond
        assertTrue(newRate < initialRate)
    }
    
    @Test
    fun `rate limiter recovers after success`() = runTest {
        val limiter = AdaptiveRateLimiter()
        val domain = "example.com"
        
        // Trigger backoff
        limiter.onResponse(domain, 429)
        val backoffRate = limiter.getStats(domain).requestsPerSecond
        
        // Successful requests
        repeat(5) { limiter.onResponse(domain, 200) }
        
        val recoveredRate = limiter.getStats(domain).requestsPerSecond
        assertTrue(recoveredRate > backoffRate)
    }
}
```

### Integration Tests

```kotlin
class CloudflareBypassIntegrationTest {
    
    @Test
    fun `bypass JS challenge with WebView`() = runTest {
        // This test requires Android instrumentation
        val bypass = CloudflareBypassManager(...)
        
        val result = bypass.bypass(
            url = "https://nowsecure.nl", // Test site with Cloudflare
            challenge = CloudflareChallenge.JSChallenge(null)
        )
        
        assertTrue(result is BypassResult.Success)
        assertNotNull((result as BypassResult.Success).cookie.cfClearance)
    }
    
    @Test
    fun `cookie replay works for same domain`() = runTest {
        val cookieStore = CloudflareCookieStoreImpl(...)
        
        // Save a cookie
        cookieStore.saveClearanceCookie(
            "example.com",
            ClearanceCookie(
                cfClearance = "test_clearance",
                cfBm = null,
                userAgent = "test_ua",
                timestamp = currentTimeMillis(),
                expiresAt = currentTimeMillis() + 3600000,
                domain = "example.com"
            )
        )
        
        // Retrieve it
        val cookie = cookieStore.getClearanceCookie("example.com")
        assertNotNull(cookie)
        assertEquals("test_clearance", cookie.cfClearance)
    }
}
```

---

## Security Considerations

### Cookie Storage Security

```kotlin
/**
 * Secure cookie storage with encryption
 */
class SecureCloudfareCookieStore(
    private val preferences: PreferenceStore,
    private val encryptionKey: ByteArray
) : CloudflareCookieStore {
    
    override suspend fun saveClearanceCookie(domain: String, cookie: ClearanceCookie) {
        val json = Json.encodeToString(cookie)
        val encrypted = encrypt(json, encryptionKey)
        preferences.putString("cf_cookie_$domain", encrypted)
    }
    
    override suspend fun getClearanceCookie(domain: String): ClearanceCookie? {
        val encrypted = preferences.getString("cf_cookie_$domain", null) ?: return null
        return try {
            val json = decrypt(encrypted, encryptionKey)
            Json.decodeFromString<ClearanceCookie>(json)
        } catch (e: Exception) {
            null
        }
    }
}
```

### User Agent Consistency

Always use the same user agent for:
1. Initial request
2. WebView bypass
3. Subsequent requests with cookies

Mismatched user agents will invalidate Cloudflare cookies.

### Proxy Support

```kotlin
data class ProxyConfig(
    val host: String,
    val port: Int,
    val type: ProxyType = ProxyType.HTTP,
    val username: String? = null,
    val password: String? = null
)

enum class ProxyType { HTTP, SOCKS4, SOCKS5 }

// Usage in HttpClients
class HttpClients(
    // ...
    private val proxyConfig: ProxyConfig? = null
) {
    private val basicClient = OkHttpClient.Builder()
        .apply {
            proxyConfig?.let { proxy ->
                proxy(Proxy(
                    when (proxy.type) {
                        ProxyType.HTTP -> Proxy.Type.HTTP
                        ProxyType.SOCKS4, ProxyType.SOCKS5 -> Proxy.Type.SOCKS
                    },
                    InetSocketAddress(proxy.host, proxy.port)
                ))
                
                if (proxy.username != null && proxy.password != null) {
                    proxyAuthenticator { _, response ->
                        response.request.newBuilder()
                            .header("Proxy-Authorization", Credentials.basic(proxy.username, proxy.password))
                            .build()
                    }
                }
            }
        }
        // ...
}
```

---

## Performance Optimizations

### Connection Pooling

```kotlin
private val connectionPool = ConnectionPool(
    maxIdleConnections = 10,
    keepAliveDuration = 5,
    timeUnit = TimeUnit.MINUTES
)

private val basicClient = OkHttpClient.Builder()
    .connectionPool(connectionPool)
    // ...
```

### DNS Caching

```kotlin
class CachingDns(
    private val ttlSeconds: Long = 300
) : Dns {
    private val cache = ConcurrentHashMap<String, CachedResult>()
    
    override fun lookup(hostname: String): List<InetAddress> {
        val cached = cache[hostname]
        if (cached != null && !cached.isExpired()) {
            return cached.addresses
        }
        
        val addresses = Dns.SYSTEM.lookup(hostname)
        cache[hostname] = CachedResult(addresses, currentTimeMillis() + ttlSeconds * 1000)
        return addresses
    }
}
```

### Response Compression

Already implemented via `ContentNegotiation` plugin, but ensure servers support it:

```kotlin
install(ContentNegotiation) {
    gson()
}

// Add Accept-Encoding header
install(DefaultRequest) {
    header(HttpHeaders.AcceptEncoding, "gzip, deflate, br")
}
```

---

## Conclusion

This refactoring proposal addresses the key weaknesses in the current source-api module:

1. **Cloudflare Bypass**: Multi-strategy approach with better detection and cross-platform support
2. **Fingerprint Evasion**: Comprehensive browser fingerprint spoofing
3. **Rate Limiting**: Adaptive rate limiting to prevent blocks
4. **Desktop Support**: FlareSolverr integration for desktop platforms
5. **Performance**: Connection pooling, DNS caching, and response compression

The changes are designed to be backward compatible, allowing gradual adoption while providing significant improvements for sources that need them.

---

## Next Steps

1. Review and approve this proposal
2. Create feature branches for each phase
3. Implement Phase 1 (Cloudflare detection & cookie store)
4. Add unit tests
5. Implement Phase 2 (Fingerprint evasion)
6. Integration testing on Android
7. Implement Phase 3-4 (Desktop support)
8. Full regression testing
9. Documentation updates
10. Release


---

## Implementation Status

### Completed (Phase 1 - Core Cloudflare)

✅ **CloudflareChallenge.kt** - Sealed class representing all Cloudflare challenge types
- JSChallenge, CaptchaChallenge, TurnstileChallenge, ManagedChallenge, BlockedIP, RateLimited, Unknown
- Helper methods: `isAutoSolvable()`, `requiresUserInteraction()`, `getDescription()`

✅ **CloudflareDetector.kt** - Detects Cloudflare protection and identifies challenge type
- Pattern matching for JS challenges, Turnstile, CAPTCHA, managed challenges, blocks
- Header-based detection (cf-ray, server: cloudflare)
- Site key extraction for Turnstile/CAPTCHA

✅ **CloudflareCookieStore.kt** - Cookie persistence and management
- `ClearanceCookie` data class with expiry tracking
- `InMemoryCloudfareCookieStore` implementation
- Interface for persistent implementations

✅ **CloudflareBypassStrategy.kt** - Strategy pattern for bypass methods
- `BypassConfig` for configuration
- `BypassResult` sealed class for results
- `CookieReplayStrategy` for reusing cached cookies

✅ **CloudflareBypassManager.kt** - Orchestrates bypass strategies
- Tries strategies in priority order
- Caches successful cookies
- Logging and error handling

✅ **FlareSolverrClient.kt** - Desktop Cloudflare bypass via FlareSolverr
- Full API client implementation
- `FlareSolverrStrategy` bypass strategy
- Session management support

✅ **WebViewBypassStrategy.kt** - WebView-based bypass strategy
- Uses BrowserEngine for JS challenge solving
- Fingerprint-aware requests
- Challenge page detection

✅ **PersistentCookieStore.kt** - Persistent cookie storage
- PreferenceStore-backed persistence
- Memory cache with lazy loading
- Automatic expiry handling

### Completed (Phase 2 - Fingerprint Evasion)

✅ **FingerprintEvasionScripts.kt** - Browser fingerprint evasion
- WebDriver detection removal
- Navigator property spoofing
- Canvas fingerprint noise
- WebGL vendor/renderer spoofing
- Audio context fingerprint noise
- Screen property spoofing
- Permission and battery API spoofing

✅ **BrowserFingerprint.kt** - Browser fingerprint profiles
- Chrome Mobile, Chrome Desktop (Windows/Mac), Firefox, Safari iOS profiles
- `FingerprintManager` interface and `InMemoryFingerprintManager` implementation
- Per-domain fingerprint consistency

### Completed (Phase 3 - Rate Limiting & Queuing)

✅ **RateLimiter.kt** - Intelligent rate limiting
- `AdaptiveRateLimiter` with token bucket algorithm
- Automatic backoff on 429/503 responses
- Gradual recovery after successful requests
- `SimpleRateLimiter` for basic use cases

✅ **RequestQueue.kt** - Priority-based request queuing
- `DefaultRequestQueue` implementation
- Priority levels: LOW, NORMAL, HIGH, CRITICAL
- Configurable concurrency limits
- Rate limiter integration

### Completed (Phase 4 - Integration)

✅ **EnhancedHttpClients.kt** - Enhanced HTTP client wrapper
- `EnhancedHttpClientsInterface` extending base interface
- `EnhancedHttpClientsWrapper` implementation
- `fetchWithBypass()` and `fetchResponseWithBypass()` methods
- Automatic Cloudflare detection and bypass
- Rate limiting integration

✅ **CloudflareHttpSource.kt** - Enhanced source base class
- Built-in Cloudflare bypass support
- Rate limiting integration
- Fingerprint management
- Cookie caching

### Pending Implementation

⏳ **iOS WebView Strategy** - WKWebView-based bypass (requires iOS-specific code)
⏳ **TLS Fingerprint Profiles** - Match real browser TLS fingerprints (requires native code)
⏳ **HTTP/2 Fingerprinting** - Match real browser HTTP/2 settings (requires OkHttp customization)
⏳ **Android HttpModule Update** - Wire up new components in Koin DI
⏳ **Desktop HttpModule Update** - Wire up FlareSolverr in Koin DI

---

## Usage Examples

### Basic Cloudflare Bypass

```kotlin
// Create bypass manager
val cookieStore = InMemoryCloudfareCookieStore()
val bypassManager = CloudflareBypassManager(
    strategies = listOf(
        CookieReplayStrategy(cookieStore),
        // Add platform-specific strategies
    ),
    cookieStore = cookieStore
)

// Use in source
class MySource(deps: Dependencies) : HttpSource(deps) {
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        val response = client.get("$baseUrl/novels")
        
        // Check if Cloudflare challenge
        if (CloudflareDetector.isChallengeLikely(response)) {
            val bypassResult = bypassManager.bypass(baseUrl, response)
            
            when (bypassResult) {
                is BypassResult.Success -> {
                    // Retry with cookies
                    val retryResponse = client.get("$baseUrl/novels") {
                        cookie("cf_clearance", bypassResult.cookie.cfClearance)
                    }
                    return parseList(retryResponse.asJsoup())
                }
                is BypassResult.Failed -> {
                    throw CloudflareBypassFailed(bypassResult.reason)
                }
                else -> {}
            }
        }
        
        return parseList(response.asJsoup())
    }
}
```

### Using Rate Limiter

```kotlin
val rateLimiter = AdaptiveRateLimiter()

// Before each request
rateLimiter.acquire("example.com")

// After response
rateLimiter.onResponse("example.com", response.status.value)
```

### Fingerprint Evasion in WebView

```kotlin
// Inject before page load
webView.evaluateJavascript(FingerprintEvasionScripts.fullEvasion, null)
webView.loadUrl(url)
```
