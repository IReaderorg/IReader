package ireader.domain.usecases.preferences.reader_preferences

import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category
import ireader.domain.models.DisplayMode
import ireader.domain.models.DisplayMode.Companion.set
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.LibraryPreferences

class LibraryLayoutTypeUseCase(
    private val libraryPreferences: LibraryPreferences,
    private val categoryRepository: CategoryRepository
) {

    suspend fun await(category: Category, displayMode: DisplayMode) {
        if (libraryPreferences.perCategorySettings().get()) {
            val newCategory = category.set(displayMode)
            categoryRepository.insert(
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
