package ireader.presentation.ui.core.theme

import ireader.domain.models.theme.Theme
import ireader.domain.plugins.ThemePlugin

/**
 * Sealed class representing different theme options
 * Requirements: 3.1, 3.2
 */
sealed class ThemeOption {
    abstract val id: String
    abstract val name: String
    abstract val isDark: Boolean
    
    /**
     * Built-in theme option
     */
    data class BuiltIn(
        val theme: Theme
    ) : ThemeOption() {
        override val id: String = theme.id.toString()
        override val name: String = getThemeName(theme.id)
        override val isDark: Boolean = theme.isDark
    }
    
    /**
     * Plugin-provided theme option
     */
    data class Plugin(
        val plugin: ThemePlugin,
        override val isDark: Boolean
    ) : ThemeOption() {
        override val id: String = "${plugin.manifest.id}_${if (isDark) "dark" else "light"}"
        override val name: String = "${plugin.manifest.name} ${if (isDark) "Dark" else "Light"}"
    }
}

/**
 * Get the display name for a built-in theme
 */
private fun getThemeName(themeId: Long): String {
    return when (themeId) {
        -1L -> "Tachiyomi Light"
        -2L -> "Tachiyomi Dark"
        -3L -> "Blue Light"
        -4L -> "Blue Dark"
        -5L -> "Midnight Light"
        -6L -> "Midnight Dark"
        -7L -> "Green Apple Light"
        -8L -> "Green Apple Dark"
        -9L -> "Strawberries Light"
        -10L -> "Strawberries Dark"
        -11L -> "Tako Light"
        -12L -> "Tako Dark"
        -13L -> "Ocean Blue Light"
        -14L -> "Ocean Blue Dark"
        -15L -> "Sunset Orange Light"
        -16L -> "Sunset Orange Dark"
        -17L -> "Lavender Purple Light"
        -18L -> "Lavender Purple Dark"
        -19L -> "Forest Green Light"
        -20L -> "Forest Green Dark"
        -21L -> "Monochrome Light"
        -22L -> "Monochrome Dark"
        -23L -> "Cherry Blossom Light"
        -24L -> "Cherry Blossom Dark"
        -25L -> "Midnight Sky Light"
        -26L -> "Midnight Sky Dark"
        -27L -> "Autumn Harvest Light"
        -28L -> "Autumn Harvest Dark"
        -29L -> "Emerald Forest Light"
        -30L -> "Emerald Forest Dark"
        -31L -> "Rose Gold Light"
        -32L -> "Rose Gold Dark"
        else -> "Custom Theme"
    }
}
