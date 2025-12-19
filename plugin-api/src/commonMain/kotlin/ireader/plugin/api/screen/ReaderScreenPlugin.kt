package ireader.plugin.api.screen

import ireader.plugin.api.Plugin
import ireader.plugin.api.PluginContext
import ireader.plugin.api.source.ContentDeliveryType
import ireader.plugin.api.source.SourceChapterContent
import kotlinx.serialization.Serializable

/**
 * Plugin interface for custom reader/viewer screens.
 * 
 * Allows creating alternative reading experiences for different content types:
 * - TEXT: Custom novel readers (different layouts, fonts, themes)
 * - IMAGE: Custom manga viewers (webtoon mode, page modes, gestures)
 * - VIDEO: Custom video players (streaming, download, casting)
 * 
 * Example:
 * ```kotlin
 * class WebtoonReaderPlugin : ReaderScreenPlugin {
 *     override val manifest = PluginManifest(...)
 *     override val screenInfo = ReaderScreenInfo(
 *         name = "Webtoon Reader",
 *         targetContentTypes = setOf(ContentDeliveryType.IMAGE),
 *         description = "Vertical scrolling reader optimized for webtoons"
 *     )
 *     
 *     override fun createScreen(context: ReaderContext): ReaderScreen {
 *         return WebtoonReaderScreen(context)
 *     }
 * }
 * ```
 */
interface ReaderScreenPlugin : Plugin {
    /**
     * Information about this reader screen.
     */
    val screenInfo: ReaderScreenInfo
    
    /**
     * Create a reader screen instance.
     * Called when user selects this reader for viewing content.
     */
    fun createScreen(context: ReaderContext): ReaderScreen
    
    /**
     * Check if this reader can handle specific content.
     * Default implementation checks content type, but can be overridden
     * for more specific checks (e.g., only handle certain sources).
     */
    fun canHandle(content: SourceChapterContent): Boolean {
        return screenInfo.targetContentTypes.contains(content.type)
    }
    
    /**
     * Get reader-specific settings/preferences.
     */
    fun getSettings(): List<ReaderSetting> = emptyList()
    
    /**
     * Update a setting value.
     */
    fun updateSetting(key: String, value: Any) {}
}

/**
 * Information about a reader screen plugin.
 */
@Serializable
data class ReaderScreenInfo(
    /** Display name */
    val name: String,
    /** Description */
    val description: String,
    /** Content types this reader handles */
    val targetContentTypes: Set<ContentDeliveryType>,
    /** Icon URL */
    val iconUrl: String? = null,
    /** Preview screenshots */
    val screenshotUrls: List<String> = emptyList(),
    /** Whether this is a default reader for its content types */
    val isDefault: Boolean = false,
    /** Priority when multiple readers available (higher = preferred) */
    val priority: Int = 0,
    /** Supported features */
    val features: Set<ReaderFeature> = emptySet()
)

/**
 * Features a reader screen can support.
 */
@Serializable
enum class ReaderFeature {
    // Text reader features
    TEXT_SELECTION,
    TEXT_TO_SPEECH,
    TRANSLATION,
    DICTIONARY_LOOKUP,
    CUSTOM_FONTS,
    CUSTOM_THEMES,
    READING_PROGRESS,
    BOOKMARKS,
    ANNOTATIONS,
    
    // Image reader features
    ZOOM,
    PAN,
    PAGE_NAVIGATION,
    VERTICAL_SCROLL,
    HORIZONTAL_SCROLL,
    WEBTOON_MODE,
    DOUBLE_PAGE,
    RIGHT_TO_LEFT,
    COLOR_FILTERS,
    
    // Video player features
    STREAMING,
    DOWNLOAD,
    CASTING,
    PICTURE_IN_PICTURE,
    SUBTITLES,
    QUALITY_SELECTION,
    PLAYBACK_SPEED,
    SKIP_INTRO,
    
    // Common features
    FULLSCREEN,
    BRIGHTNESS_CONTROL,
    KEEP_SCREEN_ON,
    GESTURE_CONTROLS,
    KEYBOARD_SHORTCUTS
}

/**
 * Context provided to reader screens.
 */
interface ReaderContext {
    /** Plugin context for accessing app resources */
    val pluginContext: PluginContext
    
    /** Current chapter content */
    val content: SourceChapterContent
    
