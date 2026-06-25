# Android Extension Loading Flow

## Overview

`AndroidCatalogLoader` is responsible for discovering and loading extension APKs
on Android. It implements both `CatalogLoader` and `AsyncPluginLoader`.

## Load Sequence

```
App Start
  └─> CatalogStore.init()
        └─> loadAll()                          # deferred, not on main thread
              ├─ 1. Add bundled sources (Local, Community, Test)
              ├─ 2. Query system packages with PackageManager
              ├─ 3. List local APK files from extension directory
              ├─ 4. List cached APK files from cache directory
              ├─ 5. Load each catalog concurrently (async + awaitAll)
              │     └─> loadCatalogWithRetry()  # up to 3 attempts per catalog
              │           └─> loadLocalCatalog() or loadSystemCatalog()
              │                 ├─> validateMetadata()   # check feature flag, lib version
              │                 ├─> createClassLoader()   # InMemoryDexClassLoader (API 28+)
              │                 └─> loadSource()          # Class.forName + newInstance
              ├─ 6. Deduplicate (prefer system over local)
              ├─ 7. Load JS plugin stubs (if enabled)
              └─ 8. Return combined list
```

## Android 15 (API 35) Compatibility

- **InMemoryDexClassLoader** on API 28+: loads DEX from memory, avoids DEX-on-disk
  permission issues introduced in Android 15.
- **Fallback**: `DexClassLoader` with timestamped output dir for older devices.
- **System packages**: use `PathClassLoader` (standard Android approach, safe on all versions).

## Error Handling

Each catalog loads independently — one failure does not affect others.

| Error Type | Retry? | Behavior |
|---|---|---|
| IO / DEX compilation | Yes (up to 2 retries) | Exponential backoff |
| ClassNotFound | Yes | May resolve after DEX recompilation |
| SecurityException | No | Permanent failure, logged |
| OOM | No | Fatal, skip catalog |
| Invalid metadata | No | Not an extension, silently skip |

Failed catalogs are tracked in `failedCatalogs` map for diagnostics.

## Key Files

- `AndroidCatalogLoader.kt` — this file
- `CatalogLoader.kt` — interface (domain module)
- `AsyncPluginLoader.kt` — interface for JS plugin background loading
- `CatalogStore.kt` — orchestrates loading and manages catalog state
