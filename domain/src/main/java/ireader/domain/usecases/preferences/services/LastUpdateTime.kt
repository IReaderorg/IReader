package ireader.domain.usecases.preferences.services

import ireader.domain.preferences.prefs.AppPreferences
import org.koin.core.annotation.Factory

@Factory
class LastUpdateTime(
    private val appPreferences: AppPreferences,
) {
    fun save(time: Long) {
        appPreferences.lastUpdateCheck().set(time)
    }

    suspend fun read(): Long {
        return appPreferences.lastUpdateCheck().get()
    }
}
