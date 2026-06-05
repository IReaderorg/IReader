# Server Module - DI and API Handlers Implementation Plan

**Date:** 2026-06-05

---

## Step 1: Create ServerModule (DI)

### File: `server/src/main/kotlin/ireader/server/di/ServerModule.kt`

```kotlin
package ireader.server.di

import ireader.core.db.IReaderDatabase
import ireader.data.local.DatabaseHandler
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.server.config.ServerConfig
import ireader.server.api.SourcesApi
import ireader.server.api.BooksApi
import ireader.server.api.ChaptersApi
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Server dependency injection module.
 * 
 * Creates and wires all dependencies for the server.
 * Reuses existing IReader domain layer components.
 */
class ServerModule(config: ServerConfig) {
    
    // HTTP client for source fetching
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    // Database
    private val databaseHandler = DatabaseHandler(config.databaseUrl)
    val database: IReaderDatabase = databaseHandler.createDatabase()
    
    // Repositories (from :data module)
    val bookRepository: BookRepository = databaseHandler.bookRepository
    val chapterRepository: ChapterRepository = databaseHandler.chapterRepository
    
    // Catalog store (from :domain module)
    // Note: CatalogStore requires platform-specific loader
    // For server, we create a simplified version
    val catalogStore: CatalogStore = createCatalogStore()
    
    // Use cases (from :domain module)
    val getInstalledCatalog = GetInstalledCatalog(catalogStore)
    val getLocalCatalogs = GetLocalCatalogs(catalogStore, bookRepository)
    
    // API handlers
    val sourcesApi = SourcesApi(getInstalledCatalog, getLocalCatalogs)
    val booksApi = BooksApi(bookRepository)
    val chaptersApi = ChaptersApi(chapterRepository)
    
    private fun createCatalogStore(): CatalogStore {
        // TODO: Create CatalogStore with server-specific loader
        // For now, return a placeholder
        TODO("Implement server-specific CatalogStore")
    }
}
```

---

## Step 2: Create SourcesApi

### File: `server/src/main/kotlin/ireader/server/api/SourcesApi.kt`

```kotlin
package ireader.server.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.GetLocalCatalogs
import ireader.domain.models.entities.CatalogInstalled
import ireader.core.source.model.MangasPageInfo

/**
 * API handler for source-related endpoints.
 * 
 * Reuses existing domain layer use cases:
 * - GetInstalledCatalog: Get installed sources
 * - GetLocalCatalogs: Get local catalogs with sorting
 */
class SourcesApi(
    private val getInstalledCatalog: GetInstalledCatalog,
    private val getLocalCatalogs: GetLocalCatalogs,
) {
    fun registerRoutes(routing: Routing) {
        routing.route("/api/v1/sources") {
            // List all installed sources
            get {
                val sources = getInstalledCatalog.get()
                call.respond(sources.map { it.toDto() })
            }
            
            // Get source details
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid source ID")
                    return@get
                }
                
                val source = getLocalCatalogs.find(id)
                if (source == null) {
                    call.respond(HttpStatusCode.NotFound, "Source not found")
                    return@get
                }
                
                call.respond(source.toDto())
            }
        }
    }
}

// Extension functions to convert domain models to API DTOs
private fun CatalogInstalled.toDto(): SourceDto {
    return SourceDto(
        id = sourceId,
        name = name,
        lang = lang,
        supportsLatest = supportsLatest,
        iconUrl = iconUrl
    )
}

data class SourceDto(
    val id: Long,
    val name: String,
    val lang: String,
    val supportsLatest: Boolean,
    val iconUrl: String? = null
)
```

---

## Step 3: Create BooksApi

### File: `server/src/main/kotlin/ireader/server/api/BooksApi.kt`

```kotlin
package ireader.server.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import ireader.domain.data.repository.BookRepository
import ireader.core.source.model.MangaInfo

/**
 * API handler for book-related endpoints.
 * 
 * Reuses existing BookRepository from :data module.
 */
class BooksApi(
    private val bookRepository: BookRepository,
) {
    fun registerRoutes(routing: Routing) {
        routing.route("/api/v1/books") {
            // Get user's library
            get {
                val books = bookRepository.findAll()
                call.respond(books)
            }
            
            // Get book details
            get("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid book ID")
                    return@get
                }
                
                val book = bookRepository.findById(id)
                if (book == null) {
                    call.respond(HttpStatusCode.NotFound, "Book not found")
                    return@get
                }
                
                call.respond(book)
            }
        }
    }
}
```

---

## Step 4: Create ChaptersApi

### File: `server/src/main/kotlin/ireader/server/api/ChaptersApi.kt`

```kotlin
package ireader.server.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import ireader.domain.data.repository.ChapterRepository
import ireader.core.source.model.ChapterInfo

/**
 * API handler for chapter-related endpoints.
 * 
 * Reuses existing ChapterRepository from :data module.
 */
class ChaptersApi(
    private val chapterRepository: ChapterRepository,
) {
    fun registerRoutes(routing: Routing) {
        routing.route("/api/v1/chapters") {
            // Get chapters for a book
            get("/book/{bookId}") {
                val bookId = call.parameters["bookId"]?.toLongOrNull()
                if (bookId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid book ID")
                    return@get
                }
                
                val chapters = chapterRepository.findAllForBook(bookId)
                call.respond(chapters)
            }
            
            // Get chapter pages
            get("/{id}/pages") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid chapter ID")
                    return@get
                }
                
                // TODO: Get pages from source
                call.respond(emptyList<String>())
            }
            
            // Mark chapter as read
            put("/{id}/read") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid chapter ID")
                    return@put
                }
                
                chapterRepository.markAsRead(id)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
```

---

## Step 5: Update ServerMain.kt

```kotlin
fun Application.module(config: ServerConfig) {
    val logger = LoggerFactory.getLogger("Server")
    
    // Initialize DI module
    val serverModule = ServerModule(config)
    
    // Install plugins
    install(ContentNegotiation) { ... }
    install(CORS) { ... }
    install(Compression) { ... }
    install(DefaultHeaders) { ... }
    install(StatusPages) { ... }
    
    // Configure routing
    routing {
        get("/health") {
            call.respond(HealthResponse(status = "ok"))
        }
        
        // Register API routes
        serverModule.sourcesApi.registerRoutes(this)
        serverModule.booksApi.registerRoutes(this)
        serverModule.chaptersApi.registerRoutes(this)
        
        // Static files
        staticFiles("/", ...) { ... }
    }
}
```

---

## Implementation Order

1. ✅ Plan the DI module
2. ✅ Plan the API handlers
3. ⬜ Implement ServerModule.kt
4. ⬜ Implement SourcesApi.kt
5. ⬜ Implement BooksApi.kt
6. ⬜ Implement ChaptersApi.kt
7. ⬜ Update ServerMain.kt
8. ⬜ Build and test

---

*This plan maximizes reuse of existing IReader domain layer code.*