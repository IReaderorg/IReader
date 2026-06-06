# WebReader - Full Implementation Plan

## Executive Summary

Build a complete self-hosted web reader application that reuses **all existing IReader backend code** (domain layer, data layer, source APIs) with a **beautiful, responsive React + Material UI frontend** that works on both mobile and desktop. The server module already exists as a skeleton — we need to fully implement the backend APIs and build a polished frontend.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           WebReader Architecture                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    React Frontend (Vite + MUI)                       │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │   │
│  │  │  Home/   │ │  Library │ │  Reader  │ │ Sources  │ │ Settings │ │   │
│  │  │Dashboard │ │          │ │          │ │          │ │          │ │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │   │
│  │  ┌──────────────────────────────────────────────────────────────┐  │   │
│  │  │  State: TanStack Query (server) + Zustand (client)           │  │   │
│  │  │  Routing: React Router v6                                    │  │   │
│  │  │  Theme: MUI v5 Custom Theme (Light/Dark/Sepia/Custom)        │  │   │
│  │  └──────────────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                       │
│                                    ▼ HTTP + WebSocket                      │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Ktor Server (JVM) - :server module               │   │
│  │  ┌──────────────────────────────────────────────────────────────┐  │   │
│  │  │  REST API (full IReader feature set)                         │  │   │
│  │  │  WebSocket (real-time: downloads, sync, progress)            │  │   │
│  │  │  Static file serving (React build output)                    │  │   │
│  │  └──────────────────────────────────────────────────────────────┘  │   │
│  │  ┌──────────────────────────────────────────────────────────────┐  │   │
│  │  │  Reused IReader Domain Layer (:domain module)                │  │   │
│  │  │  - CatalogStore (sources management)                         │  │   │
│  │  │  - BookRepository, ChapterRepository                         │  │   │
│  │  │  - DownloadManager, HistoryRepository                        │  │   │
│  │  │  - SyncManager, TTS services                                 │  │   │
│  │  │  - All preference systems (Reader, Library, etc.)            │  │   │
│  │  └──────────────────────────────────────────────────────────────┘  │   │
│  │  ┌──────────────────────────────────────────────────────────────┐  │   │
│  │  │  Reused IReader Data Layer (:data module)                    │  │   │
│  │  │  - SQLDelight database                                       │  │   │
│  │  │  - All repository implementations                            │  │   │
│  │  │  - All SQL queries and migrations                            │  │   │
│  │  └──────────────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Principles

1. **NO simplified backend** — Every API endpoint delegates to the actual IReader domain use cases and repositories
2. **NO reimplementation** — All business logic, database access, and source fetching reuse existing `:domain`, `:data`, `:source-api`, and `:core` modules
3. **Frontend is NOT a mobile app UI** — Custom MUI theme with desktop-first responsive design that also works beautifully on mobile
4. **Check desktop first** — Before implementing any feature, check if it exists in `desktop/` or `presentation/` modules and reuse the logic

---

## Phase 1: Backend — Full Server Implementation

### 1.1 Server Infrastructure (EXISTING skeleton → needs completion)

**Files:**
- [`server/build.gradle.kts`](server/build.gradle.kts) — Already has all dependencies
- [`server/src/main/kotlin/ireader/server/ServerMain.kt`](server/src/main/kotlin/ireader/server/ServerMain.kt) — Already has Ktor setup with CORS, compression, static files
- [`server/src/main/kotlin/ireader/server/di/ServerModule.kt`](server/src/main/kotlin/ireader/server/di/ServerModule.kt) — Needs full DI wiring

**Tasks:**
1. Create `ServerModule` that initializes the full IReader DI graph for JVM platform
2. Wire up `PreferenceStore`, `HttpClients`, `CatalogStore`, all repositories
3. Create platform-agnostic service layer that the API handlers call into
4. Add WebSocket support for real-time updates (download progress, sync status)
5. Add proper error handling and logging

### 1.2 Sources API (REUSE: `CatalogStore`, `GetLocalCatalogs`, `InstallCatalog`, etc.)

**Desktop reference:** `presentation/desktopMain/.../home/sources/`

