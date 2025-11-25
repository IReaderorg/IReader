-- SQL Script to Validate Book and Chapter URLs
-- Run this against your app's database to check for broken URLs

-- ============================================
-- 1. Check Books with Relative URLs (BROKEN)
-- ============================================
SELECT 
    'BROKEN BOOK URLs' as issue_type,
    COUNT(*) as count
FROM book 
WHERE key NOT LIKE 'http%' 
  AND key NOT LIKE 'local_%'
  AND key != '';

-- Show examples of broken book URLs
SELECT 
    id,
    title,
    key as broken_url,
    sourceId
FROM book 
WHERE key NOT LIKE 'http%' 
  AND key NOT LIKE 'local_%'
  AND key != ''
LIMIT 10;

-- ============================================
-- 2. Check Chapters with Relative URLs (BROKEN)
-- ============================================
SELECT 
    'BROKEN CHAPTER URLs' as issue_type,
    COUNT(*) as count
FROM chapter 
WHERE key NOT LIKE 'http%' 
  AND key NOT LIKE 'local_%'
  AND key != '';

-- Show examples of broken chapter URLs
SELECT 
    c.id,
    c.name,
    c.key as broken_url,
    c.bookId,
    b.title as book_title
FROM chapter c
JOIN book b ON c.bookId = b.id
WHERE c.key NOT LIKE 'http%' 
  AND c.key NOT LIKE 'local_%'
  AND c.key != ''
LIMIT 10;

-- ============================================
-- 3. Check Books with Valid URLs (GOOD)
-- ============================================
SELECT 
    'VALID BOOK URLs' as status,
    COUNT(*) as count
FROM book 
WHERE key LIKE 'http%' OR key LIKE 'local_%';

-- ============================================
-- 4. Check Chapters with Valid URLs (GOOD)
-- ============================================
SELECT 
    'VALID CHAPTER URLs' as status,
    COUNT(*) as count
FROM chapter 
WHERE key LIKE 'http%' OR key LIKE 'local_%';

-- ============================================
-- 5. Summary Report
-- ============================================
SELECT 
    'Books' as entity_type,
    COUNT(*) as total,
    SUM(CASE WHEN key LIKE 'http%' OR key LIKE 'local_%' THEN 1 ELSE 0 END) as valid,
    SUM(CASE WHEN key NOT LIKE 'http%' AND key NOT LIKE 'local_%' AND key != '' THEN 1 ELSE 0 END) as broken,
    ROUND(100.0 * SUM(CASE WHEN key LIKE 'http%' OR key LIKE 'local_%' THEN 1 ELSE 0 END) / COUNT(*), 2) as valid_percentage
FROM book
UNION ALL
SELECT 
    'Chapters' as entity_type,
    COUNT(*) as total,
    SUM(CASE WHEN key LIKE 'http%' OR key LIKE 'local_%' THEN 1 ELSE 0 END) as valid,
    SUM(CASE WHEN key NOT LIKE 'http%' AND key NOT LIKE 'local_%' AND key != '' THEN 1 ELSE 0 END) as broken,
    ROUND(100.0 * SUM(CASE WHEN key LIKE 'http%' OR key LIKE 'local_%' THEN 1 ELSE 0 END) / COUNT(*), 2) as valid_percentage
FROM chapter;

-- ============================================
-- 6. Books by Source (to identify problematic sources)
-- ============================================
SELECT 
    sourceId,
    COUNT(*) as total_books,
    SUM(CASE WHEN key LIKE 'http%' OR key LIKE 'local_%' THEN 1 ELSE 0 END) as valid_urls,
    SUM(CASE WHEN key NOT LIKE 'http%' AND key NOT LIKE 'local_%' AND key != '' THEN 1 ELSE 0 END) as broken_urls
FROM book
GROUP BY sourceId
HAVING broken_urls > 0
ORDER BY broken_urls DESC;
