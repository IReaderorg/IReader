package ireader.data.sync.fake

import ireader.domain.models.sync.*

/**
 * Test fixtures and builders for creating test data.
 */
object TestFixtures {
    
    fun createDevice(
        id: String = "test-device",
        name: String = "Test Device",
        deviceType: DeviceType = DeviceType.ANDROID,
        lastSeen: Long = System.currentTimeMillis()
    ) = DeviceInfo(
        deviceId = id,
        deviceName = name,
        deviceType = deviceType,
        appVersion = "1.0.0",
        ipAddress = "192.168.1.100",
        port = 8080,
        lastSeen = lastSeen
    )
    
    fun createBook(
        id: Long = 1L,
        title: String = "Test Book",
        author: String = "Test Author",
        lastModified: Long = System.currentTimeMillis(),
        coverUrl: String? = null,
        chapterCount: Int = 0
    ) = SyncableBook(
        id = id,
        title = title,
        author = author,
        lastModified = lastModified,
        coverUrl = coverUrl,
        chapters = (1..chapterCount).map { createChapter(it.toLong(), id, "Chapter $it") }
    )
    
    fun createChapter(
        id: Long = 1L,
        bookId: Long = 1L,
        title: String = "Test Chapter",
        content: String = "Test content",
        index: Int = 1
    ) = SyncableChapter(
        id = id,
        bookId = bookId,
        title = title,
        content = content,
        index = index
    )
    
    fun createPairedDevice(
        device: DeviceInfo = createDevice(),
        status: PairingStatus = PairingStatus.PAIRED,
        certificate: String = "test-cert",
        isTrusted: Boolean = true
    ) = PairedDevice(
        device = device,
        status = status,
        certificate = certificate,
        isTrusted = isTrusted
    )
    
    fun createSyncSession(
        id: String = "test-session",
        deviceId: String = "test-device",
        status: SessionStatus = SessionStatus.COMPLETED,
        totalItems: Int = 10,
        completedItems: Int = 10,
        failedItems: Int = 0,
        conflicts: List<SyncConflict> = emptyList(),
        itemsToSend: Int = 0,
        itemsToReceive: Int = 10
    ) = SyncSession(
        id = id,
        deviceId = deviceId,
        status = status,
        totalItems = totalItems,
        completedItems = completedItems,
        failedItems = failedItems,
        conflicts = conflicts,
        itemsToSend = itemsToSend,
        itemsToReceive = itemsToReceive
    )
    
    fun createSyncProgress(
        deviceId: String = "test-device",
        status: SyncStatus = SyncStatus.Syncing(
            deviceName = "Test Device",
            progress = 0.5f,
            currentItem = "Test Item",
            currentIndex = 5,
            totalItems = 10
        ),
        totalItems: Int = 10,
        completedItems: Int = 5,
        progressPercentage: Int = 50
    ) = SyncProgress(
        deviceId = deviceId,
        status = status,
        totalItems = totalItems,
        completedItems = completedItems,
        progressPercentage = progressPercentage
    )
    
    fun createConflict(
        itemId: Long = 1L,
        localVersion: SyncableBook = createBook(id = itemId, title = "Local Version"),
        remoteVersion: SyncableBook = createBook(id = itemId, title = "Remote Version")
    ) = SyncConflict(
        itemId = itemId,
        localVersion = localVersion,
        remoteVersion = remoteVersion
    )
}

/**
 * Builder for creating test books with fluent API.
 */
class BookBuilder {
    private var id: Long = 1L
    private var title: String = "Test Book"
    private var author: String = "Test Author"
    private var lastModified: Long = System.currentTimeMillis()
    private var coverUrl: String? = null
    private val chapters = mutableListOf<SyncableChapter>()
    
    fun withId(id: Long) = apply { this.id = id }
    fun withTitle(title: String) = apply { this.title = title }
    fun withAuthor(author: String) = apply { this.author = author }
    fun withLastModified(timestamp: Long) = apply { this.lastModified = timestamp }
    fun withCoverUrl(url: String?) = apply { this.coverUrl = url }
    
    fun addChapter(
        id: Long,
        title: String,
        content: String = "Test content",
        index: Int = chapters.size + 1
    ) = apply {
        chapters.add(
            SyncableChapter(
                id = id,
                bookId = this.id,
                title = title,
                content = content,
                index = index
            )
        )
    }
    
    fun addChapters(count: Int, contentSize: Int = 1000) = apply {
        repeat(count) { i ->
            addChapter(
                id = id * 1000 + i + 1,
                title = "Chapter ${i + 1}",
                content = "x".repeat(contentSize),
                index = i + 1
            )
        }
    }
    
    fun build() = SyncableBook(
        id = id,
        title = title,
        author = author,
        lastModified = lastModified,
        coverUrl = coverUrl,
        chapters = chapters.toList()
    )
}

/**
 * Extension function for creating books with builder.
 */
fun book(block: BookBuilder.() -> Unit): SyncableBook {
    return BookBuilder().apply(block).build()
}

/**
 * Extension function for creating multiple books.
 */
fun books(count: Int, block: (BookBuilder, Int) -> Unit = { _, _ -> }): List<SyncableBook> {
    return (1..count).map { i ->
        BookBuilder()
            .withId(i.toLong())
            .withTitle("Book $i")
            .apply { block(this, i) }
            .build()
    }
}