**Endpoints:**
```
GET    /api/v1/sources                    → GetLocalCatalogs, GetInstalledCatalog
GET    /api/v1/sources/{id}               → GetInstalledCatalog (by id)
GET    /api/v1/sources/{id}/search        → CatalogSource.search()
GET    /api/v1/sources/{id}/popular       → CatalogSource.getPopular()
GET    /api/v1/sources/{id}/latest        → CatalogSource.getLatest()
GET    /api/v1/sources/{id}/genres        → CatalogSource.getGenres()
GET    /api/v1/sources/{id}/filters       → CatalogSource.getFilterList()
POST   /api/v1/sources/install            → InstallCatalog
DELETE /api/v1/sources/{id}               → UninstallCatalogs
PUT    /api/v1/sources/{id}/pin           → TogglePinnedCatalog
GET    /api/v1/sources/remote             → GetRemoteCatalogs, SyncRemoteCatalogs
POST   /api/v1/sources/{id}/update        → UpdateCatalog
GET    /api/v1/sources/{id}/icon          → Get icon from CatalogSource
```

**Implementation:** Each endpoint delegates to the existing domain interactor. The `CatalogStore` already manages all source operations.

### 1.3 Library API (REUSE: `BookRepository`, `FindAllInLibraryBooks`, `SubscribeInLibraryBooks`, etc.)

**Desktop reference:** `presentation/desktopMain/.../home/library/`

**Endpoints:**
```
GET    /api/v1/library                    → FindAllInLibraryBooks, GetLibraryCategory
GET    /api/v1/library?category={id}      → GetLibraryCategory
GET    /api/v1/library?search={query}     → Search in library
GET    /api/v1/library/{id}               → SubscribeBookById
POST   /api/v1/library                    → InsertBook (add to library)
DELETE /api/v1/library/{id}               → DeleteBookById
PUT    /api/v1/library/{id}               → updateBook
GET    /api/v1/library/{id}/chapters      → SubscribeChaptersByBookId
GET    /api/v1/library/{id}/cover         → CoverCache / BookCoverFetcher
PUT    /api/v1/library/{id}/read          → MarkBookAsReadOrNot
PUT    /api/v1/library/{id}/favorite      → ToggleBookPinUseCase
GET    /api/v1/library/{id}/progress      → GetReadingProgressUseCase
GET    /api/v1/library/categories         → GetCategoriesUseCase
```

### 1.4 Reader API (REUSE: `ChapterRepository`, `LoadChapterContentUseCase`, `UpdateProgressUseCase`, etc.)

**Desktop reference:** `presentation/desktopMain/.../reader/`

**Endpoints:**
```
GET    /api/v1/reader/chapter/{id}              → LoadChapterContentUseCase
GET    /api/v1/reader/chapter/{id}/content      → GetChapterByIdUseCase + FetchAndSaveChapterContentUseCase
PUT    /api/v1/reader/progress                   → UpdateProgressUseCase
GET    /api/v1/reader/progress/{bookId}          → GetReadingProgressUseCase
PUT    /api/v1/reader/bookmark                   → UpdateChapterBookmarkStatusUseCase
GET    /api/v1/reader/bookmarks/{bookId}         → GetChaptersByBookId (filter bookmarked)
GET    /api/v1/reader/toc/{bookId}               → SubscribeChaptersByBookId
GET    /api/v1/reader/chapter/{id}/pages         → Chapter content parsed to pages
GET    /api/v1/reader/chapter/{id}/next          → NavigateChapterUseCase (next)
GET    /api/v1/reader/chapter/{id}/previous      → NavigateChapterUseCase (prev)
```

### 1.5 Downloads API (REUSE: `DownloadManagerUseCase`, `DownloadChaptersUseCase`, etc.)

**Desktop reference:** `presentation/desktopMain/.../settings/data/`

