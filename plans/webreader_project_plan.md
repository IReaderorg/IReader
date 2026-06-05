# WebReader - Project Plan & Technology Options

**Date:** 2026-06-04
**Status:** Planning Phase
**Author:** OWL (Architect Mode)

---

## 0. Executive Summary: WebReader vs Suwayomi

### Key Differences

| Aspect | Suwayomi Server | WebReader (Ktor+React) |
|--------|-----------------|------------------------|
| **Language** | Kotlin (JVM) | Kotlin (JVM) + TypeScript |
| **Web Framework** | Ktor (same!) | Ktor (same!) |
| **Frontend** | React (Tachiyomi WebUI) | React + Next.js (custom) |
| **Source Support** | Tachiyomi/Mihon extensions only | **IReader + Suwayomi + Tachiyomi** |
| **Source Runtime** | Bundled Tachiyomi source engine | **Custom unified runtime** |
| **UI Quality** | Basic (functional) | **Amazing (goal)** |
| **Reader Experience** | Basic web reader | **Advanced canvas-based reader** |
| **Performance** | Good | **Excellent (optimized)** |
| **Debugging** | Basic logging | **Comprehensive dev tools** |
| **Offline Support** | Limited | **Full PWA with service worker** |
| **Multi-user** | Basic | **Advanced with permissions** |

### Why Not Just Use Suwayomi?

Suwayomi is great, but WebReader aims to be **better** in several ways:

1. **Unified Source System**
   - Suwayomi: Only Tachiyomi/Mihon extensions (Java/Kotlin)
   - WebReader: IReader sources (Kotlin DSL + JS) + Suwayomi sources + Tachiyomi extensions
   - **3x more sources available**

2. **Superior Reader**
   - Suwayomi: Basic HTML-based reader
   - WebReader: Canvas-based reader with smooth scrolling, customizable themes, TTS, gestures

3. **Better UI/UX**
   - Suwayomi: Functional but basic WebUI
   - WebReader: Modern, animated, responsive design with Tailwind + Framer Motion

4. **Enhanced Debugging**
   - Suwayomi: Basic logging
   - WebReader: Source health dashboard, request inspector, performance metrics

5. **True Offline Support**
   - Suwayomi: Limited offline capability
   - WebReader: Full PWA with service worker, offline reading, background sync

### Architecture Comparison

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Suwayomi Architecture                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Web Browser (Client)                       │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │           Tachiyomi WebUI (React)                      │  │   │
│  │  │  - Basic library view                                  │  │   │
│  │  │  - Basic reader                                        │  │   │
│  │  │  - Basic settings                                      │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │ HTTP / WebSocket                      │
│  ┌──────────────────────────▼───────────────────────────────────┐   │
│  │                  Suwayomi Server (Ktor)                       │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              REST API Layer                             │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │         Tachiyomi Source Engine (Bundled)              │  │   │
│  │  │  - Loads Tachiyomi extension JARs                      │  │   │
│  │  │  - Executes source code                                │  │   │
│  │  │  - Returns manga data                                  │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │   │
│  │  │  Database     │  │  File Store  │  │  Download Mgr    │  │   │
│  │  │  (SQLite)     │  │  (Images)    │  │                  │  │   │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                       WebReader Architecture                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Web Browser (Client)                       │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │           Custom React UI + Next.js                    │  │   │
│  │  │  - Beautiful library with animations                   │  │   │
│  │  │  - Canvas-based reader with gestures                   │  │   │
│  │  │  - PWA with offline support                            │  │   │
│  │  │  - Advanced settings with live preview                 │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │ HTTP / WebSocket / SSE                │
│  ┌──────────────────────────▼───────────────────────────────────┐   │
│  │                  WebReader Server (Ktor)                      │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              REST API + GraphQL Layer                   │  │   │
│  │  │  - OpenAPI documentation                               │  │   │
│  │  │  - WebSocket for real-time updates                     │  │   │
│  │  │  - Server-Sent Events for progress                     │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │         Unified Source Runtime Engine                  │  │   │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │  │   │
│  │  │  │ IReader DSL  │  │ IReader JS   │  │ Suwayomi    │ │  │   │
│  │  │  │ Runtime      │  │ Runtime      │  │ Bridge      │ │  │   │
│  │  │  └──────────────┘  └──────────────┘  └─────────────┘ │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │   │
│  │  │  Database     │  │  File Store  │  │  Cache Layer     │  │   │
│  │  │  (SQLDelight) │  │  (Covers/    │  │  (Caffeine/      │  │   │
│  │  │              │  │   Chapters)   │  │   Redis)         │  │   │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │         Health Monitor + Dev Tools                     │  │   │
│  │  │  - Source health dashboard                             │  │   │
│  │  │  - Request inspector                                   │  │   │
│  │  │  - Performance metrics                                 │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Can WebReader Replace Suwayomi?

**Yes!** WebReader is designed to be a **superset** of Suwayomi's functionality:

| Suwayomi Feature | WebReader Equivalent |
|------------------|----------------------|
| Tachiyomi extensions | ✅ Supported via bridge |
| Manga library | ✅ Enhanced with better UI |
| Chapter downloads | ✅ Enhanced with queue management |
| Reading history | ✅ Enhanced with statistics |
| Categories | ✅ Enhanced with smart categories |
| Update checking | ✅ Enhanced with scheduling |
| **IReader sources** | ✅ **Native support (unique)** |
| **JS sources** | ✅ **Native support (unique)** |
| **Better reader** | ✅ **Canvas-based (unique)** |
| **Offline PWA** | ✅ **Full offline support (unique)** |

---

## 17. Embedded JVM for Sources - Detailed Explanation

### What Is "Embedded JVM"?

When we say "embedded JVM for sources," we mean running a Java Virtual Machine **inside** your server process to execute source code that was originally written for Android/Desktop JVM environments.

### Why Do We Need This?

