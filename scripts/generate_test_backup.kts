#!/usr/bin/env kotlin

/**
 * Script to generate a test backup file with 10,000+ books for performance testing.
 * 
 * Usage: 
 *   kotlinc -script generate_test_backup.kts
 * 
 * Or run via Gradle:
 *   ./gradlew -q --console=plain -PscriptArgs="10000" runGenerateBackup
 * 
 * Output: test_backup_10000.ireader (protobuf format)
 */

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.0")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import java.io.File
import kotlin.random.Random

// Proto models matching IReader's backup format
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

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CategoryProto(
    @ProtoNumber(1) val name: String,
    @ProtoNumber(2) val order: Long,
    @ProtoNumber(3) val updateInterval: Int = 0,
    @ProtoNumber(4) val flags: Long = 0
)

@Serializable
data class Backup(
    val library: List<BookProto> = emptyList(),
    val categories: List<CategoryProto> = emptyList()
)

// Sample data for realistic book generation
val genres = listOf(
    "Fantasy", "Romance", "Action", "Adventure", "Comedy", "Drama", "Horror",
    "Mystery", "Sci-Fi", "Slice of Life", "Supernatural", "Thriller", "Historical",
    "Martial Arts", "School Life", "Sports", "Tragedy", "Psychological", "Seinen",
    "Shounen", "Shoujo", "Josei", "Isekai", "Harem", "Ecchi", "Mecha"
)

val titlePrefixes = listOf(
    "The", "A", "My", "Our", "Your", "His", "Her", "Their", "This", "That",
    "One", "Last", "First", "Final", "Ultimate", "Supreme", "Divine", "Eternal",
    "Infinite", "Legendary", "Epic", "Grand", "Great", "True", "Real"
)

val titleNouns = listOf(
    "Hero", "King", "Queen", "Prince", "Princess", "Knight", "Mage", "Wizard",
    "Dragon", "Phoenix", "Wolf", "Tiger", "Lion", "Eagle", "Sword", "Shield",
    "Crown", "Throne", "Kingdom", "Empire", "World", "Universe", "Realm", "Domain",
    "Path", "Way", "Road", "Journey", "Adventure", "Quest", "Legend", "Myth",
    "Story", "Tale", "Chronicle", "Saga", "Epic", "Novel", "Book", "Chapter"
)

val titleSuffixes = listOf(
    "of Destiny", "of Fate", "of Power", "of Glory", "of Honor", "of Love",
    "of War", "of Peace", "of Light", "of Darkness", "of Fire", "of Ice",
    "of Thunder", "of Wind", "of Earth", "of Water", "of Life", "of Death",
    "Reborn", "Awakened", "Ascended", "Transcended", "Evolved", "Transformed",
    "Returns", "Rises", "Falls", "Begins", "Ends", "Continues"
)

val authorFirstNames = listOf(
    "John", "Jane", "Michael", "Sarah", "David", "Emily", "James", "Emma",
    "Robert", "Olivia", "William", "Sophia", "Richard", "Isabella", "Joseph",
    "Mia", "Thomas", "Charlotte", "Charles", "Amelia", "Daniel", "Harper",
    "Yuki", "Sakura", "Takeshi", "Haruki", "Kenji", "Akira", "Ryu", "Hana"
)

val authorLastNames = listOf(
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
    "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
    "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
    "Tanaka", "Yamamoto", "Suzuki", "Watanabe", "Sato", "Nakamura", "Kobayashi"
)

val sources = listOf(
    1L to "Novel Updates",
    2L to "Royal Road", 
    3L to "Webnovel",
    4L to "Wuxiaworld",
    5L to "Light Novel Pub",
    6L to "Novel Full",
    7L to "Read Light Novel",
    8L to "Box Novel",
    9L to "Novel Bin",
    10L to "All Novel Full"
)

fun generateTitle(): String {
    val usePrefix = Random.nextBoolean()
    val useSuffix = Random.nextBoolean()
    
    val parts = mutableListOf<String>()
    if (usePrefix) parts.add(titlePrefixes.random())
    parts.add(titleNouns.random())
    if (useSuffix) parts.add(titleSuffixes.random())
    
    return parts.joinToString(" ")
}

fun generateAuthor(): String {
    return "${authorFirstNames.random()} ${authorLastNames.random()}"
}

fun generateDescription(): String {
    val templates = listOf(
        "In a world where %s rules, one %s must rise to challenge the %s and restore %s to the land.",
        "Follow the journey of %s as they discover their hidden %s and embark on an epic %s.",
        "When %s threatens the kingdom, only the chosen %s can wield the ancient %s to save everyone.",
        "A tale of %s, %s, and the unbreakable bonds of %s that transcend time itself.",
        "After being betrayed by %s, our hero seeks %s and discovers a power beyond %s."
    )
    
    val words = listOf("power", "destiny", "fate", "love", "hatred", "courage", "wisdom", 
        "strength", "magic", "darkness", "light", "hope", "despair", "friendship", "betrayal")
    
    return templates.random().format(words.random(), words.random(), words.random(), words.random())
}

