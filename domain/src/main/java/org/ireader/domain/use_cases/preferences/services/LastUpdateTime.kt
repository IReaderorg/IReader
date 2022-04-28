package org.ireader.domain.use_cases.preferences.services

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class LastUpdateTime @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(time: Long) {
        appPreferences.lastUpdateCheck().set(time)
    }

    suspend fun read(): Long {
        return appPreferences.lastUpdateCheck().get()
    }
}
