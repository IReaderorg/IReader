package ireader.domain.services.processstate

import ireader.core.prefs.PreferenceStore
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages process state for handling Android process death.
 * 
 * When Android kills the app process in the background, all in-memory state is lost.
 * This manager persists critical screen state to preferences so it can be restored
 * when the user returns to the app.
 * 
 * Screens that need process death handling:
 * - ReaderScreen: bookId, chapterId, scrollPosition, readingParagraph
 * - TTSScreen: bookId, chapterId, sourceId, readingParagraph, isPlaying
 * - BookDetailScreen: bookId, scrollPosition
 */
class ProcessStateManager(
    private val preferenceStore: PreferenceStore
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    // Preference keys
    private val readerStatePref = preferenceStore.getString("process_state_reader", "")
    private val ttsStatePref = preferenceStore.getString("process_state_tts", "")
    private val bookDetailStatePref = preferenceStore.getString("process_state_book_detail", "")
    private val lastActiveScreenPref = preferenceStore.getString("process_state_last_screen", "")
    
    // In-memory cache for quick access
    private val _readerState = MutableStateFlow<ReaderProcessState?>(null)
    val readerState = _readerState.asStateFlow()
    
    private val _ttsState = MutableStateFlow<TTSProcessState?>(null)
    val ttsState = _ttsState.asStateFlow()
    
    private val _bookDetailState = MutableStateFlow<BookDetailProcessState?>(null)
    val bookDetailState = _bookDetailState.asStateFlow()
    
    init {
        // Load cached state on initialization
        loadCachedStates()
    }
    
    private fun loadCachedStates() {
        try {
            val readerJson = readerStatePref.get()
            if (readerJson.isNotBlank()) {
                _readerState.value = json.decodeFromString<ReaderProcessState>(readerJson)
            }
            
            val ttsJson = ttsStatePref.get()
            if (ttsJson.isNotBlank()) {
                _ttsState.value = json.decodeFromString<TTSProcessState>(ttsJson)
            }
            
            val bookDetailJson = bookDetailStatePref.get()
            if (bookDetailJson.isNotBlank()) {
                _bookDetailState.value = json.decodeFromString<BookDetailProcessState>(bookDetailJson)
            }
        } catch (e: Exception) {
            // Clear corrupted state
            clearAllState()
        }
    }
    
    // ==================== Reader State ====================
    
    fun saveReaderState(state: ReaderProcessState) {
        _readerState.value = state
        readerStatePref.set(json.encodeToString(state))
        lastActiveScreenPref.set(SCREEN_READER)
    }
    
    fun getReaderState(): ReaderProcessState? = _readerState.value
    
    fun clearReaderState() {
        _readerState.value = null
        readerStatePref.set("")
    }
    
    // ==================== TTS State ====================
    
    fun saveTTSState(state: TTSProcessState) {
        _ttsState.value = state
        ttsStatePref.set(json.encodeToString(state))
        lastActiveScreenPref.set(SCREEN_TTS)
    }
    
    fun getTTSState(): TTSProcessState? = _ttsState.value
    
    fun clearTTSState() {
        _ttsState.value = null
        ttsStatePref.set("")
    }
    
    // ==================== Book Detail State ====================
    
    fun saveBookDetailState(state: BookDetailProcessState) {
        _bookDetailState.value = state
        bookDetailStatePref.set(json.encodeToString(state))
        lastActiveScreenPref.set(SCREEN_BOOK_DETAIL)
    }
    
    fun getBookDetailState(): BookDetailProcessState? = _bookDetailState.value
    
    fun clearBookDetailState() {
        _bookDetailState.value = null
        bookDetailStatePref.set("")
    }
    
    // ==================== Utility ====================
    
    fun getLastActiveScreen(): String = lastActiveScreenPref.get()
    
    fun clearAllState() {
        _readerState.value = null
        _ttsState.value = null
        _bookDetailState.value = null
        readerStatePref.set("")
        ttsStatePref.set("")
        bookDetailStatePref.set("")
        lastActiveScreenPref.set("")
    }
    
    /**
     * Check if there's restorable state for a specific screen.
     * Used to determine if we should restore state after process death.
     */
    fun hasRestorableState(screen: String): Boolean {
        return when (screen) {
            SCREEN_READER -> _readerState.value != null
            SCREEN_TTS -> _ttsState.value != null
            SCREEN_BOOK_DETAIL -> _bookDetailState.value != null
            else -> false
        }
    }
    
    companion object {
        const val SCREEN_READER = "reader"
        const val SCREEN_TTS = "tts"
        const val SCREEN_BOOK_DETAIL = "book_detail"
    }
}

/**
 * State to persist for Reader screen across process death.
 */
@Serializable
data class ReaderProcessState(
    val bookId: Long,
    val chapterId: Long,
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0,
    val readingParagraph: Int = 0,
    val isReaderModeEnabled: Boolean = true,
    val timestamp: Long = 0L
)

/**
 * State to persist for TTS screen across process death.
 */
@Serializable
data class TTSProcessState(
    val bookId: Long,
    val chapterId: Long,
    val sourceId: Long,
    val readingParagraph: Int = 0,
    val wasPlaying: Boolean = false,
    val timestamp: Long = 0L
)

/**
 * State to persist for Book Detail screen across process death.
 */
@Serializable
data class BookDetailProcessState(
    val bookId: Long,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0,
    val timestamp: Long = 0L
)
