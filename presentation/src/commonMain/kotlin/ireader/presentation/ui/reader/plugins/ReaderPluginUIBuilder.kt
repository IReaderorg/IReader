package ireader.presentation.ui.reader.plugins

import ireader.plugin.api.*

/**
 * DSL Builder for creating Plugin UI screens.
 * This makes it easy for plugins to build declarative UIs without boilerplate.
 * 
 * Example usage:
 * ```kotlin
 * val screen = pluginScreen("main", "Summarizer") {
 *     text("Summary will appear here", TextStyle.BODY)
 *     textField("input", "Enter text", multiline = true)
 *     row {
 *         button("summarize", "Summarize", ButtonStyle.PRIMARY)
 *         button("clear", "Clear", ButtonStyle.SECONDARY)
 *     }
 * }
 * ```
 */
class ReaderPluginUIBuilder {
    private val components = mutableListOf<PluginUIComponent>()
    
    /**
     * Add a text component
     */
    fun text(
        text: String,
        style: TextStyle = TextStyle.BODY
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Text(text, style))
        return this
    }
    
    /**
     * Add a title text (large)
     */
    fun title(text: String): ReaderPluginUIBuilder {
        return text(text, TextStyle.TITLE_LARGE)
    }
    
    /**
     * Add a subtitle text (medium)
     */
    fun subtitle(text: String): ReaderPluginUIBuilder {
        return text(text, TextStyle.TITLE_MEDIUM)
    }
    
    /**
     * Add body text
     */
    fun body(text: String): ReaderPluginUIBuilder {
        return text(text, TextStyle.BODY)
    }
    
    /**
     * Add a text field
     */
    fun textField(
        id: String,
        label: String,
        value: String = "",
        multiline: Boolean = false,
        maxLines: Int = if (multiline) 5 else 1
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.TextField(id, label, value, multiline, maxLines))
        return this
    }
    
    /**
     * Add a multiline text area
     */
    fun textArea(
        id: String,
        label: String,
        value: String = "",
        maxLines: Int = 5
    ): ReaderPluginUIBuilder {
        return textField(id, label, value, multiline = true, maxLines = maxLines)
    }
    
    /**
     * Add a button
     */
    fun button(
        id: String,
        label: String,
        style: ButtonStyle = ButtonStyle.PRIMARY,
        icon: String? = null
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Button(id, label, style, icon))
        return this
    }
    
    /**
     * Add a primary button
     */
    fun primaryButton(id: String, label: String, icon: String? = null): ReaderPluginUIBuilder {
        return button(id, label, ButtonStyle.PRIMARY, icon)
    }
    
    /**
     * Add a secondary button
     */
    fun secondaryButton(id: String, label: String, icon: String? = null): ReaderPluginUIBuilder {
        return button(id, label, ButtonStyle.SECONDARY, icon)
    }
    
    /**
     * Add an outlined button
     */
    fun outlinedButton(id: String, label: String, icon: String? = null): ReaderPluginUIBuilder {
        return button(id, label, ButtonStyle.OUTLINED, icon)
    }
    
    /**
     * Add a text button
     */
    fun textButton(id: String, label: String, icon: String? = null): ReaderPluginUIBuilder {
        return button(id, label, ButtonStyle.TEXT, icon)
    }
    
    /**
     * Add a card container
     */
    fun card(content: ReaderPluginUIBuilder.() -> Unit): ReaderPluginUIBuilder {
        val builder = ReaderPluginUIBuilder().apply(content)
        components.add(PluginUIComponent.Card(builder.build()))
        return this
    }
    
    /**
     * Add a row layout
     */
    fun row(
        spacing: Int = 8,
        content: ReaderPluginUIBuilder.() -> Unit
    ): ReaderPluginUIBuilder {
        val builder = ReaderPluginUIBuilder().apply(content)
        components.add(PluginUIComponent.Row(builder.build(), spacing))
        return this
    }
    
    /**
     * Add a column layout
     */
    fun column(
        spacing: Int = 8,
        content: ReaderPluginUIBuilder.() -> Unit
    ): ReaderPluginUIBuilder {
        val builder = ReaderPluginUIBuilder().apply(content)
        components.add(PluginUIComponent.Column(builder.build(), spacing))
        return this
    }
    
    /**
     * Add a switch
     */
    fun switch(
        id: String,
        label: String,
        checked: Boolean = false
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Switch(id, label, checked))
        return this
    }
    
    /**
     * Add a chip
     */
    fun chip(
        id: String,
        label: String,
        selected: Boolean = false
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Chip(id, label, selected))
        return this
    }
    
    /**
     * Add a chip group
     */
    fun chipGroup(
        id: String,
        chips: List<PluginUIComponent.Chip>,
        singleSelection: Boolean = true
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.ChipGroup(id, chips, singleSelection))
        return this
    }
    
    /**
     * Add a chip group with builder
     */
    fun chipGroup(
        id: String,
        singleSelection: Boolean = true,
        chips: ChipGroupBuilder.() -> Unit
    ): ReaderPluginUIBuilder {
        val builder = ChipGroupBuilder().apply(chips)
        components.add(PluginUIComponent.ChipGroup(id, builder.build(), singleSelection))
        return this
    }
    
    /**
     * Add a list
     */
    fun list(
        id: String,
        items: List<ListItem>
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.ItemList(id, items))
        return this
    }
    
    /**
     * Add tabs
     */
    fun tabs(content: TabsBuilder.() -> Unit): ReaderPluginUIBuilder {
        val builder = TabsBuilder().apply(content)
        components.add(PluginUIComponent.Tabs(builder.build()))
        return this
    }
    
    /**
     * Add a loading indicator
     */
    fun loading(message: String? = null): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Loading(message))
        return this
    }
    
    /**
     * Add an empty state
     */
    fun empty(
        message: String,
        icon: String? = null,
        description: String? = null
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Empty(icon, message, description))
        return this
    }
    
    /**
     * Add an error message
     */
    fun error(message: String): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Error(message))
        return this
    }
    
    /**
     * Add a spacer
     */
    fun spacer(height: Int = 16): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Spacer(height))
        return this
    }
    
    /**
     * Add a divider
     */
    fun divider(thickness: Int = 1): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Divider(thickness))
        return this
    }
    
    /**
     * Add a progress bar
     */
    fun progressBar(
        progress: Float = 0f,
        label: String? = null
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.ProgressBar(progress, label))
        return this
    }
    
    /**
     * Add an image
     */
    fun image(
        url: String,
        width: Int? = null,
        height: Int? = null,
        contentDescription: String? = null
    ): ReaderPluginUIBuilder {
        components.add(PluginUIComponent.Image(url, width, height, contentDescription))
        return this
    }
    
    /**
     * Add conditional content
     */
    fun ifElse(
        condition: Boolean,
        ifContent: ReaderPluginUIBuilder.() -> Unit,
        elseContent: (ReaderPluginUIBuilder.() -> Unit)? = null
    ): ReaderPluginUIBuilder {
        if (condition) {
            apply(ifContent)
        } else if (elseContent != null) {
            apply(elseContent)
        }
        return this
    }
    
    /**
     * Build the component list
     */
    fun build(): List<PluginUIComponent> = components.toList()
}

