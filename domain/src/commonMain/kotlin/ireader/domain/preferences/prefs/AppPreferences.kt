package ireader.domain.preferences.prefs

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.models.ReaderColors
import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.preferences.models.prefs.asColor
import ireader.domain.preferences.models.prefs.asReaderTheme
import ireader.domain.preferences.models.prefs.asVoice

class AppPreferences(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        val DEFAULT_VERSION = 1
        const val SAVED_LIBRARY_LAYOUT_KEY = "library_layout_type"
        const val SAVED_BROWSE_LAYOUT_KEY = "browser_layout_type"

        const val SORT_LIBRARY_SCREEN = "sort_library_screen"
        const val SORT_DESC_LIBRARY_SCREEN = "sort_desc_library_screen"

        /** Services **/
        const val Last_UPDATE_CHECK = "last_update_check"

        /** Setting Pref**/
        const val SAVED_DOH_KEY = "SAVED_DOH_KEY"
        const val DEFAULT_IMAGE_LOADER = "default_image_loader"
        const val SAVED_BACKGROUND_COLOR = "background_color"
        const val SAVED_FONT_PREFERENCES = "reader_font_family"
        const val DATABASE_VERSION = "database_version"
        
        /** WebView Auto-Fetch Preferences **/
        const val AUTO_FETCH_ENABLED = "auto_fetch_enabled"
        const val AUTO_FETCH_BOOK_ENABLED = "auto_fetch_book_enabled"
        const val AUTO_FETCH_CHAPTERS_ENABLED = "auto_fetch_chapters_enabled"
        const val AUTO_FETCH_CHAPTER_CONTENT_ENABLED = "auto_fetch_chapter_content_enabled"
        
        /** Piper TTS Preferences **/
        const val SELECTED_PIPER_MODEL = "selected_piper_model"
        const val DOWNLOADED_MODELS = "downloaded_models"
        
        /** TTS Performance Preferences **/
        const val MAX_CONCURRENT_TTS_PROCESSES = "max_concurrent_tts_processes"

        enum class Orientation {
            Portrait,
            Landscape,
            Unspecified
        }
    }
    fun readerTheme(): Preference<ReaderColors> {
        return preferenceStore.getLong("readerTheme", 0).asReaderTheme()
    }
    fun textColorReader(): Preference<Color> {
        return preferenceStore.getInt(ReaderPreferences.SAVED_TEXT_COLOR, Color(0xFFE9E9E9).toArgb()).asColor()
    }
     fun backgroundColorReader(): Preference<Color> {
        return preferenceStore.getInt(SAVED_BACKGROUND_COLOR, Color(0xff262626).toArgb()).asColor()
    }
     fun backgroundColorTTS(): Preference<ReaderColors> {
        return preferenceStore.getLong("background_color_tts").asReaderTheme()
    }
    fun libraryLayoutType(): Preference<Long> {
        return preferenceStore.getLong(SAVED_LIBRARY_LAYOUT_KEY, 0)
    }
    fun unselectedScrollBarColor(): Preference<Color> {
        return preferenceStore.getInt(ReaderPreferences.SCROLL_INDICATOR_SELECTED_COLOR, Color(0xFF2A59B6).toArgb())
                .asColor()
    }
    fun selectedScrollBarColor(): Preference<Color> {
        return preferenceStore.getInt(ReaderPreferences.SCROLL_INDICATOR_UNSELECTED_COLOR, Color(0xFF5281CA).toArgb())
                .asColor()
    }
    fun orientation(): Preference<Int> {
        return preferenceStore.getInt(
                ReaderPreferences.SAVED_ORIENTATION,
                Orientation.Unspecified.ordinal
        )
    }

    fun exploreLayoutType(): Preference<Long> {
        return preferenceStore.getLong(SAVED_BROWSE_LAYOUT_KEY, 0)
    }
    fun speechVoice(): Preference<IReaderVoice> {
        return preferenceStore.getString(ReaderPreferences.TEXT_READER_SPEECH_VOICE, "").asVoice()
    }

    fun dohStateKey(): Preference<Int> {
        return preferenceStore.getInt(SAVED_DOH_KEY, 0)
    }

    fun appUpdater(): Preference<Boolean> {
        return preferenceStore.getBoolean("app_updater", true)
    }
    fun database_version(): Preference<Int> {
        return preferenceStore.getInt(DATABASE_VERSION, DEFAULT_VERSION)
    }

    fun sortLibraryScreen(): Preference<String> {
        return preferenceStore.getString(SORT_LIBRARY_SCREEN, LibrarySort.Type.LastRead.name)
    }

    fun sortDescLibraryScreen(): Preference<Boolean> {
        return preferenceStore.getBoolean(SORT_DESC_LIBRARY_SCREEN, true)
    }

    fun lastUpdateCheck(): Preference<Long> {
        return preferenceStore.getLong(Last_UPDATE_CHECK, 0)
    }
    
    /**
     * Master toggle for auto-fetch functionality
     */
    fun autoFetchEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_FETCH_ENABLED, false)
    }
    
    /**
     * Enable auto-fetch for book details
     */
    fun autoFetchBookEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_FETCH_BOOK_ENABLED, true)
    }
    
    /**
     * Enable auto-fetch for chapter lists
     */
    fun autoFetchChaptersEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_FETCH_CHAPTERS_ENABLED, true)
    }
    
    /**
     * Enable auto-fetch for chapter content
     */
    fun autoFetchChapterContentEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean(AUTO_FETCH_CHAPTER_CONTENT_ENABLED, false)
    }
    
    /**
     * Last time a donation prompt was shown (in milliseconds)
     * Used to enforce 30-day cooldown between prompts
     */
    fun lastDonationPromptTime(): Preference<Long> {
        return preferenceStore.getLong("last_donation_prompt_time", 0L)
    }
    
    /**
     * Whether user has completed their first source migration
     * Used to trigger first migration donation prompt
     */
    fun hasCompletedMigration(): Preference<Boolean> {
        return preferenceStore.getBoolean("has_completed_migration", false)
    }
    
    /**
     * Last chapter milestone for which donation prompt was shown
     * Used to avoid showing the same milestone prompt multiple times
     */
    fun lastDonationMilestone(): Preference<Int> {
        return preferenceStore.getInt("last_donation_milestone", 0)
    }
    
    /**
     * Selected Piper TTS voice model ID
     */
    fun selectedPiperModel(): Preference<String> {
        return preferenceStore.getString(SELECTED_PIPER_MODEL, "")
    }
    
    /**
     * List of downloaded Piper voice model IDs (comma-separated)
     */
    fun downloadedModels(): Preference<Set<String>> {
        return preferenceStore.getStringSet(DOWNLOADED_MODELS, emptySet())
    }
    
    /**
     * Maximum number of concurrent TTS processes (for Kokoro/Maya)
     * Default: 2, Range: 1-4
     */
    fun maxConcurrentTTSProcesses(): Preference<Int> {
        return preferenceStore.getInt(MAX_CONCURRENT_TTS_PROCESSES, 2)
    }
}
