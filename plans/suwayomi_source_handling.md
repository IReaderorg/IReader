# Suwayomi Source Handling - Deep Dive

**Date:** 2026-06-04
**Purpose:** Explain how Suwayomi handles sources and how WebReader can learn from it

---

## 1. Suwayomi Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Suwayomi Server Architecture                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Web Browser (Client)                       │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │           Tachiyomi WebUI (React)                      │  │   │
│  │  │  - Basic library view                                  │  │   │
│  │  │  - Basic reader                                        │  │   │
│  │  │  - Extension management                                │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │ HTTP / WebSocket                       │
│  ┌──────────────────────────▼───────────────────────────────────┐   │
│  │                  Suwayomi Server (Ktor)                       │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              REST API Layer                             │  │   │
│  │  │  - /api/v1/source/*                                    │  │   │
│  │  │  - /api/v1/manga/*                                     │  │   │
│  │  │  - /api/v1/chapter/*                                   │  │   │
│  │  │  - /api/v1/extension/*                                 │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │         Tachiyomi Source Engine (Bundled)              │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Extension Loader                                │  │  │   │
│  │  │  │  - Scan extensions directory                     │  │  │   │
│  │  │  │  - Load APK/JAR files                            │  │  │   │
│  │  │  │  - Create isolated ClassLoaders                  │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Source Executor                                 │  │  │   │
│  │  │  │  - Call source methods                           │  │  │   │
│  │  │  │  - Handle responses                              │  │  │   │
│  │  │  │  - Error handling                                 │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Database Layer (Exposed)                   │  │   │
│  │  │  - SQLite (default) or PostgreSQL                      │  │   │
│  │  │  - Connection pooling                                  │  │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. How Suwayomi Handles Sources

### 2.1 Source Types

Suwayomi supports **Tachiyomi/Mihon extensions** which are:

1. **APK Files** (Android packages)
   - Contains compiled Java/Kotlin code
   - Includes `classes.dex` (Dalvik bytecode)
   - Has `AndroidManifest.xml` with metadata

2. **JAR Files** (Java archives)
   - Contains compiled `.class` files
   - Has `source-manifest.json` with metadata
   - Used for desktop/server extensions

### 2.2 Extension Loading Process

```
┌─────────────────────────────────────────────────────────────────────┐
│                  Suwayomi Extension Loading Pipeline                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. SCAN                                                            │
│     └── Scan extensions directory for .apk and .jar files           │
│                                                                     │
│  2. VALIDATE                                                        │
│     ├── Check file signature (optional)                             │
│     ├── Read manifest (AndroidManifest.json or source-manifest.json)│
│     └── Verify required fields (id, name, version, mainClass)       │
│                                                                     │
│  3. EXTRACT (for APK)                                               │
│     ├── Extract classes.dex from APK                                │
│     ├── Convert DEX to JAR (using dex2jar on server)                │
│     └── Extract native libraries (.so files)                        │
│                                                                     │
│  4. LOAD                                                            │
│     ├── Create isolated ClassLoader for extension                   │
│     ├── Load main source class                                      │
│     └── Instantiate source with dependencies                        │
│                                                                     │
│  5. REGISTER                                                        │
│     ├── Add to source registry                                      │
│     ├── Update database with extension info                         │
│     └── Notify clients via WebSocket                                │
│                                                                     │
│  6. EXECUTE                                                         │
│     ├── Call source methods (getPopular, search, etc.)              │
│     ├── Handle responses                                            │
│     └── Cache results                                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.3 Extension Loader Implementation

```kotlin
// Simplified from Suwayomi's ExtensionLoader.kt
class ExtensionLoader(
    private val extensionsDir: Path,
    private val database: Database
) {
    private val extensions = mutableMapOf<String, Extension>()
    private val classLoaders = mutableMapOf<String, ClassLoader>()
    
    suspend fun loadAll() {
        val files = extensionsDir.listFiles { 
            it.extension in listOf("apk", "jar") 
        }
        
        files.forEach { file ->
            try {
                loadExtension(file)
            } catch (e: Exception) {
                logger.error("Failed to load extension: ${file.name}", e)
            }
        }
    }
    
    private suspend fun loadExtension(file: File) {
        val manifest = when (file.extension) {
            "apk" -> readApkManifest(file)
            "jar" -> readJarManifest(file)
            else -> throw IllegalArgumentException("Unknown extension type")
        }
        
        // Create isolated ClassLoader
        val classLoader = createClassLoader(file, manifest)
        
        // Load main source class
        val sourceClass = classLoader.loadClass(manifest.mainClass)
        val source = sourceClass.getConstructor().newInstance() as Source
        
        // Register extension
        extensions[manifest.id] = Extension(
            id = manifest.id,
            name = manifest.name,
            version = manifest.version,
            mainClass = manifest.mainClass,
            source = source,
            classLoader = classLoader
        )
        
        // Update database
        database.insertExtension(manifest)
        
        logger.info("Loaded extension: ${manifest.name} (${manifest.id})")
    }
    
    private fun createClassLoader(file: File, manifest: ExtensionManifest): ClassLoader {
        return when (file.extension) {
            "apk" -> {
                // Convert APK to JAR first
                val jarFile = convertApkToJar(file)
                URLClassLoader(arrayOf(jarFile.toURI().toURL()), javaClass.classLoader)
            }
            "jar" -> {
                URLClassLoader(arrayOf(file.toURI().toURL()), javaClass.classLoader)
            }
            else -> throw IllegalArgumentException("Unknown extension type")
        }
    }
}
```

### 2.4 Source Interface

```kotlin
// Tachiyomi Source interface (simplified)
interface Source {
    val id: Long
    val name: String
    val lang: String
    
    suspend fun getPopularManga(page: Int): MangasPage
    suspend fun searchManga(query: String, page: Int): MangasPage
    suspend fun getMangaDetails(manga: Manga): Manga
    suspend fun getChapterList(manga: Manga): List<Chapter>
    suspend fun getPageList(chapter: Chapter): List<Page>
}

// Suwayomi wraps this in a proxy for error handling
class SourceProxy(private val source: Source) {
    suspend fun getPopularManga(page: Int): MangasPage {
        return try {
            source.getPopularManga(page)
        } catch (e: Exception) {
            logger.error("Source ${source.name} error: ${e.message}", e)
            MangasPage(emptyList(), false)
        }
    }
    
    // ... other methods with similar error handling
}
```

### 2.5 Extension Management API

```kotlin
// Suwayomi's extension API endpoints
fun Route.extensionApi() {
    route("/api/v1/extension") {
        // List all extensions
        get {
            val extensions = extensionLoader.getAllExtensions()
            call.respond(extensions.map { it.toDto() })
        }
        
        // Install extension
        post("/install") {
            val file = call.receive<ByteArray>()
            val extension = extensionLoader.installExtension(file)
            call.respond(extension.toDto())
        }
        
        // Update extension
        put("/update/{id}") {
            val id = call.parameters["id"]!!
            val file = call.receive<ByteArray>()
            val extension = extensionLoader.updateExtension(id, file)
            call.respond(extension.toDto())
        }
        
        // Uninstall extension
        delete("/{id}") {
            val id = call.parameters["id"]!!
            extensionLoader.uninstallExtension(id)
            call.respond(HttpStatusCode.OK)
        }
        
        // Get extension icon
        get("/{id}/icon") {
            val id = call.parameters["id"]!!
            val icon = extensionLoader.getExtensionIcon(id)
            call.respondBytes(icon, ContentType.Image.PNG)
        }
    }
}
```

---

## 3. Suwayomi's Source Execution Model

### 3.1 Request Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Suwayomi Request Flow                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Client Request                                                     │
│       │                                                             │
│       ▼                                                             │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Ktor Route Handler                                          │   │
│  │  - Validate request                                          │   │
│  │  - Check authentication                                      │   │
│  │  - Rate limiting                                             │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │                                       │
│                             ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Source Manager                                              │   │
│  │  - Find source by ID                                         │   │
│  │  - Check cache                                               │   │
│  │  - Call source method                                        │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │                                       │
│                             ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Source Proxy                                                │   │
│  │  - Wrap source call with error handling                      │   │
│  │  - Add timeout                                               │   │
│  │  - Log errors                                                │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │                                       │
│                             ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Actual Source (in isolated ClassLoader)                     │   │
│  │  - Execute source code                                       │   │
│  │  - Make HTTP requests                                        │   │
│  │  - Parse HTML/JSON                                           │   │
│  │  - Return results                                            │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │                                       │
│                             ▼                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Response Processing                                         │   │
│  │  - Serialize to JSON                                         │   │
│  │  - Cache result                                              │   │
│  │  - Return to client                                          │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 Error Handling

```kotlin
// Suwayomi's error handling approach
class SafeSourceExecutor {
    suspend fun <T> execute(
        source: Source,
        operation: String,
        block: suspend () -> T
    ): Result<T> {
        return try {
            // Add timeout
            val result = withTimeout(30.seconds) {
                block()
            }
            Result.success(result)
        } catch (e: TimeoutCancellationException) {
            logger.warn("Source ${source.name} timeout: $operation")
            Result.failure(SourceException("Request timed out"))
        } catch (e: Exception) {
            logger.error("Source ${source.name} error: $operation", e)
            Result.failure(SourceException(e.message ?: "Unknown error"))
        }
    }
}

// Usage
class SourceService(private val executor: SafeSourceExecutor) {
    suspend fun getPopular(source: Source, page: Int): MangasPage {
        return executor.execute(source, "getPopular") {
            source.getPopularManga(page)
        }.getOrElse { error ->
            // Return empty result on error
            MangasPage(emptyList(), false)
        }
    }
}
```

### 3.3 Caching Strategy

```kotlin
// Suwayomi's caching approach
class SourceCache {
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    
    data class CacheEntry(
        val data: Any,
        val timestamp: Long,
        val ttl: Long = 300_000 // 5 minutes default
    )
    
    fun <T> getOrPut(
        key: String,
        ttl: Long = 300_000,
        producer: () -> T
    ): T {
        val entry = cache[key]
        val now = System.currentTimeMillis()
        
        if (entry != null && now - entry.timestamp < entry.ttl) {
            @Suppress("UNCHECKED_CAST")
            return entry.data as T
        }
        
        val data = producer()
        cache[key] = CacheEntry(data, now, ttl)
        return data
    }
    
    fun invalidate(key: String) {
        cache.remove(key)
    }
    
    fun invalidateSource(sourceId: Long) {
        cache.keys.filter { it.startsWith("source:$sourceId") }
            .forEach { cache.remove(it) }
    }
}

// Usage
class SourceService(private val cache: SourceCache) {
    suspend fun getPopular(source: Source, page: Int): MangasPage {
        return cache.getOrPut("source:${source.id}:popular:$page") {
            source.getPopularManga(page)
        }
    }
}
```

---

## 4. Lessons for WebReader

### 4.1 What Suwayomi Does Well

1. **Simple Architecture**
   - Single JVM for everything
   - Direct source loading
   - No FFI complexity

2. **Isolated ClassLoaders**
   - Each extension has its own ClassLoader
   - Prevents dependency conflicts
   - Allows unloading extensions

3. **Error Handling**
   - Source errors don't crash server
   - Timeout protection
   - Graceful degradation

4. **Caching**
   - Response caching reduces source calls
   - Configurable TTL
   - Source-specific invalidation

### 4.2 What WebReader Can Improve

1. **Multi-Source Support**
   - Suwayomi: Only Tachiyomi extensions
   - WebReader: IReader + Suwayomi + Tachiyomi

2. **Better UI**
   - Suwayomi: Basic WebUI
   - WebReader: Modern React + Next.js

3. **Advanced Reader**
   - Suwayomi: Basic HTML reader
   - WebReader: Canvas-based with gestures

4. **Health Monitoring**
   - Suwayomi: Basic logging
   - WebReader: Source health dashboard

5. **Offline Support**
   - Suwayomi: Limited
   - WebReader: Full PWA with service worker

### 4.3 WebReader Source Loading Strategy

```kotlin
// WebReader's unified source loader
class UnifiedSourceLoader(
    private val extensionsDir: Path,
    private val jsSourcesDir: Path,
    private val httpClient: HttpClient
) {
    private val sources = ConcurrentHashMap<Long, SourceWrapper>()
    
    suspend fun loadAllSources() {
        coroutineScope {
            // Load Tachiyomi/Suwayomi extensions
            launch { loadExtensions() }
            
            // Load IReader Kotlin sources
            launch { loadIReaderSources() }
            
            // Load IReader JS sources
            launch { loadJsSources() }
        }
    }
    
    private suspend fun loadExtensions() {
        extensionsDir.listFiles { it.extension in listOf("apk", "jar") }
            .forEach { file ->
                try {
                    val source = TachiyomiSourceLoader.load(file, httpClient)
                    sources[source.id] = SourceWrapper.Tachiyomi(source)
                } catch (e: Exception) {
                    logger.error("Failed to load extension: ${file.name}", e)
                }
            }
    }
    
    private suspend fun loadIReaderSources() {
        // Load IReader Kotlin DSL sources
        // These are already compiled JARs with IReader source-api
    }
    
    private suspend fun loadJsSources() {
        // Load IReader JS sources via GraalVM
    }
}

// Unified source wrapper
sealed class SourceWrapper {
    abstract val id: Long
    abstract val name: String
    abstract val lang: String
    
    data class Tachiyomi(val source: Source) : SourceWrapper() {
        override val id = source.id
        override val name = source.name
        override val lang = source.lang
    }
    
    data class IReaderKotlin(val source: CatalogSource) : SourceWrapper() {
        override val id = source.id
        override val name = source.name
        override val lang = source.lang
    }
    
    data class IReaderJs(val source: JsSource) : SourceWrapper() {
        override val id = source.id
        override val name = source.name
        override val lang = source.lang
    }
}
```

---

## 5. Suwayomi Extension Format

### 5.1 APK Extension Structure

```
extension.apk
├── AndroidManifest.xml
├── classes.dex          # Compiled Java/Kotlin code
├── resources.arsc       # Resources
├── lib/                 # Native libraries
│   ├── arm64-v8a/
│   │   └── libnative.so
│   └── x86_64/
│       └── libnative.so
└── META-INF/
    ├── MANIFEST.MF
    └── CERT.SF
```

### 5.2 JAR Extension Structure

```
extension.jar
├── source-manifest.json
├── eu/kanade/tachiyomi/extension/ExampleSource.class
├── eu/kanade/tachiyomi/extension/ExampleHelper.class
└── lib/
    └── dependency.jar
```

### 5.3 Source Manifest

```json
{
  "id": "example-source",
  "name": "Example Source",
  "version": "1.0.0",
  "mainClass": "eu.kanade.tachiyomi.extension.en.ExampleSource",
  "language": "en",
  "description": "Example manga source",
  "author": "Author Name",
  "nsfw": false,
  "icon": "icon.png"
}
```

---

## 6. WebReader Source Compatibility Layer

### 6.1 Supporting Multiple Source Types

```kotlin
// WebReader's source compatibility layer
class SourceCompatibilityLayer {
    
    // Convert Tachiyomi source to unified interface
    fun wrapTachiyomi(source: Source): UnifiedSource {
        return UnifiedSource(
            id = source.id,
            name = source.name,
            lang = source.lang,
            supportsLatest = true,
            fetchPopular = { page -> source.getPopularManga(page) },
            fetchSearch = { query, page -> source.searchManga(query, page) },
            fetchDetails = { manga -> source.getMangaDetails(manga) },
            fetchChapters = { manga -> source.getChapterList(manga) },
            fetchPages = { chapter -> source.getPageList(chapter) }
        )
    }
    
    // Convert IReader source to unified interface
    fun wrapIReader(source: CatalogSource): UnifiedSource {
        return UnifiedSource(
            id = source.id,
            name = source.name,
            lang = source.lang,
            supportsLatest = source.supportsLatest,
            fetchPopular = { page -> source.getPopularManga(page) },
            fetchSearch = { query, page -> source.searchMangas(MangasPageRequest(query, page)) },
            fetchDetails = { manga -> source.getBookDetails(manga) },
            fetchChapters = { manga -> source.getChapterList(manga) },
            fetchPages = { chapter -> source.getPageList(chapter) }
        )
    }
    
    // Convert JS source to unified interface
    fun wrapJsSource(source: JsSource): UnifiedSource {
        return UnifiedSource(
            id = source.id,
            name = source.name,
            lang = source.lang,
            supportsLatest = source.supportsLatest,
            fetchPopular = { page -> source.getPopular(page) },
            fetchSearch = { query, page -> source.search(query, page) },
            fetchDetails = { manga -> source.getDetails(manga) },
            fetchChapters = { manga -> source.getChapters(manga) },
            fetchPages = { chapter -> source.getPages(chapter) }
        )
    }
}

data class UnifiedSource(
    val id: Long,
    val name: String,
    val lang: String,
    val supportsLatest: Boolean,
    val fetchPopular: suspend (Int) -> MangasPage,
    val fetchSearch: suspend (String, Int) -> MangasPage,
    val fetchDetails: suspend (Manga) -> Manga,
    val fetchChapters: suspend (Manga) -> List<Chapter>,
    val fetchPages: suspend (Chapter) -> List<Page>
)
```

### 6.2 Source Health Monitoring

```kotlin
// WebReader's source health monitor
class SourceHealthMonitor(
    private val sourceManager: SourceManager
) {
    private val healthStatus = ConcurrentHashMap<Long, SourceHealth>()
    
    fun startMonitoring(scope: CoroutineScope) {
        scope.launch {
            while (isActive) {
                checkAllSources()
                delay(5.minutes)
            }
        }
    }
    
    private suspend fun checkAllSources() {
        sourceManager.getAllSources().forEach { source ->
            launch {
                val health = checkSourceHealth(source)
                healthStatus[source.id] = health
            }
        }
    }
    
    private suspend fun checkSourceHealth(source: UnifiedSource): SourceHealth {
        return try {
            val start = System.currentTimeMillis()
            source.fetchPopular(1) // Test with popular endpoint
            val latency = System.currentTimeMillis() - start
            
            SourceHealth(
                status = when {
                    latency < 1000 -> HealthStatus.HEALTHY
                    latency < 3000 -> HealthStatus.SLOW
                    else -> HealthStatus.VERY_SLOW
                },
                latencyMs = latency,
                lastChecked = Instant.now()
            )
        } catch (e: Exception) {
            SourceHealth(
                status = HealthStatus.DOWN,
                error = e.message,
                lastChecked = Instant.now()
            )
        }
    }
    
    fun getHealth(sourceId: Long): SourceHealth? = healthStatus[sourceId]
    
    fun getAllHealth(): Map<Long, SourceHealth> = healthStatus.toMap()
}

data class SourceHealth(
    val status: HealthStatus,
    val latencyMs: Long = 0,
    val error: String? = null,
    val lastChecked: Instant
)

enum class HealthStatus {
    HEALTHY,
    SLOW,
    VERY_SLOW,
    DOWN
}
```

---

## 7. Summary

### Key Takeaways from Suwayomi

1. **Keep it simple**: Single JVM, direct source loading
2. **Isolate sources**: Use separate ClassLoaders
3. **Handle errors gracefully**: Source errors shouldn't crash server
4. **Cache responses**: Reduce load on sources
5. **Monitor health**: Track source availability and performance

### WebReader Improvements

1. **Multi-source support**: IReader + Suwayomi + Tachiyomi
2. **Better UI**: Modern React frontend
3. **Advanced reader**: Canvas-based with gestures
4. **Health dashboard**: Real-time source monitoring
5. **Offline support**: PWA with service worker

### Recommended Approach

For WebReader, I recommend following Suwayomi's proven architecture:
- Single JVM (Ktor)
- Isolated ClassLoaders for sources
- Graceful error handling
- Response caching
- Health monitoring

The main difference is adding support for IReader sources alongside Tachiyomi extensions, which can be done by:
1. Loading IReader source-api in the same JVM
2. Creating a compatibility layer for different source types
3. Using GraalVM for JS sources

---

*This document explains Suwayomi's source handling and how WebReader can build upon it.*