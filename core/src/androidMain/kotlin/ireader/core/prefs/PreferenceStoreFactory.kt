package ireader.core.prefs

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings

actual class PreferenceStoreFactory(private val context: Context) {
    actual fun create(vararg names: String): PreferenceStore {
        return StandardPreferenceStore(
            SharedPreferencesSettings(
                context.getSharedPreferences(
                    names.joinToString(separator = "_"),
                    Context.MODE_PRIVATE,
                ),
            ),
        )
    }
}