**Endpoints:**
```
GET    /api/v1/downloads                        → SubscribeDownloadsUseCase
POST   /api/v1/downloads                        → DownloadChaptersUseCase
GET    /api/v1/downloads/{id}                   → SubscribeOneSavedDownload
DELETE /api/v1/downloads/{id}                   → DeleteSavedDownload
PUT    /api/v1/downloads/{id}/pause             → PauseDownloadUseCase
PUT    /api/v1/downloads/{id}/resume            → ResumeDownloadUseCase
PUT    /api/v1/downloads/{id}/priority          → UpdateDownloadPriority
DELETE /api/v1/downloads/completed              → DeleteAllSavedDownload
```

### 1.6 Categories API (REUSE: `CategoryRepository`, `CreateCategoryUseCase`, etc.)

**Endpoints:**
```
GET    /api/v1/categories                 → GetCategoriesUseCase
POST   /api/v1/categories                 → CreateCategoryUseCase
PUT    /api/v1/categories/{id}            → UpdateCategoryUseCase
DELETE /api/v1/categories/{id}            → DeleteCategoryUseCase
POST   /api/v1/categories/{id}/books      → SetBookCategories
DELETE /api/v1/categories/{id}/books/{bookId} → RemoveBookFromCategoryUseCase
```

### 1.7 Settings API (REUSE: All preference classes)

**Desktop reference:** `presentation/desktopMain/.../settings/`

**Endpoints:**
```
GET    /api/v1/settings                   → All preference stores
PUT    /api/v1/settings                   → Update preferences
GET    /api/v1/settings/reader            → ReaderPreferences
PUT    /api/v1/settings/reader            → Update ReaderPreferences
GET    /api/v1/settings/library           → LibraryPreferences
PUT    /api/v1/settings/library           → Update LibraryPreferences
GET    /api/v1/settings/download          → DownloadPreferences
PUT    /api/v1/settings/download          → Update DownloadPreferences
GET    /api/v1/settings/appearance        → UiPreferences, AppPreferences
PUT    /api/v1/settings/appearance        → Update appearance prefs
GET    /api/v1/settings/sync              → SyncPreferences
PUT    /api/v1/settings/sync              → Update SyncPreferences
GET    /api/v1/settings/source            → SourcePreferences
PUT    /api/v1/settings/source            → Update SourcePreferences
GET    /api/v1/settings/translation       → TranslationPreferences
PUT    /api/v1/settings/translation       → Update TranslationPreferences
GET    /api/v1/settings/tts               → VoicePreferences
PUT    /api/v1/settings/tts               → Update VoicePreferences
GET    /api/v1/settings/privacy           → PrivacyPreferences
PUT    /api/v1/settings/privacy           → Update PrivacyPreferences
GET    /api/v1/settings/security          → SecurityPreferences
PUT    /api/v1/settings/security          → Update SecurityPreferences
```

### 1.8 Search API (REUSE: Source search via `CatalogSource.search()`)

**Endpoints:**
```
GET    /api/v1/search?query={q}&source={id}  → Single source search
GET    /api/v1/search/global?query={q}        → Search across all sources
GET    /api/v1/search/suggestions?query={q}   → Search suggestions
```

### 1.9 History API (REUSE: `HistoryRepository`, `GetHistoryUseCase`, etc.)

**Endpoints:**
```
GET    /api/v1/history                    → GetHistoryUseCase
DELETE /api/v1/history                    → ClearHistoryUseCase
DELETE /api/v1/history/{id}               → DeleteHistoryUseCase
GET    /api/v1/history/recent             → GetLastReadNovelUseCase
```

### 1.10 Stats API (REUSE: `LibraryStatisticsUseCase`, `ReadingStatisticsRepositoryImpl`)

**Endpoints:**
```
GET    /api/v1/stats/library              → LibraryStatisticsUseCase
GET    /api/v1/stats/reading              → ReadingStatisticsRepositoryImpl
GET    /api/v1/stats/sources              → Source statistics
```

### 1.11 TTS API (REUSE: `PiperVoiceService`, `VoiceCatalog`, `PiperModelManager`)

**Endpoints:**
```
GET    /api/v1/tts/voices                 → VoiceCatalog / PiperVoiceService
POST   /api/v1/tts/speak                  → PiperSpeechSynthesizer (stream audio)
POST   /api/v1/tts/stop                   → Stop TTS playback
GET    /api/v1/tts/voices/{id}/download   → PiperVoiceDownloader
GET    /api/v1/tts/status                 → TTS state
```

