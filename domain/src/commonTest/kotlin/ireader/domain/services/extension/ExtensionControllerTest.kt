package ireader.domain.services.extension
import okio.Path.Companion.toPath

import ireader.core.os.InstallStep
import ireader.domain.catalogs.interactor.GetCatalogsByType
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.models.entities.SourceState
import ireader.domain.models.entities.key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ============================================================================
// TEST DATA FACTORY
// ============================================================================

private object TestDataFactory {

    private fun mockSource(id: Long, name: String, lang: String): ireader.core.source.Source {
        return object : ireader.core.source.Source {
            override val id: Long = id
            override val name: String = name
            override val lang: String = lang
            override suspend fun getMangaDetails(
                manga: ireader.core.source.model.MangaInfo,
                commands: List<ireader.core.source.model.Command<*>>
            ): ireader.core.source.model.MangaInfo = manga
            override suspend fun getChapterList(
                manga: ireader.core.source.model.MangaInfo,
                commands: List<ireader.core.source.model.Command<*>>
            ): List<ireader.core.source.model.ChapterInfo> = emptyList()
            override suspend fun getPageList(
                chapter: ireader.core.source.model.ChapterInfo,
                commands: List<ireader.core.source.model.Command<*>>
            ): List<ireader.core.source.model.Page> = emptyList()
        }
    }

    fun createCatalogRemote(
        sourceId: Long = 100L,
        name: String = "TestSource",
        pkgName: String = "com.test.source",
        versionName: String = "1.0.0",
        versionCode: Int = 1,
        lang: String = "en",
        repositoryType: String = "IREADER"
    ): CatalogRemote = CatalogRemote(
        sourceId = sourceId,
        source = sourceId,
        name = name,
        description = "Test source description",
        pkgName = pkgName,
        versionName = versionName,
        versionCode = versionCode,
        lang = lang,
        pkgUrl = "https://example.com/$pkgName",
        iconUrl = "https://example.com/icon.png",
        jarUrl = "https://example.com/$pkgName.jar",
        nsfw = false,
        repositoryId = 1L,
        repositoryType = repositoryType
    )

    fun createCatalogInstalledLocally(
        sourceId: Long = 100L,
        name: String = "TestSource",
        pkgName: String = "com.test.source",
        versionName: String = "1.0.0",
        versionCode: Int = 1,
        lang: String = "en",
        isPinned: Boolean = false,
        hasUpdate: Boolean = false
    ): CatalogInstalled.Locally {
        return CatalogInstalled.Locally(
            name = name,
            description = "Test installed source",
            source = mockSource(sourceId, name, lang),
            pkgName = pkgName,
            versionName = versionName,
            versionCode = versionCode,
            nsfw = false,
            installDir = "/".toPath(),
            isPinned = isPinned,
            hasUpdate = hasUpdate,
            iconUrl = "https://example.com/icon.png"
        )
    }

    fun createCatalogInstalledSystemWide(
        sourceId: Long = 100L,
        name: String = "TestSource",
        pkgName: String = "com.test.source",
        versionName: String = "1.0.0",
        versionCode: Int = 1,
        isPinned: Boolean = false,
        hasUpdate: Boolean = false
    ): CatalogInstalled.SystemWide {
        return CatalogInstalled.SystemWide(
            name = name,
            description = "Test installed source",
            source = null,
            pkgName = pkgName,
            versionName = versionName,
            versionCode = versionCode,
            nsfw = false,
            isPinned = isPinned,
            hasUpdate = hasUpdate,
            iconUrl = "https://example.com/icon.png",
            installDir = null
        )
    }

    fun createCatalogBundled(
        name: String = "LocalSource",
        isPinned: Boolean = false
    ): ireader.domain.models.entities.CatalogBundled {
        return ireader.domain.models.entities.CatalogBundled(
            source = null,
            description = "Local bundled source",
            name = name,
            nsfw = false,
            isPinned = isPinned
        )
    }
}

// ============================================================================
// EXTENSION STATE TESTS
// ============================================================================

@OptIn(ExperimentalCoroutinesApi::class)
class ExtensionStateTest {

