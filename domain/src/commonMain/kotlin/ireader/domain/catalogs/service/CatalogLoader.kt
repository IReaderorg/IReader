package ireader.domain.catalogs.service

import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal

interface CatalogLoader {

    suspend fun loadAll(): List<CatalogLocal>

    fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally?

    fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide?

    /**
     * Clear the cached DEX/class data for a specific catalog.
     * This should be called before reloading a catalog that has been reinstalled
     * to ensure the new version is loaded instead of the cached one.
     * 
     * @param pkgName The package name of the catalog to clear cache for
     */
    fun clearCatalogCache(pkgName: String) {
        // Default no-op implementation for platforms that don't need cache clearing
    }
}
