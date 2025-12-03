# iOS Source Architecture: Kotlin/JS Approach

## Executive Summary

This document outlines a strategy for supporting IReader's official sources on iOS using Kotlin/JS compilation. The approach allows writing sources once in Kotlin and running them on all platforms (Android, Desktop, iOS) while remaining App Store compliant.

## Problem Statement

IReader's official sources are Kotlin classes extending `HttpSource`/`ParsedHttpSource` that:
- Use Ktor for HTTP requests
- Use Jsoup for HTML parsing
- Get compiled to DEX (Android) or JAR (Desktop) for dynamic loading

**iOS Challenge:** Apple prohibits dynamic native code loading at runtime, making the Android/Desktop approach impossible.

## Evaluated Approaches

| Approach | Feasibility | Notes |
|----------|-------------|-------|
| Kotlin/Native Dynamic Framework | ❌ Blocked | iOS prohibits dlopen() for App Store apps |
| Bundled Sources (compiled into app) | ⚠️ Limited | Works but requires app update for new sources |
| WebAssembly (WASM) | ⚠️ Experimental | Kotlin/WASM too early, poor library support |
| Lua/Custom DSL | ⚠️ Rewrite | Loses Kotlin type safety, requires rewriting all sources |
| **Kotlin/JS + JavaScriptCore** | ✅ Recommended | Single codebase, dynamic loading, App Store compliant |

## Recommended Architecture: Kotlin/JS

### Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        BUILD TIME                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  source-api (commonMain)                                        │
│  ├── HttpSource.kt                                              │
│  ├── ParsedHttpSource.kt         ──► Kotlin/JS ──► runtime.js   │
│  ├── Models (MangaInfo, etc.)                      (~800KB)     │
│  └── Ksoup (HTML parsing)                                       │
│                                                                  │
│  sources/novelupdates (commonMain)                              │
│  └── NovelUpdatesSource.kt       ──► Kotlin/JS ──► novelupdates.js
│                                                        (~15KB)  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        RUNTIME (iOS)                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  JavaScriptCore Context                                         │
│  ├── runtime.js (loaded once at app start)                      │
│  ├── novelupdates.js (loaded when user installs source)         │
│  └── royalroad.js (loaded when user installs source)            │
│                                                                  │
│  Native Bridge (Kotlin/Native)                                  │
│  ├── HTTP requests (Ktor native → JS callback)                  │
│  ├── Storage (preferences)                                      │
│  └── Result marshalling (JS objects → Kotlin models)            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Key Insight: Shared Runtime

The Kotlin/JS stdlib and dependencies are loaded **once** as a shared runtime (~800KB-1.2MB). Individual sources add only ~10-30KB each, making the total overhead acceptable.

## Implementation Plan

### Phase 1: Migrate from Jsoup to Ksoup

**Why:** Jsoup is JVM-only. Ksoup is a KMP-compatible HTML parser with nearly identical API.

**Dependency Change:**
```kotlin
// build.gradle.kts (source-api module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Remove: implementation("org.jsoup:jsoup:1.x.x")
            // Add:
            implementation("com.fleeksoft.ksoup:ksoup:0.1.2")
        }
    }
}
```

**Code Migration:**
```kotlin
// Before (Jsoup - JVM only)
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.Jsoup

// After (Ksoup - KMP)
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.Ksoup
```

The API is nearly identical, so migration is mostly import changes.

### Phase 2: Create JS Runtime Module

New module that bundles the shared Kotlin/JS runtime:

```
source-runtime-js/
├── src/jsMain/kotlin/
│   └── ireader/js/runtime/
│       ├── SourceBridge.kt      # Main bridge object
│       ├── JsDependencies.kt    # Dependencies implementation for JS
│       └── Exports.kt           # @JsExport declarations
└── build.gradle.kts
```

