# Split Schema for Multiple Supabase Projects

This directory contains split schema files to distribute your database across multiple Supabase free-tier projects (500MB each).

## Architecture Overview

The schema is split into 11 separate projects for maximum storage:

1. **Project 1 (Auth)**: User authentication and profiles only
2. **Project 2 (Reading)**: Reading progress tracking
3. **Project 3 (Library)**: Synced books library
4. **Project 4 (Book Reviews)**: Book reviews only
5. **Project 5 (Chapter Reviews)**: Chapter reviews only
6. **Project 6 (Badges)**: Badge system and NFT integration
7. **Project 7 (Analytics)**: Leaderboard and statistics
8. **Project 8 (Quotes)**: Community quotes system
9. **Project 9 (Community Source)**: Community books and translations
10. **Project 10 (Glossary)**: Glossary sync system
11. **Project 11 (Character Art)**: Character art gallery

**Total Storage: 5.5GB (11 × 500MB)**

## Schema Files

| File | Project | Tables |
|------|---------|--------|
| `schema_1_auth.sql` | Auth | users |
| `schema_2_reading.sql` | Reading | reading_progress |
| `schema_3_library.sql` | Library | synced_books |
| `schema_4_book_reviews.sql` | Book Reviews | book_reviews |
| `schema_5_chapter_reviews.sql` | Chapter Reviews | chapter_reviews |
| `schema_6_badges.sql` | Badges | badges, user_badges, payment_proofs, nft_wallets |
| `schema_7_analytics.sql` | Analytics | leaderboard |
| `schema_8_quotes.sql` | Quotes | community_quotes, quote_likes, daily_quote_history |
| `schema_9_community_source.sql` | Community Source | community_books, community_chapters, chapter_reports, chapter_ratings |
| `schema_10_glossary.sql` | Glossary | glossary_entries, community_glossaries |
| `schema_11_character_art.sql` | Character Art | character_art, character_art_likes, character_art_reports |

## Setup Instructions

1. Create 11 Supabase projects (free tier)
2. Run each schema file in its respective project's SQL Editor
3. Note down each project's URL and anon key
4. Configure your app with all 11 project credentials

## Important Notes

### Cross-Project References
Since tables are in different databases:

1. **Store user_id as TEXT** in all projects (instead of UUID foreign key)
2. **Use API calls** to sync data between projects
3. **Implement application-level joins** instead of database joins

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

# Project 8 - Quotes
SUPABASE_QUOTES_URL=https://xxx8.supabase.co
SUPABASE_QUOTES_KEY=xxx8

# Project 9 - Community Source
SUPABASE_COMMUNITY_URL=https://xxx9.supabase.co
SUPABASE_COMMUNITY_KEY=xxx9

# Project 10 - Glossary
SUPABASE_GLOSSARY_URL=https://xxx10.supabase.co
SUPABASE_GLOSSARY_KEY=xxx10

# Project 11 - Character Art
SUPABASE_CHARACTER_ART_URL=https://xxx11.supabase.co
SUPABASE_CHARACTER_ART_KEY=xxx11
```

## Estimated Capacity

With 500MB per project:
- Project 1 (Auth): ~5M user profiles
- Project 2 (Reading): ~2.5M reading progress entries
- Project 3 (Library): ~5M synced books
- Project 4 (Book Reviews): ~250K book reviews
- Project 5 (Chapter Reviews): ~500K chapter reviews
- Project 6 (Badges): ~10M badge assignments
- Project 7 (Analytics): ~2.5M leaderboard entries
- Project 8 (Quotes): ~500K quotes
- Project 9 (Community Source): ~100K books with chapters
- Project 10 (Glossary): ~1M glossary entries
- Project 11 (Character Art): ~500K art submissions

**Total: 5.5GB storage, supporting millions of records**

## Pros and Cons

### Pros
✓ 5.5GB total storage (11 × 500MB)
✓ Maximum isolation of concerns
✓ Can scale individual components independently
✓ Free tier benefits × 11
✓ Each feature gets dedicated storage
✓ Better performance per table

### Cons
✗ No database-level foreign keys between projects
✗ No cross-project transactions
✗ More complex application code
✗ Need to manage 11 separate projects
✗ More API calls = higher latency

## Combining Schemas

You can combine multiple schemas into a single Supabase project if you don't need maximum storage. Some logical groupings:

### Option A: 3 Projects (1.5GB)
| Project | Schemas | Tables |
|---------|---------|--------|
| Main | 1, 2, 3, 7 | users, reading_progress, synced_books, leaderboard |
| Social | 4, 5, 8 | book_reviews, chapter_reviews, quotes |
| Community | 6, 9, 10, 11 | badges, community_source, glossary, character_art |

### Option B: 5 Projects (2.5GB)
| Project | Schemas | Tables |
|---------|---------|--------|
| Auth | 1 | users |
| Core | 2, 3, 7 | reading_progress, synced_books, leaderboard |
| Reviews | 4, 5 | book_reviews, chapter_reviews |
| Badges | 6 | badges, user_badges, payment_proofs, nft_wallets |
| Community | 8, 9, 10, 11 | quotes, community_source, glossary, character_art |

### How to Combine
Simply run multiple schema files in the same Supabase SQL Editor:
```sql
-- Run schema_1_auth.sql first
-- Then run schema_2_reading.sql in the same project
-- Continue with other schemas you want to combine
```

Note: When combining schemas, you can change `user_id TEXT` back to `user_id UUID REFERENCES public.users(id)` for proper foreign key relationships within the same project.

## Alternative: Single Project

If you prefer simplicity, use the main `schema.sql` file in the parent directory which contains all tables in a single database.
