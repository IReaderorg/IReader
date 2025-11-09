package ireader.domain.usecases.fonts

import ireader.domain.models.fonts.CustomFont

/**
 * Simple cache for loaded fonts to avoid reloading
 */
object FontCache {
    private val cache = mutableMapOf<String, CustomFont>()
    
    fun get(fontId: String): CustomFont? {
        return cache[fontId]
    }
    
    fun put(fontId: String, font: CustomFont) {
        cache[fontId] = font
    }
    
    fun remove(fontId: String) {
        cache.remove(fontId)
    }
    
    fun clear() {
        cache.clear()
    }
}
