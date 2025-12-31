package ireader.presentation.ui.quote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.data.repository.QuoteRepository
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteContext
import ireader.domain.models.quote.ShareValidation
import ireader.domain.usecases.quote.LocalQuoteUseCases
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for My Quotes tab in Reading Buddy
 */
class MyQuotesViewModel(
    private val localQuoteUseCases: LocalQuoteUseCases,
    private val quoteRepository: QuoteRepository
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
    
    var shareValidation by mutableStateOf<ShareValidation?>(null)
        private set
    
    var isSharing by mutableStateOf(false)
        private set
    
    init {
        observeQuotes()
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
        shareValidation = localQuoteUseCases.validateForCommunityShare(quote)
        showShareDialog = true
    }
    
    /**
     * Dismiss share dialog
     */
    fun dismissShareDialog() {
        showShareDialog = false
        quoteToShare = null
        shareValidation = null
    }
    
    /**
     * Share quote to community
     */
    fun shareQuoteToCommunity(truncate: Boolean = false) {
        val quote = quoteToShare ?: return
        isSharing = true
        
        scope.launch {
            val request = localQuoteUseCases.toSubmitRequest(quote, truncate = truncate)
            val result = quoteRepository.submitQuote(request)
            
            isSharing = false
            
            result.fold(
                onSuccess = {
                    showSnackBar(UiText.DynamicString("Quote shared to community!"))
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
}
