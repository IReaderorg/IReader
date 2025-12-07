package ireader.domain.preferences.prefs

import androidx.compose.ui.text.ExperimentalTextApi
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.core.prefs.getEnum
import ireader.domain.models.prefs.PreferenceValues

@OptIn(ExperimentalTextApi::class)
class ReaderPreferences constructor(
    private val preferenceStore: PreferenceStore,
) {
    companion object PreferenceKeys {
        const val SAVED_FONT_SIZE_PREFERENCES = "reader_font_size"
        const val SELECTED_FONT_ID = "selected_font_id"

        const val SAVED_BRIGHTNESS_PREFERENCES = "reader_brightness"
        const val SAVED_IMMERSIVE_MODE_PREFERENCES = "reader_immersive_mode"
        const val SAVED_AUTO_BRIGHTNESS_PREFERENCES = "reader_auto_brightness"
        const val READING_MODE = "reader_mode"


        const val SAVED_TEXT_COLOR = "text_color"
        const val SAVED_FONT_HEIGHT = "font_height"
        const val SAVED_PARAGRAPH_DISTANCE = "paragraph_distance"
        const val SAVED_PARAGRAPH_INDENT = "paragraph_indent"
        const val SAVED_ORIENTATION = "orientation_reader_screen"
        const val SLEEP_TIMER = "tts_sleep_timer"
        const val SLEEP_TIMER_MODE = "tts_sleep_mode"

        const val SCROLL_MODE = "scroll_mode"
        const val AUTO_SCROLL_MODE_INTERVAL = "auto_scroll_mode_interval"
        const val AUTO_SCROLL_MODE_OFFSET = "auto_scroll_mode_offset"
        const val SCROLL_INDICATOR_PADDING = "scroll_indicator_padding"
        const val SCROLL_INDICATOR_WIDTH = "scroll_indicator_width"
        const val SCROLL_INDICATOR_IS_ENABLE = "scroll_indicator_is_enable"
        const val SCROLL_INDICATOR_IS_DRAGGABLE = "scroll_indicator_is_draggable"
        const val SCROLL_INDICATOR_SELECTED_COLOR = "scroll_indicator_selected_color"
        const val SCROLL_INDICATOR_UNSELECTED_COLOR = "scroll_indicator_unselected_color"
        const val SCROLL_INDICATOR_ALIGNMENT = "scroll_indicator_alignment"
        const val SELECTABLE_TEXT = "selectable_text"
        const val TEXT_ALIGNMENT = "text_alignment"

        const val TEXT_READER_SPEECH_RATE = "text_reader_speech_rate"
        const val TEXT_READER_SPEECH_PITCH = "text_reader_speech_pitch"
        const val TEXT_READER_SPEECH_LANGUAGE = "text_reader_speech_language"
        const val TEXT_READER_SPEECH_VOICE = "text_reader_speech_selected_voice"
        const val TEXT_READER_AUTO_NEXT = "text_reader_auto_next"
    }

    fun brightness(): Preference<Float> {
        return preferenceStore.getFloat(SAVED_BRIGHTNESS_PREFERENCES, .5F)
    }

    fun immersiveMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SAVED_IMMERSIVE_MODE_PREFERENCES, false)
    }

    fun autoBrightness(): Preference<Boolean> {
        return preferenceStore.getBoolean(SAVED_AUTO_BRIGHTNESS_PREFERENCES, true)
    }

    fun fontSize(): Preference<Int> {
        return preferenceStore.getInt(SAVED_FONT_SIZE_PREFERENCES, 18)
    }

    fun selectedFontId(): Preference<String> {
        return preferenceStore.getString(SELECTED_FONT_ID, "")
    }

    fun followTTSSpeaker(): Preference<Boolean> {
        return preferenceStore.getBoolean("follow_tts_speaker",false)
    }



    fun webViewIntegration(): Preference<Boolean> {
        return preferenceStore.getBoolean("webView_integration", false)
    }
    
    fun webViewBackgroundMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("webView_background_mode", true)
    }





    fun scrollBarAlignment(): Preference<PreferenceValues.PreferenceTextAlignment> {
        return preferenceStore.getEnum(SCROLL_INDICATOR_ALIGNMENT, PreferenceValues.PreferenceTextAlignment.Right)
    }
    fun ttsIconAlignments(): Preference<PreferenceValues.PreferenceAlignment> {
        return preferenceStore.getEnum("tts_icons_alignments", PreferenceValues.PreferenceAlignment.TopLeft)
    }

    fun lineHeight(): Preference<Int> {
        return preferenceStore.getInt(SAVED_FONT_HEIGHT, 25)
    }

    fun readingMode(): Preference<ReadingMode> {
        return preferenceStore.getEnum(READING_MODE, ReadingMode.Page)
    }

    fun paragraphDistance(): Preference<Int> {
        return preferenceStore.getInt(SAVED_PARAGRAPH_DISTANCE, 2)
    }


    fun sleepTime(): Preference<Long> {
        return preferenceStore.getLong(SLEEP_TIMER, 15)
    }

    fun sleepMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SLEEP_TIMER_MODE, false)
    }

    fun bionicReading(): Preference<Boolean> {
        return preferenceStore.getBoolean("ENABLE_BIONIC_READING", false)
    }

    fun textAlign(): Preference<PreferenceValues.PreferenceTextAlignment> {
        return preferenceStore.getEnum(
            TEXT_ALIGNMENT,
            PreferenceValues.PreferenceTextAlignment.Left
        )
    }

    fun paragraphIndent(): Preference<Int> {
        return preferenceStore.getInt(SAVED_PARAGRAPH_INDENT, 8)
    }

    fun topMargin(): Preference<Int> {
        return preferenceStore.getInt("reader_top_margin", 0)
    }

    fun leftMargin(): Preference<Int> {
        return preferenceStore.getInt("reader_left_margin", 0)
    }

    fun rightMargin(): Preference<Int> {
        return preferenceStore.getInt("reader_right_margin", 0)
    }

    fun bottomMargin(): Preference<Int> {
        return preferenceStore.getInt("reader_bottom_margin", 0)
    }

    fun topContentPadding(): Preference<Int> {
        return preferenceStore.getInt("reader_top_padding", 2)
    }

    fun bottomContentPadding(): Preference<Int> {
        return preferenceStore.getInt("reader_bottom_padding", 2)
    }

    fun betweenLetterSpaces(): Preference<Int> {
        return preferenceStore.getInt("reader_text_space", 0)
    }

    fun textWeight(): Preference<Int> {
        return preferenceStore.getInt("reader_text_weight", 400)
    }

    fun screenAlwaysOn(): Preference<Boolean> {
        return preferenceStore.getBoolean("reader_always_on", false)
    }

    fun scrollMode(): Preference<Boolean> {
        return preferenceStore.getBoolean(SCROLL_MODE, true)
    }

    fun showScrollIndicator(): Preference<Boolean> {
        return preferenceStore.getBoolean(SCROLL_INDICATOR_IS_ENABLE, true)
    }

    fun scrollbarMode(): Preference<PreferenceValues.ScrollbarSelectionMode> {
        return preferenceStore.getEnum(
            SCROLL_INDICATOR_IS_DRAGGABLE,
            PreferenceValues.ScrollbarSelectionMode.Full
        )
    }

    fun selectableText(): Preference<Boolean> {
        return preferenceStore.getBoolean(SELECTABLE_TEXT, false)
    }

    fun autoScrollInterval(): Preference<Long> {
        return preferenceStore.getLong(AUTO_SCROLL_MODE_INTERVAL, 5000L)
    }

    fun autoScrollOffset(): Preference<Int> {
        return preferenceStore.getInt(AUTO_SCROLL_MODE_OFFSET, 1)
    }

    fun readingSpeedWPM(): Preference<Int> {
        return preferenceStore.getInt("reading_speed_wpm", 225)
    }

    fun scrollIndicatorWith(): Preference<Int> {
        return preferenceStore.getInt(SCROLL_INDICATOR_WIDTH, 2)
    }

    fun scrollIndicatorPadding(): Preference<Int> {
        return preferenceStore.getInt(SCROLL_INDICATOR_PADDING, 4)
    }

    fun speechRate(): Preference<Float> {
        return preferenceStore.getFloat(TEXT_READER_SPEECH_RATE, .8f)
    }

    fun readerAutoNext(): Preference<Boolean> {
        return preferenceStore.getBoolean(TEXT_READER_AUTO_NEXT, false)
    }

    fun speechPitch(): Preference<Float> {
        return preferenceStore.getFloat(TEXT_READER_SPEECH_PITCH, .8f)
    }



    fun speechLanguage(): Preference<String> {
        return preferenceStore.getString(TEXT_READER_SPEECH_LANGUAGE, "")
    }

    fun showChapterNumberPreferences(): Preference<ChapterDisplayMode> {
        return preferenceStore.getEnum("chapter_layout_mode", ChapterDisplayMode.Default)
    }
    fun translatorEngine(): Preference<Long> {
        return preferenceStore.getLong("translatorEngine", -1)
    }
    
    // Plugin-based translation engine ID (if using a plugin)
    // Requirements: 4.2
    fun translatorPluginId(): Preference<String> {
        return preferenceStore.getString("translator_plugin_id", "")
    }

    fun translatorOriginLanguage(): Preference<String> {
        return preferenceStore.getString("translator_origin_language", "en")
    }
    fun translatorTargetLanguage(): Preference<String> {
        return preferenceStore.getString("translator_target_language", "en")
    }
    fun translatorContentType(): Preference<Int> {
        return preferenceStore.getInt("translator_content_type", 0)
    }
    fun translatorToneType(): Preference<Int> {
        return preferenceStore.getInt("translator_tone_type", 0)
    }
    fun translatorPreserveStyle(): Preference<Boolean> {
        return preferenceStore.getBoolean("translator_preserve_style", true)
    }
    fun openAIApiKey(): Preference<String> {
        return preferenceStore.getString("openai_api_key", "")
    }
    fun deepSeekApiKey(): Preference<String> {
        return preferenceStore.getString("deepseek_api_key", "")
    }
    fun geminiApiKey(): Preference<String> {
        return preferenceStore.getString("gemini_api_key", "")
    }

    fun ollamaServerUrl(): Preference<String> {
        return preferenceStore.getString("ollama_server_url", "http://localhost:11434/api/chat")
    }
    fun ollamaModel(): Preference<String> {
        return preferenceStore.getString("ollama_model", "llama2")
    }
    fun chatGptCookies(): Preference<String> {
        return preferenceStore.getString("chatgpt_cookies", "")
    }
    
    fun deepSeekCookies(): Preference<String> {
        return preferenceStore.getString("deepseek_cookies", "")
    }
    
    fun autoPreloadNextChapter(): Preference<Boolean> {
        return preferenceStore.getBoolean("auto_preload_next_chapter", true)
    }
    
    fun preloadOnlyOnWifi(): Preference<Boolean> {
        return preferenceStore.getBoolean("preload_only_on_wifi", true)
    }
    
    fun chatGptPrompt(): Preference<String> {
        return preferenceStore.getString("chatgpt_prompt", "")
    }

    // User-selected Gemini model preference
    fun geminiModel(): Preference<String> {
        return preferenceStore.getString("gemini_model", "")
    }

    fun chapterSortType(): Preference<String> {
        return preferenceStore.getString("chapter_sort_type", "Default")
    }

    fun chapterSortAscending(): Preference<Boolean> {
        return preferenceStore.getBoolean("chapter_sort_ascending", true)
    }
    
    // Translation mode preferences
    fun showTranslatedContent(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_translated_content", false)
    }
    
    fun autoSaveTranslations(): Preference<Boolean> {
        return preferenceStore.getBoolean("auto_save_translations", true)
    }
    
    fun applyGlossaryToTranslations(): Preference<Boolean> {
        return preferenceStore.getBoolean("apply_glossary_to_translations", true)
    }
    
    // Bilingual mode preferences
    fun bilingualModeEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("bilingual_mode_enabled", false)
    }
    
    fun bilingualModeLayout(): Preference<Int> {
        // 0 = SIDE_BY_SIDE, 1 = PARAGRAPH_BY_PARAGRAPH
        return preferenceStore.getInt("bilingual_mode_layout", 0)
    }
    
    // Volume key navigation
    fun volumeKeyNavigation(): Preference<Boolean> {
        return preferenceStore.getBoolean("volume_key_navigation", false)
    }

    // Paragraph translation menu
    fun paragraphTranslationEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("paragraph_translation_enabled", false)
    }
    
    // TTS with translated text
    fun useTTSWithTranslatedText(): Preference<Boolean> {
        return preferenceStore.getBoolean("use_tts_with_translated_text", false)
    }
    
    // Auto-translate next chapter when TTS advances
    fun autoTranslateNextChapter(): Preference<Boolean> {
        return preferenceStore.getBoolean("auto_translate_next_chapter", false)
    }
    
    // Default reading mode for new books
    fun defaultReadingMode(): Preference<ReadingMode> {
        return preferenceStore.getEnum("default_reading_mode", ReadingMode.Page)
    }
    
    // Reading break reminder preferences
    fun readingBreakReminderEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("reading_break_reminder_enabled", false)
    }
    
    fun readingBreakInterval(): Preference<Int> {
        // Interval in minutes: 30, 45, 60, 90, 120
        return preferenceStore.getInt("reading_break_interval", 60)
    }
    
    fun lastReadingBreakPromptTime(): Preference<Long> {
        return preferenceStore.getLong("last_reading_break_prompt_time", 0L)
    }


    // ========== Advanced Reader System (Mihon-inspired) ==========
    // Requirements: 5.1, 5.2, 11.1, 11.2, 11.4, 11.5, 8.1, 8.2

    // Page transitions and animations
    fun pageTransitions(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_enable_transitions_key", true)
    }

    fun flashOnPageChange(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_reader_flash", false)
    }

    fun flashDurationMillis(): Preference<Int> {
        return preferenceStore.getInt("pref_reader_flash_duration", 100)
    }

    fun flashPageInterval(): Preference<Int> {
        return preferenceStore.getInt("pref_reader_flash_interval", 1)
    }

    fun flashColor(): Preference<FlashColor> {
        return preferenceStore.getEnum("pref_reader_flash_mode", FlashColor.BLACK)
    }

    fun doubleTapAnimSpeed(): Preference<Int> {
        return preferenceStore.getInt("pref_double_tap_anim_speed", 500)
    }

    // Display preferences
    fun showPageNumber(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_show_page_number_key", true)
    }

    fun showReadingMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_show_reading_mode", true)
    }

    fun fullscreen(): Preference<Boolean> {
        return preferenceStore.getBoolean("fullscreen", true)
    }

    fun drawUnderCutout(): Preference<Boolean> {
        return preferenceStore.getBoolean("cutout_short", true)
    }

    fun keepScreenOn(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_keep_screen_on_key", false)
    }

    // Viewer modes and orientation
    fun defaultReaderMode(): Preference<Int> {
        return preferenceStore.getInt("pref_default_reading_mode_key", ReaderMode.RIGHT_TO_LEFT.flagValue)
    }

    fun defaultOrientationType(): Preference<Int> {
        return preferenceStore.getInt("pref_default_orientation_type_key", ReaderOrientation.FREE.flagValue)
    }

    // Zoom and scaling
    fun webtoonDoubleTapZoomEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_enable_double_tap_zoom_webtoon", true)
    }

    fun imageScaleType(): Preference<Int> {
        return preferenceStore.getInt("pref_image_scale_type_key", 1)
    }

    fun zoomStart(): Preference<Int> {
        return preferenceStore.getInt("pref_zoom_start_key", 1)
    }

    fun landscapeZoom(): Preference<Boolean> {
        return preferenceStore.getBoolean("landscape_zoom", true)
    }

    fun navigateToPan(): Preference<Boolean> {
        return preferenceStore.getBoolean("navigate_pan", true)
    }

    // Cropping and padding
    fun cropBorders(): Preference<Boolean> {
        return preferenceStore.getBoolean("crop_borders", false)
    }

    fun cropBordersWebtoon(): Preference<Boolean> {
        return preferenceStore.getBoolean("crop_borders_webtoon", false)
    }

    fun webtoonSidePadding(): Preference<Int> {
        return preferenceStore.getInt("webtoon_side_padding", 0)
    }

    fun webtoonDisableZoomOut(): Preference<Boolean> {
        return preferenceStore.getBoolean("webtoon_disable_zoom_out", false)
    }

    // Theme and appearance
    fun readerTheme(): Preference<Int> {
        return preferenceStore.getInt("pref_reader_theme_key", 1)
    }

    fun alwaysShowChapterTransition(): Preference<Boolean> {
        return preferenceStore.getBoolean("always_show_chapter_transition", true)
    }

    fun readerHideThreshold(): Preference<ReaderHideThreshold> {
        return preferenceStore.getEnum("reader_hide_threshold", ReaderHideThreshold.LOW)
    }

    // Dual page support
    fun dualPageSplitPaged(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_dual_page_split", false)
    }

    fun dualPageInvertPaged(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_dual_page_invert", false)
    }

    fun dualPageSplitWebtoon(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_dual_page_split_webtoon", false)
    }

    fun dualPageInvertWebtoon(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_dual_page_invert_webtoon", false)
    }

    fun dualPageRotateToFit(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_dual_page_rotate", false)
    }

    fun dualPageRotateToFitInvert(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_dual_page_rotate_invert", false)
    }

    fun dualPageRotateToFitWebtoon(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_dual_page_rotate_webtoon", false)
    }

    fun dualPageRotateToFitInvertWebtoon(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_dual_page_rotate_invert_webtoon", false)
    }

    // Color filter system
    fun customBrightness(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_custom_brightness_key", false)
    }

    fun customBrightnessValue(): Preference<Int> {
        return preferenceStore.getInt("custom_brightness_value", 0)
    }

    fun colorFilter(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_color_filter_key", false)
    }

    fun colorFilterValue(): Preference<Int> {
        return preferenceStore.getInt("color_filter_value", 0)
    }

    fun colorFilterMode(): Preference<Int> {
        return preferenceStore.getInt("color_filter_mode", 0)
    }

    fun grayscale(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_grayscale", false)
    }

    fun invertedColors(): Preference<Boolean> {
        return preferenceStore.getBoolean("pref_inverted_colors", false)
    }

    // Navigation controls
    fun readWithLongTap(): Preference<Boolean> {
        return preferenceStore.getBoolean("reader_long_tap", true)
    }

    fun readWithVolumeKeys(): Preference<Boolean> {
        return preferenceStore.getBoolean("reader_volume_keys", false)
    }

    fun readWithVolumeKeysInverted(): Preference<Boolean> {
        return preferenceStore.getBoolean("reader_volume_keys_inverted", false)
    }

    fun navigationModePager(): Preference<Int> {
        return preferenceStore.getInt("reader_navigation_mode_pager", 0)
    }

    fun navigationModeWebtoon(): Preference<Int> {
        return preferenceStore.getInt("reader_navigation_mode_webtoon", 0)
    }

    fun pagerNavInverted(): Preference<TappingInvertMode> {
        return preferenceStore.getEnum("reader_tapping_inverted", TappingInvertMode.NONE)
    }

    fun webtoonNavInverted(): Preference<TappingInvertMode> {
        return preferenceStore.getEnum("reader_tapping_inverted_webtoon", TappingInvertMode.NONE)
    }

    fun showNavigationOverlayNewUser(): Preference<Boolean> {
        return preferenceStore.getBoolean("reader_navigation_overlay_new_user", true)
    }

    fun showNavigationOverlayOnStart(): Preference<Boolean> {
        return preferenceStore.getBoolean("reader_navigation_overlay_on_start", false)
    }

    // Chapter navigation
    fun skipRead(): Preference<Boolean> {
        return preferenceStore.getBoolean("skip_read", false)
    }

    fun skipFiltered(): Preference<Boolean> {
        return preferenceStore.getBoolean("skip_filtered", true)
    }

    fun skipDupe(): Preference<Boolean> {
        return preferenceStore.getBoolean("skip_dupe", false)
    }

    // Reader statistics tracking
    fun trackReadingTime(): Preference<Boolean> {
        return preferenceStore.getBoolean("track_reading_time", true)
    }

    fun totalReadingTimeMillis(): Preference<Long> {
        return preferenceStore.getLong("total_reading_time_millis", 0L)
    }

    fun currentSessionStartTime(): Preference<Long> {
        return preferenceStore.getLong("current_session_start_time", 0L)
    }

    fun pagesRead(): Preference<Long> {
        return preferenceStore.getLong("pages_read", 0L)
    }

    fun chaptersCompleted(): Preference<Long> {
        return preferenceStore.getLong("chapters_completed", 0L)
    }


    // Daily reading goal (in minutes)
    fun dailyReadingGoal(): Preference<Int> {
        return preferenceStore.getInt("daily_reading_goal", 30)
    }

    // TTS preferences
    fun ttsSpeed(): Preference<Float> {
        return preferenceStore.getFloat("tts_speed", 1.0f)
    }

    fun ttsPitch(): Preference<Float> {
        return preferenceStore.getFloat("tts_pitch", 1.0f)
    }

    fun ttsVoice(): Preference<String> {
        return preferenceStore.getString("tts_voice", "")
    }

    fun ttsAutoPlay(): Preference<Boolean> {
        return preferenceStore.getBoolean("tts_auto_play", false)
    }

    fun ttsHighlightText(): Preference<Boolean> {
        return preferenceStore.getBoolean("tts_highlight_text", true)
    }

    fun ttsSkipEmptyLines(): Preference<Boolean> {
        return preferenceStore.getBoolean("tts_skip_empty_lines", true)
    }
    
    // Performance preferences for older devices
    fun reducedAnimations(): Preference<Boolean> {
        return preferenceStore.getBoolean("reduced_animations", false)
    }
    
    // TTS Screen Display Preferences
    fun ttsUseCustomColors(): Preference<Boolean> {
        return preferenceStore.getBoolean("tts_use_custom_colors", false)
    }
    
    fun ttsBackgroundColor(): Preference<Long> {
        // Default: Dark color 0xFF1E1E1E
        return preferenceStore.getLong("tts_background_color", 0xFF1E1E1E)
    }
    
    fun ttsTextColor(): Preference<Long> {
        // Default: White 0xFFFFFFFF
        return preferenceStore.getLong("tts_text_color", 0xFFFFFFFF)
    }
    
    fun ttsFontSize(): Preference<Int> {
        return preferenceStore.getInt("tts_font_size", 18)
    }
    
    fun ttsTextAlignment(): Preference<PreferenceValues.PreferenceTextAlignment> {
        return preferenceStore.getEnum("tts_text_alignment", PreferenceValues.PreferenceTextAlignment.Left)
    }
    
    // Sentence-level highlighting for TTS (time-based estimation)
    fun ttsSentenceHighlight(): Preference<Boolean> {
        return preferenceStore.getBoolean("tts_sentence_highlight", false)
    }
    
    // ========== TTS Text Merging Settings ==========
    // Merge multiple paragraphs into one request for better reading experience
    
    /**
     * Number of words to merge for remote TTS engines (Gradio, etc.)
     * Higher values = fewer requests, smoother reading
     * Range: 50-500 words, 0 = disabled
     */
    fun ttsMergeWordsRemote(): Preference<Int> {
        return preferenceStore.getInt("tts_merge_words_remote", 0)
    }
    
    /**
     * Number of words to merge for native TTS engines (Android TTS, Piper, etc.)
     * Range: 50-500 words, 0 = disabled
     */
    fun ttsMergeWordsNative(): Preference<Int> {
        return preferenceStore.getInt("tts_merge_words_native", 0)
    }
    
    // ========== TTS Chapter Audio Caching (Remote Engines Only) ==========
    
    /**
     * Enable downloading and caching whole chapter audio for remote engines
     */
    fun ttsChapterCacheEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("tts_chapter_cache_enabled", false)
    }
    
    /**
     * Number of days to keep cached chapter audio before auto-deletion
     * Range: 1-30 days
     */
    fun ttsChapterCacheDays(): Preference<Int> {
        return preferenceStore.getInt("tts_chapter_cache_days", 7)
    }
    
    /**
     * Use TTS v2 architecture (experimental)
     * When enabled, uses the new clean architecture TTS implementation
     */
    fun useTTSV2(): Preference<Boolean> {
        return preferenceStore.getBoolean("use_tts_v2", false)
    }
}

