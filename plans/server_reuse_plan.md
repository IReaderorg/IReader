# Server Module - Reuse Existing Code Plan

**Date:** 2026-06-05
**Goal:** Reuse existing IReader domain layer instead of creating new code

---

## Current IReader Architecture

```
:domain
├── catalogs/
│   ├── CatalogStore.kt              # Manages all catalogs
│   ├── interactor/                   # Use cases
│   │   ├── GetLocalCatalogs.kt       # Get installed catalogs
│   │   ├── GetRemoteCatalogs.kt      # Get remote catalogs
│   │   ├── InstallCatalog.kt         # Install a catalog
│   │   └── ...
│   ├── service/
│   │   ├── CatalogLoader.kt          # Interface for loading catalogs
│   │   ├── CatalogInstaller.kt       # Interface for installing
│   │   └── CatalogRemoteRepository.kt
│   └── model/
│       └── CatalogSort.kt
├── data/
│   └── repository/
│       ├── BookRepository.kt
│       ├── ChapterRepository.kt
│       └── ...
└── models/entities/
    ├── CatalogLocal.kt
    ├── CatalogInstalled.kt
    ├── CatalogRemote.kt
    └── ...

:source-api
└── core/source/model/
    ├── MangaInfo.kt                  # Book model (@Serializable)
    ├── ChapterInfo.kt                # Chapter model (@Serializable)
    ├── MangasPageInfo.kt             # Search result (@Serializable)
    └── Page.kt                       # Page model (@Serializable)
```

---

## Server Module Architecture (Revised)

```
:server
├── ServerMain.kt                     # Ktor server entry point
├── config/
│   └── ServerConfig.kt
├── api/                              # API route handlers
│   ├── SourcesApi.kt                 # Source endpoints
│   ├── BooksApi.kt                   # Book endpoints
│   └── ChaptersApi.kt                # Chapter endpoints
├── di/                               # Dependency injection
│   └── ServerModule.kt               # Wires dependencies
└── model/                            # Server-specific DTOs only
    └── ServerInfo.kt
```

---

## Reuse Strategy

### 1. Sources API
**Reuse:** `CatalogStore`, `GetLocalCatalogs`, `GetRemoteCatalogs`

```kotlin
// ServerSourcesApi.kt
class ServerSourcesApi(
    private val getLocalCatalogs: GetLocalCatalogs,
    private val getRemoteCatalogs: GetRemoteCatalogs,
) {
    suspend fun listSources(): List<SourceDto> {
        // Use GetLocalCatalogs to get installed sources
        return getLocalCatalogs.subscribe().first().map { it.toDto() }
    }
    
    suspend fun searchSource(sourceId: Long, query: String, page: Int): MangasPageInfo {
        // Use CatalogStore to get source, then call getMangaList
        val source = getLocalCatalogs.find(sourceId)
        return source?.getMangaList(FilterList(), page) ?: MangasPageInfo.empty()
    }
}
```

### 2. Books API
**Reuse:** `BookRepository`, `ChapterRepository`

```kotlin
class ServerBooksApi(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
) {
    suspend fun getLibrary(): List<MangaInfo> {
        return bookRepository.findAll().map { it.toMangaInfo() }
    }
    
    suspend fun getChapters(bookId: Long): List<ChapterInfo> {
        return chapterRepository.findAllForBook(bookId)
    }
}
```

### 3. Models
**Reuse directly from :source-api:**
- `MangaInfo` (Book)
- `ChapterInfo` (Chapter)
- `MangasPageInfo` (Search results)
- `Page` (Page)

**No new DTOs needed!**

---

## Implementation Steps

### Step 1: Create Server DI Module
Create `server/src/main/kotlin/ireader/server/di/ServerModule.kt`

This will:
- Create instances of existing use cases
- Wire them to API handlers
- Handle platform-specific dependencies

### Step 2: Create API Route Handlers
Create separate files for each API domain:
- `SourcesApi.kt`
- `BooksApi.kt`
- `ChaptersApi.kt`

### Step 3: Update ServerMain.kt
- Initialize DI module
- Register route handlers

### Step 4: Build and Test

---

## Key Benefits

1. **Single source of truth** - Fix once in domain layer, works everywhere
2. **No code duplication** - Reuse existing use cases and models
3. **Type safety** - Use existing @Serializable models
4. **Testability** - Domain layer is already tested

---

## Files to Create

1. `server/src/main/kotlin/ireader/server/di/ServerModule.kt`
2. `server/src/main/kotlin/ireader/server/api/SourcesApi.kt`
3. `server/src/main/kotlin/ireader/server/api/BooksApi.kt`
4. `server/src/main/kotlin/ireader/server/api/ChaptersApi.kt`
5. Update `ServerMain.kt`

---

*This plan maximizes code reuse from the existing IReader domain layer.*