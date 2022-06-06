package org.ireader.domain.use_cases.backup

import android.content.Context
import android.net.Uri
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import okio.FileSystem
import okio.buffer
import okio.gzip
import okio.source
import org.ireader.common_data.repository.BookCategoryRepository
import org.ireader.common_data.repository.BookRepository
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_data.repository.ChapterRepository
import org.ireader.common_data.repository.HistoryRepository
import org.ireader.common_data.repository.LibraryRepository
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.BookCategory
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.UiText
import org.ireader.core_api.db.Transactions
import org.ireader.core_ui.preferences.LibraryPreferences
import org.ireader.domain.use_cases.backup.backup.Backup
import org.ireader.domain.use_cases.backup.backup.BookProto
import org.ireader.domain.use_cases.backup.backup.CategoryProto
import javax.inject.Inject

class RestoreBackup @Inject internal constructor(
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
                    restoreHistories(manga, mangaId)
                }
            }
            onSuccess()
            Result.Success
        } catch (e: Exception) {
            org.ireader.core_api.log.Log.error(e, "Restore Backup was failed")
            onError(UiText.ExceptionString(e))
            Result.Error(e)
        }
    }

    private fun readUri(context: Context, uri: Uri): ByteArray {
        return context!!.contentResolver.openInputStream(uri)!!.source().gzip().buffer().use {
            it.readByteArray()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadDump(data: ByteArray): Backup {
        return ProtoBuf.decodeFromByteArray(data)
    }

    private suspend fun restoreManga(manga: BookProto): Long {
        val dbManga = bookRepository.find(manga.key, manga.sourceId)
        if (dbManga == null) {
            val newManga = manga.toDomain()
            return bookRepository.insertBook(newManga)
        }
        if (manga.lastInit > dbManga.lastInit || !dbManga.favorite) {
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
                lastInit = manga.lastInit,
                dateAdded = manga.dateAdded,
                viewer = manga.viewer,
                flags = manga.flags,
                key = manga.key,
                type = dbManga.type,
                sourceId = manga.sourceId,
                tableId = dbManga.tableId
            )
            bookRepository.insertBook(update)
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
                    backupChapter.toDomain(dbManga.id).copy(
                        read = backupChapter.read || dbChapter.read,
                        bookmark = backupChapter.bookmark || dbChapter.bookmark,
                    )
                } else {
                    backupChapter.toDomain(dbManga.id)
                }
                chaptersToAdd.add(newChapter)
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
                    order = dbCategories.size + index
                )
            }

        if (categoriesToAdd.isNotEmpty()) {
            categoryRepository.insertOrUpdate(categoriesToAdd)
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

    private suspend fun restoreHistories(bookProto: BookProto, bookId: Long) {
        if (bookProto.histories.isEmpty()) return

        val histories = bookProto.histories.map { it.toDomain(bookId) }

        if (histories.isNotEmpty()) {
            historyRepository.insertHistories(histories)
        }
    }

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

    private suspend fun getCategoryIdsByBackupId(categories: List<CategoryProto>): Map<Int, Long> {
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
