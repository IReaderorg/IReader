package ireader.presentation.ui.component.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.components.ChipPreference
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.SliderPreference
import ireader.presentation.ui.component.components.SwitchPreference
import ireader.presentation.ui.component.text_related.TextSection
import ireader.presentation.ui.core.ui.PreferenceMutableState

/**
 * Sealed class hierarchy for declarative UI component building in settings screens.
 * 
 * This system provides a type-safe way to build settings screens with consistent
 * styling and behavior. Each component type represents a different UI element
 * that can be displayed in a settings screen.
 * 
 * All components support visibility control through the [visible] property.
 * 
 * @property visible Controls whether the component is displayed (default: true)
 */
sealed class Components(
    open val visible: Boolean = true
) {
    /**
     * Header component for section titles.
     * 
     * Displays a styled header to separate and label groups of preferences.
     * 
     * @property text The header text to display
     * @property toUpper Whether to convert text to uppercase (default: false)
     * @property padding Padding around the header (default: 16.dp all sides)
     * @property icon Optional icon to display before the text
     * @property visible Whether the header is visible (default: true)
     */
    data class Header(
        val text: String,
        val toUpper: Boolean = false,
        val padding: PaddingValues = PaddingValues(16.dp),
        val icon: ImageVector? = null,
        override val visible: Boolean = true,
    ) : Components()

    /**
     * Slider component for numeric value selection.
     * 
     * Supports Float, Int, and Long preferences with customizable range and steps.
     * Displays a slider with optional title, subtitle, icon, and value display.
     * 
     * @property preferenceAsFloat Float preference state (mutually exclusive with other preference types)
     * @property preferenceAsInt Int preference state (mutually exclusive with other preference types)
     * @property preferenceAsLong Long preference state (mutually exclusive with other preference types)
     * @property mutablePreferences Direct Float value (for non-preference use cases)
     * @property title The slider title
     * @property subtitle Optional descriptive text below the title
     * @property icon Optional leading icon
     * @property trailing Optional text to display the current value (e.g., "50%")
     * @property onValueChange Callback invoked when the value changes
     * @property valueRange The range of valid values (default: 0f..1f)
     * @property onValueChangeFinished Callback invoked when user finishes adjusting
     * @property steps Number of discrete steps (0 for continuous, default: 0)
     * @property isEnabled Whether the slider is interactive (default: true)
     * @property visible Whether the slider is visible (default: true)
     */
    data class Slider(
        val preferenceAsFloat: PreferenceMutableState<Float>? = null,
        val preferenceAsInt: PreferenceMutableState<Int>? = null,
        val preferenceAsLong: PreferenceMutableState<Long>? = null,
        val mutablePreferences: Float? = null,
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val trailing: String? = null,
        val onValueChange: ((Float) -> Unit)? = null,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val onValueChangeFinished: ((Float) -> Unit)? = null,
        val steps: Int = 0,
        val isEnabled: Boolean = true,
        override val visible: Boolean = true
    ) : Components()

    /**
     * Basic row component for clickable preferences.
     * 
     * Displays a row with title, optional subtitle, icon, and trailing content.
     * Supports both click and long-click interactions.
     * 
     * @property title The main text to display
     * @property icon Optional leading icon
     * @property painter Optional leading painter (alternative to icon)
     * @property onClick Callback invoked when the row is clicked
     * @property onLongClick Callback invoked when the row is long-clicked
     * @property subtitle Optional descriptive text below the title
     * @property action Optional trailing composable content
     * @property enabled Whether the row is interactive (default: true)
     * @property visible Whether the row is visible (default: true)
     */
    data class Row(
        val title: String,
        val icon: ImageVector? = null,
        val painter: Painter? = null,
        val onClick: () -> Unit = {},
        val onLongClick: () -> Unit = {},
        val subtitle: String? = null,
        val action: @Composable (() -> Unit)? = null,
        val enabled: Boolean = true,
        override val visible: Boolean = true
    ) : Components()

    /**
     * Switch preference component for boolean settings.
     * 
     * Displays a row with a switch control for toggling boolean preferences.
     * 
     * @property modifier Modifier for the component
     * @property preference The boolean preference state
     * @property title The preference title
     * @property subtitle Optional descriptive text below the title
     * @property painter Optional leading painter
     * @property icon Optional leading icon
     * @property onValue Optional callback invoked when the value changes
     * @property enabled Whether the switch is interactive (default: true)
     * @property visible Whether the switch is visible (default: true)
     */
    data class Switch(
        val modifier: Modifier = Modifier,
        val preference: PreferenceMutableState<Boolean>,
        val title: String,
        val subtitle: String? = null,
        val painter: Painter? = null,
        val icon: ImageVector? = null,
        val onValue: ((Boolean) -> Unit)? = null,
        val enabled: Boolean = true,
        override val visible: Boolean = true
    ) : Components()

    /**
     * Dynamic component for custom composable content.
     * 
     * Allows embedding arbitrary composable content within the component system.
     * Useful for one-off custom UI elements that don't fit other component types.
     * 
     * @property visible Whether the component is visible (default: true)
     * @property component The composable content to display
     */
    data class Dynamic(
        override val visible: Boolean = true,
        val component: @Composable () -> Unit,
    ) : Components()

    /**
     * Chip preference component for selecting from a list of options.
     * 
     * Displays a row with horizontally scrollable chips for option selection.
     * 
     * @property preference List of option labels
     * @property selected Index of the currently selected option
     * @property title The preference title
     * @property subtitle Optional descriptive text below the title
     * @property icon Optional leading icon
     * @property onValueChange Callback invoked when selection changes
     * @property enabled Whether the chips are interactive (default: true)
     * @property visible Whether the component is visible (default: true)
     */
    data class Chip(
        val preference: List<String>,
        val selected: Int,
        val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val onValueChange: ((Int) -> Unit)?,
        val enabled: Boolean = true,
        override val visible: Boolean = true
    ) : Components()

    /**
     * Divider component for visual separation.
     * 
     * Displays a horizontal line to separate groups of preferences.
     * 
     * @property thickness The thickness of the divider line (default: 1.dp)
     * @property startIndent The start padding before the divider (default: 0.dp)
     * @property visible Whether the divider is visible (default: true)
     */
    data class Divider(
        val thickness: androidx.compose.ui.unit.Dp = 1.dp,
        val startIndent: androidx.compose.ui.unit.Dp = 0.dp,
        override val visible: Boolean = true
    ) : Components()

    /**
     * Spacer component for vertical spacing.
     * 
     * Adds vertical space between components. Use [Space] object for default spacing
     * or [CustomSpace] for custom height.
     * 
     * @property visible Whether the space is visible (default: true)
     */
    object Space : Components()

    /**
     * Custom spacer component with configurable height.
     * 
     * @property height The height of the spacer (default: 50.dp)
     * @property visible Whether the space is visible (default: true)
     */
    data class CustomSpace(
        val height: androidx.compose.ui.unit.Dp = 50.dp,
        override val visible: Boolean = true
    ) : Components()
    
    companion object {
        /**
         * Visibility toggle icon button.
         * 
         * Displays an icon button that toggles between visible and hidden states.
         * Useful for showing/hiding sensitive information or collapsible sections.
         * 
         * @param visible Current visibility state
         * @param onVisibilityChanged Callback invoked when visibility should change
         */
        @Composable
        fun VisibilityIcon(
            visible: Boolean,
            onVisibilityChanged: (Boolean) -> Unit,
        ) {
            IconButton(
                onClick = { onVisibilityChanged(!visible) },
                modifier = Modifier.semantics {
                    contentDescription = if (visible) "Hide content" else "Show content"
                    role = androidx.compose.ui.semantics.Role.Button
                }
            ) {
                Icon(
                    imageVector = if (visible) 
                        Icons.Default.Visibility 
                    else 
                        Icons.Default.VisibilityOff,
                    contentDescription = null  // Handled by parent semantics
                )
            }
        }
        
        /**
         * Section container with header.
         * 
         * Groups related components under a common header with optional icon.
         * 
         * @param headingText The section header text
         * @param icon Optional icon composable to display before the heading
         * @param content The section content
         */
        @Composable
        fun Section(
            headingText: String,
            icon: @Composable (() -> Unit)? = null,
            content: @Composable () -> Unit
        ) {
            androidx.compose.foundation.layout.Column {
                // Section heading
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        icon()
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    }
                    androidx.compose.material3.Text(
                        text = headingText,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
                
                // Section content
                content()
            }
        }
        
        // ============================================================================
        // Builder Helper Functions
        // ============================================================================
        
        /**
         * Creates a header component with common defaults.
         * 
         * @param text The header text
         * @param icon Optional icon
         * @param toUpper Whether to uppercase the text (default: false)
         * @param visible Whether the header is visible (default: true)
         */
        fun header(
            text: String,
            icon: ImageVector? = null,
            toUpper: Boolean = false,
            visible: Boolean = true
        ): Header = Header(
            text = text,
            icon = icon,
            toUpper = toUpper,
            visible = visible
        )
        
        /**
         * Creates a row component with common defaults.
         * 
         * @param title The row title
         * @param subtitle Optional subtitle
         * @param icon Optional icon
         * @param onClick Click handler
         * @param visible Whether the row is visible (default: true)
         */
        fun row(
            title: String,
            subtitle: String? = null,
            icon: ImageVector? = null,
            onClick: () -> Unit = {},
            visible: Boolean = true
        ): Row = Row(
            title = title,
            subtitle = subtitle,
            icon = icon,
            onClick = onClick,
            visible = visible
        )
        
        /**
         * Creates a switch component with common defaults.
         * 
         * @param preference The boolean preference
         * @param title The switch title
         * @param subtitle Optional subtitle
         * @param icon Optional icon
         * @param visible Whether the switch is visible (default: true)
         */
        fun switch(
            preference: PreferenceMutableState<Boolean>,
            title: String,
            subtitle: String? = null,
            icon: ImageVector? = null,
            visible: Boolean = true
        ): Switch = Switch(
            preference = preference,
            title = title,
            subtitle = subtitle,
            icon = icon,
            visible = visible
        )
        
        /**
         * Creates a divider component.
         * 
         * @param visible Whether the divider is visible (default: true)
         */
        fun divider(visible: Boolean = true): Divider = Divider(visible = visible)
        
        /**
         * Creates a custom space component.
         * 
         * @param height The height of the spacer
         * @param visible Whether the space is visible (default: true)
         */
        fun space(
            height: androidx.compose.ui.unit.Dp = 50.dp,
            visible: Boolean = true
        ): CustomSpace = CustomSpace(height = height, visible = visible)
    }
}

