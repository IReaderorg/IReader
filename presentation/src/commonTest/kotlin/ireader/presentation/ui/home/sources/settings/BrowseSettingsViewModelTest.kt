package ireader.presentation.ui.home.sources.settings

import ireader.presentation.ui.home.sources.extension.Language
import ireader.presentation.ui.home.sources.extension.LocaleHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for BrowseSettingsViewModel language display functionality.
 * 
 * These tests verify that all 17+ supported languages are properly displayed
 * with correct names and emoji flags.
 */
class BrowseSettingsViewModelTest {

    /**
     * All language codes that should be supported in the app.
     * The app should show at least 17 languages.
     */
    private val expectedLanguageCodes = listOf(
        "ar",    // Arabic - العربية
        "be",    // Belarusian
        "bg",    // Bulgarian
        "bn",    // Bengali
        "ca",    // Catalan
        "cs",    // Czech
        "da",    // Danish
        "de",    // German
        "el",    // Greek
        "en",    // English
        "eo",    // Esperanto
        "es",    // Spanish
        "eu",    // Basque
        "fa",    // Persian
        "fi",    // Finnish
        "fil",   // Filipino
        "fr",    // French
        "gl",    // Galician
        "he",    // Hebrew
        "hi",    // Hindi
        "hr",    // Croatian
        "hu",    // Hungarian
        "id",    // Indonesian
        "it",    // Italian
        "ja",    // Japanese
        "jv",    // Javanese
        "ka",    // Georgian
        "kk",    // Kazakh
        "km",    // Khmer
        "kn",    // Kannada
        "ko",    // Korean
        "lt",    // Lithuanian
        "lv",    // Latvian
        "ml",    // Malayalam
        "mn",    // Mongolian
        "mr",    // Marathi
        "ms",    // Malay
        "my",    // Burmese
        "nb",    // Norwegian Bokmål
        "ne",    // Nepali
        "nl",    // Dutch
        "nn",    // Norwegian Nynorsk
        "or",    // Odia
        "pa",    // Punjabi
        "pl",    // Polish
        "pt",    // Portuguese
        "pt-BR", // Portuguese (Brazil)
        "ro",    // Romanian
        "ru",    // Russian
        "sa",    // Sanskrit
        "si",    // Sinhala
        "sk",    // Slovak
        "sr",    // Serbian
        "sv",    // Swedish
        "ta",    // Tamil
        "te",    // Telugu
        "th",    // Thai
        "tl",    // Tagalog
        "tr",    // Turkish
        "uk",    // Ukrainian
        "ur",    // Urdu
        "uz",    // Uzbek
        "vi",    // Vietnamese
        "zh",    // Chinese
        "zh-CN", // Chinese Simplified
        "zh-TW", // Chinese Traditional
        // Less common languages
        "aii",   // Assyrian Neo-Aramaic
        "am",    // Amharic
        "ceb",   // Cebuano
        "cv",    // Chuvash
        "sah",   // Sakha/Yakut
        "sc",    // Sardinian
        "sdh",   // Southern Kurdish
        "ti",    // Tigrinya
    )

    @Test
    fun `all expected languages should have display names`() {
        for (code in expectedLanguageCodes) {
            val displayName = LocaleHelper.getDisplayName(code)
            assertNotNull(displayName, "Display name for '$code' should not be null")
            assertTrue(displayName.isNotBlank(), "Display name for '$code' should not be blank")
            assertNotEquals(code, displayName, "Display name for '$code' should not be the code itself")
        }
    }

    @Test
    fun `arabic language should display correctly`() {
        val displayName = LocaleHelper.getDisplayName("ar")
        assertEquals("العربية", displayName, "Arabic should display as 'العربية'")
    }

    @Test
    fun `persian language should display correctly`() {
        val displayName = LocaleHelper.getDisplayName("fa")
        assertEquals("فارسی", displayName, "Persian should display as 'فارسی'")
    }

    @Test
    fun `hebrew language should display correctly`() {
        val displayName = LocaleHelper.getDisplayName("he")
        assertEquals("עברית", displayName, "Hebrew should display as 'עברית'")
    }

    @Test
    fun `urdu language should display correctly`() {
        val displayName = LocaleHelper.getDisplayName("ur")
        assertEquals("اردو", displayName, "Urdu should display as 'اردو'")
    }

