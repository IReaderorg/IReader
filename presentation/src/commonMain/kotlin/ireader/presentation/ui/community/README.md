# Popular Books Feature

## Overview
The Popular Books screen shows trending books based on how many users are reading them. Each book displays a cover image, description, source badge, and reader count.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│              PopularBooksScreen                      │
│  (Composable UI - observes PopularBooksViewModel)    │
├─────────────────────────────────────────────────────┤
│            PopularBooksViewModel                     │
│  (State: PopularBooksScreenState)                   │
│  - books: List<PopularBook>                         │
│  - isInitialLoading, isLoadingMore                  │
│  - hasMore, isRateLimited                          │
├─────────────────────────────────────────────────────┤
│          PopularBooksRepositoryImpl                  │
│  (Data layer - fetches from Supabase)               │
│  - getPopularBooks(limit) -> List<PopularBook>      │
│  - Maps source_name, sourceGroup, description       │
├─────────────────────────────────────────────────────┤
│        ResolvePopularBookSourceUseCase               │
│  (Domain logic - resolves source installation)       │
│  - Check if source is installed                     │
│  - Return SourceInstalled or SourceNotInstalled      │
└─────────────────────────────────────────────────────┘
```

## Files

| File | Purpose |
|------|---------|
| `PopularBooksScreen.kt` | UI with cover, description, source badge, reader count |
| `PopularBooksViewModel.kt` | State management with infinite scroll |
| `PopularBooksState.kt` | Immutable state data class |
| `PopularBooksScreenSpec.kt` | Navigation wiring |
| `ResolvePopularBookSourceUseCase.kt` | Source resolution logic |
| `SourceInstallDialog.kt` | Dialog for missing source |
| `README.md` | This documentation |

## Data Flow

```
User opens PopularBooksScreen
    ↓
PopularBooksViewModel.loadInitialBooks()
    ↓
PopularBooksRepositoryImpl.getPopularBooks(limit)
    ↓
Supabase query → PopularBookDto → PopularBook
    ↓
Map: sourceName, sourceGroup (LNREADER/IREADER), description
    ↓
lookupLocalBooks() → Check if book is in local library
    ↓
Update isInLibrary, coverUrl, localBookId
    ↓
User taps book → checkBookInLibrary()
    ↓
If in library → OpenLocalBook(bookId)
If not → OpenGlobalSearch(title)
```

## Source Badge Colors

| Source Group | Color |
|-------------|-------|
| LNREADER | Blue (#2196F3) |
| IREADER | Green (#4CAF50) |
| Unknown | Primary |

## Reusable Components Used
- `AsyncImage` - Book cover image loading
- `FeatureScreenScaffold` - (Available for future use)