### 1.12 Sync API (REUSE: `SyncRepository`, `SyncManager`)

**Endpoints:**
```
GET    /api/v1/sync/status                → SyncRepository
POST   /api/v1/sync/start                 → Start sync
POST   /api/v1/sync/stop                  → Stop sync
GET    /api/v1/sync/history               → Sync history
GET    /api/v1/sync/devices               → Discovered devices
POST   /api/v1/sync/pair                  → Pair device
```

### 1.13 Updates API (REUSE: `UpdateRepository`)

**Endpoints:**
```
GET    /api/v1/updates                    → Get chapter updates
POST   /api/v1/updates/check              → Check for new chapters
GET    /api/v1/updates/recent             → Recent updates
```

### 1.14 Local Source API (REUSE: `LocalSourceImpl`, `RefreshLocalLibrary`, `ImportEpub`, `ImportPdf`)

**Endpoints:**
```
POST   /api/v1/local/import               → ImportEpub / ImportPdf
GET    /api/v1/local/books                → LocalSourceImpl
POST   /api/v1/local/refresh              → RefreshLocalLibrary
GET    /api/v1/local/folders              → OpenLocalFolder
```

### 1.15 Backup API (REUSE: `CloudBackupManager`, `ImportLNReaderBackup`)

**Endpoints:**
```
POST   /api/v1/backup/export              → CloudBackupManager
POST   /api/v1/backup/import              → ImportLNReaderBackup
GET    /api/v1/backup/list                → List backups
```

### 1.16 Translation API (REUSE: `TranslatedChapterRepositoryImpl`, `GlossaryRepositoryImpl`)

**Endpoints:**
```
POST   /api/v1/translate                  → Translate chapter content
GET    /api/v1/translate/languages        → Available translation languages
GET    /api/v1/glossary                   → GlossaryUseCases
POST   /api/v1/glossary                   → Add glossary entry
```

### 1.17 Tracking API (REUSE: `AniListRepositoryImpl`, `MyAnimeListRepositoryImpl`, etc.)

**Endpoints:**
```
GET    /api/v1/tracking                   → List tracking services
POST   /api/v1/tracking/{service}/link    → Link tracking
PUT    /api/v1/tracking/{id}              → Update tracking
GET    /api/v1/tracking/{id}              → Get tracking status
```

### 1.18 WebSocket Events

```
ws://host:port/ws

Events:
- download.progress    → Download progress updates
- download.complete    → Download completed
- sync.status          → Sync status changes
- chapter.update       → New chapter available
- tts.state            → TTS playback state
- reader.progress      → Reading progress updates
```

---

## Phase 2: Frontend — Complete React UI

### 2.1 Technology Stack

| Layer | Technology |
|-------|-----------|
| Framework | React 18 + TypeScript |
| Build | Vite |
| UI Library | MUI v5 (heavily customized) |
| Routing | React Router v6 |
| Server State | TanStack Query v5 |
| Client State | Zustand |
| Forms | React Hook Form + Zod |
| Animations | Framer Motion |
| Charts | Recharts |
| Icons | Lucide React |
| Markdown | React Markdown |
| Virtualization | @tanstack/react-virtual |

### 2.2 Project Structure

