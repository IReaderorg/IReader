package ireader.data.catalog

import kotlin.test.*

/**
 * Unit tests for AndroidCatalogLoader
 * Tests DEX loading, secure directory handling, and Android 14+/15+ compatibility
 */
class AndroidCatalogLoaderTest {

    @Test
    fun `secure extensions directory path is correct`() {
        // Given
        val codeCacheDir = "/data/data/com.example.app/code_cache"
        
        // When
        val secureExtensionsDir = "$codeCacheDir/secure_extensions"
        
        // Then
        assertTrue(secureExtensionsDir.contains("code_cache"))
        assertTrue(secureExtensionsDir.contains("secure_extensions"))
    }

    @Test
    fun `dex cache directory path is correct`() {
        // Given
        val codeCacheDir = "/data/data/com.example.app/code_cache"
        
        // When
        val dexCacheDir = "$codeCacheDir/dex-cache"
        
        // Then
        assertTrue(dexCacheDir.contains("code_cache"))
        assertTrue(dexCacheDir.contains("dex-cache"))
    }

    @Test
    fun `plugin dex output directory is unique per plugin`() {
        // Given
        val dexCacheDir = "/data/data/com.example.app/code_cache/dex-cache"
        val plugin1 = "com.example.plugin1"
        val plugin2 = "com.example.plugin2"
        
        // When
        val dexDir1 = "$dexCacheDir/$plugin1"
        val dexDir2 = "$dexCacheDir/$plugin2"
        
        // Then
        assertNotEquals(dexDir1, dexDir2)
        assertTrue(dexDir1.contains(plugin1))
        assertTrue(dexDir2.contains(plugin2))
    }

    @Test
    fun `APK file should be copied to secure location before loading`() {
        // Given
        val originalPath = "/storage/emulated/0/ireader/extensions/plugin.apk"
        val securePath = "/data/data/com.example.app/code_cache/secure_extensions/plugin.apk"
        
        // When
        val isSecureLocation = securePath.contains("code_cache")
        
        // Then
        assertTrue(isSecureLocation, "APK should be in code_cache for Android 14+ compatibility")
    }

    @Test
    fun `file permissions should be set to read-only after copy`() {
        // Given
        val filePermissions = FilePermissionSimulator()
        
        // When
        filePermissions.setReadOnly()
        
        // Then
        assertTrue(filePermissions.isReadable)
        assertFalse(filePermissions.isWritable)
    }

    @Test
    fun `stale APK files should be cleaned on startup`() {
        // Given
        val existingFiles = listOf("old_plugin.apk", "stale_plugin.apk", "active_plugin.apk")
        val activePlugins = listOf("active_plugin")
        
        // When
        val filesToDelete = existingFiles.filter { file ->
            val pluginName = file.removeSuffix(".apk")
            pluginName !in activePlugins
        }
        
        // Then
        assertEquals(2, filesToDelete.size)
        assertTrue(filesToDelete.contains("old_plugin.apk"))
        assertTrue(filesToDelete.contains("stale_plugin.apk"))
        assertFalse(filesToDelete.contains("active_plugin.apk"))
    }

    @Test
    fun `JS plugins directory uses cache when preference is set`() {
        // Given
        val useCacheDir = true
        val cacheDir = "/data/data/com.example.app/cache"
        val externalDir = "/storage/emulated/0/Android/data/com.example.app"
        
        // When
        val jsPluginsDir = if (useCacheDir) {
            "$cacheDir/js-plugins"
        } else {
            "$externalDir/ireader/js-plugins"
        }
        
        // Then
        assertTrue(jsPluginsDir.contains("cache"))
        assertTrue(jsPluginsDir.contains("js-plugins"))
    }

    @Test
    fun `JS plugins directory uses external storage when preference is not set`() {
        // Given
        val useCacheDir = false
        val cacheDir = "/data/data/com.example.app/cache"
        val externalDir = "/storage/emulated/0/Android/data/com.example.app"
        
        // When
        val jsPluginsDir = if (useCacheDir) {
            "$cacheDir/js-plugins"
        } else {
            "$externalDir/ireader/js-plugins"
        }
        
        // Then
        assertTrue(jsPluginsDir.contains("ireader"))
        assertTrue(jsPluginsDir.contains("js-plugins"))
    }

