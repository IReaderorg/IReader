package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.layouts

class ReadBrowseLayoutTypeStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): DisplayMode {
        return layouts[appPreferences.exploreLayoutType().get()]
    }
}

class SaveLibraryLayoutTypeStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(layoutIndex: Int) {
        appPreferences.libraryLayoutType().set(layoutIndex)
    }
}

class SaveBrowseLayoutTypeStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(layoutIndex: Int) {
        appPreferences.exploreLayoutType().set(layoutIndex)
    }
}

class ReadLibraryLayoutTypeStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): DisplayMode {
        return layouts[appPreferences.libraryLayoutType().get()]
    }
}