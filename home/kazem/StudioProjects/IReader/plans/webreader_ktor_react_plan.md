# WebReader - Ktor + React Implementation Plan

**Date:** 2026-06-04
**Architecture:** Ktor Backend (Single JVM) + React Frontend
**Target:** Easy development, maximum code reuse from IReader

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                    WebReader Ktor + React Architecture               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Web Browser (Client)                       │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │           React 18 + Next.js 14 (TypeScript)           │  │   │
│  │  │  - Beautiful library with Framer Motion animations    │  │   │
│  │  │  - Canvas-based reader with smooth scrolling          │  │   │
│  │  │  - PWA with offline support                           │  │   │
│  │  │  - Tailwind CSS + Radix UI components                 │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │ HTTP / WebSocket                       │
│  ┌──────────────────────────▼───────────────────────────────────┐   │
│  │                  Ktor Server (Single JVM)                     │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              REST API Layer (Ktor Routing)              │  │   │
│  │  │  - /api/v1/sources/*                                   │  │   │
│  │  │  - /api/v1/books/*                                     │  │   │
│  │  │  - /api/v1/chapters/*                                  │  │   │
│  │  │  - /api/v1/users/*                                     │  │   │
│  │  │  - /ws (WebSocket for real-time updates)               │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Service Layer (Kotlin Coroutines)          │  │   │
│  │  │  - Authentication (JWT)                                │  │   │
│  │  │  - Rate limiting                                       │  │   │
│  │  │  - User management                                     │  │   │
│  │  │  - Cache management                                    │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Source Runtime (Same JVM)                  │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  IReader source-api (Direct Integration)         │  │  │   │
│  │  │  │  - HttpSource, ParsedHttpSource                  │  │  │   │
│  │  │  │  - CatalogSource interface                       │  │  │   │
│  │  │  │  - JS Source Runtime (GraalVM)                   │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Loaded Sources (Isolated ClassLoaders)          │  │  │   │
│  │  │  │  - Source A (ClassLoader A)                      │  │  │   │
│  │  │  │  - Source B (ClassLoader B)                      │  │  │   │
│  │  │  │  - Source C (ClassLoader C)                      │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │              Database Layer (SQLDelight)                │  │   │
│  │  │  - SQLite (default) or PostgreSQL                      │  │   │
│  │  │  - Connection pooling (HikariCP)                       │  │   │
│  │  │  - Migrations                                          │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

### Backend: Ktor + Kotlin

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Web Framework** | Ktor | 2.3 | HTTP server, routing |
| **Language** | Kotlin | 1.9 | Backend language |
| **Async** | Kotlin Coroutines | 1.7 | Non-blocking I/O |
| **Database** | SQLDelight | 2.0 | Type-safe SQL |
| **Connection Pool** | HikariCP | 5.0 | Database pooling |
| **Serialization** | kotlinx.serialization | 1.6 | JSON serialization |
| **Authentication** | Ktor Auth | 2.3 | JWT authentication |
| **HTTP Client** | Ktor Client | 2.3 | Fetch from sources |
| **JS Engine** | GraalVM | 21.0 | Run JS sources |
| **Logging** | kotlinx-logging | 5.1 | Structured logging |

### Frontend: React + Next.js

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Framework** | React 18 + Next.js 14 | UI framework |
| **Language** | TypeScript 5 | Type safety |
| **Styling** | Tailwind CSS 3 | Utility-first CSS |
| **Components** | Radix UI | Accessible primitives |
| **Animations** | Framer Motion | Smooth animations |
| **State** | Zustand | Lightweight state management |
| **Data Fetching** | TanStack Query | Server state management |
| **Forms** | React Hook Form + Zod | Form validation |

---

## 3. Project Structure

```
webreader-ktor/
├── build.gradle.kts                    # Gradle build file
├── settings.gradle.kts
├── gradle.properties
│
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── Application.kt          # Main entry point
│   │   │   │
│   │   │   ├── api/                    # REST API routes
│   │   │   │   ├── Routing.kt
│   │   │   │   ├── SourcesApi.kt
│   │   │   │   ├── BooksApi.kt
│   │   │   │   ├── ChaptersApi.kt
│   │   │   │   ├── UsersApi.kt
│   │   │   │   └── WebSocket.kt
│   │   │   │
│   │   │   ├── services/               # Business logic
│   │   │   │   ├── AuthService.kt
│   │   │   │   ├── CacheService.kt
│   │   │   │   ├── RateLimiter.kt
│   │   │   │   └── UserService.kt
│   │   │   │
│   │   │   ├── source/                 # Source runtime
│   │   │   │   ├── SourceManager.kt
│   │   │   │   ├── SourceLoader.kt
│   │   │   │   ├── SourceCache.kt
│   │   │   │   └── JsEngine.kt
│   │   │   │
│   │   │   ├── db/                     # Database
│   │   │   │   ├── DatabaseFactory.kt
│   │   │   │   ├── BooksTable.kt
│   │   │   │   ├── ChaptersTable.kt
│   │   │   │   ├── UsersTable.kt
│   │   │   │   └── Queries.kt
│   │   │   │
│   │   │   ├── models/                 # Domain models
│   │   │   │   ├── Source.kt
│   │   │   │   ├── Book.kt
│   │   │   │   ├── Chapter.kt
│   │   │   │   └── User.kt
│   │   │   │
│   │   │   └── config/                 # Configuration
│   │   │       ├── AppConfig.kt
│   │   │       └── Settings.kt
│   │   │
│   │   └── resources/
│   │       ├── application.conf        # Ktor config
│   │       ├── logback.xml             # Logging config
│   │       └── static/                 # Frontend static files
│   │
│   └── test/                           # Tests
│
├── frontend/                           # React frontend
│   ├── package.json
│   ├── next.config.js
│   ├── tailwind.config.js
│   │
│   ├── src/
│   │   ├── app/                        # Next.js app router
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx
│   │   │   ├── sources/
│   │   │   ├── library/
│   │   │   ├── reader/
│   │   │   └── settings/
│   │   │
│   │   ├── components/                 # Reusable components
│   │   │   ├── ui/                     # Base UI components
│   │   │   ├── library/                # Library components
│   │   │   ├── reader/                 # Reader components
│   │   │   └── sources/                # Source components
│   │   │
│   │   ├── hooks/                      # Custom hooks
│   │   │   ├── useSources.ts
│   │   │   ├── useBooks.ts
│   │   │   └── useReader.ts
│   │   │
│   │   ├── lib/                        # Utilities
│   │   │   ├── api.ts                  # API client
│   │   │   ├── auth.ts                 # Auth utilities
│   │   │   └── utils.ts
│   │   │
│   │   ├── stores/                     # Zustand stores
│   │   │   ├── authStore.ts
│   │   │   ├── libraryStore.ts
│   │   │   └── readerStore.ts
│   │   │
│   │   └── types/                      # TypeScript types
│   │       ├── source.ts
│   │       ├── book.ts
│   │       └── chapter.ts
│   │
│   └── public/                         # Static assets
│
├── sources/                            # Source JARs directory
│   ├── ireader/
│   └── suwayomi/
│
├── migrations/                         # Database migrations
│   ├── 001_initial.sql
│   └── 002_add_users.sql
│
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## 4. Implementation Details

### 4.1 Main Application

```kotlin
// src/main/kotlin/Application.kt
package ireader.webreader

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import ireader.webreader.api.*
import ireader.webreader.config.*
import ireader.webreader.db.*
import ireader.webreader.services.*
import ireader.webreader.source.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Load configuration
    val config = AppConfig.load()
    
    // Initialize database
    val database = DatabaseFactory.init(config.database)
    
    // Initialize source manager
    val sourceManager = SourceManager(
        sourcesDir = config.sourcesDir,
        httpClient = createHttpClient(),
        preferences = createPreferences()
    )
    sourceManager.loadAllSources()
    
    // Initialize services
    val authService = AuthService(database, config.jwt)
    val cacheService = CacheService(config.cacheSize)
    val rateLimiter = RateLimiter(config.rateLimit)
    
    // Install plugins
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(CORS) {
        anyHost()
        allowHeader("Authorization")
        allowHeader("Content-Type")
    }
    
    install(DefaultHeaders) {
        header("X-Engine", "WebReader")
    }
    
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    
    // Configure routing
    routing {
        // Static files (frontend)
        static("/") {
            resources("static")
            defaultResource("static/index.html")
        }
        
        // API routes
        route("/api/v1") {
            sourcesApi(sourceManager, cacheService, rateLimiter)
            booksApi(database, cacheService, rateLimiter)
            chaptersApi(database, cacheService, rateLimiter)
            usersApi(authService, database)
            webSocketApi()
        }
        
        // Health check
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}
```

### 4.2 Source Manager

```kotlin
// src/main/kotlin/source/SourceManager.kt
package ireader.webreader.source

import ireader.core.source.*
import ireader.core.source.model.*
import io.ktor.client.*
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.*

class SourceManager(
    private val sourcesDir: Path,
    private val httpClient: HttpClient,
    private val preferences: PreferenceStore
) {
    private val sources = ConcurrentHashMap<Long, CatalogSource>()
    private val classLoaders = ConcurrentHashMap<Long, URLClassLoader>()
    
    private val dependencies = Dependencies(httpClient, preferences)
    
    fun loadAllSources() {
        if (!sourcesDir.exists()) {
            sourcesDir.createDirectories()
        }
        
        val jarFiles = sourcesDir.listDirectoryEntries("*.jar")
        
        jarFiles.forEach { jarPath ->
            try {
                loadSource(jarPath)
            } catch (e: Exception) {
                println("Failed to load source from ${jarPath.fileName}: ${e.message}")
            }
        }
        
        println("Loaded ${sources.size} sources")
    }
    
    private fun loadSource(jarPath: Path) {
        val jarUrl = jarPath.toUri().toURL()
        val classLoader = URLClassLoader(arrayOf(jarUrl), this::class.java.classLoader)
        
        // Read manifest to find main class
        val manifest = classLoader.getResourceAsStream("source-manifest.json")
            ?.bufferedReader()?.readText()
            ?: throw IllegalArgumentException("No source manifest found")
        
        val sourceInfo = Json.decodeFromString<SourceManifest>(manifest)
        
        // Load source class
        val sourceClass = classLoader.loadClass(sourceInfo.mainClass)
        val source = sourceClass.getConstructor(Dependencies::class.java)
            .newInstance(dependencies) as CatalogSource
        
        sources[source.id] = source
        classLoaders[source.id] = classLoader
        
        println("Loaded source: ${source.name} (${source.id})")
    }
    
    fun getSource(id: Long): CatalogSource? = sources[id]
    
    fun getAllSources(): List<CatalogSource> = sources.values.toList()
    
    suspend fun search(sourceId: Long, query: String, page: Int): MangasPage {
        val source = sources[sourceId] ?: throw SourceNotFoundException(sourceId)
        return source.searchMangas(MangasPageRequest(query, page))
    }
    
    suspend fun getPopular(sourceId: Long, page: Int): MangasPage {
        val source = sources[sourceId] ?: throw SourceNotFoundException(sourceId)
        return source.getPopularManga(page)
    }
    
    suspend fun getBookDetails(sourceId: Long, book: Book): Book {
        val source = sources[sourceId] ?: throw SourceNotFoundException(sourceId)
        return source.getBookDetails(book)
    }
    
    suspend fun getChapters(sourceId: Long, book: Book): List<Chapter> {
        val source = sources[sourceId] ?: throw SourceNotFoundException(sourceId)
        return source.getChapterList(book)
    }
    
    suspend fun getChapterContent(sourceId: Long, chapter: Chapter): List<Page> {
        val source = sources[sourceId] ?: throw SourceNotFoundException(sourceId)
        return source.getPageList(chapter)
    }
}

data class SourceManifest(
    val id: Long,
    val name: String,
    val mainClass: String,
    val version: String,
    val language: String
)

class SourceNotFoundException(sourceId: Long) : Exception("Source not found: $sourceId")
```

### 4.3 API Routes

```kotlin
// src/main/kotlin/api/SourcesApi.kt
package ireader.webreader.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ireader.webreader.source.SourceManager
import ireader.webreader.services.CacheService
import ireader.webreader.services.RateLimiter

fun Route.sourcesApi(
    sourceManager: SourceManager,
    cacheService: CacheService,
    rateLimiter: RateLimiter
) {
    route("/sources") {
        // List all sources
        get {
            val sources = sourceManager.getAllSources().map { source ->
                SourceDto(
                    id = source.id,
                    name = source.name,
                    lang = source.lang,
                    supportsLatest = source.supportsLatest
                )
            }
            call.respond(sources)
        }
        
        // Get source details
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid source ID")
            
            val source = sourceManager.getSource(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Source not found")
            
            call.respond(SourceDto(
                id = source.id,
                name = source.name,
                lang = source.lang,
                supportsLatest = source.supportsLatest
            ))
        }
        
        // Search source
        get("/{id}/search") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid source ID")
            val query = call.request.queryParameters["q"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Query required")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            
            // Rate limit check
            val userId = call.principal<UserPrincipal>()?.userId ?: 0L
            if (!rateLimiter.acquire(userId)) {
                return@get call.respond(HttpStatusCode.TooManyRequests, "Rate limit exceeded")
            }
            
            // Check cache
            val cacheKey = "search:$id:$query:$page"
            cacheService.get(cacheKey)?.let { cached ->
                return@get call.respond(Json.decodeFromString<MangasPage>(cached))
            }
            
            // Search source
            val result = sourceManager.search(id, query, page)
            
            // Cache result
            cacheService.set(cacheKey, Json.encodeToString(result), 300) // 5 minutes
            
            call.respond(result)
        }
        
        // Get popular books
        get("/{id}/popular") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid source ID")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            
            val userId = call.principal<UserPrincipal>()?.userId ?: 0L
            if (!rateLimiter.acquire(userId)) {
                return@get call.respond(HttpStatusCode.TooManyRequests, "Rate limit exceeded")
            }
            
            val cacheKey = "popular:$id:$page"
            cacheService.get(cacheKey)?.let { cached ->
                return@get call.respond(Json.decodeFromString<MangasPage>(cached))
            }
            
            val result = sourceManager.getPopular(id, page)
            cacheService.set(cacheKey, Json.encodeToString(result), 300)
            
            call.respond(result)
        }
    }
}

data class SourceDto(
    val id: Long,
    val name: String,
    val lang: String,
    val supportsLatest: Boolean
)
```

### 4.4 Database Layer

```kotlin
// src/main/kotlin/db/DatabaseFactory.kt
package ireader.webreader.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: DatabaseConfig): Database {
        val hikariConfig = HikariConfig().apply {
            driverClassName = when (config) {
                is DatabaseConfig.SQLite -> "org.sqlite.JDBC"
                is DatabaseConfig.PostgreSQL -> "org.postgresql.Driver"
            }
            jdbcUrl = config.url
            maximumPoolSize = config.poolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
            validate()
        }
        
        val dataSource = HikariDataSource(hikariConfig)
        val database = Database.connect(dataSource)
        
        // Create tables
        transaction(database) {
            SchemaUtils.create(Users, Books, Chapters, Categories, BookCategories)
        }
        
        return database
    }
}

sealed class DatabaseConfig {
    abstract val url: String
    abstract val poolSize: Int
    
    data class SQLite(
        val path: String,
        override val poolSize: Int = 5
    ) : DatabaseConfig() {
        override val url: String = "jdbc:sqlite:$path"
    }
    
    data class PostgreSQL(
        val host: String,
        val port: Int,
        val database: String,
        val username: String,
        val password: String,
        override val poolSize: Int = 10
    ) : DatabaseConfig() {
        override val url: String = "jdbc:postgresql://$host:$port/$database"
    }
}

// src/main/kotlin/db/BooksTable.kt
package ireader.webreader.db

import org.jetbrains.exposed.sql.Table

object Books : Table("books") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val sourceId = long("source_id")
    val title = varchar("title", 500)
    val author = varchar("author", 500).nullable()
    val description = text("description").nullable()
    val coverUrl = varchar("cover_url", 1000).nullable()
    val url = varchar("url", 1000)
    val status = integer("status").default(0)
    val createdAt = long("created_at")
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        index("idx_books_user", false, userId)
        index("idx_books_source", false, sourceId)
    }
}

object Chapters : Table("chapters") {
    val id = long("id").autoIncrement()
    val bookId = long("book_id")
    val title = varchar("title", 500)
    val url = varchar("url", 1000)
    val chapterNumber = float("chapter_number").default(0f)
    val read = bool("read").default(false)
    val bookmarked = bool("bookmarked").default(false)
    val createdAt = long("created_at")
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        index("idx_chapters_book", false, bookId)
    }
}

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = long("created_at")
    
    override val primaryKey = PrimaryKey(id)
}
```

### 4.5 Cache Service

```kotlin
// src/main/kotlin/services/CacheService.kt
package ireader.webreader.services

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache
import java.util.concurrent.TimeUnit

class CacheService(maxSize: Int = 100) {
    private val cache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(maxSize.toLong())
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build()
    
    fun get(key: String): String? = cache.getIfPresent(key)
    
    fun set(key: String, value: String, ttlSeconds: Long = 300) {
        cache.put(key, value)
    }
    
    fun invalidate(key: String) {
        cache.invalidate(key)
    }
    
    fun invalidateAll() {
        cache.invalidateAll()
    }
}
```

### 4.6 Rate Limiter

```kotlin
// src/main/kotlin/services/RateLimiter.kt
package ireader.webreader.services

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class RateLimiter(
    private val requestsPerMinute: Int = 30
) {
    private val buckets = ConcurrentHashMap<Long, TokenBucket>()
    
    fun acquire(userId: Long): Boolean {
        val bucket = buckets.computeIfAbsent(userId) {
            TokenBucket(requestsPerMinute)
        }
        return bucket.tryAcquire()
    }
    
    fun getRemaining(userId: Long): Int {
        return buckets[userId]?.remaining ?: requestsPerMinute
    }
}

class TokenBucket(private val capacity: Int) {
    private val tokens = AtomicInteger(capacity)
    private var lastRefill = System.currentTimeMillis()
    
    @Synchronized
    fun tryAcquire(): Boolean {
        refill()
        val current = tokens.get()
        if (current > 0) {
            return tokens.compareAndSet(current, current - 1)
        }
        return false
    }
    
    private fun refill() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRefill
        val tokensToAdd = (elapsed / 60000.0 * capacity).toInt()
        
        if (tokensToAdd > 0) {
            tokens.set(minOf(capacity, tokens.get() + tokensToAdd))
            lastRefill = now
        }
    }
    
    val remaining: Int
        get() {
            refill()
            return tokens.get()
        }
}
```

---

## 5. Frontend Implementation

### 5.1 API Client

```typescript
// frontend/src/lib/api.ts
const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

interface Source {
  id: number;
  name: string;
  lang: string;
  supportsLatest: boolean;
}

interface Book {
  id: number;
  title: string;
  author?: string;
  description?: string;
  coverUrl?: string;
  url: string;
}

interface Chapter {
  id: number;
  bookId: number;
  title: string;
  url: string;
  chapterNumber: number;
  read: boolean;
  bookmarked: boolean;
}

interface SearchResult {
  books: Book[];
  hasNextPage: boolean;
}

class ApiClient {
  private token: string | null = null;
  
  setToken(token: string) {
    this.token = token;
    localStorage.setItem('token', token);
  }
  
  getToken(): string | null {
    if (!this.token) {
      this.token = localStorage.getItem('token');
    }
    return this.token;
  }
  
  private async fetch<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const token = this.getToken();
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    };
    
    const res = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });
    
    if (!res.ok) {
      throw new Error(`API error: ${res.status}`);
    }
    
    return res.json();
  }
  
  // Sources
  async getSources(): Promise<Source[]> {
    return this.fetch<Source[]>('/sources');
  }
  
  async searchSource(
    sourceId: number,
    query: string,
    page: number = 1
  ): Promise<SearchResult> {
    return this.fetch<SearchResult>(
      `/sources/${sourceId}/search?q=${encodeURIComponent(query)}&page=${page}`
    );
  }
  
  async getPopular(sourceId: number, page: number = 1): Promise<SearchResult> {
    return this.fetch<SearchResult>(`/sources/${sourceId}/popular?page=${page}`);
  }
  
  // Books
  async getBooks(): Promise<Book[]> {
    return this.fetch<Book[]>('/books');
  }
  
  async addBook(sourceId: number, book: Partial<Book>): Promise<Book> {
    return this.fetch<Book>('/books', {
      method: 'POST',
      body: JSON.stringify({ sourceId, ...book }),
    });
  }
  
  // Chapters
  async getChapters(bookId: number): Promise<Chapter[]> {
    return this.fetch<Chapter[]>(`/books/${bookId}/chapters`);
  }
  
  async getChapterContent(chapterId: number): Promise<string[]> {
    return this.fetch<string[]>(`/chapters/${chapterId}/content`);
  }
  
  // Auth
  async login(username: string, password: string): Promise<{ token: string }> {
    return this.fetch<{ token: string }>('/users/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  }
  
  async register(username: string, password: string): Promise<{ token: string }> {
    return this.fetch<{ token: string }>('/users/register', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  }
}

export const api = new ApiClient();
```

### 5.2 Source List Component

```tsx
// frontend/src/components/sources/SourceList.tsx
'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { motion } from 'framer-motion';
import { SourceCard } from './SourceCard';

export function SourceList() {
  const { data: sources, isLoading, error } = useQuery({
    queryKey: ['sources'],
    queryFn: () => api.getSources(),
  });

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {[...Array(6)].map((_, i) => (
          <div
            key={i}
            className="h-32 bg-gray-200 dark:bg-gray-800 rounded-lg animate-pulse"
          />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center text-red-500">
        Error loading sources: {error.message}
      </div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"
    >
      {sources?.map((source, index) => (
        <motion.div
          key={source.id}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: index * 0.05 }}
        >
          <SourceCard source={source} />
        </motion.div>
      ))}
    </motion.div>
  );
}
```

### 5.3 Reader Component

```tsx
// frontend/src/components/reader/Reader.tsx
'use client';

import { useEffect, useState, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import { api } from '@/lib/api';

interface ReaderProps {
  chapterId: number;
  onNext?: () => void;
  onPrev?: () => void;
}

export function Reader({ chapterId, onNext, onPrev }: ReaderProps) {
  const [currentPage, setCurrentPage] = useState(0);
  const [pages, setPages] = useState<string[]>([]);

  const { data: content } = useQuery({
    queryKey: ['chapterContent', chapterId],
    queryFn: () => api.getChapterContent(chapterId),
  });

  useEffect(() => {
    if (content) {
      setPages(content);
    }
  }, [content]);

  const nextPage = useCallback(() => {
    if (currentPage < pages.length - 1) {
      setCurrentPage(currentPage + 1);
    } else {
      onNext?.();
    }
  }, [currentPage, pages.length, onNext]);

  const prevPage = useCallback(() => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    } else {
      onPrev?.();
    }
  }, [currentPage, onPrev]);

  const handleClick = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const x = e.clientX - rect.left;
    if (x > rect.width / 2) {
      nextPage();
    } else {
      prevPage();
    }
  };

  return (
    <div className="relative w-full h-screen bg-black" onClick={handleClick}>
      <AnimatePresence mode="wait">
        <motion.img
          key={currentPage}
          src={pages[currentPage]}
          alt={`Page ${currentPage + 1}`}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="w-full h-full object-contain"
        />
      </AnimatePresence>
      
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="absolute bottom-4 left-4 right-4 flex justify-between items-center text-white"
      >
        <button
          onClick={(e) => { e.stopPropagation(); prevPage(); }}
          disabled={currentPage === 0 && !onPrev}
          className="px-4 py-2 bg-gray-800/80 rounded disabled:opacity-50"
        >
          Previous
        </button>
        
        <span className="px-4 py-2 bg-gray-800/80 rounded">
          {currentPage + 1} / {pages.length}
        </span>
        
        <button
          onClick={(e) => { e.stopPropagation(); nextPage(); }}
          disabled={currentPage === pages.length - 1 && !onNext}
          className="px-4 py-2 bg-gray-800/80 rounded disabled:opacity-50"
        >
          Next
        </button>
      </motion.div>
    </div>
  );
}
```

---

## 6. Memory Usage Analysis

### Ktor (Single JVM) Memory Breakdown

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Ktor Single JVM Memory Layout                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    JVM Overhead                               │   │
│  │  - JVM Internal Structures: ~50-100 MB                       │   │
│  │  - Metaspace (class metadata): ~30-50 MB                     │   │
│  │  - Thread stacks: ~10-20 MB                                  │   │
│  │  - JIT compiled code cache: ~20-40 MB                        │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Application Code                           │   │
│  │  - Ktor server runtime: ~20-30 MB                            │   │
│  │  - kotlinx.serialization: ~5-10 MB                           │   │
│  │  - Ktor HTTP client: ~10-15 MB                               │   │
│  │  - SQLDelight: ~5-10 MB                                      │   │
│  │  - HikariCP: ~5-10 MB                                        │   │
│  │  - IReader source-api: ~10-20 MB                             │   │
│  │  Total Application: ~55-95 MB                                │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Loaded Sources                             │   │
│  │  - Per source: ~2-5 MB                                       │   │
│  │  - 50 sources: ~150 MB                                       │   │
│  │  - 100 sources: ~300 MB                                      │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Runtime Data                               │   │
│  │  - Database connections: ~10-20 MB                           │   │
│  │  - HTTP client cache: ~20-50 MB                              │   │
│  │  - Response cache: ~50-100 MB                                │   │
│  │  - User sessions: ~1-5 MB                                    │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Total Memory                               │   │
│  │  - Minimal (10 sources, 5 users): ~400-600 MB                │   │
│  │  - Small (50 sources, 20 users): ~768 MB - 1 GB              │   │
│  │  - Medium (100 sources, 50 users): ~1-2 GB                   │   │
│  │  - Large (200 sources, 100 users): ~2-4 GB                   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Optimized JVM Flags

```bash
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

---

## 7. Deployment

### Dockerfile

```dockerfile
# Build stage
FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /app
COPY . .
RUN ./gradlew shadowJar

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy JAR
COPY --from=builder /app/build/libs/webreader-all.jar /app/

# Create directories
RUN mkdir -p /app/data /app/sources

# Environment variables
ENV JAVA_OPTS="-Xms512m -Xmx768m -XX:+UseG1GC"
ENV DATABASE_URL="sqlite:///app/data/webreader.db"
ENV SOURCES_DIR="/app/sources"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/webreader-all.jar"]
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  webreader:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xms512m -Xmx768m -XX:+UseG1GC -XX:MaxMetaspaceSize=192m
      - DATABASE_URL=sqlite:///app/data/webreader.db
      - SOURCES_DIR=/app/sources
      - JWT_SECRET=your-secret-key-change-in-production
      - CACHE_SIZE=100
      - RATE_LIMIT=30
    volumes:
      - ./data:/app/data
      - ./sources:/app/sources:ro
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '2'
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

---

## 8. Comparison: Ktor vs Rust+JVM

| Aspect | Ktor (Single JVM) | Rust + JVM | Winner |
|--------|-------------------|------------|--------|
| **Memory (50 sources)** | 768 MB - 1 GB | 400-600 MB | Rust+JVM |
| **Memory (100 sources)** | 1-2 GB | 600-900 MB | Rust+JVM |
| **Startup Time** | 2-5 seconds | 1-3 seconds | Rust+JVM |
| **Code Reuse** | ✅ Full IReader reuse | ⚠️ Partial | Ktor |
| **Development Speed** | ✅ Fast | ⚠️ Medium | Ktor |
| **Debugging** | ✅ Excellent tools | ⚠️ Complex (FFI) | Ktor |
| **Source Compatibility** | ✅ Native | ✅ Via JNI | Tie |
| **Deployment** | ⚠️ JVM required | ⚠️ JVM + Rust | Tie |
| **Hiring/Contributors** | ⚠️ Kotlin devs | ✅ Rust + Kotlin | Rust+JVM |

---

## 9. Advantages of Ktor Approach

1. **Maximum Code Reuse**
   - Direct use of IReader source-api
   - No FFI/JNI complexity
   - Shared database schema (SQLDelight)

2. **Faster Development**
   - Single language (Kotlin)
   - No cross-language debugging
   - Familiar ecosystem for IReader contributors

3. **Easier Debugging**
   - Single JVM process
   - Standard Kotlin/Java debugging tools
   - No FFI boundary issues

4. **Source Compatibility**
   - Native IReader source support
   - JS sources via GraalVM (same JVM)
   - Suwayomi bridge via ClassLoader

5. **Mature Ecosystem**
   - Ktor is battle-tested
   - Excellent documentation
   - Large community

---

## 10. Implementation Phases

### Phase 1: Foundation
- [ ] Ktor server setup
- [ ] Basic REST API
- [ ] SQLite database with SQLDelight
- [ ] JWT authentication
- [ ] React frontend setup

### Phase 2: Source Integration
- [ ] SourceManager implementation
- [ ] Source loading from JARs
- [ ] Source method calls
- [ ] Error handling

### Phase 3: Source Support
- [ ] IReader Kotlin DSL sources
- [ ] IReader JS sources (via GraalVM)
- [ ] Suwayomi source bridge

### Phase 4: Reader
- [ ] Canvas-based reader
- [ ] Touch/click navigation
- [ ] Reading progress tracking
- [ ] Bookmarks

### Phase 5: Multi-User
- [ ] User registration/login
- [ ] Per-user libraries
- [ ] Rate limiting
- [ ] Session management

### Phase 6: Performance
- [ ] Response caching (Caffeine)
- [ ] Connection pooling (HikariCP)
- [ ] Lazy source loading
- [ ] Image optimization

---

*This plan provides a complete implementation guide for the Ktor + React architecture.*