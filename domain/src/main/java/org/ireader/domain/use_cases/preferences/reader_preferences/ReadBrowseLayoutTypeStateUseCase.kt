package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.common_models.DisplayMode
import org.ireader.common_models.layouts
import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject


class LibraryLayoutTypeUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(layoutIndex: Int) {
        appPreferences.libraryLayoutType().set(layoutIndex)
    }

    fun read(): DisplayMode {
        return layouts[appPreferences.libraryLayoutType().get()]
    }
}

class BrowseLayoutTypeUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(layoutIndex: Int) {
        appPreferences.exploreLayoutType().set(layoutIndex)
    }

    fun read(): DisplayMode {
        return layouts[appPreferences.exploreLayoutType().get()]
    }
}

