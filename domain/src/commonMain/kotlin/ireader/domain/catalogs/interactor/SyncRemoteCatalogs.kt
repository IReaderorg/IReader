package ireader.domain.catalogs.interactor
import ireader.domain.utils.extensions.ioDispatcher

import ireader.core.log.Log
import ireader.core.util.IO
import ireader.domain.catalogs.CatalogPreferences
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.catalogs.service.CatalogRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class SyncRemoteCatalogs(
    private val catalogRemoteRepository: CatalogRemoteRepository,
    private val catalogRemoteApi: CatalogRemoteApi,
    private val catalogPreferences: CatalogPreferences,
) {

    @OptIn(ExperimentalTime::class)
    suspend fun await(forceRefresh: Boolean, onError: (Throwable) -> Unit = {}): Boolean {
        val lastCheckPref = catalogPreferences.lastRemoteCheck()
        val lastCheck = kotlin.time.Instant.fromEpochMilliseconds(lastCheckPref.get())
        val now = kotlin.time.Clock.System.now()

        if (forceRefresh || now - lastCheck > minTimeApiCheck) {
            try {
                withContext(ioDispatcher) {
                    val newCatalogs = catalogRemoteApi.fetchCatalogs()
                    catalogRemoteRepository.deleteAllRemoteCatalogs()
                    catalogRemoteRepository.insertRemoteCatalogs(newCatalogs)
                    lastCheckPref.set(kotlin.time.Clock.System.now().toEpochMilliseconds())
                }
                return true
            } catch (e: Throwable) {
                onError(e)
                Log.warn(e, "Failed to fetch remote catalogs")
            }
        }

        return false
    }
    internal companion object {
        val minTimeApiCheck = 5.minutes
    }
}
