# Database Optimizations Guide

This document describes the database performance optimizations implemented in IReader.

## Overview

The optimizations provide:
- **Query caching** with configurable TTL (2-10x speedup for repeated queries)
- **Batch operations** with proper transaction handling
- **Flow optimizations** (debounce, distinctUntilChanged)
- **Performance monitoring** and logging
- **Data preloading** for faster app startup
- **Periodic maintenance** (WAL checkpoint, incremental vacuum, ANALYZE)

## Components

### 1. DatabaseOptimizations

The main optimization class providing caching and batch operations.

```kotlin
// Inject via Koin
val dbOptimizations: DatabaseOptimizations by inject()

// Cached query (results cached for 60 seconds)
val books = dbOptimizations.awaitListCached(
    cacheKey = "library_books",
    ttl = DatabaseOptimizations.MEDIUM_CACHE_TTL
) {
    bookQueries.findInLibraryBooks(booksMapper)
}

// Batch insert with progress tracking
val result = dbOptimizations.executeBatchChunked(
    items = chapters,
    chunkSize = 100,
    operationName = "insert_chapters",
    onProgress = { processed, total -> 
        Log.info("Progress: $processed/$total")
    }
) { chapter ->
    chapterQueries.upsert(/* params */)
}

// Optimized Flow subscription
val flow = dbOptimizations.subscribeOptimized("library_updates") {
    bookQueries.findInLibraryBooks(booksMapper)
}
```

### 2. DatabasePreloader

Preloads critical data during app startup.

```kotlin
// In Application.onCreate or similar
val preloader: DatabasePreloader by inject()

// Async preload (non-blocking)
preloader.preloadAsync()

// Or wait for completion
lifecycleScope.launch {
    preloader.preloadCriticalData()
}

// Preload book data when navigating to detail screen
preloader.preloadBookData(bookId)
```

### 3. DatabaseMaintenance

Periodic maintenance operations.

```kotlin
val maintenance = DatabaseMaintenance(handler, dbOptimizations)

// Regular maintenance (safe to run frequently)
val result = maintenance.runMaintenance()

// Deep maintenance (run monthly)
val deepResult = maintenance.runDeepMaintenance()
```

### 4. AndroidDatabaseOptimizations (Android only)

Android-specific SQLite optimizations.

```kotlin
// Applied automatically in DatabaseDriverFactory
// Includes:
// - WAL mode
// - NORMAL synchronous mode
// - 64MB cache
// - Memory-mapped I/O
// - Periodic WAL checkpoint
// - Incremental vacuum
```

## Cache TTL Constants

```kotlin
DatabaseOptimizations.SHORT_CACHE_TTL   // 10 seconds
DatabaseOptimizations.MEDIUM_CACHE_TTL  // 1 minute
DatabaseOptimizations.LONG_CACHE_TTL    // 5 minutes
```

## Performance Monitoring

```kotlin
// Get performance report
val report = dbOptimizations.getStats()
println("Cache hit rate: ${report.cacheHitRate}%")
println("Slow queries: ${report.slowQueryCount}")

// Log full report
dbOptimizations.logPerformanceReport()
```

## Cache Invalidation

Always invalidate cache after write operations:

```kotlin
// After inserting/updating books
dbOptimizations.invalidateCache("library_books")

// After chapter updates for a specific book
dbOptimizations.invalidateCache("book_${bookId}_chapters")

// Clear all cache
dbOptimizations.clearAllCache()
```

## SQLDelight Indexes

New indexes added for common query patterns:

### book.sq
- `idx_book_favorite_title` - Library sorted by title
- `idx_book_favorite_date_added` - Library sorted by date added
- `idx_book_favorite_last_update` - Library sorted by last update
- `idx_book_pinned` - Pinned books
- `idx_book_archived` - Archived books

### chapter.sq
- `idx_chapter_book_source_order` - Chapters sorted by source order
- `idx_chapter_book_read_number` - Unread chapters by number
- `idx_chapter_has_content` - Downloaded chapters

### history.sq
- `idx_history_last_read_active` - Recent history
- `idx_history_time_read` - Reading time tracking

## Best Practices

1. **Use caching for read-heavy operations**
   - Library book list
   - Category list
   - Chapter lists (without content)

2. **Invalidate cache after writes**
   - Always call `invalidateCache()` after insert/update/delete

3. **Use batch operations for bulk updates**
   - Chapter inserts
   - Read status updates
   - Bookmark updates

4. **Use lightweight queries for lists**
   - Use `*Light` queries that exclude large fields like `content`

5. **Preload data proactively**
   - Call `preloadBookData()` when user taps on a book
   - Preload next chapter while reading

6. **Monitor performance**
   - Check `logPerformanceReport()` periodically
   - Watch for slow queries (>100ms)

## Migration Notes

Existing repositories have been updated to use optimizations:
- `BookRepositoryImpl` - Uses cached queries for library
- `ChapterRepositoryImpl` - Uses cached queries, debounced flows
- `HistoryRepositoryImpl` - Uses debounced flows

The optimizations are optional - if `DatabaseOptimizations` is not injected,
repositories fall back to standard queries.
