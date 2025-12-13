package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Permissions that plugins can request.
 * Plugins must declare required permissions in their manifest.
 * Users will be prompted to grant permissions when installing plugins.
 */
@Serializable
enum class PluginPermission {
    /**
     * Access to network for API calls.
     * Required for plugins that need to make HTTP requests.
     */
    NETWORK,
    
    /**
     * Access to local storage.
     * Required for plugins that need to store files.
     */
    STORAGE,
    
    /**
     * Access to current reading context (book, chapter, position).
     * Required for plugins that interact with the reader.
     */
    READER_CONTEXT,
    
    /**
     * Access to user's library.
     * Required for plugins that need to read library data.
     */
    LIBRARY_ACCESS,
    
    /**
     * Access to app preferences.
     * Required for plugins that need to read/write preferences.
     */
    PREFERENCES,
    
    /**
     * Ability to show notifications.
     * Required for plugins that display notifications.
     */
    NOTIFICATIONS
}
