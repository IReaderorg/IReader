# Server Module - Next Steps Implementation Plan

**Date:** 2026-06-04
**Current Status:** Server running on port 8080 with basic endpoints

---

## Phase 2: IReader Source Integration

### Goal
Connect the server to IReader's existing source system to list and search sources.

### Approach
The `:domain` module contains `CatalogStore` which manages sources. However, it has many dependencies on Android/Desktop specific code. We need to create a simplified version for the server.

### Steps

#### 1. Create Server Source Service
Create `server/src/main/kotlin/ireader/server/service/SourceService.kt`

This service will:
- Use IReader's `CatalogStore` to get available sources
- Provide methods to search sources
- Return data in a format suitable for the API

#### 2. Create Data Transfer Objects (DTOs)
Create `server/src/main/kotlin/ireader/server/model/DTOs.kt`

```kotlin
@Serializable
data class SourceDto(
    val id: Long,
    val name: String,
    val lang: String,
    val supportsLatest: Boolean,
    val iconUrl: String? = null
)

@Serializable
data class BookDto(
    val id: Long,
    val sourceId: Long,
    val title: String,
    val author: String? = null,
    val description: String? = null,
    val coverUrl: String? = null,
    val url: String
)

@Serializable
data class ChapterDto(
    val id: Long,
    val bookId: Long,
    val title: String,
    val url: String,
    val chapterNumber: Float,
    val read: Boolean = false
)

@Serializable
data class PageDto(
    val index: Int,
    val url: String
)
```

#### 3. Update API Routes
Update `ServerMain.kt` to use the new DTOs and service

---

## Phase 3: Database Integration

### Goal
Store user library (books, reading progress) in a local SQLite database.

### Approach
Reuse IReader's SQLDelight schema from the `:data` module.

### Steps

#### 1. Add SQLDelight to server module
Update `build.gradle.kts` to include SQLDelight

#### 2. Create Database Service
Create `server/src/main/kotlin/ireader/server/db/DatabaseService.kt`

#### 3. Create Repository Classes
- `BookRepository.kt` - Manage user's book library
- `ChapterRepository.kt` - Manage chapters
- `HistoryRepository.kt` - Track reading progress

---

## Phase 4: React Frontend

### Goal
Create a modern React UI that connects to the server API.

### Approach
Use Vite + React + TypeScript in `server/frontend/`

### Steps

#### 1. Initialize React Project
```bash
cd server/frontend
npm create vite@latest . -- --template react-ts
npm install
```

#### 2. Install Dependencies
```bash
npm install @tanstack/react-query axios react-router-dom zustand
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

#### 3. Create Basic Structure
```
frontend/src/
├── main.tsx
├── App.tsx
├── api/
│   └── client.ts
├── components/
│   ├── Layout.tsx
│   ├── SourceCard.tsx
│   ├── BookCard.tsx
│   └── ChapterList.tsx
├── pages/
│   ├── HomePage.tsx
│   ├── SourcesPage.tsx
│   ├── LibraryPage.tsx
│   └── ReaderPage.tsx
├── stores/
│   └── appStore.ts
└── types/
    └── index.ts
```

#### 4. Build Script
Add to `build.gradle.kts`:
```kotlin
tasks.register<Exec>("buildFrontend") {
    workingDir = file("frontend")
    commandLine = listOf("npm", "run", "build")
}

tasks.named("processResources") {
    dependsOn("buildFrontend")
}
```

---

## Phase 5: Mobile Access Testing

### Goal
Ensure the server is accessible from mobile devices on the same network.

### Steps

#### 1. Configure Server for Network Access
- Server binds to `0.0.0.0` (already done)
- CORS allows all hosts (already done)

#### 2. Create Connection Guide
Document how to connect from mobile:
1. Find PC's IP address
2. Open mobile browser
3. Navigate to `http://<pc-ip>:8080`

#### 3. Test Endpoints
- Health check
- API endpoints
- Static file serving

---

## Implementation Order

1. **Phase 2: Source Integration** (Priority: High)
   - Create DTOs
   - Create SourceService
   - Update API routes
   - Test with real sources

2. **Phase 3: Database Integration** (Priority: Medium)
   - Add SQLDelight
   - Create database service
   - Create repositories

3. **Phase 4: React Frontend** (Priority: Medium)
   - Initialize project
   - Create basic components
   - Connect to API

4. **Phase 5: Mobile Testing** (Priority: Low)
   - Test network access
   - Document setup

---

## Files to Create

### Server Module
- [ ] `server/src/main/kotlin/ireader/server/model/DTOs.kt`
- [ ] `server/src/main/kotlin/ireader/server/service/SourceService.kt`
- [ ] `server/src/main/kotlin/ireader/server/service/BookService.kt`
- [ ] `server/src/main/kotlin/ireader/server/service/ChapterService.kt`
- [ ] `server/src/main/kotlin/ireader/server/db/DatabaseService.kt`
- [ ] `server/src/main/kotlin/ireader/server/repository/BookRepository.kt`
- [ ] `server/src/main/kotlin/ireader/server/repository/ChapterRepository.kt`

### Frontend
- [ ] `server/frontend/package.json`
- [ ] `server/frontend/vite.config.ts`
- [ ] `server/frontend/tailwind.config.js`
- [ ] `server/frontend/src/main.tsx`
- [ ] `server/frontend/src/App.tsx`
- [ ] `server/frontend/src/api/client.ts`
- [ ] `server/frontend/src/types/index.ts`
- [ ] `server/frontend/src/components/Layout.tsx`
- [ ] `server/frontend/src/pages/HomePage.tsx`

---

*This plan outlines the next steps for implementing the server module.*