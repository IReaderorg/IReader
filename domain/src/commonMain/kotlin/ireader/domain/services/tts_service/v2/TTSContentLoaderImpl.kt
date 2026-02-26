package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.reader.ContentFilterUseCase
import ireader.domain.usecases.reader.TextReplacementUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.tts.TTSTextSanitizer
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Implementation of TTSContentLoader that uses existing repositories
 * 
 * This is a simple, focused use case that:
 * - Loads book and chapter from repositories
 * - Parses chapter content (List<Page>) into paragraphs
 * - Fetches content from remote if not available locally
 * - Applies text replacements first
 * - Applies content filter to remove unwanted text patterns
 * - Sanitizes text for TTS (removes brackets, special characters)
 */
class TTSContentLoaderImpl(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val chapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val catalogStore: CatalogStore,
    private val contentFilterUseCase: ContentFilterUseCase? = null,
    private val textReplacementUseCase: TextReplacementUseCase? = null,
    private val ttsSanitizer: TTSTextSanitizer = TTSTextSanitizer()
) : TTSContentLoader {
    
    companion object {
        private const val TAG = "TTSContentLoader"
    }
    
    override suspend fun loadChapter(bookId: Long, chapterId: Long): TTSContentLoader.ChapterContent {
        Log.warn { "$TAG: loadChapter(bookId=$bookId, chapterId=$chapterId)" }
        
        // Load book
        val book = bookRepository.findBookById(bookId)
        if (book == null) {
            Log.warn { "$TAG: Book not found: $bookId" }
            throw IllegalStateException("Book not found: $bookId")
        }
        
        // Load chapter using use case (applies content filtering)
        var chapter = chapterUseCase.findChapterById(chapterId)
        if (chapter == null) {
            Log.warn { "$TAG: Chapter not found: $chapterId" }
            throw IllegalStateException("Chapter not found: $chapterId")
        }
        
        // Check if content is available, fetch if needed
        var content = chapter.content
        if (content.isEmpty()) {
            Log.warn { "$TAG: Chapter content empty, attempting to fetch from remote" }
            val fetchedContent = fetchChapterContent(book, chapter)
            if (fetchedContent != null && fetchedContent.isNotEmpty()) {
                // Re-read from DB to get filtered content
                val refreshedChapter = chapterUseCase.findChapterById(chapterId)
                if (refreshedChapter != null) {
                    chapter = refreshedChapter
                    content = refreshedChapter.content
                } else {
                    content = fetchedContent
                    chapter = chapter.copy(content = content)
                }
            }
        }
        
        // Parse content into paragraphs (extract text from Page objects)
        // Note: content is already filtered by the use case
        val paragraphs = parseContent(content)
        
        Log.warn { "$TAG: Loaded chapter with ${paragraphs.size} paragraphs" }
        
        return TTSContentLoader.ChapterContent(
            book = book,
            chapter = chapter,
            paragraphs = paragraphs
        )
    }
    
    /**
     * Fetch chapter content from remote source using FetchAndSaveChapterContentUseCase.
     * Returns the filtered content after saving to DB.
     */
    private suspend fun fetchChapterContent(book: Book, chapter: Chapter): List<Page>? {
        Log.warn { "$TAG: fetchChapterContent - sourceId=${book.sourceId}" }
        
        // Get the catalog/source for this book
        val catalog = catalogStore.get(book.sourceId)
        if (catalog == null) {
            Log.warn { "$TAG: Catalog not found for sourceId=${book.sourceId}" }
            return null
        }
        
        return try {
            // Use suspendCancellableCoroutine to convert callback-based API to suspend
            suspendCancellableCoroutine { continuation ->
                var resumed = false
                
                CoroutineScope(ioDispatcher).launch {
                    remoteUseCases.fetchAndSaveChapterContent(
                        chapter = chapter,
                        catalog = catalog,
                        onError = { error ->
                            Log.error { "$TAG: Failed to fetch from remote: $error" }
                            if (!resumed) {
                                resumed = true
                                continuation.resume(null)
                            }
                        },
                        onSuccess = { filteredChapter ->
                            Log.warn { "$TAG: Successfully fetched and filtered ${filteredChapter.content.size} pages" }
                            if (!resumed) {
                                resumed = true
                                continuation.resume(filteredChapter.content)
                            }
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to fetch chapter content: ${e.message}" }
            null
        }
    }
    
    /**
     * Parse chapter content (List<Page>) into paragraphs
     * 
     * Extracts text from Text pages and cleans HTML if present.
     * Note: Content is already filtered at the Page level by FindChapterById use case.
     * This method applies additional string-level processing after HTML cleaning/splitting.
     */
    private suspend fun parseContent(content: List<Page>): List<String> {
        if (content.isEmpty()) {
            return emptyList()
        }
        
        // Extract text from Text pages (already filtered at Page level)
        val textContent = content
            .filterIsInstance<Text>()
            .map { it.text }
            .filter { it.isNotBlank() }
        
        // If we have text pages, process them
        if (textContent.isNotEmpty()) {
            val paragraphs = textContent.flatMap { text ->
                cleanAndSplitText(text)
            }
            
            // Step 1: Apply text replacements first
            val replaced = textReplacementUseCase?.applyReplacementsToStrings(paragraphs) ?: paragraphs
            
            // Step 2: Apply content filter to remove unwanted patterns
            val filtered = contentFilterUseCase?.filterStrings(replaced) ?: replaced
            
            // Step 3: Sanitize for TTS - remove brackets and special characters that shouldn't be read aloud
            return ttsSanitizer.sanitizeList(filtered)
        }
        
        return emptyList()
    }
    
    /**
     * Clean HTML and split text into paragraphs
     */
    private fun cleanAndSplitText(text: String): List<String> {
        // Basic HTML tag removal (if present)
        val cleanContent = text
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), "") // Remove remaining HTML tags
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
        
        // Split into paragraphs and filter empty ones
        return cleanContent
            .split(Regex("\n+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
    
    /**
     * Get the next chapter ID for a book
     */
    override suspend fun getNextChapterId(bookId: Long, currentChapterId: Long): Long? {
        Log.warn { "$TAG: getNextChapterId(bookId=$bookId, currentChapterId=$currentChapterId)" }
        
        try {
            // Get all chapters for the book sorted by source order
            val chapters = chapterRepository.findChaptersByBookId(bookId)
            if (chapters.isEmpty()) return null
            
            // Find current chapter index
            val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
            if (currentIndex == -1) return null
            
            // Get next chapter (chapters are typically sorted by sourceOrder ascending)
            val nextIndex = currentIndex + 1
            return if (nextIndex < chapters.size) {
                chapters[nextIndex].id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to get next chapter: ${e.message}" }
            return null
        }
    }
    
    /**
     * Get the previous chapter ID for a book
     */
    override suspend fun getPreviousChapterId(bookId: Long, currentChapterId: Long): Long? {
        Log.warn { "$TAG: getPreviousChapterId(bookId=$bookId, currentChapterId=$currentChapterId)" }
        
        try {
            // Get all chapters for the book sorted by source order
            val chapters = chapterRepository.findChaptersByBookId(bookId)
            if (chapters.isEmpty()) return null
            
            // Find current chapter index
            val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }
            if (currentIndex == -1) return null
            
            // Get previous chapter
            val prevIndex = currentIndex - 1
            return if (prevIndex >= 0) {
                chapters[prevIndex].id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to get previous chapter: ${e.message}" }
            return null
        }
    }
}
