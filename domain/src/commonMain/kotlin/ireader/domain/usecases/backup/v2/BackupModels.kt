package ireader.domain.usecases.backup.v2

import ireader.core.source.model.decode
import ireader.core.source.model.encode
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.models.entities.Track
import ireader.domain.models.entities.TrackStatus
import ireader.domain.models.theme.ReaderTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

// ── Single source of truth for backup format ──────────────────────────────

@Serializable
data class BackupPayload(
    @ProtoNumber(1) val version: Int = CURRENT_VERSION,
    @ProtoNumber(2) val checksum: String = "",
    @ProtoNumber(3) val books: List<BookSnapshot> = emptyList(),
    @ProtoNumber(4) val categories: List<CategorySnapshot> = emptyList(),
    @ProtoNumber(5) val metadata: BackupMetadata = BackupMetadata(),
    @ProtoNumber(6) val histories: List<HistorySnapshot> = emptyList(),
    @ProtoNumber(7) val tracks: List<TrackSnapshot> = emptyList(),
    @ProtoNumber(8) val themes: List<ReaderThemeSnapshot> = emptyList(),
    @ProtoNumber(9) val readingStats: List<ReadingStatsSnapshot> = emptyList(),
    @ProtoNumber(10) val settings: List<SettingSnapshot> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 4
    }
}

@Serializable
data class BackupMetadata(
    @ProtoNumber(1) val appVersion: String = "",
    @ProtoNumber(2) val deviceName: String = "",
    @ProtoNumber(3) val createdAt: Long = 0L,
    @ProtoNumber(4) val bookCount: Int = 0,
    @ProtoNumber(5) val chapterCount: Int = 0,
    @ProtoNumber(6) val historyCount: Int = 0,
    @ProtoNumber(7) val categoryCount: Int = 0,
    @ProtoNumber(8) val trackCount: Int = 0,
    @ProtoNumber(9) val themeCount: Int = 0,
)

@Serializable
data class BookSnapshot(
    @ProtoNumber(1) val sourceId: Long,
    @ProtoNumber(2) val key: String,
    @ProtoNumber(3) val title: String,
    @ProtoNumber(4) val author: String = "",
    @ProtoNumber(5) val description: String = "",
    @ProtoNumber(6) val genres: List<String> = emptyList(),
    @ProtoNumber(7) val status: Long = 0,
    @ProtoNumber(8) val cover: String = "",
    @ProtoNumber(9) val customCover: String = "",
    @ProtoNumber(10) val lastUpdate: Long = 0,
    @ProtoNumber(11) val initialized: Boolean = false,
    @ProtoNumber(12) val dateAdded: Long = 0,
    @ProtoNumber(13) val viewer: Long = 0,
    @ProtoNumber(14) val flags: Long = 0,
    @ProtoNumber(15) val chapters: List<ChapterSnapshot> = emptyList(),
    @ProtoNumber(16) val categoryOrders: List<Long> = emptyList(),
) {
    fun toBook(bookId: Long = 0): Book = Book(
        id = bookId,
        sourceId = sourceId,
        key = key,
        title = title,
        author = author,
        description = description,
        genres = genres,
        status = status,
        cover = cover,
        customCover = customCover,
        favorite = true,
        lastUpdate = lastUpdate,
        initialized = initialized,
        dateAdded = dateAdded,
        viewer = viewer,
        flags = flags,
    )

    companion object {
        fun fromBook(
            book: Book,
            chapters: List<ChapterSnapshot> = emptyList(),
            categoryOrders: List<Long> = emptyList(),
        ): BookSnapshot = BookSnapshot(
            sourceId = book.sourceId,
            key = book.key,
            title = book.title,
            author = book.author,
            description = book.description,
            genres = book.genres,
            status = book.status,
            cover = book.cover,
            customCover = book.customCover,
            lastUpdate = book.lastUpdate,
            initialized = book.initialized,
            dateAdded = book.dateAdded,
            viewer = book.viewer,
            flags = book.flags,
            chapters = chapters,
            categoryOrders = categoryOrders,
        )
    }
}

