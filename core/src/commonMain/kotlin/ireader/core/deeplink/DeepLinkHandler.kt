package ireader.core.deeplink

import ireader.core.log.IReaderLog

/**
 * Deep link handler for processing external URLs and content links
 * Supports various URL schemes for manga/book content
 */
class DeepLinkHandler {
    
    private val handlers = mutableMapOf<String, (DeepLink) -> Unit>()
    
    /**
     * Register a handler for a specific deep link type
     */
    fun registerHandler(type: DeepLinkType, handler: (DeepLink) -> Unit) {
        handlers[type.name] = handler
        IReaderLog.info("Registered deep link handler for ${type.name}", tag = "DeepLink")
    }
    
    /**
     * Parse and handle a deep link URL
     */
    fun handleDeepLink(url: String): Boolean {
        val deepLink = parseDeepLink(url) ?: return false
        
        IReaderLog.info("Handling deep link: ${deepLink.type} - ${deepLink.url}", tag = "DeepLink")
        
        val handler = handlers[deepLink.type.name]
        if (handler != null) {
            try {
                handler(deepLink)
                return true
            } catch (e: Exception) {
                IReaderLog.error("Error handling deep link", e, tag = "DeepLink")
                return false
            }
        }
        
        IReaderLog.warn("No handler registered for deep link type: ${deepLink.type}", tag = "DeepLink")
        return false
    }
    
    /**
     * Parse a URL into a DeepLink object
     */
    private fun parseDeepLink(url: String): DeepLink? {
        return when {
            // IReader custom scheme: ireader://
            url.startsWith("ireader://") -> parseIReaderScheme(url)
            
            // HTTP/HTTPS URLs
            url.startsWith("http://") || url.startsWith("https://") -> parseHttpUrl(url)
            
            // Content URIs
            url.startsWith("content://") -> parseContentUri(url)
            
            else -> null
        }
    }
    
    private fun parseIReaderScheme(url: String): DeepLink? {
        val uri = url.removePrefix("ireader://")
        val parts = uri.split("/")
        
        if (parts.isEmpty()) return null
        
        return when (parts[0]) {
            "book" -> {
                val bookId = parts.getOrNull(1)?.toLongOrNull()
                if (bookId != null) {
                    DeepLink(
                        type = DeepLinkType.BOOK,
                        url = url,
                        bookId = bookId
                    )
                } else null
            }
            
            "chapter" -> {
                val chapterId = parts.getOrNull(1)?.toLongOrNull()
                if (chapterId != null) {
                    DeepLink(
                        type = DeepLinkType.CHAPTER,
                        url = url,
                        chapterId = chapterId
                    )
                } else null
            }
            
            "source" -> {
                val sourceId = parts.getOrNull(1)?.toLongOrNull()
                if (sourceId != null) {
                    DeepLink(
                        type = DeepLinkType.SOURCE,
                        url = url,
                        sourceId = sourceId
                    )
                } else null
            }
            
            "browse" -> {
                DeepLink(
                    type = DeepLinkType.BROWSE,
                    url = url
                )
            }
            
            "library" -> {
                DeepLink(
                    type = DeepLinkType.LIBRARY,
                    url = url
                )
            }
            
            "settings" -> {
                val section = parts.getOrNull(1)
                DeepLink(
                    type = DeepLinkType.SETTINGS,
                    url = url,
                    settingsSection = section
                )
            }
            
            else -> null
        }
    }
    
    private fun parseHttpUrl(url: String): DeepLink? {
        // Parse HTTP URLs that might link to manga/book content
        // This would be customized based on supported sources
        return DeepLink(
            type = DeepLinkType.EXTERNAL_URL,
            url = url,
            externalUrl = url
        )
    }
    
    private fun parseContentUri(url: String): DeepLink? {
        // Parse content URIs for file sharing
        return DeepLink(
            type = DeepLinkType.CONTENT_URI,
            url = url,
            contentUri = url
        )
    }
    
    /**
     * Check if a URL can be handled as a deep link
     */
    fun canHandle(url: String): Boolean {
        return parseDeepLink(url) != null
    }
}

/**
 * Deep link data class
 */
data class DeepLink(
    val type: DeepLinkType,
    val url: String,
    val bookId: Long? = null,
    val chapterId: Long? = null,
    val sourceId: Long? = null,
    val externalUrl: String? = null,
    val contentUri: String? = null,
    val settingsSection: String? = null,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * Types of deep links supported
 */
enum class DeepLinkType {
    BOOK,           // Link to a specific book
    CHAPTER,        // Link to a specific chapter
    SOURCE,         // Link to a specific source
    BROWSE,         // Link to browse screen
    LIBRARY,        // Link to library screen
    SETTINGS,       // Link to settings screen
    EXTERNAL_URL,   // External HTTP/HTTPS URL
    CONTENT_URI     // Content URI for file sharing
}
