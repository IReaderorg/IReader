package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Declarative UI definition for plugins.
 * Plugins define their UI using these data classes, and the app renders them.
 * This avoids the complexity of loading Compose code from plugins at runtime.
 */

/**
 * A complete screen definition that the app can render.
 */
@Serializable
data class PluginUIScreen(
    val id: String,
    val title: String,
    val components: List<PluginUIComponent>
)

/**
 * Base sealed class for UI components that plugins can use.
 */
@Serializable
sealed class PluginUIComponent {
    
    @Serializable
    data class Text(
        val text: String,
        val style: TextStyle = TextStyle.BODY
    ) : PluginUIComponent()
    
    @Serializable
    data class TextField(
        val id: String,
        val label: String,
        val value: String = "",
        val multiline: Boolean = false,
        val maxLines: Int = 1
    ) : PluginUIComponent()
    
    @Serializable
    data class Button(
        val id: String,
        val label: String,
        val style: ButtonStyle = ButtonStyle.PRIMARY,
        val icon: String? = null
    ) : PluginUIComponent()
    
    @Serializable
    data class Card(
        val children: kotlin.collections.List<PluginUIComponent>
    ) : PluginUIComponent()
    
    @Serializable
    data class Row(
        val children: kotlin.collections.List<PluginUIComponent>,
        val spacing: Int = 8
    ) : PluginUIComponent()
    
    @Serializable
    data class Column(
        val children: kotlin.collections.List<PluginUIComponent>,
        val spacing: Int = 8
    ) : PluginUIComponent()
    
    @Serializable
    data class ItemList(
        val id: String,
        val items: kotlin.collections.List<ListItem>
    ) : PluginUIComponent()
    
    @Serializable
    data class Tabs(
        val tabs: kotlin.collections.List<Tab>
    ) : PluginUIComponent()
    
    @Serializable
    data class Switch(
        val id: String,
        val label: String,
        val checked: Boolean = false
    ) : PluginUIComponent()
    
    @Serializable
    data class Chip(
        val id: String,
        val label: String,
        val selected: Boolean = false
    ) : PluginUIComponent()
    
    @Serializable
    data class ChipGroup(
        val id: String,
        val chips: kotlin.collections.List<Chip>,
        val singleSelection: Boolean = true
    ) : PluginUIComponent()
    
    @Serializable
    data class Loading(
        val message: String? = null
    ) : PluginUIComponent()
    
    @Serializable
    data class Empty(
        val icon: String? = null,
        val message: String,
        val description: String? = null
    ) : PluginUIComponent()
    
    @Serializable
    data class Error(
        val message: String
    ) : PluginUIComponent()
    
    @Serializable
    data class Spacer(
        val height: Int = 16
    ) : PluginUIComponent()
    
    @Serializable
    data class Divider(
        val thickness: Int = 1
    ) : PluginUIComponent()
    
    @Serializable
    data class ProgressBar(
        val progress: Float = 0f,
        val label: String? = null
    ) : PluginUIComponent()
    
    @Serializable
    data class Image(
        val url: String,
        val width: Int? = null,
        val height: Int? = null,
        val contentDescription: String? = null
    ) : PluginUIComponent()
}

@Serializable
data class ListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: String? = null,
    val trailing: String? = null
)

@Serializable
data class Tab(
    val id: String,
    val title: String,
    val icon: String? = null,
    val content: List<PluginUIComponent>
)

@Serializable
enum class TextStyle {
    TITLE_LARGE,
    TITLE_MEDIUM,
    TITLE_SMALL,
    BODY,
    BODY_SMALL,
    LABEL
}

@Serializable
enum class ButtonStyle {
    PRIMARY,
    SECONDARY,
    OUTLINED,
    TEXT
}

/**
 * UI event sent from the app to the plugin when user interacts with UI.
 */
@Serializable
data class PluginUIEvent(
    val componentId: String,
    val eventType: UIEventType,
    val data: Map<String, String> = emptyMap()
)

@Serializable
enum class UIEventType {
    CLICK,
    TEXT_CHANGED,
    SWITCH_TOGGLED,
    CHIP_SELECTED,
    LIST_ITEM_CLICKED,
    TAB_SELECTED
}

/**
 * Interface for plugins that provide declarative UI.
 */
interface PluginUIProvider {
    /**
     * Get the initial UI state for a screen.
     */
    fun getScreen(screenId: String, context: PluginScreenContext): PluginUIScreen?
    
    /**
     * Handle UI events and return updated UI state.
     */
    suspend fun handleEvent(
        screenId: String,
        event: PluginUIEvent,
        context: PluginScreenContext
    ): PluginUIScreen?
}
