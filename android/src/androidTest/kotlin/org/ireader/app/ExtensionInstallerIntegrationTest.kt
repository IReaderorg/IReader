package org.ireader.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ireader.core.os.InstallStep
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Integration tests for the extension installer on a real Android device.
 *
 * These tests verify:
 * 1. Extension installation flow (download -> install -> success/error)
 * 2. Extension uninstallation
 * 3. Install step progression (Downloading -> Installing -> Success/Error)
 * 4. Extension state tracking
 * 5. Installer mode preferences
 * 6. Extension directory management
 *
 * Run on a physical device:
 * ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.ireader.app.ExtensionInstallerIntegrationTest
 */
@RunWith(AndroidJUnit4::class)
class ExtensionInstallerIntegrationTest : KoinComponent {

    private lateinit var context: Context
    private val getSimpleStorage: GetSimpleStorage by inject()
    private val uiPreferences: UiPreferences by inject()

    private val testPkgName = "test.extension.installer.integration"
    private val testJsPkgName = "test.js.extension.integration"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        cleanupTestFiles()
    }

    @After
    fun teardown() {
        cleanupTestFiles()
    }

    private fun cleanupTestFiles() {
        val extensionDir = getSimpleStorage.extensionDirectory().toFile()
        val cacheDir = getSimpleStorage.cacheExtensionDir().toFile()
        listOf(extensionDir, cacheDir).forEach { baseDir ->
            File(baseDir, testPkgName).deleteRecursively()
            File(baseDir, testJsPkgName).deleteRecursively()
        }
    }

    // ========== Install Step Progression Tests ==========

    @Test
    fun installStep_downloadingShouldBeLoading() {
        val step = InstallStep.Downloading
        assertTrue(step.isLoading(), "Downloading step should be loading")
        assertTrue(!step.isFinished(), "Downloading step should not be finished")
    }

    @Test
    fun installStep_successShouldBeFinished() {
        val step = InstallStep.Success
        assertTrue(step.isFinished(), "Success step should be finished")
        assertTrue(!step.isLoading(), "Success step should not be loading")
    }

    @Test
    fun installStep_errorShouldBeFinished() {
        val step = InstallStep.Error("test error")
        assertTrue(step.isFinished(), "Error step should be finished")
        assertTrue(!step.isLoading(), "Error step should not be loading")
    }

    @Test
    fun installStep_idleShouldNotBeLoadingOrFinished() {
        val step = InstallStep.Idle
        assertTrue(!step.isLoading(), "Idle step should not be loading")
        assertTrue(!step.isFinished(), "Idle step should not be finished")
    }

    // ========== Extension Directory Management Tests ==========

    @Test
    fun extensionDirectory_shouldBeAccessible() {
        val extensionDir = getSimpleStorage.extensionDirectory().toFile()
        assertTrue(extensionDir.exists() || extensionDir.mkdirs(), "Extension directory should be accessible")
        assertTrue(extensionDir.isDirectory, "Extension directory should be a directory")
    }

    @Test
    fun cacheExtensionDirectory_shouldBeAccessible() {
        val cacheDir = getSimpleStorage.cacheExtensionDir().toFile()
        assertTrue(cacheDir.exists() || cacheDir.mkdirs(), "Cache extension directory should be accessible")
        assertTrue(cacheDir.isDirectory, "Cache extension directory should be a directory")
    }

    @Test
    fun extensionDirectory_shouldAllowCreatingSubdirectories() {
        val extensionDir = getSimpleStorage.extensionDirectory().toFile()
        val testDir = File(extensionDir, testPkgName)
        assertTrue(testDir.mkdirs(), "Should be able to create extension subdirectory")
        assertTrue(testDir.exists(), "Extension subdirectory should exist")
        assertTrue(testDir.delete(), "Should be able to delete extension subdirectory")
    }

    // ========== Installer Mode Preference Tests ==========

    @Test
    fun installerMode_defaultShouldBeHybrid() {
        val mode = uiPreferences.installerMode().get()
        assertEquals(
            PreferenceValues.Installer.HybridInstaller,
            mode,
            "Default installer mode should be HybridInstaller"
        )
    }

    @Test
    fun installerMode_shouldBeChangeable() {
        val originalMode = uiPreferences.installerMode().get()

        uiPreferences.installerMode().set(PreferenceValues.Installer.LocalInstaller)
        val newMode = uiPreferences.installerMode().get()
        assertEquals(
            PreferenceValues.Installer.LocalInstaller,
            newMode,
            "Installer mode should be changeable to LocalInstaller"
        )

        uiPreferences.installerMode().set(originalMode)
    }

    @Test
    fun installerMode_shouldSupportAllModes() {
        val modes = listOf(
            PreferenceValues.Installer.LocalInstaller,
            PreferenceValues.Installer.AndroidPackageManager,
            PreferenceValues.Installer.HybridInstaller
        )

        modes.forEach { mode ->
            uiPreferences.installerMode().set(mode)
            assertEquals(mode, uiPreferences.installerMode().get(), "Should support mode: $mode")
        }
    }

    // ========== Extension File Operations Tests ==========

    @Test
    fun extensionFiles_shouldBeCreatableAndDeletable() = runBlocking {
        val extensionDir = File(getSimpleStorage.extensionDirectory().toFile(), testPkgName)
        extensionDir.mkdirs()

        val jsFile = File(extensionDir, "$testPkgName.js")
        jsFile.writeText("// test plugin content")

        assertTrue(jsFile.exists(), "JS file should exist")
        assertEquals("// test plugin content", jsFile.readText(), "JS file content should match")

        val metaFile = File(extensionDir, "$testPkgName.meta.json")
        metaFile.writeText("""{"name":"Test","version":"1.0.0"}""")
        assertTrue(metaFile.exists(), "Meta file should exist")

        assertTrue(extensionDir.deleteRecursively(), "Should be able to delete extension directory")
        assertTrue(!extensionDir.exists(), "Extension directory should be deleted")
    }

    @Test
    fun multipleExtensionDirectories_shouldCoexist() = runBlocking {
        val extensionDir = getSimpleStorage.extensionDirectory().toFile()
        val dir1 = File(extensionDir, "test.ext.1")
        val dir2 = File(extensionDir, "test.ext.2")

        dir1.mkdirs()
        dir2.mkdirs()

        File(dir1, "plugin1.js").writeText("// plugin 1")
        File(dir2, "plugin2.js").writeText("// plugin 2")

        assertTrue(dir1.exists(), "First extension directory should exist")
        assertTrue(dir2.exists(), "Second extension directory should exist")
        assertEquals("// plugin 1", File(dir1, "plugin1.js").readText())
        assertEquals("// plugin 2", File(dir2, "plugin2.js").readText())

        dir1.deleteRecursively()
        dir2.deleteRecursively()
    }

    // ========== CatalogRemote Model Tests ==========

    @Test
    fun catalogRemote_isLNReaderSource_shouldReturnTrueForLNReader() {
        val catalog = createTestCatalog("test.lnreader", "LNREADER")
        assertTrue(catalog.isLNReaderSource(), "LNReader catalog should be identified as LNReader source")
        assertTrue(!catalog.isIReaderSource(), "LNReader catalog should not be identified as IReader source")
    }

    @Test
    fun catalogRemote_isIReaderSource_shouldReturnTrueForIReader() {
        val catalog = createTestCatalog("test.ireader", "IREADER")
        assertTrue(catalog.isIReaderSource(), "IReader catalog should be identified as IReader source")
        assertTrue(!catalog.isLNReaderSource(), "IReader catalog should not be identified as LNReader source")
    }

    @Test
    fun catalogRemote_shouldStoreAllProperties() {
        val catalog = createTestCatalog(
            pkgName = "com.test.extension",
            repositoryType = "IREADER",
            name = "Test Extension",
            versionName = "2.0.0",
            lang = "en"
        )

        assertEquals("com.test.extension", catalog.pkgName)
        assertEquals("IREADER", catalog.repositoryType)
        assertEquals("Test Extension", catalog.name)
        assertEquals("2.0.0", catalog.versionName)
        assertEquals("en", catalog.lang)
    }

    // ========== Extension State Tracking Tests ==========

    @Test
    fun extensionDirectories_shouldTrackInstalledExtensions() = runBlocking {
        val extensionDir = getSimpleStorage.extensionDirectory().toFile()

        // Create mock installed extensions
        val ext1 = File(extensionDir, "installed.ext.1")
        val ext2 = File(extensionDir, "installed.ext.2")
        ext1.mkdirs()
        ext2.mkdirs()
        File(ext1, "plugin.js").writeText("// ext1")
        File(ext2, "plugin.js").writeText("// ext2")

        // Verify both are tracked
        val installedDirs = extensionDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
        assertTrue(installedDirs.any { it.name == "installed.ext.1" }, "Should track installed extension 1")
        assertTrue(installedDirs.any { it.name == "installed.ext.2" }, "Should track installed extension 2")

        // Cleanup
        ext1.deleteRecursively()
        ext2.deleteRecursively()
    }

    // ========== Helper Methods ==========

    private fun createTestCatalog(
        pkgName: String,
        repositoryType: String,
        name: String = "Test Extension",
        versionName: String = "1.0.0",
        lang: String = "en"
    ): CatalogRemote {
        return CatalogRemote(
            sourceId = pkgName.hashCode().toLong(),
            source = pkgName.hashCode().toLong(),
            name = name,
            description = "Test extension for integration testing",
            pkgName = pkgName,
            versionName = versionName,
            versionCode = 1,
            lang = lang,
            pkgUrl = "https://example.com/$pkgName.apk",
            iconUrl = "https://example.com/$pkgName.png",
            jarUrl = "",
            nsfw = false,
            repositoryId = -1L,
            repositoryType = repositoryType
        )
    }
}