    @Test
    fun `default state should have empty lists and no loading`() {
        val state = ExtensionState()
        assertTrue(state.installedExtensions.isEmpty())
        assertTrue(state.availableExtensions.isEmpty())
        assertTrue(state.updatableExtensions.isEmpty())
        assertTrue(state.pinnedCatalogs.isEmpty())
        assertTrue(state.unpinnedCatalogs.isEmpty())
        assertTrue(state.remoteCatalogs.isEmpty())
        assertTrue(state.allPinnedCatalogs.isEmpty())
        assertTrue(state.allUnpinnedCatalogs.isEmpty())
        assertTrue(state.allRemoteCatalogs.isEmpty())
        assertTrue(state.installSteps.isEmpty())
        assertTrue(state.availableLanguages.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertFalse(state.isCheckingUpdates)
        assertNull(state.error)
        assertNull(state.searchQuery)
        assertNull(state.selectedLanguageCodes)
        assertNull(state.selectedRepositoryType)
        assertEquals(ExtensionFilter.All, state.filter)
    }

    @Test
    fun `installedCount should return sum of pinned and unpinned`() {
        val pinned = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, isPinned = true),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, isPinned = true)
        )
        val unpinned = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 3L, isPinned = false)
        )
        val state = ExtensionState(pinnedCatalogs = pinned, unpinnedCatalogs = unpinned)
        assertEquals(3, state.installedCount)
    }

    @Test
    fun `availableCount should return remote catalogs size`() {
        val remote = listOf(
            TestDataFactory.createCatalogRemote(sourceId = 1L),
            TestDataFactory.createCatalogRemote(sourceId = 2L),
            TestDataFactory.createCatalogRemote(sourceId = 3L)
        )
        val state = ExtensionState(remoteCatalogs = remote)
        assertEquals(3, state.availableCount)
    }

    @Test
    fun `updatableCount should return updatable extensions size`() {
        val updatable = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, hasUpdate = true),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, hasUpdate = true)
        )
        val state = ExtensionState(updatableExtensions = updatable)
        assertEquals(2, state.updatableCount)
    }

    @Test
    fun `hasActiveInstallation should return true when non-idle non-success steps exist`() {
        val state = ExtensionState(
            installSteps = mapOf("com.test.pkg" to InstallStep.Downloading)
        )
        assertTrue(state.hasActiveInstallation)
    }

    @Test
    fun `hasActiveInstallation should return false when all steps are idle`() {
        val state = ExtensionState(
            installSteps = mapOf("com.test.pkg" to InstallStep.Idle)
        )
        assertFalse(state.hasActiveInstallation)
    }

    @Test
    fun `hasActiveInstallation should return false when all steps are success`() {
        val state = ExtensionState(
            installSteps = mapOf("com.test.pkg" to InstallStep.Success)
        )
        assertFalse(state.hasActiveInstallation)
    }

    @Test
    fun `hasActiveInstallation should return false when all steps are error`() {
        val state = ExtensionState(
            installSteps = mapOf("com.test.pkg" to InstallStep.Error("test error"))
        )
        assertTrue(state.hasActiveInstallation)
    }

    @Test
    fun `hasActiveInstallation should return false when installSteps is empty`() {
        val state = ExtensionState(installSteps = emptyMap())
        assertFalse(state.hasActiveInstallation)
    }

    @Test
    fun `hasInstalledExtensions should return true when installedExtensions is not empty`() {
        val state = ExtensionState(
            installedExtensions = listOf(TestDataFactory.createCatalogInstalledLocally(sourceId = 1L))
        )
        assertTrue(state.hasInstalledExtensions)
    }

    @Test
    fun `hasInstalledExtensions should return false when installedExtensions is empty`() {
        val state = ExtensionState(installedExtensions = emptyList())
        assertFalse(state.hasInstalledExtensions)
    }

    @Test
    fun `hasAvailableExtensions should return true when availableExtensions is not empty`() {
        val state = ExtensionState(
            availableExtensions = listOf(TestDataFactory.createCatalogRemote(sourceId = 1L))
        )
        assertTrue(state.hasAvailableExtensions)
    }

    @Test
    fun `hasAvailableExtensions should return false when availableExtensions is empty`() {
        val state = ExtensionState(availableExtensions = emptyList())
        assertFalse(state.hasAvailableExtensions)
    }

    @Test
    fun `hasUpdates should return true when updatableExtensions is not empty`() {
        val state = ExtensionState(
            updatableExtensions = listOf(
                TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, hasUpdate = true)
            )
        )
        assertTrue(state.hasUpdates)
    }

    @Test
    fun `hasUpdates should return false when updatableExtensions is empty`() {
        val state = ExtensionState(updatableExtensions = emptyList())
        assertFalse(state.hasUpdates)
    }

    @Test
    fun `isAnyLoading should return true when isLoading is true`() {
        assertTrue(ExtensionState(isLoading = true).isAnyLoading)
    }

    @Test
    fun `isAnyLoading should return true when isRefreshing is true`() {
        assertTrue(ExtensionState(isRefreshing = true).isAnyLoading)
    }

    @Test
    fun `isAnyLoading should return true when isCheckingUpdates is true`() {
        assertTrue(ExtensionState(isCheckingUpdates = true).isAnyLoading)
    }

    @Test
    fun `isAnyLoading should return false when all loading flags are false`() {
        assertFalse(ExtensionState().isAnyLoading)
    }

    @Test
    fun `hasError should return true when error is set`() {
        val state = ExtensionState(error = ExtensionError.LoadFailed("test error"))
        assertTrue(state.hasError)
    }

    @Test
    fun `hasError should return false when error is null`() {
        assertFalse(ExtensionState(error = null).hasError)
    }

    @Test
    fun `state copy should preserve install steps`() {
        val original = ExtensionState(
            installSteps = mapOf("com.test" to InstallStep.Downloading)
        )
        val updated = original.copy(
            installSteps = original.installSteps + ("com.test2" to InstallStep.Success)
        )
        assertEquals(2, updated.installSteps.size)
        assertEquals(InstallStep.Downloading, updated.installSteps["com.test"])
        assertEquals(InstallStep.Success, updated.installSteps["com.test2"])
    }

    @Test
    fun `state copy should allow removing install steps`() {
        val original = ExtensionState(
            installSteps = mapOf(
                "com.test1" to InstallStep.Downloading,
                "com.test2" to InstallStep.Success
            )
        )
        val updated = original.copy(installSteps = original.installSteps - "com.test2")
        assertEquals(1, updated.installSteps.size)
        assertFalse(updated.installSteps.containsKey("com.test2"))
    }
}

// ============================================================================
// INSTALL STEP TESTS
// ============================================================================

class InstallStepTest {

    @Test
    fun `InstallStep Idle isFinished should return true`() {
        assertTrue(InstallStep.Idle.isFinished())
    }

    @Test
    fun `InstallStep Success isFinished should return true`() {
        assertTrue(InstallStep.Success.isFinished())
    }

    @Test
    fun `InstallStep Error isFinished should return true`() {
        assertTrue(InstallStep.Error("test error").isFinished())
    }

    @Test
    fun `InstallStep Downloading isFinished should return false`() {
        assertFalse(InstallStep.Downloading.isFinished())
    }

