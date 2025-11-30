/**
 * LNReader Plugin with Filters Example
 * 
 * This plugin demonstrates how to implement filters for browsing novels.
 * Filters allow users to refine their search by status, genre, sort order, etc.
 */

// Plugin Metadata
const id = 'filter-example';
const name = 'Filter Example Plugin';
const version = '1.0.0';
const site = 'https://example.com';
const lang = 'en';
const icon = 'https://via.placeholder.com/96';

// Filter Definitions
const filters = {
    // Picker filter: Dropdown selection
    status: {
        type: 'Picker',
        label: 'Status',
        options: [
            { label: 'All', value: 'all' },
            { label: 'Ongoing', value: 'ongoing' },
            { label: 'Completed', value: 'completed' },
            { label: 'Hiatus', value: 'hiatus' }
        ],
        defaultValue: 'all'
    },
    
    // Picker filter: Sort order
    sort: {
        type: 'Picker',
        label: 'Sort By',
        options: [
            { label: 'Latest Updates', value: 'latest' },
            { label: 'Most Popular', value: 'popular' },
            { label: 'Highest Rated', value: 'rating' },
            { label: 'Most Views', value: 'views' },
            { label: 'Alphabetical', value: 'alpha' }
        ],
        defaultValue: 'latest'
    },
    
    // TextInput filter: Author search
    author: {
        type: 'TextInput',
        label: 'Author Name',
        defaultValue: ''
    },
    
    // CheckboxGroup filter: Multiple genre selection
    genres: {
        type: 'CheckboxGroup',
        label: 'Genres',
        options: [
            { label: 'Action', value: 'action' },
            { label: 'Adventure', value: 'adventure' },
            { label: 'Comedy', value: 'comedy' },
            { label: 'Drama', value: 'drama' },
            { label: 'Fantasy', value: 'fantasy' },
            { label: 'Horror', value: 'horror' },
            { label: 'Mystery', value: 'mystery' },
            { label: 'Romance', value: 'romance' },
            { label: 'Sci-Fi', value: 'scifi' },
            { label: 'Slice of Life', value: 'slice-of-life' }
        ],
        defaultValues: []
    },
    
    // ExcludableCheckboxGroup filter: Include/Exclude genres
    genresAdvanced: {
        type: 'ExcludableCheckboxGroup',
        label: 'Advanced Genre Filter',
        options: [
            { label: 'Action', value: 'action' },
            { label: 'Romance', value: 'romance' },
            { label: 'Horror', value: 'horror' },
            { label: 'Tragedy', value: 'tragedy' }
        ],
        included: [],
        excluded: []
    }
};

/**
 * Fetch popular novels with filter support
 */
async function popularNovels(page, { filters: userFilters }) {
    // Extract filter values
    const status = userFilters.status || 'all';
    const sort = userFilters.sort || 'latest';
    const author = userFilters.author || '';
    const genres = userFilters.genres || [];
    const genresAdvanced = userFilters.genresAdvanced || { included: [], excluded: [] };
    
    // Build query URL based on filters
    // In a real plugin, you would construct the actual API/page URL
    let url = `${site}/novels?page=${page}`;
    
    if (status !== 'all') {
        url += `&status=${status}`;
    }
    
    if (sort) {
        url += `&sort=${sort}`;
    }
    
    if (author) {
        url += `&author=${encodeURIComponent(author)}`;
    }
    
    if (genres.length > 0) {
        url += `&genres=${genres.join(',')}`;
    }
    
    if (genresAdvanced.included.length > 0) {
        url += `&include=${genresAdvanced.included.join(',')}`;
    }
    
    if (genresAdvanced.excluded.length > 0) {
        url += `&exclude=${genresAdvanced.excluded.join(',')}`;
    }
    
    // Fetch and parse the page
    // For this example, we return mock data
    console.log('Fetching with filters:', url);
    
    // Mock data that varies based on filters
    const mockNovels = [
        {
            name: `Novel (${status}, ${sort})`,
            path: '/novel/filtered-1',
            cover: 'https://via.placeholder.com/300x400'
        },
        {
            name: `Another Novel (${genres.join(', ')})`,
            path: '/novel/filtered-2',
            cover: 'https://via.placeholder.com/300x400'
        }
    ];
    
    // Filter by author if specified
    if (author) {
        return mockNovels.filter(novel => 
            novel.name.toLowerCase().includes(author.toLowerCase())
        );
    }
    
    return mockNovels;
}

/**
 * Search for novels
 */
async function searchNovels(searchTerm, page) {
    // In a real plugin, construct search URL
    const url = `${site}/search?q=${encodeURIComponent(searchTerm)}&page=${page}`;
    
    console.log('Searching:', url);
    
    return [
        {
            name: `Search Result: ${searchTerm}`,
            path: '/novel/search-result',
            cover: 'https://via.placeholder.com/300x400'
        }
    ];
}

/**
 * Parse novel details
 */
async function parseNovel(novelPath) {
    return {
        name: 'Filtered Novel',
        path: novelPath,
        cover: 'https://via.placeholder.com/300x400',
        summary: 'This novel was found using filters.',
        author: 'Filter Author',
        genres: 'Action, Fantasy',
        status: 'Ongoing',
        chapters: [
            {
                name: 'Chapter 1',
                path: '/chapter/1',
                releaseTime: '2024-01-01'
            }
        ]
    };
}

/**
 * Parse chapter content
 */
async function parseChapter(chapterPath) {
    return '<p>Chapter content goes here.</p>';
}
