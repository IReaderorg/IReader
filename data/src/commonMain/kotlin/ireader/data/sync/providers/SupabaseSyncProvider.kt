package ireader.data.sync.providers

import ireader.core.log.Log
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.SyncedBook
import ireader.domain.models.sync.SyncBookItem
import ireader.domain.models.sync.SyncProgressItem
import ireader.domain.models.sync.SyncProviderType
import ireader.domain.models.sync.UnifiedSyncManifest
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.domain.services.sync.SyncProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Supabase implementation of SyncProvider.
 * Synchronizes books, reading progress, and manifests with user-owned Supabase instances.
 */
class SupabaseSyncProvider(
    private val remoteRepository: RemoteRepository,
    private val supabasePreferences: SupabasePreferences
) : SyncProvider {

    companion object {
        private const val TAG = "SupabaseSyncProvider"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override val type: SyncProviderType = SyncProviderType.SUPABASE
    override val name: String = "Supabase Cloud"

    override suspend fun isAuthenticated(): Boolean {
        return supabasePreferences.isPersonalSupabaseConfigured()
    }

    private suspend fun getEffectiveUserId(): String {
        return remoteRepository.getCurrentUser().getOrNull()?.id
            ?: ("user_" + supabasePreferences.getEffectiveSyncUrl().hashCode().toUInt().toString(16))
    }

    override suspend fun fetchRemoteManifest(): Result<UnifiedSyncManifest?> {
        return try {
            if (!isAuthenticated()) {
                return Result.failure(IllegalStateException("Personal Supabase not configured"))
            }

            val userId = getEffectiveUserId()

            // 1. Primary: Fetch full manifest from sync_manifest table (full fidelity)
            val rawManifest = remoteRepository.getSyncManifest(userId).getOrNull()
            if (!rawManifest.isNullOrBlank()) {
                val parsed = json.decodeFromString<UnifiedSyncManifest>(rawManifest)
                return Result.success(parsed)
            }

            // 2. Fallback: Query relational synced_books table
            val syncedBooks = remoteRepository.getSyncedBooks(userId).getOrDefault(emptyList())
            if (syncedBooks.isEmpty()) {
                return Result.success(null)
            }

            val bookItems = syncedBooks.map {
                SyncBookItem(
                    globalId = it.bookId,
                    sourceId = it.sourceId,
                    key = it.bookUrl,
                    title = it.title,
                    author = it.author,
                    description = it.description,
                    genres = if (it.genres.isNotBlank()) it.genres.split(",") else emptyList(),
                    status = it.status,
                    coverUrl = it.coverUrl,
                    favorite = it.favorite,
                    lastModified = it.lastRead
                )
            }

            val manifest = UnifiedSyncManifest(
                version = 1,
                deviceId = "supabase-remote",
                timestamp = ireader.core.util.currentTimeMillis(),
                books = bookItems,
                progress = emptyList(),
                tombstones = emptyList()
            )

            Result.success(manifest)
        } catch (e: Exception) {
            Log.warn { "$TAG: Failed to fetch remote manifest: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun uploadManifest(manifest: UnifiedSyncManifest): Result<Unit> {
        return try {
            if (!isAuthenticated()) {
                return Result.failure(IllegalStateException("Personal Supabase not configured"))
            }

            val userId = getEffectiveUserId()

            // 1. Upload full manifest to sync_manifest table
            val manifestJson = json.encodeToString(manifest)
            runCatching {
                remoteRepository.saveSyncManifest(userId, manifestJson).getOrThrow()
            }

            // 2. Also populate synced_books relational table with rich data
            manifest.books.forEach { bookItem ->
                val syncedBook = SyncedBook(
                    userId = userId,
                    bookId = bookItem.globalId,
                    sourceId = bookItem.sourceId,
                    title = bookItem.title,
                    bookUrl = bookItem.key,
                    lastRead = bookItem.lastModified,
                    coverUrl = bookItem.coverUrl,
                    sourceName = "",
                    author = bookItem.author,
                    description = bookItem.description,
                    genres = bookItem.genres.joinToString(","),
                    status = bookItem.status,
                    favorite = bookItem.favorite
                )
                runCatching { remoteRepository.syncBook(syncedBook) }
            }

            // 3. Sync reading progress items to reading_progress table
            manifest.progress.forEach { progressItem ->
                val readingProgress = ReadingProgress(
                    userId = userId,
                    bookId = progressItem.bookGlobalId,
                    lastChapterSlug = progressItem.chapterKey,
                    lastScrollPosition = progressItem.progress,
                    updatedAt = progressItem.lastModified
                )
                runCatching { remoteRepository.syncReadingProgress(readingProgress) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to upload manifest: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun pushProgress(progress: SyncProgressItem): Result<Unit> {
        return try {
            if (!isAuthenticated()) return Result.success(Unit)
            val userId = getEffectiveUserId()
            val readingProgress = ReadingProgress(
                userId = userId,
                bookId = progress.bookGlobalId,
                lastChapterSlug = progress.chapterKey,
                lastScrollPosition = progress.progress,
                updatedAt = progress.lastModified
            )
            remoteRepository.syncReadingProgress(readingProgress)
        } catch (e: Exception) {
            Log.warn { "$TAG: Failed to push realtime progress: ${e.message}" }
            Result.failure(e)
        }
    }
}
