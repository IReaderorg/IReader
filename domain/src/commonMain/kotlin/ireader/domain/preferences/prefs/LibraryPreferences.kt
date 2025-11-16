package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.entities.Category
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.domain.models.library.deserialize
import ireader.domain.models.library.deserializeList
import ireader.domain.models.library.serialize

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
        return preferenceStore.getLong("category_flags", 0b0011L)
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
        return preferenceStore.getInt("columns_portrait", 3)
    }

    fun columnsInLandscape(): Preference<Int> {
        return preferenceStore.getInt("columns_landscape", 4)
    }
    fun columnsInPortraitCompact(): Preference<Int> {
        return preferenceStore.getInt("columns_portrait_compact", 2)
    }

    // Badge preferences
    fun showDownloadedChaptersBadge(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_downloaded_chapters_badge", true)
    }

    fun showUnreadChaptersBadge(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_unread_chapters_badge", true)
    }

    fun showLocalMangaBadge(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_local_manga_badge", false)
    }

    fun showLanguageBadge(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_language_badge", false)
    }

    fun showEmptyCategories(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_empty_categories", false)
    }

    fun showSmartCategories(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_smart_categories", false)
    }

    fun showArchivedBooks(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_archived_books", false)
    }

    fun showResumeReadingCard(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_resume_reading_card", false)
    }
}
