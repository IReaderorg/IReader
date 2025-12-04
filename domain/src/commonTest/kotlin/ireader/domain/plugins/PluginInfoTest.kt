package ireader.domain.plugins

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Unit tests for Plugin-related classes
 */
class PluginInfoTest {

    // ==================== PluginType Tests ====================

    @Test
    fun `PluginType has all expected values`() {
        val types = PluginType.values()
        
        assertEquals(4, types.size)
        assertTrue(types.contains(PluginType.THEME))
        assertTrue(types.contains(PluginType.TRANSLATION))
        assertTrue(types.contains(PluginType.TTS))
        assertTrue(types.contains(PluginType.FEATURE))
    }

    @Test
    fun `PluginType valueOf works correctly`() {
        assertEquals(PluginType.THEME, PluginType.valueOf("THEME"))
        assertEquals(PluginType.TRANSLATION, PluginType.valueOf("TRANSLATION"))
        assertEquals(PluginType.TTS, PluginType.valueOf("TTS"))
        assertEquals(PluginType.FEATURE, PluginType.valueOf("FEATURE"))
    }

    @Test
    fun `PluginType ordinal values`() {
        assertEquals(0, PluginType.THEME.ordinal)
        assertEquals(1, PluginType.TRANSLATION.ordinal)
        assertEquals(2, PluginType.TTS.ordinal)
        assertEquals(3, PluginType.FEATURE.ordinal)
    }

    // ==================== PluginStatus Tests ====================

    @Test
    fun `PluginStatus has all expected values`() {
        val statuses = PluginStatus.values()
        
        assertEquals(4, statuses.size)
        assertTrue(statuses.contains(PluginStatus.ENABLED))
        assertTrue(statuses.contains(PluginStatus.DISABLED))
        assertTrue(statuses.contains(PluginStatus.ERROR))
        assertTrue(statuses.contains(PluginStatus.UPDATING))
    }

    @Test
    fun `PluginStatus valueOf works correctly`() {
        assertEquals(PluginStatus.ENABLED, PluginStatus.valueOf("ENABLED"))
        assertEquals(PluginStatus.DISABLED, PluginStatus.valueOf("DISABLED"))
        assertEquals(PluginStatus.ERROR, PluginStatus.valueOf("ERROR"))
        assertEquals(PluginStatus.UPDATING, PluginStatus.valueOf("UPDATING"))
    }

    // ==================== Platform Tests ====================

    @Test
    fun `Platform has all expected values`() {
        val platforms = Platform.values()
        
        assertEquals(3, platforms.size)
        assertTrue(platforms.contains(Platform.ANDROID))
        assertTrue(platforms.contains(Platform.IOS))
        assertTrue(platforms.contains(Platform.DESKTOP))
    }

    // ==================== PluginAuthor Tests ====================

    @Test
    fun `PluginAuthor with all fields`() {
        val author = PluginAuthor(
            name = "John Doe",
            email = "john@example.com",
            website = "https://example.com"
        )
        
        assertEquals("John Doe", author.name)
        assertEquals("john@example.com", author.email)
        assertEquals("https://example.com", author.website)
    }

    @Test
    fun `PluginAuthor with only name`() {
        val author = PluginAuthor(name = "Jane Doe")
        
        assertEquals("Jane Doe", author.name)
        assertNull(author.email)
        assertNull(author.website)
    }

    @Test
    fun `PluginAuthor equality`() {
        val author1 = PluginAuthor(name = "Test", email = "test@test.com")
        val author2 = PluginAuthor(name = "Test", email = "test@test.com")
        
        assertEquals(author1, author2)
    }

    // ==================== PluginManifest Tests ====================

    @Test
    fun `PluginManifest with required fields`() {
        val manifest = PluginManifest(
            id = "com.example.plugin",
            name = "Example Plugin",
            version = "1.0.0",
            versionCode = 1,
            description = "An example plugin",
            author = PluginAuthor(name = "Developer"),
            type = PluginType.THEME,
            permissions = emptyList(),
            minIReaderVersion = "1.0.0",
            platforms = listOf(Platform.ANDROID)
        )
        
        assertEquals("com.example.plugin", manifest.id)
        assertEquals("Example Plugin", manifest.name)
        assertEquals("1.0.0", manifest.version)
        assertEquals(1, manifest.versionCode)
        assertEquals("An example plugin", manifest.description)
        assertEquals(PluginType.THEME, manifest.type)
        assertTrue(manifest.permissions.isEmpty())
        assertEquals("1.0.0", manifest.minIReaderVersion)
        assertEquals(1, manifest.platforms.size)
        assertNull(manifest.monetization)
        assertNull(manifest.iconUrl)
        assertTrue(manifest.screenshotUrls.isEmpty())
    }

    @Test
    fun `PluginManifest with all fields`() {
        val manifest = PluginManifest(
            id = "com.example.fullplugin",
            name = "Full Plugin",
            version = "2.0.0",
            versionCode = 10,
            description = "A full-featured plugin",
            author = PluginAuthor(
                name = "Full Developer",
                email = "dev@example.com",
                website = "https://dev.example.com"
            ),
            type = PluginType.TRANSLATION,
            permissions = listOf(PluginPermission.NETWORK, PluginPermission.STORAGE),
            minIReaderVersion = "2.0.0",
            platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP),
            iconUrl = "https://example.com/icon.png",
            screenshotUrls = listOf("https://example.com/ss1.png", "https://example.com/ss2.png")
        )
        
