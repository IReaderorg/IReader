package org.ireader.domain.use_cases.backup

import android.content.Context
import android.net.Uri
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import okio.FileSystem
import okio.buffer
import okio.gzip
import okio.sink
import okio.source
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_data.repository.ChapterRepository
import org.ireader.common_data.repository.HistoryRepository
import org.ireader.common_data.repository.LibraryRepository
import org.ireader.common_resources.UiText
import org.ireader.core_api.db.Transactions
import org.ireader.domain.use_cases.backup.backup.Backup
import org.ireader.domain.use_cases.backup.backup.BookProto
import org.ireader.domain.use_cases.backup.backup.CategoryProto
import org.ireader.domain.use_cases.backup.backup.ChapterProto
import org.ireader.domain.use_cases.backup.backup.HistoryProto
import org.ireader.domain.use_cases.backup.backup.TrackProto
import javax.inject.Inject

class CreateBackup @Inject internal constructor(
    private val fileSystem: FileSystem,
    private val mangaRepository: LibraryRepository,
    private val categoryRepository: CategoryRepository,
    private val chapterRepository: ChapterRepository,
    private val historyRepository: HistoryRepository,
    private val transactions: Transactions,
) {

    suspend fun saveTo(
        uri: Uri,
        context: Context,
        onError: (UiText) -> Unit,
        onSuccess: () -> Unit
    ): Result {
        return try {
            val dump = createDump()
            saveUri(context, uri, dump)
            val isValid = validate(uri, context)
            onSuccess()
            isValid
        } catch (e: Exception) {
            onError(UiText.ExceptionString(e))
            Result.Error(e)
        }
    }

    fun validate(uri: Uri, context: Context): CreateBackup.Result {
        context.contentResolver.openInputStream(uri)!!.source().gzip().buffer()
            .use { it.readByteArray() }
        return CreateBackup.Result.Success
    }

    private fun saveUri(context: Context, uri: Uri, byteArray: ByteArray) {
        return context.contentResolver.openOutputStream(uri, "w")!!.sink().gzip().buffer()
            .use { output ->
                output.write(byteArray)
            }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun createDump(): ByteArray {
        val backup = transactions.run {
            Backup(
                library = dumpLibrary(),
                categories = dumpCategories()
            )
        }

        return kotlinx.serialization.protobuf.ProtoBuf.encodeToByteArray(backup)
    }

    private suspend fun dumpLibrary(): List<BookProto> {
        return mangaRepository.findFavorites()
            .map { book ->
                val chapters = dumpChapters(book.id)
                val mangaCategories = dumpMangaCategories(book.id)
                val tracks = dumpTracks(book.id)
                val histories = dumpHistories(book.id)

                BookProto.fromDomain(book, chapters, mangaCategories, tracks, histories = histories)
            }
    }

    private suspend fun dumpChapters(bookId: Long): List<ChapterProto> {
        return chapterRepository.findChaptersByBookId(bookId).map { chapter ->
            ChapterProto.fromDomain(chapter)
        }
    }

    private suspend fun dumpHistories(bookId: Long): List<HistoryProto> {
        return historyRepository.findHistoriesByBookId(bookId).map { HistoryProto.fromDomain(it) }
    }

    private suspend fun dumpMangaCategories(mangaId: Long): List<Int> {
        return categoryRepository.findCategoriesOfBook(mangaId)
            .filter { !it.isSystemCategory }
            .map { it.order }
    }

    private suspend fun dumpTracks(mangaId: Long): List<TrackProto> {
        // trackRepository.findAllForManga(mangaId).map { TrackProto.fromDomain(it) }
        return emptyList()
    }

    private suspend fun dumpCategories(): List<CategoryProto> {
        return categoryRepository.findAll()
            .filter { !it.category.isSystemCategory }
            .map { cat -> CategoryProto.fromDomain(cat.category) }
    }

    sealed class Result {
        object Success : Result()
        data class Error(val error: Exception) : Result()
    }
}
