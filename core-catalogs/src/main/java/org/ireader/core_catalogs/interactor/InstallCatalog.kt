

package org.ireader.core_catalogs.interactor

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_api.os.InstallStep
import org.ireader.core_catalogs.service.CatalogInstaller

class InstallCatalog(
    private val catalogInstaller: CatalogInstaller,
) {

    fun await(catalog: CatalogRemote): Flow<InstallStep> {
        return catalogInstaller.install(catalog)
    }
}
