package ireader.presentation.ui.settings.contentfilter

import androidx.compose.runtime.Immutable
import ireader.core.log.Log
import ireader.domain.models.entities.ContentFilter
import ireader.domain.usecases.reader.ContentFilterUseCase
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for Content Filter management screen.
 * 
 * Manages CRUD operations for content filter patterns.
 */
class ContentFilterViewModel(
    private val contentFilterUseCase: ContentFilterUseCase
) : BaseViewModel() {
    
    private val _state = MutableStateFlow<ContentFilterState>(ContentFilterState.Loading)
    val state: StateFlow<ContentFilterState> = _state.asStateFlow()
    
    init {
        loadPatterns()
    }
    
    /**
     * Load all global patterns
     */
    fun loadPatterns() {
        scope.launch {
            _state.value = ContentFilterState.Loading
            
            try {
                contentFilterUseCase.getGlobalPatterns()
                    ?.catch { e ->
                        Log.error(e, "Failed to load patterns")
                        _state.value = ContentFilterState.Error(
                            e.message ?: "Failed to load patterns"
                        )
                    }
                    ?.collect { patterns ->
                        _state.value = ContentFilterState.Success(patterns)
                    }
                    ?: run {
                        _state.value = ContentFilterState.Error("Content filter repository not available")
                    }
            } catch (e: Exception) {
                Log.error(e, "Failed to load patterns")
                _state.value = ContentFilterState.Error(
                    e.message ?: "Failed to load patterns"
                )
            }
        }
    }
    
    /**
     * Add a new pattern
     */
    fun addPattern(
        name: String,
        pattern: String,
        description: String? = null,
        bookId: Long? = null,
        enabled: Boolean = true
    ) {
        scope.launch {
            try {
                // Validate pattern first
                val validationError = contentFilterUseCase.validatePattern(pattern)
                if (validationError != null) {
                    _state.value = ContentFilterState.Error(validationError)
                    return@launch
                }
                
                contentFilterUseCase.addPattern(
                    name = name,
                    pattern = pattern,
                    description = description,
                    bookId = bookId,
                    enabled = enabled
                )
                
                // Reload patterns after adding
                loadPatterns()
            } catch (e: Exception) {
                Log.error(e, "Failed to add pattern")
                _state.value = ContentFilterState.Error(
                    e.message ?: "Failed to add pattern"
                )
            }
        }
    }
    
    /**
     * Update an existing pattern
     */
    fun updatePattern(filter: ContentFilter) {
        scope.launch {
            try {
                // Validate pattern first
                val validationError = contentFilterUseCase.validatePattern(filter.pattern)
                if (validationError != null) {
                    _state.value = ContentFilterState.Error(validationError)
                    return@launch
                }
                
                contentFilterUseCase.updatePattern(filter)
                
                // Reload patterns after updating
                loadPatterns()
            } catch (e: Exception) {
                Log.error(e, "Failed to update pattern")
                _state.value = ContentFilterState.Error(
                    e.message ?: "Failed to update pattern"
                )
            }
        }
    }
    
    /**
     * Toggle a pattern's enabled state
     */
    fun togglePattern(id: Long) {
        scope.launch {
            try {
                contentFilterUseCase.togglePattern(id)
                
                // Reload patterns after toggling
                loadPatterns()
            } catch (e: Exception) {
                Log.error(e, "Failed to toggle pattern")
                _state.value = ContentFilterState.Error(
                    e.message ?: "Failed to toggle pattern"
                )
            }
        }
    }
    
    /**
     * Delete a pattern
     */
    fun deletePattern(id: Long) {
        scope.launch {
            try {
                contentFilterUseCase.deletePattern(id)
                
                // Reload patterns after deleting
                loadPatterns()
            } catch (e: Exception) {
                Log.error(e, "Failed to delete pattern")
                _state.value = ContentFilterState.Error(
                    e.message ?: "Failed to delete pattern"
                )
            }
        }
    }
    
    /**
     * Test a pattern against sample text
     */
    fun testPattern(text: String, pattern: String): String {
        return contentFilterUseCase.testPatterns(text, pattern)
    }
    
    /**
     * Validate a regex pattern
     * @return null if valid, error message if invalid
     */
    fun validatePattern(pattern: String): String? {
        return contentFilterUseCase.validatePattern(pattern)
    }
    
    /**
     * Initialize preset patterns
     */
    fun initializePresets() {
        scope.launch {
            try {
                contentFilterUseCase.initializePresets()
                loadPatterns()
            } catch (e: Exception) {
                Log.error(e, "Failed to initialize presets")
            }
        }
    }
}

/**
 * State for Content Filter screen
 */
@Immutable
sealed interface ContentFilterState {
    @Immutable
    data object Loading : ContentFilterState
    
    @Immutable
    data class Success(
        val patterns: List<ContentFilter>
    ) : ContentFilterState
    
    @Immutable
    data class Error(
        val message: String
    ) : ContentFilterState
}
