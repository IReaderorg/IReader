package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.DisplayMode.Companion.set
import org.ireader.common_models.entities.Category
import org.ireader.core_ui.preferences.AppPreferences
import org.ireader.core_ui.preferences.LibraryPreferences

class LibraryLayoutTypeUseCase(
    private val libraryPreferences: LibraryPreferences,
    private val categoryRepository: CategoryRepository
) {

    suspend fun await(category: Category, displayMode: DisplayMode) {
        if (libraryPreferences.perCategorySettings().get()) {
            val newCategory = category.set(displayMode)
            categoryRepository.insertOrUpdate(
                newCategory
            )
        } else {
            val flags = libraryPreferences.categoryFlags().set(displayMode)
            categoryRepository.updateAllFlags(flags)
        }
    }
}

class BrowseLayoutTypeUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(mode: DisplayMode) {
        appPreferences.exploreLayoutType().set(mode)
    }

    suspend fun read(): DisplayMode {
        return DisplayMode.getFlag(appPreferences.exploreLayoutType().get()) ?: DisplayMode.ComfortableGrid
    }
}

data class BrowseScreenPrefUseCase(
    val browseLayoutTypeUseCase: BrowseLayoutTypeUseCase
)