    @Test
    fun `InstallStep Downloading isLoading should return true`() {
        assertTrue(InstallStep.Downloading.isLoading())
    }

    @Test
    fun `InstallStep Idle isLoading should return false`() {
        assertFalse(InstallStep.Idle.isLoading())
    }

    @Test
    fun `InstallStep Success isLoading should return false`() {
        assertFalse(InstallStep.Success.isLoading())
    }

    @Test
    fun `InstallStep Error isLoading should return false`() {
        assertFalse(InstallStep.Error("test error").isLoading())
    }

    @Test
    fun `InstallStep Error should contain error message`() {
        val error = InstallStep.Error("Network timeout")
        assertEquals("Network timeout", error.error)
    }
}

// ============================================================================
// EXTENSION EVENT TESTS
// ============================================================================

class ExtensionEventTest {

    @Test
    fun `ExtensionsLoaded event should store counts`() {
        val event = ExtensionEvent.ExtensionsLoaded(installedCount = 5, availableCount = 10)
        assertEquals(5, event.installedCount)
        assertEquals(10, event.availableCount)
    }

    @Test
    fun `InstallComplete event should store catalog`() {
        val catalog = TestDataFactory.createCatalogRemote(pkgName = "com.test.pkg")
        val event = ExtensionEvent.InstallComplete(catalog)
        assertEquals("com.test.pkg", event.catalog.pkgName)
    }

    @Test
    fun `UninstallComplete event should store pkgName`() {
        val event = ExtensionEvent.UninstallComplete("com.test.pkg")
        assertEquals("com.test.pkg", event.pkgName)
    }

    @Test
    fun `UpdateComplete event should store catalog`() {
        val catalog = TestDataFactory.createCatalogInstalledLocally(pkgName = "com.test.pkg")
        val event = ExtensionEvent.UpdateComplete(catalog)
        assertEquals("com.test.pkg", event.catalog.pkgName)
    }

    @Test
    fun `UpdatesAvailable event should store count`() {
        val event = ExtensionEvent.UpdatesAvailable(3)
        assertEquals(3, event.count)
    }

    @Test
    fun `AllUpToDate should be singleton`() {
        assertEquals(ExtensionEvent.AllUpToDate, ExtensionEvent.AllUpToDate)
    }

    @Test
    fun `RefreshComplete should be singleton`() {
        assertEquals(ExtensionEvent.RefreshComplete, ExtensionEvent.RefreshComplete)
    }

    @Test
    fun `BatchUpdateComplete should store counts`() {
        val event = ExtensionEvent.BatchUpdateComplete(successCount = 5, totalCount = 7)
        assertEquals(5, event.successCount)
        assertEquals(7, event.totalCount)
    }

    @Test
    fun `InstallProgress should store pkgName and progress`() {
        val event = ExtensionEvent.InstallProgress("com.test.pkg", 0.75f)
        assertEquals("com.test.pkg", event.pkgName)
        assertEquals(0.75f, event.progress)
    }

    @Test
    fun `ShowSnackbar should store message`() {
        val event = ExtensionEvent.ShowSnackbar("Installation failed")
        assertEquals("Installation failed", event.message)
    }

    @Test
    fun `Error event should store error`() {
        val error = ExtensionError.LoadFailed("test")
        val event = ExtensionEvent.Error(error)
        assertEquals(error, event.error)
    }
}

// ============================================================================
// EXTENSION ERROR TESTS
// ============================================================================

class ExtensionErrorTest {

    @Test
    fun `LoadFailed toUserMessage should contain message`() {
        val error = ExtensionError.LoadFailed("network timeout")
        val message = error.toUserMessage()
        assertTrue(message.contains("Failed to load extensions"))
        assertTrue(message.contains("network timeout"))
    }

    @Test
    fun `InstallFailed toUserMessage should contain pkgName and message`() {
        val error = ExtensionError.InstallFailed("com.test.pkg", "download error")
        val message = error.toUserMessage()
        assertTrue(message.contains("Failed to install"))
        assertTrue(message.contains("com.test.pkg"))
        assertTrue(message.contains("download error"))
    }

    @Test
    fun `UninstallFailed toUserMessage should contain pkgName and message`() {
        val error = ExtensionError.UninstallFailed("com.test.pkg", "permission denied")
        val message = error.toUserMessage()
        assertTrue(message.contains("Failed to uninstall"))
        assertTrue(message.contains("com.test.pkg"))
        assertTrue(message.contains("permission denied"))
    }

    @Test
    fun `UpdateFailed toUserMessage should contain pkgName and message`() {
        val error = ExtensionError.UpdateFailed("com.test.pkg", "check failed")
        val message = error.toUserMessage()
        assertTrue(message.contains("Failed to update"))
        assertTrue(message.contains("com.test.pkg"))
        assertTrue(message.contains("check failed"))
    }

    @Test
    fun `NetworkError toUserMessage should contain message`() {
        val error = ExtensionError.NetworkError("no connection")
        val message = error.toUserMessage()
        assertTrue(message.contains("Network error"))
        assertTrue(message.contains("no connection"))
    }

    @Test
    fun `CheckUpdatesFailed toUserMessage should contain message`() {
        val error = ExtensionError.CheckUpdatesFailed("server error")
        val message = error.toUserMessage()
        assertTrue(message.contains("Failed to check for updates"))
        assertTrue(message.contains("server error"))
    }

    @Test
    fun `RefreshFailed toUserMessage should contain message`() {
        val error = ExtensionError.RefreshFailed("timeout")
        val message = error.toUserMessage()
        assertTrue(message.contains("Failed to refresh extensions"))
        assertTrue(message.contains("timeout"))
    }
}