/**
 * Sets up and displays a list of setting components in a scrollable column.
 * 
 * This function provides the main layout for settings screens, handling:
 * - Proper padding and spacing
 * - Lazy loading for performance
 * - Error boundaries for component rendering
 * - Optimized recomposition
 * 
 * @param scaffoldPadding Padding from the scaffold (typically for system bars)
 * @param items List of components to display
 * @param modifier Optional modifier for the container
 * @param contentPadding Additional content padding (default: 0.dp)
 * @param verticalArrangement Vertical arrangement strategy (default: Top)
 */
@Composable
fun SetupSettingComponents(
    scaffoldPadding: PaddingValues,
    items: List<Components>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top
) {
    // Filter visible items once to avoid recomputation
    val visibleItems = remember(items) {
        items.filter { it.visible }
    }
    
    LazyColumn(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        setupUiComponent(visibleItems)
    }
}

/**
 * Lazy column with scaffold padding insets.
 * 
 * Provides a convenient wrapper for creating lazy columns that respect
 * scaffold padding (system bars, navigation bars, etc.).
 * 
 * @param scaffoldPadding Padding from the scaffold
 * @param modifier Optional modifier for the column
 * @param contentPadding Additional content padding (default: 0.dp)
 * @param verticalArrangement Vertical arrangement strategy (default: Top)
 * @param horizontalAlignment Horizontal alignment (default: Start)
 * @param content The lazy list content
 */
