package org.ireader.domain.extensions.cataloge_service

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.CatalogRemote
import org.ireader.domain.models.entities.model.InstallStep

interface CatalogInstaller {

    fun install(catalog: CatalogRemote): Flow<InstallStep>

    suspend fun uninstall(pkgName: String): Boolean

}


class InstallCatalog(
    private val catalogInstaller: CatalogInstaller,
) {

    fun await(catalog: CatalogRemote): Flow<InstallStep> {
        return catalogInstaller.install(catalog)
    }

}
