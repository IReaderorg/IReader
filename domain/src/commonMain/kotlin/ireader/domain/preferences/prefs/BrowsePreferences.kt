package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

class BrowsePreferences(private val preferenceStore: PreferenceStore) {

    fun concurrentGlobalSearches(): Preference<Int> {
        return preferenceStore.getInt("browse_concurrent_searches", 3)
    }

    fun selectedLanguages(): Preference<Set<String>> {
        return preferenceStore.getStringSet("browse_selected_languages", setOf("en"))
    }

    fun searchTimeout(): Preference<Long> {
        return preferenceStore.getLong("browse_search_timeout", 30000L)
    }

    fun maxResultsPerSource(): Preference<Int> {
        return preferenceStore.getInt("browse_max_results", 25)
    }
}