**SourceBridge.kt:**
```kotlin
package ireader.js.runtime

import ireader.core.source.HttpSource
import ireader.core.source.model.*
import kotlinx.serialization.json.Json

@JsExport
object SourceBridge {
    private val loadedSources = mutableMapOf<String, HttpSource>()
    
    fun registerSource(id: String, source: HttpSource) {
        loadedSources[id] = source
    }
    
    fun getSourceInfo(id: String): String {
        val source = loadedSources[id] ?: return "{}"
        return Json.encodeToString(SourceInfo.serializer(), SourceInfo(
            id = source.id,
            name = source.name,
            lang = source.lang,
            baseUrl = (source as? HttpSource)?.baseUrl ?: ""
        ))
    }
    
    suspend fun search(sourceId: String, query: String, page: Int): String {
        val source = loadedSources[sourceId] ?: return "[]"
        val result = source.getMangaList(sort = null, page = page, query = query)
        return Json.encodeToString(MangasPageInfo.serializer(), result)
    }
    
    suspend fun getBookDetails(sourceId: String, bookJson: String): String {
        val source = loadedSources[sourceId] ?: return "{}"
        val book = Json.decodeFromString(MangaInfo.serializer(), bookJson)
        val result = source.getMangaDetails(book, emptyList())
        return Json.encodeToString(MangaInfo.serializer(), result)
    }
    
    suspend fun getChapters(sourceId: String, bookJson: String): String {
        val source = loadedSources[sourceId] ?: return "[]"
        val book = Json.decodeFromString(MangaInfo.serializer(), bookJson)
        val result = source.getChapterList(book, emptyList())
        return Json.encodeToString(result)
    }
    
    suspend fun getContent(sourceId: String, chapterJson: String): String {
        val source = loadedSources[sourceId] ?: return "[]"
        val chapter = Json.decodeFromString(ChapterInfo.serializer(), chapterJson)
        val result = source.getPageList(chapter, emptyList())
        return Json.encodeToString(result)
    }
}

@Serializable
data class SourceInfo(
    val id: Long,
    val name: String,
    val lang: String,
    val baseUrl: String
)
```

### Phase 3: Configure Source Modules for JS Output

Each source module produces both JVM and JS artifacts:

**sources/novelupdates/build.gradle.kts:**
```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    // Existing targets
    jvm()  // For Android/Desktop
    
    // New JS target for iOS
    js(IR) {
        browser {
            webpackTask {
                outputFileName = "novelupdates.js"
                output.libraryTarget = "commonjs2"
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":source-api"))
            implementation("com.fleeksoft.ksoup:ksoup:0.1.2")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
        }
        
        jsMain.dependencies {
            implementation(project(":source-runtime-js"))
        }
    }
}
```

**Source JS Initialization (jsMain):**
```kotlin
// sources/novelupdates/src/jsMain/kotlin/Init.kt
package ireader.sources.novelupdates

import ireader.js.runtime.SourceBridge

@JsExport
fun initSource(httpClient: dynamic) {
    val deps = JsDependencies(httpClient)
    val source = NovelUpdatesSource(deps)
    SourceBridge.registerSource("novelupdates", source)
}
```

### Phase 4: iOS Source Loader

