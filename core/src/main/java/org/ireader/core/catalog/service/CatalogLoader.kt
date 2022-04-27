package org.ireader.core.catalog.service

import org.ireader.common_models.entities.CatalogInstalled
import org.ireader.common_models.entities.CatalogLocal

interface CatalogLoader {

    fun loadAll(): List<CatalogLocal>

    fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally?

    fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide?

}
