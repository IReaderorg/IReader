# Source ID → Source Name Migration Phase 2 & 3

## Phase 2: Migrate Internal Logic to Use sourceName

### Priority 1: Core Models (remove sourceId)
These models are referenced throughout the codebase. Remove sourceId and make sourceName non-optional.

1. **Book.kt** - Remove `sourceId: Long`, make `sourceName: String` required
2. **ExploreBook.kt** - Remove `sourceId`, make `sourceName` required
3. **PopularBook.kt** - Remove `sourceId`, make `sourceName` required
4. **SyncedBook.kt** - Remove `sourceId`, make `sourceName` required
5. **Download.kt** - Remove `sourceId`, make `sourceName` required
6. **SavedDownload.kt** - Remove `sourceId`, make `sourceName` required
7. **BookCover.kt** - Remove `sourceId`, make `sourceName` required
8. **Update.kt** - Remove `sourceId`, make `sourceName` required
9. **BookUpdate.kt** - Remove `sourceId`, make `sourceName` required
10. **BookSyncData.kt** - Change sourceId from String to sourceName
11. **ChapterSyncData.kt** - Update globalId format to use sourceName

### Priority 2: Secondary Models
12. **SourceHealth.kt** - Remove `sourceId`, make `sourceName` required
13. **SourceReport.kt** - Remove `sourceId`, make `sourceName` required
14. **LibraryBackup.kt** - Remove `sourceId`, make `sourceName` required
15. **CatalogRemote.kt** - Remove `sourceId`, make `sourceName` required
16. **JSPluginCatalog.kt** - Remove `sourceId`, make `sourceName` required
17. **LibraryInsights.kt** - Remove `sourceId` from all nested models
18. **MigrationModels.kt** - Remove `sourceId` from all nested models

### Priority 3: Base Interfaces
19. **BaseBook** interface - Remove `sourceId`, add `sourceName`
20. **BookBase** interface - Remove `sourceId`, add `sourceName`
21. **Catalog** sealed class - Remove `sourceId`, add `sourceName`
22. **CatalogLocal** - Remove `sourceId`, add `sourceName`

### Data Layer Changes
- Update all repositories to use sourceName for lookups
- Update SQL queries to filter by source_name instead of source_id
- Update Supabase queries to use source_name
- Update database schema (add source_name column, drop source_id column)

### Presentation Layer Changes
- Update all ViewModels to use sourceName
- Update all UI components to display sourceName
- Update navigation to pass sourceName instead of sourceId

## Phase 3: Cleanup

### Remove Dead Code
- Remove sourceId from all remaining models
- Remove any helper functions that convert sourceId → sourceName
- Remove sourceId from database migration scripts
- Update Supabase functions to use source_name

### Database Migration
- Create migration to drop source_id column from all tables
- Create migration to make source_name column NOT NULL
- Update Supabase functions to use source_name

### Testing
- Write unit tests for sourceName-based lookups
- Write integration tests for sync with sourceName
- Verify all features work with sourceName-only approach

## Execution Order
1. Start with Book model (most referenced)
2. Update BaseBook/BookBase interfaces
3. Update all repositories
4. Update all other domain models
5. Update presentation layer
6. Database migration
7. Cleanup and tests
