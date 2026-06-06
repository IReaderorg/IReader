# Source ID → Source Name Migration Plan

## Goal
Replace `sourceId: Long` with `sourceName: String` as the primary source identifier throughout the codebase.

## Source Name Convention
- lnreader sources: `lnreader-mangadex`, `lnreader-novelupdates`, ...
- ireader sources: `ireader-mangadex`, `ireader-novelupdates`, ...

## Phase 1: Add sourceName alongside sourceId (backward compatible)

### Domain Models to Modify

| Model | File | Change |
|-------|------|--------|
| Book | `domain/.../entities/Book.kt` | Add `sourceName: String = ""` |
| PopularBook | `domain/.../remote/PopularBook.kt` | Add `sourceName: String = ""`, `sourceGroup: SourceGroup` |
| SyncedBook | `domain/.../remote/SyncedBook.kt` | Add `sourceName: String = ""` |
| ExploreBook | `domain/.../entities/ExploreBook.kt` | Add `sourceName: String = ""` |
| Download | `domain/.../download/Download.kt` | Add `sourceName: String = ""` |
| CatalogRemote | `domain/.../entities/CatalogRemote.kt` | Add `sourceName: String = ""` |
| SourceHealth | `domain/.../entities/SourceHealth.kt` | Add `sourceName: String = ""` |
| SourceReport | `domain/.../entities/SourceReport.kt` | Add `sourceName: String = ""` |
| LibraryBackup | `domain/.../backup/LibraryBackup.kt` | Add `sourceName: String = ""` |
| Update | `domain/.../entities/Update.kt` | Add `sourceName: String = ""` |
| SavedDownload | `domain/.../entities/SavedDownload.kt` | Add `sourceName: String = ""` |
| BookCover | `domain/.../BookCover.kt` | Add `sourceName: String = ""` |
| BookSyncData | `domain/.../sync/BookSyncData.kt` | Change sourceId from String to sourceName |
| ChapterSyncData | `domain/.../sync/ChapterSyncData.kt` | Update globalId format |
| JSPluginCatalog | `domain/.../entities/JSPluginCatalog.kt` | Add `sourceName: String = ""` |

### New Domain Models to Create

| Model | File | Purpose |
|-------|------|---------|
| SourceGroup | `domain/.../entities/SourceGroup.kt` | Enum: LNREADER, IREADER |

### Data Layer to Modify

| File | Change |
|------|--------|
| `PopularBooksRepositoryImpl.kt` | Map sourceName from Supabase |
| `BookRepositoryImpl.kt` | Populate sourceName from source table |
| `ExploreRepositoryImpl.kt` | Populate sourceName from source table |
| `DownloadRepositoryImpl.kt` | Populate sourceName from source table |
| `CatalogRepositoryImpl.kt` | Populate sourceName from source table |
| `SyncRepositoryImpl.kt` | Use sourceName for sync operations |

### Key Implementation Details

1. **sourceName population**: When creating/updating entities, look up the source name from the source table using sourceId
2. **Backward compatibility**: Keep sourceId in all models, add sourceName as new field
3. **Default values**: sourceName defaults to empty string "" for backward compatibility
4. **Source resolution**: New helper use case `ResolveSourceNameUseCase` to convert sourceId → sourceName

### Execution Order
1. Create SourceGroup enum
2. Add sourceName to Book model (most referenced)
3. Add sourceName to PopularBook model
4. Add sourceName to remaining domain models
5. Update data layer repositories
6. Update presentation layer to use sourceName where needed
