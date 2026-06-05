# Server Module - Detailed Implementation Plan

**Date:** 2026-06-04
**Module:** `:server`
**Purpose:** Ktor server with React UI for IReader

---

## Implementation Order

### Step 1: Create Module Structure
1. Create `server/` directory
2. Create `server/build.gradle.kts`
3. Create `settings.gradle.kts` entry
4. Create package structure

### Step 2: Configure Build
1. Add Ktor plugin
2. Add dependencies (Ktor, serialization, etc.)
3. Add IReader module dependencies
4. Configure application plugin

### Step 3: Implement Server Entry Point
1. Create `ServerMain.kt`
2. Configure Ktor plugins
3. Configure routing
4. Add static file serving

### Step 4: Implement API Routes
1. Create route handlers
2. Add source endpoints
3. Add book endpoints
4. Add chapter endpoints

### Step 5: Create Frontend
1. Initialize React project
2. Create basic components
3. Build and copy to server

### Step 6: Test
1. Run server
2. Test API endpoints
3. Test frontend

---

## File Checklist

### Server Module Files
- [ ] `server/build.gradle.kts`
- [ ] `server/src/main/kotlin/ireader/server/ServerMain.kt`
- [ ] `server/src/main/kotlin/ireader/server/config/ServerConfig.kt`
- [ ] `server/src/main/kotlin/ireader/server/api/Routing.kt`
- [ ] `server/src/main/kotlin/ireader/server/api/SourcesApi.kt`
- [ ] `server/src/main/kotlin/ireader/server/api/BooksApi.kt`
- [ ] `server/src/main/kotlin/ireader/server/api/ChaptersApi.kt`
- [ ] `server/src/main/kotlin/ireader/server/service/SourceService.kt`
- [ ] `server/src/main/kotlin/ireader/server/service/BookService.kt`
- [ ] `server/src/main/kotlin/ireader/server/service/ChapterService.kt`
- [ ] `server/src/main/resources/application.conf`
- [ ] `server/src/main/resources/logback.xml`

### Frontend Files
- [ ] `server/frontend/package.json`
- [ ] `server/frontend/tsconfig.json`
- [ ] `server/frontend/vite.config.ts`
- [ ] `server/frontend/index.html`
- [ ] `server/frontend/src/main.tsx`
- [ ] `server/frontend/src/App.tsx`
- [ ] `server/frontend/src/api/client.ts`
- [ ] `server/frontend/src/components/Layout.tsx`
- [ ] `server/frontend/src/pages/HomePage.tsx`

### Configuration Changes
- [ ] Update `settings.gradle.kts` to include `:server`
- [ ] Update `buildSrc/src/main/kotlin/Modules.kt`

---

## Detailed Steps

### Step 1: Create Module Structure

```
server/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── ireader/
│   │   │       └── server/
│   │   │           ├── ServerMain.kt
│   │   │           ├── config/
│   │   │           │   └── ServerConfig.kt
│   │   │           ├── api/
│   │   │           │   ├── Routing.kt
│   │   │           │   ├── SourcesApi.kt
│   │   │           │   ├── BooksApi.kt
│   │   │           │   └── ChaptersApi.kt
│   │   │           └── service/
│   │   │               ├── SourceService.kt
│   │   │               ├── BookService.kt
│   │   │               └── ChapterService.kt
│   │   └── resources/
│   │       ├── application.conf
│   │       ├── logback.xml
│   │       └── static/
│   └── test/
│       └── kotlin/
│           └── ireader/
│               └── server/
│                   └── ServerTest.kt
└── frontend/
    ├── package.json
    ├── tsconfig.json
    ├── vite.config.ts
    ├── index.html
    └── src/
        ├── main.tsx
        ├── App.tsx
        ├── api/
        │   └── client.ts
        ├── components/
        │   └── Layout.tsx
        └── pages/
            └── HomePage.tsx
```

### Step 2: Build Configuration

```kotlin
// server/build.gradle.kts
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("io.ktor.plugin")
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
    // IReader modules
    implementation(project(":source-api"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core"))
    
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:2.3.7")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.7")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.7")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.3.7")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.7")
    implementation("io.ktor:ktor-server-compression-jvm:2.3.7")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.7")
    
    // Ktor client
    implementation("io.ktor:ktor-client-core-jvm:2.3.7")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:2.3.7")
    
    // Database
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.7")
    testImplementation(kotlin("test"))
}
```

### Step 3: Settings Update

```kotlin
// settings.gradle.kts - Add at the end
include(":server")
```

### Step 4: Modules Update

```kotlin
// buildSrc/src/main/kotlin/Modules.kt
object Modules {
    // ... existing modules
    const val server = ":server"
}
```

---

## API Endpoints

### Sources
- `GET /api/v1/sources` - List all sources
- `GET /api/v1/sources/{id}` - Get source details
- `GET /api/v1/sources/{id}/search?q={query}&page={page}` - Search source
- `GET /api/v1/sources/{id}/popular?page={page}` - Get popular books

### Books
- `GET /api/v1/books` - Get user's library
- `POST /api/v1/books` - Add book to library
- `GET /api/v1/books/{id}` - Get book details
- `GET /api/v1/books/{id}/chapters` - Get chapters for a book

### Chapters
- `GET /api/v1/chapters/{id}/pages` - Get chapter pages
- `PUT /api/v1/chapters/{id}/read` - Mark chapter as read

---

## Testing Plan

1. **Build Test**: `./gradlew :server:build`
2. **Run Test**: `./gradlew :server:run`
3. **API Test**: `curl http://localhost:8080/api/v1/sources`
4. **Health Test**: `curl http://localhost:8080/health`

---

*This is the detailed implementation plan for the server module.*