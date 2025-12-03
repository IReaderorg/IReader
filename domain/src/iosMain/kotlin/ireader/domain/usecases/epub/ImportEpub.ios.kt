package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri

/**
 * iOS implementation of ImportEpub
 * 
 * TODO: Full implementation using okio and XML parsing
 */
actual class ImportEpub {
    
    actual suspend fun parse(uris: List<Uri>) {
        // TODO: Implement EPUB parsing for iOS
    }
    
    actual fun getCacheSize(): String {
        return "0 B"
    }
    
    actual fun removeCache() {
        // TODO: Implement cache removal
    }
}
