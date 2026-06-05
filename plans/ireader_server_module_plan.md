# IReader Server Module - Implementation Plan

**Date:** 2026-06-04
**Goal:** Add a new `:server` module to IReader that runs a Ktor server with React UI
**Use Case:** Run server on PC, connect from mobile browser, maximum code reuse

---

## 1. Vision

```
┌─────────────────────────────────────────────────────────────────────┐
│                    IReader Server Module Vision                     │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Your PC (Server)                          │   │
│  │  ┌────────────────────────────────────────────────────────┐  │   │
│  │  │           IReader Server Module (:server)              │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Ktor Server                                     │  │  │   │
│  │  │  │  - REST API                                      │  │  │   │
│  │  │  │  - Static files (React UI)                       │  │  │   │
│  │  │  │  - WebSocket                                     │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  │  ┌──────────────────────────────────────────────────┐  │  │   │
│  │  │  │  Reused IReader Modules                          │  │  │   │
│  │  │  │  - :source-api (sources)                         │  │  │   │
│  │  │  │  - :domain (business logic)                      │  │  │   │
│  │  │  │  - :data (database)                              │  │  │   │
│  │  │  │  - :core (utilities)                             │  │  │   │
│  │  │  └──────────────────────────────────────────────────┘  │  │   │
│  │  └────────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │ Local Network (WiFi)                  │
│         ┌───────────────────┼───────────────────┐                   │
│         │                   │                   │                   │
│  ┌──────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐            │
│  │   Mobile    │    │   Tablet    │    │   Browser   │            │
│  │   Phone     │    │             │    │   (any)     │            │
│  │             │    │             │    │             │            │
│  │  http://    │    │  http://    │    │  http://    │            │
│  │  192.168.x  │    │  192.168.x  │    │  your-cloud │            │
│  │  :8080      │    │  :8080      │    │  .com       │            │
│  └─────────────┘    └─────────────┘    └─────────────┘            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Module Structure

### 2.1 New Module: `:server`

```
IReader/
├── settings.gradle.kts              # Add :server module
├── build.gradle.kts                 # Root build file
│
├── server/                          # NEW: Server module
│   ├── build.gradle.kts
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   ├── ServerMain.kt           # Entry point
│   │   │   │   │
│   │   │   │   ├── api/                    # REST API routes
│   │   │   │   │   ├── Routing.kt
│   │   │   │   │   ├── SourcesApi.kt
│   │   │   │   │   ├── BooksApi.kt
│   │   │   │   │   ├── ChaptersApi.kt
│   │   │   │   │   └── WebSocket.kt
│   │   │   │   │
│   │   │   │   ├── config/                 # Server configuration
│   │   │   │   │   └── ServerConfig.kt
│   │   │   │   │
│   │   │   │   └── di/                     # Dependency injection
│   │   │   │       └── ServerModule.kt
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.conf        # Ktor config
│   │   │       ├── logback.xml             # Logging
│   │   │       └── static/                 # React build output
│   │   │           ├── index.html
│   │   │           ├── assets/
│   │   │           └── ...
│   │   │
│   │   └── test/
│   │       └── kotlin/
│   │           └── ServerTest.kt
│   │
│   └── frontend/                    # React frontend (separate project)
│       ├── package.json
│       ├── tsconfig.json
│       ├── vite.config.ts
│       ├── public/
│       └── src/
│           ├── main.tsx
│           ├── App.tsx
│           ├── components/
│           ├── hooks/
│           ├── stores/
│           └── types/
│
├── source-api/                      # EXISTING: Source interfaces
├── domain/                          # EXISTING: Business logic
├── data/                            # EXISTING: Database
├── core/                            # EXISTING: Utilities
├── desktop/                         # EXISTING: Desktop app
└── android/                         # EXISTING: Android app
```

### 2.2 Settings.gradle.kts Update

```kotlin
// settings.gradle.kts
rootProject.name = "Infinity"
include(":android")
include(":benchmark")
include(":data")
include(":domain")
include(":presentation")
include(":presentation-core")
include(":core")
include(":i18n")
include(":desktop")
include(":source-api")
include(":plugin-api")
include(":server")                    // NEW: Server module
```

---

## 3. Server Module Implementation

### 3.1 build.gradle.kts

```kotlin
// server/build.gradle.kts
plugins {
    id(kotlinx.plugins.kotlin.jvm.get().pluginId)
    id(kotlinx.plugins.kotlinSerilization.get().pluginId)
    id("io.ktor.plugin") version "2.3.7"
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("ireader.server.ServerMainKt")
}

