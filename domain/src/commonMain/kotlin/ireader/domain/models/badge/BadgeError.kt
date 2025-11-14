package ireader.domain.models.badge

/**
 * Sealed class representing all possible errors that can occur in badge operations.
 */
sealed class BadgeError : Exception() {
    
    /**
     * Error when an invalid Ethereum wallet address is provided.
     */
    object InvalidWalletAddress : BadgeError() {
        override val message: String = "Invalid wallet address format"
    }
    
    /**
     * Error when payment proof is required but not provided.
     */
    object PaymentProofRequired : BadgeError() {
        override val message: String = "Payment proof is required"
    }
    
    /**
     * Error when user tries to purchase a badge they already own.
     */
    object BadgeAlreadyOwned : BadgeError() {
        override val message: String = "Badge already owned"
    }
    
    /**
     * Error when a network operation fails.
     */
    data class NetworkError(override val message: String = "Network error occurred") : BadgeError()
    
    /**
     * Error when NFT verification fails.
     */
    data class VerificationFailed(override val message: String = "Verification failed") : BadgeError()
    
    /**
     * Error when user doesn't have permission for an operation.
     */
    object InsufficientPermissions : BadgeError() {
        override val message: String = "Insufficient permissions"
    }
    
    /**
     * Error when a server error occurs.
     */
    data class ServerError(override val message: String = "Server error occurred") : BadgeError()
    
    /**
     * Error when user tries to select more than 3 featured badges.
     */
    object MaxFeaturedBadgesExceeded : BadgeError() {
        override val message: String = "Maximum 3 featured badges allowed"
    }
    
    /**
     * Error when a badge is not found.
     */
    data class BadgeNotFound(val badgeId: String) : BadgeError() {
        override val message: String = "Badge not found: $badgeId"
    }
    
    /**
     * Error when an invalid badge selection is made.
     */
    data class InvalidBadgeSelection(override val message: String = "Invalid badge selection") : BadgeError()
}