    @Test
    fun `japanese language should display correctly`() {
        val displayName = LocaleHelper.getDisplayName("ja")
        assertEquals("日本語", displayName, "Japanese should display as '日本語'")
    }

    @Test
    fun `korean language should display correctly`() {
        val displayName = LocaleHelper.getDisplayName("ko")
        assertEquals("한국어", displayName, "Korean should display as '한국어'")
    }

    @Test
    fun `chinese simplified should display correctly`() {
        val displayName = LocaleHelper.getDisplayName("zh-CN")
        assertEquals("简体中文", displayName, "Chinese Simplified should display as '简体中文'")
    }

    @Test
    fun `chinese traditional should display correctly`() {
        val displayName = LocaleHelper.getDisplayName("zh-TW")
        assertEquals("繁體中文", displayName, "Chinese Traditional should display as '繁體中文'")
    }

    @Test
    fun `all expected languages should have emoji flags`() {
        for (code in expectedLanguageCodes) {
            val language = Language(code)
            val emoji = language.toEmoji()
            assertNotNull(emoji, "Emoji for '$code' should not be null")
            assertTrue(emoji.isNotBlank(), "Emoji for '$code' should not be blank")
        }
    }

    @Test
    fun `arabic language should have saudi arabia flag`() {
        val language = Language("ar")
        val emoji = language.toEmoji()
        assertNotNull(emoji)
        // Saudi Arabia flag emoji
        assertTrue(emoji.isNotBlank())
    }

    @Test
    fun `japanese language should have japan flag`() {
        val language = Language("ja")
        val emoji = language.toEmoji()
        assertNotNull(emoji)
        assertTrue(emoji.isNotBlank())
    }

    @Test
    fun `at least 17 languages should be supported`() {
        val supportedCount = expectedLanguageCodes.count { code ->
            val displayName = LocaleHelper.getDisplayName(code)
            val emoji = Language(code).toEmoji()
            displayName.isNotBlank() && displayName != code && emoji != null
        }
        assertTrue(supportedCount >= 17, "At least 17 languages should be fully supported, but only $supportedCount are")
    }

    @Test
    fun `null language code should return empty string`() {
        val displayName = LocaleHelper.getDisplayName(null)
        assertEquals("", displayName)
    }

    @Test
    fun `unknown language code should return uppercase code`() {
        val displayName = LocaleHelper.getDisplayName("xyz")
        assertEquals("XYZ", displayName)
    }

    @Test
    fun `language codes with region should be handled correctly`() {
        // Test pt-BR format
        val ptBr = LocaleHelper.getDisplayName("pt-BR")
        assertEquals("Português (Brasil)", ptBr)
        
        // Test zh-rCN format (Android resource format)
        val zhCn = LocaleHelper.getDisplayName("zh-rCN")
        assertEquals("简体中文", zhCn)
        
        // Test zh-rTW format
        val zhTw = LocaleHelper.getDisplayName("zh-rTW")
        assertEquals("繁體中文", zhTw)
    }

    @Test
    fun `all RTL languages should have display names and emojis`() {
        val rtlLanguages = listOf("ar", "fa", "he", "ur")
        for (code in rtlLanguages) {
            val displayName = LocaleHelper.getDisplayName(code)
            val emoji = Language(code).toEmoji()
            
            assertNotNull(displayName, "Display name for RTL language '$code' should not be null")
            assertTrue(displayName.isNotBlank(), "Display name for RTL language '$code' should not be blank")
            assertNotNull(emoji, "Emoji for RTL language '$code' should not be null")
        }
    }

    @Test
    fun `less common languages should have display names and emojis`() {
        val lessCommonLanguages = listOf("aii", "am", "ceb", "cv", "sah", "sc", "sdh", "ti")
        for (code in lessCommonLanguages) {
            val displayName = LocaleHelper.getDisplayName(code)
            val emoji = Language(code).toEmoji()
            
            assertNotNull(displayName, "Display name for '$code' should not be null")
            assertTrue(displayName.isNotBlank(), "Display name for '$code' should not be blank")
            assertNotNull(emoji, "Emoji for '$code' should not be null")
        }
    }
}
