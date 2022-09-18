package ireader.domain.preferences.prefs

import ireader.common.models.entities.Category
import ireader.common.models.library.LibraryFilter
import ireader.common.models.library.LibrarySort
import ireader.common.models.library.deserialize
import ireader.common.models.library.deserializeList
import ireader.common.models.library.serialize
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

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

    fun goToLastChapterBadges(): Preference<Boolean> {
        return preferenceStore.getBoolean("last_chapter_badges", true)
    }

    fun downloadBadges(): Preference<Boolean> {
        return preferenceStore.getBoolean("download_badges", true)
    }

    fun unreadBadges(): Preference<Boolean> {
        return preferenceStore.getBoolean("unread_badges", true)
    }

    fun showCategoryTabs(): Preference<Boolean> {
        return preferenceStore.getBoolean("category_tabs", true)
    }

    fun showAllCategory(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_all_category", true)
    }

    fun showCountInCategory(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_count_in_category", true)
    }

    fun columnsInPortrait(): Preference<Int> {
        return preferenceStore.getInt("columns_portrait", 0)
    }

    fun columnsInLandscape(): Preference<Int> {
        return preferenceStore.getInt("columns_landscape", 0)
    }
}
