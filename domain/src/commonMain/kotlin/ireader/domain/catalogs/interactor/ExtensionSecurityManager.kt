package ireader.domain.catalogs.interactor

import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.ExtensionSecurity
import ireader.domain.models.entities.ExtensionTrustLevel

/**
 * Handles extension security verification and trust management
 */
interface ExtensionSecurityManager {
    
    /**
     * Perform comprehensive security scan on extension
     */
    suspend fun scanExtension(catalog: Catalog): ExtensionSecurity
    
    /**
     * Verify extension signature
     */
    suspend fun verifySignature(catalog: Catalog): Boolean
    
    /**
     * Analyze extension permissions
     */
    suspend fun analyzePermissions(catalog: Catalog): List<String>
    
    /**
     * Check for malware indicators
     */
    suspend fun checkForMalware(catalog: Catalog): List<String>
    
    /**
     * Get trust level for extension
     */
    suspend fun getTrustLevel(catalog: Catalog): ExtensionTrustLevel
    
    /**
     * Set trust level for extension
     */
    suspend fun setTrustLevel(extensionId: Long, trustLevel: ExtensionTrustLevel): Result<Unit>
    
    /**
     * Check if extension is trusted
     */
    suspend fun isTrusted(extensionId: Long): Boolean
    
    /**
     * Get security warnings for extension
     */
    suspend fun getSecurityWarnings(catalog: Catalog): List<String>
}
