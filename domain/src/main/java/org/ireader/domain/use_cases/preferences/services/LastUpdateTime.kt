package org.ireader.domain.use_cases.preferences.services

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class SetLastUpdateTime @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(time: Long) {
        appPreferences.lastUpdateCheck().set(time)
    }
}

class ReadLastUpdateTime @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Long {
        return appPreferences.lastUpdateCheck().get()
    }
}