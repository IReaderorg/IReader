

package ireader.domain.catalogs.interactor

import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.models.entities.CatalogInstalled
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow



class UpdateCatalog(
    private val catalogRemoteRepository: CatalogRemoteRepository,
    private val installCatalog: InstallCatalog,
    private val syncRemoteCatalogs: SyncRemoteCatalogs,
) {

    suspend fun await(catalog: CatalogInstalled): Flow<InstallStep> {
        var catalogs = catalogRemoteRepository.getRemoteCatalogs()
        var catalogToUpdate = catalogs.find { it.pkgName == catalog.pkgName }

        // If the remote catalog isn't in our local cache, force a sync before giving up.
        // This previously returned emptyFlow(), which left the install job alive forever
        // (no Success/Error emitted) and the UI spinner never cleared.
        if (catalogToUpdate == null) {
            Log.info("UpdateCatalog: remote catalog for ${catalog.pkgName} not cached, forcing sync")
            try {
                syncRemoteCatalogs.await(forceRefresh = true)
            } catch (e: Exception) {
                Log.warn(e, "UpdateCatalog: forced sync failed")
            }
            catalogs = catalogRemoteRepository.getRemoteCatalogs()
            catalogToUpdate = catalogs.find { it.pkgName == catalog.pkgName }
        }

        return if (catalogToUpdate == null) {
            // Still not found after sync — emit a real error so the UI clears the spinner.
            flow {
                emit(InstallStep.Error("Source not found in remote repository. It may have been removed."))
            }
        } else {
            installCatalog.await(catalogToUpdate)
        }
    }
}