**The Problem:**
- IReader sources are written in **Kotlin/Java** and compiled to **JVM bytecode** (.class files in JARs)
- Suwayomi/Tachiyomi extensions are also **JVM bytecode**
- These sources expect to run on a JVM with specific classes available (IReader's source-api, Android SDK, etc.)

**The Solution:**
Embed a JVM in your server to execute these sources directly, without rewriting them.

### How It Works

```
┌─────────────────────────────────────────────────────────────────────┐
│                    WebReader Server Process                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Ktor Application                           │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              REST API Handlers                          │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │           Source Manager Service                        │  │   │
│  │  │  - Loads source JARs                                   │  │   │
│  │  │  - Manages source lifecycle                            │  │   │
│  │  │  - Routes requests to sources                          │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Embedded JVM                               │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              System ClassLoader                        │  │   │
│  │  │  - IReader source-api classes                          │  │   │
│  │  │  - Ktor HTTP client classes                            │  │   │
│  │  │  - Kotlin stdlib                                       │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │           Source ClassLoader (per source)              │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Source A JAR                                     │  │  │   │
│  │  │  │  - MySource.class                                 │  │  │   │
│  │  │  │  - Source-specific dependencies                   │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Source B JAR                                     │  │  │   │
│  │  │  │  - AnotherSource.class                            │  │  │   │
│  │  │  │  - Different dependencies                         │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Implementation Approaches

#### Approach 1: Same JVM (Recommended for Ktor)

Since Ktor **already runs on JVM**, you don't need a separate embedded JVM. You can load sources directly into the same JVM:

```kotlin
// SourceLoader.kt - Load IReader sources directly
class SourceLoader(
    private val sourcesDir: Path,
    private val dependencies: Dependencies
) {
    private val loadedSources = mutableMapOf<String, CatalogSource>()
    
    fun loadSource(jarPath: Path): CatalogSource {
        // Create a classloader for this source
        val classLoader = URLClassLoader(
            arrayOf(jarPath.toUri().toURL()),
            this::class.java.classLoader  // Parent has IReader classes
        )
        
        // Find the source class (implements CatalogSource)
        val sourceClass = classLoader.loadClasses()
            .firstOrNull { CatalogSource::class.java.isAssignableFrom(it) }
            ?: throw IllegalArgumentException("No CatalogSource found in $jarPath")
        
        // Instantiate with dependencies
        val constructor = sourceClass.getConstructor(Dependencies::class.java)
        val source = constructor.newInstance(dependencies) as CatalogSource
        
        loadedSources[source.id.toString()] = source
        return source
    }
}
```

**Advantages:**
- ✅ No overhead - same JVM
- ✅ Direct method calls (no IPC)
- ✅ Shared memory for caching
- ✅ Simple debugging

**Disadvantages:**
- ⚠️ Sources share classpath (potential conflicts)
- ⚠️ One bad source can crash the server (needs isolation)

#### Approach 2: Isolated ClassLoaders (Production)

For better isolation, use separate ClassLoader per source:

```kotlin
// IsolatedSourceLoader.kt
class IsolatedSourceLoader(
    private val sourcesDir: Path
) {
    private val sourceIsolates = mutableMapOf<String, SourceIsolate>()
    
    fun loadSource(jarPath: Path): SourceIsolate {
        // Create isolated classloader
        val isolatedClassLoader = IsolatedClassLoader(
            urls = arrayOf(jarPath.toUri().toURL()),
            parent = ClassLoader.getSystemClassLoader(),
            // Block access to server internals
            blockedPackages = listOf(
                "ireader.server.",
                "io.ktor."
            )
        )
        
        return SourceIsolate(
            classLoader = isolatedClassLoader,
            source = instantiateSource(isolatedClassLoader)
        )
    }
}

// SourceIsolate.kt - Wrapper for isolated source
class SourceIsolate(
    val classLoader: ClassLoader,
    val source: CatalogSource
) {
    suspend fun search(query: String, page: Int): SearchResults {
        // Execute in isolated context
        return withContext(Dispatchers.IO) {
            source.searchPopular(page)  // Direct call, but isolated
        }
    }
}
```

#### Approach 3: Separate JVM Process (Maximum Isolation)

For maximum isolation, run sources in separate JVM processes:

```kotlin
// ProcessSourceLoader.kt
class ProcessSourceLoader(
    private val sourcesDir: Path
) {
    private val sourceProcesses = mutableMapOf<String, SourceProcess>()
    
    fun loadSource(jarPath: Path): SourceProcess {
        // Start a new JVM process for this source
        val process = ProcessBuilder(
            "java",
            "-cp", jarPath.toString() + ":${getSourceApiJar()}",
            "ireader.source.runner.SourceRunner",
            "--port", findFreePort().toString()
        ).start()
        
        return SourceProcess(
            process = process,
            port = port,
            client = HttpClient {
                install(ContentNegotiation) { json() }
            }
        )
    }
    
    suspend fun search(sourceId: String, query: String, page: Int): SearchResults {
        val sourceProcess = sourceProcesses[sourceId]!!
        return sourceProcess.client.get("http://localhost:${sourceProcess.port}/search") {
            parameter("query", query)
            parameter("page", page)
        }.body()
    }
}
```

**Advantages:**
- ✅ Maximum isolation (one crash doesn't affect others)
- ✅ Can restart individual sources
- ✅ Different JVM versions per source

**Disadvantages:**
- ❌ Higher memory usage (separate JVM per source)
- ❌ IPC overhead (HTTP calls instead of method calls)
- ❌ More complex deployment

### Comparison Table

| Approach | Isolation | Memory | Performance | Complexity | Recommended For |
|----------|-----------|--------|-------------|------------|-----------------|
| **Same JVM** | Low | Lowest | Fastest | Low | Development, trusted sources |
| **Isolated ClassLoaders** | Medium | Low | Fast | Medium | **Production (recommended)** |
| **Separate JVM Process** | High | High | Slower | High | Untrusted sources, plugins |

### IReader's Current Approach

IReader uses **Isolated ClassLoaders** on Android:

```kotlin
// From IReader's PluginClassLoader.android.kt
class PluginClassLoader(
    private val pluginPath: String,
    private val parent: ClassLoader
) : DexClassLoader(
    pluginPath,                    // Path to plugin APK/JAR
    optimizedDirectory,            // Optimized dex output
    nativeLibraryPath,             // Native libraries
    parent                         // Parent classloader
) {
    override fun loadClass(name: String): Class<*> {
        // Try to load from plugin first
        try {
            return findClass(name)
        } catch (e: ClassNotFoundException) {
            // Fall back to parent (IReader classes)
            return parent.loadClass(name)
        }
    }
}
```

### WebReader Recommendation

For WebReader, I recommend **Isolated ClassLoaders** (Approach 2):

```kotlin
// WebReaderSourceRuntime.kt
class WebReaderSourceRuntime(
    private val sourcesDir: Path,
    private val httpClient: HttpClient,
    private val preferences: PreferenceStore
) {
    private val sourceRegistry = ConcurrentHashMap<String, IsolatedSource>()
    
    suspend fun loadAllSources() {
        val sourceFiles = sourcesDir.listFiles { it.extension in listOf("jar", "apk") }
        
        sourceFiles.forEach { file ->
            launch(Dispatchers.IO) {
                try {
                    val source = loadSource(file)
                    sourceRegistry[source.id.toString()] = source
                    logger.info { "Loaded source: ${source.name} (${source.id})" }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to load source: ${file.name}" }
                }
            }
        }
    }
    
    private fun loadSource(file: Path): IsolatedSource {
        // Create isolated classloader
        val classLoader = URLClassLoader(
            arrayOf(file.toUri().toURL()),
            getSystemClassLoader()
        )
        
        // Read source metadata
        val manifest = classLoader.getResourceAsStream("source-manifest.json")
            ?.use { Json.decodeFromString<SourceManifest>(it.readBytes().decodeToString()) }
            ?: throw IllegalArgumentException("No source manifest found")
        
        // Find and instantiate source class
        val sourceClass = classLoader.loadClass(manifest.mainClass)
        val source = sourceClass.getConstructor(Dependencies::class.java)
            .newInstance(Dependencies(httpClient, preferences)) as CatalogSource
        
        return IsolatedSource(
            id = source.id,
            name = source.name,
            source = source,
            classLoader = classLoader
        )
    }
    
    suspend fun search(sourceId: String, query: String, page: Int): SearchResults {
        val source = sourceRegistry[sourceId]
            ?: throw SourceNotFoundException(sourceId)
        
        return withContext(Dispatchers.IO) {
            source.search(query, page)
        }
    }
}

data class IsolatedSource(
    val id: Long,
    val name: String,
    val source: CatalogSource,
    val classLoader: ClassLoader
)
```

### Handling Source Dependencies

Sources may have dependencies (e.g., JS engine, HTML parsers). Handle this with:

```kotlin
// DependencyResolver.kt
class DependencyResolver(
    private val pluginsDir: Path
) {
    private val loadedPlugins = mutableMapOf<String, Plugin>()
    
    fun resolveDependencies(source: SourceManifest): ClassLoader {
        // Load required plugins first
        val pluginClassLoaders = source.dependencies.map { dep ->
            val pluginFile = pluginsDir.resolve("${dep.name}.iplugin")
            loadPlugin(pluginFile)
        }
        
        // Create composite classloader
        return CompositeClassLoader(
            pluginClassLoaders + getSystemClassLoader()
        )
    }
}
```

### Summary

| Concept | Explanation |
|---------|-------------|
| **What** | Running source code (JVM bytecode) inside your server |
| **Why** | IReader/Suwayomi sources are written in Kotlin/Java |
| **How** | Use ClassLoader to load JARs and instantiate source classes |
| **Best Approach** | Isolated ClassLoaders for balance of safety and performance |
| **Alternative** | Separate JVM processes for maximum isolation |

---

## 18. Single JVM Instance - Memory Analysis & Multi-User Architecture

### JVM Memory Breakdown for Single Instance

For a **single JVM instance** running Ktor + sources, here's the memory analysis:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    JVM Memory Layout (Single Instance)               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    JVM Overhead                               │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │ JVM Internal Structures          ~50-100 MB            │  │   │
│  │  │ - Metaspace (class metadata)                            │  │   │
│  │  │ - Thread stacks                                           │  │   │
│  │  │ - JIT compiled code cache                               │  │   │
│  │  │ - GC internal structures                                │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Application Code                           │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │ Ktor + Dependencies             ~30-50 MB              │  │   │
│  │  │ - Ktor server runtime                                     │  │   │
│  │  │ - kotlinx.serialization                                  │  │   │
│  │  │ - Ktor HTTP client                                       │  │   │
│  │  │ - SQLDelight                                             │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │ IReader source-api              ~10-20 MB              │  │   │
│  │  │ - Core source interfaces                                │  │   │
│  │  │ - HTTP client wrappers                                  │  │   │
│  │  │ - Parsing utilities                                     │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Loaded Sources                             │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │ Per Source (average)            ~2-5 MB                │  │   │
│  │  │ - Source class bytecode                                   │  │   │
│  │  │ - Source-specific dependencies                          │  │   │
│  │  │ - Cached responses                                      │  │   │
│  │  │ - Parsing state                                         │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │                                                              │   │
│  │  Example: 50 sources × 3 MB = ~150 MB                       │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Runtime Data                               │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │ Database Connection Pool         ~10-20 MB             │  │   │
│  │  │ HTTP Client Cache                ~20-50 MB             │  │   │
│  │  │ Response Cache                   ~50-100 MB            │  │   │
│  │  │ User Sessions                    ~1-5 MB per 100 users│  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Memory Estimates by Server Size

| Configuration | Sources | Concurrent Users | RAM Needed | Cost/Month |
|---------------|---------|------------------|------------|------------|
| **Minimal** | 10 | 5-10 | 512 MB | $3-5 |
| **Small** | 50 | 20-50 | 1 GB | $5-10 |
| **Medium** | 100 | 50-100 | 2 GB | $10-20 |
| **Large** | 200+ | 100-200 | 4 GB | $20-40 |

### Optimized JVM Flags for Low Memory

```bash
# For 512 MB server (minimal configuration)
java -Xms256m -Xmx384m \
     -XX:+UseSerialGC \
     -XX:MaxMetaspaceSize=128m \
     -XX:CompressedClassSpaceSize=32m \
     -XX:+UseCompressedOops \
     -XX:+UseCompressedClassPointers \
     -XX:ReservedCodeCacheSize=32m \
     -XX:+TieredCompilation \
     -XX:TieredStopAtLevel=1 \
     -jar webreader-server.jar

# For 1 GB server (recommended for small-medium)
java -Xms512m -Xmx768m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -XX:MaxMetaspaceSize=192m \
     -XX:CompressedClassSpaceSize=48m \
     -XX:+UseCompressedOops \
     -XX:ReservedCodeCacheSize=48m \
     -jar webreader-server.jar

# For 2 GB server (medium-large)
java -Xms1g -Xmx1536m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -XX:MaxMetaspaceSize=256m \
     -XX:CompressedClassSpaceSize=64m \
     -XX:+UseCompressedOops \
     -XX:ReservedCodeCacheSize=64m \
     -jar webreader-server.jar
```

### Multi-User Architecture (Single JVM)

```
┌─────────────────────────────────────────────────────────────────────┐
│                    WebReader Multi-User Architecture                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Load Balancer (Optional)                   │   │
│  │              (Nginx / Cloudflare / Direct)                    │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │                                       │
│  ┌──────────────────────────▼───────────────────────────────────┐   │
│  │                    Ktor Server (Single JVM)                   │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Authentication Middleware                  │  │   │
│  │  │  - JWT token validation                                │  │   │
│  │  │  - Session management                                  │  │   │
│  │  │  - Rate limiting per user                              │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │                                                              │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              User Context (Per Request)                 │  │   │
│  │  │  - User ID                                              │  │   │
│  │  │  - User preferences                                     │  │   │
│  │  │  - User's library (isolated)                            │  │   │
│  │  │  - Rate limit bucket                                    │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │                                                              │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Shared Source Runtime                      │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Source A (shared across all users)              │  │  │   │
│  │  │  │  - Single instance serves all users              │  │  │   │
│  │  │  │  - Cached responses shared                       │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Source B (shared across all users)              │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │                                                              │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Per-User Data Isolation                    │  │   │
│  │  │  - User A: Library A, History A, Bookmarks A          │  │   │
│  │  │  - User B: Library B, History B, Bookmarks B          │  │   │
│  │  │  - User C: Library C, History C, Bookmarks C          │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Database (SQLite/PostgreSQL)               │   │
│  │  - Row-level security (user_id column)                       │   │
│  │  - Connection pooling (HikariCP)                             │   │
│  │  - Shared cache for sources                                  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### User Isolation Strategy

```kotlin
// UserContext.kt - Per-request user isolation
data class UserContext(
    val userId: Long,
    val username: String,
    val preferences: UserPreferences,
    val rateLimitBucket: RateLimitBucket
)

// Database schema with user isolation
// All tables include user_id column
object Books : Table() {
    val id = long("id").autoIncrement()
    val userId = long("user_id")  // User isolation
    val sourceId = long("source_id")
    val title = varchar("title", 500)
    // ...
    
    override val primaryKey = PrimaryKey(id)
    
    // Index for fast user queries
    val userIndex = index("idx_books_user", userId)
}

// Repository with user filtering
class BookRepository {
    suspend fun getUserBooks(userId: Long): List<Book> = dbQuery {
        Books.select { Books.userId eq userId }
            .map { it.toBook() }
    }
}
```

### Performance Optimizations for Multi-User

#### 1. Shared Source Cache

```kotlin
// SourceCache.kt - Shared across all users
class SourceCache(
    private val cacheSize: Int = 100  // Max cached responses
) {
    private val cache = Caffeine.newBuilder()
        .maximumSize(cacheSize.toLong())
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build<String, Any>()
    
    suspend fun getPopular(sourceId: Long, page: Int): PopularResult {
        val cacheKey = "popular:$sourceId:$page"
        
        return cache.get(cacheKey) {
            // Fetch from source (only once for all users)
            runBlocking { sourceManager.getSource(sourceId).getPopular(page) }
        } as PopularResult
    }
}
```

#### 2. Connection Pooling

```kotlin
// Database connection pool (shared)
val database = Database.connect(
    url = "jdbc:sqlite:webreader.db",
    driver = "org.sqlite.JDBC",
    // HikariCP settings for SQLite
    pool = HikariDataSource().apply {
        maximumPoolSize = 10
        connectionTimeout = 30000
        idleTimeout = 600000
        maxLifetime = 1800000
    }
)

// HTTP client connection pool (shared)
val httpClient = HttpClient(OkHttp) {
    engine {
        config {
            connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
        }
    }
}
```

#### 3. Rate Limiting Per User

```kotlin
// RateLimiter.kt
class RateLimiter(
    private val requestsPerMinute: Int = 30
) {
    private val buckets = ConcurrentHashMap<Long, TokenBucket>()
    
    fun acquire(userId: Long): Boolean {
        val bucket = buckets.computeIfAbsent(userId) {
            TokenBucket(requestsPerMinute, 1, TimeUnit.MINUTES)
        }
        return bucket.tryAcquire()
    }
}

// Ktor middleware
fun Application.rateLimitModule() {
    intercept(ApplicationCallPipeline.Plugins) {
        val userId = call.principal<UserPrincipal>()?.userId
            ?: return@intercept
        
        if (!rateLimiter.acquire(userId)) {
            call.respond(HttpStatusCode.TooManyRequests, "Rate limit exceeded")
            return@intercept finish()
        }
    }
}
```

#### 4. Lazy Source Loading

```kotlin
// Load sources only when first requested
class LazySourceManager(
    private val sourceLoader: SourceLoader
) {
    private val loadedSources = ConcurrentHashMap<Long, CatalogSource>()
    private val loadingSources = ConcurrentHashMap<Long, Deferred<CatalogSource>>()
    
    suspend fun getSource(sourceId: Long): CatalogSource {
        // Return if already loaded
        loadedSources[sourceId]?.let { return it }
        
        // Start loading if not already loading
        val deferred = loadingSources.computeIfAbsent(sourceId) {
            CoroutineScope(Dispatchers.IO).async {
                sourceLoader.loadSource(sourceId).also {
                    loadedSources[sourceId] = it
                    loadingSources.remove(sourceId)
                }
            }
        }
        
        return deferred.await()
    }
}
```

### Recommended Cheap Server Configurations

| Provider | Plan | RAM | CPU | Storage | Cost | Users Supported |
|----------|------|-----|-----|---------|------|-----------------|
| **Hetzner** | CX11 | 2 GB | 1 vCPU | 20 GB | €3.29 | 20-50 |
| **DigitalOcean** | Basic | 1 GB | 1 vCPU | 25 GB | $6 | 10-20 |
| **Vultr** | Cloud | 1 GB | 1 vCPU | 25 GB | $5 | 10-20 |
| **Oracle** | Free Tier | 1 GB | 1 OCPU | 50 GB | Free | 5-10 |
| **Raspberry Pi** | 4B | 2 GB | 4 cores | 64 GB | ~$55 one-time | 10-20 |

### Docker Compose for Cheap Server

```yaml
# docker-compose.yml - Optimized for 1-2 GB RAM
version: '3.8'

services:
  webreader:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m
      - DB_URL=jdbc:sqlite:/data/webreader.db
      - CACHE_SIZE=50
      - MAX_SOURCES=50
    volumes:
      - ./data:/data
      - ./sources:/sources:ro
    deploy:
      resources:
        limits:
          memory: 768M
          cpus: '1.5'
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### Summary: Single JVM is the Right Choice

| Factor | Single JVM | Multiple JVMs |
|--------|------------|---------------|
| **Memory** | ✅ 512 MB - 1 GB | ❌ 2-4 GB+ |
| **Startup Time** | ✅ 2-5 seconds | ❌ 10-30 seconds |
| **Source Sharing** | ✅ Shared cache | ❌ Separate caches |
| **Debugging** | ✅ Single process | ❌ Multiple processes |
| **Deployment** | ✅ Simple | ❌ Complex |
| **Isolation** | ⚠️ ClassLoaders | ✅ Process-level |

**Recommendation:** Use **single JVM with Isolated ClassLoaders** for sources. This gives you:
- Low memory usage (512 MB - 1 GB for 50 sources, 20-50 users)
- Shared caching (responses cached once for all users)
- Simple deployment (single Docker container)
- Good enough isolation (ClassLoader per source)

---

## 1. Project Vision

WebReader is a self-hosted, web-based reader application inspired by [IReader](https://github.com/IReaderorg/IReader) and [Suwayomi](https://github.com/Suwayomi/Suwayomi-Server). It allows users to:

- Host a server on their local machine or a remote server
- Access the reader UI from any modern browser (desktop, tablet, phone)
- Use content sources from **both** IReader (Kotlin/JS extensions) and Suwayomi (Java/Kotlin extensions) simultaneously
- Enjoy a beautiful, performant, and highly customizable reading experience
- Debug easily with comprehensive logging and developer tools

### Key Differentiators

| Feature | IReader | Suwayomi | WebReader |
|---------|---------|----------|-----------|
| Platform | Android + Desktop | Server + Web UI | Server + Web UI |
| UI Framework | Compose Multiplatform | React (Tachiyomi WebUI) | TBD (see options) |
| Source Compatibility | Kotlin DSL + JS | Java/Kotlin (Tachiyomi) | **Both** |
| Self-hosted | No | Yes | Yes |
| Amazing UI | Yes | Basic | **Yes (goal)** |
| Performance | Good | Good | **Excellent (goal)** |

---

## 2. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        WebReader Architecture                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Web Browser (Client)                       │   │
│  │  ┌─────────────┐  ┌──────────────┐  ┌───────────────────┐  │   │
│  │  │  Library UI  │  │  Reader UI   │  │  Settings/Config  │  │   │
│  │  └─────────────┘  └──────────────┘  └───────────────────┘  │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │ HTTP / WebSocket                      │
│  ┌──────────────────────────▼───────────────────────────────────┐   │
│  │                  WebReader Server (Backend)                   │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              REST API / GraphQL Layer                   │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │   │
│  │  │  Library Mgr  │  │  Download Mgr│  │  Source Manager  │  │   │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Source Runtime Engine                      │  │   │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │  │   │
│  │  │  │ IReader DSL  │  │ IReader JS   │  │ Suwayomi    │ │  │   │
│  │  │  │ Runtime      │  │ Runtime      │  │ Runtime     │ │  │   │
│  │  │  └──────────────┘  └──────────────┘  └─────────────┘ │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │   │
│  │  │  Database     │  │  File Store  │  │  Cache Layer     │  │   │
│  │  │  (SQLite/PG)  │  │  (Covers/    │  │  (Redis/In-Mem)  │  │   │
│  │  │              │  │   Chapters)   │  │                  │  │   │
│  │  └──────────────┘  └──────────────┘  └──────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Technology Stack Options

### Option A: Kotlin Full-Stack (Recommended)

**Backend:** Ktor + Kotlin
**Frontend:** Kotlin/JS + Compose HTML or Kotlin/Wasm
**Database:** SQLDelight (shared with IReader)
**Source Runtime:** Reuse IReader source-api directly

#### Pros:
- **Maximum code reuse** from IReader project (source-api, domain logic, data layer)
- **Single language** (Kotlin) across entire stack
- **Type safety** end-to-end
- **Familiar ecosystem** for IReader contributors
- **SQLDelight** already used in IReader, can share database schema
- **Ktor** is battle-tested, excellent performance, coroutines-native
- **Compose HTML** allows sharing UI patterns from IReader's Compose Multiplatform

#### Cons:
- Compose HTML has **limited browser support** and smaller ecosystem than React/Vue
- Kotlin/Wasm is still **experimental** for UI
- Smaller community for Kotlin full-stack compared to Node.js
- May need to bridge Suwayomi sources via JVM interop

#### Sub-options for Frontend:

| Approach | Maturity | Performance | Ecosystem | Recommendation |
|----------|----------|-------------|-----------|----------------|
| Compose HTML | Medium | Good | Small | Viable for MVP |
| Kotlin/JS + React wrappers | Medium | Good | Medium | Good balance |
| Kotlin/Wasm + Canvas | Experimental | Excellent | Tiny | Future option |

---

### Option B: Kotlin Backend + TypeScript Frontend (Pragmatic)

**Backend:** Ktor + Kotlin
**Frontend:** React/Next.js or Vue/Nuxt (TypeScript)
**Database:** SQLDelight or Exposed + PostgreSQL
**Source Runtime:** Reuse IReader source-api directly

#### Pros:
- **Best of both worlds:** Kotlin for backend (reuse IReader code), TypeScript for frontend (largest ecosystem)
- **React/Vue** have massive component libraries, excellent dev tools, huge community
- **Next.js/Nuxt** provide SSR, routing, optimization out of the box
- **Easier to hire** contributors (TypeScript is more common than Kotlin/JS)
- **Better debugging** with browser dev tools, React DevTools, Vue DevTools
- **Mature state management** (Zustand, Pinia, Redux Toolkit)

#### Cons:
- **Two languages** to maintain (Kotlin + TypeScript)
- **API contract** between frontend and backend needs careful design
- **Type sharing** requires code generation (OpenAPI/GraphQL)
- Slightly **less code reuse** than full Kotlin stack

#### Recommended Frontend Frameworks:

| Framework | Pros | Cons | Recommendation |
|-----------|------|------|----------------|
| **React + Next.js** | Huge ecosystem, SSR, great dev tools | Larger bundle size | **Top choice** |
| **Vue 3 + Nuxt** | Simpler learning curve, great perf | Smaller ecosystem than React | Strong alternative |
| **Svelte + SvelteKit** | Best performance, smallest bundle | Smallest ecosystem | Future option |
| **SolidJS + SolidStart** | Excellent performance, fine-grained reactivity | Very new, small ecosystem | Experimental |

---

### Option C: Rust Backend + TypeScript Frontend (Performance-First)

**Backend:** Axum/Actix-web + Rust
**Frontend:** React/Next.js or Vue/Nuxt (TypeScript)
**Database:** SQLx + PostgreSQL
**Source Runtime:** Custom implementation or FFI to IReader

#### Pros:
- **Maximum performance** and memory safety
- **Excellent concurrency** with async Rust
- **Small binary size** for server deployment
- **Great for CPU-intensive** tasks (parsing, image processing)

#### Cons:
- **Cannot reuse IReader Kotlin code** - must reimplement everything
- **Steeper learning curve** for team
- **Longer development time**
- **Source compatibility** requires reimplementing both IReader and Suwayomi extension APIs
- **Smaller ecosystem** for web frameworks compared to Kotlin/Node.js

---

### Option D: Node.js Full-Stack (Fastest Development)

**Backend:** Node.js + Fastify/Express
**Frontend:** React/Next.js or Vue/Nuxt (TypeScript)
**Database:** Prisma + PostgreSQL
**Source Runtime:** Custom JS runtime or bridge to IReader

#### Pros:
- **Fastest development** with single language (TypeScript)
- **Largest ecosystem** of packages
- **Easy to find contributors**
- **Great tooling** and debugging

#### Cons:
- **Cannot reuse IReader Kotlin code**
- **Single-threaded** limitations for CPU-intensive tasks
- **Source compatibility** requires reimplementing extension APIs
- **Performance** not as good as Kotlin/Rust for concurrent operations

---

## 4. Recommended Approach: Option B (Ktor + React/Next.js)

### Why This Is The Best Choice

1. **Maximum IReader code reuse** - The source-api, domain models, and much of the data layer can be directly reused from IReader
2. **Amazing UI** - React ecosystem has the best component libraries, animation libraries (Framer Motion), and design systems
3. **Amazing Performance** - Ktor is extremely fast (built on coroutines), React 18+ with concurrent features is excellent
4. **Easy to Debug** - Browser dev tools + React DevTools + Ktor logging = excellent debugging experience
5. **No Limitations** - Both ecosystems are mature and can handle any feature requirement
6. **Source Compatibility** - Can directly embed IReader's source runtime; Suwayomi sources can run via JVM interop

### Detailed Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Browser)                        │
│  React 18 + Next.js 14 + TypeScript + Tailwind CSS         │
│  State: Zustand + React Query                               │
│  UI: Radix UI + Framer Motion                              │
│  Reader: Custom canvas-based reader component               │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST API + WebSocket
┌──────────────────────────▼──────────────────────────────────┐
│                    Backend (Server)                          │
│  Ktor + Kotlin Coroutines                                  │
│  Serialization: kotlinx.serialization                      │
│  Database: SQLDelight (SQLite/PostgreSQL)                  │
│  Cache: Caffeine (in-memory) + optional Redis              │
│  File Storage: Local filesystem / S3-compatible            │
│  Source Runtime: IReader source-api (embedded)             │
│  Suwayomi Bridge: JVM interop or REST proxy               │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Source Compatibility Strategy

### IReader Sources (Kotlin DSL + JS Runtime)

IReader has two types of sources:

1. **Kotlin DSL Sources** - Written using `NovelSourceBuilder` DSL, compiled to JVM bytecode
2. **JavaScript Sources** - Written in JS, executed via IReader's JS runtime (`source-runtime-js`)

**Strategy:** Embed IReader's source-api directly into the Ktor server. Since both are Kotlin/JVM, this is straightforward.

```kotlin
// IReader source-api can be directly reused
val source = NovelSourceBuilder("My Source") {
    baseUrl = "https://example.com"
    // ... DSL configuration
}
```

### Suwayomi Sources (Java/Kotlin - Tachiyomi Extensions)

Suwayomi uses Tachiyomi's extension system (Java/Kotlin JAR files).

**Strategy Options:**

| Approach | Complexity | Performance | Maintenance |
|----------|-----------|-------------|-------------|
| **A. JVM Interop** - Load Suwayomi JARs directly via classloader | Medium | Excellent | Medium |
| **B. REST Proxy** - Run Suwayomi server alongside, proxy requests | Low | Good | Low |
| **C. Source Translation** - Auto-convert Suwayomi sources to IReader format | High | Excellent | High |

**Recommended:** Start with **Option B (REST Proxy)** for MVP, then implement **Option A (JVM Interop)** for production.

---

## 6. Database Design

### Reuse IReader's SQLDelight Schema

IReader already has a well-designed database schema with tables for:
- `book` - Book metadata
- `chapter` - Chapter information
- `category` - User categories/collections
- `download` - Download queue
- `history` - Reading history
- `bookmark` - Bookmarks
- `source` - Source information

**Strategy:** Reuse IReader's SQLDelight schema directly. Add web-specific tables:
- `session` - User sessions (for multi-user support)
- `sync` - Sync state for offline reading
- `setting` - Server-side settings

---

## 7. Project Structure

```
webreader/
├── backend/                          # Ktor server
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   ├── api/             # REST API routes
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── source/          # Source runtime integration
│   │   │   │   ├── database/        # Database access
│   │   │   │   ├── model/           # Domain models
│   │   │   │   └── Application.kt   # Main entry point
│   │   │   └── resources/
│   │   └── test/
│   └── build.gradle.kts
│
├── frontend/                         # React/Next.js app
│   ├── src/
│   │   ├── app/                     # Next.js app router
│   │   ├── components/              # Reusable UI components
│   │   ├── features/                # Feature modules
│   │   │   ├── library/             # Library management
│   │   │   ├── reader/              # Reading experience
│   │   │   ├── browse/              # Source browsing
│   │   │   ├── downloads/           # Download management
│   │   │   └── settings/            # Settings
│   │   ├── hooks/                   # Custom React hooks
│   │   ├── lib/                     # Utilities
│   │   ├── stores/                  # Zustand stores
│   │   └── types/                   # TypeScript types
│   ├── public/
│   └── package.json
│
├── source-adapter/                   # Source compatibility layer
│   ├── ireader/                     # IReader source integration
│   ├── suwayomi/                    # Suwayomi source bridge
│   └── common/                      # Shared interfaces
│
├── shared/                           # Shared code (optional)
│   └── src/                         # Common models, API types
│
├── docker/                           # Docker deployment
│   ├── Dockerfile
│   └── docker-compose.yml
│
└── docs/                             # Documentation
```

---

## 8. Key Features & Implementation Phases

### Phase 1: Foundation (MVP)
- [ ] Ktor server with basic REST API
- [ ] React frontend with basic UI
- [ ] SQLite database with IReader schema
- [ ] IReader Kotlin DSL source support
- [ ] Basic library management (add/remove books)
- [ ] Basic reader with chapter navigation
- [ ] Source browsing and search

### Phase 2: Enhanced Reader
- [ ] Advanced reader with customizable themes
- [ ] Reading progress tracking
- [ ] Bookmarks and notes
- [ ] Text-to-speech support
- [ ] Offline reading (service worker)
- [ ] Gesture support for mobile

### Phase 3: Source Compatibility
- [ ] IReader JavaScript source support
- [ ] Suwayomi source bridge (REST proxy)
- [ ] Extension manager UI
- [ ] Auto-update extensions
- [ ] Source health monitoring

### Phase 4: Advanced Features
- [ ] Multi-user support
- [ ] Sync across devices
- [ ] Download manager
- [ ] Smart recommendations
- [ ] Reading statistics
- [ ] Import/Export (IReader, Suwayomi, Legado)

### Phase 5: Performance & Polish
- [ ] Image optimization pipeline
- [ ] CDN integration for covers
- [ ] Advanced caching strategies
- [ ] Performance monitoring
- [ ] A/B testing framework

---

## 9. Deployment Options

### Local Machine
```bash
# Single binary deployment
./webreader-server
# Opens browser at http://localhost:8080
```

### Docker
```bash
docker run -p 8080:8080 -v /path/to/data:/data webreader/webreader
```

### Docker Compose (with Suwayomi bridge)
```yaml
version: '3'
services:
  webreader:
    image: webreader/webreader
    ports:
      - "8080:8080"
    volumes:
      - ./data:/data
  suwayomi:
    image: ghcr.io/suwayomi/suwayomi-server
    ports:
      - "4567:4567"
```

---

## 10. Performance Considerations

### Backend Performance
- **Ktor + Coroutines** - Non-blocking I/O, handles thousands of concurrent connections
- **Connection pooling** - Database and HTTP client connection pools
- **Response compression** - Gzip/Brotli for API responses
- **Caching** - Multi-level cache (in-memory + Redis)
- **Lazy loading** - Sources loaded on-demand

### Frontend Performance
- **Next.js SSR/SSG** - Fast initial page loads
- **React Concurrent Features** - Smooth UI during data fetching
- **Virtual scrolling** - For large lists (library, chapter list)
- **Image optimization** - Next.js Image component, WebP format
- **Code splitting** - Route-based lazy loading
- **Service Worker** - Offline support and caching

### Reader Performance
- **Canvas-based rendering** - Smooth scrolling, no DOM overhead
- **Preloading** - Load next/previous chapters in background
- **Text shaping** - HarfBuzz for complex scripts
- **Font subsetting** - Only load required glyphs

---

## 11. Debugging & Developer Experience

### Backend Debugging
- Structured logging with kotlinx-logging
- Request/response logging middleware
- Health check endpoints
- Swagger/OpenAPI documentation
- Kotlin coroutine debugger

### Frontend Debugging
- React DevTools
- Redux DevTools (if using Redux)
- Network tab for API inspection
- TypeScript strict mode
- ESLint + Prettier

### Source Debugging
- Source execution logs
- Request/response inspection for each source
- Source health dashboard
- Extension hot-reload during development

---

## 12. Summary & Recommendation

### Recommended Stack

| Component | Technology | Reason |
|-----------|-----------|--------|
| **Backend** | Ktor + Kotlin | Reuse IReader code, excellent performance |
| **Frontend** | React 18 + Next.js 14 + TypeScript | Best UI ecosystem, amazing dev tools |
| **Database** | SQLDelight (SQLite/PostgreSQL) | Reuse IReader schema, type-safe queries |
| **Styling** | Tailwind CSS + Radix UI | Rapid UI development, accessible components |
| **State** | Zustand + React Query | Simple, performant state management |
| **Animation** | Framer Motion | Smooth, declarative animations |
| **Deployment** | Docker + Single binary | Easy deployment anywhere |

### Why Not Other Options?

- **Full Kotlin (Option A):** Compose HTML ecosystem too limited for "amazing UI" goal
- **Rust (Option C):** Cannot reuse IReader code, much longer development time
- **Node.js (Option D):** Cannot reuse IReader code, single-threaded limitations

### Next Steps

1. **Approve this plan** - Confirm the technology choices
2. **Set up project structure** - Create initial repository with backend + frontend
3. **Implement Phase 1** - Build MVP with basic functionality
4. **Iterate** - Add features based on feedback

---

---

## 13. Server Hosting Requirements by Option

### Option A: Kotlin Full-Stack (Ktor + Compose HTML)

| Deployment Type | RAM | CPU | Storage | Notes |
|-----------------|-----|-----|---------|-------|
| **Minimal (1-5 sources)** | 512 MB | 1 core | 500 MB | SQLite, no cache |
| **Recommended (50+ sources)** | 1-2 GB | 2 cores | 2 GB | With JS engine (J2V8/GraalVM) |
| **Heavy (100+ sources)** | 2-4 GB | 2-4 cores | 5 GB | Full JS engine + caching |

**Server Requirements:**
- JVM 17+ (OpenJDK recommended)
- Linux/macOS/Windows
- Optional: Redis for distributed caching

---

### Option B: Ktor + React/Next.js (Recommended)

| Deployment Type | RAM | CPU | Storage | Notes |
|-----------------|-----|-----|---------|-------|
| **Minimal (1-5 sources)** | 256 MB (backend) + 128 MB (frontend) | 1 core | 500 MB | SQLite, static frontend |
| **Recommended (50+ sources)** | 512 MB (backend) + 256 MB (frontend) | 2 cores | 2 GB | With JS engine |
| **Heavy (100+ sources)** | 1-2 GB (backend) + 512 MB (frontend) | 2-4 cores | 5 GB | Full features |

**Server Requirements:**
- JVM 17+ for backend
- Node.js 18+ for frontend (or static build)
- Linux/macOS/Windows
- Optional: PostgreSQL, Redis

**Docker Resource Limits:**
```yaml
# docker-compose.yml
services:
  webreader:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '2'
```

---

### Option C: Rust Backend + TypeScript Frontend

| Deployment Type | RAM | CPU | Storage | Notes |
|-----------------|-----|-----|---------|-------|
| **Minimal (1-5 sources)** | 64 MB (backend) + 128 MB (frontend) | 1 core | 200 MB | Extremely efficient |
| **Recommended (50+ sources)** | 128-256 MB (backend) + 256 MB (frontend) | 2 cores | 1 GB | With source caching |
| **Heavy (100+ sources)** | 512 MB (backend) + 512 MB (frontend) | 2-4 cores | 3 GB | Full features |

**Server Requirements:**
- No runtime dependencies (static binary)
- Linux/macOS/Windows
- Optional: Node.js for frontend (or static build)

**Advantages:**
- Smallest memory footprint
- Fastest startup time
- Single binary deployment
- No GC pauses

---

### Option D: Node.js Full-Stack

| Deployment Type | RAM | CPU | Storage | Notes |
|-----------------|-----|-----|---------|-------|
| **Minimal (1-5 sources)** | 256 MB | 1 core | 500 MB | SQLite |
| **Recommended (50+ sources)** | 512 MB - 1 GB | 2 cores | 2 GB | With caching |
| **Heavy (100+ sources)** | 1-2 GB | 2-4 cores | 5 GB | Full features |

**Server Requirements:**
- Node.js 18+
- Linux/macOS/Windows
- Optional: PostgreSQL, Redis

---

### Hosting Comparison Summary

| Option | Binary Size | Startup Time | Memory (Idle) | Memory (Load) | Best For |
|--------|-------------|--------------|---------------|---------------|----------|
| **Kotlin Full-Stack** | 50-100 MB (JAR) | 2-5s | 100-200 MB | 500 MB - 2 GB | Code reuse |
| **Ktor + React** | 30-50 MB (JAR) + static | 1-3s | 50-100 MB | 256 MB - 1 GB | **Recommended** |
| **Rust + React** | 5-20 MB (binary) | 0.1-0.5s | 10-50 MB | 64-256 MB | Performance |
| **Node.js** | N/A (source) | 1-2s | 50-100 MB | 256 MB - 1 GB | Fast development |

---

## 14. Rust Backend - Detailed Evaluation

### Why Rust Is Worth Considering

After analyzing the requirements more deeply, Rust deserves a closer look:

#### Advantages for WebReader

1. **Exceptional Performance**
   - Zero-cost abstractions
   - No garbage collection pauses
   - Predictable latency for source scraping
   - Excellent for CPU-intensive parsing

2. **Memory Efficiency**
   - 10-50x less memory than JVM
   - Ideal for low-end hardware (Raspberry Pi, VPS)
   - Can run on $5/month VPS comfortably

3. **Single Binary Deployment**
   - No runtime dependencies
   - Easy Docker images (5-20 MB)
   - Cross-compilation support

4. **Safety & Reliability**
   - Memory safety without GC
   - Thread safety for concurrent source requests
   - Excellent error handling

5. **Async Ecosystem**
   - Tokio runtime (battle-tested)
   - Excellent HTTP client (reqwest)
   - Great WebSocket support

#### Rust Web Framework Options

| Framework | Maturity | Performance | Ecosystem | Recommendation |
|-----------|----------|-------------|-----------|----------------|
| **Axum** | High | Excellent | Growing | **Top choice** |
| **Actix-web** | Very High | Excellent | Mature | Strong alternative |
| **Rocket** | High | Good | Good | Ergonomic but slower |

#### Rust Source Runtime Challenge

**Problem:** IReader sources are written in Kotlin/JS. Running them in Rust requires:

1. **Option A: Reimplement Source API in Rust**
   - Write Rust equivalents of `HttpSource`, `ParsedHttpSource`, etc.
   - Port IReader's Kotlin DSL to Rust
   - **Effort:** High (6-12 months)
   - **Benefit:** Native performance

2. **Option B: Embed JavaScript Runtime**
   - Use `deno_core` or `rquickjs` to run JS sources
   - Bridge Rust HTTP client to JS
   - **Effort:** Medium (2-3 months)
   - **Benefit:** Compatible with IReader JS sources

3. **Option C: Hybrid Architecture**
   - Rust backend for API, caching, downloads
   - Embedded Kotlin/JVM for source runtime
   - **Effort:** Medium (1-2 months)
   - **Benefit:** Full compatibility

#### Recommended Rust Stack

```rust
// Cargo.toml dependencies
[dependencies]
axum = "0.7"           // Web framework
tokio = { version = "1", features = ["full"] }  // Async runtime
sqlx = { version = "0.7", features = ["sqlite", "postgres"] }  // Database
reqwest = "0.11"       // HTTP client
serde = { version = "1", features = ["derive"] }  // Serialization
deno_core = "0.240"    // JS runtime (for IReader sources)
tower-http = "0.5"     // Middleware
```

#### Rust Source Loading Strategy

```rust
// Source trait in Rust
#[async_trait]
pub trait Source: Send + Sync {
    fn id(&self) -> i64;
    fn name(&self) -> &str;
    fn lang(&self) -> &str;
    
    async fn get_popular_manga(&self, page: i32) -> Result<MangaPage>;
    async fn search_manga(&self, query: &str, page: i32) -> Result<MangaPage>;
    async fn get_manga_details(&self, manga: &Manga) -> Result<Manga>;
    async fn get_chapter_list(&self, manga: &Manga) -> Result<Vec<Chapter>>;
    async fn get_page_list(&self, chapter: &Chapter) -> Result<Vec<Page>>;
}

// JS source wrapper
pub struct JsSource {
    runtime: JsRuntime,
    source_id: String,
}

#[async_trait]
impl Source for JsSource {
    async fn get_popular_manga(&self, page: i32) -> Result<MangaPage> {
        // Call JS function from Rust
        self.runtime.call("getPopularManga", &[page.into()]).await
    }
    // ... other methods
}
```

#### Verdict on Rust

| Factor | Kotlin (Ktor) | Rust (Axum) | Winner |
|--------|---------------|-------------|--------|
| **Code Reuse** | ✅ Full IReader reuse | ❌ Must reimplement | Kotlin |
| **Performance** | ✅ Good | ✅ Excellent | Rust |
| **Memory** | ⚠️ 100-500 MB | ✅ 10-50 MB | Rust |
| **Source Compatibility** | ✅ Native | ⚠️ Needs bridge | Kotlin |
| **Development Speed** | ✅ Fast | ⚠️ Medium | Kotlin |
| **Deployment** | ⚠️ JVM required | ✅ Single binary | Rust |
| **Debugging** | ✅ Excellent tools | ✅ Good tools | Tie |
| **Hiring/Contributors** | ⚠️ Smaller pool | ✅ Growing pool | Rust |

**Recommendation:** If maximum IReader code reuse is priority → **Kotlin**. If maximum performance on low-end hardware is priority → **Rust with deno_core for JS sources**.

---

## 15. Source Loading Strategy

### Understanding IReader's Source System

IReader has a sophisticated multi-layer source loading system:

#### Source Types

1. **Kotlin DSL Sources** (`.kt` files)
   - Compiled to JVM bytecode
   - Use `NovelSourceBuilder` DSL
   - Fastest execution
   - Example: `class MySource(deps: Dependencies) : ParsedHttpSource(deps)`

2. **JavaScript Sources** (`.js` files)
   - Executed via JS engine (J2V8 on Android, GraalVM on Desktop)
   - Use `registerSource()` function
   - Slower but easier to write
   - Example: `registerSource("my-source", (deps) => new MySource(deps))`

3. **Engine Plugins** (`.iplugin` files)
   - Native libraries (J2V8, GraalVM, Piper TTS)
   - Loaded via custom ClassLoader
   - Required for JS source execution

#### IReader's Loading Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                    IReader Source Loading Pipeline               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. DISCOVER                                                    │
│     ├── Scan plugins directory for .iplugin files               │
│     ├── Scan sources directory for .js files                    │
│     └── Scan compiled sources (Kotlin)                          │
│                                                                 │
│  2. LOAD ENGINE PLUGINS                                         │
│     ├── Extract native libraries                                │
│     ├── Create custom ClassLoader                               │
│     └── Initialize JS engine (J2V8/GraalVM)                     │
│                                                                 │
│  3. LOAD SOURCES                                                │
│     ├── Priority sources first (sequential)                     │
│     ├── Remaining sources (parallel, max 4 concurrent)          │
│     └── Create stub sources for instant UI display              │
│                                                                 │
│  4. INITIALIZE                                                  │
│     ├── Validate source metadata                                │
│     ├── Register with SourceBridge                              │
│     └── Replace stubs with actual sources                       │
│                                                                 │
│  5. HOT RELOAD (Development)                                    │
│     ├── Watch file system for changes                           │
│     ├── Preserve source state                                   │
│     └── Reload without restart                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### WebReader Source Loading Strategy

#### Phase 1: Source Discovery

```kotlin
// Backend source scanner
class SourceScanner(
    private val pluginsDir: Path,
    private val sourcesDir: Path
) {
    suspend fun discoverSources(): List<SourceDescriptor> {
        val sources = mutableListOf<SourceDescriptor>()
        
        // Scan for Kotlin/Java sources (compiled JARs)
        sources.addAll(scanForCompiledSources(pluginsDir))
        
        // Scan for JavaScript sources
        sources.addAll(scanForJsSources(sourcesDir))
        
        // Scan for Suwayomi extensions (JARs)
        sources.addAll(scanForSuwayomiExtensions(pluginsDir))
        
        return sources
    }
}
```

#### Phase 2: Source Runtime

```kotlin
// Unified source interface
sealed class SourceRuntime {
    // IReader Kotlin source - direct instantiation
    data class KotlinSource(val source: CatalogSource) : SourceRuntime()
    
    // IReader JS source - via embedded JS engine
    data class JsSource(
        val engine: JsEngine,
        val sourceId: String
    ) : SourceRuntime()
    
    // Suwayomi source - via JVM interop or REST proxy
    data class SuwayomiSource(
        val extension: SuwayomiExtension,
        val proxyUrl: String? = null
    ) : SourceRuntime()
}
```

#### Phase 3: Loading Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                  WebReader Source Loading Pipeline               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  SERVER STARTUP                                                 │
│     │                                                           │
│     ▼                                                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 1. Initialize Database                                    │   │
│  │    - Create tables if not exist                           │   │
│  │    - Run migrations                                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│     │                                                           │
│     ▼                                                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 2. Load Engine Plugins                                    │   │
│  │    - Scan for .iplugin files                              │   │
│  │    - Initialize JS engine (GraalVM recommended)           │   │
│  │    - Initialize Suwayomi bridge if needed                 │   │
│  └─────────────────────────────────────────────────────────┘   │
│     │                                                           │
│     ▼                                                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 3. Discover Sources                                       │   │
│  │    - Scan configured directories                          │   │
│  │    - Read metadata from source files                      │   │
│  │    - Build source registry                                │   │
│  └─────────────────────────────────────────────────────────┘   │
│     │                                                           │
│     ▼                                                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 4. Load Sources (Priority Order)                          │   │
│  │    a. User's favorite sources (sequential)                │   │
│  │    b. Recently used sources (sequential)                  │   │
│  │    c. Remaining sources (parallel, configurable)          │   │
│  └─────────────────────────────────────────────────────────┘   │
│     │                                                           │
│     ▼                                                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 5. Register with API                                      │   │
│  │    - Expose via REST endpoints                            │   │
│  │    - Update health status                                 │   │
│  │    - Enable source in UI                                  │   │
│  └─────────────────────────────────────────────────────────┘   │
│     │                                                           │
│     ▼                                                           │
│  READY                                                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### Phase 4: Source API Endpoints

```kotlin
// Ktor routes
fun Application.sourceRoutes() {
    route("/api/v1/sources") {
        // List all sources
        get {
            val sources = sourceManager.getAllSources()
            call.respond(sources.map { it.toDto() })
        }
        
        // Get source details
        get("/{id}") {
            val id = call.parameters["id"]!!.toLong()
            val source = sourceManager.getSource(id)
            call.respond(source.toDto())
        }
        
        // Search across sources
        get("/search") {
            val query = call.request.queryParameters["q"]!!
            val sourceIds = call.request.queryParameters["sources"]?.split(",")
            val results = sourceManager.search(query, sourceIds)
            call.respond(results)
        }
        
        // Get popular books from source
        get("/{id}/popular") {
            val id = call.parameters["id"]!!.toLong()
            val page = call.request.queryParameters["page"]?.toInt() ?: 1
            val books = sourceManager.getPopular(id, page)
            call.respond(books)
        }
        
        // Get book details
        get("/{id}/books/{bookId}") {
            val sourceId = call.parameters["id"]!!.toLong()
            val bookId = call.parameters["bookId"]!!
            val book = sourceManager.getBookDetails(sourceId, bookId)
            call.respond(book)
        }
        
        // Get chapters
        get("/{id}/books/{bookId}/chapters") {
            val sourceId = call.parameters["id"]!!.toLong()
            val bookId = call.parameters["bookId"]!!
            val chapters = sourceManager.getChapters(sourceId, bookId)
            call.respond(chapters)
        }
        
        // Get chapter content
        get("/{id}/chapters/{chapterId}/content") {
            val sourceId = call.parameters["id"]!!.toLong()
            val chapterId = call.parameters["chapterId"]!!
            val content = sourceManager.getChapterContent(sourceId, chapterId)
            call.respond(content)
        }
    }
}
```

#### Phase 5: Source Health Monitoring

```kotlin
// Background health checker
class SourceHealthChecker(
    private val sourceManager: SourceManager,
    private val coroutineScope: CoroutineScope
) {
    fun startMonitoring() {
        coroutineScope.launch {
            while (isActive) {
                sourceManager.getAllSources().forEach { source ->
                    launch {
                        val health = checkSourceHealth(source)
                        sourceManager.updateHealth(source.id, health)
                    }
                }
                delay(5.minutes) // Check every 5 minutes
            }
        }
    }
    
    private suspend fun checkSourceHealth(source: Source): SourceHealth {
        return try {
            val start = System.currentTimeMillis()
            source.getPopularManga(1) // Test with popular endpoint
            val latency = System.currentTimeMillis() - start
            
            SourceHealth(
                status = if (latency < 3000) HealthStatus.HEALTHY else HealthStatus.SLOW,
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
}
```

### Suwayomi Source Bridge

#### Option A: REST Proxy (Recommended for MVP)

```kotlin
// Suwayomi proxy service
class SuwayomiProxy(
    private val suwayomiBaseUrl: String = "http://localhost:4567",
    private val httpClient: HttpClient
) {
    suspend fun getSources(): List<SuwayomiSource> {
        return httpClient.get("$suwayomiBaseUrl/api/v1/source/list")
            .body()
    }
    
    suspend fun search(sourceId: Long, query: String, page: Int): SearchResults {
        return httpClient.get("$suwayomiBaseUrl/api/v1/source/$sourceId/search") {
            parameter("query", query)
            parameter("page", page)
        }.body()
    }
    
    // ... other endpoints
}
```

#### Option B: JVM Interop (Production)

```kotlin
// Load Suwayomi extension JARs directly
class SuwayomiExtensionLoader(
    private val extensionsDir: Path
) {
    private val classLoader: URLClassLoader
    
    fun loadExtension(jarPath: Path): SuwayomiExtension {
        val urls = arrayOf(jarPath.toUri().toURL())
        classLoader = URLClassLoader(urls, this::class.java.classLoader)
        
        // Load extension class
        val extensionClass = classLoader.loadClass("eu.kanade.tachiyomi.extension.Extension")
        val instance = extensionClass.getDeclaredConstructor().newInstance()
        
        return SuwayomiExtension(instance, classLoader)
    }
}
```

---

## 16. Final Recommendation

### Recommended Stack: Ktor + React/Next.js

**Rationale:**
1. **Maximum code reuse** from IReader (source-api, domain models, database schema)
2. **Best UI ecosystem** for "amazing UI" goal
3. **Excellent performance** with Ktor coroutines
4. **Easy debugging** with mature tooling
5. **Source compatibility** through direct embedding

### Alternative: Rust + React/Next.js

**Consider if:**
- Targeting low-end hardware (Raspberry Pi, cheap VPS)
- Memory efficiency is critical
- Willing to invest in source API reimplementation

### Server Hosting Recommendation

| Scenario | Recommended Setup |
|----------|-------------------|
| **Local machine** | Docker Compose with 512 MB RAM limit |
| **Home server** | Docker with 1-2 GB RAM |
| **VPS (budget)** | Rust backend with 512 MB RAM |
| **VPS (standard)** | Ktor backend with 1 GB RAM |
| **Cloud** | Kubernetes with auto-scaling |

---

*This plan is a living document and will be updated as the project evolves.*