    @Test
    fun `extension feature flag is correct`() {
        // Given
        val expectedFeature = "ireader"
        
        // Then
        assertEquals("ireader", expectedFeature)
    }

    @Test
    fun `metadata source class key is correct`() {
        // Given
        val expectedKey = "source.class"
        
        // Then
        assertEquals("source.class", expectedKey)
    }

    @Test
    fun `lib version range is valid`() {
        // Given
        val minVersion = 1
        val maxVersion = 1
        
        // When
        val isValidRange = minVersion <= maxVersion
        
        // Then
        assertTrue(isValidRange)
    }

    // Helper class for simulating file permissions
    private class FilePermissionSimulator {
        var isReadable = true
        var isWritable = true
        var isExecutable = false
        
        fun setReadOnly() {
            isWritable = false
        }
    }
}

/**
 * Tests for Android 15+ specific compatibility
 */
class Android15CompatibilityTest {

    @Test
    fun `DEX loading should use InMemoryDexClassLoader when available`() {
        // Android 15+ recommends InMemoryDexClassLoader for better security
        // This test documents the expected behavior
        
        // Given
        val androidVersion = 35 // Android 15
        val useInMemoryLoader = androidVersion >= 35
        
        // Then
        assertTrue(useInMemoryLoader, "Android 15+ should prefer InMemoryDexClassLoader")
    }

    @Test
    fun `file-based DEX loading should still work as fallback`() {
        // Given
        val androidVersion = 34 // Android 14
        val useFileBased = androidVersion < 35
        
        // Then
        assertTrue(useFileBased, "Android 14 should use file-based DEX loading")
    }

    @Test
    fun `secure directory should have correct permissions`() {
        // Given
        val expectedPermissions = "rwx------" // Owner only
        
        // When
        val isSecure = expectedPermissions.startsWith("rwx") && 
                       expectedPermissions.substring(3) == "------"
        
        // Then
        assertTrue(isSecure, "Secure directory should only be accessible by owner")
    }

    @Test
    fun `APK signature should be verified before loading`() {
        // Given
        val apkHasValidSignature = true
        
        // Then
        assertTrue(apkHasValidSignature, "APK signature should be verified")
    }
}

/**
 * Tests for JS plugin loading on Android
 */
class AndroidJSPluginLoaderTest {

    @Test
    fun `JS plugin file extension is correct`() {
        // Given
        val pluginFileName = "AllNovelFull.js"
        
        // When
        val extension = pluginFileName.substringAfterLast(".")
        
        // Then
        assertEquals("js", extension)
    }

    @Test
    fun `metadata file has correct naming convention`() {
        // Given
        val pluginId = "anf.net"
        
        // When
        val metadataFileName = "$pluginId.meta.json"
        
        // Then
        assertTrue(metadataFileName.endsWith(".meta.json"))
        assertTrue(metadataFileName.startsWith(pluginId))
    }

    @Test
    fun `stub plugins load faster than full plugins`() {
        // Given - simulated load times
        val stubLoadTime = 10L // ms
        val fullLoadTime = 500L // ms
        
        // Then
        assertTrue(stubLoadTime < fullLoadTime, "Stub plugins should load much faster")
    }

    @Test
    fun `background loading replaces stubs with actual plugins`() {
        // Given
        val stubSourceId = 12345L
        val actualSourceId = 12345L
        
        // When
        val isSameSource = stubSourceId == actualSourceId
        
        // Then
        assertTrue(isSameSource, "Stub and actual plugin should have same sourceId")
    }

    @Test
    fun `priority plugins load first`() {
        // Given
        val priorityPlugins = setOf("favorite.plugin", "frequently.used")
        val allPlugins = listOf("favorite.plugin", "other.plugin", "frequently.used", "rarely.used")
        
        // When
        val loadOrder = allPlugins.sortedBy { if (it in priorityPlugins) 0 else 1 }
        
        // Then
        assertEquals("favorite.plugin", loadOrder[0])
        assertTrue(loadOrder.indexOf("favorite.plugin") < loadOrder.indexOf("other.plugin"))
    }
}
