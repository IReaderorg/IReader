/**
 * Adapter that wraps LNReader plugins to match the LNReaderPlugin interface.
 * This is injected into the Zipline context before loading the plugin.
 */

// Get the bridge service from Zipline
const bridge = zipline.take("bridge");

// Override fetch to use the bridge
globalThis.fetchApi = async function(url, options) {
    const response = await bridge.fetch(url, options || {});
    return {
        ok: response.ok,
        status: response.status,
        statusText: response.statusText,
        headers: response.headers,
        text: async () => response.text,
        json: async () => JSON.parse(response.text)
    };
};

// After the plugin is loaded, wrap it
function wrapPlugin(plugin) {
    return {
        // Metadata methods
        getId: async () => plugin.id || "unknown",
        getName: async () => plugin.name || "Unknown Plugin",
        getSite: async () => plugin.site || "",
        getVersion: async () => plugin.version || "1.0.0",
        getLang: async () => plugin.lang || "en",
        getIcon: async () => plugin.icon || "",
        
        // Search methods
        searchNovels: async (query, page) => {
            if (typeof plugin.searchNovels === 'function') {
                const results = await plugin.searchNovels(query, page);
                return Array.isArray(results) ? results.map(adaptNovel) : [];
            }
            return [];
        },
        
        popularNovels: async (page) => {
            if (typeof plugin.popularNovels === 'function') {
                const results = await plugin.popularNovels(page);
                return Array.isArray(results) ? results.map(adaptNovel) : [];
            }
            return [];
        },
        
        latestNovels: async (page) => {
            if (typeof plugin.latestNovels === 'function') {
                const results = await plugin.latestNovels(page);
                return Array.isArray(results) ? results.map(adaptNovel) : [];
            }
            // Fallback to popularNovels if latestNovels doesn't exist
            return this.popularNovels(page);
        },
        
        // Details methods
        getNovelDetails: async (url) => {
            if (typeof plugin.parseNovel === 'function') {
                const details = await plugin.parseNovel(url);
                return adaptNovelDetails(details);
            } else if (typeof plugin.getNovelDetails === 'function') {
                const details = await plugin.getNovelDetails(url);
                return adaptNovelDetails(details);
            }
            return {
                name: "",
                url: url,
                cover: "",
                author: null,
                description: null,
                genres: [],
                status: null
            };
        },
        
        getChapters: async (url) => {
            if (typeof plugin.parseChapters === 'function') {
                const chapters = await plugin.parseChapters(url);
                return Array.isArray(chapters) ? chapters.map(adaptChapter) : [];
            } else if (typeof plugin.getChapters === 'function') {
                const chapters = await plugin.getChapters(url);
                return Array.isArray(chapters) ? chapters.map(adaptChapter) : [];
            }
            return [];
        },
        
        getChapterContent: async (url) => {
            if (typeof plugin.parseChapter === 'function') {
                const content = await plugin.parseChapter(url);
                return typeof content === 'string' ? content : (content.text || "");
            } else if (typeof plugin.getChapterContent === 'function') {
                const content = await plugin.getChapterContent(url);
                return typeof content === 'string' ? content : (content.text || "");
            }
            return "";
        }
    };
}

// Adapt novel data
function adaptNovel(novel) {
    return {
        name: novel.name || novel.title || "",
        url: novel.url || novel.path || "",
        cover: novel.cover || novel.image || ""
    };
}

// Adapt novel details
function adaptNovelDetails(details) {
    return {
        name: details.name || details.title || "",
        url: details.url || details.path || "",
        cover: details.cover || details.image || "",
        author: details.author || null,
        description: details.description || details.summary || null,
        genres: Array.isArray(details.genres) ? details.genres : [],
        status: details.status || null
    };
}

// Adapt chapter data
function adaptChapter(chapter) {
    return {
        name: chapter.name || chapter.title || "",
        url: chapter.url || chapter.path || "",
        releaseTime: chapter.releaseTime || chapter.date || null
    };
}

// Export the wrapper function
globalThis.wrapPlugin = wrapPlugin;
