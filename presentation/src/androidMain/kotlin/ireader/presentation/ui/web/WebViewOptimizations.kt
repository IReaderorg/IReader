package ireader.presentation.ui.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import java.io.ByteArrayInputStream

/**
 * Resource loading optimizer for WebView
 * Blocks unnecessary resources like ads and optimizes loading performance
 */
object WebViewResourceOptimizer {
    
    /**
     * List of URL patterns to block (ads, trackers, etc.)
     */
    private val blockedPatterns = listOf(
        // Ad networks
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "googletagmanager.com",
        "facebook.com/tr",
        "facebook.net",
        
        // Common ad servers
        "adservice",
        "advertising",
        "ads.",
        "/ads/",
        "banner",
        "popup",
        
        // Tracking
        "analytics",
        "tracking",
        "tracker",
        "pixel",
        
        // Social media widgets
        "twitter.com/widgets",
        "platform.twitter.com",
        "connect.facebook.net"
    )
    
    /**
     * Check if a resource should be blocked
     */
    @Suppress("UNUSED_PARAMETER")
    fun shouldBlockResource(url: String, resourceType: String? = null): Boolean {
        // Check if URL matches blocked patterns
        val urlLower = url.lowercase()
        if (blockedPatterns.any { urlLower.contains(it) }) {
            return true
        }
        
        // Optionally block certain resource types
        // This can be made configurable via settings
        return false
    }
    
    /**
     * Create an empty response for blocked resources
     */
    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream("".toByteArray())
        )
    }
    
    /**
     * Intercept and optimize resource loading
     */
    @Suppress("UNUSED_PARAMETER")
    fun interceptRequest(
        view: WebView?,
        request: WebResourceRequest,
        blockImages: Boolean = false
    ): WebResourceResponse? {
        val url = request.url.toString()
        
        // Block ads and trackers
        if (shouldBlockResource(url)) {
            return createEmptyResponse()
        }
        
        // Optionally block images for faster loading
        if (blockImages && isImageResource(url)) {
            return createEmptyResponse()
        }
        
        // Let other resources load normally
        return null
    }
    
    /**
     * Check if URL is an image resource
     */
    private fun isImageResource(url: String): Boolean {
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp")
        val urlLower = url.lowercase()
        return imageExtensions.any { urlLower.contains(it) }
    }
}

/**
 * Cache manager for WebView resources
 */
class WebViewCacheManager {
    
    private val contentCache = mutableMapOf<String, CachedContent>()
    private val maxCacheSize = 50 // Maximum number of cached pages
    private val cacheExpiryMs = 5 * 60 * 1000L // 5 minutes
    
    data class CachedContent(
        val html: String,
        val timestamp: Long,
        val url: String
    )
    
    /**
     * Cache page content
     */
    fun cacheContent(url: String, html: String) {
        // Remove expired entries
        cleanExpiredCache()
        
        // Remove oldest entry if cache is full
        if (contentCache.size >= maxCacheSize) {
            val oldestKey = contentCache.entries
                .minByOrNull { it.value.timestamp }
                ?.key
            oldestKey?.let { contentCache.remove(it) }
        }
        
        contentCache[url] = CachedContent(
            html = html,
            timestamp = System.currentTimeMillis(),
            url = url
        )
    }
    
    /**
     * Retrieve cached content
     */
    fun getCachedContent(url: String): String? {
        val cached = contentCache[url] ?: return null
        
        // Check if cache is expired
        if (System.currentTimeMillis() - cached.timestamp > cacheExpiryMs) {
            contentCache.remove(url)
            return null
        }
        
        return cached.html
    }
    
    /**
     * Check if content is cached
     */
    fun isCached(url: String): Boolean {
        return getCachedContent(url) != null
    }
    
    /**
     * Clear all cached content
     */
    fun clearCache() {
        contentCache.clear()
    }
    
    /**
     * Remove expired cache entries
     */
    private fun cleanExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = contentCache.entries
            .filter { currentTime - it.value.timestamp > cacheExpiryMs }
            .map { it.key }
        
        expiredKeys.forEach { contentCache.remove(it) }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        cleanExpiredCache()
        return CacheStats(
            size = contentCache.size,
            maxSize = maxCacheSize,
            urls = contentCache.keys.toList()
        )
    }
    
    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val urls: List<String>
    )
}

/**
 * JavaScript execution optimizer
 */
object JavaScriptOptimizer {
    
    /**
     * Inject JavaScript to disable unnecessary features
     */
    fun getOptimizationScript(): String {
        return """
            (function() {
                // Disable console logging in production
                if (typeof console !== 'undefined') {
                    console.log = function() {};
                    console.debug = function() {};
                    console.info = function() {};
                }
                
                // Disable animations for faster rendering
                var style = document.createElement('style');
                style.innerHTML = '* { animation-duration: 0s !important; transition-duration: 0s !important; }';
                document.head.appendChild(style);
                
                // Remove social media widgets
                var socialWidgets = document.querySelectorAll('[class*="social"], [id*="social"], [class*="share"], [id*="share"]');
                socialWidgets.forEach(function(widget) {
                    widget.remove();
                });
            })();
        """.trimIndent()
    }
    
    /**
     * Inject script to extract novel content efficiently
     */
    fun getContentExtractionScript(): String {
        return """
            (function() {
                // Helper to find main content area
                function findMainContent() {
                    var selectors = [
                        'article',
                        '[role="main"]',
                        '.content',
                        '#content',
                        '.post-content',
                        '.entry-content',
                        'main'
                    ];
                    
                    for (var i = 0; i < selectors.length; i++) {
                        var element = document.querySelector(selectors[i]);
                        if (element && element.textContent.length > 100) {
                            return element;
                        }
                    }
                    
                    return document.body;
                }
                
                return findMainContent();
            })();
        """.trimIndent()
    }
}
