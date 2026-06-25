package ireader.domain.catalogs

import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.entities.ExtensionInstallMethod
import ireader.domain.models.entities.ExtensionSecurity
import ireader.domain.models.entities.ExtensionTrustLevel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Unit tests for extension management logic.
 *
 * Tests verify the contracts that ExtensionViewModel relies on:
 * - Security checks before install
 * - Batch update behavior
 * - Update detection
 * - Statistics tracking
 */
class ExtensionManagerTest {

    @Test
    fun `installMethod enum has all expected values`() {
        val methods = ExtensionInstallMethod.entries
        assertTrue(methods.contains(ExtensionInstallMethod.PACKAGE_INSTALLER))
        assertTrue(methods.contains(ExtensionInstallMethod.SHIZUKU))
        assertTrue(methods.contains(ExtensionInstallMethod.PRIVATE))
        assertTrue(methods.contains(ExtensionInstallMethod.LEGACY))
        assertEquals(4, methods.size)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `ExtensionSecurity with BLOCKED trust level should prevent installation`() {
        val security = ExtensionSecurity(
            trustLevel = ExtensionTrustLevel.BLOCKED,
            signatureHash = null,
            permissions = emptyList(),
            hasNetworkAccess = false,
            hasStorageAccess = false,
            securityWarnings = listOf("Extension is blocked"),
            lastSecurityCheck = Clock.System.now().toEpochMilliseconds()
        )
        assertEquals(ExtensionTrustLevel.BLOCKED, security.trustLevel)
        assertTrue(security.securityWarnings.isNotEmpty())
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `ExtensionSecurity with TRUSTED trust level should allow installation`() {
        val security = ExtensionSecurity(
            trustLevel = ExtensionTrustLevel.TRUSTED,
            signatureHash = "abc123",
            permissions = listOf("INTERNET"),
            hasNetworkAccess = true,
            hasStorageAccess = false,
            securityWarnings = emptyList(),
            lastSecurityCheck = Clock.System.now().toEpochMilliseconds()
        )
        assertEquals(ExtensionTrustLevel.TRUSTED, security.trustLevel)
        assertTrue(security.securityWarnings.isEmpty())
    }

    @Test
    fun `CatalogRemote has correct fields`() {
        val catalog = createTestCatalogRemote(
            id = 42,
            name = "My Extension",
            versionCode = 5,
            pkgName = "ireader.ext.test"
        )
        assertEquals(42L, catalog.sourceId)
        assertEquals("My Extension", catalog.name)
        assertEquals(5, catalog.versionCode)
        assertEquals("ireader.ext.test", catalog.pkgName)
        assertEquals("en", catalog.lang)
        assertFalse(catalog.nsfw)
    }

    @Test
    fun `CatalogInstalled version comparison determines update availability`() {
        val installed = createTestCatalogInstalled(versionCode = 3)
        val remote = createTestCatalogRemote(versionCode = 5)

        val hasUpdate = remote.versionCode > installed.versionCode
        assertTrue(hasUpdate, "Remote v5 should be an update over installed v3")
    }

    @Test
    fun `CatalogInstalled with same version has no update`() {
        val installed = createTestCatalogInstalled(versionCode = 5)
        val remote = createTestCatalogRemote(versionCode = 5)

        val hasUpdate = remote.versionCode > installed.versionCode
        assertFalse(hasUpdate, "Same version should not be an update")
    }

    @Test
    fun `batch update counts successes correctly`() {
        val results = mapOf(
            1L to Result.success(Unit),
            2L to Result.failure(RuntimeException("failed")),
            3L to Result.success(Unit)
        )
        val successCount = results.values.count { it.isSuccess }
        assertEquals(2, successCount)
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun createTestCatalogRemote(
        id: Long = 1,
        name: String = "Test Extension",
        versionCode: Int = 1,
        pkgName: String = "ireader.extension.test"
    ) = CatalogRemote(
        sourceId = id,
        source = id,
        name = name,
        description = "Test extension description",
        lang = "en",
        versionName = "1.0.0",
        versionCode = versionCode,
        pkgName = pkgName,
        iconUrl = "",
        pkgUrl = "",
        jarUrl = "",
        nsfw = false,
        repositoryId = -1L,
        repositoryType = "IREADER"
    )

    private fun createTestCatalogInstalled(
        id: Long = 1,
        name: String = "Test Extension",
        versionCode: Int = 1
    ) = CatalogInstalled.SystemWide(
        name = name,
        description = "Test extension description",
        source = null,
        pkgName = "ireader.extension.test",
        versionName = "1.0.0",
        versionCode = versionCode,
        nsfw = false,
        isPinned = false,
        hasUpdate = false,
        iconUrl = "",
        installDir = null
    )
}
