package ireader.domain.models.entities

/**
 * Extension properties and functions for Catalog entities
 */

/**
 * Minimum supported version code for catalogs
 */
private const val MIN_SUPPORTED_VERSION_CODE = 1

/**
 * Check if the installed catalog is obsolete (too old to use)
 */
val CatalogInstalled.isObsolete: Boolean
    get() = versionCode < MIN_SUPPORTED_VERSION_CODE

/**
 * Check if the installed catalog needs an update
 * This checks if hasUpdate flag is set
 */
val CatalogInstalled.needsUpdate: Boolean
    get() = hasUpdate

/**
 * Check if the catalog is usable (not obsolete and has a valid source)
 */
val CatalogInstalled.isUsable: Boolean
    get() = !isObsolete && source != null

/**
 * Get version display string (e.g., "1.2.3")
 */
val CatalogInstalled.versionDisplay: String
    get() = versionName.ifBlank { "v$versionCode" }

/**
 * Check if this catalog version is newer than another version code
 */
fun CatalogInstalled.isNewerThan(otherVersionCode: Int): Boolean {
    return this.versionCode > otherVersionCode
}

/**
 * Check if this catalog version is older than another version code
 */
fun CatalogInstalled.isOlderThan(otherVersionCode: Int): Boolean {
    return this.versionCode < otherVersionCode
}

/**
 * Compare version with another catalog
 */
fun CatalogInstalled.compareVersionWith(other: CatalogInstalled): Int {
    return this.versionCode.compareTo(other.versionCode)
}

/**
 * Check if catalog is from a specific repository type
 */
fun CatalogRemote.isFromRepository(repoType: String): Boolean {
    return repositoryType.equals(repoType, ignoreCase = true)
}

/**
 * Check if any catalog is valid for migration
 */
val CatalogLocal.isValidForMigration: Boolean
    get() = when (this) {
        is CatalogInstalled -> isUsable
        is CatalogBundled -> source != null
        else -> source != null
    }