enum class ReadingMode {
    Page,
    Continues;

    companion object {
        fun valueOf(index: Int): ReadingMode {
            return when (index) {
                Page.ordinal -> Page
                Continues.ordinal -> Continues
                else -> throw IllegalArgumentException()
            }
        }
    }
}

enum class ChapterDisplayMode {
    Default,
    SourceTitle,
    ChapterNumber
}

// ========== Advanced Reader Enums (Mihon-inspired) ==========

enum class FlashColor {
    BLACK,
    WHITE,
    WHITE_BLACK
}

enum class ReaderMode(val flagValue: Int) {
    LEFT_TO_RIGHT(1),
    RIGHT_TO_LEFT(2),
    VERTICAL(3),
    WEBTOON(4),
    CONTINUOUS_VERTICAL(5);

    companion object {
        fun fromFlagValue(value: Int): ReaderMode {
            return entries.find { it.flagValue == value } ?: RIGHT_TO_LEFT
        }
    }
}

enum class ReaderOrientation(val flagValue: Int) {
    FREE(0),
    PORTRAIT(1),
    LANDSCAPE(2),
    LOCKED_PORTRAIT(3),
    LOCKED_LANDSCAPE(4);

    companion object {
        fun fromFlagValue(value: Int): ReaderOrientation {
            return entries.find { it.flagValue == value } ?: FREE
        }
    }
}

enum class TappingInvertMode {
    NONE,
    HORIZONTAL,
    VERTICAL,
    BOTH;

    val shouldInvertHorizontal: Boolean
        get() = this == HORIZONTAL || this == BOTH

    val shouldInvertVertical: Boolean
        get() = this == VERTICAL || this == BOTH
}

enum class ReaderHideThreshold(val threshold: Int) {
    HIGHEST(5),
    HIGH(13),
    LOW(31),
    LOWEST(47)
}
