package ireader.domain.plugins.providers

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.library.LibrarySort
import ireader.plugin.api.CharacterInfo
import ireader.plugin.api.CharacterServiceProvider
import ireader.plugin.api.ChapterInfo
import ireader.plugin.api.Glossary
import ireader.plugin.api.GlossaryEntry
import ireader.plugin.api.GlossaryServiceProvider
import ireader.plugin.api.HttpResponse
import ireader.plugin.api.LibraryServiceProvider
import ireader.plugin.api.MultipartData
import ireader.plugin.api.PluginCharacterRelationship
import ireader.plugin.api.PluginHttpClientProvider
import ireader.plugin.api.ReaderContextProvider
import ireader.plugin.api.ReadingProgress
import ireader.plugin.api.SyncServiceProvider
import ireader.plugin.api.SyncStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.time.ExperimentalTime
import ireader.plugin.api.LibraryBook as PluginLibraryBook

/**
 * HTTP client provider implementation for plugins.
 */
class PluginHttpClientProviderImpl(
    private val httpClient: HttpClient
) : PluginHttpClientProvider {
    
    override suspend fun get(url: String, headers: Map<String, String>): HttpResponse {
        val response = httpClient.get(url) {
            headers.forEach { (key, value) -> header(key, value) }
        }
        return HttpResponse(
            statusCode = response.status.value,
            body = response.bodyAsText(),
            headers = response.headers.entries().associate { it.key to it.value.firstOrNull().orEmpty() }
        )
    }
    
    override suspend fun post(url: String, body: String, headers: Map<String, String>): HttpResponse {
        val response = httpClient.post(url) {
            headers.forEach { (key, value) -> header(key, value) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return HttpResponse(
            statusCode = response.status.value,
            body = response.bodyAsText(),
            headers = response.headers.entries().associate { it.key to it.value.firstOrNull().orEmpty() }
        )
    }
    
    override suspend fun postForm(url: String, formData: Map<String, String>, headers: Map<String, String>): HttpResponse {
        val response = httpClient.post(url) {
            headers.forEach { (key, value) -> header(key, value) }
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(formData.entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        return HttpResponse(
            statusCode = response.status.value,
            body = response.bodyAsText(),
            headers = response.headers.entries().associate { it.key to it.value.firstOrNull().orEmpty() }
        )
    }
    
    override suspend fun postMultipart(url: String, parts: List<MultipartData>, headers: Map<String, String>): HttpResponse {
        // Simplified multipart - full implementation would use Ktor's multipart support
        val response = httpClient.post(url) {
            headers.forEach { (key, value) -> header(key, value) }
        }
        return HttpResponse(
            statusCode = response.status.value,
            body = response.bodyAsText(),
            headers = response.headers.entries().associate { it.key to it.value.firstOrNull().orEmpty() }
        )
    }
    
    override suspend fun download(url: String, headers: Map<String, String>): ByteArray {
        val response = httpClient.get(url) {
            headers.forEach { (key, value) -> header(key, value) }
        }
        return response.bodyAsText().encodeToByteArray()
    }
}

/**
 * Library service provider implementation for plugins.
 */
class LibraryServiceProviderImpl(
    private val libraryRepository: LibraryRepository,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository
) : LibraryServiceProvider {
    
    override suspend fun getLibraryBooks(): List<PluginLibraryBook> {
        return libraryRepository.findAll(LibrarySort.default).map { book ->
            PluginLibraryBook(
                id = book.id.toString(),
                sourceId = book.sourceId.toString(),
                title = book.title,
                author = null, // LibraryBook doesn't have author
                coverUrl = book.cover,
                status = book.status.toString(),
                chapterCount = book.totalChapters,
                lastReadChapter = null,
                addedAt = book.dateFetched,
                lastUpdated = book.lastUpdate
            )
        }
    }
    
    override suspend fun getBook(bookId: String): PluginLibraryBook? {
        val id = bookId.toLongOrNull() ?: return null
        val book = bookRepository.findBookById(id) ?: return null
        return PluginLibraryBook(
            id = book.id.toString(),
            sourceId = book.sourceId.toString(),
            title = book.title,
            author = book.author,
            coverUrl = book.cover,
            status = book.getStatusByName(),
            chapterCount = 0,
            lastReadChapter = null,
            addedAt = book.dateAdded,
            lastUpdated = book.lastUpdate
        )
    }
    
    override suspend fun getReadingProgress(bookId: String): ReadingProgress? {
        val id = bookId.toLongOrNull() ?: return null
        val chapters = chapterRepository.findChaptersByBookId(id)
        val lastRead = chapters.filter { it.read }.maxByOrNull { it.dateUpload }
        return lastRead?.let {
            ReadingProgress(
                chapterId = it.id.toString(),
                position = 0,
                percentage = 0f,
                lastRead = it.dateFetch
            )
        }
    }
    
    override suspend fun updateReadingProgress(bookId: String, progress: ReadingProgress) {
        // Mark chapter as read - simplified implementation
        // Note: insertChapter will update if the chapter already exists (based on ID)
        val chapterId = progress.chapterId.toLongOrNull() ?: return
        val chapter = chapterRepository.findChapterById(chapterId) ?: return
        chapterRepository.insertChapter(chapter.copy(read = true, dateFetch = progress.lastRead))
    }
}

/**
 * Reader context provider implementation for plugins.
 * This needs to be connected to the actual reader state.
 */
class ReaderContextProviderImpl(
    private val getCurrentBookProvider: () -> ireader.domain.models.entities.Book?,
    private val getCurrentChapterProvider: () -> ireader.domain.models.entities.Chapter?,
    private val getCurrentPositionProvider: () -> Int,
    private val getSelectedTextProvider: () -> String?,
    private val navigateToChapterAction: (String) -> Unit,
    private val navigateToPositionAction: (Int) -> Unit
) : ReaderContextProvider {
    
    override fun getCurrentBook(): PluginLibraryBook? {
        val book = getCurrentBookProvider() ?: return null
        return PluginLibraryBook(
            id = book.id.toString(),
            sourceId = book.sourceId.toString(),
            title = book.title,
            author = book.author,
            coverUrl = book.cover,
            status = book.getStatusByName(),
            chapterCount = 0,
            lastReadChapter = null,
            addedAt = book.dateAdded,
            lastUpdated = book.lastUpdate
        )
    }
    
    override fun getCurrentChapter(): ChapterInfo? {
        val chapter = getCurrentChapterProvider() ?: return null
        // Extract text content from chapter pages
        val textContent = chapter.content.mapNotNull { page ->
            when (page) {
                is ireader.core.source.model.Text -> page.text
                else -> null
            }
        }.joinToString("\n")
        return ChapterInfo(
            id = chapter.id.toString(),
            bookId = chapter.bookId.toString(),
            title = chapter.name,
            number = chapter.number,
            content = textContent
        )
    }
    
    override fun getCurrentPosition(): Int = getCurrentPositionProvider()
    
    override fun getSelectedText(): String? = getSelectedTextProvider()
    
    override fun navigateToChapter(chapterId: String) = navigateToChapterAction(chapterId)
    
    override fun navigateToPosition(position: Int) = navigateToPositionAction(position)
}

/**
 * Sync service provider implementation for plugins.
 */
class SyncServiceProviderImpl : SyncServiceProvider {
    private var lastSyncTime: Long? = null
    
    override suspend fun getLastSyncTime(): Long? = lastSyncTime
    
    @OptIn(ExperimentalTime::class)
    override suspend fun triggerSync() {
        // Would trigger actual sync
        lastSyncTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
    }
    
    override suspend fun getSyncStatus(): SyncStatus {
        return SyncStatus(
            isSyncing = false,
            progress = 100f,
            statusMessage = "Idle",
            lastSyncTime = lastSyncTime,
            autoSyncEnabled = false,
            pendingChanges = 0
        )
    }
}

/**
 * Glossary service provider implementation for plugins.
 * This is a stub - would need actual glossary storage.
 */
class GlossaryServiceProviderImpl : GlossaryServiceProvider {
    override suspend fun getGlossaries(): List<Glossary> = emptyList()
    
    override suspend fun lookupTerm(term: String, glossaryIds: List<String>): List<GlossaryEntry> = emptyList()
    
    override suspend fun applyGlossary(text: String, glossaryIds: List<String>): String = text
}

/**
 * Character service provider implementation for plugins.
 * This is a stub - would need actual character database.
 */
class CharacterServiceProviderImpl : CharacterServiceProvider {
    private val characters = mutableMapOf<String, CharacterInfo>()
    
    override suspend fun getCharacters(bookId: String): List<CharacterInfo> {
        val bookIdLong = bookId.toLongOrNull() ?: return emptyList()
        return characters.values.filter { bookIdLong in it.bookIds }
    }
    
    override suspend fun addCharacter(bookId: String, character: CharacterInfo): CharacterInfo {
        characters[character.id] = character
        return character
    }
    
    override suspend fun updateCharacter(character: CharacterInfo): CharacterInfo {
        characters[character.id] = character
        return character
    }
    
    override suspend fun deleteCharacter(characterId: String) {
        characters.remove(characterId)
    }
    
    override suspend fun getCharacterRelationships(characterId: String): List<PluginCharacterRelationship> {
        return emptyList()
    }
}
