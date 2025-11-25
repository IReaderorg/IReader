# Split Schema for Multiple Supabase Projects

This directory contains split schema files to distribute your database across multiple Supabase free-tier projects (500MB each).

## Architecture Overview

The schema is split into 7 separate projects for maximum storage:

1. **Project 1 (Auth)**: User authentication and profiles only
2. **Project 2 (Reading)**: Reading progress tracking
3. **Project 3 (Library)**: Synced books library
4. **Project 4 (Book Reviews)**: Book reviews only
5. **Project 5 (Chapter Reviews)**: Chapter reviews only
6. **Project 6 (Badges)**: Badge system and NFT integration
7. **Project 7 (Analytics)**: Leaderboard and statistics

**Total Storage: 3.5GB (7 × 500MB)**

## Setup Instructions

### Project 1 - Auth (Primary Database)
```sql
-- File: schema_1_auth.sql
```
- users table (authentication and profiles)

### Project 2 - Reading
```sql
-- File: schema_2_reading.sql
```
- reading_progress table (current reading positions)

### Project 3 - Library
```sql
-- File: schema_3_library.sql
```
- synced_books table (favorite books)

### Project 4 - Book Reviews
```sql
-- File: schema_4_book_reviews.sql
```
- book_reviews table (dedicated 500MB)

### Project 5 - Chapter Reviews
```sql
-- File: schema_5_chapter_reviews.sql
```
- chapter_reviews table (dedicated 500MB)

### Project 6 - Badges
```sql
-- File: schema_6_badges.sql
```
- badges table
- user_badges table
- payment_proofs table
- nft_wallets table

### Project 7 - Analytics
```sql
-- File: schema_7_analytics.sql
```
- leaderboard table
- Statistics views and functions

## Important Notes

### Cross-Project References
Since tables are in different databases, you'll need to:

1. **Store user_id as TEXT** in Projects 2 and 3 (instead of UUID foreign key)
2. **Use API calls** to sync data between projects
3. **Implement application-level joins** instead of database joins

### Application Changes Required

Your app will need to:
- Connect to multiple Supabase projects
- Sync user_id across all projects when a user signs up
- Fetch data from multiple sources and combine in the app
- Handle eventual consistency between projects

### Environment Variables

```env
# Project 1 - Auth
SUPABASE_AUTH_URL=https://xxx1.supabase.co
SUPABASE_AUTH_KEY=xxx1

# Project 2 - Reading
SUPABASE_READING_URL=https://xxx2.supabase.co
SUPABASE_READING_KEY=xxx2

# Project 3 - Library
SUPABASE_LIBRARY_URL=https://xxx3.supabase.co
SUPABASE_LIBRARY_KEY=xxx3

# Project 4 - Book Reviews
SUPABASE_BOOK_REVIEWS_URL=https://xxx4.supabase.co
SUPABASE_BOOK_REVIEWS_KEY=xxx4

# Project 5 - Chapter Reviews
SUPABASE_CHAPTER_REVIEWS_URL=https://xxx5.supabase.co
SUPABASE_CHAPTER_REVIEWS_KEY=xxx5

# Project 6 - Badges
SUPABASE_BADGES_URL=https://xxx6.supabase.co
SUPABASE_BADGES_KEY=xxx6

# Project 7 - Analytics
SUPABASE_ANALYTICS_URL=https://xxx7.supabase.co
SUPABASE_ANALYTICS_KEY=xxx7
```

## Data Distribution Strategy

### High-Volume Tables (Prioritize splitting these)
- **reading_progress**: ~1KB per book per user → Project 1
- **synced_books**: ~500 bytes per book per user → Project 1
- **book_reviews**: ~2KB per review → Project 2
- **chapter_reviews**: ~1KB per review → Project 2
- **leaderboard**: ~200 bytes per user → Project 3

### Estimated Capacity
With 500MB per project:
- Project 1 (Auth): ~5M user profiles
- Project 2 (Reading): ~2.5M reading progress entries
- Project 3 (Library): ~5M synced books
- Project 4 (Book Reviews): ~250K book reviews (dedicated)
- Project 5 (Chapter Reviews): ~500K chapter reviews (dedicated)
- Project 6 (Badges): ~10M badge assignments + NFT data
- Project 7 (Analytics): ~2.5M leaderboard entries

**Total: 3.5GB storage, supporting millions of records**

## Migration Path

If you're already using the single schema:

1. Create 7 new Supabase projects
2. Run each split schema file in its respective project
3. Export data from your current project
4. Import data into the appropriate new projects
5. Update your app to use the MultiSupabaseClient (7 projects version)
6. Test thoroughly before switching production traffic

## Pros and Cons

### Pros
✓ 3.5GB total storage (7 × 500MB) - 7x single project!
✓ Maximum isolation of concerns
✓ Can scale individual components independently
✓ Free tier benefits × 7
✓ Each major table gets dedicated 500MB
✓ Better performance per table (smaller indexes)
✓ Reviews can scale independently (book vs chapter)

### Cons
✗ No database-level foreign keys between projects
✗ No cross-project transactions
✗ More complex application code
✗ Need to manage 7 separate projects
✗ Potential data consistency issues
✗ More API calls = higher latency
✗ More environment variables to manage

## Alternative: Single Project Optimization

Before splitting, consider:
1. Enable Row Level Security to reduce overhead
2. Use JSONB columns for flexible data
3. Archive old data periodically
4. Compress large text fields
5. Use Supabase Storage for images/files instead of database