/**
 * Builder for chip groups
 */
class ChipGroupBuilder {
    private val chips = mutableListOf<PluginUIComponent.Chip>()
    
    fun chip(
        id: String,
        label: String,
        selected: Boolean = false
    ): ChipGroupBuilder {
        chips.add(PluginUIComponent.Chip(id, label, selected))
        return this
    }
    
    fun build(): List<PluginUIComponent.Chip> = chips.toList()
}

/**
 * Builder for tabs
 */
class TabsBuilder {
    private val tabs = mutableListOf<Tab>()
    
    fun tab(
        id: String,
        title: String,
        icon: String? = null,
        content: ReaderPluginUIBuilder.() -> Unit
    ): TabsBuilder {
        val builder = ReaderPluginUIBuilder().apply(content)
        tabs.add(Tab(id, title, icon, builder.build()))
        return this
    }
    
    fun build(): List<Tab> = tabs.toList()
}

/**
 * Create a plugin screen using DSL
 */
fun pluginScreen(
    id: String,
    title: String,
    content: ReaderPluginUIBuilder.() -> Unit
): PluginUIScreen {
    val builder = ReaderPluginUIBuilder().apply(content)
    return PluginUIScreen(id, title, builder.build())
}

/**
 * Extension to create UI easily from a list of components
 */
fun List<PluginUIComponent>.toScreen(id: String, title: String): PluginUIScreen {
    return PluginUIScreen(id, title, this)
}

/**
 * Helper to build UI components
 */
fun buildUI(content: ReaderPluginUIBuilder.() -> Unit): List<PluginUIComponent> {
    return ReaderPluginUIBuilder().apply(content).build()
}
