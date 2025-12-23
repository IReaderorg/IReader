package ireader.domain.usecases.backup

import ireader.core.db.Transactions
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.usecases.backup.backup.Backup
import ireader.domain.usecases.backup.backup.BookProto
import ireader.domain.usecases.backup.backup.CategoryProto
import ireader.domain.usecases.backup.backup.dunmpStableBackup
import ireader.domain.usecases.backup.backup.legecy.nineteensep.dumpNineteenSepLegacyBackup
import ireader.domain.usecases.backup.backup.legecy.twnetysep.dumpTwentySepLegacyBackup
import ireader.domain.usecases.file.FileSaver
import ireader.i18n.UiText
import kotlinx.serialization.ExperimentalSerializationApi


class RestoreBackup internal constructor(
    private val bookRepository: BookRepository,
    private val categoryRepository: CategoryRepository,
    private val chapterRepository: ChapterRepository,
    private val mangaCategoryRepository: BookCategoryRepository,
    private val libraryPreferences: LibraryPreferences,
    private val transactions: Transactions,
    private val fileSaver: FileSaver
) {

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun restoreFrom(
        uri: Uri,
        onError: (UiText) -> Unit,
        onSuccess: () -> Unit
    ): Result {
        return try {
            val bytes = fileSaver.read(uri)
            val backup = loadDump(bytes)
            var booksRestored = 0
            var chaptersRestored = 0

            transactions.run {
                restoreCategories(backup.categories)
                val backupCategoriesWithId = getCategoryIdsByBackupId(backup.categories)
                for (manga in backup.library) {
                    val mangaId = restoreManga(manga)
                    val categoryIdsOfManga =
                        manga.categories.mapNotNull(backupCategoriesWithId::get)
                    try {
                        restoreChapters(manga)
                        chaptersRestored += manga.chapters.size
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore chapters for book: ${manga.title}")
                    }
                    try {
                        restoreCategoriesOfBook(mangaId, categoryIdsOfManga)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore categories for book: ${manga.title}")
                    }
                    try {
                        restoreTracks(manga, mangaId)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore tracks for book: ${manga.title}")
                    }
                    booksRestored++
                }
            }
            onSuccess()
            Result.Success(booksRestored, chaptersRestored)
        } catch (e: Exception) {
            ireader.core.log.Log.error(e, "Restore Backup was failed")
            onError(UiText.ExceptionString(e))
            Result.Error(e)
        }
    }


    private fun loadDump(data: ByteArray): Backup {
        return kotlin.runCatching {
            data.dunmpStableBackup()
        }.getOrElse {
            kotlin.runCatching {
                data.dumpNineteenSepLegacyBackup()

            }.getOrElse {
                data.dumpTwentySepLegacyBackup()
            }
        }

    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun restoreManga(manga: BookProto): Long {
        val dbManga = try {
            bookRepository.find(manga.key, manga.sourceId)
        } catch (e: Exception) {
            // Handle case where there might be duplicate books in database
            ireader.core.log.Log.warn(e, "Error finding book ${manga.title}, attempting to continue")
            null
        }
        
        if (dbManga == null) {
            val newManga = manga.toDomain()
            return try {
                bookRepository.upsert(newManga)
            } catch (e: Exception) {
                // If upsert fails (e.g., due to unique constraint), try to find existing book
                ireader.core.log.Log.warn(e, "Failed to upsert book ${manga.title}, trying to find existing")
                bookRepository.find(manga.key, manga.sourceId)?.id ?: throw e
            }
        }
        
        if (manga.initialized != dbManga.initialized || !dbManga.favorite) {
            val update = Book(
                dbManga.id,
                title = manga.title,
                author = manga.author,
                description = manga.description,
                genres = manga.genres,
                status = manga.status,
                cover = manga.cover,
                customCover = manga.customCover,
                favorite = true,
                lastUpdate = manga.lastUpdate,
                initialized = manga.initialized,
                dateAdded = manga.dateAdded,
                viewer = manga.viewer,
                flags = manga.flags,
                key = manga.key,
                sourceId = manga.sourceId,
            )
            try {
                bookRepository.updateBook(update)
            } catch (e: Exception) {
                ireader.core.log.Log.warn(e, "Failed to update book ${manga.title}")
            }
        }
        return dbManga.id
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun restoreChapters(manga: BookProto) {
        if (manga.chapters.isEmpty()) return

        val dbManga = try {
            bookRepository.find(manga.key, manga.sourceId)
        } catch (e: Exception) {
            ireader.core.log.Log.warn(e, "Error finding book for chapter restore: ${manga.title}")
            null
        }
        
        if (dbManga == null) {
            ireader.core.log.Log.warn("Book not found for chapter restore: ${manga.title}")
            return
        }
        
        val dbChapters = chapterRepository.findChaptersByBookId(dbManga.id)

        if (dbChapters.isEmpty()) {
            chapterRepository.insertChapters(manga.chapters.map { it.toDomain(dbManga.id) })
            return
        }

        // Create maps for merging read/bookmark status
        val dbChaptersMap = dbChapters.associateBy { it.key }
        val backupChaptersMap = manga.chapters.associateBy { it.key }

        // Keep the backup chapters (backup is newer)
        if (manga.lastUpdate > dbManga.lastUpdate) {
            // Build merged chapters list BEFORE deleting existing chapters
            val chaptersToAdd = mutableListOf<Chapter>()
            for (backupChapter in manga.chapters) {
                val dbChapter = dbChaptersMap[backupChapter.key]
                val newChapter = if (dbChapter != null) {
                    // Merge read/bookmark status: preserve if either backup or db has it marked
                    kotlin.runCatching {
                        backupChapter.toDomain(dbManga.id).copy(
                            read = backupChapter.read || dbChapter.read,
                            bookmark = backupChapter.bookmark || dbChapter.bookmark,
                            lastPageRead = maxOf(backupChapter.lastPageRead, dbChapter.lastPageRead)
                        )
                    }.getOrNull()
                } else {
                    kotlin.runCatching {
                        backupChapter.toDomain(dbManga.id)
                    }.getOrNull()
                }
                if (newChapter != null) {
                    chaptersToAdd.add(newChapter)
                }
            }
            
            // Now delete existing chapters and insert merged ones
            chapterRepository.deleteChapters(dbChapters)
            chapterRepository.insertChapters(chaptersToAdd)
        }
        // Keep the database chapters (database is newer or same)
        else {
            val chaptersToUpdate = mutableListOf<Chapter>()
            val chaptersToAdd = mutableListOf<Chapter>()
            
            // Update existing chapters with backup read/bookmark status
            for (dbChapter in dbChapters) {
                val backupChapter = backupChaptersMap[dbChapter.key]
                if (backupChapter != null) {
                    // Only update if there's something to merge
                    if (backupChapter.read || backupChapter.bookmark || backupChapter.lastPageRead > 0) {
                        val update = dbChapter.copy(
                            id = dbChapter.id,
                            read = dbChapter.read || backupChapter.read,
                            bookmark = dbChapter.bookmark || backupChapter.bookmark,
                            lastPageRead = maxOf(dbChapter.lastPageRead, backupChapter.lastPageRead)
                        )
                        chaptersToUpdate.add(update)
                    }
                }
            }
            
            // Add chapters from backup that don't exist in database
            for (backupChapter in manga.chapters) {
                if (!dbChaptersMap.containsKey(backupChapter.key)) {
                    kotlin.runCatching {
                        chaptersToAdd.add(backupChapter.toDomain(dbManga.id))
                    }
                }
            }
            
            if (chaptersToUpdate.isNotEmpty()) {
                chapterRepository.insertChapters(chaptersToUpdate)
            }
            if (chaptersToAdd.isNotEmpty()) {
                chapterRepository.insertChapters(chaptersToAdd)
            }
        }
    }

    private suspend fun restoreCategories(categories: List<CategoryProto>) {
        if (categories.isEmpty()) return

        val dbCategories = categoryRepository.findAll()
        val dbCategoryNames = dbCategories.map { it.name }
        val categoriesToAdd = categories
            .filter { category -> dbCategoryNames.none { category.name.equals(it, true) } }
            .mapIndexed { index, category ->
                category.toDomain().copy(
                    order = (dbCategories.size + index).toLong()
                )
            }

        if (categoriesToAdd.isNotEmpty()) {
            categoryRepository.insert(categoriesToAdd)
        }

        // Turn on per category settings if not all flags match
        libraryPreferences.perCategorySettings().set(
            (dbCategories.map { it.category } + categoriesToAdd)
                .distinctBy { it.flags }
                .size > 1
        )
    }

    private suspend fun restoreCategoriesOfBook(bookId: Long, categoryIds: List<Long>) {
        if (categoryIds.isEmpty()) return

        val bookCategories = categoryIds.map { categoryId ->
            BookCategory(bookId, categoryId)
        }

        if (bookCategories.isNotEmpty()) {
            mangaCategoryRepository.insertAll(bookCategories)
        }
    }

//    private suspend fun restoreHistories(bookProto: BookProto, bookId: Long) {
//        if (bookProto.histories.isEmpty()) return
//
//        val histories = bookProto.histories.map { it.toDomain(bookId).copy(chapterId = 0) }
//
//        if (histories.isNotEmpty()) {
//          //  historyRepository.insertHistories(histories)
//        }
//    }

    private suspend fun restoreTracks(manga: BookProto, mangaId: Long) {
        return
        // if (manga.tracks.isEmpty()) return

//    val dbTracks = trackRepository.findAllForManga(mangaId)
//    val tracksToAdd = mutableListOf<Track>()
//    val tracksToUpdate = mutableListOf<TrackUpdate>()
//
//    for (track in manga.tracks) {
//      val dbTrack = dbTracks.find { it.siteId == track.siteId }
//
//      if (dbTrack == null) {
//        tracksToAdd.add(track.toDomain(mangaId))
//      } else {
//        if (track.lastRead > dbTrack.lastRead || track.totalChapters > dbTrack.totalChapters) {
//          val update = TrackUpdate(
//            dbTrack.id,
//            lastRead = maxOf(dbTrack.lastRead, track.lastRead),
//            totalChapters = maxOf(dbTrack.totalChapters, track.totalChapters)
//          )
//          tracksToUpdate.add(update)
//        }
//      }
//    }
//
//    if (tracksToAdd.isNotEmpty()) {
//      trackRepository.insert(tracksToAdd)
//    }
//    if (tracksToUpdate.isNotEmpty()) {
//      trackRepository.updatePartial(tracksToUpdate)
//    }
    }

    private suspend fun getCategoryIdsByBackupId(categories: List<CategoryProto>): Map<Long, Long> {
        val dbCategories = categoryRepository.findAll()
        return categories.mapNotNull { backupCategory ->
            val dbCategory = dbCategories.find { it.name.equals(backupCategory.name, true) }
            if (dbCategory != null) {
                backupCategory.order to dbCategory.id
            } else {
                // Category not found in database, skip mapping
                // This can happen if category restoration failed or was skipped
                null
            }
        }.toMap()
    }

    /**
     * Restore backup from byte array
     * This is a convenience method for restoring from in-memory data
     */
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun restoreFromBytes(bytes: ByteArray): Result {
        return try {
            val backup = loadDump(bytes)

            transactions.run {
                restoreCategories(backup.categories)
                val backupCategoriesWithId = getCategoryIdsByBackupId(backup.categories)
                for (manga in backup.library) {
                    val mangaId = restoreManga(manga)
                    val categoryIdsOfManga =
                        manga.categories.mapNotNull(backupCategoriesWithId::get)
                    try {
                        restoreChapters(manga)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore chapters for book: ${manga.title}")
                    }
                    try {
                        restoreCategoriesOfBook(mangaId, categoryIdsOfManga)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore categories for book: ${manga.title}")
                    }
                    try {
                        restoreTracks(manga, mangaId)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore tracks for book: ${manga.title}")
                    }
                }
            }
            Result.Success(backup.library.size, backup.library.sumOf { it.chapters.size })
        } catch (e: Exception) {
            ireader.core.log.Log.error(e, "Restore Backup from bytes failed")
            Result.Error(e)
        }
    }
    
    /**
     * Restore backup from byte array with progress callback
     * @param bytes The backup data
     * @param onProgress Callback for progress updates (currentBook, totalBooks, bookName)
     */
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun restoreFromBytesWithProgress(
        bytes: ByteArray,
        onProgress: (current: Int, total: Int, bookName: String) -> Unit
    ): Result {
        return try {
            val backup = loadDump(bytes)
            val totalBooks = backup.library.size
            var booksRestored = 0
            var chaptersRestored = 0

            transactions.run {
                restoreCategories(backup.categories)
                val backupCategoriesWithId = getCategoryIdsByBackupId(backup.categories)
                
                backup.library.forEachIndexed { index, manga ->
                    // Report progress
                    onProgress(index + 1, totalBooks, manga.title)
                    
                    val mangaId = restoreManga(manga)
                    val categoryIdsOfManga =
                        manga.categories.mapNotNull(backupCategoriesWithId::get)
                    try {
                        restoreChapters(manga)
                        chaptersRestored += manga.chapters.size
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore chapters for book: ${manga.title}")
                    }
                    try {
                        restoreCategoriesOfBook(mangaId, categoryIdsOfManga)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore categories for book: ${manga.title}")
                    }
                    try {
                        restoreTracks(manga, mangaId)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to restore tracks for book: ${manga.title}")
                    }
                    booksRestored++
                }
            }
            Result.Success(booksRestored, chaptersRestored)
        } catch (e: Exception) {
            ireader.core.log.Log.error(e, "Restore Backup from bytes failed")
            Result.Error(e)
        }
    }

    sealed class Result {
        data class Success(val booksRestored: Int = 0, val chaptersRestored: Int = 0) : Result()
        data class Error(val error: Exception) : Result()
    }
}
