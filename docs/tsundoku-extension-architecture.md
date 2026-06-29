# Tsundoku Extension Support — Architecture & Flow

## Overview

IReader natively loads Tsundoku (Tachiyomi/Mihon) extensions alongside its own. The approach:
- **android-compat** module (from Suwayomi) provides Android API stubs for Desktop
- **source-api** module hosts the tsundoku source-api alongside IReader's
- **data** module has adapters that bridge tsundoku sources to IReader's catalog system

---

## Module Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        IReader App                           │
│                                                              │
│  ┌─────────────┐   ┌─────────────┐   ┌───────────────────┐ │
│  │ presentation │   │   domain    │   │      data         │ │
│  │   (UI)       │──▶│  (logic)    │──▶│  (persistence)    │ │
│  └─────────────┘   └─────────────┘   └───────────────────┘ │
│                                              │              │
│                                    ┌─────────┴──────────┐  │
│                                    │                     │  │
│                              ┌─────▼──────┐    ┌────────▼─┐│
│                              │ source-api  │    │android-  ││
│                              │ (KMP)       │    │compat    ││
│                              │             │    │(JVM)     ││
│                              │ IReader API │    │          ││
│                              │ + Tsundoku  │    │ Android  ││
│                              │   API       │    │ stubs    ││
│                              └─────────────┘    └──────────┘│
└─────────────────────────────────────────────────────────────┘
```

### What Each Module Contains

```
android-compat/ (JVM-only, Desktop only)
├── android/app/Application.kt          ← real Android lifecycle
├── android/content/Context.kt          ← full Context impl
├── android/content/SharedPreferences.kt ← Java Preferences backed
├── android/net/Uri.kt                  ← full URI impl
├── android/os/Build.kt                 ← version constants
├── android/util/Log.kt                 ← println wrapper
├── androidx/preference/*.kt            ← real preference classes
└── xyz/nulldev/androidcompat/          ← Koin integration, PackageManager, etc.

source-api/ (KMP: androidMain + desktopMain)
├── commonMain/
│   └── ireader/core/source/            ← IReader's source-api
│       ├── Source.kt
│       ├── CatalogSource.kt
│       └── model/{MangaInfo,ChapterInfo,Filter,Page,...}
├── androidMain/
│   ├── eu/kanade/tachiyomi/source/     ← Tsundoku source-api (exact copy)
│   │   ├── Source.kt
│   │   ├── CatalogueSource.kt
│   │   ├── ConfigurableSource.kt
│   │   ├── online/HttpSource.kt
│   │   └── model/{SManga,SChapter,Filter,Page,...}
│   ├── eu/kanade/tachiyomi/network/    ← OkHttp helpers
│   └── tachiyomi/core/.../awaitSingle  ← RxJava bridge
└── desktopMain/
    └── (same as androidMain — android-compat stubs provide android.* classes)

data/ (KMP: androidMain + desktopMain + commonMain)
├── commonMain/
│   └── ireader/data/catalog/impl/tsundoku/
│       ├── TsundokuCatalogSource.kt    ← wraps tsundoku Source as IReader CatalogSource
│       └── TsundokuValidatedData.kt    ← APK metadata container
├── androidMain/
│   └── ireader/data/catalog/impl/tsundoku/
│       └── TsundokuExtensionLoader.kt  ← PackageManager + PathClassLoader
└── desktopMain/
    └── ireader/data/catalog/impl/tsundoku/
        └── DesktopTsundokuExtensionLoader.kt ← ApkFile + dex2jar + URLClassLoader
```

---

## Flow: Loading Tsundoku Extensions

### Flow 1: App Startup → Extension Discovery

```
App starts
    │
    ▼
CatalogStore.initializeCatalogs()
    │
    ├──▶ IReader CatalogLoader.loadAll()
    │       │
    │       ├── Load IReader APK extensions (feature: "ireader")
    │       ├── Load JS plugins
    │       └── Load Tsundoku extensions ──────────────┐
    │                                                    │
    ▼                                                    ▼
AndroidCatalogLoader.loadTsundokuExtensions()    DesktopCatalogLoader.loadTsundokuExtensions()
    │                                                    │
    ├── Query PackageManager for                       ├── Scan ExtensionDir for .apk files
    │   packages with feature:                          ├── Parse APK metadata with ApkFile
    │   "tachiyomi.extension"                           │
    │   "tachiyomi.novelextension"                      │
    │                                                   │
    ▼                                                   ▼
loadTsundokuCatalog(pkgInfo)                    loadTsundokuCatalog(apkFile)
    │                                               │
    ├── Validate metadata                           ├── Validate metadata
    │   (lib version 1.4-1.6)                       │   (same validation)
    │                                               │
    ▼                                               ▼
Create ClassLoader                          dex2jar → URLClassLoader
    │                                               │
    ▼                                               ▼
TsundokuExtensionLoader.loadSources()       DesktopTsundokuExtensionLoader.loadSources()
    │                                               │
    ├── Class.forName(className)                    ├── Class.forName(className)
    ├── newInstance() (no-arg constructor)           ├── newInstance()
    ├── Check if SourceFactory → createSources()    ├── Same logic
    └── Wrap in TsundokuCatalogSource               └── Wrap in TsundokuCatalogSource
```

### Flow 2: Extension Instantiation → DI Setup

```
First tsundoku extension loaded
    │
    ▼
initializeDependencies()
    │
    ├── Create OkHttpClient (30s connect, 30s read, 2min call)
    ├── Create NetworkHelper(cacheDir)
    ├── Create Json { ignoreUnknownKeys = true }
    │
    ├── Register in Injekt:
    │   ├── Injekt.addSingletonFactory(NetworkHelper) { networkHelper }
    │   ├── Injekt.addSingletonFactory(Json) { json }
    │   └── Injekt.addSingletonFactory(Application) { app }
    │
    └── Mark initialized (once only)
```

### Flow 3: Extension Source Usage

```
User browses catalog
    │
    ▼
CatalogStore.getSource(sourceId)
    │
    ▼
TsundokuCatalogSource (wrapper)
    │
    ├── getMangaList(Listing, page)
    │       │
    │       ├── Convert Listing → getPopularManga/getLatestUpdates
    │       ├── Call tsundoku source via reflection
    │       ├── Convert result: SManga → MangaInfo
    │       └── Return MangasPageInfo
    │
    ├── getMangaDetails(MangaInfo, commands)
    │       │
    │       ├── Convert MangaInfo → SManga (reflection)
    │       ├── Call tsundoku source
    │       ├── Convert result: SManga → MangaInfo
    │       └── Return MangaInfo
    │
    ├── getChapterList(MangaInfo, commands)
    │       │
    │       ├── Convert MangaInfo → SManga
    │       ├── Call tsundoku source
    │       ├── Convert result: List<SChapter> → List<ChapterInfo>
    │       └── Return List<ChapterInfo>
    │
    └── getPageList(ChapterInfo, commands)
            │
            ├── Convert ChapterInfo → SChapter
            ├── Call tsundoku source
            ├── Convert result: List<Page> → List<Page>
            └── Return List<Page>
```

### Flow 4: Extension HTTP Requests

```
Extension's HttpSource.getPopularManga(page)
    │
    ▼
client.newCall(popularMangaRequest(page))
    │
    ├── client = network.client (from Injekt → NetworkHelper)
    ├── NetworkHelper provides OkHttpClient with:
    │   ├── 30s timeouts
    │   ├── User-Agent interceptor
    │   └── Optional cache
    │
    ▼
asObservableSuccess() → RxJava Observable
    │
    ├── Execute OkHttp request
    ├── Check response.isSuccessful
    └── Return Response wrapped in Observable
    │
    ▼
awaitSingle() → suspend function
    │
    ├── Bridge RxJava → Coroutines
    └── Return Response
    │
    ▼
popularMangaParse(response)
    │
    ├── Parse HTML with Jsoup
    └── Return MangasPage
```

---

## Model Conversion Map

```
Tsundoku (SManga)          ↔    IReader (MangaInfo)
─────────────────────────────────────────────────────
url                    →    key
title                  →    title
thumbnail_url          →    cover
artist                 →    artist
author                 →    author
description            →    description
genre (String)         →    genres (List<String>)
status (Int)           →    status (Long)

Tsundoku (SChapter)        ↔    IReader (ChapterInfo)
─────────────────────────────────────────────────────
url                    →    key
name                   →    name
chapter_number (Float) →    number (Float)
date_upload (Long)     →    dateUpload (Long)
scanlator (String?)    →    scanlator (String)

Tsundoku (Page)            ↔    IReader (Page)
─────────────────────────────────────────────────────
imageUrl               →    ImageUrl(url)
url                    →    PageUrl(url)
text                   →    Text(text)

Tsundoku (Filter)          ↔    IReader (Filter)
─────────────────────────────────────────────────────
Filter.Text            →    Filter.Text
Filter.CheckBox        →    Filter.Check
Filter.TriState        →    Filter.Check (allowsExclusion)
Filter.Select          →    Filter.Select
Filter.Group           →    Filter.Group
Filter.Sort            →    Filter.Sort
Filter.Header          →    Filter.Note
```

---

## Dependency Chain (Desktop)

```
android-compat
    ├── android.* stubs
    ├── androidx.* stubs
    ├── RxAndroid stubs
    └── No external deps (pure Java/Kotlin)

source-api
    ├── depends on: android-compat (desktopMain)
    ├── provides: eu.kanade.tachiyomi.source.*
    ├── provides: eu.kanade.tachiyomi.network.*
    └── deps: OkHttp, RxJava, Injekt, Jsoup

data
    ├── depends on: source-api (transitive)
    ├── depends on: android-compat (desktopMain, for direct stubs)
    ├── provides: TsundokuCatalogSource (adapter)
    ├── provides: DesktopTsundokuExtensionLoader
    └── deps: dex2jar, apk-parser
```

## Dependency Chain (Android)

```
source-api (androidMain)
    ├── provides: eu.kanade.tachiyomi.source.*
    ├── provides: eu.kanade.tachiyomi.network.*
    └── deps: OkHttp, RxJava, Injekt, Jsoup

data (androidMain)
    ├── depends on: source-api (transitive)
    ├── provides: TsundokuExtensionLoader
    └── uses: real Android APIs (PackageManager, PathClassLoader)
```

---

## Key Design Decisions

1. **No shims** — Real RxJava, real Injekt, real OkHttp. Only Android APIs are stubbed.

2. **Reflection for model conversion** — `TsundokuCatalogSource` uses reflection to access tsundoku types (SManga, SChapter, etc.) since they're in a different module.

3. **Child-first ClassLoader** — Extensions load their own classes first, then delegate to parent (where our shims live). This matches tsundoku's approach.

4. **Direct Injekt registration** — No Koin bridge. Register `NetworkHelper`, `Json`, `Application` directly in Injekt before loading extensions.

5. **Platform-specific loaders** — Android uses `PackageManager` + `PathClassLoader`. Desktop uses `ApkFile` + `dex2jar` + `URLClassLoader`.

6. **android-compat from Suwayomi** — Battle-tested Android API stubs for Desktop. No custom stubs needed.

---

## Error Handling

```
Extension load fails
    │
    ├── ClassNotFoundException → Skip, log warning
    ├── NoClassDefFoundError → Skip, log warning
    ├── ExceptionInInitializerError → Log cause chain, skip
    ├── InvocationTargetException → Unwrap cause, log, skip
    └── NoClassDefFoundError (android.*) → Skip on Desktop, expected
```

---

## What Works Where

| Feature | Android | Desktop |
|---------|---------|---------|
| Load tsundoku APK extensions | ✅ | ✅ |
| Browse popular/latest | ✅ | ✅ |
| Search with filters | ✅ | ✅ |
| View manga details | ✅ | ✅ |
| View chapter list | ✅ | ✅ |
| Read chapters | ✅ | ✅ |
| Extension settings (ConfigurableSource) | ✅ | ❌ (stub only) |
| WebView-based sources | ✅ | ❌ (no WebView) |
| Cloudflare bypass | ✅ | ❌ (no WebView) |
