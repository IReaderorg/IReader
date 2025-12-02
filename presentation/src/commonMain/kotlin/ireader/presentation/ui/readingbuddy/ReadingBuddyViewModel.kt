package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Stable
import ireader.domain.data.repository.QuoteRepository
import ireader.domain.models.quote.*
import ireader.domain.preferences.prefs.ReadingBuddyPreferences
import ireader.domain.usecases.quote.ReadingBuddyUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State for the Reading Buddy & Quotes screen
 */
@Stable
data class ReadingBuddyScreenState(
    val buddyState: ReadingBuddyState = ReadingBuddyState(),
    val dailyQuote: Quote? = null,
    val approvedQuotes: List<Quote> = emptyList(),
    val userQuotes: List<Quote> = emptyList(),
    val pendingQuotes: List<Quote> = emptyList(), // Admin only
    val selectedCardStyle: QuoteCardStyle = QuoteCardStyle.GRADIENT_SUNSET,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val unlockedAchievements: List<BuddyAchievement> = emptyList(),
    val levelProgress: Float = 0f,
    val showAchievementDialog: Boolean = false,
    val newAchievement: BuddyAchievement? = null,
    val isAdmin: Boolean = false
)

/**
 * ViewModel for Reading Buddy and Daily Quotes feature
 */
@Stable
class ReadingBuddyViewModel(
    private val quoteRepository: QuoteRepository,
    private val readingBuddyUseCases: ReadingBuddyUseCases,
    private val preferences: ReadingBuddyPreferences,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(ReadingBuddyScreenState())
    val state: StateFlow<ReadingBuddyScreenState> = _state.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Load buddy state
            val buddyState = readingBuddyUseCases.getBuddyState()
            val achievements = readingBuddyUseCases.getUnlockedAchievements()
            val levelProgress = readingBuddyUseCases.getLevelProgress()
            
            // Check if user is admin
            val isAdmin = try {
                getCurrentUser()?.isAdmin == true
            } catch (e: Exception) {
                false
            }
            
            _state.update { current ->
                current.copy(
                    buddyState = buddyState,
                    unlockedAchievements = achievements,
                    levelProgress = levelProgress,
                    isAdmin = isAdmin
                )
            }
            
            // Load quotes
            loadDailyQuote()
            loadApprovedQuotes()
            
            if (isAdmin) {
                loadPendingQuotes()
            }
            
            _state.update { it.copy(isLoading = false) }
        }
    }
    
    fun loadDailyQuote() {
        scope.launch {
            quoteRepository.getDailyQuote()
                .onSuccess { quote ->
                    _state.update { it.copy(dailyQuote = quote) }
                }
                .onFailure { error ->
                    // Silent fail for daily quote
                }
        }
    }
    
    fun loadApprovedQuotes() {
        scope.launch {
            quoteRepository.getApprovedQuotes()
                .onSuccess { quotes ->
                    _state.update { it.copy(approvedQuotes = quotes) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = "Failed to load quotes: ${error.message}") }
                }
        }
    }
    
    fun loadPendingQuotes() {
        scope.launch {
            quoteRepository.getPendingQuotes()
                .onSuccess { quotes ->
                    _state.update { it.copy(pendingQuotes = quotes) }
                }
                .onFailure { error ->
                    // Silent fail for admin feature
                }
        }
    }
    
    fun submitQuote(quoteText: String, bookTitle: String, author: String, chapterTitle: String) {
        scope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            
            val request = SubmitQuoteRequest(
                quoteText = quoteText.trim(),
                bookTitle = bookTitle.trim(),
                author = author.trim(),
                chapterTitle = chapterTitle.trim()
            )
            
            quoteRepository.submitQuote(request)
                .onSuccess { quote ->
                    _state.update { current ->
                        current.copy(
                            isSubmitting = false,
                            successMessage = "Quote submitted for review! 📚",
                            userQuotes = current.userQuotes + quote
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to submit quote"
                        )
                    }
                }
        }
    }
    
    fun toggleLike(quoteId: String) {
        scope.launch {
            quoteRepository.toggleLike(quoteId)
                .onSuccess { isLiked ->
                    // Update the quote in the list
                    _state.update { current ->
                        val updatedQuotes = current.approvedQuotes.map { quote ->
                            if (quote.id == quoteId) {
                                quote.copy(
                                    isLikedByUser = isLiked,
                                    likesCount = if (isLiked) quote.likesCount + 1 else quote.likesCount - 1
                                )
                            } else quote
                        }
                        current.copy(approvedQuotes = updatedQuotes)
                    }
                }
        }
    }
    
    fun approveQuote(quoteId: String, featured: Boolean = false) {
        scope.launch {
            quoteRepository.approveQuote(quoteId, featured)
                .onSuccess {
                    _state.update { current ->
                        current.copy(
                            pendingQuotes = current.pendingQuotes.filter { it.id != quoteId },
                            successMessage = "Quote approved! ✅"
                        )
                    }
                    loadApprovedQuotes()
                }
                .onFailure { error ->
                    _state.update { it.copy(error = "Failed to approve: ${error.message}") }
                }
        }
    }
    
    fun rejectQuote(quoteId: String) {
        scope.launch {
            quoteRepository.rejectQuote(quoteId)
                .onSuccess {
                    _state.update { current ->
                        current.copy(
                            pendingQuotes = current.pendingQuotes.filter { it.id != quoteId },
                            successMessage = "Quote rejected"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = "Failed to reject: ${error.message}") }
                }
        }
    }
    
    fun setCardStyle(style: QuoteCardStyle) {
        _state.update { it.copy(selectedCardStyle = style) }
        scope.launch {
            preferences.preferredCardStyle().set(style.name)
        }
    }
    
    fun onReadingStarted() {
        scope.launch {
            readingBuddyUseCases.onReadingStarted()
            refreshBuddyState()
        }
    }
    
    fun onChapterCompleted() {
        scope.launch {
            val achievement = readingBuddyUseCases.onChapterCompleted()
            if (achievement != null) {
                _state.update { 
                    it.copy(
                        showAchievementDialog = true,
                        newAchievement = achievement
                    )
                }
            }
            refreshBuddyState()
        }
    }
    
    fun onBookCompleted() {
        scope.launch {
            val achievement = readingBuddyUseCases.onBookCompleted()
            if (achievement != null) {
                _state.update { 
                    it.copy(
                        showAchievementDialog = true,
                        newAchievement = achievement
                    )
                }
            }
            refreshBuddyState()
        }
    }
    
    fun dismissAchievementDialog() {
        _state.update { it.copy(showAchievementDialog = false, newAchievement = null) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
    
    fun showMessage(message: String) {
        _state.update { it.copy(successMessage = message) }
    }
    
    fun showError(error: String) {
        _state.update { it.copy(error = error) }
    }
    
    private fun refreshBuddyState() {
        val buddyState = readingBuddyUseCases.getBuddyState()
        val achievements = readingBuddyUseCases.getUnlockedAchievements()
        val levelProgress = readingBuddyUseCases.getLevelProgress()
        
        _state.update { current ->
            current.copy(
                buddyState = buddyState,
                unlockedAchievements = achievements,
                levelProgress = levelProgress
            )
        }
    }
}