        assertEquals("com.example.fullplugin", manifest.id)
        assertEquals(2, manifest.permissions.size)
        assertEquals(3, manifest.platforms.size)
        assertEquals("https://example.com/icon.png", manifest.iconUrl)
        assertEquals(2, manifest.screenshotUrls.size)
    }

    @Test
    fun `PluginManifest equality`() {
        val manifest1 = createTestManifest("plugin1")
        val manifest2 = createTestManifest("plugin1")
        
        assertEquals(manifest1, manifest2)
    }

    @Test
    fun `PluginManifest inequality with different id`() {
        val manifest1 = createTestManifest("plugin1")
        val manifest2 = createTestManifest("plugin2")
        
        assertNotEquals(manifest1, manifest2)
    }

    // ==================== PluginInfo Tests ====================

    @Test
    fun `PluginInfo with all fields`() {
        val manifest = createTestManifest("test.plugin")
        val info = PluginInfo(
            id = "test.plugin",
            manifest = manifest,
            status = PluginStatus.ENABLED,
            installDate = 1234567890L,
            lastUpdate = 1234567900L,
            isPurchased = true,
            rating = 4.5f,
            downloadCount = 1000
        )
        
        assertEquals("test.plugin", info.id)
        assertEquals(manifest, info.manifest)
        assertEquals(PluginStatus.ENABLED, info.status)
        assertEquals(1234567890L, info.installDate)
        assertEquals(1234567900L, info.lastUpdate)
        assertTrue(info.isPurchased)
        assertEquals(4.5f, info.rating)
        assertEquals(1000, info.downloadCount)
    }

    @Test
    fun `PluginInfo with null optional fields`() {
        val manifest = createTestManifest("test.plugin")
        val info = PluginInfo(
            id = "test.plugin",
            manifest = manifest,
            status = PluginStatus.DISABLED,
            installDate = 1234567890L,
            lastUpdate = null,
            isPurchased = false,
            rating = null,
            downloadCount = 0
        )
        
        assertNull(info.lastUpdate)
        assertNull(info.rating)
        assertFalse(info.isPurchased)
        assertEquals(0, info.downloadCount)
    }

    @Test
    fun `PluginInfo copy with status change`() {
        val manifest = createTestManifest("test.plugin")
        val original = PluginInfo(
            id = "test.plugin",
            manifest = manifest,
            status = PluginStatus.ENABLED,
            installDate = 1234567890L,
            lastUpdate = null,
            isPurchased = false,
            rating = null,
            downloadCount = 100
        )
        
        val updated = original.copy(status = PluginStatus.DISABLED)
        
        assertEquals(PluginStatus.ENABLED, original.status)
        assertEquals(PluginStatus.DISABLED, updated.status)
        assertEquals(original.id, updated.id)
    }

    // ==================== Practical Usage Tests ====================

    @Test
    fun `filter plugins by type`() {
        val plugins = listOf(
            createPluginInfo("theme1", PluginType.THEME),
            createPluginInfo("trans1", PluginType.TRANSLATION),
            createPluginInfo("theme2", PluginType.THEME),
            createPluginInfo("tts1", PluginType.TTS)
        )
        
        val themePlugins = plugins.filter { it.manifest.type == PluginType.THEME }
        
        assertEquals(2, themePlugins.size)
    }

    @Test
    fun `filter plugins by status`() {
        val plugins = listOf(
            createPluginInfo("p1", status = PluginStatus.ENABLED),
            createPluginInfo("p2", status = PluginStatus.DISABLED),
            createPluginInfo("p3", status = PluginStatus.ENABLED),
            createPluginInfo("p4", status = PluginStatus.ERROR)
        )
        
        val enabledPlugins = plugins.filter { it.status == PluginStatus.ENABLED }
        
        assertEquals(2, enabledPlugins.size)
    }

    @Test
    fun `sort plugins by download count`() {
        val plugins = listOf(
            createPluginInfo("p1", downloadCount = 100),
            createPluginInfo("p2", downloadCount = 500),
            createPluginInfo("p3", downloadCount = 250)
        )
        
        val sorted = plugins.sortedByDescending { it.downloadCount }
        
        assertEquals("p2", sorted[0].id)
        assertEquals("p3", sorted[1].id)
        assertEquals("p1", sorted[2].id)
    }

    @Test
    fun `filter plugins by platform`() {
        val androidPlugin = createTestManifest("android", platforms = listOf(Platform.ANDROID))
        val multiPlatformPlugin = createTestManifest("multi", platforms = listOf(Platform.ANDROID, Platform.DESKTOP))
        val desktopPlugin = createTestManifest("desktop", platforms = listOf(Platform.DESKTOP))
        
        val manifests = listOf(androidPlugin, multiPlatformPlugin, desktopPlugin)
        val androidCompatible = manifests.filter { Platform.ANDROID in it.platforms }
        
        assertEquals(2, androidCompatible.size)
    }

    // ==================== Helper Functions ====================

    private fun createTestManifest(
        id: String,
        type: PluginType = PluginType.THEME,
        platforms: List<Platform> = listOf(Platform.ANDROID)
    ): PluginManifest {
        return PluginManifest(
            id = id,
            name = "Test Plugin $id",
            version = "1.0.0",
            versionCode = 1,
            description = "Test description",
            author = PluginAuthor(name = "Test Author"),
            type = type,
            permissions = emptyList(),
            minIReaderVersion = "1.0.0",
            platforms = platforms
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun createPluginInfo(
        id: String,
        type: PluginType = PluginType.THEME,
        status: PluginStatus = PluginStatus.ENABLED,
        downloadCount: Int = 0
    ): PluginInfo {
        return PluginInfo(
            id = id,
            manifest = createTestManifest(id, type),
            status = status,
            installDate = Clock.System.now().toEpochMilliseconds(),
            lastUpdate = null,
            isPurchased = false,
            rating = null,
            downloadCount = downloadCount
        )
    }
}
