package org.ireader.domain.extensions.cataloge_service

import org.ireader.core.prefs.Preference
import org.ireader.core.prefs.PreferenceStore

class CatalogPreferences(private val store: PreferenceStore) {

    fun gridMode(): Preference<Boolean> {
        return store.getBoolean("grid_mode", true)
    }

    fun lastListingUsed(sourceId: Long): Preference<Int> {
        return store.getInt("last_listing_$sourceId", 0)
    }

    fun lastRemoteCheck(): Preference<Long> {
        return store.getLong("last_remote_check", 0)
    }

    fun pinnedCatalogs(): Preference<Set<String>> {
        return store.getStringSet("pinned_catalogs", setOf())
    }

}
