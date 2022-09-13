

package ireader.domain.catalogs

import ireader.core.api.prefs.Preference
import ireader.core.api.prefs.PreferenceStore

class CatalogPreferences(private val store: PreferenceStore) {

    fun lastRemoteCheck(): Preference<Long> {
        return store.getLong("last_remote_check", 0)
    }

    fun pinnedCatalogs(): Preference<Set<String>> {
        return store.getStringSet("pinned_catalogs", setOf())
    }
}
