package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Permissions that plugins can request
 * Requirements: 10.1, 10.2, 10.3
 */
@Serializable
enum class PluginPermission {
    /**
     * Access to network for API calls
     */
    NETWORK,
    
    /**
     * Access to local storage
     */
    STORAGE,
    
    /**
     * Access to current reading context (book, chapter, position)
     */
    READER_CONTEXT,
    
    /**
     * Access to user's library
     */
    LIBRARY_ACCESS,
    
    /**
     * Access to app preferences
     */
    PREFERENCES,
    
    /**
     * Ability to show notifications
     */
    NOTIFICATIONS
}
