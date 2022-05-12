package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.common_models.DisplayMode
import org.ireader.common_models.layouts
import org.ireader.core_ui.preferences.AppPreferences

class LibraryLayoutTypeUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(layoutIndex: Int) {
        appPreferences.libraryLayoutType().set(layoutIndex)
    }

    suspend fun read(): DisplayMode {
        return layouts[appPreferences.libraryLayoutType().read()]
    }
}

class BrowseLayoutTypeUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(layoutIndex: Int) {
        appPreferences.exploreLayoutType().set(layoutIndex)
    }

    suspend fun read(): DisplayMode {
        return layouts[appPreferences.exploreLayoutType().get()]
    }
}

data class BrowseScreenPrefUseCase(
    val browseLayoutTypeUseCase: BrowseLayoutTypeUseCase
)
