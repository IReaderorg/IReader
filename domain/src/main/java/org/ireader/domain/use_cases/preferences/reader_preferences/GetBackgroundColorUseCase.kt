package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class GetBackgroundColorUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Int {
        return appPreferences.backgroundColorIndex().get()
    }
}

class SetBackgroundColorUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(index: Int) {
        appPreferences.backgroundColorIndex().set(index)
    }
}