**data/src/iosMain/kotlin/ireader/data/catalog/impl/IosCatalogLoader.kt:**
```kotlin
package ireader.data.catalog.impl

import platform.JavaScriptCore.*
import platform.Foundation.*
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.models.entities.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

class IosCatalogLoader(
    private val httpClients: HttpClients,
    private val uiPreferences: UiPreferences,
    private val preferences: PreferenceStoreFactory
) : CatalogLoader {
    
    private val jsContext: JSContext = JSContext()
    private var runtimeLoaded = false
    private val json = Json { ignoreUnknownKeys = true }
    
    init {
        setupNativeBridge()
    }
    
    private fun setupNativeBridge() {
        // Expose HTTP GET to JavaScript
        val httpGet: @convention(block) (String, JSValue) -> Unit = { url, callback ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = httpClients.default.get(url).bodyAsText()
                    withContext(Dispatchers.Main) {
                        callback.callWithArguments(listOf(null, response))
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        callback.callWithArguments(listOf(e.message, null))
                    }
                }
            }
        }
        jsContext.setObject(httpGet, forKeyedSubscript: "nativeHttpGet")
        
        // Expose logging
        val log: @convention(block) (String) -> Unit = { message ->
            println("[JS Source] $message")
        }
        jsContext.setObject(log, forKeyedSubscript: "nativeLog")
    }
    
    suspend fun loadRuntime() {
        if (runtimeLoaded) return
        
        val runtimeJs = loadBundledFile("runtime.js")
        jsContext.evaluateScript(runtimeJs)
        runtimeLoaded = true
    }
    
    suspend fun loadSource(sourceId: String): Boolean {
        if (!runtimeLoaded) loadRuntime()
        
        val sourceJs = downloadSourceFile(sourceId) ?: return false
        jsContext.evaluateScript(sourceJs)
        jsContext.evaluateScript("initSource(nativeHttpGet)")
        
        return jsContext.exception == null
    }
    
    override suspend fun loadAll(): List<CatalogLocal> {
        val bundled = mutableListOf<CatalogLocal>(
            CatalogBundled(
                source = LocalSource(),
                description = "Read novels from local storage",
                name = "Local Source"
            )
        )
        
        // Load JS runtime
        loadRuntime()
        
        // Load installed JS sources
        val installedIds = getInstalledSourceIds()
        val jsSources = installedIds.mapNotNull { id ->
            if (loadSource(id)) {
                createJsSourceCatalog(id)
            } else null
        }
        
        return bundled + jsSources
    }
    
    private fun createJsSourceCatalog(sourceId: String): CatalogLocal {
        val infoJson = jsContext.evaluateScript(
            "SourceBridge.getSourceInfo('$sourceId')"
        )?.toString() ?: "{}"
        
        val info = json.decodeFromString<SourceInfo>(infoJson)
        return JsSourceCatalog(sourceId, info, this)
    }
    
    // Bridge methods for JsSourceWrapper to call
    internal fun jsSearch(sourceId: String, query: String, page: Int): String {
        return jsContext.evaluateScript(
            "SourceBridge.search('$sourceId', '$query', $page)"
        )?.toString() ?: "[]"
    }
    
    internal fun jsGetDetails(sourceId: String, bookJson: String): String {
        val escaped = bookJson.replace("'", "\\'")
        return jsContext.evaluateScript(
            "SourceBridge.getBookDetails('$sourceId', '$escaped')"
        )?.toString() ?: "{}"
    }
    
    internal fun jsGetChapters(sourceId: String, bookJson: String): String {
        val escaped = bookJson.replace("'", "\\'")
        return jsContext.evaluateScript(
            "SourceBridge.getChapters('$sourceId', '$escaped')"
        )?.toString() ?: "[]"
    }
    
    internal fun jsGetContent(sourceId: String, chapterJson: String): String {
        val escaped = chapterJson.replace("'", "\\'")
        return jsContext.evaluateScript(
            "SourceBridge.getContent('$sourceId', '$escaped')"
        )?.toString() ?: "[]"
    }
    
    // APK loading not supported on iOS
    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? = null
    override fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide? = null
    
    private fun loadBundledFile(filename: String): String {
        val bundle = NSBundle.mainBundle
        val name = filename.substringBeforeLast(".")
        val ext = filename.substringAfterLast(".")
        val path = bundle.pathForResource(name, ext) ?: error("File not found: $filename")
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as String
    }
    
    private suspend fun downloadSourceFile(sourceId: String): String? {
        return try {
            val url = "https://sources.ireader.app/js/$sourceId.js"
            httpClients.default.get(url).bodyAsText()
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getInstalledSourceIds(): List<String> {
        // Read from preferences/database
        return preferences.create("installed_sources")
            .getString("ids", "")
            .split(",")
            .filter { it.isNotBlank() }
    }
}
```

**JsSourceWrapper.kt:**
```kotlin
package ireader.data.catalog.impl

import ireader.core.source.CatalogSource
import ireader.core.source.model.*
import kotlinx.serialization.json.Json

class JsSourceWrapper(
    private val sourceId: String,
    private val info: SourceInfo,
    private val loader: IosCatalogLoader
) : CatalogSource {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override val id: Long = info.id
    override val name: String = info.name
    override val lang: String = info.lang
    
    override suspend fun getMangaList(
        sort: Listing?,
        page: Int,
        query: String
    ): MangasPageInfo {
        val resultJson = loader.jsSearch(sourceId, query, page)
        return json.decodeFromString(resultJson)
    }
    
    override suspend fun getMangaDetails(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): MangaInfo {
        val bookJson = json.encodeToString(MangaInfo.serializer(), manga)
        val resultJson = loader.jsGetDetails(sourceId, bookJson)
        return json.decodeFromString(resultJson)
    }
    
    override suspend fun getChapterList(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): List<ChapterInfo> {
        val bookJson = json.encodeToString(MangaInfo.serializer(), manga)
        val resultJson = loader.jsGetChapters(sourceId, bookJson)
        return json.decodeFromString(resultJson)
    }
    
    override suspend fun getPageList(
        chapter: ChapterInfo,
        commands: List<Command<*>>
    ): List<Page> {
        val chapterJson = json.encodeToString(ChapterInfo.serializer(), chapter)
        val resultJson = loader.jsGetContent(sourceId, chapterJson)
        return json.decodeFromString(resultJson)
    }
    
    override fun getListings(): List<Listing> = emptyList()
    override fun getCommands(): CommandList = emptyList()
}
```

