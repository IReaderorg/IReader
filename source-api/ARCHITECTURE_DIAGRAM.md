# Network Layer Architecture Diagram

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                         │
│  (Repositories, Use Cases, ViewModels)                          │
└────────────────────────┬────────────────────────────────────────┘
                         │ Inject via Koin DI
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      HttpClients (expect)                        │
│  ┌──────────────┬──────────────┬──────────────┬───────────────┐ │
│  │   default    │ cloudflare   │   browser    │    config     │ │
│  │  HttpClient  │  HttpClient  │BrowserEngine │ NetworkConfig │ │
│  └──────────────┴──────────────┴──────────────┴───────────────┘ │
│  ┌──────────────┬──────────────────────────────────────────────┐ │
│  │  sslConfig   │         cookieSynchronizer                   │ │
│  │SSLConfig     │         CookieSynchronizer                   │ │
│  └──────────────┴──────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────────┘
                         │ Platform-specific implementation
         ┌───────────────┴───────────────┐
         ▼                               ▼
┌─────────────────────┐         ┌─────────────────────┐
│   Android (actual)  │         │   Desktop (actual)  │
└─────────────────────┘         └─────────────────────┘
```

## Android Implementation

```
┌─────────────────────────────────────────────────────────────────┐
│                    Android HttpClients                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ default: HttpClient (OkHttp)                               │ │
│  │  • Ktor client with OkHttp engine                          │ │
│  │  • Content negotiation (Gson)                              │ │
│  │  • Cookie support (CookiesStorage)                         │ │
│  │  • HTTP response cache (5 min default)                     │ │
│  │  • Compression support                                     │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ cloudflareClient: HttpClient (OkHttp + Interceptor)        │ │
│  │  • CloudflareInterceptor for challenge bypass              │ │
│  │  • WebView integration for solving challenges              │ │
│  │  • Automatic cookie extraction                             │ │
│  │  • Seamless WebViewManager integration                     │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ browser: BrowserEngine                                      │ │
│  │  • Android WebView wrapper                                 │ │
│  │  • JavaScript execution                                    │ │
│  │  • Cloudflare challenge detection                          │ │
│  │  • AJAX completion tracking                                │ │
│  │  • Ad/tracker blocking                                     │ │
│  │  • Browser fingerprinting evasion                          │ │
│  │  • Dynamic content extraction                              │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ cookieSynchronizer: CookieSynchronizer                     │ │
│  │  • Android CookieManager integration                       │ │
│  │  • Sync WebView ↔ OkHttp cookies                          │ │
│  │  • Automatic cookie persistence                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ sslConfig: SSLConfiguration                                │ │
│  │  • Certificate pinning (SHA-256)                           │ │
│  │  • TLS 1.2/1.3 support                                     │ │
│  │  • Self-signed cert support (dev only)                     │ │
│  │  • Custom trust managers                                   │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘

Supporting Components:
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ WebViewManager   │  │WebViewCookieJar  │  │ CookiesStorage   │
│ • Lifecycle mgmt │  │ • Cookie bridge  │  │ • In-memory      │
│ • Background load│  │ • WebView ↔ HTTP │  │ • Ktor cookies   │
│ • Content ready  │  │ • Persistence    │  │ • Auto-expire    │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

## Desktop Implementation

```
┌─────────────────────────────────────────────────────────────────┐
│                    Desktop HttpClients                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ default: HttpClient (OkHttp)                               │ │
│  │  • Ktor client with OkHttp engine                          │ │
│  │  • Content negotiation (Gson)                              │ │
│  │  • Cookie support (HttpCookies)                            │ │
│  │  • HTTP response cache (5 min default)                     │ │
│  │  • Compression support                                     │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ cloudflareClient: HttpClient (OkHttp)                      │ │
│  │  • Standard HTTP client (no interceptor)                   │ │
│  │  • Limited Cloudflare support                              │ │
│  │  • Cookie support                                          │ │
│  │  • Cache support                                           │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ browser: BrowserEngine (STUB)                              │ │
│  │  • Returns error message                                   │ │
│  │  • isAvailable() returns false                             │ │
│  │  • Suggests alternatives (JavaFX, JCEF)                    │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ cookieSynchronizer: CookieSynchronizer (NO-OP)             │ │
│  │  • No WebView on desktop                                   │ │
│  │  • All methods are no-ops                                  │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ sslConfig: SSLConfiguration                                │ │
│  │  • Certificate pinning (SHA-256)                           │ │
│  │  • TLS 1.2/1.3 support                                     │ │
│  │  • Self-signed cert support (dev only)                     │ │
│  │  • Custom trust managers                                   │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘

Supporting Components:
┌──────────────────┐  ┌──────────────────┐
│PersistentCookie  │  │  CookieStore     │
│      Jar         │  │  • Persistence   │
│ • OkHttp cookies │  │  • Preferences   │
│ • Persistent     │  │  • Domain-based  │
└──────────────────┘  └──────────────────┘
```

