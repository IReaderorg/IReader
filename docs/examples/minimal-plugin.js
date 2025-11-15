/**
 * Minimal LNReader Plugin Example
 * 
 * This is the simplest possible plugin that demonstrates the required structure.
 * It returns hardcoded data for demonstration purposes.
 */

// Plugin Metadata (Required)
const id = 'minimal-example';
const name = 'Minimal Example';
const version = '1.0.0';
const site = 'https://example.com';
const lang = 'en';
const icon = 'https://via.placeholder.com/96';

/**
 * Fetch popular novels
 * @param {number} page - Page number (1-indexed)
 * @param {object} options - Options object containing filters
 * @returns {Promise<Array>} Array of NovelItem objects
 */
async function popularNovels(page, { filters }) {
    // In a real plugin, you would fetch data from the source website
    // For this example, we return hardcoded data
    
    return [
        {
            name: 'Example Novel 1',
            path: '/novel/example-novel-1',
            cover: 'https://via.placeholder.com/300x400'
        },
        {
            name: 'Example Novel 2',
            path: '/novel/example-novel-2',
            cover: 'https://via.placeholder.com/300x400'
        },
        {
            name: 'Example Novel 3',
            path: '/novel/example-novel-3',
            cover: 'https://via.placeholder.com/300x400'
        }
    ];
}

/**
 * Search for novels
 * @param {string} searchTerm - Search query
 * @param {number} page - Page number (1-indexed)
 * @returns {Promise<Array>} Array of NovelItem objects
 */
async function searchNovels(searchTerm, page) {
    // Filter novels by search term (in a real plugin, this would query the API)
    const allNovels = [
        {
            name: 'Example Novel 1',
            path: '/novel/example-novel-1',
            cover: 'https://via.placeholder.com/300x400'
        },
        {
            name: 'Another Novel',
            path: '/novel/another-novel',
            cover: 'https://via.placeholder.com/300x400'
        }
    ];
    
    // Simple search filter
    return allNovels.filter(novel => 
        novel.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
}

/**
 * Parse novel details and chapters
 * @param {string} novelPath - Novel path from NovelItem
 * @returns {Promise<object>} SourceNovel object with full details
 */
async function parseNovel(novelPath) {
    // In a real plugin, you would fetch and parse the novel page
    
    return {
        name: 'Example Novel',
        path: novelPath,
        cover: 'https://via.placeholder.com/300x400',
        summary: 'This is an example novel description. In a real plugin, this would be scraped from the source website.',
        author: 'Example Author',
        artist: 'Example Artist',
        genres: 'Action, Fantasy, Adventure',
        status: 'Ongoing',
        chapters: [
            {
                name: 'Chapter 1: The Beginning',
                path: '/chapter/1',
                releaseTime: '2024-01-01'
            },
            {
                name: 'Chapter 2: The Journey',
                path: '/chapter/2',
                releaseTime: '2024-01-02'
            },
            {
                name: 'Chapter 3: The Discovery',
                path: '/chapter/3',
                releaseTime: '2024-01-03'
            }
        ]
    };
}

/**
 * Parse chapter content
 * @param {string} chapterPath - Chapter path from ChapterItem
 * @returns {Promise<string>} HTML content of the chapter
 */
async function parseChapter(chapterPath) {
    // In a real plugin, you would fetch and parse the chapter page
    
    return `
        <h1>Chapter Title</h1>
        <p>This is the first paragraph of the chapter.</p>
        <p>This is the second paragraph with more content.</p>
        <p>In a real plugin, this content would be scraped from the source website.</p>
    `;
}
