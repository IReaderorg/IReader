package org.ireader.core_catalogs.service

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_catalogs.model.InstallStep

interface CatalogInstaller {

    fun install(catalog: CatalogRemote, onError: (Throwable) -> Unit = {}): Flow<InstallStep>

    suspend fun uninstall(pkgName: String, onError: (Throwable) -> Unit = {}): Boolean
}
