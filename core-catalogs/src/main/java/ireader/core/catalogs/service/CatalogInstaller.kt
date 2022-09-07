package ireader.core.catalogs.service

import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.CatalogRemote
import ireader.core.api.os.InstallStep

interface CatalogInstaller {

    fun install(catalog: CatalogRemote): Flow<InstallStep>

    suspend fun uninstall(pkgName: String): InstallStep
}
