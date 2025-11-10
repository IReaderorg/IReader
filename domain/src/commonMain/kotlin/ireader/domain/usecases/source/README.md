# Smart Source Switching

This module implements the Smart Source Switching feature that automatically detects when better sources are available for a book and allows users to migrate seamlessly.

## Components

### Use Cases

#### CheckSourceAvailabilityUseCase
Compares chapter counts across all installed sources to find better alternatives.

**Features:**
- Searches all installed sources for the same book
- Compares chapter counts
- Caches results for 24 hours to avoid repeated checks
- Respects dismissal periods (7 days)
- Requires minimum 5 chapter difference to suggest switching

**Usage:**
```kotlin
val result = checkSourceAvailabilityUseCase(bookId)
result.onSuccess { comparison ->
    if (comparison != null) {
        // Better source found
        val betterSourceName = catalogStore.get(comparison.betterSourceId)?.name
        val chapterDifference = comparison.chapterDifference
    }
}
```

#### MigrateToSourceUseCase
Handles the actual migration process with progress tracking.

**Features:**
- Searches for the book in the target source
- Fetches all chapters from the new source
- Preserves read status, bookmarks, and reading progress
- Provides real-time progress updates
- Handles errors gracefully

**Usage:**
```kotlin
migrateToSourceUseCase(bookId, targetSourceId).collect { progress ->
    when {
        progress.isComplete && progress.error == null -> {
            // Migration successful
        }
        progress.isComplete && progress.error != null -> {
            // Migration failed
        }
        else -> {
            // Show progress: progress.currentStep, progress.progress
        }
    }
}
```

### Data Models

#### SourceComparison
Stores comparison results between sources.

```kotlin
data class SourceComparison(
    val bookId: Long,
    val currentSourceId: Long,
    val betterSourceId: Long?,
    val chapterDifference: Int,
    val cachedAt: Long,
    val dismissedUntil: Long? = null
)
```

### Repository

#### SourceComparisonRepository
Manages caching of source comparison results.

**Features:**
- 24-hour cache TTL
- Dismissal tracking (7 days)
- Automatic cleanup of old entries

## UI Components

### SourceSwitchingBanner
A non-intrusive banner that appears at the top of the book detail screen.

**Features:**
- Shows source name and chapter difference
- "Switch" button to initiate migration
- "Dismiss" button to hide for 7 days
- Animated appearance/disappearance

### MigrationProgressDialog
Shows real-time progress during source migration.

**Features:**
- Step-by-step progress messages
- Progress bar with percentage
- Cannot be dismissed during migration
- "Done" button appears when complete

## Integration

### BookDetailViewModel
The ViewModel manages the source switching state and orchestrates the process.

**Key Methods:**
- `checkSourceAvailability(bookId)` - Checks for better sources
- `migrateToSource()` - Initiates migration
- `dismissSourceSwitchingBanner()` - Dismisses banner for 7 days

### BookDetailScreen
The screen displays the banner and migration dialog.

**Integration Points:**
- Banner appears between ChapterBar and ChapterListFilterBar
- Migration dialog overlays the screen during migration
- Automatically refreshes book data after successful migration

## Database Schema

### sourceComparison Table
```sql
CREATE TABLE IF NOT EXISTS sourceComparison(
    book_id INTEGER NOT NULL PRIMARY KEY,
    current_source_id INTEGER NOT NULL,
    better_source_id INTEGER,
    chapter_difference INTEGER NOT NULL,
    cached_at INTEGER NOT NULL,
    dismissed_until INTEGER,
    FOREIGN KEY(book_id) REFERENCES book (_id) ON DELETE CASCADE
);
```

## Configuration

### Constants
- `CACHE_TTL_HOURS = 24` - Cache validity period
- `MIN_CHAPTER_DIFFERENCE = 5` - Minimum chapters to suggest switching
- `DISMISSAL_PERIOD = 7 days` - How long banner stays hidden after dismissal

## Error Handling

The implementation handles various error scenarios:
- Source not found
- Book not found in target source
- Network failures during search
- Chapter fetch failures
- Database errors

All errors are logged and user-friendly messages are displayed.

## Performance Considerations

- Background checking: Source availability is checked in the background when opening a book
- Caching: Results are cached for 24 hours to avoid repeated network calls
- Lazy loading: Only checks sources when needed
- Efficient search: Uses title matching with fuzzy logic

## Future Enhancements

Potential improvements:
- Support for manual source selection
- Batch migration for multiple books
- Source quality scoring
- Automatic migration preferences
- Migration history tracking
