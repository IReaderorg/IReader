package ireader.domain.models.entities

/**
 * Security information for an extension
 */
data class ExtensionSecurity(
    val trustLevel: ExtensionTrustLevel,
    val signatureHash: String?,
    val permissions: List<String>,
    val hasNetworkAccess: Boolean,
    val hasStorageAccess: Boolean,
    val securityWarnings: List<String>,
    val lastSecurityCheck: Long,
)
