# Popular Books Cover Images — Setup Guide

## Problem

Popular books screen shows no cover images because the `synced_books` table in Supabase has no `cover_url` column. Cover data is never stored or returned.

## Prerequisites

- Access to your Supabase project dashboard
- SQL Editor access

## Step 1: Add `cover_url` Column

In the Supabase SQL Editor, run:

```sql
ALTER TABLE public.synced_books ADD COLUMN IF NOT EXISTS cover_url TEXT DEFAULT '';
```

## Step 2: Update the RPC Function

The existing `get_popular_books` function must be dropped and recreated because its return type changes (adds `cover_url` column).

Run these queries **in order**:

```sql
-- Drop the old function first
DROP FUNCTION IF EXISTS get_popular_books(INTEGER);

-- Recreate with cover_url in return type
CREATE OR REPLACE FUNCTION get_popular_books(p_limit INTEGER DEFAULT 50)
RETURNS TABLE (
    book_id TEXT,
    title TEXT,
    book_url TEXT,
    source_id BIGINT,
    reader_count INTEGER,
    last_read BIGINT,
    cover_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        sb.book_id,
        sb.title,
        sb.book_url,
        sb.source_id,
        COUNT(DISTINCT sb.user_id)::INTEGER AS reader_count,
        MAX(sb.last_read) AS last_read,
        COALESCE(MAX(sb.cover_url), '') AS cover_url
    FROM public.synced_books sb
    GROUP BY sb.book_id, sb.title, sb.book_url, sb.source_id
    ORDER BY reader_count DESC, last_read DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Re-grant permissions
GRANT EXECUTE ON FUNCTION get_popular_books(INTEGER) TO authenticated;
GRANT EXECUTE ON FUNCTION get_popular_books(INTEGER) TO anon;
```

## Step 3: Verify

1. Add a book to your library (or re-add one) so the cover URL gets synced
2. Open the **Popular Books** screen
3. Cover images should now appear in the hero card, trending rail, ranked list, and detail sheet

## How It Works

| Step | What Happens |
|------|-------------|
| Book added to library | `SyncBooksUseCase` sends `cover_url` to Supabase in the `synced_books` row |
| Popular Books loaded | `get_popular_books` RPC returns `cover_url` (best cover via `MAX()`) |
| UI renders | `AsyncImage` (Coil 3) loads the cover URL; no image if `coverUrl` is null |

## Existing Books

Books already in the library before this migration will have an empty `cover_url`. They will show covers once:
- The book is re-opened and re-synced, **or**
- You run a bulk update (optional):

```sql
-- Optional: populate cover_url for existing books from local data
-- This only works if you know the cover URLs; otherwise covers populate on next sync
```

## Files Changed

| File | Change |
|------|--------|
| `domain/.../SyncedBook.kt` | Added `coverUrl` field |
| `domain/.../SyncBooksUseCase.kt` | Passes `book.cover` when syncing |
| `data/.../SupabaseRemoteRepository.kt` | Added `cover_url` to DTO, upsert payload, converters |
| `data/.../PopularBooksRepositoryImpl.kt` | Added `cover_url` to DTO and mapping |
| `supabase/schema.sql` | Added column + updated RPC function |
| `supabase/split/schema_3_library.sql` | Added column definition |
| `supabase/migrations/003_synced_books_cover_url.sql` | Migration script |