@Serializable
data class ChapterSnapshot(
    @ProtoNumber(1) val key: String,
    @ProtoNumber(2) val name: String,
    @ProtoNumber(3) val translator: String = "",
    @ProtoNumber(4) val read: Boolean = false,
    @ProtoNumber(5) val bookmark: Boolean = false,
    @ProtoNumber(6) val dateFetch: Long = 0,
    @ProtoNumber(7) val dateUpload: Long = 0,
    @ProtoNumber(8) val number: Float = 0f,
    @ProtoNumber(9) val sourceOrder: Long = 0,
    @ProtoNumber(10) val content: String = "",
    @ProtoNumber(11) val type: Long = 0,
    @ProtoNumber(12) val lastPageRead: Long = 0,
) {
    fun toChapter(bookId: Long): Chapter = Chapter(
        bookId = bookId,
        key = key,
        name = name,
        translator = translator,
        read = read,
        bookmark = bookmark,
        dateFetch = dateFetch,
        dateUpload = dateUpload,
        number = number,
        sourceOrder = sourceOrder,
        content = content.decode(),
        type = type,
        lastPageRead = lastPageRead,
    )

    companion object {
        fun fromChapter(chapter: Chapter): ChapterSnapshot = ChapterSnapshot(
            key = chapter.key,
            name = chapter.name,
            translator = chapter.translator,
            read = chapter.read,
            bookmark = chapter.bookmark,
            dateFetch = chapter.dateFetch,
            dateUpload = chapter.dateUpload,
            number = chapter.number,
            sourceOrder = chapter.sourceOrder,
            content = chapter.content.encode(),
            type = chapter.type,
            lastPageRead = chapter.lastPageRead,
        )
    }
}

@Serializable
data class CategorySnapshot(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val order: Long,
    @ProtoNumber(3) val flags: Long = 0,
) {
    fun toCategory(): Category = Category(
        name = name,
        order = order,
        flags = flags,
    )

    companion object {
        fun fromCategory(category: Category): CategorySnapshot = CategorySnapshot(
            name = category.name,
            order = category.order,
            flags = category.flags,
        )
    }
}

@Serializable
data class HistorySnapshot(
    @ProtoNumber(1) val bookKey: String,
    @ProtoNumber(2) val bookSourceId: Long,
    @ProtoNumber(3) val chapterKey: String,
    @ProtoNumber(4) val lastRead: Long = 0,
    @ProtoNumber(5) val timeRead: Long = 0,
    @ProtoNumber(6) val progress: Float = 0f,
)

@Serializable
data class TrackSnapshot(
    @ProtoNumber(1) val bookKey: String,
    @ProtoNumber(2) val bookSourceId: Long,
    @ProtoNumber(3) val siteId: Int,
    @ProtoNumber(4) val entryId: Long,
    @ProtoNumber(5) val mediaId: Long = 0,
    @ProtoNumber(6) val mediaUrl: String = "",
    @ProtoNumber(7) val title: String = "",
    @ProtoNumber(8) val lastRead: Float = 0f,
    @ProtoNumber(9) val totalChapters: Int = 0,
    @ProtoNumber(10) val score: Float = 0f,
    @ProtoNumber(11) val status: Int = 1,
    @ProtoNumber(12) val startReadTime: Long = 0,
    @ProtoNumber(13) val endReadTime: Long = 0,
) {
    fun toTrack(mangaId: Long, trackId: Long = 0): Track = Track(
        id = trackId,
        mangaId = mangaId,
        siteId = siteId,
        entryId = entryId,
        mediaId = mediaId,
        mediaUrl = mediaUrl,
        title = title,
        lastRead = lastRead,
        totalChapters = totalChapters,
        score = score,
        status = try { TrackStatus.from(status) } catch (_: Exception) { TrackStatus.Reading },
        startReadTime = startReadTime,
        endReadTime = endReadTime
    )

    companion object {
        fun fromTrack(track: Track, bookKey: String, bookSourceId: Long): TrackSnapshot = TrackSnapshot(
            bookKey = bookKey,
            bookSourceId = bookSourceId,
            siteId = track.siteId,
            entryId = track.entryId,
            mediaId = track.mediaId,
            mediaUrl = track.mediaUrl,
            title = track.title,
            lastRead = track.lastRead,
            totalChapters = track.totalChapters,
            score = track.score,
            status = track.status.value,
            startReadTime = track.startReadTime,
            endReadTime = track.endReadTime
        )
    }
}