```
server/frontend/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── public/
│   └── fonts/
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── api/                    # API client layer
│   │   ├── client.ts           # Axios/fetch client
│   │   ├── sources.ts
│   │   ├── library.ts
│   │   ├── reader.ts
│   │   ├── downloads.ts
│   │   ├── categories.ts
│   │   ├── settings.ts
│   │   ├── search.ts
│   │   ├── history.ts
│   │   ├── stats.ts
│   │   ├── tts.ts
│   │   ├── sync.ts
│   │   ├── updates.ts
│   │   ├── local.ts
│   │   ├── backup.ts
│   │   ├── translation.ts
│   │   └── tracking.ts
│   ├── components/             # Reusable UI components
│   │   ├── common/             # Shared components
│   │   │   ├── AppBar.tsx
│   │   │   ├── SideNav.tsx
│   │   │   ├── BottomNav.tsx
│   │   │   ├── SearchBar.tsx
│   │   │   ├── EmptyState.tsx
│   │   │   ├── LoadingState.tsx
│   │   │   ├── ErrorBoundary.tsx
│   │   │   ├── ConfirmDialog.tsx
│   │   │   ├── ImageWithFallback.tsx
│   │   │   ├── VirtualList.tsx
│   │   │   └── ThemeToggle.tsx
│   │   ├── books/              # Book-related components
│   │   │   ├── BookCard.tsx
│   │   │   ├── BookGrid.tsx
│   │   │   ├── BookList.tsx
│   │   │   ├── BookDetail.tsx
│   │   │   ├── BookCover.tsx
│   │   │   ├── BookMetadata.tsx
│   │   │   ├── ChapterList.tsx
│   │   │   ├── ChapterItem.tsx
│   │   │   └── BookActions.tsx
│   │   ├── reader/             # Reader components
│   │   │   ├── Reader.tsx
│   │   │   ├── ReaderContent.tsx
│   │   │   ├── ReaderSettings.tsx
│   │   │   ├── ReaderTOC.tsx
│   │   │   ├── ReaderProgressBar.tsx
│   │   │   ├── ReaderNavigation.tsx
│   │   │   ├── TextSelectionToolbar.tsx
│   │   │   ├── BookmarkButton.tsx
│   │   │   ├── TTSControls.tsx
│   │   │   ├── FontPicker.tsx
│   │   │   ├── ThemeSelector.tsx
│   │   │   └── PageTransition.tsx
│   │   ├── sources/            # Source components
│   │   │   ├── SourceCard.tsx
│   │   │   ├── SourceList.tsx
│   │   │   ├── SourceDetail.tsx
│   │   │   ├── SourceSearch.tsx
│   │   │   ├── SourceBrowser.tsx
│   │   │   └── SourceInstall.tsx
│   │   ├── downloads/          # Download components
│   │   │   ├── DownloadQueue.tsx
│   │   │   ├── DownloadItem.tsx
│   │   │   └── DownloadManager.tsx
│   │   ├── settings/           # Settings components
│   │   │   ├── SettingsSection.tsx
│   │   │   ├── SettingsGroup.tsx
│   │   │   ├── SettingsItem.tsx
│   │   │   ├── ColorPicker.tsx
│   │   │   ├── SliderSetting.tsx
│   │   │   ├── SwitchSetting.tsx
│   │   │   ├── SelectSetting.tsx
│   │   │   └── NumberSetting.tsx
│   │   └── layout/             # Layout components
│   │       ├── MainLayout.tsx
│   │       ├── MobileLayout.tsx
│   │       ├── DesktopLayout.tsx
│   │       └── ResponsiveLayout.tsx
│   ├── hooks/                  # Custom React hooks
│   │   ├── useApi.ts
│   │   ├── useWebSocket.ts
│   │   ├── useTheme.ts
│   │   ├── useSettings.ts
│   │   ├── useReaderSettings.ts
│   │   ├── useMediaQuery.ts
│   │   ├── useInfiniteScroll.ts
│   │   ├── useDebounce.ts
│   │   └── useLocalStorage.ts
│   ├── pages/                  # Page components
│   │   ├── HomePage.tsx
│   │   ├── LibraryPage.tsx
│   │   ├── BookDetailPage.tsx
│   │   ├── ReaderPage.tsx
│   │   ├── SourcesPage.tsx
│   │   ├── SourceDetailPage.tsx
│   │   ├── SourceSearchPage.tsx
│   │   ├── DownloadsPage.tsx
│   │   ├── HistoryPage.tsx
│   │   ├── SettingsPage.tsx
│   │   ├── SettingsReaderPage.tsx
│   │   ├── SettingsLibraryPage.tsx
│   │   ├── SettingsAppearancePage.tsx
│   │   ├── SettingsDownloadPage.tsx
│   │   ├── SettingsSyncPage.tsx
│   │   ├── SettingsSourcePage.tsx
│   │   ├── SettingsTranslationPage.tsx
│   │   ├── SettingsTTSPage.tsx
│   │   ├── SettingsPrivacyPage.tsx
│   │   ├── SettingsSecurityPage.tsx
│   │   ├── SettingsAdvancedPage.tsx
│   │   ├── StatsPage.tsx
│   │   ├── UpdatesPage.tsx
│   │   ├── BackupPage.tsx
│   │   ├── LocalSourcePage.tsx
│   │   └── NotFoundPage.tsx
│   ├── stores/                 # Zustand stores
│   │   ├── themeStore.ts
│   │   ├── readerStore.ts
│   │   ├── settingsStore.ts
│   │   ├── uiStore.ts
│   │   ├── libraryStore.ts
│   │   └── downloadStore.ts
│   ├── theme/                  # MUI theme
│   │   ├── index.ts
│   │   ├── palette.ts
│   │   ├── typography.ts
│   │   ├── components.ts
│   │   ├── breakpoints.ts
│   │   ├── shadows.ts
│   │   ├── themes/
│   │   │   ├── light.ts
│   │   │   ├── dark.ts
│   │   │   ├── sepia.ts
│   │   │   └── custom.ts
│   │   └── globalStyles.ts
│   ├── types/                  # TypeScript types
│   │   ├── api.ts
│   │   ├── book.ts
│   │   ├── chapter.ts
│   │   ├── source.ts
│   │   ├── settings.ts
│   │   ├── download.ts
│   │   └── common.ts
│   ├── utils/                  # Utility functions
│   │   ├── format.ts
│   │   ├── validators.ts
│   │   ├── constants.ts
│   │   └── helpers.ts
│   └── routes/                 # Route definitions
│       ├── index.tsx
│       └── routes.tsx
```

