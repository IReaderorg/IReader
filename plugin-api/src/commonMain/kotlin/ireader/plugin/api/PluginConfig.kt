package ireader.plugin.api

/**
 * Plugin configuration system - similar to Filter system for sources.
 * Plugins define their configuration fields using these sealed classes,
 * and the app automatically generates the UI.
 * 
 * ## Usage Example
 * ```kotlin
 * class MyPlugin : TranslationPlugin {
 *     override fun getConfigFields(): List<PluginConfig<*>> = listOf(
 *         PluginConfig.Text(
 *             key = "server_url",
 *             name = "Server URL",
 *             defaultValue = "http://localhost:8080",
 *             description = "The URL of your server"
 *         ),
 *         PluginConfig.Password(
 *             key = "api_key",
 *             name = "API Key",
 *             defaultValue = "",
 *             description = "Your API key"
 *         ),
 *         PluginConfig.Select(
 *             key = "model",
 *             name = "Model",
 *             options = listOf("gpt-4", "gpt-3.5-turbo", "claude-3"),
 *             defaultValue = 0
 *         )
 *     )
 * }
 * ```
 */
sealed class PluginConfig<V>(
    /** Unique key for storing this config value */
    open val key: String,
    /** Display name shown in UI */
    open val name: String,
    /** Default value */
    open val defaultValue: V,
    /** Optional description/help text */
    open val description: String? = null,
    /** Whether this field is required */
    open val required: Boolean = false
) {
    /** Current value - mutable for UI binding */
    var value: V = defaultValue
    
    /** Check if value has been modified from default */
    open fun isModified(): Boolean = value != defaultValue
    
    /** Reset to default value */
    fun reset() { value = defaultValue }
    
    /** Validate the current value */
    open fun isValid(): Boolean = true
    
    // ==================== Text Input Fields ====================
    
    /**
     * Simple text input field
     */
    data class Text(
        override val key: String,
        override val name: String,
        override val defaultValue: String = "",
        override val description: String? = null,
        override val required: Boolean = false,
        /** Placeholder text */
        val placeholder: String? = null,
        /** Maximum character length */
        val maxLength: Int = 500,
        /** Input type hint (url, email, number, etc.) */
        val inputType: TextInputType = TextInputType.TEXT
    ) : PluginConfig<String>(key, name, defaultValue, description, required) {
        override fun isValid(): Boolean = !required || value.isNotBlank()
    }
    
    /**
     * Password/secret input field (masked)
     */
    data class Password(
        override val key: String,
        override val name: String,
        override val defaultValue: String = "",
        override val description: String? = null,
        override val required: Boolean = false,
        val placeholder: String? = null
    ) : PluginConfig<String>(key, name, defaultValue, description, required) {
        override fun isValid(): Boolean = !required || value.isNotBlank()
    }
    
    /**
     * Multi-line text area
     */
    data class TextArea(
        override val key: String,
        override val name: String,
        override val defaultValue: String = "",
        override val description: String? = null,
        override val required: Boolean = false,
        val placeholder: String? = null,
        val maxLines: Int = 5
    ) : PluginConfig<String>(key, name, defaultValue, description, required)
    
    // ==================== Selection Fields ====================
    
    /**
     * Dropdown/select field
     */
    data class Select(
        override val key: String,
        override val name: String,
        /** List of option display names */
        val options: List<String>,
        /** Index of default selected option */
        override val defaultValue: Int = 0,
        override val description: String? = null,
        override val required: Boolean = false
    ) : PluginConfig<Int>(key, name, defaultValue, description, required) {
        override fun isValid(): Boolean = value in options.indices
        
        /** Get currently selected option text */
        fun getSelectedOption(): String? = options.getOrNull(value)
    }
    
    /**
     * Dropdown with key-value pairs (display name -> actual value)
     */
    data class SelectWithValues(
        override val key: String,
        override val name: String,
        /** List of (displayName, actualValue) pairs */
        val options: List<Pair<String, String>>,
        override val defaultValue: Int = 0,
        override val description: String? = null,
        override val required: Boolean = false
    ) : PluginConfig<Int>(key, name, defaultValue, description, required) {
        override fun isValid(): Boolean = value in options.indices
        
        fun getSelectedDisplayName(): String? = options.getOrNull(value)?.first
        fun getSelectedValue(): String? = options.getOrNull(value)?.second
    }
    
    // ==================== Toggle Fields ====================
    
    /**
     * Boolean toggle/switch
     */
    data class Toggle(
        override val key: String,
        override val name: String,
        override val defaultValue: Boolean = false,
        override val description: String? = null,
        override val required: Boolean = false
    ) : PluginConfig<Boolean>(key, name, defaultValue, description, required)
    
    /**
     * Tri-state checkbox (true/false/null)
     */
    data class TriState(
        override val key: String,
        override val name: String,
        override val defaultValue: Boolean? = null,
        override val description: String? = null,
        override val required: Boolean = false
    ) : PluginConfig<Boolean?>(key, name, defaultValue, description, required)
    
    // ==================== Numeric Fields ====================
    
    /**
     * Integer number input
     */
    data class Number(
        override val key: String,
        override val name: String,
        override val defaultValue: Int = 0,
        override val description: String? = null,
        override val required: Boolean = false,
        val min: Int? = null,
        val max: Int? = null,
        val step: Int = 1
    ) : PluginConfig<Int>(key, name, defaultValue, description, required) {
        override fun isValid(): Boolean {
            if (min != null && value < min) return false
            if (max != null && value > max) return false
            return true
        }
    }
    
    /**
     * Decimal number input
     */
    data class Decimal(
        override val key: String,
        override val name: String,
        override val defaultValue: Float = 0f,
        override val description: String? = null,
        override val required: Boolean = false,
        val min: Float? = null,
        val max: Float? = null
    ) : PluginConfig<Float>(key, name, defaultValue, description, required) {
        override fun isValid(): Boolean {
            if (min != null && value < min) return false
            if (max != null && value > max) return false
            return true
        }
    }
    
    /**
     * Slider for numeric range
     */
    data class Slider(
        override val key: String,
        override val name: String,
        override val defaultValue: Float = 0.5f,
        override val description: String? = null,
        override val required: Boolean = false,
        val min: Float = 0f,
        val max: Float = 1f,
        val steps: Int = 0,
        /** Format string for displaying value (e.g., "%.1f", "%d%%") */
        val valueFormat: String? = null
    ) : PluginConfig<Float>(key, name, defaultValue, description, required)
    
    // ==================== Special Fields ====================
    
    /**
     * Informational note (no input, just displays text)
     */
    data class Note(
        override val key: String,
        override val name: String,
        override val description: String? = null,
        val noteType: NoteType = NoteType.INFO
    ) : PluginConfig<Unit>(key, name, Unit, description, false)
    
    /**
     * Clickable link
     */
    data class Link(
        override val key: String,
        override val name: String,
        val url: String,
        override val description: String? = null,
        val linkType: LinkType = LinkType.EXTERNAL
    ) : PluginConfig<Unit>(key, name, Unit, description, false)
    
    /**
     * Action button (triggers callback)
     */
    data class Action(
        override val key: String,
        override val name: String,
        override val description: String? = null,
        val buttonText: String? = null,
        val actionType: ActionType = ActionType.DEFAULT
    ) : PluginConfig<Unit>(key, name, Unit, description, false)
    
    /**
     * Group of related config fields
     */
    data class Group(
        override val key: String,
        override val name: String,
        val fields: List<PluginConfig<*>>,
        override val description: String? = null,
        val collapsible: Boolean = true,
        val initiallyExpanded: Boolean = true
    ) : PluginConfig<Unit>(key, name, Unit, description, false)
    
    /**
     * Header/separator for organizing fields
     */
    data class Header(
        override val key: String,
        override val name: String,
        override val description: String? = null
    ) : PluginConfig<Unit>(key, name, Unit, description, false)
    
    /**
     * Divider line
     */
    data class Divider(
        override val key: String = "divider"
    ) : PluginConfig<Unit>(key, "", Unit, null, false)
}

/**
 * Text input type hints for keyboard optimization
 */
enum class TextInputType {
    TEXT,
    URL,
    EMAIL,
    NUMBER,
    PHONE,
    PASSWORD
}

/**
 * Note display types
 */
enum class NoteType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS,
    TIP
}

/**
 * Link types
 */
enum class LinkType {
    EXTERNAL,      // Opens in browser
    INTERNAL,      // In-app navigation
    MARKETPLACE,   // App store/marketplace
    DOCUMENTATION  // Help/docs
}

/**
 * Action button types
 */
enum class ActionType {
    DEFAULT,
    PRIMARY,
    SECONDARY,
    DANGER,
    TEST_CONNECTION
}
