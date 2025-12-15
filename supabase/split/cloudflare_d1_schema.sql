-- ============================================================================
-- CLOUDFLARE D1 SCHEMA FOR COMMUNITY TRANSLATIONS
-- ============================================================================
-- This schema is for Cloudflare D1 (SQLite) database
-- Used in conjunction with R2 for storing compressed translation content
-- 
-- Setup Instructions:
-- 1. Create a D1 database: wrangler d1 create community-translations
-- 2. Run this schema: wrangler d1 execute community-translations --file=cloudflare_d1_schema.sql
-- 3. Create an R2 bucket: wrangler r2 bucket create community-translations
-- 4. Create an API token with D1 and R2 permissions
-- ============================================================================

-- Translations metadata table
-- Content is stored in R2, this table stores metadata and indexes
CREATE TABLE IF NOT EXISTS translations (
    id TEXT PRIMARY KEY,
    -- Content hash for deduplication (hash of original content)
    content_hash TEXT NOT NULL,
    -- Book hash for grouping (hash of title + author)
    book_hash TEXT NOT NULL,
    book_title TEXT NOT NULL,
    book_author TEXT DEFAULT '',
    chapter_name TEXT NOT NULL,
    chapter_number REAL DEFAULT -1,
    source_language TEXT NOT NULL,
    target_language TEXT NOT NULL,
    -- Translation engine used (openai, gemini, deepseek, etc.)
    engine_id TEXT NOT NULL,
    -- R2 object key for compressed content
    r2_object_key TEXT NOT NULL,
    -- Size metrics for compression stats
    original_size INTEGER NOT NULL,
    compressed_size INTEGER NOT NULL,
    compression_ratio REAL NOT NULL,
    -- Contributor info
    contributor_id TEXT DEFAULT '',
    contributor_name TEXT DEFAULT '',
    -- Quality metrics
    rating REAL DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    download_count INTEGER DEFAULT 0,
    -- Timestamps (milliseconds since epoch)
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- Index for deduplication lookup (most important)
CREATE INDEX IF NOT EXISTS idx_translations_content_hash 
ON translations(content_hash, target_language, engine_id);

-- Index for book-based queries
CREATE INDEX IF NOT EXISTS idx_translations_book_hash 
ON translations(book_hash);

-- Index for chapter lookup within a book
CREATE INDEX IF NOT EXISTS idx_translations_book_chapter 
ON translations(book_hash, chapter_number, target_language);

-- Index for language pair queries
CREATE INDEX IF NOT EXISTS idx_translations_languages 
ON translations(source_language, target_language);

-- Index for popular translations
CREATE INDEX IF NOT EXISTS idx_translations_downloads 
ON translations(download_count DESC);

-- Index for highly rated translations
CREATE INDEX IF NOT EXISTS idx_translations_rating 
ON translations(rating DESC);

-- Index for contributor queries
CREATE INDEX IF NOT EXISTS idx_translations_contributor 
ON translations(contributor_id);

-- Index for title search
CREATE INDEX IF NOT EXISTS idx_translations_title 
ON translations(book_title);

-- ============================================================================
-- VIEWS
-- ============================================================================

-- View for unique books with translation counts
CREATE VIEW IF NOT EXISTS v_books_with_translations AS
SELECT 
    book_hash,
    book_title,
    book_author,
    COUNT(DISTINCT id) as translation_count,
    COUNT(DISTINCT target_language) as language_count,
    SUM(download_count) as total_downloads,
    AVG(rating) as avg_rating,
    MAX(created_at) as last_updated
FROM translations
GROUP BY book_hash, book_title, book_author;

-- View for storage statistics
CREATE VIEW IF NOT EXISTS v_storage_stats AS
SELECT 
    COUNT(*) as total_translations,
    SUM(original_size) as total_original_bytes,
    SUM(compressed_size) as total_compressed_bytes,
    ROUND(AVG(compression_ratio), 3) as avg_compression_ratio,
    ROUND((1 - (CAST(SUM(compressed_size) AS REAL) / SUM(original_size))) * 100, 1) as storage_saved_percent
FROM translations;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Trigger to update updated_at on modification
CREATE TRIGGER IF NOT EXISTS update_translation_timestamp
AFTER UPDATE ON translations
FOR EACH ROW
BEGIN
    UPDATE translations SET updated_at = strftime('%s', 'now') * 1000 WHERE id = NEW.id;
END;

-- ============================================================================
-- SAMPLE QUERIES
-- ============================================================================

-- Find existing translation by content hash (deduplication check)
-- SELECT * FROM translations 
-- WHERE content_hash = ? AND target_language = ? AND engine_id = ?
-- ORDER BY rating DESC, download_count DESC
-- LIMIT 1;

-- Find translations for a book chapter
-- SELECT * FROM translations 
-- WHERE book_hash = ? AND chapter_number = ? AND target_language = ?
-- ORDER BY rating DESC, download_count DESC;

-- Get popular translations
-- SELECT * FROM translations 
-- WHERE target_language = ?
-- ORDER BY download_count DESC, rating DESC
-- LIMIT 50;

-- Search by title
-- SELECT * FROM translations 
-- WHERE book_title LIKE '%search_term%'
-- GROUP BY book_hash
-- ORDER BY download_count DESC
-- LIMIT 50;

-- Get storage statistics
-- SELECT * FROM v_storage_stats;

-- ============================================================================
-- NOTES
-- ============================================================================
-- 
-- R2 Object Key Format:
-- translations/{book_hash}/{language}/{chapter}_{engine}_{uuid}.bin
--
-- Example:
-- translations/a1b2c3d4e5f6/en/1_0_openai_abc123.bin
--
-- Compression:
-- - Dictionary-based compression for common words
-- - Run-length encoding for repeated characters
-- - Typical compression ratio: 0.5-0.7 (30-50% savings)
--
-- Free Tier Limits:
-- - D1: 5GB storage, 25 billion row reads/month
-- - R2: 10GB storage, 10 million Class A ops, 1 million Class B ops
--
-- ============================================================================
