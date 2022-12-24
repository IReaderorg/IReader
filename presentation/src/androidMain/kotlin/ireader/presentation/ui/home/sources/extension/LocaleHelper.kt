package ireader.presentation.ui.home.sources.extension

import android.content.Context
import androidx.core.os.LocaleListCompat
import ireader.presentation.R
import java.util.Locale

/**
 * Utility class to change the application's language in runtime.
 */
object LocaleHelper {

    /**
     * Returns Display name of a string language code
     */
    fun getSourceDisplayName(lang: String?, context: Context): String {
        return when (lang) {
            SourceKeys.LAST_USED_KEY -> context.getString(R.string.last_used_source)
            SourceKeys.PINNED_KEY -> context.getString(R.string.pinned_sources)
            SourceKeys.INSTALLED_KEY -> context.getString(R.string.installed)
            SourceKeys.AVAILABLE -> context.getString(R.string.available)
            "other" -> context.getString(R.string.other_source)
            "all" -> context.getString(R.string.all_lang)
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

        val locale = if (lang.isEmpty()) {
            LocaleListCompat.getAdjustedDefault()[0]
        } else {
            getLocale(lang)
        }
        return locale!!.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
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
object SourceKeys {
    const val PINNED_KEY = "pinned"
    const val INSTALLED_KEY = "installed"
    const val AVAILABLE = "available"
    const val LAST_USED_KEY = "last_used"
}
