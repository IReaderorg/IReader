package ireader.domain.models.entities

/**
 * Trust level for extensions based on signature verification
 */
enum class ExtensionTrustLevel {
    /**
     * Extension is from official repository with verified signature
     */
    TRUSTED,
    
    /**
     * Extension signature is valid but from unknown source
     */
    VERIFIED,
    
    /**
     * Extension has no signature or invalid signature
     */
    UNTRUSTED,
    
    /**
     * Extension failed security checks
     */
    BLOCKED
}