## Request Flow - Standard HTTP

```
Application
    │
    ├─> httpClients.default.get(url)
    │
    ▼
HttpClient (Ktor)
    │
    ├─> OkHttp Engine
    │       │
    │       ├─> SSL/TLS Configuration
    │       │       └─> Certificate validation
    │       │
    │       ├─> Cookie Management
    │       │       └─> Load cookies for domain
    │       │
    │       └─> HTTP Request
    │               │
    │               ▼
    │           Network
    │               │
    │               ▼
    │           Response
    │               │
    │               ├─> Save cookies
    │               ├─> Cache response
    │               └─> Return to app
    │
    ▼
Application receives response
```

## Request Flow - Cloudflare Bypass (Android)

```
Application
    │
    ├─> httpClients.cloudflareClient.get(url)
    │
    ▼
HttpClient (Ktor)
    │
    ├─> OkHttp Engine
    │       │
    │       ├─> CloudflareInterceptor
    │       │       │
    │       │       ├─> Detect Cloudflare (403/503)
    │       │       │
    │       │       ├─> Initialize WebView
    │       │       │       │
    │       │       │       ├─> Load URL in WebView
    │       │       │       ├─> Wait for challenge solve
    │       │       │       ├─> Detect cf_clearance cookie
    │       │       │       └─> Extract cookies
    │       │       │
    │       │       ├─> Sync cookies to OkHttp
    │       │       │
    │       │       └─> Retry request with cookies
    │       │
    │       └─> HTTP Request (with cookies)
    │               │
    │               ▼
    │           Network
    │               │
    │               ▼
    │           Response (200 OK)
    │
    ▼
Application receives response
```

## Request Flow - Browser Engine (Android)

```
Application
    │
    ├─> httpClients.browser.fetch(url, selector)
    │
    ▼
BrowserEngine
    │
    ├─> Initialize WebView
    │       │
    │       ├─> Configure settings
    │       │   • JavaScript enabled
    │       │   • DOM storage enabled
    │       │   • User agent
    │       │
    │       ├─> Set up WebViewClient
    │       │   • Page load detection
    │       │   • Error handling
    │       │   • Cloudflare detection
    │       │
    │       └─> Inject JavaScript
    │           • AJAX tracking
    │           • Selector monitoring
    │           • Content extraction
    │
    ├─> Load URL
    │       │
    │       ├─> Wait for page load
    │       ├─> Wait for AJAX completion
    │       ├─> Wait for selector (if specified)
    │       └─> Extract HTML
    │
    ├─> Extract cookies from WebView
    │
    └─> Return Result
            │
            ├─> responseBody (HTML)
            ├─> cookies (List<Cookie>)
            ├─> statusCode (Int)
            └─> error (String?)
    │
    ▼
Application processes result
```

## Cookie Synchronization Flow (Android)

```
┌─────────────────────────────────────────────────────────────────┐
│                    Cookie Synchronization                        │
└─────────────────────────────────────────────────────────────────┘

WebView → HTTP Client:
    WebView
        │
        ├─> Android CookieManager
        │       │
        │       └─> getCookie(url)
        │
        ▼
    CookieSynchronizer.syncFromWebView(url)
        │
        ├─> Parse cookie strings
        ├─> Convert to OkHttp Cookie objects
        │
        ▼
    WebViewCookieJar
        │
        └─> saveFromResponse(url, cookies)
                │
                ├─> Store in OkHttp
                └─> Store in Ktor CookiesStorage

HTTP Client → WebView:
    OkHttp/Ktor
        │
        ├─> WebViewCookieJar
        │       │
        │       └─> loadForRequest(url)
        │
        ▼
    CookieSynchronizer.syncToWebView(url)
        │
        ├─> Convert to cookie strings
        │
        ▼
    Android CookieManager
        │
        └─> setCookie(url, cookieString)
```

