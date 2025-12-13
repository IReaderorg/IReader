package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Status of a plugin in the system
 * Requirements: 14.1, 14.2, 14.3
 */
@Serializable
enum class PluginStatus {
    /**
     * Plugin is not installed (available in marketplace)
     */
    NOT_INSTALLED,

    /**
     * Plugin is loaded and active
     */
    ENABLED,

    /**
     * Plugin is installed but not active
     */
    DISABLED,

    /**
     * Plugin encountered an error
     */
    ERROR,

    /**
     * Plugin is being updated
     */
    UPDATING
}
