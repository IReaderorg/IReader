package ireader.domain.use_cases.preferences.reader_preferences

import ireader.common.data.repository.CategoryRepository
import ireader.common.models.DisplayMode
import ireader.common.models.DisplayMode.Companion.set
import ireader.common.models.entities.Category
import ireader.core.ui.preferences.AppPreferences
import ireader.core.ui.preferences.LibraryPreferences

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
