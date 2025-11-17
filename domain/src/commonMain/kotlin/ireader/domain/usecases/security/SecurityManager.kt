package ireader.domain.usecases.security

import ireader.domain.preferences.prefs.SecurityPreferences
import ireader.domain.preferences.prefs.SecureScreenMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Security manager for app lock and secure screen features
 * Requirements: 11.2, 11.4, 11.5
 */
class SecurityManager(
    private val securityPreferences: SecurityPreferences,
) {
    /**
     * Check if app lock is enabled
     */
    suspend fun isAppLockEnabled(): Boolean {
        return securityPreferences.useAuthenticator().get()
    }

    /**
     * Check if app should be locked based on time elapsed
     */
    suspend fun shouldLockApp(): Boolean {
        if (!isAppLockEnabled()) return false
        
        val lockAfter = securityPreferences.lockAppAfter().get()
        if (lockAfter < 0) return false // Never lock
        if (lockAfter == 0) return true // Always lock
        
        val lastClosed = securityPreferences.lastAppClosed().get()
        if (lastClosed == 0L) return false
        
        val elapsedMinutes = (System.currentTimeMillis() - lastClosed) / (1000 * 60)
        return elapsedMinutes >= lockAfter
    }

    /**
     * Record app close time for timed lock
     */
    suspend fun recordAppClosed() {
        if (isAppLockEnabled()) {
            securityPreferences.lastAppClosed().set(System.currentTimeMillis())
        }
    }

    /**
     * Clear app close time after successful unlock
     */
    suspend fun clearAppClosedTime() {
        securityPreferences.lastAppClosed().set(0L)
    }

    /**
     * Check if secure screen should be enabled
     */
    suspend fun shouldEnableSecureScreen(): Boolean {
        val mode = securityPreferences.secureScreen().get()
        val incognitoMode = securityPreferences.incognitoMode().get()
        val alwaysSecure = securityPreferences.secureScreenAlways().get()
        
        return when {
            alwaysSecure -> true
            mode == SecureScreenMode.ALWAYS -> true
            mode == SecureScreenMode.INCOGNITO && incognitoMode -> true
            else -> false
        }
    }

    /**
     * Observe secure screen state
     */
    fun observeSecureScreenState(): Flow<Boolean> {
        return securityPreferences.secureScreen().changes().map {
            shouldEnableSecureScreen()
        }
    }

    /**
     * Toggle incognito mode
     */
    suspend fun toggleIncognitoMode() {
        val current = securityPreferences.incognitoMode().get()
        securityPreferences.incognitoMode().set(!current)
    }

    /**
     * Check if incognito mode is active
     */
    suspend fun isIncognitoMode(): Boolean {
        return securityPreferences.incognitoMode().get()
    }

    /**
     * Enable app lock with biometric authentication
     */
    suspend fun enableAppLock(lockAfterMinutes: Int = 0) {
        securityPreferences.useAuthenticator().set(true)
        securityPreferences.lockAppAfter().set(lockAfterMinutes)
    }

    /**
     * Disable app lock
     */
    suspend fun disableAppLock() {
        securityPreferences.useAuthenticator().set(false)
        securityPreferences.lastAppClosed().set(0L)
    }

    /**
     * Set secure screen mode
     */
    suspend fun setSecureScreenMode(mode: SecureScreenMode) {
        securityPreferences.secureScreen().set(mode)
    }

    /**
     * Check if notification content should be hidden
     */
    suspend fun shouldHideNotificationContent(): Boolean {
        return securityPreferences.hideNotificationContent().get()
    }
}
