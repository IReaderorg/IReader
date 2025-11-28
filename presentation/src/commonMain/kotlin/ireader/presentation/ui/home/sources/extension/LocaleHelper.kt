package ireader.presentation.ui.home.sources.extension

import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import java.util.*

/**
 * Utility class to change the application's language in runtime.
 */
object LocaleHelper {

    /**
     * Comprehensive mapping of language codes to their native display names.
     * This ensures all languages are displayed correctly, even those not recognized by java.util.Locale.
     */
    private val languageDisplayNames = mapOf(
        // Common languages
        "ar" to "العربية",
        "be" to "Беларуская",
        "bg" to "Български",
        "bn" to "বাংলা",
        "ca" to "Català",
        "cs" to "Čeština",
        "da" to "Dansk",
        "de" to "Deutsch",
        "el" to "Ελληνικά",
        "en" to "English",
        "eo" to "Esperanto",
        "es" to "Español",
        "eu" to "Euskara",
        "fa" to "فارسی",
        "fi" to "Suomi",
        "fil" to "Filipino",
        "fr" to "Français",
        "gl" to "Galego",
        "he" to "עברית",
        "hi" to "हिन्दी",
        "hr" to "Hrvatski",
        "hu" to "Magyar",
        "id" to "Indonesia",
        "in" to "Indonesia", // Alternative code for Indonesian
        "it" to "Italiano",
        "ja" to "日本語",
        "jv" to "Basa Jawa",
        "ka" to "ქართული",
        "kk" to "Қазақша",
        "km" to "ភាសាខ្មែរ",
        "kn" to "ಕನ್ನಡ",
        "ko" to "한국어",
        "lt" to "Lietuvių",
        "lv" to "Latviešu",
        "ml" to "മലയാളം",
        "mn" to "Монгол",
        "mr" to "मराठी",
        "ms" to "Bahasa Melayu",
        "my" to "မြန်မာဘာသာ",
        "nb" to "Norsk Bokmål",
        "ne" to "नेपाली",
        "nl" to "Nederlands",
        "nn" to "Norsk Nynorsk",
        "no" to "Norsk",
        "or" to "ଓଡ଼ିଆ",
        "pa" to "ਪੰਜਾਬੀ",
        "pl" to "Polski",
        "pt" to "Português",
        "pt-BR" to "Português (Brasil)",
        "pt-PT" to "Português (Portugal)",
        "ro" to "Română",
        "ru" to "Русский",
        "sa" to "संस्कृतम्",
        "si" to "සිංහල",
        "sk" to "Slovenčina",
        "sr" to "Српски",
        "sv" to "Svenska",
        "ta" to "தமிழ்",
        "te" to "తెలుగు",
        "th" to "ไทย",
        "tl" to "Tagalog",
        "tr" to "Türkçe",
        "uk" to "Українська",
        "ur" to "اردو",
        "uz" to "Oʻzbekcha",
        "vi" to "Tiếng Việt",
        "zh" to "中文",
        "zh-CN" to "简体中文",
        "zh-TW" to "繁體中文",
        "zh-Hans" to "简体中文",
        "zh-Hant" to "繁體中文",
        // Less common languages that may not be recognized by Locale
        "aii" to "ܣܘܪܝܬ", // Assyrian Neo-Aramaic
        "am" to "አማርኛ", // Amharic
        "ceb" to "Cebuano",
        "cv" to "Чӑвашла", // Chuvash
        "sah" to "Саха тыла", // Sakha/Yakut
        "sc" to "Sardu", // Sardinian
        "sdh" to "کوردی خوارین", // Southern Kurdish
        "ti" to "ትግርኛ", // Tigrinya
        // Multi-language
        "multi" to "Multi",
    )

    /**
     * Returns Display name of a string language code
     */
    fun getSourceDisplayName(lang: String?, localizeHelper: LocalizeHelper): String {
        return when (lang) {
            SourceKeys.LAST_USED_KEY -> localizeHelper.localize(Res.string.last_used_source)
            SourceKeys.PINNED_KEY -> localizeHelper.localize(Res.string.pinned_sources)
            SourceKeys.INSTALLED_KEY -> localizeHelper.localize(Res.string.installed)
            SourceKeys.AVAILABLE -> localizeHelper.localize(Res.string.available)
            "other" -> localizeHelper.localize(Res.string.other_source)
            "all" -> localizeHelper.localize(Res.string.all_lang)
            else -> getDisplayName(lang)
        }
    }

    /**
     * Returns Display name of a string language code
     *
     * @param lang empty for system language
     */
    fun getDisplayName(lang: String?): String {
        if (lang == null) {
            return ""
        }
        
        // First check our comprehensive mapping
        languageDisplayNames[lang]?.let { return it }
        
        // Try normalized versions (e.g., "zh-rCN" -> "zh-CN")
        val normalizedLang = lang.replace("-r", "-")
        languageDisplayNames[normalizedLang]?.let { return it }
        
        // Fall back to Locale for any unmapped languages
        val locale = getLocale(lang)
        val displayName = locale.getDisplayName(locale)
        
        // If Locale returns the code itself or empty, return the code in uppercase
        return if (displayName.isBlank() || displayName == lang) {
            lang.uppercase()
        } else {
            displayName.replaceFirstChar { it.uppercase(locale) }
        }
    }

    /**
     * Return Locale from string language code
     */
    private fun getLocale(lang: String): Locale {
        val sp = lang.split("_", "-")
        return when (sp.size) {
            2 -> Locale(sp[0], sp[1])
            3 -> Locale(sp[0], sp[1], sp[2])
            else -> Locale(lang)
        }
    }
}
