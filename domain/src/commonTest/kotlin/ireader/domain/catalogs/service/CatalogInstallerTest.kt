package ireader.domain.catalogs.service

import ireader.core.os.InstallStep
import ireader.domain.models.entities.CatalogRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for CatalogInstaller interface and implementations
 * Tests installation flow, error handling, and uninstallation
 */
class CatalogInstallerTest {

    @Test
    fun `install returns flow with InstallStep states`() = runTest {
        // Given
        val installer = TestCatalogInstaller()
        val catalog = createTestCatalogRemote()
        
        // When
        val steps = installer.install(catalog).toList()
        
        // Then
        assertTrue(steps.isNotEmpty())
        assertTrue(steps.contains(InstallStep.Downloading))
        assertTrue(steps.contains(InstallStep.Success))
    }

    @Test
    fun `install handles error gracefully`() = runTest {
        // Given
        val installer = TestCatalogInstaller(shouldFail = true)
        val catalog = createTestCatalogRemote()
        
        // When
        val steps = installer.install(catalog).toList()
        
        // Then
        assertTrue(steps.any { it is InstallStep.Error })
    }

    @Test
    fun `uninstall returns Success for existing package`() = runTest {
        // Given
        val installer = TestCatalogInstaller()
        
        // When
        val result = installer.uninstall("test.package")
        
        // Then
        assertEquals(InstallStep.Success, result)
    }

    @Test
    fun `uninstall returns Error for non-existing package`() = runTest {
        // Given
        val installer = TestCatalogInstaller(uninstallShouldFail = true)
        
        // When
        val result = installer.uninstall("non.existing.package")
        
        // Then
        assertTrue(result is InstallStep.Error)
    }

    @Test
    fun `install JS plugin uses correct flow`() = runTest {
        // Given
        val installer = TestCatalogInstaller()
        val jsPlugin = createTestCatalogRemote(isJSPlugin = true)
        
        // When
        val steps = installer.install(jsPlugin).toList()
        
        // Then
        assertTrue(steps.contains(InstallStep.Downloading))
        assertTrue(steps.contains(InstallStep.Success))
    }

    @Test
    fun `install APK uses correct flow`() = runTest {
        // Given
        val installer = TestCatalogInstaller()
        val apkCatalog = createTestCatalogRemote(isJSPlugin = false)
        
        // When
        val steps = installer.install(apkCatalog).toList()
        
        // Then
        assertTrue(steps.contains(InstallStep.Downloading))
    }

    private fun createTestCatalogRemote(isJSPlugin: Boolean = false): CatalogRemote {
        val pkgUrl = if (isJSPlugin) {
            "https://example.com/plugin.js"
        } else {
            "https://example.com/extension.apk"
        }
        
        return CatalogRemote(
            name = "Test Catalog",
            description = "Test Description",
            sourceId = 12345L,
            source = 12345L,
            pkgName = "test.package",
            versionName = "1.0.0",
            versionCode = 1,
            lang = "en",
            pkgUrl = pkgUrl,
            iconUrl = "https://example.com/icon.png",
            jarUrl = "",
            nsfw = false
        )
    }

    // Test double for CatalogInstaller
    private class TestCatalogInstaller(
        private val shouldFail: Boolean = false,
        private val uninstallShouldFail: Boolean = false
    ) : CatalogInstaller {
        
        override fun install(catalog: CatalogRemote): Flow<InstallStep> {
            return if (shouldFail) {
                flowOf(
                    InstallStep.Downloading,
                    InstallStep.Error("Test error"),
                    InstallStep.Idle
                )
            } else {
                flowOf(
                    InstallStep.Downloading,
                    InstallStep.Success,
                    InstallStep.Idle
                )
            }
        }

        override suspend fun uninstall(pkgName: String): InstallStep {
            return if (uninstallShouldFail) {
                InstallStep.Error("Package not found")
            } else {
                InstallStep.Success
            }
        }
    }
}

/**
 * Tests for InstallStep sealed class
 */
class InstallStepTest {

    @Test
    fun `InstallStep Idle is singleton`() {
        // Given & When
        val idle1 = InstallStep.Idle
        val idle2 = InstallStep.Idle
        
        // Then
        assertSame(idle1, idle2)
    }

    @Test
    fun `InstallStep Downloading is singleton`() {
        // Given & When
        val downloading1 = InstallStep.Downloading
        val downloading2 = InstallStep.Downloading
        
        // Then
        assertSame(downloading1, downloading2)
    }

    @Test
    fun `InstallStep Success is singleton`() {
        // Given & When
        val success1 = InstallStep.Success
        val success2 = InstallStep.Success
        
        // Then
        assertSame(success1, success2)
    }

    @Test
    fun `InstallStep Error contains message`() {
        // Given
        val errorMessage = "Installation failed"
        
        // When
        val error = InstallStep.Error(errorMessage)
        
        // Then
        assertEquals(errorMessage, error.error)
    }

    @Test
    fun `InstallStep Error with different messages are not equal`() {
        // Given
        val error1 = InstallStep.Error("Error 1")
        val error2 = InstallStep.Error("Error 2")
        
        // Then
        assertNotEquals(error1, error2)
    }
}
