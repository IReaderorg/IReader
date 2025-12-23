#!/usr/bin/env kotlin

/**
 * Kotlin script to create a test backup and dump its hex format.
 * This helps verify the correct protobuf encoding.
 * 
 * Run with: kotlinc -script dump_backup_format.main.kts
 */

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.0")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

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
    @ProtoNumber(17) val tracks: List<String> = emptyList(),
    @ProtoNumber(18) val histories: List<String> = emptyList(),
)

@Serializable
data class Backup(
    val library: List<BookProto> = emptyList(),
    val categories: List<CategoryProto> = emptyList()
)

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val now = System.currentTimeMillis()
    
    // Create simple test data
    val chapter = ChapterProto(
        key = "/novel/1/chapter-1",
        name = "Chapter 1: Test",
        read = true,
        dateFetch = now,
        number = 1.0f,
        sourceOrder = 1
    )
    
    val book = BookProto(
        sourceId = 1,
        key = "/novel/1",
        title = "Test Book",
        author = "Test Author",
        genres = listOf("Fantasy"),
        status = 1,
        cover = "https://example.com/cover.jpg",
        lastUpdate = now,
        initialized = true,
        dateAdded = now,
        chapters = listOf(chapter),
        categories = listOf(1)
    )
    
    val category = CategoryProto(
        name = "Reading",
        order = 1
    )
    
    val backup = Backup(
        library = listOf(book),
        categories = listOf(category)
    )
    
    val data = ProtoBuf.encodeToByteArray(backup)
    
    println("Backup size: ${data.size} bytes")
    println("Hex dump (first 200 bytes):")
    println(data.take(200).joinToString("") { "%02x".format(it) })
    
    // Also dump individual components
    println("\n--- Book only ---")
    val bookData = ProtoBuf.encodeToByteArray(book)
    println("Book size: ${bookData.size} bytes")
    println("Hex: ${bookData.take(100).joinToString("") { "%02x".format(it) }}")
    
    println("\n--- Chapter only ---")
    val chapterData = ProtoBuf.encodeToByteArray(chapter)
    println("Chapter size: ${chapterData.size} bytes")
    println("Hex: ${chapterData.joinToString("") { "%02x".format(it) }}")
    
    println("\n--- Category only ---")
    val categoryData = ProtoBuf.encodeToByteArray(category)
    println("Category size: ${categoryData.size} bytes")
    println("Hex: ${categoryData.joinToString("") { "%02x".format(it) }}")
}

main()