### 2.3 Custom MUI Theme Design System

**Design Principles:**
- NOT default Material UI — heavily customized
- Desktop-first with responsive mobile support
- Smooth animations (200ms ease-out)
- Consistent 8px spacing system
- Custom color palette (not MUI default)

**Color Palette:**
```typescript
// Brand colors
primary:   { main: '#6366f1', light: '#818cf8', dark: '#4f46e5' }
secondary: { main: '#f43f5e', light: '#fb7185', dark: '#e11d48' }
// Surface
surface:   { paper: '#ffffff', background: '#f8fafc', card: '#ffffff' }
// Dark mode
dark:      { paper: '#1e293b', background: '#0f172a', card: '#1e293b' }
// Sepia
sepia:     { paper: '#f4ecd8', background: '#e8dcc8', card: '#f4ecd8' }
```

**Typography:**
- Headings: 'Inter' or 'Plus Jakarta Sans'
- Body: 'Inter'
- Reading: 'Merriweather', 'Source Serif 4', 'Nunito Sans'
- Monospace: 'JetBrains Mono'

**Breakpoints:**
- Mobile: < 600px
- Tablet: 600px - 1024px
- Desktop: > 1024px
- Wide: > 1440px

**Component Overrides:**
- Cards: 16px border-radius, subtle shadow, hover elevation
- Buttons: 8px border-radius, custom color variants
- Inputs: 8px border-radius, custom focus states
- Dialogs: 16px border-radius, backdrop blur
- Navigation: Custom sidebar (desktop) + bottom nav (mobile)

### 2.4 Page-by-Page Specification

#### Home/Dashboard (`/`)
- **Continue Reading** section with book covers and progress
- **Recent Updates** from followed sources
- **Reading Stats** (books read, time spent, streak)
- **Quick Actions** (add source, browse, settings)
- **Library Statistics** chart

#### Library (`/library`)
- **View Modes**: Grid (covers), List (compact), Detailed
- **Filters**: Category, Source, Status, Reading Progress
- **Sort**: Title, Author, Date Added, Last Read, Progress
- **Search**: Real-time with debounce
- **Bulk Actions**: Select multiple, bulk delete, bulk categorize
- **Infinite Scroll** with virtualization
- **Category Tabs** at top
- **Empty State** with CTA to add sources

