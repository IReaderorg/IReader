package ireader.domain.usecases.tracking

/**
 * Desktop implementation of OAuth callback handler.
 * Desktop doesn't use deep links for OAuth, so this is a no-op.
 */
actual class OAuthCallbackHandler {
    
    actual fun consumeAniListToken(): String? = null
    
    actual fun hasPendingAniListToken(): Boolean = false
}
