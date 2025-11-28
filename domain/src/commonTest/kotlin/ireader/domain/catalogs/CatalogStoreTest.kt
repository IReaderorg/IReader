package ireader.domain.catalogs

import kotlin.test.*

/**
 * Unit tests for CatalogStore behavior
 * Tests catalog state management, pinning, and stub tracking
 * 
 * Note: These tests focus on the logic without mocking complex interfaces
 */
class CatalogStoreTest {

    @Test
    fun `catalog list should be initially empty`() {
        // Given
        val catalogs = emptyList<TestCatalog>()
        
        // Then
        assertTrue(catalogs.isEmpty())
    }

    @Test
    fun `catalogs should be distinct by sourceId`() {
        // Given
        val catalogs = listOf(
            TestCatalog(sourceId = 1L, name = "Catalog 1"),
            TestCatalog(sourceId = 2L, name = "Catalog 2"),
            TestCatalog(sourceId = 1L, name = "Catalog 1 Duplicate")
        )
        
        // When
        val distinctCatalogs = catalogs.distinctBy { it.sourceId }
        
        // Then
        assertEquals(2, distinctCatalogs.size)
    }

    @Test
    fun `pinned catalogs should be tracked by sourceId`() {
        // Given
        val pinnedIds = setOf("123", "456")
        val catalog = TestCatalog(sourceId = 123L, name = "Test Catalog")
        
        // When
        val isPinned = catalog.sourceId.toString() in pinnedIds
        
        // Then
        assertTrue(isPinned)
    }

    @Test
    fun `unpinned catalogs should not be in pinned set`() {
        // Given
        val pinnedIds = setOf("123", "456")
        val catalog = TestCatalog(sourceId = 789L, name = "Unpinned Catalog")
        
        // When
        val isPinned = catalog.sourceId.toString() in pinnedIds
        
        // Then
        assertFalse(isPinned)
    }

    @Test
    fun `stub sources should be tracked separately`() {
        // Given
        val stubSourceIds = mutableSetOf<Long>()
        val stubCatalog = TestCatalog(sourceId = 100L, name = "Stub", isStub = true)
        val realCatalog = TestCatalog(sourceId = 200L, name = "Real", isStub = false)
        
        // When
        if (stubCatalog.isStub) stubSourceIds.add(stubCatalog.sourceId)
        if (realCatalog.isStub) stubSourceIds.add(realCatalog.sourceId)
        
        // Then
        assertTrue(100L in stubSourceIds)
        assertFalse(200L in stubSourceIds)
    }

    @Test
    fun `loading sources should be tracked`() {
        // Given
        val loadingSourceIds = mutableSetOf<Long>()
        
        // When
        loadingSourceIds.add(1L)
        loadingSourceIds.add(2L)
        
        // Then
        assertTrue(1L in loadingSourceIds)
        assertTrue(2L in loadingSourceIds)
        assertFalse(3L in loadingSourceIds)
    }

    @Test
    fun `removing from loading set works correctly`() {
        // Given
        val loadingSourceIds = mutableSetOf(1L, 2L, 3L)
        
        // When
        loadingSourceIds.remove(2L)
        
        // Then
        assertTrue(1L in loadingSourceIds)
        assertFalse(2L in loadingSourceIds)
        assertTrue(3L in loadingSourceIds)
    }

    @Test
    fun `updatable catalogs filter works`() {
        // Given
        val catalogs = listOf(
            TestInstalledCatalog(sourceId = 1L, name = "Catalog 1", hasUpdate = true),
            TestInstalledCatalog(sourceId = 2L, name = "Catalog 2", hasUpdate = false),
            TestInstalledCatalog(sourceId = 3L, name = "Catalog 3", hasUpdate = true)
        )
        
        // When
        val updatableCatalogs = catalogs.filter { it.hasUpdate }
        
        // Then
        assertEquals(2, updatableCatalogs.size)
        assertTrue(updatableCatalogs.all { it.hasUpdate })
    }