#### Book Detail (`/library/:bookId`)
- Cover image, title, author, description
- Chapter list with read/unread status
- Progress indicator
- Actions: Read, Download, Add to category, Delete
- Metadata: Source, Status, Genres, Tags
- Tracking status

#### Reader (`/reader/:bookId/:chapterId`)
- **Reading Modes**: Scroll, Paginated, Webtoon
- **Settings Panel**: Font, size, line height, margins, theme, alignment
- **Navigation**: Swipe/click, chapter selector, progress bar, TOC sidebar
- **Features**: Text selection (highlight, note, translate, TTS), bookmarks, fullscreen
- **TTS Controls**: Play, pause, speed, voice selection
- **Volume Key Navigation** (desktop keyboard support)
- **Auto-scroll** mode
- **Reading statistics** overlay

#### Sources (`/sources`)
- **Installed Sources** tab with cards
- **Browse/Remote Sources** tab with search and install
- **Source Detail** page with search, popular, latest
- **Extension Management** with update all

#### Source Search (`/sources/:sourceId/search`)
- Search bar with filters
- Results grid with book covers
- Infinite scroll
- Filter panel (genre, status, sort)

#### Downloads (`/downloads`)
- Download queue with progress bars
- Grouped by book
- Actions: Pause, resume, cancel, reorder
- Completed downloads section

#### Updates (`/updates`)
- Recent chapter updates from library books
- Grouped by date
- Quick read action

#### History (`/history`)
- Reading history list
- Clear history option
- Continue reading from history

#### Settings (`/settings`)
- **Reader**: Font, size, line height, margins, theme, scroll mode, TTS settings
- **Library**: Display, categories, update settings
- **Appearance**: Theme, font scaling, animations, locale
- **Download**: Path, quality, concurrency, auto-download
- **Sync**: Server, auto-sync, conflict resolution
- **Source**: Auto-update, notifications
- **Translation**: Engine, API keys, glossary
- **TTS**: Voice, speed, pitch, model management
- **Privacy**: Analytics, crash reports
- **Security**: App lock, biometric
- **Advanced**: Logs, cache, debug, import/export
- **About**: Version, licenses

#### Stats (`/stats`)
- Library statistics (total books, read, unread)
- Reading statistics (time spent, pages read, streak)
- Source statistics
- Charts and graphs

#### Backup (`/backup`)
- Export/Import backup
- Google Drive backup
- LNReader backup import
- Auto-backup settings

#### Local Source (`/local`)
- Import EPUB/PDF
- Local library browser
- Folder browser

### 2.5 State Management

**Server State (TanStack Query):**
- Queries for all API endpoints
- Automatic cache invalidation
- Optimistic updates for mutations
- Infinite queries for paginated lists
- Prefetching on hover

**Client State (Zustand):**
- `themeStore`: Theme mode, custom colors
- `readerStore`: Reader settings, current position
- `settingsStore`: UI preferences
- `uiStore`: Sidebar state, modals, toasts
- `libraryStore`: View mode, filters, sort
- `downloadStore`: Download UI state

### 2.6 Responsive Design Strategy

**Desktop (> 1024px):**
- Left sidebar navigation (collapsible)
- Multi-column layouts
- Hover interactions
- Keyboard shortcuts
- Right-side panels (TOC, settings)

**Tablet (600px - 1024px):**
- Collapsible sidebar (icon-only mode)
- Two-column layouts
- Touch-friendly targets
- Swipe gestures

**Mobile (< 600px):**
- Bottom navigation bar
- Single column layouts
- Full-screen modals
- Pull-to-refresh
- Swipe navigation in reader

---

## Phase 3: Implementation Order

### Step 1: Backend Foundation
1. Create JVM platform DI module for server (reuse `DomainModule` pattern from desktop)
2. Create `ServerService` layer that wraps domain use cases
3. Implement Sources API (full CRUD + search + browse)
4. Implement Library API (full CRUD + categories)
5. Implement Reader API (chapter content + progress)
6. Implement Downloads API
7. Implement Settings API (all preference groups)
8. Implement Search API
9. Implement History API
10. Implement Stats API
11. Implement WebSocket events
12. Implement remaining APIs (TTS, Sync, Updates, Local, Backup, Translation, Tracking)

