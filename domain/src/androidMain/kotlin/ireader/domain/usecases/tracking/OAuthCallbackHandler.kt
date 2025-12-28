package ireader.domain.usecases.tracking

import kotlin.concurrent.Volatile

/**
 * Android implementation of OAuth callback handler.
 * Checks for pending tokens stored by MainActivity from deep link callbacks.
 */
actual class OAuthCallbackHandler {
    
    actual fun consumeAniListToken(): String? {
        val token = pendingAniListToken
        pendingAniListToken = null
        return token
    }
    
    actual fun hasPendingAniListToken(): Boolean {
        return pendingAniListToken != null
    }
    
    companion object {
        /**
         * Pending AniList OAuth token from deep link callback.
         * Set by MainActivity when handling ireader://anilist-auth callback.
         */
        @Volatile
        var pendingAniListToken: String? = null
    }
}