## Dependency Injection Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         Koin DI Setup                            │
└─────────────────────────────────────────────────────────────────┘

startKoin {
    modules(
        httpModule,      ◄─── Platform-specific (Android/Desktop)
        domainModule,
        ...
    )
}

httpModule (Android):
    │
    ├─> single<CookiesStorage> { AcceptAllCookiesStorage() }
    ├─> single<WebViewCookieJar> { WebViewCookieJar(get()) }
    ├─> single<WebViewManger> { WebViewManger(androidContext()) }
    ├─> single<BrowserEngine> { BrowserEngine(get(), get()) }
    ├─> single<NetworkConfig> { NetworkConfig() }
    └─> single<HttpClients> { HttpClients(...) }

httpModule (Desktop):
    │
    ├─> single<NetworkConfig> { NetworkConfig() }
    ├─> single<HttpClients> { HttpClients(get(), get()) }
    ├─> single<BrowserEngine> { BrowserEngine() }  ◄─── Stub
    └─> single<WebViewManger> { WebViewManger() }  ◄─── Stub

Application:
    │
    ├─> class MyRepository(
    │       private val httpClients: HttpClients  ◄─── Injected by Koin
    │   )
    │
    └─> Koin resolves dependencies automatically
```

## Configuration Hierarchy

```
┌─────────────────────────────────────────────────────────────────┐
│                      NetworkConfig                               │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ • connectTimeoutSeconds: 30                                │ │
│  │ • readTimeoutMinutes: 5                                    │ │
│  │ • callTimeoutMinutes: 5                                    │ │
│  │ • cacheSize: 15MB                                          │ │
│  │ • cacheDurationMs: 5 minutes                               │ │
│  │ • userAgent: Chrome/120.0.0.0                              │ │
│  │ • enableCaching: true                                      │ │
│  │ • enableCookies: true                                      │ │
│  │ • enableCompression: true                                  │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                         │
                         ├─> Applied to OkHttpClient.Builder
                         ├─> Applied to Ktor HttpClient
                         └─> Applied to Cache configuration

Override in Koin:
    single<NetworkConfig> {
        NetworkConfig(
            connectTimeoutSeconds = 60,  // Custom value
            cacheSize = 50L * 1024 * 1024  // 50MB
        )
    }
```

## Platform Decision Tree

```
                    Need to fetch content?
                            │
                ┌───────────┴───────────┐
                │                       │
        Standard HTTP          Requires JavaScript?
                │                       │
                │               ┌───────┴───────┐
                │               │               │
                │              Yes              No
                │               │               │
                │       Check platform      Use default
                │               │           HttpClient
                │       ┌───────┴───────┐
                │       │               │
                │    Android         Desktop
                │       │               │
                │   Use browser     Use default
                │   engine with     HttpClient
                │   JavaScript      (fallback)
                │   rendering
                │
        ┌───────┴───────┐
        │               │
    Protected      Not protected
    (Cloudflare)        │
        │               │
    Use cloudflare  Use default
    Client          HttpClient
    (Android)
```

## Legend

```
┌─────────┐
│  Box    │  Component or module
└─────────┘

    │
    ├─>     Flow or dependency
    │
    ▼

◄───        Points to or provides

• Bullet    Feature or property

