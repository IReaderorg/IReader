package ireader.presentation.ui.component.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import ireader.core.log.IReaderLog

/**
 * Accessibility utilities following Mihon's patterns
 * Provides comprehensive accessibility support for UI components
 */
object AccessibilityUtils {
    
    /**
     * Minimum touch target size for accessibility compliance (48dp)
     */
    val MinimumTouchTargetSize = 48.dp
    
    /**
     * Enhanced clickable modifier with accessibility support
     */
    @Composable
    fun Modifier.accessibleClickable(
        contentDescription: String? = null,
        role: Role? = null,
        enabled: Boolean = true,
        onClickLabel: String? = null,
        onClick: () -> Unit
    ): Modifier {
        IReaderLog.accessibility("Creating accessible clickable: $contentDescription")
        
        return this
            .sizeIn(minWidth = MinimumTouchTargetSize, minHeight = MinimumTouchTargetSize)
            .clip(MaterialTheme.shapes.small)
            .clickable(
                enabled = enabled,
                role = role,
                onClickLabel = onClickLabel,
                indication = ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                role?.let { this.role = it }
            }
    }
    
    /**
     * Enhanced selectable modifier with accessibility support
     */
    @Composable
    fun Modifier.accessibleSelectable(
        selected: Boolean,
        contentDescription: String? = null,
        role: Role = Role.RadioButton,
        enabled: Boolean = true,
        onClick: () -> Unit
    ): Modifier {
        val stateDescription = if (selected) "Selected" else "Not selected"
        
        return this
            .sizeIn(minWidth = MinimumTouchTargetSize, minHeight = MinimumTouchTargetSize)
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = role,
                indication = ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                this.role = role
                this.stateDescription = stateDescription
            }
    }
    
    /**
     * Modifier for grouping selectable items
     */
    fun Modifier.accessibleSelectableGroup(): Modifier {
        return this.selectableGroup()
    }
    
    /**
     * Modifier for heading text with proper semantic role
     */
    fun Modifier.accessibleHeading(level: Int = 1): Modifier {
        return this.semantics {
            heading()
        }
    }
    
    /**
     * Clear semantics for decorative elements
     */
    fun Modifier.decorative(): Modifier {
        return this.clearAndSetSemantics { }
    }
    
    /**
     * Add content description to any composable
     */
    fun Modifier.contentDescription(description: String): Modifier {
        return this.semantics {
            contentDescription = description
        }
    }
    
    /**
     * Add state description for dynamic content
     */
    fun Modifier.stateDescription(description: String): Modifier {
        return this.semantics {
            stateDescription = description
        }
    }
}

/**
 * Accessibility-focused button component
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .sizeIn(minWidth = AccessibilityUtils.MinimumTouchTargetSize, minHeight = AccessibilityUtils.MinimumTouchTargetSize)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                role = Role.Button
            },
        enabled = enabled,
        content = { content() }
    )
}

/**
 * Accessibility-focused icon button component
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier
            .size(AccessibilityUtils.MinimumTouchTargetSize)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        enabled = enabled,
        content = content
    )
    
    IReaderLog.accessibility("Created accessible icon button: $contentDescription")
}