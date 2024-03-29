package ireader.presentation.core.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import ireader.core.prefs.Preference
import ireader.domain.preferences.prefs.UiPreferences

actual class LocaleHelper(
    val context: Context,
    val uiPreferences: UiPreferences
) {
     val language : Preference<String> = uiPreferences.language()

    actual val languages :  MutableList<String> = mutableListOf<String>()

    init {
        getLocales()
    }


    actual fun setLocaleLang() {
        val lang = language.get()
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }


    actual fun updateLocal() {
        context.resources.apply {
            val locale = java.util.Locale(language.get())
            val config = Configuration(configuration)

            context.createConfigurationContext(configuration)
            java.util.Locale.setDefault(locale)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, displayMetrics)
        }
    }

    actual fun resetLocale() {
        context.resources.apply {
            val config = Configuration(configuration)
            val default = this.configuration.locale
            config.setLocale(default)
            context.resources.updateConfiguration(config, displayMetrics)
        }
    }


    actual  fun getLocales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = context.resources.assets.locales
            for (i in locales.indices) {
                languages.add(locales[i])
            }
            languages.sort()
        }
    }
}
