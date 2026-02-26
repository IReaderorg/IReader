package ireader.presentation.ui.quote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.DiscordQuoteRepository
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.models.quote.QuoteCreationParams
import ireader.domain.models.quote.ShareValidation
import ireader.domain.usecases.quote.LocalQuoteUseCases
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for the Quote Creation screen
 */
class QuoteCreationViewModel(
    private val localQuoteUseCases: LocalQuoteUseCases,
    private val discordQuoteRepository: DiscordQuoteRepository,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?,
    val params: QuoteCreationParams
) : BaseViewModel() {
    
    // Quote text input
    var quoteText by mutableStateOf("")
        private set
    
    // Context backup toggle
    var includeContextBackup by mutableStateOf(false)
        private set
    
    // Loading states
    var isSaving by mutableStateOf(false)
        private set
    
    var isSharing by mutableStateOf(false)
        private set
    
    // Success state
    var saveSuccess by mutableStateOf(false)
        private set
    
    /**
     * Update quote text
     */
    fun updateQuoteText(text: String) {
        quoteText = text
    }
    
    /**
     * Toggle context backup
     */
    fun toggleContextBackup() {
        includeContextBackup = !includeContextBackup
    }
    
    /**
     * Save quote locally
     */
    fun saveQuoteLocally(onSuccess: () -> Unit) {
        if (quoteText.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Please enter quote text"))
            }
            return
        }
        
        // Validate minimum quote length
        if (quoteText.length < 10) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Quote must be at least 10 characters"))
            }
            return
        }
        
        isSaving = true
        scope.launch {
            val result = localQuoteUseCases.saveQuote(
                text = quoteText,
                bookId = params.bookId,
                bookTitle = params.bookTitle,
                chapterTitle = params.chapterTitle,
                chapterNumber = params.chapterNumber,
                author = params.author,
                includeContext = includeContextBackup,
                currentChapterId = params.currentChapterId,
                prevChapterId = params.prevChapterId,
                nextChapterId = params.nextChapterId
            )
            
            isSaving = false
            
            result.fold(
                onSuccess = {
                    saveSuccess = true
                    showSnackBar(UiText.DynamicString("Quote saved!"))
                    onSuccess()
                },
                onFailure = { error ->
                    showSnackBar(UiText.DynamicString("Failed to save: ${error.message}"))
                }
            )
        }
    }
    
    /**
     * Share quote to Discord
     */
    fun shareToDiscord(style: QuoteCardStyle, username: String, onSuccess: () -> Unit) {
        if (quoteText.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Please enter quote text"))
            }
            return
        }
        
        isSharing = true
        
        scope.launch {
            // Get username from Supabase if available
            val finalUsername = try {
                getCurrentUser()?.username ?: username
            } catch (e: Exception) {
                username
            }
            
            // Save locally first
            val saveResult = localQuoteUseCases.saveQuote(
                text = quoteText,
                bookId = params.bookId,
                bookTitle = params.bookTitle,
                chapterTitle = params.chapterTitle,
                chapterNumber = params.chapterNumber,
                author = params.author,
                includeContext = includeContextBackup,
                currentChapterId = params.currentChapterId,
                prevChapterId = params.prevChapterId,
                nextChapterId = params.nextChapterId
            )
            
            saveResult.fold(
                onSuccess = { quoteId ->
                    // Create LocalQuote object for Discord submission
                    val localQuote = LocalQuote(
                        id = quoteId,
                        text = quoteText,
                        bookId = params.bookId,
                        bookTitle = params.bookTitle,
                        chapterTitle = params.chapterTitle,
                        chapterNumber = params.chapterNumber,
                        author = params.author,
                        hasContextBackup = includeContextBackup
                    )
                    
                    // Submit to Discord
                    val result = discordQuoteRepository.submitQuote(
                        quote = localQuote,
                        style = style,
                        username = finalUsername
                    )
                    
                    isSharing = false
                    
                    result.fold(
                        onSuccess = {
                            showSnackBar(UiText.DynamicString("Quote shared to Discord!"))
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
    
    /**
     * Get character count info
     */
    val characterCount: Int get() = quoteText.length
}
