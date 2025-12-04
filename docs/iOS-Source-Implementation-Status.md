# iOS Source Implementation Status

This document tracks the implementation progress of iOS source support based on the [iOS-Source-Architecture.md](iOS-Source-Architecture.md) plan.

## Implementation Progress

### ✅ Phase 1: Migrate from Jsoup to Ksoup
**Status: COMPLETE**

- [x] Added Ksoup dependency to source-api
- [x] Updated imports in ParsedHttpSource.kt
- [x] Updated imports in SourceFactory.kt
- [x] All source implementations use Ksoup
- [x] Removed Jsoup dependency from commonMain

### ✅ Phase 2: Create JS Runtime Module
**Status: COMPLETE**

Created `source-runtime-js` module with:

- [x] `SourceBridge.kt` - Main bridge object with @JsExport
- [x] `JsDependencies.kt` - Dependencies implementation for JS
- [x] `Exports.kt` - @JsExport declarations and helpers
- [x] `ExampleJsSource.kt` - Example source for reference

**Files Created:**
```
source-runtime-js/
├── build.gradle.kts
└── src/jsMain/kotlin/ireader/js/runtime/
    ├── SourceBridge.kt      # Main bridge object
    ├── JsDependencies.kt    # Dependencies for JS
    ├── Exports.kt           # @JsExport declarations
    └── ExampleJsSource.kt   # Example source
```

### ✅ Phase 3: Configure Source Modules for JS Output
**Status: COMPLETE**

- [x] Added JS target to source-api build.gradle.kts
- [x] Added ktor-client-js dependency
- [x] Added jsMain source set
- [x] Configured webpack for JS output
- [x] Created JS actual implementations for all expect declarations:
  - `BrowserEngine.js.kt`
  - `CookieSynchronizer.js.kt`
  - `HttpClients.js.kt`
  - `HttpModule.js.kt`
  - `JS.js.kt`
  - `SSLConfiguration.js.kt`
  - `WebViewManger.js.kt`
  - `CoroutineExt.js.kt`

### ✅ Phase 4: iOS Integration
**Status: COMPLETE**

- [x] Created iosMain source set in data module
- [x] Implemented IosCatalogLoader with JavaScriptCore
- [x] Implemented JsSourceWrapper
- [x] Added @Serializable to MangaInfo, ChapterInfo, MangasPageInfo

**Files Created/Updated:**
```
data/src/iosMain/kotlin/ireader/data/catalog/impl/
├── IosCatalogLoader.kt    # JS runtime loader
└── JsSourceWrapper.kt     # Source wrapper for JS sources
```

### ⏳ Phase 5: Distribution
**Status: IN PROGRESS**

- [x] Create migration script (`scripts/migrate-source-to-js.py`)
- [ ] Update IReader-extensions workflow for JS builds
- [ ] Set up source CDN/hosting (sources.ireader.app)
- [ ] Implement source update checking in app
- [ ] Add source installation UI for iOS

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        BUILD TIME                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  source-api (commonMain)                                        │
│  ├── HttpSource.kt                                              │
│  ├── SourceFactory.kt        ──► Kotlin/JS ──► source-api.js    │
│  ├── Models (MangaInfo, etc.)                                   │
│  └── Ksoup (HTML parsing)                                       │
│                                                                  │
│  source-runtime-js (jsMain)                                     │
│  ├── SourceBridge.kt         ──► Kotlin/JS ──► runtime.js       │
│  ├── JsDependencies.kt                                          │
│  └── Exports.kt                                                 │
│                                                                  │
│  Individual Sources                                             │
│  └── MySource.kt             ──► Kotlin/JS ──► mysource.js      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        RUNTIME (iOS)                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  JavaScriptCore Context (IosCatalogLoader)                      │
│  ├── runtime.js (loaded once at app start)                      │
│  ├── mysource.js (loaded when user installs source)             │
│  └── SourceBridge (Kotlin/JS → Swift bridge)                    │
│                                                                  │
│  JsSourceWrapper (Kotlin/Native)                                │
│  ├── Implements CatalogSource interface                         │
│  ├── Delegates to JS via IosCatalogLoader                       │
│  └── Handles JSON serialization/deserialization                 │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## How to Create an iOS-Compatible Source

### 1. Create Source in commonMain

```kotlin
// sources/mysource/src/commonMain/kotlin/MySource.kt
class MySource(deps: Dependencies) : SourceFactory(deps) {
    override val name = "My Source"
    override val baseUrl = "https://example.com"
    override val lang = "en"
    
    override val exploreFetchers = listOf(
        BaseExploreFetcher(
            key = "search",
            endpoint = "/search?q={query}&page={page}",
            selector = "div.novel-item",
            // ... configuration
        )
    )
    
    // ... other fetchers
}
```

