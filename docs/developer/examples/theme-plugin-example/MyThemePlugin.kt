package com.example.mytheme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginContext
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.ThemePlugin
import ireader.domain.plugins.ThemeBackgrounds
import ireader.presentation.ui.theme.ExtraColors

/**
 * Example theme plugin demonstrating how to create custom themes for IReader.
 * 
 * This plugin provides a "Midnight Blue" theme with custom colors and backgrounds.
 */
class MyThemePlugin : ThemePlugin {
    
    override val manifest: PluginManifest by lazy {
        // In production, load from plugin.json
        PluginManifest(
            id = "com.example.mytheme",
            name = "Midnight Blue Theme",
            version = "1.0.0",
            versionCode = 1,
            description = "A beautiful dark blue theme for comfortable night reading",
            author = PluginAuthor(
                name = "Example Developer",
                email = "dev@example.com",
                website = "https://example.com"
            ),
            type = PluginType.THEME,
            permissions = emptyList(),
            minIReaderVersion = "1.0.0",
            platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP),
            monetization = PluginMonetization.Free,
            iconUrl = "icon.png",
            screenshotUrls = listOf("screenshot1.png", "screenshot2.png")
        )
    }
    
    override fun initialize(context: PluginContext) {
        // Initialize theme resources if needed
        context.logger.info("Midnight Blue Theme initialized")
    }
    
    override fun cleanup() {
        // Clean up any resources
    }
    
    override fun getColorScheme(isDark: Boolean): ColorScheme {
        return if (isDark) {
            darkColorScheme(
                primary = Color(0xFF4A90E2),           // Bright blue
                onPrimary = Color(0xFFFFFFFF),         // White
                primaryContainer = Color(0xFF1E3A5F),  // Dark blue
                onPrimaryContainer = Color(0xFFB3D4FF), // Light blue
                
                secondary = Color(0xFF7B68EE),         // Medium slate blue
                onSecondary = Color(0xFFFFFFFF),       // White
                secondaryContainer = Color(0xFF2D2654), // Dark purple
                onSecondaryContainer = Color(0xFFD4CCFF), // Light purple
                
                tertiary = Color(0xFF50C878),          // Emerald green
                onTertiary = Color(0xFF003822),        // Dark green
                tertiaryContainer = Color(0xFF005233), // Medium dark green
                onTertiaryContainer = Color(0xFFB8F4D3), // Light green
                
                error = Color(0xFFFF6B6B),             // Soft red
                onError = Color(0xFF690005),           // Dark red
                errorContainer = Color(0xFF93000A),    // Medium red
                onErrorContainer = Color(0xFFFFDAD6),  // Light red
                
                background = Color(0xFF0A1929),        // Very dark blue
                onBackground = Color(0xFFE1E8F0),      // Light gray-blue
                
                surface = Color(0xFF132F4C),           // Dark blue surface
                onSurface = Color(0xFFE1E8F0),         // Light gray-blue
                surfaceVariant = Color(0xFF1E3A5F),    // Medium dark blue
                onSurfaceVariant = Color(0xFFB3D4FF),  // Light blue
                
                outline = Color(0xFF4A90E2),           // Bright blue
                outlineVariant = Color(0xFF2D5A8C),    // Medium blue
                
                scrim = Color(0xFF000000),             // Black
                
                inverseSurface = Color(0xFFE1E8F0),    // Light gray-blue
                inverseOnSurface = Color(0xFF0A1929),  // Very dark blue
                inversePrimary = Color(0xFF2D5A8C)     // Medium blue
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF2D5A8C),           // Medium blue
                onPrimary = Color(0xFFFFFFFF),         // White
                primaryContainer = Color(0xFFB3D4FF),  // Light blue
                onPrimaryContainer = Color(0xFF001D35), // Very dark blue
                
                secondary = Color(0xFF5B4FA3),         // Purple
                onSecondary = Color(0xFFFFFFFF),       // White
                secondaryContainer = Color(0xFFD4CCFF), // Light purple
                onSecondaryContainer = Color(0xFF1A0F3D), // Dark purple
                
                tertiary = Color(0xFF00875A),          // Green
                onTertiary = Color(0xFFFFFFFF),        // White
                tertiaryContainer = Color(0xFFB8F4D3), // Light green
                onTertiaryContainer = Color(0xFF002114), // Dark green
                
                error = Color(0xFFBA1A1A),             // Red
                onError = Color(0xFFFFFFFF),           // White
                errorContainer = Color(0xFFFFDAD6),    // Light red
                onErrorContainer = Color(0xFF410002),  // Dark red
                
                background = Color(0xFFF8FAFC),        // Very light blue-gray
                onBackground = Color(0xFF0A1929),      // Very dark blue
                
                surface = Color(0xFFFFFFFF),           // White
                onSurface = Color(0xFF0A1929),         // Very dark blue
                surfaceVariant = Color(0xFFE1E8F0),    // Light gray-blue
                onSurfaceVariant = Color(0xFF2D5A8C),  // Medium blue
                
                outline = Color(0xFF4A90E2),           // Bright blue
                outlineVariant = Color(0xFFB3D4FF),    // Light blue
                
                scrim = Color(0xFF000000),             // Black
                
                inverseSurface = Color(0xFF0A1929),    // Very dark blue
                inverseOnSurface = Color(0xFFF8FAFC),  // Very light blue-gray
                inversePrimary = Color(0xFF4A90E2)     // Bright blue
            )
        }
    }
    
    override fun getExtraColors(isDark: Boolean): ExtraColors {
        return if (isDark) {
            ExtraColors(
                bars = Color(0xFF0A1929),              // Very dark blue
                onBars = Color(0xFFE1E8F0),            // Light gray-blue
                
                // Reader-specific colors
                readerBackground = Color(0xFF0F1F2E),  // Dark blue for reading
                readerText = Color(0xFFE8EEF5),        // Soft white for text
                
                // Additional custom colors
                accent = Color(0xFF4A90E2),            // Bright blue accent
                divider = Color(0xFF1E3A5F),           // Dark blue divider
                
                // Status colors
                success = Color(0xFF50C878),           // Emerald green
                warning = Color(0xFFFFA500),           // Orange
                info = Color(0xFF4A90E2)               // Bright blue
            )
        } else {
            ExtraColors(
                bars = Color(0xFFFFFFFF),              // White
                onBars = Color(0xFF0A1929),            // Very dark blue
                
                // Reader-specific colors
                readerBackground = Color(0xFFFAFBFC),  // Off-white for reading
                readerText = Color(0xFF1A2332),        // Dark blue-gray for text
                
                // Additional custom colors
                accent = Color(0xFF2D5A8C),            // Medium blue accent
                divider = Color(0xFFE1E8F0),           // Light gray-blue divider
                
                // Status colors
                success = Color(0xFF00875A),           // Green
                warning = Color(0xFFFF8C00),           // Dark orange
                info = Color(0xFF2D5A8C)               // Medium blue
            )
        }
    }
    
    override fun getTypography(): Typography? {
        // Return null to use default typography
        // Or customize:
        // return Typography(
        //     displayLarge = TextStyle(...),
        //     bodyMedium = TextStyle(...),
        //     ...
        // )
        return null
    }
    
    override fun getBackgroundAssets(): ThemeBackgrounds? {
        // Optionally provide custom background images
        return ThemeBackgrounds(
            readerBackground = "backgrounds/reader_bg.png",
            appBackground = "backgrounds/app_bg.png"
        )
    }
}
