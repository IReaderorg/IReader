package ireader.data.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogMapperTest {

    @Test
    fun catalogMapperShouldMapAllFields() {
        // catalogMapper: (Long, Long, String, String, String, String, Int, String, String, String, Boolean, String) -> CatalogRemote
        val catalog = catalogMapper(
            1L,           // sourceId
            100L,         // source
            "Test Source", // name
            "A test source", // description
            "com.test.source", // pkgName
            "1.0.0",      // versionName
            1,            // versionCode
            "en",         // lang
            "https://example.com/source.apk", // pkgUrl
            "https://example.com/icon.png", // iconUrl
            false,        // nsfw
            "repo"        // repositoryType
        )

        assertEquals(1L, catalog.sourceId)
        assertEquals(100L, catalog.source)
        assertEquals("Test Source", catalog.name)
        assertEquals("A test source", catalog.description)
        assertEquals("com.test.source", catalog.pkgName)
        assertEquals("1.0.0", catalog.versionName)
        assertEquals(1, catalog.versionCode)
        assertEquals("en", catalog.lang)
        assertEquals("https://example.com/source.apk", catalog.pkgUrl)
        assertEquals("https://example.com/icon.png", catalog.iconUrl)
        assertEquals("https://example.com/source.jar", catalog.jarUrl)
        assertFalse(catalog.nsfw)
        assertEquals("repo", catalog.repositoryType)
    }

    @Test
    fun catalogMapperShouldGenerateJarUrlFromApkUrl() {
        val catalog = catalogMapper(
            1L, 100L, "Source", "Desc", "pkg", "1.0", 1, "en",
            "https://example.com/my-source.apk", "icon.png",
            false, "repo"
        )

        assertEquals("https://example.com/my-source.jar", catalog.jarUrl)
    }

    @Test
    fun catalogMapperShouldHandleNsfw() {
        val catalog = catalogMapper(
            1L, 100L, "NSFW Source", "Desc", "pkg", "1.0", 1, "en",
            "url.apk", "icon.png", true, "repo"
        )

        assertTrue(catalog.nsfw)
    }

    @Test
    fun catalogMapperShouldHandleEmptyUrl() {
        val catalog = catalogMapper(
            1L, 100L, "Source", "Desc", "pkg", "1.0", 1, "en",
            "", "icon.png", false, "repo"
        )

        assertEquals("", catalog.pkgUrl)
        assertEquals("", catalog.jarUrl) // replace on empty string returns empty
    }
}
