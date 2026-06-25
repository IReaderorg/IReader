# Extension Screen Flow

## Overview

`ExtensionViewModel` manages the Extensions/Source tab screen.
It talks directly to `CatalogStore` and use cases — no intermediate controller.

## Architecture

```
ExtensionViewModel
  ├─ state: StateFlow<ExtensionScreenState>   # single source of truth for UI
  ├─ currentDialog: ExtensionDialog           # dialog state (separate for simplicity)
  └─ Preferences as StateFlows (incognito, lastUsedSource, etc.)

Data sources:
  ├─ CatalogStore ──> getCatalogsByType.subscribe() ──> state update
  ├─ CatalogStore ──> getLoadingSourcesFlow() ──> loadingSources in state
  └─ ExtensionWatcherService ──> events ──> snackbar + quiet refresh
```

## Screen State

`ExtensionScreenState` is an @Immutable data class containing:
- `pinnedCatalogs`, `unpinnedCatalogs`, `remoteCatalogs` — filtered lists
- `allPinnedCatalogs`, `allUnpinnedCatalogs`, `allRemoteCatalogs` — unfiltered
- `installSteps` — per-package install progress
- `isRefreshing` — pull-to-refresh state
- `languageChoices`, `selectedLanguage` — language filter
- `sourceStatuses` — health check results
- `loadingSources` — IDs of sources still loading plugins

## Key Flows

### Install
```
User taps Install → installCatalog(catalog)
  └─> scope.launch { installCatalog.await(catalog).collect { step → updateState } }
```

### Refresh
```
User pulls to refresh → refreshCatalogs()
  └─> scope.launch { syncRemoteCatalogs.await(true) → updateState }
      Includes retry on failure.
```

### Crash Recovery
```
init { scheduleEmptySourcesRetry() }
  └─> After 1.5s, check if catalogs are empty
      └─> If empty: catalogStore.reloadCatalogs()
      └─> After 4.5s: second retry if still empty
```

## Removed: ExtensionController

The old architecture had ViewModel → ExtensionController → CatalogStore,
causing race conditions between two subscriptions to the same data.
Now ViewModel subscribes directly — one path, no races.
