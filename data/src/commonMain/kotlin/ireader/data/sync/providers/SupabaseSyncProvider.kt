package ireader.data.sync.providers

import ireader.core.log.Log
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.SyncedBook
import ireader.domain.models.sync.SyncBookItem
import ireader.domain.models.sync.SyncProgressItem
import ireader.domain.models.sync.SyncProviderType
import ireader.domain.models.sync.UnifiedSyncManifest
import ireader.domain.services.sync.SyncProvider

/**
 * Supabase implementation of SyncProvider.
 * Synchronizes books and reading progress with Supabase PostgreSQL tables.
 */
class SupabaseSyncProvider(
    private val remoteRepository: RemoteRepository
) : SyncProvider {

    companion object {
        private const val TAG = "SupabaseSyncProvider"
    }

    override val type: SyncProviderType = SyncProviderType.SUPABASE
    override val name: String = "Supabase Cloud"

    override suspend fun isAuthenticated(): Boolean {
        return try {
            remoteRepository.getCurrentUser().getOrNull() != null
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun fetchRemoteManifest(): Result<UnifiedSyncManifest?> {
        return try {
            val user = remoteRepository.getCurrentUser().getOrNull()
                ?: return Result.failure(IllegalStateException("Supabase not authenticated"))

            val syncedBooks = remoteRepository.getSyncedBooks(user.id).getOrDefault(emptyList())

            val bookItems = syncedBooks.map {
                SyncBookItem(
                    globalId = it.bookId,
                    sourceId = it.sourceId,
                    key = it.bookUrl,
                    title = it.title,
                    coverUrl = it.coverUrl,
                    favorite = true,
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
            val user = remoteRepository.getCurrentUser().getOrNull()
                ?: return Result.failure(IllegalStateException("Supabase not authenticated"))

            // Sync all books to Supabase
            manifest.books.forEach { bookItem ->
                val syncedBook = SyncedBook(
                    userId = user.id,
                    bookId = bookItem.globalId,
                    sourceId = bookItem.sourceId,
                    title = bookItem.title,
                    bookUrl = bookItem.key,
                    lastRead = bookItem.lastModified,
                    coverUrl = bookItem.coverUrl,
                    sourceName = ""
                )
                remoteRepository.syncBook(syncedBook).getOrThrow()
            }

            // Sync reading progress items
            manifest.progress.forEach { progressItem ->
                val readingProgress = ReadingProgress(
                    userId = user.id,
                    bookId = progressItem.bookGlobalId,
                    lastChapterSlug = progressItem.chapterKey,
                    lastScrollPosition = progressItem.progress,
                    updatedAt = progressItem.lastModified
                )
                remoteRepository.syncReadingProgress(readingProgress)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to upload manifest: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun pushProgress(progress: SyncProgressItem): Result<Unit> {
        return try {
            val user = remoteRepository.getCurrentUser().getOrNull() ?: return Result.success(Unit)
            val readingProgress = ReadingProgress(
                userId = user.id,
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
