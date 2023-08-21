package ireader.presentation.ui.home.sources.extension

import ireader.i18n.LocalizeHelper
import java.util.Locale

/**
 * Utility class to change the application's language in runtime.
 */
object LocaleHelper {

    /**
     * Returns Display name of a string language code
     */
    fun getSourceDisplayName(lang: String?, localizeHelper: LocalizeHelper): String {
        return when (lang) {
            SourceKeys.LAST_USED_KEY -> localizeHelper.localize { xml -> xml.lastUsedSource }
            SourceKeys.PINNED_KEY -> localizeHelper.localize { xml -> xml.pinnedSources }
            SourceKeys.INSTALLED_KEY -> localizeHelper.localize { xml -> xml.installed }
            SourceKeys.AVAILABLE -> localizeHelper.localize { xml -> xml.available }
            "other" -> localizeHelper.localize { xml -> xml.otherSource }
            "all" -> localizeHelper.localize { xml -> xml.allLang }
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
