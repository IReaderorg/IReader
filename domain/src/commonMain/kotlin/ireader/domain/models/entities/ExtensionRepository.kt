package ireader.domain.models.entities

/**
 * Represents an extension repository
 */
data class ExtensionRepository(
    val id: Long = 0,
    val name: String,
    val url: String,
    val fingerprint: String?,
    val enabled: Boolean = true,
    val autoUpdate: Boolean = true,
    val trustLevel: ExtensionTrustLevel = ExtensionTrustLevel.VERIFIED,
    val lastSync: Long = 0,
    val extensionCount: Int = 0,
)
