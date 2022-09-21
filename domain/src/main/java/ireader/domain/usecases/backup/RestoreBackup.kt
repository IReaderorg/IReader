package ireader.domain.usecases.backup

import android.content.Context
import android.net.Uri
import ireader.common.models.entities.Book
import ireader.common.models.entities.BookCategory
import ireader.common.models.entities.Chapter
import ireader.core.db.Transactions
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.usecases.backup.backup.Backup
import ireader.domain.usecases.backup.backup.BookProto
import ireader.domain.usecases.backup.backup.CategoryProto
import ireader.domain.usecases.backup.backup.createStableBackup
import ireader.domain.usecases.backup.backup.legecy.createLegacyBackup
import ireader.i18n.UiText
import kotlinx.serialization.ExperimentalSerializationApi
import okio.FileSystem
import okio.buffer
import okio.gzip
import okio.source
import org.koin.core.annotation.Factory

@Factory
class RestoreBackup internal constructor(
    private val fileSystem: FileSystem,
    private val libraryRepository: LibraryRepository,
    private val bookRepository: BookRepository,
    private val categoryRepository: CategoryRepository,
    private val chapterRepository: ChapterRepository,
    private val mangaCategoryRepository: BookCategoryRepository,
    private val historyRepository: HistoryRepository,
    private val libraryPreferences: LibraryPreferences,
    private val transactions: Transactions
) {

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun restoreFrom(
        uri: Uri,
        context: Context,
        onError: (UiText) -> Unit,
        onSuccess: () -> Unit
    ): Result {
        return try {

            val bytes = readUri(context, uri)

            // val bytes = fileSystem.withAsyncGzipSource(path) { it.readByteArray() }
            val backup = loadDump(bytes)

            transactions.run {
                restoreCategories(backup.categories)
                val backupCategoriesWithId = getCategoryIdsByBackupId(backup.categories)
                for (manga in backup.library) {
                    val mangaId = restoreManga(manga)
                    val categoryIdsOfManga =
                        manga.categories.mapNotNull(backupCategoriesWithId::get)
                    restoreChapters(manga)
                    restoreCategoriesOfBook(mangaId, categoryIdsOfManga)
                    restoreTracks(manga, mangaId)
                    // restoreHistories(manga, mangaId)
                }
            }
            onSuccess()
            Result.Success
        } catch (e: Exception) {
            ireader.core.log.Log.error(e, "Restore Backup was failed")
            onError(UiText.ExceptionString(e))
            Result.Error(e)
        }
    }

    private fun readUri(context: Context, uri: Uri): ByteArray {
        return context!!.contentResolver.openInputStream(uri)!!.source().gzip().buffer().use {
            it.readByteArray()
        }
    }


    private fun loadDump(data: ByteArray): Backup {
        return kotlin.runCatching {
            data.createStableBackup()
        }.getOrElse {
            data.createLegacyBackup()
        }

    }

    private suspend fun restoreManga(manga: BookProto): Long {
        val dbManga = bookRepository.find(manga.key, manga.sourceId)
        if (dbManga == null) {
            val newManga = manga.toDomain()
            return bookRepository.upsert(newManga)
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
            bookRepository.updateBook(update)
        }
        return dbManga.id
    }

    private suspend fun restoreChapters(manga: BookProto) {
        if (manga.chapters.isEmpty()) return

        val dbManga = checkNotNull(bookRepository.find(manga.key, manga.sourceId))
        val dbChapters = chapterRepository.findChaptersByBookId(dbManga.id)

        if (dbChapters.isEmpty()) {
            chapterRepository.insertChapters(manga.chapters.map { it.toDomain(dbManga.id) })
            return
        }

        // Keep the backup chapters
        if (manga.lastUpdate > dbManga.lastUpdate) {
            chapterRepository.deleteChapters(dbChapters)
            val dbChaptersMap = dbChapters.associateBy { it.key }

            val chaptersToAdd = mutableListOf<Chapter>()
            for (backupChapter in manga.chapters) {
                val dbChapter = dbChaptersMap[backupChapter.key]
                val newChapter = if (dbChapter != null) {
                    kotlin.runCatching {
                    backupChapter.toDomain(dbManga.id).copy(
                        read = backupChapter.read || dbChapter.read,
                        bookmark = backupChapter.bookmark || dbChapter.bookmark,
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
            chapterRepository.insertChapters(chaptersToAdd)
        }
        // Keep the database chapters
        else {
            val backupChaptersMap = manga.chapters.associateBy { it.key }

            val chaptersToUpdate = mutableListOf<Chapter>()
            for (dbChapter in dbChapters) {
                val backupChapter = backupChaptersMap[dbChapter.key]
                if (backupChapter != null) {
                    val update = dbChapter.copy(
                        id = dbChapter.id,
                        read = dbChapter.read || backupChapter.read,
                        bookmark = dbChapter.bookmark || backupChapter.bookmark,
                    )
                    chaptersToUpdate.add(update)
                }
            }
            if (chaptersToUpdate.isNotEmpty()) {
                chapterRepository.insertChapters(chaptersToUpdate)
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
        return categories.associate { backupCategory ->
            val dbId = dbCategories.first { it.name.equals(backupCategory.name, true) }.id
            backupCategory.order to dbId
        }
    }

    sealed class Result {
        object Success : Result()
        data class Error(val error: Exception) : Result()
    }
}