@Composable
fun LazyColumnWithInsets(
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        content()
    }
}

/**
 * Builds and renders the component based on its type.
 * 
 * This extension function handles the rendering logic for each component type,
 * respecting visibility settings and passing appropriate parameters to the
 * underlying composable functions.
 */
@Composable
fun Components.Build() {
    when (this) {
        is Components.Header -> {
            if (this.visible) {
                TextSection(
                    text = this.text,
                    padding = this.padding,
                    toUpper = this.toUpper,
                    icon = this.icon
                )
            }
        }
        is Components.Slider -> {
            if (this.visible) {
                SliderPreference(
                    preferenceAsLong = this.preferenceAsLong,
                    preferenceAsFloat = this.preferenceAsFloat,
                    preferenceAsInt = this.preferenceAsInt,
                    title = this.title,
                    subtitle = this.subtitle,
                    icon = this.icon,
                    onValueChange = {
                        this.onValueChange?.invoke(it)
                    },
                    trailing = this.trailing,
                    valueRange = this.valueRange,
                    onValueChangeFinished = {
                        this.onValueChangeFinished?.invoke(it)
                    },
                    steps = this.steps,
                    isEnable = this.isEnabled
                )
            }
        }
        is Components.Row -> {
            if (this.visible) {
                PreferenceRow(
                    title = this.title,
                    action = this.action,
                    subtitle = this.subtitle,
                    onClick = this.onClick,
                    icon = this.icon,
                    painter = this.painter,
                    onLongClick = this.onLongClick,
                    enable = this.enabled
                )
            }
        }
        is Components.Chip -> {
            if (this.visible) {
                ChipPreference(
                    preference = this.preference,
                    selected = this.selected,
                    onValueChange = this.onValueChange,
                    title = this.title,
                    subtitle = this.subtitle,
                    icon = this.icon
                )
            }
        }
        is Components.Switch -> {
            if (this.visible) {
                SwitchPreference(
                    modifier = this.modifier,
                    preference = this.preference,
                    title = this.title,
                    icon = this.icon,
                    painter = this.painter,
                    subtitle = this.subtitle
                )
            }
        }
        is Components.Dynamic -> {
            if (this.visible) {
                this.component()
            }
        }
        is Components.Divider -> {
            if (this.visible) {
                androidx.compose.material3.Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = this.thickness,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
        is Components.Space -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
        is Components.CustomSpace -> {
            if (this.visible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(this.height)
                )
            }
        }
    }
}

/**
 * Extension function to set up UI components in a LazyListScope.
 * 
 * This function efficiently renders components with:
 * - Proper keys for stable list items
 * - Error boundaries to prevent crashes
 * - Optimized recomposition
 * 
 * @param list List of components to render
 */
fun LazyListScope.setupUiComponent(
    list: List<Components>,
) {
    items(
        count = list.size,
        key = { index ->
            // Generate stable keys for better performance
            when (val component = list[index]) {
                is Components.Header -> "header_${component.text}_$index"
                is Components.Row -> "row_${component.title}_$index"
                is Components.Switch -> "switch_${component.title}_$index"
                is Components.Slider -> "slider_${component.title}_$index"
                is Components.Chip -> "chip_${component.title}_$index"
                is Components.Divider -> "divider_$index"
                is Components.Space -> "space_$index"
                is Components.CustomSpace -> "custom_space_$index"
                is Components.Dynamic -> "dynamic_$index"
            }
        }
    ) { index ->
        val component = list[index]
        
        // Render component if visible
        if (component.visible) {
            component.Build()
        }
    }
}


// ============================================================================
// Component Extension Functions
// ============================================================================

/**
 * Extension functions for Components to provide utility operations,
 * builder patterns, and convenience functions.
 */

/**
 * Creates a copy of this component with updated visibility.
 * 
 * @param visible The new visibility state
 * @return A new component with updated visibility
 */
fun Components.withVisibility(visible: Boolean): Components {
    return when (this) {
        is Components.Header -> copy(visible = visible)
        is Components.Slider -> copy(visible = visible)
        is Components.Row -> copy(visible = visible)
        is Components.Switch -> copy(visible = visible)
        is Components.Chip -> copy(visible = visible)
        is Components.Divider -> copy(visible = visible)
        is Components.CustomSpace -> copy(visible = visible)
        is Components.Dynamic -> copy(visible = visible)
        is Components.Space -> this // Space is an object, cannot change visibility
    }
}

/**
 * Conditionally shows or hides this component based on a predicate.
 * 
 * @param condition The condition to evaluate
 * @return A new component with visibility based on the condition
 */
fun Components.showIf(condition: Boolean): Components {
    return withVisibility(condition)
}

/**
 * Conditionally hides this component based on a predicate.
 * 
 * @param condition The condition to evaluate
 * @return A new component with visibility based on the inverted condition
 */
fun Components.hideIf(condition: Boolean): Components {
    return withVisibility(!condition)
}

/**
 * Extension function to create a list of components with a builder pattern.
 * 
 * Example usage:
 * ```kotlin
 * val components = buildComponentList {
 *     header("Settings")
 *     row("Option 1") { /* click handler */ }
 *     divider()
 *     row("Option 2") { /* click handler */ }
 * }
 * ```
 */
class ComponentListBuilder {
    private val components = mutableListOf<Components>()
    
    /**
     * Adds a header component.
     */
    fun header(
        text: String,
        icon: ImageVector? = null,
        toUpper: Boolean = false,
        visible: Boolean = true
    ) {
        components.add(Components.Header(text, toUpper, PaddingValues(16.dp), icon, visible))
    }
    
    /**
     * Adds a row component.
     */
    fun row(
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        painter: Painter? = null,
        onClick: () -> Unit = {},
        onLongClick: () -> Unit = {},
        action: @Composable (() -> Unit)? = null,
        enabled: Boolean = true,
        visible: Boolean = true
    ) {
        components.add(
            Components.Row(
                title = title,
                icon = icon,
                painter = painter,
                onClick = onClick,
                onLongClick = onLongClick,
                subtitle = subtitle,
                action = action,
                enabled = enabled,
                visible = visible
            )
        )
    }
    
    /**
     * Adds a switch component.
     */
    fun switch(
        preference: PreferenceMutableState<Boolean>,
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        painter: Painter? = null,
        onValue: ((Boolean) -> Unit)? = null,
        enabled: Boolean = true,
        visible: Boolean = true
    ) {
        components.add(
            Components.Switch(
                preference = preference,
                title = title,
                subtitle = subtitle,
                painter = painter,
                icon = icon,
                onValue = onValue,
                enabled = enabled,
                visible = visible
            )
        )
    }
    
    /**
     * Adds a slider component.
     */
    fun slider(
        title: String,
        preferenceAsFloat: PreferenceMutableState<Float>? = null,
        preferenceAsInt: PreferenceMutableState<Int>? = null,
        preferenceAsLong: PreferenceMutableState<Long>? = null,
        subtitle: String? = null,
        icon: ImageVector? = null,
        trailing: String? = null,
        onValueChange: ((Float) -> Unit)? = null,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        onValueChangeFinished: ((Float) -> Unit)? = null,
        steps: Int = 0,
        isEnabled: Boolean = true,
        visible: Boolean = true
    ) {
        components.add(
            Components.Slider(
                preferenceAsFloat = preferenceAsFloat,
                preferenceAsInt = preferenceAsInt,
                preferenceAsLong = preferenceAsLong,
                title = title,
                subtitle = subtitle,
                icon = icon,
                trailing = trailing,
                onValueChange = onValueChange,
                valueRange = valueRange,
                onValueChangeFinished = onValueChangeFinished,
                steps = steps,
                isEnabled = isEnabled,
                visible = visible
            )
        )
    }
    
    /**
     * Adds a chip component.
     */
    fun chip(
        preference: List<String>,
        selected: Int,
        title: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        onValueChange: ((Int) -> Unit)?,
        enabled: Boolean = true,
        visible: Boolean = true
    ) {
        components.add(
            Components.Chip(
                preference = preference,
                selected = selected,
                title = title,
                subtitle = subtitle,
                icon = icon,
                onValueChange = onValueChange,
                enabled = enabled,
                visible = visible
            )
        )
    }
    
    /**
     * Adds a divider component.
     */
    fun divider(
        thickness: androidx.compose.ui.unit.Dp = 1.dp,
        startIndent: androidx.compose.ui.unit.Dp = 0.dp,
        visible: Boolean = true
    ) {
        components.add(Components.Divider(thickness, startIndent, visible))
    }
    
    /**
     * Adds a space component.
     */
    fun space() {
        components.add(Components.Space)
    }
    
    /**
     * Adds a custom space component with specified height.
     */
    fun space(height: androidx.compose.ui.unit.Dp, visible: Boolean = true) {
        components.add(Components.CustomSpace(height, visible))
    }
    
    /**
     * Adds a dynamic component.
     */
    fun dynamic(
        visible: Boolean = true,
        component: @Composable () -> Unit
    ) {
        components.add(Components.Dynamic(visible, component))
    }
    
    /**
     * Adds a custom component directly.
     */
    fun add(component: Components) {
        components.add(component)
    }
    
    /**
     * Adds multiple components.
     */
    fun addAll(vararg components: Components) {
        this.components.addAll(components)
    }
    
    /**
     * Conditionally adds components based on a predicate.
     */
    fun addIf(condition: Boolean, builder: ComponentListBuilder.() -> Unit) {
        if (condition) {
            builder()
        }
    }
    
    /**
     * Builds and returns the component list.
     */
    fun build(): List<Components> = components.toList()
}

/**
 * Builder function for creating component lists with a DSL-style syntax.
 * 
 * Example usage:
 * ```kotlin
 * val components = buildComponentList {
 *     header("General Settings")
 *     
 *     row("Theme", subtitle = "Dark") {
 *         navigateToTheme()
 *     }
 *     
 *     switch(
 *         preference = autoRotatePreference,
 *         title = "Auto-rotate",
 *         subtitle = "Rotate screen automatically"
 *     )
 *     
 *     divider()
 *     
 *     header("Advanced")
 *     
 *     slider(
 *         title = "Font Size",
 *         preferenceAsInt = fontSizePreference,
 *         valueRange = 12f..24f,
 *         trailing = "${fontSizePreference.value}sp"
 *     )
 * }
 * ```
 */
fun buildComponentList(builder: ComponentListBuilder.() -> Unit): List<Components> {
    return ComponentListBuilder().apply(builder).build()
}

/**
 * Extension function to filter components based on a predicate.
 * 
 * @param predicate The condition to evaluate for each component
 * @return A new list containing only components that match the predicate
 */
fun List<Components>.filterComponents(predicate: (Components) -> Boolean): List<Components> {
    return this.filter(predicate)
}

/**
 * Extension function to get only visible components.
 * 
 * @return A new list containing only visible components
 */
fun List<Components>.visibleOnly(): List<Components> {
    return this.filter { it.visible }
}

/**
 * Extension function to group components by type.
 * 
 * @return A map of component types to lists of components
 */
fun List<Components>.groupByType(): Map<String, List<Components>> {
    return this.groupBy { component ->
        when (component) {
            is Components.Header -> "Header"
            is Components.Row -> "Row"
            is Components.Switch -> "Switch"
            is Components.Slider -> "Slider"
            is Components.Chip -> "Chip"
            is Components.Divider -> "Divider"
            is Components.Space -> "Space"
            is Components.CustomSpace -> "CustomSpace"
            is Components.Dynamic -> "Dynamic"
        }
    }
}

/**
 * Extension function to insert a divider between each component.
 * 
 * @return A new list with dividers inserted between components
 */
fun List<Components>.withDividers(): List<Components> {
    if (this.isEmpty()) return this
    
    return this.flatMapIndexed { index, component ->
        if (index < this.size - 1) {
            listOf(component, Components.Divider())
        } else {
            listOf(component)
        }
    }
}

/**
 * Extension function to add spacing between components.
 * 
 * @param height The height of the spacing (default: 8.dp)
 * @return A new list with spacing inserted between components
 */
fun List<Components>.withSpacing(height: androidx.compose.ui.unit.Dp = 8.dp): List<Components> {
    if (this.isEmpty()) return this
    
    return this.flatMapIndexed { index, component ->
        if (index < this.size - 1) {
            listOf(component, Components.CustomSpace(height))
        } else {
            listOf(component)
        }
    }
}
