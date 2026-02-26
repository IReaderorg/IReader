package ireader.presentation.ui.quote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.DiscordQuoteRepository
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.models.quote.QuoteContext
import ireader.domain.models.quote.QuoteCardConstants
import ireader.domain.usecases.quote.LocalQuoteUseCases
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Pending share data for confirmation dialog
 */
data class PendingShare(
    val text: String,
    val bookTitle: String,
    val author: String,
    val style: QuoteCardStyle
)

/**
 * ViewModel for My Quotes tab in Reading Buddy
 */
class MyQuotesViewModel(
    private val localQuoteUseCases: LocalQuoteUseCases,
    private val discordQuoteRepository: DiscordQuoteRepository,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?,
    private val uiPreferences: ireader.domain.preferences.prefs.UiPreferences
) : BaseViewModel() {
    
    private val _quotes = MutableStateFlow<List<LocalQuote>>(emptyList())
    val quotes: StateFlow<List<LocalQuote>> = _quotes.asStateFlow()
    
    var isLoading by mutableStateOf(true)
        private set
    
    var searchQuery by mutableStateOf("")
        private set
    
    var selectedFilter by mutableStateOf("All")
        private set
    
    var selectedQuote by mutableStateOf<LocalQuote?>(null)
        private set
    
    var quoteContext by mutableStateOf<List<QuoteContext>>(emptyList())
        private set
    
    var showDeleteDialog by mutableStateOf(false)
        private set
    
    var quoteToDelete by mutableStateOf<LocalQuote?>(null)
        private set
    
    var showShareDialog by mutableStateOf(false)
        private set
    
    var quoteToShare by mutableStateOf<LocalQuote?>(null)
        private set
    
    var isSharing by mutableStateOf(false)
        private set
    
    var shareValidation by mutableStateOf<ireader.domain.models.quote.ShareValidation?>(null)
        private set
    
    var showCreateDialog by mutableStateOf(false)
        private set
    
    var isCreating by mutableStateOf(false)
        private set
    
    var showShareConfirmDialog by mutableStateOf(false)
        private set
    
    var pendingShareData by mutableStateOf<PendingShare?>(null)
        private set
    
    // Rate limiting: track last share timestamp
    private var lastShareTimestamp: Long = 0L
    private val shareRateLimitMs: Long = QuoteCardConstants.SHARE_RATE_LIMIT_MS
    
    // Preferred quote style
    private val _preferredQuoteStyle = MutableStateFlow(QuoteCardStyle.GRADIENT_SUNSET)
    val preferredQuoteStyle: StateFlow<QuoteCardStyle> = _preferredQuoteStyle.asStateFlow()
    
    init {
        observeQuotes()
        loadPreferredStyle()
    }
    
    private fun loadPreferredStyle() {
        scope.launch {
            val styleName = uiPreferences.preferredQuoteStyle().get()
            _preferredQuoteStyle.value = try {
                QuoteCardStyle.valueOf(styleName)
            } catch (e: Exception) {
                QuoteCardStyle.GRADIENT_SUNSET
            }
        }
    }
    
    fun savePreferredStyle(style: QuoteCardStyle) {
        scope.launch {
            uiPreferences.preferredQuoteStyle().set(style.name)
            _preferredQuoteStyle.value = style
        }
    }
    
    private fun observeQuotes() {
        scope.launch {
            localQuoteUseCases.observeQuotes().collect { quoteList ->
                // Sort by createdAt descending to ensure newest quotes appear first
                val sorted = quoteList.sortedByDescending { it.createdAt }
                _quotes.value = applyFilter(sorted)
                isLoading = false
            }
        }
    }
    
    /**
     * Apply current filter to quotes
     */
    private fun applyFilter(quotes: List<LocalQuote>): List<LocalQuote> {
        return when (selectedFilter) {
            "With Context" -> quotes.filter { it.hasContextBackup }
            "Recent" -> quotes.take(10)
            else -> quotes
        }
    }
    
    /**
     * Set filter and refresh quotes
     */
    fun setFilter(filter: String) {
        selectedFilter = filter
        observeQuotes()
    }
    
    /**
     * Update search query and filter quotes
     */
    fun updateSearchQuery(query: String) {
        searchQuery = query
        if (query.isBlank()) {
            observeQuotes()
        } else {
            scope.launch {
                isLoading = true
                val results = localQuoteUseCases.searchQuotes(query)
                // Sort by createdAt descending to ensure newest quotes appear first
                _quotes.value = results.sortedByDescending { it.createdAt }
                isLoading = false
            }
        }
    }
    
    /**
     * Select a quote to view details
     */
    fun selectQuote(quote: LocalQuote) {
        selectedQuote = quote
        if (quote.hasContextBackup) {
            loadQuoteContext(quote.id)
        } else {
            quoteContext = emptyList()
        }
    }
    
    /**
     * Clear selected quote
     */
    fun clearSelectedQuote() {
        selectedQuote = null
        quoteContext = emptyList()
    }
    
    /**
     * Load context for a quote
     */
    private fun loadQuoteContext(quoteId: Long) {
        scope.launch {
            val result = localQuoteUseCases.getQuoteWithContext(quoteId)
            quoteContext = result?.second ?: emptyList()
        }
    }
    
    /**
     * Show delete confirmation dialog
     */
    fun showDeleteConfirmation(quote: LocalQuote) {
        quoteToDelete = quote
        showDeleteDialog = true
    }
    
    /**
     * Dismiss delete dialog
     */
    fun dismissDeleteDialog() {
        showDeleteDialog = false
        quoteToDelete = null
    }
    
    /**
     * Delete a quote
     */
    fun deleteQuote() {
        val quote = quoteToDelete ?: return
        scope.launch {
            localQuoteUseCases.deleteQuote(quote.id)
            showSnackBar(UiText.DynamicString("Quote deleted"))
            dismissDeleteDialog()
            if (selectedQuote?.id == quote.id) {
                clearSelectedQuote()
            }
        }
    }
    
    /**
     * Show share dialog
     */
    fun showShareConfirmation(quote: LocalQuote) {
        quoteToShare = quote
        // Calculate share validation (Discord has no length limit, but we validate for UX)
        shareValidation = ireader.domain.models.quote.ShareValidation(
            canShare = quote.text.length >= QuoteCardConstants.MIN_QUOTE_LENGTH,
            currentLength = quote.text.length,
            maxLength = Int.MAX_VALUE, // Discord has no practical limit
            minLength = 10,
            reason = if (quote.text.length < 10) "Quote too short" else null
        )
        showShareDialog = true
    }
    
    /**
     * Dismiss share dialog
     */
    fun dismissShareDialog() {
        showShareDialog = false
        quoteToShare = null
    }
    
    /**
     * Share quote to Discord
     */
    fun shareQuoteToDiscord(style: QuoteCardStyle, username: String) {
        val quote = quoteToShare ?: return
        isSharing = true
        
        scope.launch {
            // Get username from Supabase if available, otherwise use provided username
            val finalUsername = try {
                val user = getCurrentUser()
                user?.username?.takeIf { it.isNotBlank() } ?: username.ifBlank { "Anonymous" }
            } catch (e: Exception) {
                username.ifBlank { "Anonymous" }
            }
            
            val result = discordQuoteRepository.submitQuote(
                quote = quote,
                style = style,
                username = finalUsername
            )
            
            isSharing = false
            
            result.fold(
                onSuccess = {
                    showSnackBar(UiText.DynamicString("Quote shared to Discord! 🎉"))
                    dismissShareDialog()
                },
                onFailure = { error ->
                    showSnackBar(UiText.DynamicString("Failed to share: ${error.message}"))
                }
            )
        }
    }
    
    /**
     * Get quote count
     */
    val quoteCount: Int get() = _quotes.value.size
    
    /**
     * Show create quote dialog
     */
    fun showCreateDialog() {
        showCreateDialog = true
    }
    
    /**
     * Dismiss create quote dialog
     */
    fun dismissCreateDialog() {
        showCreateDialog = false
    }
    
    /**
     * Create a new quote manually (without book context)
     */
    fun createQuote(text: String, bookTitle: String, author: String) {
        if (text.length < 10) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Quote must be at least 10 characters"))
            }
            return
        }
        
        if (bookTitle.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Book title is required"))
            }
            return
        }
        
        isCreating = true
        scope.launch {
            val result = localQuoteUseCases.saveQuote(
                text = text,
                bookId = 0L, // No book context
                bookTitle = bookTitle,
                chapterTitle = "Manual Entry",
                chapterNumber = null,
                author = author.ifBlank { null },
                includeContext = false,
                currentChapterId = null,
                prevChapterId = null,
                nextChapterId = null
            )
            
            isCreating = false
            
            result.fold(
                onSuccess = {
                    showSnackBar(UiText.DynamicString("Quote created! 📚"))
                    dismissCreateDialog()
                },
                onFailure = { error ->
                    showSnackBar(UiText.DynamicString("Failed to create: ${error.message}"))
                }
            )
        }
    }
    
    /**
     * Show share confirmation dialog (with rate limit check)
     */
    fun showShareConfirmation(text: String, bookTitle: String, author: String, style: QuoteCardStyle) {
        if (text.length < 10) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Quote must be at least 10 characters"))
            }
            return
        }
        
        if (bookTitle.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Book title is required"))
            }
            return
        }
        
        // Check rate limit
        val currentTime = System.currentTimeMillis()
        val timeSinceLastShare = currentTime - lastShareTimestamp
        
        if (timeSinceLastShare < shareRateLimitMs) {
            val remainingSeconds = ((shareRateLimitMs - timeSinceLastShare) / 1000).toInt()
            scope.launch {
                showSnackBar(UiText.DynamicString("Please wait $remainingSeconds seconds before sharing again"))
            }
            return
        }
        
        // Show confirmation dialog
        pendingShareData = PendingShare(text, bookTitle, author, style)
        showShareConfirmDialog = true
    }
    
    /**
     * Dismiss share confirmation dialog
     */
    fun dismissShareConfirmation() {
        showShareConfirmDialog = false
        pendingShareData = null
    }
    
    /**
     * Confirm and execute share to Discord
     */
    fun confirmShare() {
        val shareData = pendingShareData ?: return
        dismissShareConfirmation()
        
        isSharing = true
        
        scope.launch {
            // Create temporary quote for sharing
            val tempQuote = LocalQuote(
                id = 0L,
                text = shareData.text,
                bookId = 0L,
                bookTitle = shareData.bookTitle,
                chapterTitle = "Manual Entry",
                chapterNumber = null,
                author = shareData.author.ifBlank { null },
                createdAt = ireader.domain.utils.extensions.currentTimeToLong(),
                hasContextBackup = false
            )
            
            // Get username from Supabase if available
            val username = try {
                val user = getCurrentUser()
                user?.username?.takeIf { it.isNotBlank() } ?: "Anonymous"
            } catch (e: Exception) {
                "Anonymous"
            }
            
            val result = discordQuoteRepository.submitQuote(
                quote = tempQuote,
                style = shareData.style,
                username = username
            )
            
            isSharing = false
            
            result.fold(
                onSuccess = {
                    // Update last share timestamp
                    lastShareTimestamp = System.currentTimeMillis()
                    showSnackBar(UiText.DynamicString("Quote shared to Discord! 🎉"))
                    dismissCreateDialog()
                },
                onFailure = { error ->
                    showSnackBar(UiText.DynamicString("Failed to share: ${error.message}"))
                }
            )
        }
    }
    
    /**
     * Share quote directly from editor (DEPRECATED - use showShareConfirmation instead)
     */
    fun shareQuoteDirectly(text: String, bookTitle: String, author: String, style: QuoteCardStyle) {
        showShareConfirmation(text, bookTitle, author, style)
    }
    
    /**
     * Update an existing quote
     */
    fun updateQuote(quoteId: Long, text: String, bookTitle: String, author: String) {
        if (text.length < 10) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Quote must be at least 10 characters"))
            }
            return
        }
        
        if (bookTitle.isBlank()) {
            scope.launch {
                showSnackBar(UiText.DynamicString("Book title is required"))
            }
            return
        }
        
        scope.launch {
            // Delete old quote and create new one with updated data
            localQuoteUseCases.deleteQuote(quoteId)
            
            val result = localQuoteUseCases.saveQuote(
                text = text,
                bookId = 0L,
                bookTitle = bookTitle,
                chapterTitle = "Manual Entry",
                chapterNumber = null,
                author = author.ifBlank { null },
                includeContext = false,
                currentChapterId = null,
                prevChapterId = null,
                nextChapterId = null
            )
            
            result.fold(
                onSuccess = {
                    showSnackBar(UiText.DynamicString("Quote updated! 📚"))
                    clearSelectedQuote()
                },
                onFailure = { error ->
                    showSnackBar(UiText.DynamicString("Failed to update: ${error.message}"))
                }
            )
        }
    }
}