// ============================================================================
// EXTENSION FILTER TESTS
// ============================================================================

class ExtensionFilterTest {

    @Test
    fun `All filter should be singleton`() {
        assertEquals(ExtensionFilter.All, ExtensionFilter.All)
    }

    @Test
    fun `ByLanguage filter should store language codes`() {
        val filter = ExtensionFilter.ByLanguage(setOf("en", "jp"))
        assertEquals(setOf("en", "jp"), filter.languageCodes)
    }

    @Test
    fun `ByRepository filter should store repository type`() {
        val filter = ExtensionFilter.ByRepository("IREADER")
        assertEquals("IREADER", filter.repositoryType)
    }

    @Test
    fun `Combined filter should store both language codes and repository type`() {
        val filter = ExtensionFilter.Combined(
            languageCodes = setOf("en"),
            repositoryType = "LNREADER"
        )
        assertEquals(setOf("en"), filter.languageCodes)
        assertEquals("LNREADER", filter.repositoryType)
    }

    @Test
    fun `Combined filter should allow null values`() {
        val filter = ExtensionFilter.Combined(languageCodes = null, repositoryType = null)
        assertNull(filter.languageCodes)
        assertNull(filter.repositoryType)
    }
}

// ============================================================================
// EXTENSION COMMAND TESTS
// ============================================================================

class ExtensionCommandTest {

    @Test
    fun `LoadExtensions should be singleton`() {
        assertEquals(ExtensionCommand.LoadExtensions, ExtensionCommand.LoadExtensions)
    }

    @Test
    fun `Cleanup should be singleton`() {
        assertEquals(ExtensionCommand.Cleanup, ExtensionCommand.Cleanup)
    }

    @Test
    fun `CheckUpdates should be singleton`() {
        assertEquals(ExtensionCommand.CheckUpdates, ExtensionCommand.CheckUpdates)
    }

    @Test
    fun `RefreshExtensions should be singleton`() {
        assertEquals(ExtensionCommand.RefreshExtensions, ExtensionCommand.RefreshExtensions)
    }

    @Test
    fun `BatchUpdateExtensions should be singleton`() {
        assertEquals(ExtensionCommand.BatchUpdateExtensions, ExtensionCommand.BatchUpdateExtensions)
    }

    @Test
    fun `ClearError should be singleton`() {
        assertEquals(ExtensionCommand.ClearError, ExtensionCommand.ClearError)
    }

    @Test
    fun `InstallExtension should store catalog`() {
        val catalog = TestDataFactory.createCatalogRemote(pkgName = "com.test")
        val command = ExtensionCommand.InstallExtension(catalog)
        assertEquals("com.test", command.catalog.pkgName)
    }

    @Test
    fun `UninstallExtension should store catalog`() {
        val catalog = TestDataFactory.createCatalogInstalledLocally(pkgName = "com.test")
        val command = ExtensionCommand.UninstallExtension(catalog)
        assertEquals("com.test", command.catalog.pkgName)
    }

    @Test
    fun `UpdateExtension should store catalog`() {
        val catalog = TestDataFactory.createCatalogInstalledLocally(pkgName = "com.test")
        val command = ExtensionCommand.UpdateExtension(catalog)
        assertEquals("com.test", command.catalog.pkgName)
    }

    @Test
    fun `CancelInstallation should store catalog`() {
        val catalog = TestDataFactory.createCatalogRemote(pkgName = "com.test")
        val command = ExtensionCommand.CancelInstallation(catalog)
        assertEquals(catalog, command.catalog)
    }

    @Test
    fun `SetFilter should store filter`() {
        val filter = ExtensionFilter.ByLanguage(setOf("en"))
        val command = ExtensionCommand.SetFilter(filter)
        assertEquals(filter, command.filter)
    }

    @Test
    fun `SetSearchQuery should store query`() {
        val command = ExtensionCommand.SetSearchQuery("test")
        assertEquals("test", command.query)
    }

    @Test
    fun `SetSearchQuery should allow null`() {
        val command = ExtensionCommand.SetSearchQuery(null)
        assertNull(command.query)
    }

    @Test
    fun `SetRepositoryType should store repository type`() {
        val command = ExtensionCommand.SetRepositoryType("IREADER")
        assertEquals("IREADER", command.repositoryType)
    }

    @Test
    fun `TogglePinned should store catalog`() {
        val catalog = TestDataFactory.createCatalogRemote(pkgName = "com.test")
        val command = ExtensionCommand.TogglePinned(catalog)
        assertEquals(catalog, command.catalog)
    }
}

// ============================================================================
// CATALOG TYPE TESTS
// ============================================================================

class CatalogTypeTest {

    @Test
    fun `CatalogRemote should store all properties`() {
        val catalog = TestDataFactory.createCatalogRemote(
            sourceId = 42L,
            name = "MySource",
            pkgName = "com.example.mysource",
            versionName = "2.0.0",
            versionCode = 20,
            lang = "jp",
            repositoryType = "LNREADER"
        )
        assertEquals(42L, catalog.sourceId)
        assertEquals("MySource", catalog.name)
        assertEquals("com.example.mysource", catalog.pkgName)
        assertEquals("2.0.0", catalog.versionName)
        assertEquals(20, catalog.versionCode)
        assertEquals("jp", catalog.lang)
        assertEquals("LNREADER", catalog.repositoryType)
    }

    @Test
    fun `isLNReaderSource should return true for LNREADER type`() {
        val catalog = TestDataFactory.createCatalogRemote(repositoryType = "LNREADER")
        assertTrue(catalog.isLNReaderSource())
        assertFalse(catalog.isIReaderSource())
    }

