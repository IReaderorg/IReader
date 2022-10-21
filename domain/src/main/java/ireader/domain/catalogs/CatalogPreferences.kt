

package ireader.domain.catalogs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

class CatalogPreferences(private val store: PreferenceStore) {

    fun lastRemoteCheck(): Preference<Long> {
        return store.getLong("last_remote_check", 0)
    }

    fun pinnedCatalogs(): Preference<Set<String>> {
        return store.getStringSet("pinned_catalogs", setOf())
    }
}
