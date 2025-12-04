package ireader.core.source

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for SourceHelpers.buildAbsoluteUrl to ensure proper URL construction
 */
class SourceHelpersTest {
    
    private val baseUrl = "https://example.com"
    
    @Test
    fun `buildAbsoluteUrl with absolute URL returns unchanged`() {
        val path = "https://other.com/path"
        val result = SourceHelpers.buildAbsoluteUrl(baseUrl, path)
        assertEquals("https://other.com/path", result)
    }
    
    @Test
    fun `buildAbsoluteUrl with protocol-relative URL adds https`() {
        val path = "//cdn.example.com/image.jpg"
        val result = SourceHelpers.buildAbsoluteUrl(baseUrl, path)
        assertEquals("https://cdn.example.com/image.jpg", result)
    }
    
    @Test
    fun `buildAbsoluteUrl with root-relative URL prepends baseUrl`() {
        val path = "/checkout/chapter-1"
        val result = SourceHelpers.buildAbsoluteUrl(baseUrl, path)
        assertEquals("https://example.com/checkout/chapter-1", result)
    }
    
    @Test
    fun `buildAbsoluteUrl with relative URL joins with slash`() {
        val path = "chapter-1"
        val result = SourceHelpers.buildAbsoluteUrl(baseUrl, path)
        assertEquals("https://example.com/chapter-1", result)
    }
    
    @Test
    fun `buildAbsoluteUrl handles baseUrl with trailing slash`() {
        val baseUrlWithSlash = "https://example.com/"
        val path = "chapter-1"
        val result = SourceHelpers.buildAbsoluteUrl(baseUrlWithSlash, path)
        // Should normalize and not have double slash
        assertEquals("https://example.com/chapter-1", result)
    }
    
    @Test
    fun `buildAbsoluteUrl handles empty path`() {
        val path = ""
        val result = SourceHelpers.buildAbsoluteUrl(baseUrl, path)
        assertEquals("https://example.com/", result)
    }
    
    @Test
    fun `buildAbsoluteUrl handles baseUrl without protocol`() {
        val baseUrlNoProtocol = "example.com"
        val path = "/chapter-1"
        val result = SourceHelpers.buildAbsoluteUrl(baseUrlNoProtocol, path)
        assertEquals("https://example.com/chapter-1", result)
    }
    
    @Test
    fun `buildAbsoluteUrl handles complex paths`() {
        val path = "/novel/123/chapter/456?page=1"
        val result = SourceHelpers.buildAbsoluteUrl(baseUrl, path)
        assertEquals("https://example.com/novel/123/chapter/456?page=1", result)
    }
    
    @Test
    fun `buildAbsoluteUrl handles paths with fragments`() {
        val path = "/chapter-1#section-2"
        val result = SourceHelpers.buildAbsoluteUrl(baseUrl, path)
        assertEquals("https://example.com/chapter-1#section-2", result)
    }
    
    @Test
    fun `normalizeUrl removes trailing slash`() {
        val url = "https://example.com/"
        val result = SourceHelpers.normalizeUrl(url)
        assertEquals("https://example.com", result)
    }
    
    @Test
    fun `normalizeUrl adds https to protocol-less URL`() {
        val url = "example.com"
        val result = SourceHelpers.normalizeUrl(url)
        assertEquals("https://example.com", result)
    }
    
    @Test
    fun `normalizeUrl handles protocol-relative URL`() {
        val url = "//example.com"
        val result = SourceHelpers.normalizeUrl(url)
        assertEquals("https://example.com", result)
    }

    
    @Test
    fun `extractDomain extracts domain correctly`() {
        assertEquals("example.com", SourceHelpers.extractDomain("https://example.com/path"))
        assertEquals("sub.example.com", SourceHelpers.extractDomain("https://sub.example.com"))
        assertEquals("example.com:8080", SourceHelpers.extractDomain("http://example.com:8080/path"))
    }
}
