package org.ireader.domain.catalog.service

import org.ireader.domain.models.entities.CatalogInstalled
import org.ireader.domain.models.entities.CatalogLocal

interface CatalogLoader {

    fun loadAll(): List<CatalogLocal>

    fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally?

    fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide?

}
