package ireader.presentation.ui.home.sources.extension

import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.MR
import java.util.*

/**
 * Utility class to change the application's language in runtime.
 */
object LocaleHelper {

    /**
     * Returns Display name of a string language code
     */
    fun getSourceDisplayName(lang: String?, localizeHelper: LocalizeHelper): String {
        return when (lang) {
            SourceKeys.LAST_USED_KEY -> localizeHelper.localize(MR.strings.last_used_source)
            SourceKeys.PINNED_KEY -> localizeHelper.localize(MR.strings.pinned_sources)
            SourceKeys.INSTALLED_KEY -> localizeHelper.localize(MR.strings.installed)
            SourceKeys.AVAILABLE -> localizeHelper.localize(MR.strings.available)
            "other" -> localizeHelper.localize(MR.strings.other_source)
            "all" -> localizeHelper.localize(MR.strings.all_lang)
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
        val locale = getLocale(lang)
        return locale.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
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
