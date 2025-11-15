package ireader.presentation.core.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized dimension constants for consistent UI sizing across the application.
 * These dimensions ensure visual consistency and proper touch target sizes.
 */
object ToolbarDimensions {
    /**
     * Standard toolbar height used throughout the app (Material Design default)
     */
    val StandardHeight = 64.dp
    
    /**
     * Minimized toolbar height for immersive experiences like WebView
     * Provides 16dp more vertical space compared to standard toolbar
     */
    val MinimizedHeight = 48.dp
    
    /**
     * Standard icon size for toolbar icons
     */
    val StandardIconSize = 24.dp
    
    /**
     * Minimized icon size for compact toolbars
     * Visually smaller but maintains proper touch targets
     */
    val MinimizedIconSize = 20.dp
    
    /**
     * Standard padding for toolbar content
     */
    val StandardPadding = 16.dp
    
    /**
     * Minimized padding for compact toolbars
     */
    val MinimizedPadding = 8.dp
    
    /**
     * Minimum touch target size for accessibility compliance
     * Ensures all interactive elements are easily tappable
     */
    val MinimumTouchTarget = 48.dp
}
