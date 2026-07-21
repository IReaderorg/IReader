package ireader.core.deeplink

import ireader.core.log.IReaderLog

/**
 * URL resolution system for handling manga/book links from external sources
 * Resolves URLs to internal content identifiers
 */
class UrlResolver(
    private val resolvers: List<SourceUrlResolver>
) {
    
    /**
     * Resolve a URL to content information
     */
    suspend fun resolve(url: String): ResolvedContent? {
        IReaderLog.info("Resolving URL: $url", tag = "UrlResolver")
        
        for (resolver in resolvers) {
            if (resolver.canResolve(url)) {
                return try {
                    val content = resolver.resolve(url)
                    IReaderLog.info(
                        "URL resolved by ${resolver.sourceName}: ${content.type}",
                        tag = "UrlResolver"
                    )
                    content
                } catch (e: Exception) {
                    IReaderLog.error(
                        "Error resolving URL with ${resolver.sourceName}",
                        e,
                        tag = "UrlResolver"
                    )
                    null
                }
            }
        }
        
        IReaderLog.warn("No resolver found for URL: $url", tag = "UrlResolver")
        return null
    }
    
    /**
     * Check if a URL can be resolved
     */
    fun canResolve(url: String): Boolean {
        return resolvers.any { it.canResolve(url) }
    }
    
    /**
     * Get all supported URL patterns
     */
    fun getSupportedPatterns(): List<String> {
        return resolvers.flatMap { it.supportedPatterns }
    }
}

/**
 * Interface for source-specific URL resolvers
 */
interface SourceUrlResolver {
    val sourceName: String
    val supportedPatterns: List<String>
    
    /**
     * Check if this resolver can handle the given URL
     */
    fun canResolve(url: String): Boolean
    
    /**
     * Resolve the URL to content information
     */
    suspend fun resolve(url: String): ResolvedContent
}

/**
 * Resolved content information
 */
data class ResolvedContent(
    val type: ContentType,
    val sourceId: Long,
    val sourceName: String,
    val bookId: String? = null,
    val bookTitle: String? = null,
    val chapterId: String? = null,
    val chapterTitle: String? = null,
    val url: String,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Type of resolved content
 */
enum class ContentType {
    BOOK,
    CHAPTER,
    SEARCH,
    BROWSE
}

/**
 * Example resolver for common manga/book URL patterns
 */
class GenericUrlResolver : SourceUrlResolver {
    override val sourceName: String = "Generic"
    override val supportedPatterns: List<String> = listOf(
        "*/manga/*",
        "*/book/*",
        "*/title/*",
        "*/series/*"
    )
    
    override fun canResolve(url: String): Boolean {
        return supportedPatterns.any { pattern ->
            val regex = pattern.replace("*", ".*").toRegex()
            regex.matches(url)
        }
    }
    
    override suspend fun resolve(url: String): ResolvedContent {
        // Extract book/manga ID from URL
        val bookId = extractIdFromUrl(url)
        
        return ResolvedContent(
            type = ContentType.BOOK,
            sourceId = -1, // Unknown source
            sourceName = "Unknown",
            bookId = bookId,
            url = url
        )
    }
    
    private fun extractIdFromUrl(url: String): String? {
        // Try to extract ID from common URL patterns
        val patterns = listOf(
            """/manga/([^/]+)""",
            """/book/([^/]+)""",
            """/title/([^/]+)""",
            """/series/([^/]+)"""
        )
        
        for (pattern in patterns) {
            val match = pattern.toRegex().find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        
        return null
    }
}