dependencies {
    // IReader modules (maximum reuse!)
    implementation(project(Modules.sourceApi))
    implementation(project(Modules.domain))
    implementation(project(Modules.data))
    implementation(project(Modules.coreApi))
    
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-compression-jvm")
    
    // Ktor client (for source fetching)
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")
    
    // Database
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation(kotlin("test"))
}

// Task to copy frontend build output to server resources
tasks.register<Copy>("copyFrontend") {
    from("${projectDir}/frontend/dist")
    into("${projectDir}/src/main/resources/static")
}

// Build frontend before server
tasks.named("processResources") {
    dependsOn("copyFrontend")
}

// Fat JAR for distribution
tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "ireader.server.ServerMainKt"
    }
    from(configurations.runtimeClasspath.get().map { 
        if (it.isDirectory) it else zipTree(it) 
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

### 3.2 Server Main

```kotlin
// server/src/main/kotlin/ServerMain.kt
package ireader.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ireader.server.api.*
import ireader.server.config.*
import ireader.server.di.*
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val config = ServerConfig.load(args)
    embeddedServer(Netty, config.port) {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: ServerConfig) {
    val logger = LoggerFactory.getLogger("Server")
    
    // Initialize DI
    val serverModule = ServerModule(config, environment)
    
    // Install plugins
    configureSerialization()
    configureCORS()
    configureCompression()
    configureCallLogging()
    configureStatusPages()
    configureWebSockets()
    
    // Configure routing
    routing {
        // API routes
        route("/api/v1") {
            sourcesApi(serverModule.sourceService)
            booksApi(serverModule.bookService)
            chaptersApi(serverModule.chapterService)
        }
        
        // Static files (React UI)
        staticResources("/", "static") {
            default("index.html")
        }
        
        // Health check
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
    
    // Start source manager
    serverModule.sourceManager.initialize()
    
    logger.info("IReader Server started on port ${config.port}")
    logger.info("Access the UI at http://localhost:${config.port}")
}
```

### 3.3 Server Configuration

```kotlin
// server/src/main/kotlin/config/ServerConfig.kt
package ireader.server.config

import java.nio.file.Path
import java.nio.file.Paths

data class ServerConfig(
    val port: Int,
    val host: String,
    val dataDir: Path,
    val sourcesDir: Path,
    val enableCors: Boolean,
    val corsHosts: List<String>
) {
    val databaseUrl: String get() = "jdbc:sqlite:${dataDir.resolve("ireader.db")}"
    
    companion object {
        fun load(args: Array<String>): ServerConfig {
            // Parse command line args or use defaults
            val port = args.find { it.startsWith("--port=") }
                ?.substringAfter("=")
                ?.toIntOrNull()
                ?: System.getenv("IREADER_PORT")?.toIntOrNull()
                ?: 8080
            
            val host = args.find { it.startsWith("--host=") }
                ?.substringAfter("=")
                ?: System.getenv("IREADER_HOST")
                ?: "0.0.0.0"  // Listen on all interfaces for mobile access
            
            val dataDir = args.find { it.startsWith("--data-dir=") }
                ?.substringAfter("=")
                ?.let { Paths.get(it) }
                : System.getenv("IREADER_DATA_DIR")?.let { Paths.get(it) }
                ?: Paths.get(System.getProperty("user.home"), ".ireader", "server")
            
            val sourcesDir = args.find { it.startsWith("--sources-dir=") }
                ?.substringAfter("=")
                ?.let { Paths.get(it) }
                : dataDir.resolve("sources")
            
            val enableCors = System.getenv("IREADER_CORS")?.toBoolean() ?: true
            
            val corsHosts = System.getenv("IREADER_CORS_HOSTS")
                ?.split(",")
                ?: listOf("http://localhost:3000", "http://localhost:8080")
            
            return ServerConfig(
                port = port,
                host = host,
                dataDir = dataDir,
                sourcesDir = sourcesDir,
                enableCors = enableCors,
                corsHosts = corsHosts
            )
        }
    }
}
```

### 3.4 Dependency Injection

```kotlin
// server/src/main/kotlin/di/ServerModule.kt
package ireader.server.di

import ireader.core.db.IReaderDatabase
import ireader.data.local.DatabaseHandler
import ireader.server.config.ServerConfig
import ireader.server.service.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.nio.file.Files

class ServerModule(config: ServerConfig, environment: ApplicationEnvironment) {
    
    // Create data directory if not exists
    init {
        Files.createDirectories(config.dataDir)
        Files.createDirectories(config.sourcesDir)
    }
    
    // HTTP client for source fetching
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    // Database
    val databaseHandler = DatabaseHandler(config.databaseUrl)
    val database: IReaderDatabase = databaseHandler.createDatabase()
    
    // Source manager (reuse from :domain module)
    val sourceManager = ireader.domain.catalogs.CatalogStore(
        catalogRepository = databaseHandler.catalogRepository,
        preferenceStore = databaseHandler.preferenceStore
    )
    
    // Services
    val sourceService = SourceService(sourceManager, httpClient)
    val bookService = BookService(database)
    val chapterService = ChapterService(database)
}
```

### 3.5 API Routes

```kotlin
// server/src/main/kotlin/api/SourcesApi.kt
package ireader.server.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ireader.server.service.SourceService

fun Route.sourcesApi(sourceService: SourceService) {
    route("/api/v1/sources") {
        // List all sources
        get {
            val sources = sourceService.getAllSources()
            call.respond(sources)
        }
        
        // Get source details
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val source = sourceService.getSource(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Source not found")
            
            call.respond(source)
        }
        
        // Search source
        get("/{id}/search") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val query = call.request.queryParameters["q"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Query required")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            
            val results = sourceService.search(id, query, page)
            call.respond(results)
        }
        
        // Get popular books
        get("/{id}/popular") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            
            val results = sourceService.getPopular(id, page)
            call.respond(results)
        }
    }
}

// server/src/main/kotlin/api/BooksApi.kt
package ireader.server.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ireader.server.service.BookService

fun Route.booksApi(bookService: BookService) {
    route("/api/v1/books") {
        // Get user's library
        get {
            val books = bookService.getLibrary()
            call.respond(books)
        }
        
        // Add book to library
        post {
            val request = call.receive<AddBookRequest>()
            val book = bookService.addBook(request)
            call.respond(book)
        }
        
        // Get book details
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val book = bookService.getBook(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Book not found")
            
            call.respond(book)
        }
        
        // Get chapters for a book
        get("/{id}/chapters") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val chapters = bookService.getChapters(id)
            call.respond(chapters)
        }
    }
}

// server/src/main/kotlin/api/ChaptersApi.kt
package ireader.server.api

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ireader.server.service.ChapterService

fun Route.chaptersApi(chapterService: ChapterService) {
    route("/api/v1/chapters") {
        // Get chapter content (pages)
        get("/{id}/pages") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            val pages = chapterService.getPages(id)
            call.respond(pages)
        }
        
        // Mark chapter as read
        put("/{id}/read") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            
            chapterService.markAsRead(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
```

### 3.6 Ktor Configuration

```kotlin
// server/src/main/kotlin/api/Routing.kt
package ireader.server.api

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json
import java.time.Duration

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureCORS() {
    install(CORS) {
        anyHost()  // Allow all hosts for local network access
        allowHeader("Content-Type")
        allowHeader("Authorization")
    }
}

fun Application.configureCompression() {
    install(Compression) {
        gzip()
        deflate()
    }
}

fun Application.configureCallLogging() {
    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }
}

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
}
```

### 3.7 Application Configuration

```hocon
# server/src/main/resources/application.conf
ktor {
    deployment {
        port = 8080
        port = ${?IREADER_PORT}
        host = "0.0.0.0"
    }
    
    application {
        modules = [ ireader.server.ServerMainKt.module ]
    }
}
```

---

## 4. Frontend Implementation

### 4.1 Frontend Structure

```
server/frontend/
├── package.json
├── tsconfig.json
├── vite.config.ts
├── index.html
│
├── public/
│   └── favicon.ico
│
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── vite-env.d.ts
    │
    ├── api/                    # API client
    │   └── client.ts
    │
    ├── components/             # Reusable components
    │   ├── Layout.tsx
    │   ├── SourceCard.tsx
    │   ├── BookCard.tsx
    │   ├── ChapterList.tsx
    │   └── Reader.tsx
    │
    ├── hooks/                  # Custom hooks
    │   ├── useSources.ts
    │   ├── useBooks.ts
    │   └── useReader.ts
    │
    ├── pages/                  # Page components
    │   ├── HomePage.tsx
    │   ├── SourcesPage.tsx
    │   ├── LibraryPage.tsx
    │   ├── ReaderPage.tsx
    │   └── SettingsPage.tsx
    │
    ├── stores/                 # State management
    │   └── appStore.ts
    │
    └── types/                  # TypeScript types
        ├── source.ts
        ├── book.ts
        └── chapter.ts
```

### 4.2 Package.json

```json
{
  "name": "ireader-server-ui",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.21.0",
    "@tanstack/react-query": "^5.13.0",
    "zustand": "^4.4.7",
    "framer-motion": "^10.16.0",
    "axios": "^1.6.2"
  },
  "devDependencies": {
    "@types/react": "^18.2.43",
    "@types/react-dom": "^18.2.17",
    "@vitejs/plugin-react": "^4.2.1",
    "typescript": "^5.3.3",
    "vite": "^5.0.8"
  }
}
```

### 4.3 Vite Config

```typescript
// server/frontend/vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://localhost:8080',
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true
  }
})
```

### 4.4 API Client

```typescript
// server/frontend/src/api/client.ts
const API_BASE = import.meta.env.VITE_API_URL || '/api/v1';

export interface Source {
  id: number;
  name: string;
  lang: string;
  supportsLatest: boolean;
}

export interface Book {
  id: number;
  title: string;
  author?: string;
  description?: string;
  coverUrl?: string;
  url: string;
}

export interface Chapter {
  id: number;
  bookId: number;
  title: string;
  url: string;
  chapterNumber: number;
  read: boolean;
}

export interface Page {
  url: string;
  index: number;
}

class ApiClient {
  private async fetch<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
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
  
  async searchSource(sourceId: number, query: string, page = 1) {
    return this.fetch(`/sources/${sourceId}/search?q=${encodeURIComponent(query)}&page=${page}`);
  }
  
  async getPopular(sourceId: number, page = 1) {
    return this.fetch(`/sources/${sourceId}/popular?page=${page}`);
  }
  
  // Books
  async getLibrary(): Promise<Book[]> {
    return this.fetch<Book[]>('/books');
  }
  
  async addBook(sourceId: number, book: Partial<Book>): Promise<Book> {
    return this.fetch<Book>('/books', {
      method: 'POST',
      body: JSON.stringify({ sourceId, ...book }),
    });
  }
  
  async getChapters(bookId: number): Promise<Chapter[]> {
    return this.fetch<Chapter[]>(`/books/${bookId}/chapters`);
  }
  
  // Chapters
  async getPages(chapterId: number): Promise<Page[]> {
    return this.fetch<Page[]>(`/chapters/${chapterId}/pages`);
  }
  
  async markAsRead(chapterId: number): Promise<void> {
    return this.fetch(`/chapters/${chapterId}/read`, { method: 'PUT' });
  }
}

export const api = new ApiClient();
```

### 4.5 Main App Component

```tsx
// server/frontend/src/App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Layout } from './components/Layout';
import { HomePage } from './pages/HomePage';
import { SourcesPage } from './pages/SourcesPage';
import { LibraryPage } from './pages/LibraryPage';
import { ReaderPage } from './pages/ReaderPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Layout>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/sources" element={<SourcesPage />} />
            <Route path="/library" element={<LibraryPage />} />
            <Route path="/reader/:chapterId" element={<ReaderPage />} />
          </Routes>
        </Layout>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
```

### 4.6 Source List Page

```tsx
// server/frontend/src/pages/SourcesPage.tsx
import { useQuery } from '@tanstack/react-query';
import { api, Source } from '../api/client';
import { SourceCard } from '../components/SourceCard';

export function SourcesPage() {
  const { data: sources, isLoading, error } = useQuery({
    queryKey: ['sources'],
    queryFn: () => api.getSources(),
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center text-red-500 p-4">
        Error loading sources: {error.message}
      </div>
    );
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Sources</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {sources?.map((source) => (
          <SourceCard key={source.id} source={source} />
        ))}
      </div>
    </div>
  );
}
```

---

## 5. Local Network Access (PC to Mobile)

### 5.1 How It Works

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Local Network Setup                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  PC (Server)                                                        │
│  ├── IP: 192.168.1.100 (example)                                    │
│  ├── Port: 8080                                                     │
│  └── Runs: IReader Server Module                                    │
│                                                                     │
│  Mobile (Client)                                                    │
│  ├── Connected to same WiFi                                         │
│  ├── Browser opens: http://192.168.1.100:8080                       │
│  └── Sees: React UI                                                 │
│                                                                     │
│  Requirements:                                                      │
│  ├── Both devices on same network                                   │
│  ├── Firewall allows port 8080                                      │
│  └── Server binds to 0.0.0.0 (all interfaces)                       │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 Server Configuration for Local Access

```kotlin
// ServerConfig.kt - Default to 0.0.0.0 for local network access
val host = "0.0.0.0"  // Listen on all interfaces
val port = 8080
```

### 5.3 Finding Your PC's IP Address

```bash
# Windows
ipconfig | findstr "IPv4"

# macOS/Linux
ifconfig | grep "inet " | grep -v 127.0.0.1

# Or use hostname
hostname -I
```

### 5.4 Firewall Configuration

```bash
# Linux (ufw)
sudo ufw allow 8080/tcp

# macOS
# System Preferences → Security & Privacy → Firewall → Options
# Add Java/Ktor application

# Windows
# Windows Defender Firewall → Advanced Settings → Inbound Rules
# New Rule → Port → TCP 8080 → Allow
```

### 5.5 Running the Server

```bash
# Development mode (with frontend dev server)
./gradlew :server:run

# Production mode (with built frontend)
./gradlew :server:shadowJar
java -jar server/build/libs/server-all.jar

# With custom port
java -jar server/build/libs/server-all.jar --port=9090

# Access from mobile
# Open browser: http://192.168.1.100:8080
```

---

## 6. Cloud Deployment Option

### 6.1 Docker Support

```dockerfile
# server/Dockerfile
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy server JAR
COPY build/libs/server-all.jar /app/server.jar

# Create directories
RUN mkdir -p /app/data /app/sources

# Environment variables
ENV IREADER_PORT=8080
ENV IREADER_HOST=0.0.0.0
ENV IREADER_DATA_DIR=/app/data
ENV IREADER_CORS=true

# Expose port
EXPOSE 8080

# Volume for persistent data
VOLUME ["/app/data", "/app/sources"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run server
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app/server.jar"]
```

### 6.2 Docker Compose

```yaml
# server/docker-compose.yml
version: '3.8'

services:
  ireader-server:
    build:
      context: ..
      dockerfile: server/Dockerfile
    ports:
      - "8080:8080"
    environment:
      - IREADER_PORT=8080
      - IREADER_HOST=0.0.0.0
      - IREADER_CORS=true
    volumes:
      - ireader-data:/app/data
      - ./sources:/app/sources:ro
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 768M
          cpus: '1.5'

volumes:
  ireader-data:
```

### 6.3 Cloud Deployment Options

| Provider | Plan | RAM | Cost | Setup |
|----------|------|-----|------|-------|
| **Oracle Cloud** | Free Tier | 1 GB | Free | Docker |
| **Hetzner** | CX11 | 2 GB | €3.29 | Docker |
| **DigitalOcean** | Basic | 1 GB | $6 | Docker |
| **Railway** | Starter | 512 MB | $5 | GitHub |
| **Render** | Free | 512 MB | Free | GitHub |

### 6.4 Future Cloud-Ready Configuration

```kotlin
// ServerConfig.kt - Support cloud environment variables
companion object {
    fun load(args: Array<String>): ServerConfig {
        return ServerConfig(
            port = System.getenv("PORT")?.toIntOrNull()  // Heroku, Railway
                ?: System.getenv("IREADER_PORT")?.toIntOrNull()
                ?: 8080,
            host = System.getenv("HOST")  // Cloud providers
                ?: System.getenv("IREADER_HOST")
                ?: "0.0.0.0",
            dataDir = System.getenv("DATA_DIR")?.let { Paths.get(it) }
                ?: Paths.get(System.getProperty("user.home"), ".ireader", "server"),
            // ...
        )
    }
}
```

---

## 7. Implementation Phases

### Phase 1: Server Module Setup
- [ ] Create `:server` module in `settings.gradle.kts`
- [ ] Configure `build.gradle.kts` with Ktor dependencies
- [ ] Create `ServerMain.kt` entry point
- [ ] Configure Ktor plugins (CORS, serialization, etc.)
- [ ] Test basic server startup

### Phase 2: API Implementation
- [ ] Implement `/api/v1/sources/*` endpoints
- [ ] Implement `/api/v1/books/*` endpoints
- [ ] Implement `/api/v1/chapters/*` endpoints
- [ ] Add error handling
- [ ] Test with curl/Postman

### Phase 3: Frontend Setup
- [ ] Create React frontend with Vite
- [ ] Configure proxy for development
- [ ] Implement API client
- [ ] Create basic page structure
- [ ] Build and copy to server resources

### Phase 4: Source Integration
- [ ] Integrate IReader's `CatalogStore`
- [ ] Load sources from IReader extensions
- [ ] Implement source search and popular
- [ ] Test with real sources

### Phase 5: Local Network Testing
- [ ] Configure server to bind to `0.0.0.0`
- [ ] Test access from mobile on same WiFi
- [ ] Configure firewall rules
- [ ] Document setup process

### Phase 6: Cloud Deployment
- [ ] Create Dockerfile
- [ ] Create docker-compose.yml
- [ ] Test on cloud provider
- [ ] Document deployment process

---

## 8. Usage

### Running Locally

```bash
# Terminal 1: Start server
./gradlew :server:run

# Terminal 2: Start frontend dev server (optional)
cd server/frontend
npm run dev

# Access:
# - Local: http://localhost:8080
# - Mobile: http://192.168.1.100:8080 (your PC's IP)
```

### Running with Docker

```bash
# Build and run
cd server
docker-compose up -d

# Access:
# - Local: http://localhost:8080
# - Mobile: http://192.168.1.100:8080
```

### Deploying to Cloud

```bash
# Build Docker image
docker build -t ireader-server -f server/Dockerfile .

# Push to registry
docker tag ireader-server your-registry/ireader-server
docker push your-registry/ireader-server

# Deploy to cloud
# (varies by provider)
```

---

## 9. Summary

### What We're Building

1. **New `:server` module** in IReader project
2. **Ktor server** that reuses existing IReader modules
3. **React frontend** served by Ktor
4. **Local network access** for mobile devices
5. **Cloud-ready** Docker configuration

### Key Benefits

- ✅ **Maximum code reuse** - Uses `:source-api`, `:domain`, `:data`, `:core`
- ✅ **Simple deployment** - Single JAR or Docker container
- ✅ **Mobile access** - Connect from any device on same network
- ✅ **Cloud-ready** - Docker support for future cloud deployment
- ✅ **Low resource usage** - Runs on 512 MB RAM

### Next Steps

1. Create the `:server` module
2. Implement basic Ktor server
3. Add API endpoints
4. Create React frontend
5. Test local network access
6. Add Docker support

---

*This plan provides a complete implementation guide for adding a server module to IReader.*