@Serializable
data class ReaderThemeSnapshot(
    @ProtoNumber(1) val id: Long = 0,
    @ProtoNumber(2) val backgroundColor: Int,
    @ProtoNumber(3) val onTextColor: Int,
) {
    fun toReaderTheme(): ReaderTheme = ReaderTheme(
        id = id,
        backgroundColor = backgroundColor,
        onTextColor = onTextColor
    )

    companion object {
        fun fromReaderTheme(theme: ReaderTheme): ReaderThemeSnapshot = ReaderThemeSnapshot(
            id = theme.id,
            backgroundColor = theme.backgroundColor,
            onTextColor = theme.onTextColor
        )
    }
}

@Serializable
data class ReadingStatsSnapshot(
    @ProtoNumber(1) val date: Long,
    @ProtoNumber(2) val readDuration: Long,
    @ProtoNumber(3) val chaptersRead: Long,
    @ProtoNumber(4) val wordsRead: Long,
)

@Serializable
data class SettingSnapshot(
    @ProtoNumber(1) val key: String,
    @ProtoNumber(2) val value: String,
)

// ── Operation results ─────────────────────────────────────────────────────

data class BackupSummary(
    val booksCount: Int,
    val chaptersCount: Int,
    val historyCount: Int = 0,
    val categoriesCount: Int = 0,
    val tracksCount: Int = 0,
    val fileSizeBytes: Long = 0,
)

data class RestoreSummary(
    val booksRestored: Int,
    val chaptersRestored: Int,
    val historyRestored: Int = 0,
    val categoriesRestored: Int = 0,
    val tracksRestored: Int = 0,
    val themesRestored: Int = 0,
    val settingsRestored: Int = 0,
    val errors: List<RestoreItemError> = emptyList(),
)

data class RestoreItemError(
    val itemType: String,
    val itemName: String,
    val error: String,
)

data class ValidationResult(
    val isValid: Boolean,
    val version: Int = 0,
    val bookCount: Int = 0,
    val chapterCount: Int = 0,
    val historyCount: Int = 0,
    val categoryCount: Int = 0,
    val errors: List<String> = emptyList(),
)

// ── Options (ALL DEFAULT TO TRUE FOR FULL BACKUP & RESTORE) ────────────────

data class BackupOptions(
    val includeBooks: Boolean = true,
    val includeChapters: Boolean = true,
    val includeChapterContent: Boolean = true,
    val includeCategories: Boolean = true,
    val includeHistory: Boolean = true,
    val includeTracks: Boolean = true,
    val includeThemes: Boolean = true,
    val includeSettings: Boolean = true,
)

data class RestoreOptions(
    val restoreBooks: Boolean = true,
    val restoreChapters: Boolean = true,
    val restoreCategories: Boolean = true,
    val restoreHistory: Boolean = true,
    val restoreTracks: Boolean = true,
    val restoreThemes: Boolean = true,
    val restoreSettings: Boolean = true,
    val mergeMode: MergeMode = MergeMode.MERGE_PREFER_BACKUP,
)

enum class MergeMode {
    MERGE_PREFER_BACKUP,
    MERGE_PREFER_DB,
    REPLACE_EXISTING,
}

// ── Progress ──────────────────────────────────────────────────────────────

sealed class BackupProgress {
    data object Collecting : BackupProgress()
    data class Serializing(val bookIndex: Int, val totalBooks: Int, val bookName: String) : BackupProgress()
    data object Compressing : BackupProgress()
    data object Writing : BackupProgress()
    data object Verifying : BackupProgress()
    data object Complete : BackupProgress()
}

sealed class RestoreProgress {
    data object Reading : RestoreProgress()
    data object Decompressing : RestoreProgress()
    data object Validating : RestoreProgress()
    data class Restoring(val bookIndex: Int, val totalBooks: Int, val bookName: String) : RestoreProgress()
    data object Complete : RestoreProgress()
}