fun generateChapters(bookId: Long, count: Int): List<ChapterProto> {
    val baseTime = System.currentTimeMillis() - (count * 86400000L) // Start from count days ago
    
    return (1..count).map { i ->
        val readProgress = Random.nextFloat()
        ChapterProto(
            key = "/book/$bookId/chapter/$i",
            name = "Chapter $i: ${titleNouns.random()} ${titleSuffixes.random()}",
            translator = if (Random.nextFloat() > 0.7) generateAuthor() else "",
            read = readProgress > 0.5,
            bookmark = Random.nextFloat() > 0.9,
            dateFetch = baseTime + (i * 86400000L),
            dateUpload = baseTime + (i * 86400000L) - 3600000L,
            number = i.toFloat(),
            sourceOrder = i.toLong(),
            content = "", // Empty content to keep file size reasonable
            type = 0,
            lastPageRead = if (readProgress > 0.3) Random.nextLong(0, 1000) else 0
        )
    }
}

fun generateBook(id: Long, categoryIds: List<Long>): BookProto {
    val source = sources.random()
    val chapterCount = Random.nextInt(10, 200) // 10-200 chapters per book
    val baseTime = System.currentTimeMillis() - Random.nextLong(0, 365L * 24 * 60 * 60 * 1000) // Within last year
    
    val bookGenres = genres.shuffled().take(Random.nextInt(1, 5))
    val assignedCategories = if (categoryIds.isNotEmpty() && Random.nextFloat() > 0.3) {
        categoryIds.shuffled().take(Random.nextInt(1, minOf(3, categoryIds.size + 1)))
    } else {
        emptyList()
    }
    
    return BookProto(
        sourceId = source.first,
        key = "/novel/$id",
        title = generateTitle(),
        author = generateAuthor(),
        description = generateDescription(),
        genres = bookGenres,
        status = Random.nextLong(0, 4), // 0=Unknown, 1=Ongoing, 2=Completed, 3=Licensed, etc.
        cover = "https://picsum.photos/seed/$id/300/400", // Random placeholder image
        customCover = "",
        lastUpdate = baseTime + Random.nextLong(0, 30L * 24 * 60 * 60 * 1000),
        initialized = true,
        dateAdded = baseTime,
        viewer = 0,
        flags = 0,
        chapters = generateChapters(id, chapterCount),
        categories = assignedCategories,
        tracks = emptyList(),
        histories = if (Random.nextFloat() > 0.5) {
            listOf(HistoryProto(
                bookId = id,
                chapterId = Random.nextLong(1, chapterCount.toLong()),
                readAt = System.currentTimeMillis() - Random.nextLong(0, 7L * 24 * 60 * 60 * 1000),
                progress = Random.nextLong(0, 100)
            ))
        } else emptyList()
    )
}

fun generateCategories(): List<CategoryProto> {
    return listOf(
        CategoryProto("Reading", 1, 0, 0),
        CategoryProto("Completed", 2, 0, 0),
        CategoryProto("On Hold", 3, 0, 0),
        CategoryProto("Plan to Read", 4, 0, 0),
        CategoryProto("Dropped", 5, 0, 0),
        CategoryProto("Favorites", 6, 0, 0),
        CategoryProto("Re-reading", 7, 0, 0),
        CategoryProto("Korean", 8, 0, 0),
        CategoryProto("Chinese", 9, 0, 0),
        CategoryProto("Japanese", 10, 0, 0)
    )
}

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val bookCount = 10500 // Generate 10,500 books
    val outputFile = File("test_backup_${bookCount}.ireader")
    
    println("Generating test backup with $bookCount books...")
    println("This may take a few minutes...")
    
    val categories = generateCategories()
    val categoryIds = categories.map { it.order }
    
    val books = mutableListOf<BookProto>()
    
    for (i in 1..bookCount) {
        books.add(generateBook(i.toLong(), categoryIds))
        
        if (i % 1000 == 0) {
            println("Generated $i / $bookCount books...")
        }
    }
    
    println("Creating backup structure...")
    val backup = Backup(
        library = books,
        categories = categories
    )
    
    println("Serializing to protobuf...")
    val data = ProtoBuf.encodeToByteArray(backup)
    
    println("Writing to file: ${outputFile.absolutePath}")
    outputFile.writeBytes(data)
    
    val fileSizeMB = outputFile.length() / (1024.0 * 1024.0)
    println("Done! Generated backup file: ${outputFile.name}")
    println("File size: %.2f MB".format(fileSizeMB))
    println("Books: $bookCount")
    println("Categories: ${categories.size}")
    
    val totalChapters = books.sumOf { it.chapters.size }
    println("Total chapters: $totalChapters")
}

main()
