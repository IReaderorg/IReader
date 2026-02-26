package ireader.presentation.ui.settings.textreplacement

import androidx.compose.runtime.Immutable
import ireader.core.log.Log
import ireader.domain.models.entities.TextReplacement
import ireader.domain.usecases.reader.TextReplacementUseCase
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * ViewModel for Text Replacement management screen.
 * 
 * Manages CRUD operations for text replacement rules.
 */
class TextReplacementViewModel(
    private val textReplacementUseCase: TextReplacementUseCase
) : BaseViewModel() {
    
    private val _state = MutableStateFlow<TextReplacementState>(TextReplacementState.Loading)
    val state: StateFlow<TextReplacementState> = _state.asStateFlow()
    
    init {
        loadReplacements()
        initializeDefaultReplacements()
    }
    
    /**
     * Initialize default text replacements (migrated from content filters)
     */
    private fun initializeDefaultReplacements() {
        scope.launch {
            try {
                // Check if defaults already exist
                val existing = textReplacementUseCase.getGlobalReplacements()
                    ?.catch { }
                    ?.firstOrNull() ?: emptyList()
                
                // Only add defaults if no replacements exist yet
                if (existing.isEmpty()) {
                    // Default patterns from content filters
                    val defaults = listOf(
                        Triple("Navigation Hint 1", "Use arrow keys.*chapter", ""),
                        Triple("Navigation Hint 2", "(?:A|D|←|→).*(?:PREV|NEXT).*chapter", ""),
                        Triple("Navigation Hint 3", "(?:Previous|Next).*Chapter.*(?:←|→|A|D)", ""),
                        Triple("Promotion 1", "Read more at.*", ""),
                        Triple("Promotion 2", "Visit.*for more chapters", "")
                    )
                    
                    defaults.forEachIndexed { index, (name, find, replace) ->
                        textReplacementUseCase.addReplacementWithId(
                            id = -(index + 1).toLong(), // Negative IDs for defaults
                            name = name,
                            findText = find,
                            replaceText = replace,
                            description = "Default filter (removes unwanted text)",
                            bookId = null,
                            enabled = true,
                            caseSensitive = false
                        )
                    }
                    
                    loadReplacements()
                }
            } catch (e: Exception) {
                Log.error(e, "Failed to initialize default replacements")
            }
        }
    }
    
    /**
     * Load all global replacements
     */
    fun loadReplacements() {
        scope.launch {
            _state.value = TextReplacementState.Loading
            
            try {
                textReplacementUseCase.getGlobalReplacements()
                    ?.catch { e ->
                        Log.error(e, "Failed to load replacements")
                        _state.value = TextReplacementState.Error(
                            e.message ?: "Failed to load replacements"
                        )
                    }
                    ?.collect { replacements ->
                        _state.value = TextReplacementState.Success(replacements)
                    }
                    ?: run {
                        _state.value = TextReplacementState.Error("Text replacement repository not available")
                    }
            } catch (e: Exception) {
                Log.error(e, "Failed to load replacements")
                _state.value = TextReplacementState.Error(
                    e.message ?: "Failed to load replacements"
                )
            }
        }
    }
    
    /**
     * Add a new replacement
     */
    fun addReplacement(
        name: String,
        findText: String,
        replaceText: String,
        description: String? = null,
        bookId: Long? = null,
        enabled: Boolean = true,
        caseSensitive: Boolean = false
    ) {
        scope.launch {
            try {
                // Validate inputs
                if (name.isBlank()) {
                    _state.value = TextReplacementState.Error("Replacement name cannot be empty")
                    return@launch
                }
                
                if (findText.isBlank()) {
                    _state.value = TextReplacementState.Error("Find text cannot be empty")
                    return@launch
                }
                
                // Validate regex pattern if it looks like regex
                val regexMetaChars = setOf('.', '*', '+', '?', '^', '$', '{', '}', '(', ')', '|', '[', ']', '\\')
                val isRegex = findText.any { it in regexMetaChars }
                if (isRegex) {
                    try {
                        Regex(findText) // Test if valid regex
                    } catch (e: Exception) {
                        _state.value = TextReplacementState.Error("Invalid regex pattern: ${e.message}")
                        return@launch
                    }
                }
                
                textReplacementUseCase.addReplacement(
                    name = name,
                    findText = findText,
                    replaceText = replaceText,
                    description = description,
                    bookId = bookId,
                    enabled = enabled,
                    caseSensitive = caseSensitive
                )
                
                // Reload replacements after adding
                loadReplacements()
            } catch (e: Exception) {
                Log.error(e, "Failed to add replacement")
                _state.value = TextReplacementState.Error(
                    e.message ?: "Failed to add replacement"
                )
            }
        }
    }
    
    /**
     * Update an existing replacement
     */
    fun updateReplacement(replacement: TextReplacement) {
        scope.launch {
            try {
                // Validate inputs
                if (replacement.findText.isBlank()) {
                    _state.value = TextReplacementState.Error("Find text cannot be empty")
                    return@launch
                }
                
                textReplacementUseCase.updateReplacement(replacement)
                
                // Reload replacements after updating
                loadReplacements()
            } catch (e: Exception) {
                Log.error(e, "Failed to update replacement")
                _state.value = TextReplacementState.Error(
                    e.message ?: "Failed to update replacement"
                )
            }
        }
    }
    
    /**
     * Toggle a replacement's enabled state
     */
    fun toggleReplacement(id: Long) {
        scope.launch {
            try {
                textReplacementUseCase.toggleReplacement(id)
                
                // Reload replacements after toggling
                loadReplacements()
            } catch (e: Exception) {
                Log.error(e, "Failed to toggle replacement")
                _state.value = TextReplacementState.Error(
                    e.message ?: "Failed to toggle replacement"
                )
            }
        }
    }
    
    /**
     * Delete a replacement
     */
    fun deleteReplacement(id: Long) {
        scope.launch {
            try {
                textReplacementUseCase.deleteReplacement(id)
                
                // Reload replacements after deleting
                loadReplacements()
            } catch (e: Exception) {
                Log.error(e, "Failed to delete replacement")
                _state.value = TextReplacementState.Error(
                    e.message ?: "Failed to delete replacement"
                )
            }
        }
    }
    
    /**
     * Test a replacement against sample text
     */
    fun testReplacement(text: String, findText: String, replaceText: String, caseSensitive: Boolean = false): String {
        return textReplacementUseCase.testReplacement(text, findText, replaceText, caseSensitive)
    }
    
    /**
     * Export all text replacements to JSON
     */
    fun exportToJson(onSuccess: (String) -> Unit) {
        scope.launch {
            try {
                val json = textReplacementUseCase.exportToJson()
                onSuccess(json)
            } catch (e: Exception) {
                Log.error(e, "Failed to export text replacements")
                _state.value = TextReplacementState.Error(
                    e.message ?: "Failed to export text replacements"
                )
            }
        }
    }
    
    /**
     * Import text replacements from JSON
     */
    fun importFromJson(jsonString: String) {
        scope.launch {
            try {
                val result = textReplacementUseCase.importFromJson(jsonString)
                result.fold(
                    onSuccess = { count ->
                        Log.info { "Imported $count text replacements" }
                        loadReplacements()
                    },
                    onFailure = { e ->
                        Log.error(e, "Failed to import text replacements")
                        _state.value = TextReplacementState.Error(
                            e.message ?: "Failed to import text replacements"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.error(e, "Failed to import text replacements")
                _state.value = TextReplacementState.Error(
                    e.message ?: "Failed to import text replacements"
                )
            }
        }
    }
}

/**
 * State for Text Replacement screen
 */
@Immutable
sealed interface TextReplacementState {
    @Immutable
    data object Loading : TextReplacementState
    
    @Immutable
    data class Success(
        val replacements: List<TextReplacement>
    ) : TextReplacementState
    
    @Immutable
    data class Error(
        val message: String
    ) : TextReplacementState
}
