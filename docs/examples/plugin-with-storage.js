/**
 * LNReader Plugin with Storage Example
 * 
 * This plugin demonstrates how to use the Storage API for caching and persistence.
 * Storage is useful for:
 * - Caching API responses to reduce network requests
 * - Storing user preferences
 * - Maintaining session data
 */

// Plugin Metadata
const id = 'storage-example';
const name = 'Storage Example Plugin';
const version = '1.0.0';
const site = 'https://example.com';
const lang = 'en';
const icon = 'https://via.placeholder.com/96';

// Cache duration: 1 hour (in milliseconds)
const CACHE_DURATION = 60 * 60 * 1000;

/**
 * Fetch popular novels with caching
 */
async function popularNovels(page, { filters }) {
    // Create cache key based on page and filters
    const cacheKey = `popular_${page}_${JSON.stringify(filters)}`;
    
    // Try to get cached data
    const cached = await storage.get(cacheKey);
    if (cached) {
        console.log('Returning cached popular novels');
        return cached;
    }
    
    // Fetch fresh data
    console.log('Fetching fresh popular novels');
    const url = `${site}/novels/popular?page=${page}`;
    
    // In a real plugin, you would fetch and parse the actual page
    const novels = [
        {
            name: 'Cached Novel 1',
            path: '/novel/cached-1',
            cover: 'https://via.placeholder.com/300x400'
        },
        {
            name: 'Cached Novel 2',
            path: '/novel/cached-2',
            cover: 'https://via.placeholder.com/300x400'
        }
    ];
    
    // Store in cache with expiration
    await storage.set(cacheKey, novels, CACHE_DURATION);
    
    return novels;
}

/**
 * Search for novels with search history
 */
async function searchNovels(searchTerm, page) {
    // Save search term to history
    await addToSearchHistory(searchTerm);
    
    // Perform search
    const url = `${site}/search?q=${encodeURIComponent(searchTerm)}&page=${page}`;
    
    console.log('Searching:', url);
    
    return [
        {
            name: `Result for: ${searchTerm}`,
            path: '/novel/search-result',
            cover: 'https://via.placeholder.com/300x400'
        }
    ];
}

/**
 * Parse novel details with caching
 */
async function parseNovel(novelPath) {
    // Check cache first
    const cacheKey = `novel_${novelPath}`;
    const cached = await storage.get(cacheKey);
    
    if (cached) {
        console.log('Returning cached novel details');
        return cached;
    }
    
    // Fetch fresh data
    console.log('Fetching fresh novel details');
    const url = `${site}${novelPath}`;
    
    // In a real plugin, fetch and parse the page
    const novel = {
        name: 'Cached Novel Details',
        path: novelPath,
        cover: 'https://via.placeholder.com/300x400',
        summary: 'This novel data is cached for better performance.',
        author: 'Cache Author',
        genres: 'Action, Fantasy',
        status: 'Ongoing',
        chapters: [
            {
                name: 'Chapter 1',
                path: '/chapter/1',
                releaseTime: '2024-01-01'
            },
            {
                name: 'Chapter 2',
                path: '/chapter/2',
                releaseTime: '2024-01-02'
            }
        ]
    };
    
    // Cache for 1 hour
    await storage.set(cacheKey, novel, CACHE_DURATION);
    
    return novel;
}

/**
 * Parse chapter content with caching
 */
async function parseChapter(chapterPath) {
    // Check cache
    const cacheKey = `chapter_${chapterPath}`;
    const cached = await storage.get(cacheKey);
    
    if (cached) {
        console.log('Returning cached chapter content');
        return cached;
    }
    
    // Fetch fresh content
    console.log('Fetching fresh chapter content');
    const url = `${site}${chapterPath}`;
    
    // In a real plugin, fetch and parse the page
    const content = `
        <h1>Cached Chapter</h1>
        <p>This chapter content is cached for offline reading.</p>
        <p>Cache helps reduce network requests and improves performance.</p>
    `;
    
    // Cache chapter content (no expiration for chapters)
    await storage.set(cacheKey, content);
    
    return content;
}

/**
 * Helper: Add search term to history
 */
async function addToSearchHistory(searchTerm) {
    const historyKey = 'search_history';
    const maxHistorySize = 10;
    
    // Get existing history
    let history = await storage.get(historyKey) || [];
    
    // Remove duplicates and add new term at the beginning
    history = history.filter(term => term !== searchTerm);
    history.unshift(searchTerm);
    
    // Limit history size
    if (history.length > maxHistorySize) {
        history = history.slice(0, maxHistorySize);
    }
    
    // Save updated history
    await storage.set(historyKey, history);
    
    console.log('Search history updated:', history);
}

/**
 * Helper: Get search history
 */
async function getSearchHistory() {
    return await storage.get('search_history') || [];
}

/**
 * Helper: Clear all cache
 */
async function clearCache() {
    // Get all keys
    const keys = await storage.getAllKeys();
    
    // Delete cache keys (but keep search history)
    for (const key of keys) {
        if (key !== 'search_history') {
            await storage.delete(key);
        }
    }
    
    console.log('Cache cleared');
}

/**
 * Helper: Get cache statistics
 */
async function getCacheStats() {
    const keys = await storage.getAllKeys();
    
    const stats = {
        totalKeys: keys.length,
        cacheKeys: keys.filter(k => k.startsWith('popular_') || k.startsWith('novel_') || k.startsWith('chapter_')).length,
        searchHistory: keys.includes('search_history')
    };
    
    console.log('Cache stats:', stats);
    return stats;
}
