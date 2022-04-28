

package org.ireader.core_catalogs.interactor

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_catalogs.model.InstallStep
import org.ireader.core_catalogs.service.CatalogInstaller
import javax.inject.Inject

class InstallCatalog @Inject constructor(
    private val catalogInstaller: CatalogInstaller,
) {

    fun await(catalog: CatalogRemote, onError: (Throwable) -> Unit): Flow<InstallStep> {
        return catalogInstaller.install(catalog, onError)
    }
}
