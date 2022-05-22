package org.ireader.core_ui.preferences

import org.ireader.common_models.entities.Category
import org.ireader.common_models.library.LibraryFilter
import org.ireader.common_models.library.LibrarySort
import org.ireader.common_models.library.deserialize
import org.ireader.common_models.library.deserializeList
import org.ireader.common_models.library.serialize
import org.ireader.core_api.prefs.Preference
import org.ireader.core_api.prefs.PreferenceStore

class LibraryPreferences(private val preferenceStore: PreferenceStore) {

    fun sorting(): Preference<LibrarySort> {
        return preferenceStore.getObject(
            key = "sorting",
            defaultValue = LibrarySort.default,
            serializer = { it.serialize() },
            deserializer = { LibrarySort.deserialize(it) }
        )
    }

    fun filters(includeAll: Boolean = false): Preference<List<LibraryFilter>> {
        return preferenceStore.getObject(
            key = "filters",
            defaultValue = LibraryFilter.getDefault(includeAll),
            serializer = { it.serialize() },
            deserializer = { LibraryFilter.deserializeList(it, includeAll) }
        )
    }

    fun lastUsedCategory(): Preference<Long> {
        return preferenceStore.getLong("last_used_category", Category.ALL_ID)
    }

    fun defaultCategory(): Preference<Long> {
        return preferenceStore.getLong("default_category", Category.UNCATEGORIZED_ID)
    }

    fun categoryFlags(): Preference<Long> {
        return preferenceStore.getLong("category_flags", 0L)
    }

    fun perCategorySettings(): Preference<Boolean> {
        return preferenceStore.getBoolean("per_category_settings", false)
    }

    fun downloadBadges(): Preference<Boolean> {
        return preferenceStore.getBoolean("download_badges", false)
    }

    fun unreadBadges(): Preference<Boolean> {
        return preferenceStore.getBoolean("unread_badges", true)
    }

    fun showCategoryTabs(): Preference<Boolean> {
        return preferenceStore.getBoolean("category_tabs", true)
    }

    fun showAllCategory(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_all_category", false)
    }

    fun showCountInCategory(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_count_in_category", false)
    }

    fun columnsInPortrait(): Preference<Int> {
        return preferenceStore.getInt("columns_portrait", 0)
    }

    fun columnsInLandscape(): Preference<Int> {
        return preferenceStore.getInt("columns_landscape", 0)
    }

}
