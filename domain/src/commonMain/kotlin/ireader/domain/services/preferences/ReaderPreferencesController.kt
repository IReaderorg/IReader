package ireader.domain.services.preferences

import ireader.core.log.Log
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.FontType
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.ReadingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * ReaderPreferencesController - The central coordinator for all reader preference operations.
 * 
 * This is the SINGLE SOURCE OF TRUTH for reader preference state across all screens
 * (Reader, Settings, etc.).
 * 
 * Responsibilities:
 * - Owns and manages the PreferenceState (single source of truth)
 * - Processes PreferenceCommands and updates state accordingly
 * - Persists preference changes to the preference store
 * - Emits PreferenceEvents for one-time occurrences
 * 
 * NOT responsible for:
 * - UI concerns (UI observes state, sends commands)
 * - Platform-specific implementations
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 5.1, 5.2, 5.4, 7.1
 */
class ReaderPreferencesController(
    private val readerPreferences: ReaderPreferences
) {
    companion object {
        private const val TAG = "ReaderPreferencesController"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Mutex to ensure commands are processed sequentially
    private val commandMutex = Mutex()
    
    // State - single source of truth
    private val _state = MutableStateFlow(PreferenceState())
    val state: StateFlow<PreferenceState> = _state.asStateFlow()
    
    // Events - one-time occurrences
    private val _events = MutableSharedFlow<PreferenceEvent>()
    val events: SharedFlow<PreferenceEvent> = _events.asSharedFlow()
    
    // Font registry for looking up fonts by ID
    private var fontRegistry: Map<String, FontType> = emptyMap()
    
    // Track if preferences have been loaded (lazy initialization)
    private var preferencesLoaded = false
    
    // NOTE: Removed init block that loaded preferences at startup
    // Preferences are now loaded lazily when first accessed via ensurePreferencesLoaded()
    // This improves app startup time significantly
    
    /**
     * Register available fonts for lookup by ID.
     */
    fun registerFonts(fonts: List<FontType>) {
        fontRegistry = fonts.associateBy { it.name }
    }
    
    /**
     * Ensure preferences are loaded. Call this before accessing state.
     * This enables lazy loading of preferences to improve startup time.
     */
    fun ensurePreferencesLoaded() {
        if (!preferencesLoaded) {
            preferencesLoaded = true
            scope.launch {
                loadAllPreferences()
            }
        }
    }

    
    /**
     * Process a command - ALL interactions go through here.
     * Commands are processed sequentially using a mutex to prevent race conditions.
     * Requirements: 1.4, 5.2
     */
    fun dispatch(command: PreferenceCommand) {
        Log.debug { "$TAG: dispatch($command)" }
        
        scope.launch {
            commandMutex.withLock {
                try {
                    processCommand(command)
                } catch (e: Exception) {
                    Log.error(e, "$TAG: Error processing command")
                    handleError(PreferenceError.SaveFailed("unknown", e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    private suspend fun processCommand(command: PreferenceCommand) {
        when (command) {
            // Display preferences
            is PreferenceCommand.SetFontSize -> setFontSize(command.size)
            is PreferenceCommand.SetLineHeight -> setLineHeight(command.height)
            is PreferenceCommand.SetTextAlignment -> setTextAlignment(command.alignment)
            is PreferenceCommand.SetFont -> setFont(command.fontType)
            
            // Color preferences
            is PreferenceCommand.SetBackgroundColor -> setBackgroundColor(command.color)
            is PreferenceCommand.SetTextColor -> setTextColor(command.color)
            is PreferenceCommand.SetReaderTheme -> setReaderTheme(command.themeIndex)
            
            // Layout preferences
            is PreferenceCommand.SetMargins -> setMargins(command.top, command.bottom, command.left, command.right)
            is PreferenceCommand.SetParagraphSpacing -> setParagraphSpacing(command.spacing)
            is PreferenceCommand.SetParagraphIndent -> setParagraphIndent(command.indent)
            
            // Screen preferences
            is PreferenceCommand.SetBrightness -> setBrightness(command.brightness)
            is PreferenceCommand.SetScreenAlwaysOn -> setScreenAlwaysOn(command.enabled)
            is PreferenceCommand.SetImmersiveMode -> setImmersiveMode(command.enabled)
            
            // Reading mode preferences
            is PreferenceCommand.SetReadingMode -> setReadingMode(command.mode)
            is PreferenceCommand.SetScrollMode -> setScrollMode(command.vertical)
            
            // Batch operations
            is PreferenceCommand.BatchUpdate -> processBatchUpdate(command.updates)
            
            // Lifecycle
            is PreferenceCommand.Reload -> loadAllPreferences()
        }
    }
    
    // ========== Display Preference Handlers ==========
    
    private suspend fun setFontSize(size: Int) {
        Log.debug { "$TAG: setFontSize($size)" }
        
        try {
            readerPreferences.fontSize().set(size)
            _state.update { it.copy(fontSize = size, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("fontSize"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("fontSize", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setLineHeight(height: Int) {
        Log.debug { "$TAG: setLineHeight($height)" }
        
        try {
            readerPreferences.lineHeight().set(height)
            _state.update { it.copy(lineHeight = height, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("lineHeight"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("lineHeight", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setTextAlignment(alignment: PreferenceValues.PreferenceTextAlignment) {
        Log.debug { "$TAG: setTextAlignment($alignment)" }
        
        try {
            readerPreferences.textAlign().set(alignment)
            _state.update { it.copy(textAlignment = alignment, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("textAlignment"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("textAlignment", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setFont(fontType: FontType?) {
        Log.debug { "$TAG: setFont($fontType)" }
        
        try {
            val fontId = fontType?.name ?: ""
            readerPreferences.selectedFontId().set(fontId)
            _state.update { it.copy(font = fontType, selectedFontId = fontId, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("font"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("font", e.message ?: "Unknown error"))
        }
    }

    
    // ========== Color Preference Handlers ==========
    
    private suspend fun setBackgroundColor(color: Long) {
        Log.debug { "$TAG: setBackgroundColor($color)" }
        
        try {
            // Background color is typically managed through theme, but we track it in state
            _state.update { it.copy(backgroundColor = color, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("backgroundColor"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("backgroundColor", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setTextColor(color: Long) {
        Log.debug { "$TAG: setTextColor($color)" }
        
        try {
            // Text color is typically managed through theme, but we track it in state
            _state.update { it.copy(textColor = color, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("textColor"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("textColor", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setReaderTheme(themeIndex: Int) {
        Log.debug { "$TAG: setReaderTheme($themeIndex)" }
        
        try {
            readerPreferences.readerTheme().set(themeIndex)
            _state.update { it.copy(readerTheme = themeIndex, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("readerTheme"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("readerTheme", e.message ?: "Unknown error"))
        }
    }
    
    // ========== Layout Preference Handlers ==========
    
    private suspend fun setMargins(top: Int, bottom: Int, left: Int, right: Int) {
        Log.debug { "$TAG: setMargins(top=$top, bottom=$bottom, left=$left, right=$right)" }
        
        try {
            readerPreferences.topMargin().set(top)
            readerPreferences.bottomMargin().set(bottom)
            readerPreferences.leftMargin().set(left)
            readerPreferences.rightMargin().set(right)
            
            _state.update { 
                it.copy(
                    topMargin = top,
                    bottomMargin = bottom,
                    leftMargin = left,
                    rightMargin = right,
                    error = null
                )
            }
            _events.emit(PreferenceEvent.PreferenceSaved("margins"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("margins", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setParagraphSpacing(spacing: Int) {
        Log.debug { "$TAG: setParagraphSpacing($spacing)" }
        
        try {
            readerPreferences.paragraphDistance().set(spacing)
            _state.update { it.copy(paragraphSpacing = spacing, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("paragraphSpacing"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("paragraphSpacing", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setParagraphIndent(indent: Int) {
        Log.debug { "$TAG: setParagraphIndent($indent)" }
        
        try {
            readerPreferences.paragraphIndent().set(indent)
            _state.update { it.copy(paragraphIndent = indent, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("paragraphIndent"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("paragraphIndent", e.message ?: "Unknown error"))
        }
    }
    
    // ========== Screen Preference Handlers ==========
    
    private suspend fun setBrightness(brightness: Float) {
        Log.debug { "$TAG: setBrightness($brightness)" }
        
        try {
            val clampedBrightness = brightness.coerceIn(0f, 1f)
            readerPreferences.brightness().set(clampedBrightness)
            readerPreferences.autoBrightness().set(false) // Disable auto when manually setting
            
            _state.update { 
                it.copy(
                    brightness = clampedBrightness,
                    autoBrightness = false,
                    error = null
                )
            }
            _events.emit(PreferenceEvent.PreferenceSaved("brightness"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("brightness", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setScreenAlwaysOn(enabled: Boolean) {
        Log.debug { "$TAG: setScreenAlwaysOn($enabled)" }
        
        try {
            readerPreferences.screenAlwaysOn().set(enabled)
            _state.update { it.copy(screenAlwaysOn = enabled, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("screenAlwaysOn"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("screenAlwaysOn", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setImmersiveMode(enabled: Boolean) {
        Log.debug { "$TAG: setImmersiveMode($enabled)" }
        
        try {
            readerPreferences.immersiveMode().set(enabled)
            _state.update { it.copy(immersiveMode = enabled, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("immersiveMode"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("immersiveMode", e.message ?: "Unknown error"))
        }
    }

    
    // ========== Reading Mode Preference Handlers ==========
    
    private suspend fun setReadingMode(mode: ReadingMode) {
        Log.debug { "$TAG: setReadingMode($mode)" }
        
        try {
            readerPreferences.readingMode().set(mode)
            _state.update { it.copy(readingMode = mode, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("readingMode"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("readingMode", e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun setScrollMode(vertical: Boolean) {
        Log.debug { "$TAG: setScrollMode($vertical)" }
        
        try {
            readerPreferences.scrollMode().set(vertical)
            _state.update { it.copy(verticalScrolling = vertical, error = null) }
            _events.emit(PreferenceEvent.PreferenceSaved("scrollMode"))
        } catch (e: Exception) {
            handleError(PreferenceError.SaveFailed("scrollMode", e.message ?: "Unknown error"))
        }
    }
    
    // ========== Batch Operations ==========
    
    private suspend fun processBatchUpdate(updates: List<PreferenceCommand>) {
        Log.debug { "$TAG: processBatchUpdate(${updates.size} updates)" }
        
        // Process each update without emitting individual events
        for (update in updates) {
            if (update !is PreferenceCommand.BatchUpdate) { // Prevent nested batch
                try {
                    processCommand(update)
                } catch (e: Exception) {
                    Log.error(e, "$TAG: Error in batch update")
                    // Continue with other updates even if one fails
                }
            }
        }
    }
    
    // ========== Lifecycle ==========
    
    /**
     * Load all preferences from the preference store.
     * Requirements: 1.1
     */
    private suspend fun loadAllPreferences() {
        Log.debug { "$TAG: loadAllPreferences()" }
        
        _state.update { it.copy(isLoading = true, error = null) }
        
        try {
            val fontId = readerPreferences.selectedFontId().get()
            val font = fontRegistry[fontId]
            
            _state.update {
                it.copy(
                    // Display
                    fontSize = readerPreferences.fontSize().get(),
                    lineHeight = readerPreferences.lineHeight().get(),
                    textAlignment = readerPreferences.textAlign().get(),
                    font = font,
                    selectedFontId = fontId,
                    
                    // Theme
                    readerTheme = readerPreferences.readerTheme().get(),
                    
                    // Layout
                    topMargin = readerPreferences.topMargin().get(),
                    bottomMargin = readerPreferences.bottomMargin().get(),
                    leftMargin = readerPreferences.leftMargin().get(),
                    rightMargin = readerPreferences.rightMargin().get(),
                    paragraphSpacing = readerPreferences.paragraphDistance().get(),
                    paragraphIndent = readerPreferences.paragraphIndent().get(),
                    
                    // Screen
                    brightness = readerPreferences.brightness().get(),
                    autoBrightness = readerPreferences.autoBrightness().get(),
                    screenAlwaysOn = readerPreferences.screenAlwaysOn().get(),
                    immersiveMode = readerPreferences.immersiveMode().get(),
                    
                    // Reading mode
                    readingMode = readerPreferences.readingMode().get(),
                    verticalScrolling = readerPreferences.scrollMode().get(),
                    
                    // Loading state
                    isLoading = false,
                    error = null
                )
            }
            
            _events.emit(PreferenceEvent.PreferencesLoaded)
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to load preferences")
            handleError(PreferenceError.LoadFailed(e.message ?: "Unknown error"))
        }
    }
    
    // ========== Error Handling ==========
    
    /**
     * Handle errors by updating state and emitting events.
     * Requirements: 5.4
     */
    private suspend fun handleError(error: PreferenceError) {
        Log.error { "$TAG: Error - ${error.toUserMessage()}" }
        
        _state.update { 
            it.copy(
                error = error,
                isLoading = false
            )
        }
        
        _events.emit(PreferenceEvent.Error(error))
    }
    
    /**
     * Clear the current error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    /**
     * Release all resources. Call when the controller is no longer needed.
     */
    fun release() {
        Log.debug { "$TAG: release()" }
        scope.cancel()
    }
}