### Phase 5: Build & Distribution Pipeline

**CI/CD Workflow (.github/workflows/build-sources.yml):**
```yaml
name: Build Sources

on:
  push:
    paths:
      - 'sources/**'

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build Android AARs
        run: ./gradlew :sources:assembleRelease
      - uses: actions/upload-artifact@v4
        with:
          name: android-sources
          path: sources/*/build/outputs/aar/*.aar

  build-js:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build JS bundles
        run: ./gradlew :sources:jsBrowserProductionWebpack
      - uses: actions/upload-artifact@v4
        with:
          name: js-sources
          path: sources/*/build/dist/js/productionExecutable/*.js

  deploy:
    needs: [build-android, build-js]
    runs-on: ubuntu-latest
    steps:
      - name: Download artifacts
        uses: actions/download-artifact@v4
      - name: Deploy to CDN
        run: |
          # Upload to sources.ireader.app
          aws s3 sync android-sources/ s3://ireader-sources/android/
          aws s3 sync js-sources/ s3://ireader-sources/js/
```

**Distribution Structure:**
```
sources.ireader.app/
├── index.json                    # Source catalog
├── android/
│   ├── novelupdates-1.0.0.aar
│   ├── royalroad-1.2.0.aar
│   └── ...
├── desktop/
│   ├── novelupdates-1.0.0.jar
│   └── ...
└── js/
    ├── runtime.js                # Shared runtime (~1MB)
    ├── novelupdates-1.0.0.js     # Individual sources (~15KB each)
    ├── royalroad-1.2.0.js
    └── ...
```

## File Size Estimates

| Component | Estimated Size |
|-----------|----------------|
| runtime.js (Kotlin stdlib + Ktor + Ksoup) | 800KB - 1.2MB |
| Per source .js file | 10-30KB |
| 50 sources total | ~2.5MB (runtime + all sources) |

## Migration Checklist

### Prerequisites
- [ ] Evaluate Ksoup compatibility with existing parsing code
- [ ] Set up Kotlin/JS build configuration
- [ ] Create test iOS project with JavaScriptCore

### Phase 1: Jsoup → Ksoup Migration
- [ ] Add Ksoup dependency to source-api
- [ ] Update imports in ParsedHttpSource.kt
- [ ] Update imports in all source implementations
- [ ] Run tests to verify parsing still works
- [ ] Remove Jsoup dependency

### Phase 2: JS Runtime
- [ ] Create source-runtime-js module
- [ ] Implement SourceBridge with @JsExport
- [ ] Implement JsDependencies for HTTP/storage
- [ ] Build and test runtime.js output

### Phase 3: Source JS Builds
- [ ] Add JS target to source build configs
- [ ] Create jsMain init functions for each source
- [ ] Test source loading in browser JS console
- [ ] Set up webpack optimization for smaller bundles

### Phase 4: iOS Integration
- [ ] Create iosMain source set in data module
- [ ] Implement IosCatalogLoader
- [ ] Implement JsSourceWrapper
- [ ] Test on iOS simulator
- [ ] Handle async/coroutine bridging

### Phase 5: Distribution
- [ ] Set up source CDN/hosting
- [ ] Create CI pipeline for multi-platform builds
- [ ] Implement source update checking in app
- [ ] Add source installation UI for iOS

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Ksoup API differences | Medium | Create adapter layer if needed |
| JS performance on iOS | Low | JavaScriptCore is well-optimized |
| Coroutine handling in JS | Medium | Use promise-based bridge |
| Large runtime size | Low | Tree-shaking, lazy loading |
| Debugging complexity | Medium | Source maps, logging bridge |

## Alternatives Considered

### Why Not Kotlin/Native for iOS?
iOS prohibits loading dynamic native code (frameworks) at runtime for App Store apps. This is a hard platform limitation.

### Why Not WASM?
Kotlin/WASM is experimental and lacks mature library support (Ktor, serialization). Could revisit in 1-2 years.

### Why Not Bundled Sources Only?
Bundling sources into the app binary works but requires app updates for source changes. The JS approach allows dynamic updates without app store review.

## Conclusion

The Kotlin/JS approach provides the best balance of:
- **Developer experience**: Write sources once in Kotlin
- **Platform coverage**: Same code runs on Android, Desktop, iOS
- **Dynamic loading**: Update sources without app updates
- **App Store compliance**: JavaScript execution is allowed

The main investment is the Jsoup → Ksoup migration and building the JS bridge infrastructure. Once complete, adding iOS support to any source is automatic through the build system.