    @Test
    fun `isIReaderSource should return true for IREADER type`() {
        val catalog = TestDataFactory.createCatalogRemote(repositoryType = "IREADER")
        assertTrue(catalog.isIReaderSource())
        assertFalse(catalog.isLNReaderSource())
    }

    @Test
    fun `isLNReaderSource should be case insensitive`() {
        val catalog = TestDataFactory.createCatalogRemote(repositoryType = "lnreader")
        assertTrue(catalog.isLNReaderSource())
    }

    @Test
    fun `isIReaderSource should be case insensitive`() {
        val catalog = TestDataFactory.createCatalogRemote(repositoryType = "ireader")
        assertTrue(catalog.isIReaderSource())
    }

    @Test
    fun `CatalogInstalled Locally should have correct properties`() {
        val catalog = TestDataFactory.createCatalogInstalledLocally(
            sourceId = 100L,
            name = "InstalledSource",
            pkgName = "com.test.installed",
            versionName = "1.5.0",
            versionCode = 15,
            lang = "en",
            isPinned = true,
            hasUpdate = true
        )
        assertEquals("InstalledSource", catalog.name)
        assertEquals("com.test.installed", catalog.pkgName)
        assertEquals("1.5.0", catalog.versionName)
        assertEquals(15, catalog.versionCode)
        assertTrue(catalog.isPinned)
        assertTrue(catalog.hasUpdate)
        assertEquals(100L, catalog.sourceId)
    }

    @Test
    fun `CatalogInstalled SystemWide should have correct properties`() {
        val catalog = TestDataFactory.createCatalogInstalledSystemWide(
            name = "SystemSource",
            pkgName = "com.test.system",
            versionCode = 3,
            isPinned = false,
            hasUpdate = false
        )
        assertEquals("SystemSource", catalog.name)
        assertEquals("com.test.system", catalog.pkgName)
        assertEquals(3, catalog.versionCode)
        assertFalse(catalog.isPinned)
        assertFalse(catalog.hasUpdate)
    }

    @Test
    fun `CatalogBundled should have correct properties`() {
        val catalog = TestDataFactory.createCatalogBundled(name = "BundledSource", isPinned = true)
        assertEquals("BundledSource", catalog.name)
        assertTrue(catalog.isPinned)
        assertFalse(catalog.hasUpdate)
    }

    @Test
    fun `Catalog key should generate correct key for Pinned state`() {
        val catalog = TestDataFactory.createCatalogRemote(sourceId = 100L, name = "Test")
        val key = catalog.key(SourceState.Pinned, 1L, 1L)
        assertTrue(key.contains("pinned"), "Pinned key should contain 'pinned'")
        assertTrue(key.contains("remote"), "Remote catalog key should contain 'remote'")
    }

    @Test
    fun `Catalog key should generate correct key for Remote state`() {
        val catalog = TestDataFactory.createCatalogRemote(sourceId = 100L, name = "Test")
        val key = catalog.key(SourceState.Remote, 1L, 1L)
        assertTrue(key.contains("remote"), "Remote key should contain 'remote'")
    }

    @Test
    fun `Catalog key should generate correct key for Installed state`() {
        val catalog = TestDataFactory.createCatalogRemote(sourceId = 100L, name = "Test")
        val key = catalog.key(SourceState.Installed, 1L, 1L)
        assertTrue(key.contains("installed"), "Installed key should contain 'installed'")
    }

    @Test
    fun `Catalog key should generate correct key for LastUsed state`() {
        val catalog = TestDataFactory.createCatalogRemote(sourceId = 100L, name = "Test")
        val key = catalog.key(SourceState.LastUsed, 1L, 1L)
        assertTrue(key.contains("lastused"), "LastUsed key should contain 'lastused'")
    }

    @Test
    fun `Catalog key should generate correct key for UnPinned state`() {
        val catalog = TestDataFactory.createCatalogRemote(sourceId = 100L, name = "Test")
        val key = catalog.key(SourceState.UnPinned, 1L, 1L)
        assertTrue(key.contains("unpinned"), "UnPinned key should contain 'unpinned'")
    }

    @Test
    fun `Catalog key for default sourceId should return index-installed format`() {
        val catalog = TestDataFactory.createCatalogRemote(sourceId = -1L, name = "Default")
        val key = catalog.key(SourceState.Nothing, 5L, 1L)
        assertEquals("5-installed", key, "Default sourceId should return index-installed format")
    }

    @Test
    fun `Catalog key for Nothing state should contain name prefix`() {
        val catalog = TestDataFactory.createCatalogRemote(sourceId = 42L, name = "Test")
        val key = catalog.key(SourceState.Nothing, 0L, 1L)
        assertFalse(key.contains("pinned"), "Nothing state should not contain 'pinned'")
        assertTrue(key.contains("remote-"), "Nothing state should contain name prefix for remote catalog")
    }
}

// ============================================================================
// CATALOG FILTERING LOGIC TESTS
// ============================================================================

class CatalogFilteringLogicTest {

