package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.layouts
import javax.inject.Inject

class ReadBrowseLayoutTypeStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): DisplayMode {
        return layouts[appPreferences.exploreLayoutType().get()]
    }
}

class SaveLibraryLayoutTypeStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(layoutIndex: Int) {
        appPreferences.libraryLayoutType().set(layoutIndex)
    }
}

class SaveBrowseLayoutTypeStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(layoutIndex: Int) {
        appPreferences.exploreLayoutType().set(layoutIndex)
    }
}

class ReadLibraryLayoutTypeStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): DisplayMode {
        return layouts[appPreferences.libraryLayoutType().get()]
    }
}