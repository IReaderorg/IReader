package ireader.domain.usecases.tracking

/**
 * Platform-specific handler for OAuth callbacks.
 * On Android: Checks for pending tokens from deep link callbacks
 * On Desktop/iOS: No-op (OAuth handled differently)
 */
expect class OAuthCallbackHandler {
    /**
     * Check if there's a pending AniList OAuth token from a deep link callback.
     * Returns the token and clears it, or null if no pending token.
     */
    fun consumeAniListToken(): String?
    
    /**
     * Check if there's a pending token without consuming it.
     */
    fun hasPendingAniListToken(): Boolean
}