(STUB)      Stub implementation
(NO-OP)     No-operation implementation
```


---

## Enhanced HTTP Layer (New)

The enhanced HTTP layer provides additional features on top of the base HttpClients:

```
┌─────────────────────────────────────────────────────────────────┐
│                  EnhancedHttpClientsWrapper                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Delegates to: HttpClientsInterface                         │ │
│  │  • default, cloudflareClient, browser, config, etc.        │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ rateLimiter: AdaptiveRateLimiter                           │ │
│  │  • Token bucket algorithm                                  │ │
│  │  • Per-domain rate limiting                                │ │
│  │  • Automatic backoff on 429/503                            │ │
│  │  • Gradual recovery after success                          │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ cloudflareBypass: CloudflareBypassManager                  │ │
│  │  • Multi-strategy bypass system                            │ │
│  │  • Challenge detection & classification                    │ │
│  │  • Cookie caching & reuse                                  │ │
│  │  • Fingerprint-aware requests                              │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ fingerprintManager: FingerprintManager                     │ │
│  │  • Browser profile management                              │ │
│  │  • Per-domain fingerprint consistency                      │ │
│  │  • Chrome/Firefox/Safari profiles                          │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ cookieStore: CloudflareCookieStore                         │ │
│  │  • cf_clearance cookie persistence                         │ │
│  │  • Automatic expiry handling                               │ │
│  │  • User agent association                                  │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ requestQueue: RequestQueue (optional)                      │ │
│  │  • Priority-based queuing                                  │ │
│  │  • Concurrency control                                     │ │
│  │  • Rate limiter integration                                │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

## Cloudflare Bypass Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                   CloudflareBypassManager                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Strategies (tried in priority order):                          │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 1. CookieReplayStrategy (priority: 200)                    │ │
│  │    • Reuses valid cached cf_clearance cookies              │ │
│  │    • Checks user agent consistency                         │ │
│  │    • Fastest - no network request needed                   │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 2. WebViewBypassStrategy (priority: 100) [Android]         │ │
│  │    • Uses BrowserEngine/WebView                            │ │
│  │    • Injects fingerprint evasion scripts                   │ │
│  │    • Waits for challenge resolution                        │ │
│  │    • Extracts cookies from WebView                         │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 3. FlareSolverrStrategy (priority: 80) [Desktop]           │ │
│  │    • Connects to FlareSolverr service                      │ │
│  │    • External browser-based solving                        │ │
│  │    • Supports sessions for efficiency                      │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘

Challenge Types Detected:
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   JSChallenge    │  │ TurnstileChall.  │  │  CaptchaChall.   │
│ • Auto-solvable  │  │ • Needs solver   │  │ • Needs solver   │
│ • 5-10 seconds   │  │ • Site key req.  │  │ • Site key req.  │
└──────────────────┘  └──────────────────┘  └──────────────────┘
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ ManagedChallenge │  │   RateLimited    │  │    BlockedIP     │
│ • Interactive    │  │ • Wait & retry   │  │ • Cannot bypass  │
│ • May auto-solve │  │ • Retry-After    │  │ • Need new IP    │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

## Fingerprint Evasion

```
┌─────────────────────────────────────────────────────────────────┐
│                  FingerprintEvasionScripts                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Scripts injected before page load:                             │
│                                                                  │
│  • webdriverEvasion    - Remove automation detection flags      │
│  • navigatorEvasion    - Spoof navigator properties             │
│  • canvasEvasion       - Add noise to canvas fingerprint        │
│  • webglEvasion        - Spoof WebGL vendor/renderer            │
│  • audioEvasion        - Add noise to audio fingerprint         │
│  • screenEvasion       - Spoof screen dimensions                │
│  • permissionsEvasion  - Spoof permission states                │
│  • batteryEvasion      - Spoof battery API                      │
│                                                                  │
│  Usage:                                                         │
│  • fullEvasion    - All scripts combined                        │
│  • minimalEvasion - Just webdriver + navigator (faster)         │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

## Usage Flow

```
Application
    │
    ├─> httpClients.enhanced()  // Upgrade to enhanced
    │       │
    │       └─> EnhancedHttpClientsWrapper
    │
    ├─> enhancedClients.fetchWithBypass(url)
    │       │
    │       ├─> rateLimiter.acquire(domain)
    │       │
    │       ├─> Apply fingerprint & cached cookies
    │       │
    │       ├─> Make HTTP request
    │       │       │
    │       │       ├─> Success (200) → Return response
    │       │       │
    │       │       └─> Cloudflare detected (403/503)
    │       │               │
    │       │               ├─> CloudflareDetector.detect()
    │       │               │
    │       │               └─> CloudflareBypassManager.bypass()
    │       │                       │
    │       │                       ├─> Try CookieReplayStrategy
    │       │                       ├─> Try WebViewBypassStrategy
    │       │                       └─> Try FlareSolverrStrategy
    │       │
    │       └─> Return FetchResult<String>
    │
    ▼
Application processes result
```
