package ireader.domain.usecases.tracking

/**
 * iOS implementation of OAuth callback handler.
 * iOS uses universal links or custom URL schemes handled separately.
 */
actual class OAuthCallbackHandler {
    
    actual fun consumeAniListToken(): String? = null
    
    actual fun hasPendingAniListToken(): Boolean = false
}
