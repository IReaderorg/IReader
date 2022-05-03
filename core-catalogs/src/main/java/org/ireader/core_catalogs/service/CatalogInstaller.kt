package org.ireader.core_catalogs.service

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_api.os.InstallStep

interface CatalogInstaller {

    fun install(catalog: CatalogRemote): Flow<InstallStep>

    suspend fun uninstall(pkgName: String): InstallStep
}