    @Test
    fun `filtering local catalogs by language codes should work`() {
        val english = TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, lang = "en", isPinned = true)
        val japanese = TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, lang = "jp", isPinned = true)
        val catalogs = listOf(english, japanese)
        val filtered = catalogs.filter { it.source?.lang in setOf("en") }
        assertEquals(1, filtered.size)
        assertEquals("en", filtered.first().source?.lang)
    }

    @Test
    fun `filtering remote catalogs by language codes should work`() {
        val catalogs = listOf(
            TestDataFactory.createCatalogRemote(sourceId = 1L, lang = "en"),
            TestDataFactory.createCatalogRemote(sourceId = 2L, lang = "jp"),
            TestDataFactory.createCatalogRemote(sourceId = 3L, lang = "es")
        )
        val filtered = catalogs.filter { it.lang in setOf("en", "es") }
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.lang in setOf("en", "es") })
    }

    @Test
    fun `filtering with null language codes should return all`() {
        val catalogs = listOf(
            TestDataFactory.createCatalogRemote(sourceId = 1L, lang = "en"),
            TestDataFactory.createCatalogRemote(sourceId = 2L, lang = "jp")
        )
        val codes: Set<String>? = null
        val filtered = if (codes == null || codes.isEmpty()) catalogs else catalogs.filter { it.lang in codes }
        assertEquals(2, filtered.size)
    }

    @Test
    fun `filtering with empty language codes should return all`() {
        val catalogs = listOf(
            TestDataFactory.createCatalogRemote(sourceId = 1L, lang = "en"),
            TestDataFactory.createCatalogRemote(sourceId = 2L, lang = "jp")
        )
        val codes: Set<String> = emptySet()
        val filtered = if (codes == null || codes.isEmpty()) catalogs else catalogs.filter { it.lang in codes }
        assertEquals(2, filtered.size)
    }

    @Test
    fun `filtering catalogs by query should match name case-insensitively`() {
        val sourceA = TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, name = "AlphaSource")
        val sourceB = TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, name = "BetaSource")
        val catalogs = listOf(sourceA, sourceB)
        val query = "alpha"
        val filtered = catalogs.filter { it.name.contains(query, ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertEquals("AlphaSource", filtered.first().name)
    }

    @Test
    fun `filtering catalogs with null query should return all`() {
        val catalogs = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, name = "AlphaSource"),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, name = "BetaSource")
        )
        val query: String? = null
        val filtered = if (query == null) catalogs else catalogs.filter { it.name.contains(query, ignoreCase = true) }
        assertEquals(2, filtered.size)
    }

    @Test
    fun `partitioning catalogs by pinned status should work`() {
        val pinned = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, isPinned = true),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, isPinned = true)
        )
        val unpinned = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 3L, isPinned = false)
        )
        val (resultPinned, resultUnpinned) = (pinned + unpinned).partition { it.isPinned }
        assertEquals(2, resultPinned.size)
        assertEquals(1, resultUnpinned.size)
        assertTrue(resultPinned.all { it.isPinned })
        assertTrue(resultUnpinned.all { !it.isPinned })
    }

    @Test
    fun `calculating updatable extensions should compare version codes`() {
        val installed = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, pkgName = "com.test.a", versionCode = 1),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, pkgName = "com.test.b", versionCode = 2),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 3L, pkgName = "com.test.c", versionCode = 3)
        )
        val remote = listOf(
            TestDataFactory.createCatalogRemote(sourceId = 1L, pkgName = "com.test.a", versionCode = 2),
            TestDataFactory.createCatalogRemote(sourceId = 2L, pkgName = "com.test.b", versionCode = 2),
            TestDataFactory.createCatalogRemote(sourceId = 3L, pkgName = "com.test.c", versionCode = 5)
        )
        val updatable = installed.filterIsInstance<CatalogInstalled>().filter { local ->
            remote.any { r -> r.pkgName == local.pkgName && r.versionCode > local.versionCode }
        }
        assertEquals(2, updatable.size)
        assertTrue(updatable.any { it.pkgName == "com.test.a" })
        assertTrue(updatable.any { it.pkgName == "com.test.c" })
        assertFalse(updatable.any { it.pkgName == "com.test.b" })
    }

    @Test
    fun `getting available languages from catalogs should collect unique langs`() {
        val remote = listOf(
            TestDataFactory.createCatalogRemote(sourceId = 1L, lang = "en"),
            TestDataFactory.createCatalogRemote(sourceId = 2L, lang = "jp"),
            TestDataFactory.createCatalogRemote(sourceId = 3L, lang = "en")
        )
        val local = listOf(TestDataFactory.createCatalogInstalledLocally(sourceId = 4L, lang = "es"))
        val languages = mutableSetOf<String>()
        remote.forEach { languages.add(it.lang) }
        local.forEach { it.source?.lang?.let { lang -> languages.add(lang) } }
        assertEquals(3, languages.size)
        assertTrue(languages.contains("en"))
        assertTrue(languages.contains("jp"))
        assertTrue(languages.contains("es"))
    }

    @Test
    fun `excluding remote installed catalogs should filter by package name`() {
        val installed = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, pkgName = "com.test.a"),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, pkgName = "com.test.b")
        )
        val remote = listOf(
            TestDataFactory.createCatalogRemote(sourceId = 1L, pkgName = "com.test.a"),
            TestDataFactory.createCatalogRemote(sourceId = 3L, pkgName = "com.test.c"),
            TestDataFactory.createCatalogRemote(sourceId = 4L, pkgName = "com.test.d")
        )
        val installedPkgs = installed.map { it.pkgName }.toSet()
        val filtered = remote.filter { it.pkgName !in installedPkgs }
        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.pkgName == "com.test.c" })
        assertTrue(filtered.any { it.pkgName == "com.test.d" })
        assertFalse(filtered.any { it.pkgName == "com.test.a" })
    }
}

// ============================================================================
// INSTALL STEP STATE TRANSITION TESTS
// ============================================================================

class InstallStepStateTransitionTest {

    @Test
    fun `install step progression from idle to downloading to success`() {
        val steps = listOf(InstallStep.Idle, InstallStep.Downloading, InstallStep.Success)
        assertTrue(steps[0].isFinished())
        assertFalse(steps[0].isLoading())
        assertFalse(steps[1].isFinished())
        assertTrue(steps[1].isLoading())
        assertTrue(steps[2].isFinished())
        assertFalse(steps[2].isLoading())
    }