    /** Book/manga information */
    val itemInfo: ReaderItemInfo
    
    /** Chapter information */
    val chapterInfo: ReaderChapterInfo
    
    /** Navigate to previous chapter */
    suspend fun previousChapter(): SourceChapterContent?
    
    /** Navigate to next chapter */
    suspend fun nextChapter(): SourceChapterContent?
    
    /** Update reading progress */
    fun updateProgress(progress: ReaderProgress)
    
    /** Get saved reading progress */
    fun getProgress(): ReaderProgress?
    
    /** Show toast message */
    fun showToast(message: String)
    
    /** Request fullscreen mode */
    fun requestFullscreen(enabled: Boolean)
    
    /** Get screen brightness (0.0 - 1.0) */
    fun getBrightness(): Float
    
    /** Set screen brightness (0.0 - 1.0) */
    fun setBrightness(brightness: Float)
    
    /** Keep screen on */
    fun setKeepScreenOn(enabled: Boolean)
}

/**
 * Book/manga information for reader.
 */
@Serializable
data class ReaderItemInfo(
    val id: Long,
    val title: String,
    val coverUrl: String?,
    val author: String?,
    val sourceId: Long,
    val sourceName: String
)

/**
 * Chapter information for reader.
 */
@Serializable
data class ReaderChapterInfo(
    val id: Long,
    val title: String,
    val number: Float,
    val index: Int,
    val totalChapters: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)

/**
 * Reading progress information for reader screens.
 */
@Serializable
data class ReaderProgress(
    /** For text: character/word position. For images: page index. For video: timestamp ms */
    val position: Long,
    /** Total length (characters, pages, or duration ms) */
    val total: Long,
    /** Progress percentage (0.0 - 1.0) */
    val percentage: Float = if (total > 0) position.toFloat() / total else 0f,
    /** Last read timestamp */
    val timestamp: Long = 0L,
    /** Additional data (e.g., scroll position, zoom level) */
    val extras: Map<String, String> = emptyMap()
)

/**
 * Abstract reader screen that plugins implement.
 * The actual UI implementation depends on the platform (Compose, etc.)
 */
interface ReaderScreen {
    /**
     * Called when screen is created/shown.
     */
    fun onCreate()
    
    /**
     * Called when screen is destroyed/hidden.
     */
    fun onDestroy()
    
    /**
     * Called when content changes (e.g., chapter navigation).
     */
    fun onContentChanged(content: SourceChapterContent)
    
    /**
     * Called when screen is paused (e.g., app backgrounded).
     */
    fun onPause() {}
    
    /**
     * Called when screen is resumed.
     */
    fun onResume() {}
    
    /**
     * Handle back press. Return true if handled, false to let system handle.
     */
    fun onBackPressed(): Boolean = false
    
    /**
     * Get current reading progress.
     */
    fun getCurrentProgress(): ReaderProgress
    
    /**
     * Restore reading progress.
     */
    fun restoreProgress(progress: ReaderProgress)
    
    /**
     * Get the Compose content for this screen.
     * Returns a lambda that can be invoked in a Compose context.
     * 
     * For non-Compose platforms, this can return null and use
     * platform-specific rendering.
     */
    fun getComposeContent(): Any? = null
}

/**
 * Reader setting definition.
 */
@Serializable
sealed class ReaderSetting {
    abstract val key: String
    abstract val title: String
    abstract val description: String?
    abstract val category: String?
    
    @Serializable
    data class Toggle(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val category: String? = null,
        val defaultValue: Boolean = false,
        val currentValue: Boolean = false
    ) : ReaderSetting()
    
    @Serializable
    data class Slider(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val category: String? = null,
        val min: Float,
        val max: Float,
        val step: Float = 1f,
        val defaultValue: Float,
        val currentValue: Float
    ) : ReaderSetting()
    
    @Serializable
    data class Selection(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val category: String? = null,
        val options: List<String>,
        val optionValues: List<String>,
        val defaultValue: String,
        val currentValue: String
    ) : ReaderSetting()
    
    @Serializable
    data class Color(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val category: String? = null,
        val defaultValue: Long, // ARGB color
        val currentValue: Long
    ) : ReaderSetting()
    
    @Serializable
    data class Font(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val category: String? = null,
        val availableFonts: List<String>,
        val defaultValue: String,
        val currentValue: String
    ) : ReaderSetting()
}
