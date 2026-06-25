# Catalog Store Flow

## Overview

`CatalogStore` is the single source of truth for all loaded catalogs.
It manages the lifecycle of catalog loading, stub replacement, and UI updates.

## Initialization Flow

```
CatalogStore.init()
  └─> startBatchUpdateProcessor()     # processes CatalogUpdate channel

First access (lazy):
  └─> initializeIfNeeded()
        ├─ 1. loader.loadAll()         # platform-specific (AndroidCatalogLoader etc.)
        ├─ 2. Process catalogs: track stubs, apply pinned state
        ├─ 3. Load user-defined sources
        ├─ 4. Set catalogs = processedCatalogs  # triggers UI update
        ├─ 5. Mark stub sources as loading
        ├─ 6. Load engine plugins in background (J2V8 etc.)
        └─ 7. startBackgroundPluginLoading()    # replaces stubs with real plugins
```

## Stub → Real Plugin Replacement

JS plugins load as stubs first (instant), then get replaced in background:

```
Stub shown in UI (loading spinner)
  └─> startBackgroundPluginLoading()
        └─> asyncLoader.loadJSPluginsAsync { catalog ->
              └─> replaceStubSource(catalog)    # atomic swap via channel
                    ├─ Remove from stubSourceIds
                    └─ Send CatalogUpdate.Replace to channel
            }
```

## Data Flow

```
CatalogStore
  ├─ catalogs: List<CatalogLocal>           # main list, setter triggers UI
  ├─ catalogsFlow: StateFlow                # observable by GetCatalogsByType
  ├─ loadingSourcesFlow: StateFlow<Set<Long>> # which sources are still loading
  ├─ catalogUpdateChannel: Channel<CatalogUpdate>  # batched updates
  └─ catalogsBySourceMap: Map<Long, CatalogLocal>  # O(1) lookup
```

## Thread Safety

- `lock: Mutex` — protects reloadCatalogs() and replaceStubSource()
- `loadingSemaphore` — limits concurrent plugin loads to 4
- `catalogUpdateChannel` — buffered channel for batched UI updates
- All maps use `synchronizedMapOf()`