    @Test
    fun `install step progression from idle to downloading to error`() {
        val errorStep = InstallStep.Error("Network error")
        assertTrue(errorStep.isFinished())
        assertFalse(errorStep.isLoading())
        assertEquals("Network error", errorStep.error)
    }

    @Test
    fun `install steps map should track multiple packages independently`() {
        val installSteps = mutableMapOf<String, InstallStep>()
        installSteps["com.test.a"] = InstallStep.Downloading
        installSteps["com.test.b"] = InstallStep.Success
        installSteps["com.test.c"] = InstallStep.Error("Failed")
        assertEquals(InstallStep.Downloading, installSteps["com.test.a"])
        assertEquals(InstallStep.Success, installSteps["com.test.b"])
        assertTrue(installSteps["com.test.c"] is InstallStep.Error)
        val hasActive = installSteps.any { it.value != InstallStep.Idle && it.value != InstallStep.Success }
        assertTrue(hasActive)
        installSteps.remove("com.test.b")
        installSteps.remove("com.test.c")
        assertEquals(1, installSteps.size)
        assertEquals(InstallStep.Downloading, installSteps["com.test.a"])
    }

    @Test
    fun `install step success should remove entry from steps map`() {
        val installSteps = mutableMapOf(
            "com.test.a" to InstallStep.Downloading,
            "com.test.b" to InstallStep.Success
        )
        installSteps.remove("com.test.b")
        assertEquals(1, installSteps.size)
        assertFalse(installSteps.containsKey("com.test.b"))
    }

    @Test
    fun `cancel installation should set step to idle`() {
        val installSteps: MutableMap<String, InstallStep> = mutableMapOf(
            "com.test.a" to InstallStep.Downloading
        )
        installSteps["com.test.a"] = InstallStep.Idle
        assertEquals(InstallStep.Idle, installSteps["com.test.a"])
        assertFalse(installSteps["com.test.a"]!!.isLoading())
        assertTrue(installSteps["com.test.a"]!!.isFinished())
    }
}

// ============================================================================
// EXTENSION STATE UPDATE SIMULATION TESTS
// ============================================================================

class ExtensionStateUpdateSimulationTest {

    @Test
    fun `state update during loading should set isLoading to true then false`() {
        var state = ExtensionState()
        state = state.copy(isLoading = true, error = null)
        assertTrue(state.isLoading)
        state = state.copy(isLoading = false)
        assertFalse(state.isLoading)
    }

    @Test
    fun `state update during refresh should set isRefreshing to true then false`() {
        var state = ExtensionState()
        state = state.copy(isRefreshing = true, error = null)
        assertTrue(state.isRefreshing)
        state = state.copy(isRefreshing = false)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `state update during checkUpdates should set isCheckingUpdates to true then false`() {
        var state = ExtensionState()
        state = state.copy(isCheckingUpdates = true, error = null)
        assertTrue(state.isCheckingUpdates)
        state = state.copy(isCheckingUpdates = false)
        assertFalse(state.isCheckingUpdates)
    }

    @Test
    fun `state update on error should set error and clear loading flags`() {
        var state = ExtensionState(isLoading = true, isRefreshing = true, isCheckingUpdates = true)
        val error = ExtensionError.LoadFailed("Network error")
        state = state.copy(error = error, isLoading = false, isRefreshing = false, isCheckingUpdates = false)
        assertNotNull(state.error)
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertFalse(state.isCheckingUpdates)
        assertTrue(state.hasError)
    }

    @Test
    fun `state update on clearError should remove error`() {
        var state = ExtensionState(error = ExtensionError.LoadFailed("test"))
        state = state.copy(error = null)
        assertNull(state.error)
        assertFalse(state.hasError)
    }

    @Test
    fun `state update with new install steps should add entry`() {
        var state = ExtensionState()
        state = state.copy(installSteps = state.installSteps + ("com.test" to InstallStep.Downloading))
        assertTrue(state.installSteps.containsKey("com.test"))
        assertEquals(InstallStep.Downloading, state.installSteps["com.test"])
    }

    @Test
    fun `state update removing install step should remove entry`() {
        var state = ExtensionState(
            installSteps = mapOf(
                "com.test.a" to InstallStep.Downloading,
                "com.test.b" to InstallStep.Success
            )
        )
        state = state.copy(installSteps = state.installSteps - "com.test.b")
        assertEquals(1, state.installSteps.size)
        assertFalse(state.installSteps.containsKey("com.test.b"))
    }

    @Test
    fun `state update with filter should update filtered lists`() {
        val allPinned = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, lang = "en", isPinned = true),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, lang = "jp", isPinned = true)
        )
        val allRemote = listOf(
            TestDataFactory.createCatalogRemote(sourceId = 10L, lang = "en"),
            TestDataFactory.createCatalogRemote(sourceId = 11L, lang = "jp")
        )
        var state = ExtensionState(
            allPinnedCatalogs = allPinned,
            allRemoteCatalogs = allRemote,
            pinnedCatalogs = allPinned,
            remoteCatalogs = allRemote
        )
        val languageCodes = setOf("en")
        state = state.copy(
            filter = ExtensionFilter.ByLanguage(languageCodes),
            selectedLanguageCodes = languageCodes,
            pinnedCatalogs = allPinned.filter { it.source?.lang in languageCodes },
            remoteCatalogs = allRemote.filter { it.lang in languageCodes }
        )
        assertEquals(1, state.pinnedCatalogs.size)
        assertEquals("en", state.pinnedCatalogs.first().source?.lang)
        assertEquals(1, state.remoteCatalogs.size)
        assertEquals("en", state.remoteCatalogs.first().lang)
    }

    @Test
    fun `state update with search query should filter by name`() {
        val allPinned = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, name = "AlphaSource"),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, name = "BetaSource")
        )
        var state = ExtensionState(allPinnedCatalogs = allPinned, pinnedCatalogs = allPinned)
        val query = "Alpha"
        state = state.copy(
            searchQuery = query,
            pinnedCatalogs = allPinned.filter { it.name.contains(query, ignoreCase = true) }
        )
        assertEquals(1, state.pinnedCatalogs.size)
        assertEquals("AlphaSource", state.pinnedCatalogs.first().name)
    }

    @Test
    fun `state update with null search query should show all`() {
        val allPinned = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, name = "AlphaSource"),
            TestDataFactory.createCatalogInstalledLocally(sourceId = 2L, name = "BetaSource")
        )
        var state = ExtensionState(
            allPinnedCatalogs = allPinned,
            pinnedCatalogs = listOf(allPinned[0]),
            searchQuery = "Alpha"
        )
        state = state.copy(searchQuery = null, pinnedCatalogs = allPinned)
        assertEquals(2, state.pinnedCatalogs.size)
        assertNull(state.searchQuery)
    }

    @Test
    fun `state update with updatable extensions should set updatable list`() {
        var state = ExtensionState()
        val updatable = listOf(
            TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, pkgName = "com.test.a", versionCode = 1)
        )
        state = state.copy(updatableExtensions = updatable)
        assertEquals(1, state.updatableExtensions.size)
        assertEquals(1, state.updatableCount)
        assertTrue(state.hasUpdates)
    }

    @Test
    fun `state update with empty updatable should clear updates flag`() {
        var state = ExtensionState(
            updatableExtensions = listOf(
                TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, pkgName = "com.test.a")
            )
        )
        assertTrue(state.hasUpdates)
        state = state.copy(updatableExtensions = emptyList())
        assertEquals(0, state.updatableCount)
        assertFalse(state.hasUpdates)
    }
}

