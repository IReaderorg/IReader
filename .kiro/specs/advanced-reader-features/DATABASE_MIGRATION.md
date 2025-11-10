# Database Migration Guide - Smart Source Switching

## Overview
This document describes the database changes required for the Smart Source Switching feature.

## New Table: sourceComparison

### Purpose
Caches source comparison results to avoid repeated network calls and tracks user dismissals.

### Schema
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

### Columns

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| book_id | INTEGER | NO | Primary key, references book table |
| current_source_id | INTEGER | NO | ID of the book's current source |
| better_source_id | INTEGER | YES | ID of the better source (null if none found) |
| chapter_difference | INTEGER | NO | Number of additional chapters in better source |
| cached_at | INTEGER | NO | Timestamp when comparison was cached |
| dismissed_until | INTEGER | YES | Timestamp until which banner is dismissed (null if not dismissed) |

### Indexes
```sql
CREATE INDEX IF NOT EXISTS source_comparison_cached_at_index 
ON sourceComparison(cached_at);

CREATE INDEX IF NOT EXISTS source_comparison_dismissed_until_index 
ON sourceComparison(dismissed_until);
```

### Foreign Key Constraints
- `book_id` references `book(_id)` with `ON DELETE CASCADE`
  - When a book is deleted, its source comparison cache is automatically deleted

## Migration Steps

### For SQLDelight
The schema file is already created at:
`data/src/commonMain/sqldelight/data/sourceComparison.sq`

SQLDelight will automatically generate the necessary code when you build the project.

### Manual Migration (if needed)
If you need to manually create the table:

```sql
-- Create the table
CREATE TABLE IF NOT EXISTS sourceComparison(
    book_id INTEGER NOT NULL PRIMARY KEY,
    current_source_id INTEGER NOT NULL,
    better_source_id INTEGER,
    chapter_difference INTEGER NOT NULL,
    cached_at INTEGER NOT NULL,
    dismissed_until INTEGER,
    FOREIGN KEY(book_id) REFERENCES book (_id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS source_comparison_cached_at_index 
ON sourceComparison(cached_at);

CREATE INDEX IF NOT EXISTS source_comparison_dismissed_until_index 
ON sourceComparison(dismissed_until);
```

## Data Lifecycle

### Cache Expiration
- Entries older than 24 hours are considered expired
- Expired entries are automatically ignored by the use case
- Periodic cleanup can be performed using `deleteOldEntries(timestamp)`

### Dismissal Period
- When user dismisses the banner, `dismissed_until` is set to current time + 7 days
- During this period, the banner won't be shown even if cache is valid
- After the period expires, the banner can be shown again

### Automatic Cleanup
The repository provides a method to clean up old entries:
```kotlin
suspend fun deleteOldEntries(timestamp: Long)
```

Recommended to run this periodically (e.g., on app startup or daily):
```kotlin
val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
sourceComparisonRepository.deleteOldEntries(oneDayAgo)
```

## Performance Considerations

### Indexes
Two indexes are created for optimal query performance:
1. `cached_at` - For finding expired entries
2. `dismissed_until` - For checking dismissal status

### Query Patterns
Most common queries:
1. Get comparison by book ID (uses primary key)
2. Find expired entries (uses cached_at index)
3. Check dismissal status (uses dismissed_until index)

### Storage Impact
- Minimal storage footprint
- One row per book that has been checked
- Automatic cleanup via CASCADE delete
- Recommended periodic cleanup of old entries

## Rollback Plan

If you need to rollback this feature:

```sql
-- Drop indexes
DROP INDEX IF EXISTS source_comparison_cached_at_index;
DROP INDEX IF EXISTS source_comparison_dismissed_until_index;

-- Drop table
DROP TABLE IF EXISTS sourceComparison;
```

Note: This will delete all cached comparison data, but won't affect any other data.

## Testing

### Verify Table Creation
```sql
SELECT name FROM sqlite_master 
WHERE type='table' AND name='sourceComparison';
```

### Verify Indexes
```sql
SELECT name FROM sqlite_master 
WHERE type='index' AND tbl_name='sourceComparison';
```

### Sample Queries

#### Insert a comparison
```sql
INSERT INTO sourceComparison(
    book_id, current_source_id, better_source_id, 
    chapter_difference, cached_at, dismissed_until
) VALUES (1, 100, 200, 10, 1699564800000, NULL);
```

#### Get comparison for a book
```sql
SELECT * FROM sourceComparison WHERE book_id = 1;
```

#### Find expired entries
```sql
SELECT * FROM sourceComparison 
WHERE cached_at < (strftime('%s', 'now') * 1000 - 86400000);
```

#### Check dismissal status
```sql
SELECT * FROM sourceComparison 
WHERE book_id = 1 
AND dismissed_until IS NOT NULL 
AND dismissed_until > (strftime('%s', 'now') * 1000);
```

## Monitoring

### Metrics to Track
1. Cache hit rate (how often cached data is used)
2. Average cache age
3. Number of dismissed comparisons
4. Table size growth over time

### Recommended Queries

#### Cache statistics
```sql
SELECT 
    COUNT(*) as total_entries,
    COUNT(CASE WHEN better_source_id IS NOT NULL THEN 1 END) as with_better_source,
    COUNT(CASE WHEN dismissed_until IS NOT NULL THEN 1 END) as dismissed,
    AVG((strftime('%s', 'now') * 1000) - cached_at) / 3600000 as avg_age_hours
FROM sourceComparison;
```

#### Expired entries
```sql
SELECT COUNT(*) as expired_entries
FROM sourceComparison
WHERE cached_at < (strftime('%s', 'now') * 1000 - 86400000);
```

## Troubleshooting

### Issue: Table not found
**Solution**: Ensure SQLDelight has generated the code. Run a clean build.

### Issue: Foreign key constraint failed
**Solution**: Ensure the book exists before inserting a comparison.

### Issue: Slow queries
**Solution**: Verify indexes are created. Run `ANALYZE` to update statistics.

### Issue: Table growing too large
**Solution**: Implement periodic cleanup of old entries.

## Conclusion

The sourceComparison table is a simple, efficient cache for source comparison results. With proper indexes and periodic cleanup, it provides fast lookups with minimal storage overhead.