### 2. Add JS Initialization in jsMain

```kotlin
// sources/mysource/src/jsMain/kotlin/Init.kt
@JsExport
fun initMySource() {
    registerSource("mysource") { deps ->
        MySource(deps as JsDependencies)
    }
}
```

### 3. Configure Build for JS Output

```kotlin
// sources/mysource/build.gradle.kts
kotlin {
    jvm()  // For Android/Desktop
    
    js(IR) {
        browser {
            webpackTask {
                outputFileName = "mysource.js"
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":source-api"))
        }
        jsMain.dependencies {
            implementation(project(":source-runtime-js"))
        }
    }
}
```

## API Reference

### SourceBridge (JavaScript)

```javascript
// Get source info
SourceBridge.getSourceInfo("mysource")  // Returns JSON string

// Search
SourceBridge.search("mysource", "query", 1)  // Returns Promise<JSON>

// Get details
SourceBridge.getBookDetails("mysource", bookJson)  // Returns Promise<JSON>

// Get chapters
SourceBridge.getChapters("mysource", bookJson)  // Returns Promise<JSON>

// Get content
SourceBridge.getContent("mysource", chapterJson)  // Returns Promise<JSON>
```

### SourceRegistry (JavaScript)

```javascript
// Register a source factory
SourceRegistry.register("mysource", factory)

// Initialize a source
SourceRegistry.initSource("mysource")  // Returns boolean

// Initialize all sources
SourceRegistry.initAllSources()  // Returns count

// Get available source IDs
SourceRegistry.getAvailableSourceIds()  // Returns string[]
```

## File Size Estimates

| Component | Estimated Size |
|-----------|----------------|
| runtime.js (Kotlin stdlib + Ktor + Ksoup) | 800KB - 1.2MB |
| Per source .js file | 10-30KB |
| 50 sources total | ~2.5MB (runtime + all sources) |

## Next Steps

1. **Build and test** the JS output
2. **Set up CDN** for source distribution
3. **Create CI pipeline** for automated builds
4. **Test on iOS simulator** with JavaScriptCore
5. **Add source browser UI** for iOS app

## Related Documents

- [iOS-Source-Architecture.md](iOS-Source-Architecture.md) - Original architecture design
- [SOURCE_CREATION_GUIDE.md](SOURCE_CREATION_GUIDE.md) - How to create sources
- [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) - Migrating from old API


## Migration Script

A Python script is provided to help migrate existing sources to support iOS:

```bash
# Migrate a single source
python scripts/migrate-source-to-js.py ../IReader-extensions/sources/en/novelupdates

# The script will:
# 1. Migrate Jsoup imports to Ksoup
# 2. Update build.gradle.kts with JS target
# 3. Create jsMain init file for iOS registration
```

### What the Script Does

1. **Import Migration**: Converts `org.jsoup.*` imports to `com.fleeksoft.ksoup.*`
2. **Build Configuration**: Adds JS target to `build.gradle.kts`
3. **JS Init File**: Creates `src/jsMain/kotlin/.../Init.kt` with `@JsExport` function

### Manual Steps After Migration

1. Review the generated code
2. Test compilation: `./gradlew :sources:<name>:compileKotlinJs`
3. Build JS bundle: `./gradlew :sources:<name>:jsBrowserProductionWebpack`
4. Test in browser console or iOS simulator

## Testing on iOS

### 1. Load Runtime in JavaScriptCore

```swift
import JavaScriptCore

let context = JSContext()!

// Load runtime.js
let runtimeJS = loadBundledFile("runtime.js")
context.evaluateScript(runtimeJS)

// Initialize runtime
context.evaluateScript("initRuntime()")
```

### 2. Load a Source

```swift
// Load source JS file
let sourceJS = loadSourceFile("novelupdates.js")
context.evaluateScript(sourceJS)

// Initialize the source
context.evaluateScript("initNovelUpdates()")
```

### 3. Use the Source

```swift
// Search
let searchPromise = context.evaluateScript("""
    SourceBridge.search('novelupdates', 'isekai', 1)
""")

// Get results (handle Promise)
// ...
```

## Troubleshooting

### Common Issues

**"Module not found" in JS**
- Ensure runtime.js is loaded before source JS files
- Check webpack configuration

**"Source not registered"**
- Verify init function is called after loading JS
- Check console for registration logs

**"Promise not resolving"**
- Ensure coroutines are properly configured
- Check for exceptions in console.error logs

**iOS JavaScriptCore issues**
- Ensure proper memory management
- Handle async/Promise bridging correctly