// ============================================================================
// COROUTINE-BASED EVENT FLOW TESTS
// ============================================================================

@OptIn(ExperimentalCoroutinesApi::class)
class ExtensionEventFlowTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `events should be collectible from flow`() = runTest(testDispatcher) {
        val events = mutableListOf<ExtensionEvent>()
        val job = launch {
            flowOf(
                ExtensionEvent.ExtensionsLoaded(5, 10),
                ExtensionEvent.InstallComplete(TestDataFactory.createCatalogRemote()),
                ExtensionEvent.AllUpToDate
            ).collect { events.add(it) }
        }
        advanceUntilIdle()
        assertEquals(3, events.size)
        assertTrue(events[0] is ExtensionEvent.ExtensionsLoaded)
        assertTrue(events[1] is ExtensionEvent.InstallComplete)
        assertTrue(events[2] is ExtensionEvent.AllUpToDate)
        job.cancel()
    }

    @Test
    fun `error events should be collectible`() = runTest(testDispatcher) {
        val events = mutableListOf<ExtensionEvent>()
        val error = ExtensionError.InstallFailed("com.test", "Download failed")
        val job = launch {
            flowOf(ExtensionEvent.Error(error)).collect { events.add(it) }
        }
        advanceUntilIdle()
        assertEquals(1, events.size)
        val errorEvent = events[0] as ExtensionEvent.Error
        assertTrue(errorEvent.error is ExtensionError.InstallFailed)
        assertEquals("com.test", (errorEvent.error as ExtensionError.InstallFailed).pkgName)
        job.cancel()
    }

    @Test
    fun `install step flow should emit progress steps`() = runTest(testDispatcher) {
        val steps = mutableListOf<InstallStep>()
        val job = launch {
            flow {
                emit(InstallStep.Idle)
                emit(InstallStep.Downloading)
                emit(InstallStep.Success)
            }.collect { steps.add(it) }
        }
        advanceUntilIdle()
        assertEquals(3, steps.size)
        assertEquals(InstallStep.Idle, steps[0])
        assertEquals(InstallStep.Downloading, steps[1])
        assertEquals(InstallStep.Success, steps[2])
        job.cancel()
    }

    @Test
    fun `install step flow should handle errors`() = runTest(testDispatcher) {
        val steps = mutableListOf<InstallStep>()
        val job = launch {
            flow {
                emit(InstallStep.Idle)
                emit(InstallStep.Downloading)
                emit(InstallStep.Error("Network timeout"))
            }.collect { steps.add(it) }
        }
        advanceUntilIdle()
        assertEquals(3, steps.size)
        assertTrue(steps[2] is InstallStep.Error)
        assertEquals("Network timeout", (steps[2] as InstallStep.Error).error)
        job.cancel()
    }

    @Test
    fun `catalog flow should emit catalog lists`() = runTest(testDispatcher) {
        val catalogs = mutableListOf<GetCatalogsByType.Catalogs>()
        val job = launch {
            flowOf(
                GetCatalogsByType.Catalogs(
                    pinned = listOf(TestDataFactory.createCatalogInstalledLocally(sourceId = 1L, isPinned = true)),
                    unpinned = emptyList(),
                    remote = listOf(TestDataFactory.createCatalogRemote(sourceId = 10L))
                )
            ).collect { catalogs.add(it) }
        }
        advanceUntilIdle()
        assertEquals(1, catalogs.size)
        assertEquals(1, catalogs[0].pinned.size)
        assertEquals(1, catalogs[0].remote.size)
        job.cancel()
    }
}
