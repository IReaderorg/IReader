package ireader.data.catalog

import ireader.core.log.Log
import ireader.domain.catalogs.interactor.ExtensionSecurityManager
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.ExtensionSecurity
import ireader.domain.models.entities.ExtensionTrustLevel

/**
 * Implementation of extension security management
 */
class ExtensionSecurityManagerImpl(
    private val log: Log
) : ExtensionSecurityManager {
    
    private val trustedExtensions = mutableSetOf<Long>()
    private val blockedExtensions = mutableSetOf<Long>()
    
    override suspend fun scanExtension(catalog: Catalog): ExtensionSecurity {
        log.debug("Scanning extension: ${catalog.name}")
        
        val trustLevel = getTrustLevel(catalog)
        val permissions = analyzePermissions(catalog)
        val warnings = getSecurityWarnings(catalog)
        val signatureHash = if (catalog is CatalogInstalled) {
            catalog.pkgName.hashCode().toString()
        } else null
        
        return ExtensionSecurity(
            trustLevel = trustLevel,
            signatureHash = signatureHash,
            permissions = permissions,
            hasNetworkAccess = permissions.contains("INTERNET"),
            hasStorageAccess = permissions.any { it.contains("STORAGE") },
            securityWarnings = warnings,
            lastSecurityCheck = System.currentTimeMillis()
        )
    }
    
    override suspend fun verifySignature(catalog: Catalog): Boolean {
        // In a real implementation, this would verify APK signature
        // For now, we'll do basic validation
        return when (catalog) {
            is CatalogInstalled -> {
                // Check if package name follows expected pattern
                catalog.pkgName.startsWith("ireader.extension") ||
                catalog.pkgName.startsWith("eu.kanade.tachiyomi.extension")
            }
            else -> false
        }
    }
    
    override suspend fun analyzePermissions(catalog: Catalog): List<String> {
        // In a real implementation, this would parse AndroidManifest.xml
        // For now, return common permissions
        return listOf(
            "INTERNET",
            "ACCESS_NETWORK_STATE"
        )
    }
    
    override suspend fun checkForMalware(catalog: Catalog): List<String> {
        val warnings = mutableListOf<String>()
        
        // Basic malware checks
        if (catalog is CatalogInstalled) {
            // Check for suspicious package names
            if (!catalog.pkgName.startsWith("ireader.extension") &&
                !catalog.pkgName.startsWith("eu.kanade.tachiyomi.extension")) {
                warnings.add("Suspicious package name")
            }
        }
        
        return warnings
    }
    
    override suspend fun getTrustLevel(catalog: Catalog): ExtensionTrustLevel {
        val extensionId = catalog.sourceId
        
        return when {
            blockedExtensions.contains(extensionId) -> ExtensionTrustLevel.BLOCKED
            trustedExtensions.contains(extensionId) -> ExtensionTrustLevel.TRUSTED
            verifySignature(catalog) -> ExtensionTrustLevel.VERIFIED
            else -> ExtensionTrustLevel.UNTRUSTED
        }
    }
    
    override suspend fun setTrustLevel(extensionId: Long, trustLevel: ExtensionTrustLevel): Result<Unit> {
        return try {
            when (trustLevel) {
                ExtensionTrustLevel.TRUSTED -> {
                    trustedExtensions.add(extensionId)
                    blockedExtensions.remove(extensionId)
                }
                ExtensionTrustLevel.BLOCKED -> {
                    blockedExtensions.add(extensionId)
                    trustedExtensions.remove(extensionId)
                }
                else -> {
                    trustedExtensions.remove(extensionId)
                    blockedExtensions.remove(extensionId)
                }
            }
            log.info("Set trust level for extension $extensionId to $trustLevel")
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to set trust level", e)
            Result.failure(e)
        }
    }
    
    override suspend fun isTrusted(extensionId: Long): Boolean {
        return trustedExtensions.contains(extensionId)
    }
    
    override suspend fun getSecurityWarnings(catalog: Catalog): List<String> {
        val warnings = mutableListOf<String>()
        
        // Check trust level
        val trustLevel = getTrustLevel(catalog)
        when (trustLevel) {
            ExtensionTrustLevel.UNTRUSTED -> {
                warnings.add("Extension signature could not be verified")
            }
            ExtensionTrustLevel.BLOCKED -> {
                warnings.add("Extension has been blocked due to security concerns")
            }
            else -> {}
        }
        
        // Add malware warnings
        warnings.addAll(checkForMalware(catalog))
        
        return warnings
    }
}
