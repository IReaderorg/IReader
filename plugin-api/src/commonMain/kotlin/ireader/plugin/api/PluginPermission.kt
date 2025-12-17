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
    NOTIFICATIONS,
    
    /**
     * Can add/modify catalog sources.
     * Required for catalog plugins that manage content sources.
     */
    CATALOG_WRITE,
    
    /**
     * Can sync user data (reading progress, library, settings).
     * Required for sync plugins.
     */
    SYNC_DATA,
    
    /**
     * Can run background services.
     * Required for plugins that need to run in background (sync, downloads).
     */
    BACKGROUND_SERVICE,
    
    /**
     * Can connect to local servers (localhost, LAN).
     * Required for plugins connecting to local TTS, upscaling, or sync servers.
     */
    LOCAL_SERVER,
    
    /**
     * Can process images (upscaling, enhancement).
     * Required for image processing plugins.
     */
    IMAGE_PROCESSING,
    
    /**
     * Can inject custom UI screens and components.
     * Required for community screen plugins.
     */
    UI_INJECTION,
    
    /**
     * Can access and modify glossaries.
     * Required for glossary/dictionary plugins.
     */
    GLOSSARY_ACCESS,
    
    /**
     * Can access character database.
     * Required for character AI and analysis plugins.
     */
    CHARACTER_DATABASE,
    
    /**
     * Can access audio playback system.
     * Required for TTS and audio plugins.
     */
    AUDIO_PLAYBACK,
    
    /**
     * Can access Gradio endpoints.
     * Required for Gradio-based TTS and AI plugins.
     */
    GRADIO_ACCESS
}
