package ireader.presentation.ui.quote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.DiscordQuoteRepository
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.usecases.quote.LocalQuoteUseCases
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for Quote Story Editor screen.
 * Handles saving locally and sharing to Discord.
 */
class QuoteStoryEditorViewModel(
    private val localQuoteUseCases: LocalQuoteUseCases,
    private val discordQuoteRepository: DiscordQuoteRepository,
    private val bookId: Long,
    private val initialBookTitle: String,
    private val initialChapterTitle: String,
    private val initialAuthor: String?,
    private val chapterNumber: Int?,
    private val currentChapterId: Long?,
    private val prevChapterId: Long?,
    private val nextChapterId: Long?
) : BaseViewModel() {
    
    var quoteText by mutableStateOf("")
        private set
    
    var bookTitle by mutableStateOf(initialBookTitle)
        private set
    
    var author by mutableStateOf(initialAuthor ?: "")
        private set
    
    var selectedStyle by mutableStateOf(QuoteCardStyle.GRADIENT_SUNSET)
        private set
    
    var isSaving by mutableStateOf(false)
        private set
    
    var isSharing by mutableStateOf(false)
        private set
    
    var saveSuccess by mutableStateOf(false)
        private set
    
    fun updateQuoteText(text: String) {
        quoteText = text
    }
    
    fun updateBookTitle(title: String) {
        bookTitle = title
    }
    
    fun updateAuthor(authorName: String) {
        author = authorName
    }
    
    fun setStyle(style: QuoteCardStyle) {
        selectedStyle = style
    }
    
    /**
     * Save quote locally to SQLDelight database
     */
    fun saveLocally(onSuccess: () -> Unit) {
        if (quoteText.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Please enter quote text"))
            }
            return
        }
        
        isSaving = true
        scope.launch {
            val result = localQuoteUseCases.saveQuote(
                text = quoteText,
                bookId = bookId,
                bookTitle = bookTitle,
                chapterTitle = initialChapterTitle,
                chapterNumber = chapterNumber,
                author = author.ifBlank { null },
                includeContext = false,
                currentChapterId = currentChapterId,
                prevChapterId = prevChapterId,
                nextChapterId = nextChapterId
            )
            
            isSaving = false
            
            result.fold(
                onSuccess = {
                    saveSuccess = true
                    showSnackBar(UiText.DynamicString("Quote saved! 📚"))
                    onSuccess()
                },
                onFailure = { error ->
                    showSnackBar(UiText.DynamicString("Failed to save: ${error.message}"))
                }
            )
        }
    }
    
    /**
     * Share quote to Discord webhook
     * Saves locally first, then submits to Discord
     */
    fun shareToDiscord(username: String, onSuccess: () -> Unit) {
        if (quoteText.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Please enter quote text"))
            }
            return
        }
        
        if (bookTitle.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Book title is required for sharing"))
            }
            return
        }
        
        isSharing = true
        
        scope.launch {
            // Save locally first
            val saveResult = localQuoteUseCases.saveQuote(
                text = quoteText,
                bookId = bookId,
                bookTitle = bookTitle,
                chapterTitle = initialChapterTitle,
                chapterNumber = chapterNumber,
                author = author.ifBlank { null },
                includeContext = false,
                currentChapterId = currentChapterId,
                prevChapterId = prevChapterId,
                nextChapterId = nextChapterId
            )
            
            saveResult.fold(
                onSuccess = { quoteId ->
                    // Create LocalQuote object for Discord submission
                    val localQuote = LocalQuote(
                        id = quoteId,
                        text = quoteText,
                        bookId = bookId,
                        bookTitle = bookTitle,
                        chapterTitle = initialChapterTitle,
                        chapterNumber = chapterNumber,
                        author = author.ifBlank { null },
                        hasContextBackup = false
                    )
                    
                    // Submit to Discord
                    val result = discordQuoteRepository.submitQuote(
                        quote = localQuote,
                        style = selectedStyle,
                        username = username
                    )
                    
                    isSharing = false
                    
                    result.fold(
                        onSuccess = {
                            showSnackBar(UiText.DynamicString("Quote shared to Discord! 🎉"))
                            onSuccess()
                        },
                        onFailure = { error ->
                            showSnackBar(UiText.DynamicString("Failed to share: ${error.message}"))
                        }
                    )
                },
                onFailure = { error ->
                    isSharing = false
                    showSnackBar(UiText.DynamicString("Failed to save: ${error.message}"))
                }
            )
        }
    }
    
    val characterCount: Int get() = quoteText.length
}
