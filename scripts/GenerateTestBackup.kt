package scripts

/**
 * Run this from Android Studio or command line to generate a test backup.
 * 
 * Usage: 
 *   ./gradlew :domain:test --tests "scripts.GenerateTestBackupKt.main"
 * 
 * Or copy this code into a scratch file in Android Studio.
 */

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import java.io.File
import java.util.zip.GZIPOutputStream
import kotlin.random.Random

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ChapterProto(
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
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class HistoryProto(
    @ProtoNumber(1) val bookId: Long,
    @ProtoNumber(2) val chapterId: Long,
    @ProtoNumber(3) val readAt: Long,
    @ProtoNumber(4) val progress: Long = 0,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class TrackProto(
    @ProtoNumber(1) val siteId: Int,
    @ProtoNumber(2) val entryId: Long,
    @ProtoNumber(3) val mediaId: Long = 0,
    @ProtoNumber(4) val mediaUrl: String = "",
    @ProtoNumber(5) val title: String = "",
    @ProtoNumber(6) val lastRead: Float = 0f,
    @ProtoNumber(7) val totalChapters: Int = 0,
    @ProtoNumber(8) val score: Float = 0f,
    @ProtoNumber(9) val status: Int = 0,
    @ProtoNumber(10) val startReadTime: Long = 0,
    @ProtoNumber(11) val endReadTime: Long = 0
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CategoryProto(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val order: Long,
    @ProtoNumber(3) val updateInterval: Int = 0,
    @ProtoNumber(4) val flags: Long = 0
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BookProto(
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
    @ProtoNumber(15) val chapters: List<ChapterProto> = emptyList(),
    @ProtoNumber(16) val categories: List<Long> = emptyList(),
    @ProtoNumber(17) val tracks: List<TrackProto> = emptyList(),
    @ProtoNumber(18) val histories: List<HistoryProto> = emptyList(),
)

@Serializable
data class Backup(
    val library: List<BookProto> = emptyList(),
    val categories: List<CategoryProto> = emptyList()
)

val GENRES = listOf("Fantasy", "Romance", "Action", "Adventure", "Comedy", "Drama", "Sci-Fi", "Mystery")
val TITLE_PARTS = listOf("Hero", "King", "Dragon", "Sword", "Magic", "World", "Legend", "Path")

fun generateTitle(): String {
    return "The ${TITLE_PARTS.random()} of ${TITLE_PARTS.random()}"
}

fun generateChapters(bookId: Long, count: Int): List<ChapterProto> {
    val now = System.currentTimeMillis()
    val readCount = (count * Random.nextFloat() * 0.8).toInt()
    
    return (1..count).map { i ->
        ChapterProto(
            key = "/novel/$bookId/chapter-$i",
            name = "Chapter $i: ${TITLE_PARTS.random()}",
            read = i <= readCount,
            dateFetch = now - (count - i) * 86400000L,
            dateUpload = now - (count - i) * 86400000L,
            number = i.toFloat(),
            sourceOrder = i.toLong(),
            lastPageRead = if (i == readCount) Random.nextLong(100, 2000) else 0
        )
    }
}

fun generateBook(id: Long, categoryIds: List<Long>): BookProto {
    val now = System.currentTimeMillis()
    val chapterCount = Random.nextInt(20, 100)
    val chapters = generateChapters(id, chapterCount)
    val lastReadChapter = chapters.indexOfLast { it.read }.takeIf { it >= 0 } ?: 0
    
    return BookProto(
        sourceId = Random.nextLong(1, 10),
        key = "/novel/$id",
        title = generateTitle(),
        author = "Author ${id % 100}",
        description = "This is a test book description for book $id.",
        genres = GENRES.shuffled().take(Random.nextInt(1, 4)),
        status = Random.nextLong(0, 3),
        cover = "https://picsum.photos/seed/$id/300/400",
        lastUpdate = now - Random.nextLong(0, 30) * 86400000L,
        initialized = true,
        dateAdded = now - Random.nextLong(30, 365) * 86400000L,
        chapters = chapters,
        categories = if (categoryIds.isNotEmpty()) listOf(categoryIds.random()) else emptyList(),
        histories = if (lastReadChapter > 0) listOf(
            HistoryProto(
                bookId = id,
                chapterId = lastReadChapter.toLong(),
                readAt = now - Random.nextLong(0, 7) * 86400000L,
                progress = Random.nextLong(50, 100)
            )
        ) else emptyList()
    )
}

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val bookCount = 10500
    println("Generating $bookCount books...")
    
    val categories = listOf(
        CategoryProto("Reading", 1),
        CategoryProto("Completed", 2),
        CategoryProto("On Hold", 3),
        CategoryProto("Plan to Read", 4),
        CategoryProto("Dropped", 5),
    )
    val categoryIds = categories.map { it.order }
    
    val books = (1..bookCount).map { i ->
        if (i % 1000 == 0) println("Generated $i books...")
        generateBook(i.toLong(), categoryIds)
    }
    
    println("Creating backup...")
    val backup = Backup(library = books, categories = categories)
    
    println("Encoding to protobuf...")
    val data = ProtoBuf.encodeToByteArray(backup)
    
    println("Compressing with gzip...")
    val outputFile = File("test_backup_kotlin_$bookCount.gz")
    GZIPOutputStream(outputFile.outputStream()).use { gzip ->
        gzip.write(data)
    }
    
    println("Done!")
    println("File: ${outputFile.absolutePath}")
    println("Size: ${outputFile.length() / 1024 / 1024} MB")
    println("Books: $bookCount")
    println("Chapters: ${books.sumOf { it.chapters.size }}")
}
