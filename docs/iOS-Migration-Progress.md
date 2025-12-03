# iOS Migration Progress

## Status: ✅ COMPLETE

The source-api and i18n modules have been successfully migrated to support iOS targets.

## What Was Done

### source-api Module
- Replaced Jsoup with Ksoup (com.fleeksoft.ksoup) for HTML parsing
- Replaced OkHttp types with KMP-compatible alternatives
- Replaced `System.currentTimeMillis()` with `kotlinx.datetime.Clock`
- Removed `@Keep` annotations (Google ErrorProne - JVM only)
- Removed `okio.Closeable` from JS interface
- Created iOS actual implementations for:
  - BrowserEngine (WKWebView stub)
  - CookieSynchronizer (NSHTTPCookieStorage stub)
  - HttpClients (Darwin engine)
  - JS (JavaScriptCore stub)
  - SSLConfiguration (ATS configuration)
  - WebViewManger (WKWebView stub)

### i18n Module
- Fixed `localizedMessage` → `message` (JVM-only property)
- Created iOS actual implementations for:
  - Images (using Compose Resources)
  - LocalizeHelper (using NSLocale)

## Build Commands

```bash
# Build source-api for iOS
./gradlew :source-api:compileKotlinIosArm64

# Build i18n for iOS
./gradlew :i18n:compileKotlinIosArm64

# Build iOS framework
./gradlew :source-api:linkDebugFrameworkIosArm64
```

## Known Warnings

The build shows warnings about expect/actual classes being in Beta. These can be suppressed by adding `-Xexpect-actual-classes` compiler flag if needed.

## iOS Implementation TODOs

The iOS actual implementations are stubs that need full native implementation:

1. **BrowserEngine**: Implement WKWebView for JavaScript rendering
2. **JS**: Implement JavaScriptCore for script evaluation
3. **WebViewManger**: Implement WKWebView for content loading
4. **CookieSynchronizer**: Implement NSHTTPCookieStorage/WKHTTPCookieStore sync