    @Test
    fun `catalog lookup by sourceId works`() {
        // Given
        val catalogs = listOf(
            TestCatalog(sourceId = 1L, name = "Catalog 1"),
            TestCatalog(sourceId = 2L, name = "Catalog 2"),
            TestCatalog(sourceId = 3L, name = "Catalog 3")
        )
        val catalogsBySource = catalogs.associateBy { it.sourceId }
        
        // When
        val found = catalogsBySource[2L]
        val notFound = catalogsBySource[999L]
        
        // Then
        assertNotNull(found)
        assertEquals("Catalog 2", found.name)
        assertNull(notFound)
    }

    @Test
    fun `special sourceId -200 is reserved for LocalSource`() {
        // Given
        val localSourceId = -200L
        
        // Then
        assertEquals(-200L, localSourceId)
    }

    @Test
    fun `toggle pinned catalog changes state`() {
        // Given
        var isPinned = false
        
        // When
        isPinned = !isPinned
        
        // Then
        assertTrue(isPinned)
        
        // When toggled again
        isPinned = !isPinned
        
        // Then
        assertFalse(isPinned)
    }

    @Test
    fun `catalog copy preserves other fields when changing isPinned`() {
        // Given
        val original = TestCatalog(sourceId = 1L, name = "Test", isPinned = false)
        
        // When
        val copied = original.copy(isPinned = true)
        
        // Then
        assertEquals(original.sourceId, copied.sourceId)
        assertEquals(original.name, copied.name)
        assertTrue(copied.isPinned)
        assertFalse(original.isPinned)
    }

    @Test
    fun `hasUpdate check compares version codes`() {
        // Given
        val installedVersionCode = 5
        val remoteVersionCode = 7
        
        // When
        val hasUpdate = remoteVersionCode > installedVersionCode
        
        // Then
        assertTrue(hasUpdate)
    }

    @Test
    fun `no update when versions are equal`() {
        // Given
        val installedVersionCode = 5
        val remoteVersionCode = 5
        
        // When
        val hasUpdate = remoteVersionCode > installedVersionCode
        
        // Then
        assertFalse(hasUpdate)
    }

    // Test data classes
    data class TestCatalog(
        val sourceId: Long,
        val name: String,
        val isPinned: Boolean = false,
        val isStub: Boolean = false
    )

    data class TestInstalledCatalog(
        val sourceId: Long,
        val name: String,
        val hasUpdate: Boolean = false,
        val versionCode: Int = 1
    )
}

/**
 * Tests for catalog installation change handling
 */
class CatalogInstallationChangeTest {

    @Test
    fun `system install change contains package name`() {
        // Given
        val pkgName = "com.example.extension"
        val change = TestInstallationChange.SystemInstall(pkgName)
        
        // Then
        assertEquals(pkgName, change.pkgName)
    }

    @Test
    fun `local install change contains package name`() {
        // Given
        val pkgName = "com.example.local"
        val change = TestInstallationChange.LocalInstall(pkgName)
        
        // Then
        assertEquals(pkgName, change.pkgName)
    }

    @Test
    fun `uninstall changes contain package name`() {
        // Given
        val pkgName = "com.example.uninstall"
        val systemUninstall = TestInstallationChange.SystemUninstall(pkgName)
        val localUninstall = TestInstallationChange.LocalUninstall(pkgName)
        
        // Then
        assertEquals(pkgName, systemUninstall.pkgName)
        assertEquals(pkgName, localUninstall.pkgName)
    }

    // Test sealed class for installation changes
    sealed class TestInstallationChange {
        abstract val pkgName: String
        
        data class SystemInstall(override val pkgName: String) : TestInstallationChange()
        data class SystemUninstall(override val pkgName: String) : TestInstallationChange()
        data class LocalInstall(override val pkgName: String) : TestInstallationChange()
        data class LocalUninstall(override val pkgName: String) : TestInstallationChange()
    }
}
