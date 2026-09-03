package ireader.domain.services.sync

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.sync.*
import ireader.domain.preferences.prefs.SyncPreferences
import ireader.domain.repositories.SyncLocalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedSyncEngineTest {

    private class TestPreferenceStore : PreferenceStore {
        val stringValues = mutableMapOf<String, String>()
        val booleanValues = mutableMapOf<String, Boolean>()
        val longValues = mutableMapOf<String, Long>()

        override fun getString(key: String, defaultValue: String): Preference<String> {
            return object : Preference<String> {
                override fun key(): String = key
                override fun get(): String = stringValues[key] ?: defaultValue
                override fun set(value: String) { stringValues[key] = value }
                override fun isSet(): Boolean = stringValues.containsKey(key)
                override fun delete() { stringValues.remove(key) }
                override fun defaultValue(): String = defaultValue
                override fun changes(): Flow<String> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<String> = MutableStateFlow(get())
            }
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
            return object : Preference<Boolean> {
                override fun key(): String = key
                override fun get(): Boolean = booleanValues[key] ?: defaultValue
                override fun set(value: Boolean) { booleanValues[key] = value }
                override fun isSet(): Boolean = booleanValues.containsKey(key)
                override fun delete() { booleanValues.remove(key) }
                override fun defaultValue(): Boolean = defaultValue
                override fun changes(): Flow<Boolean> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Boolean> = MutableStateFlow(get())
            }
        }

        override fun getLong(key: String, defaultValue: Long): Preference<Long> {
            return object : Preference<Long> {
                override fun key(): String = key
                override fun get(): Long = longValues[key] ?: defaultValue
                override fun set(value: Long) { longValues[key] = value }
                override fun isSet(): Boolean = longValues.containsKey(key)
                override fun delete() { longValues.remove(key) }
                override fun defaultValue(): Long = defaultValue
                override fun changes(): Flow<Long> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<Long> = MutableStateFlow(get())
            }
        }

        override fun getInt(key: String, defaultValue: Int): Preference<Int> = throw UnsupportedOperationException()
        override fun getFloat(key: String, defaultValue: Float): Preference<Float> = throw UnsupportedOperationException()
        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> = throw UnsupportedOperationException()
        override fun <T> getObject(key: String, defaultValue: T, serializer: (T) -> String, deserializer: (String) -> T): Preference<T> = throw UnsupportedOperationException()
        override fun <T> getJsonObject(key: String, defaultValue: T, serializer: KSerializer<T>, serializersModule: SerializersModule): Preference<T> = throw UnsupportedOperationException()
    }

    private class MockSyncLocalRepository : SyncLocalRepository {
        val books = mutableListOf<BookSyncData>()
        val history = mutableListOf<HistorySyncData>()
        val deletedIds = mutableListOf<String>()

        override suspend fun getBooks(): List<BookSyncData> = books
        override suspend fun applyBooks(books: List<BookSyncData>) {
            this.books.removeAll { existing -> books.any { it.globalId == existing.globalId } }
            this.books.addAll(books)
        }

        override suspend fun getHistory(): List<HistorySyncData> = history
        override suspend fun applyHistory(history: List<HistorySyncData>) {
            this.history.removeAll { existing -> history.any { it.chapterGlobalId == existing.chapterGlobalId } }
            this.history.addAll(history)
        }

        override suspend fun getChapters(includeDownloadedContent: Boolean): List<ChapterSyncData> = emptyList()
        override suspend fun applyChapters(chapters: List<ChapterSyncData>) {}
        override suspend fun deleteBooksByGlobalIds(globalIds: List<String>) {
            deletedIds.addAll(globalIds)
            books.removeAll { globalIds.contains(it.globalId) }
        }
    }

    private class MockSyncProvider(
        override val type: SyncProviderType = SyncProviderType.GOOGLE_DRIVE,
        override val name: String = "Mock Drive",
        var isAuth: Boolean = true
    ) : SyncProvider {
        var remoteManifest: UnifiedSyncManifest? = null
        var lastUploadedManifest: UnifiedSyncManifest? = null

        override suspend fun isAuthenticated(): Boolean = isAuth
        override suspend fun fetchRemoteManifest(): Result<UnifiedSyncManifest?> = Result.success(remoteManifest)
        override suspend fun uploadManifest(manifest: UnifiedSyncManifest): Result<Unit> {
            lastUploadedManifest = manifest
            return Result.success(Unit)
        }
    }

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `switch provider updates sync state`() {
        val prefStore = TestPreferenceStore()
        val prefs = SyncPreferences(prefStore)
        val provider = MockSyncProvider()
        val localRepo = MockSyncLocalRepository()

        val engine = UnifiedSyncEngine(
            syncPreferences = prefs,
            providers = listOf(provider),
            localRepository = localRepo,
            deviceId = "test-device"
        )

        assertEquals(SyncProviderType.NONE, engine.syncState.value.provider)

        engine.setProvider(SyncProviderType.GOOGLE_DRIVE)

        assertEquals(SyncProviderType.GOOGLE_DRIVE, engine.syncState.value.provider)
        assertEquals(SyncProviderType.GOOGLE_DRIVE, prefs.getSelectedProviderType())
    }

    @Test
    fun `delta merge applies newer remote books locally and updates manifest`() = runTest(testDispatcher) {
        val prefStore = TestPreferenceStore()
        val prefs = SyncPreferences(prefStore)
        prefs.setSelectedProviderType(SyncProviderType.GOOGLE_DRIVE)

        val provider = MockSyncProvider(
            type = SyncProviderType.GOOGLE_DRIVE,
            isAuth = true
        )
        provider.remoteManifest = UnifiedSyncManifest(
            version = 1,
            deviceId = "other-device",
            timestamp = 1000L,
            books = listOf(
                SyncBookItem(
                    globalId = "1-book-remote",
                    sourceId = 1L,
                    key = "book-remote",
                    title = "Remote Novel",
                    favorite = true,
                    lastModified = 2000L
                )
            )
        )

        val localRepo = MockSyncLocalRepository()
        localRepo.books.add(
            BookSyncData(
                globalId = "1-book-local",
                sourceId = "1",
                key = "book-local",
                title = "Local Novel",
                author = "",
                description = "",
                genres = emptyList(),
                status = 0L,
                coverUrl = "",
                favorite = true,
                updatedAt = 500L,
                addedAt = 500L
            )
        )

        val engine = UnifiedSyncEngine(
            syncPreferences = prefs,
            providers = listOf(provider),
            localRepository = localRepo,
            deviceId = "my-device-id"
        )

        val result = engine.syncNow()
        assertTrue(result.isSuccess)

        // Remote book should now exist in local data source
        assertTrue(localRepo.books.any { it.globalId == "1-book-remote" })
        assertEquals(2, localRepo.books.size)

        // Uploaded manifest should contain both books
        val uploaded = assertNotNull(provider.lastUploadedManifest)
        assertEquals(2, uploaded.books.size)
        assertTrue(uploaded.books.any { it.globalId == "1-book-remote" })
        assertTrue(uploaded.books.any { it.globalId == "1-book-local" })
    }


    @Test
    fun `tombstones delete local books without resurrecting`() = runTest(testDispatcher) {
        val prefStore = TestPreferenceStore()
        val prefs = SyncPreferences(prefStore)
        prefs.setSelectedProviderType(SyncProviderType.GOOGLE_DRIVE)

        val provider = MockSyncProvider(
            type = SyncProviderType.GOOGLE_DRIVE,
            isAuth = true
        )
        val now = ireader.core.util.currentTimeMillis()
        provider.remoteManifest = UnifiedSyncManifest(
            version = 1,
            deviceId = "other-device",
            timestamp = now,
            tombstones = listOf(
                SyncTombstone(
                    itemType = UniversalSyncItemType.BOOK,
                    globalId = "1-deleted-book",
                    deletedAt = now - 1000L
                )
            )
        )


        val localRepo = MockSyncLocalRepository()
        localRepo.books.add(
            BookSyncData(
                globalId = "1-deleted-book",
                sourceId = "1",
                key = "deleted-book",
                title = "Deleted Novel",
                author = "",
                description = "",
                genres = emptyList(),
                status = 0L,
                coverUrl = "",
                favorite = true,
                updatedAt = now - 5000L,
                addedAt = now - 5000L
            )
        )

        val engine = UnifiedSyncEngine(
            syncPreferences = prefs,
            providers = listOf(provider),
            localRepository = localRepo,
            deviceId = "my-device-id"
        )

        val result = engine.syncNow()
        assertTrue(result.isSuccess)

        // Book should have been deleted locally
        assertFalse(localRepo.books.any { it.globalId == "1-deleted-book" })
        assertTrue(localRepo.deletedIds.contains("1-deleted-book"))
    }
}