### Step 2: Frontend Foundation
1. Set up Vite + React + TypeScript project
2. Install all dependencies (MUI, TanStack Query, Zustand, React Router, etc.)
3. Create custom MUI theme with all color modes
4. Create responsive layout system (desktop sidebar + mobile bottom nav)
5. Create API client layer with all endpoints
6. Create reusable component library
7. Set up routing and navigation
8. Set up state management (stores + query client)

### Step 3: Core Pages
1. Home/Dashboard page
2. Library page (grid/list views, filters, search)
3. Book Detail page
4. Reader page (all reading modes + settings)
5. Sources page (installed + browse)
6. Source Search page
7. Downloads page
8. Settings page (all sub-pages)

### Step 4: Secondary Pages
1. History page
2. Stats page
3. Updates page
4. Backup page
5. Local Source page
6. Translation settings page
7. TTS settings page
8. Sync settings page

### Step 5: Polish
1. Animations and transitions
2. Keyboard shortcuts
3. PWA support (service worker, manifest)
4. Offline support (cached library)
5. Performance optimization (virtualization, lazy loading)
6. Error handling and error boundaries
7. Loading states and skeletons
8. Toast notifications

---

## Feature Reuse Checklist

Before implementing each feature, verify against existing desktop code:

| Feature | Desktop Location | Reuse Strategy |
|---------|-----------------|----------------|
| Source management | `presentation/desktopMain/.../home/sources/` | Reuse domain interactors directly |
| Library management | `presentation/desktopMain/.../home/library/` | Reuse BookRepository + use cases |
| Reader | `presentation/desktopMain/.../reader/` | Reuse ChapterRepository + content loading |
| Downloads | `presentation/desktopMain/.../settings/data/` | Reuse DownloadManagerUseCase |
| Settings | `presentation/desktopMain/.../settings/` | Reuse all Preference classes |
| TTS | `presentation/desktopMain/.../home/tts/` | Reuse PiperVoiceService + PiperModelManager |
| Sync | `presentation/desktopMain/.../sync/` | Reuse SyncRepository |
| Backup | `presentation/desktopMain/.../settings/backups/` | Reuse CloudBackupManager |
| Local source | `presentation/desktopMain/.../settings/advance/` | Reuse LocalSourceImpl + ImportEpub |
| Translation | `presentation/desktopMain/.../settings/translation/` | Reuse TranslationPreferences |
| Tracking | `presentation/desktopMain/.../settings/tracking/` | Reuse tracking repositories |
| Categories | `presentation/.../commonMain/.../ui/` | Reuse CategoryRepository |
| History | `presentation/.../commonMain/.../ui/` | Reuse HistoryRepository |
| Stats | `presentation/.../commonMain/.../ui/` | Reuse ReadingStatisticsRepositoryImpl |

---

## API Response Format

All API responses follow a consistent format:

```typescript
// Success
{
  "data": T,
  "meta": {
    "page": 1,
    "totalPages": 10,
    "totalItems": 100
  }
}

// Error
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Book not found",
    "details": {}
  }
}
```

---

## WebSocket Protocol

```typescript
// Client → Server: Subscribe to events
{ "action": "subscribe", "channel": "downloads" }

// Server → Client: Event
{
  "channel": "downloads",
  "event": "progress",
  "data": {
    "downloadId": 123,
    "progress": 0.75,
    "status": "downloading"
  }
}
```

---

## Testing Strategy

1. **Backend**: Each API endpoint tested against real domain layer
2. **Frontend**: Component tests with React Testing Library
3. **E2E**: Critical flows (add source → search → add to library → read)
4. **Responsive**: Test on mobile, tablet, and desktop viewports

---

## Deployment

1. **Build frontend**: `cd server/frontend && npm run build`
2. **Copy to static**: Copy `dist/` to `server/src/main/resources/static/`
3. **Build server**: `./gradlew :server:installDist`
4. **Run**: `./server/build/install/server/bin/server --port=8080`
5. **Docker** (optional): Create Dockerfile for containerized deployment
