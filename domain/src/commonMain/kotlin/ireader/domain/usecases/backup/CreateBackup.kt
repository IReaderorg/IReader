package ireader.domain.usecases.backup


import ireader.core.db.Transactions
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.common.Uri
import ireader.domain.usecases.backup.backup.*
import ireader.domain.usecases.file.FileSaver
import ireader.domain.utils.fastMap
import ireader.i18n.UiText
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import okio.FileSystem


class CreateBackup  internal constructor(
    private val fileSystem: FileSystem,
    private val mangaRepository: LibraryRepository,
    private val categoryRepository: CategoryRepository,
    private val chapterRepository: ChapterRepository,
    private val historyRepository: HistoryRepository,
    private val transactions: Transactions,
    private val fileSaver: FileSaver
) {

    suspend fun saveTo(
        uri: Uri,
        onError: (UiText) -> Unit,
        onSuccess: () -> Unit,
        currentEvent: (String) -> Unit
    ): Boolean {
        return try {
            val dump = createDump(currentEvent)
            fileSaver.save(uri,dump)
            val isValid = fileSaver.validate(uri)
            onSuccess()
            isValid
        } catch (e: Exception) {
            onError(UiText.ExceptionString(e))
            false
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun createDump(
        currentEvent: (String) -> Unit
    ): ByteArray {
        val backup = transactions.run {
            Backup(
                library = dumpLibrary(currentEvent),
                categories = dumpCategories()
            )
        }

        return kotlinx.serialization.protobuf.ProtoBuf.encodeToByteArray(backup)
    }

    private suspend fun dumpLibrary(
        currentEvent: (String) -> Unit
    ): List<BookProto> {
        return mangaRepository.findFavorites()
            .fastMap { book ->
                val chapters = dumpChapters(book.id,currentEvent)
                val mangaCategories = dumpMangaCategories(book.id)
                val tracks = dumpTracks(book.id)
                val histories = dumpHistories(book.id)

                BookProto.fromDomain(book, chapters, mangaCategories, tracks, histories = histories)
            }
    }

    private suspend fun dumpChapters(bookId: Long, currentEvent: (String) -> Unit): List<ChapterProto> {
        return chapterRepository.findChaptersByBookId(bookId).fastMap { chapter ->
            currentEvent(chapter.name)
            ChapterProto.fromDomain(chapter)
        }
    }

    private suspend fun dumpHistories(bookId: Long): List<HistoryProto> {
        return historyRepository.findHistoriesByBookId(bookId).fastMap { HistoryProto.fromDomain(it) }
    }

    private suspend fun dumpMangaCategories(mangaId: Long): List<Long> {
        return categoryRepository.getCategoriesByMangaId(mangaId)
            .filter { !it.isSystemCategory }
            .fastMap { it.order }
    }

    private suspend fun dumpTracks(mangaId: Long): List<TrackProto> {
        // trackRepository.findAllForManga(mangaId).map { TrackProto.fromDomain(it) }
        return emptyList()
    }

    private suspend fun dumpCategories(): List<CategoryProto> {
        return categoryRepository.findAll()
            .filter { !it.category.isSystemCategory }
            .fastMap { cat -> CategoryProto.fromDomain(cat.category) }
    }
    
    /**
     * Create backup data from a list of books
     * Returns compressed backup data as ByteArray
     */
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun createBackupData(books: List<ireader.domain.models.entities.Book>): ByteArray {
        val backup = transactions.run {
            val bookProtos = books.fastMap { book ->
                val chapters = dumpChapters(book.id) { }
                val mangaCategories = dumpMangaCategories(book.id)
                val tracks = dumpTracks(book.id)
                val histories = dumpHistories(book.id)

                BookProto.fromDomain(book, chapters, mangaCategories, tracks, histories = histories)
            }
            
            Backup(
                library = bookProtos,
                categories = dumpCategories()
            )
        }

        return kotlinx.serialization.protobuf.ProtoBuf.encodeToByteArray(backup)
    }
}
