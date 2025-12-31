package ireader.presentation.ui.quote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.QuoteRepository
import ireader.domain.models.quote.LocalQuote
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
    private val quoteRepository: QuoteRepository,
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
    
    // Share validation
    var shareValidation by mutableStateOf<ShareValidation?>(null)
        private set
    
    // Show truncation dialog
    var showTruncationDialog by mutableStateOf(false)
        private set
    
    /**
     * Update quote text
     */
    fun updateQuoteText(text: String) {
        quoteText = text
        // Update share validation
        shareValidation = if (text.isNotEmpty()) {
            localQuoteUseCases.validateForCommunityShare(
                LocalQuote(
                    text = text,
                    bookId = params.bookId,
                    bookTitle = params.bookTitle,
                    chapterTitle = params.chapterTitle
                )
            )
        } else null
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
     * Attempt to share quote to community
     */
    fun shareQuoteToCommunity(onSuccess: () -> Unit) {
        if (quoteText.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Please enter quote text"))
            }
            return
        }
        
        val validation = localQuoteUseCases.validateForCommunityShare(
            LocalQuote(
                text = quoteText,
                bookId = params.bookId,
                bookTitle = params.bookTitle,
                chapterTitle = params.chapterTitle
            )
        )
        
        when {
            validation.tooShort -> {
                scope.launch {
                    showSnackBar(UiText.DynamicString("Quote is too short (min 10 characters)"))
                }
            }
            validation.needsTruncation -> {
                showTruncationDialog = true
            }
            validation.canShare -> {
                submitToCommunity(truncate = false, onSuccess = onSuccess)
            }
        }
    }
    
    /**
     * Dismiss truncation dialog
     */
    fun dismissTruncationDialog() {
        showTruncationDialog = false
    }
    
    /**
     * Submit quote to community (with optional truncation)
     */
    fun submitToCommunity(truncate: Boolean, onSuccess: () -> Unit) {
        showTruncationDialog = false
        isSharing = true
        
        scope.launch {
            val localQuote = LocalQuote(
                text = quoteText,
                bookId = params.bookId,
                bookTitle = params.bookTitle,
                chapterTitle = params.chapterTitle,
                chapterNumber = params.chapterNumber,
                author = params.author
            )
            
            val request = localQuoteUseCases.toSubmitRequest(localQuote, truncate = truncate)
            
            val result = quoteRepository.submitQuote(request)
            
            isSharing = false
            
            result.fold(
                onSuccess = {
                    showSnackBar(UiText.DynamicString("Quote shared to community!"))
                    onSuccess()
                },
                onFailure = { error ->
                    showSnackBar(UiText.DynamicString("Failed to share: ${error.message}"))
                }
            )
        }
    }
    
    /**
     * Get character count info
     */
    val characterCount: Int get() = quoteText.length
    val maxCommunityLength: Int = 1000
    val minCommunityLength: Int = 10
}
