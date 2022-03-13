package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.layouts
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

