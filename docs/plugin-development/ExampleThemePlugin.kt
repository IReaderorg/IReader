package ireader.plugins.examples

import ireader.domain.plugins.*

/**
 * Example theme plugin demonstrating how to create a custom theme
 * 
 * This example creates a "Sunset" theme with warm orange and purple colors
 */
class SunsetThemePlugin : ThemePlugin {
    
    override val manifest = PluginManifest(
        id = "com.example.sunset-theme",
        name = "Sunset Theme",
        version = "1.0.0",
        versionCode = 1,
        description = "A warm theme inspired by sunset colors",
        author = PluginAuthor(
            name = "Example Developer",
            email = "dev@example.com",
            website = "https://example.com"
        ),
        type = PluginType.THEME,
        permissions = emptyList(), // Theme plugins typically don't need permissions
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP, Platform.IOS),
        monetization = PluginMonetization.Free,
        iconUrl = "https://example.com/sunset-icon.png",
        screenshotUrls = listOf(
            "https://example.com/sunset-screenshot1.png",
            "https://example.com/sunset-screenshot2.png"
        )
    )
    
    override fun initialize(context: PluginContext) {
        // Initialize plugin resources if needed
        // For simple themes, this can be empty
    }
    
    override fun cleanup() {
        // Cleanup plugin resources if needed
        // For simple themes, this can be empty
    }
    
    override fun getColorScheme(isDark: Boolean): ThemeColorScheme {
        return if (isDark) {
            getDarkColorScheme()
        } else {
            getLightColorScheme()
        }
    }
    
    override fun getExtraColors(isDark: Boolean): ThemeExtraColors {
        return if (isDark) {
            ThemeExtraColors(
                bars = 0xFF1A1A2E,
                onBars = 0xFFFFFFFF,
                isBarLight = false
            )
        } else {
            ThemeExtraColors(
                bars = 0xFFFFF5E1,
                onBars = 0xFF000000,
                isBarLight = true
            )
        }
    }
    
    override fun getTypography(): ThemeTypography? {
        // Return null to use default typography
        // Or customize:
        return ThemeTypography(
            fontFamily = "sans-serif",
            // Customize specific font sizes if needed
            titleLargeFontSize = 22f,
            bodyMediumFontSize = 14f
        )
    }
    
    override fun getBackgroundAssets(): ThemeBackgrounds? {
        // Return null for no custom backgrounds
        // Or provide paths to background images:
        return ThemeBackgrounds(
            readerBackground = "assets://sunset-reader-bg.jpg",
            appBackground = "assets://sunset-app-bg.jpg"
        )
    }
    
    /**
     * Light color scheme with warm sunset colors
     */
    private fun getLightColorScheme(): ThemeColorScheme {
        return ThemeColorScheme(
            primary = 0xFFFF6B35,           // Warm orange
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFFFDAB9,
            onPrimaryContainer = 0xFF4A1500,
            
            secondary = 0xFFB565D8,         // Soft purple
            onSecondary = 0xFFFFFFFF,
            secondaryContainer = 0xFFE8D5F2,
            onSecondaryContainer = 0xFF2E0A3D,
            
            tertiary = 0xFFFFC857,          // Golden yellow
            onTertiary = 0xFF000000,
            tertiaryContainer = 0xFFFFE8B3,
            onTertiaryContainer = 0xFF4A3800,
            
            error = 0xFFBA1A1A,
            onError = 0xFFFFFFFF,
            errorContainer = 0xFFFFDAD6,
            onErrorContainer = 0xFF410002,
            
            background = 0xFFFFF5E1,        // Cream background
            onBackground = 0xFF1A1A1A,
            
            surface = 0xFFFFFAF0,
            onSurface = 0xFF1A1A1A,
            surfaceVariant = 0xFFFFE4CC,
            onSurfaceVariant = 0xFF4A4A4A,
            
            outline = 0xFFB8B8B8,
            outlineVariant = 0xFFE0E0E0,
            scrim = 0xFF000000,
            
            inverseSurface = 0xFF2E2E2E,
            inverseOnSurface = 0xFFF5F5F5,
            inversePrimary = 0xFFFFB399
        )
    }
    
    /**
     * Dark color scheme with deep sunset colors
     */
    private fun getDarkColorScheme(): ThemeColorScheme {
        return ThemeColorScheme(
            primary = 0xFFFFB399,           // Soft orange
            onPrimary = 0xFF4A1500,
            primaryContainer = 0xFFB84A1F,
            onPrimaryContainer = 0xFFFFDAB9,
            
            secondary = 0xFFD4A5F5,         // Light purple
            onSecondary = 0xFF2E0A3D,
            secondaryContainer = 0xFF7B3FA3,
            onSecondaryContainer = 0xFFE8D5F2,
            
            tertiary = 0xFFFFD97D,          // Pale yellow
            onTertiary = 0xFF4A3800,
            tertiaryContainer = 0xFFCC9A00,
            onTertiaryContainer = 0xFFFFE8B3,
            
            error = 0xFFFFB4AB,
            onError = 0xFF690005,
            errorContainer = 0xFF93000A,
            onErrorContainer = 0xFFFFDAD6,
            
            background = 0xFF1A1A2E,        // Deep blue-black
            onBackground = 0xFFE8E8E8,
            
            surface = 0xFF16213E,
            onSurface = 0xFFE8E8E8,
            surfaceVariant = 0xFF2A3A5A,
            onSurfaceVariant = 0xFFB8B8B8,
            
            outline = 0xFF6B6B6B,
            outlineVariant = 0xFF3A3A3A,
            scrim = 0xFF000000,
            
            inverseSurface = 0xFFE8E8E8,
            inverseOnSurface = 0xFF1A1A1A,
            inversePrimary = 0xFFFF6B35
        )
    }
}

/**
 * Example of a minimal theme plugin
 * This shows the absolute minimum required for a theme plugin
 */
class MinimalThemePlugin : ThemePlugin {
    
    override val manifest = PluginManifest(
        id = "com.example.minimal-theme",
        name = "Minimal Theme",
        version = "1.0.0",
        versionCode = 1,
        description = "A minimal example theme",
        author = PluginAuthor(
            name = "Example Developer",
            email = null,
            website = null
        ),
        type = PluginType.THEME,
        permissions = emptyList(),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP, Platform.IOS),
        monetization = PluginMonetization.Free,
        iconUrl = null,
        screenshotUrls = emptyList()
    )
    
    override fun initialize(context: PluginContext) {}
    
    override fun cleanup() {}
    
    override fun getColorScheme(isDark: Boolean): ThemeColorScheme {
        // Return a simple monochrome color scheme
        return if (isDark) {
            ThemeColorScheme(
                primary = 0xFFBBBBBB,
                onPrimary = 0xFF000000,
                primaryContainer = 0xFF888888,
                onPrimaryContainer = 0xFFFFFFFF,
                secondary = 0xFF999999,
                onSecondary = 0xFF000000,
                secondaryContainer = 0xFF666666,
                onSecondaryContainer = 0xFFFFFFFF,
                tertiary = 0xFF777777,
                onTertiary = 0xFF000000,
                tertiaryContainer = 0xFF555555,
                onTertiaryContainer = 0xFFFFFFFF,
                error = 0xFFFF5555,
                onError = 0xFFFFFFFF,
                errorContainer = 0xFFCC0000,
                onErrorContainer = 0xFFFFFFFF,
                background = 0xFF000000,
                onBackground = 0xFFFFFFFF,
                surface = 0xFF1A1A1A,
                onSurface = 0xFFFFFFFF,
                surfaceVariant = 0xFF2A2A2A,
                onSurfaceVariant = 0xFFCCCCCC,
                outline = 0xFF666666,
                outlineVariant = 0xFF444444,
                scrim = 0xFF000000,
                inverseSurface = 0xFFFFFFFF,
                inverseOnSurface = 0xFF000000,
                inversePrimary = 0xFF444444
            )
        } else {
            ThemeColorScheme(
                primary = 0xFF444444,
                onPrimary = 0xFFFFFFFF,
                primaryContainer = 0xFF777777,
                onPrimaryContainer = 0xFF000000,
                secondary = 0xFF666666,
                onSecondary = 0xFFFFFFFF,
                secondaryContainer = 0xFF999999,
                onSecondaryContainer = 0xFF000000,
                tertiary = 0xFF888888,
                onTertiary = 0xFFFFFFFF,
                tertiaryContainer = 0xFFAAAAAA,
                onTertiaryContainer = 0xFF000000,
                error = 0xFFCC0000,
                onError = 0xFFFFFFFF,
                errorContainer = 0xFFFF5555,
                onErrorContainer = 0xFF000000,
                background = 0xFFFFFFFF,
                onBackground = 0xFF000000,
                surface = 0xFFF5F5F5,
                onSurface = 0xFF000000,
                surfaceVariant = 0xFFE5E5E5,
                onSurfaceVariant = 0xFF333333,
                outline = 0xFF999999,
                outlineVariant = 0xFFBBBBBB,
                scrim = 0xFF000000,
                inverseSurface = 0xFF000000,
                inverseOnSurface = 0xFFFFFFFF,
                inversePrimary = 0xFFBBBBBB
            )
        }
    }
    
    override fun getExtraColors(isDark: Boolean): ThemeExtraColors {
        return if (isDark) {
            ThemeExtraColors(
                bars = 0xFF1A1A1A,
                onBars = 0xFFFFFFFF,
                isBarLight = false
            )
        } else {
            ThemeExtraColors(
                bars = 0xFFF5F5F5,
                onBars = 0xFF000000,
                isBarLight = true
            )
        }
    }
    
    override fun getTypography(): ThemeTypography? = null
    
    override fun getBackgroundAssets(): ThemeBackgrounds? = null
}

/**
 * Example of a premium theme plugin with monetization
 */
class PremiumThemePlugin : ThemePlugin {
    
    override val manifest = PluginManifest(
        id = "com.example.premium-theme",
        name = "Premium Sunset Pro",
        version = "1.0.0",
        versionCode = 1,
        description = "Premium theme with exclusive colors and backgrounds",
        author = PluginAuthor(
            name = "Example Developer",
            email = "dev@example.com",
            website = "https://example.com"
        ),
        type = PluginType.THEME,
        permissions = emptyList(),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP, Platform.IOS),
        monetization = PluginMonetization.Premium(
            price = 2.99,
            currency = "USD",
            trialDays = 7
        ),
        iconUrl = "https://example.com/premium-icon.png",
        screenshotUrls = listOf(
            "https://example.com/premium-screenshot1.png",
            "https://example.com/premium-screenshot2.png"
        )
    )
    
    // Implementation similar to SunsetThemePlugin
    // ... (rest of implementation)
    
    override fun initialize(context: PluginContext) {}
    override fun cleanup() {}
    override fun getColorScheme(isDark: Boolean): ThemeColorScheme {
        // Premium color scheme implementation
        return ThemeColorScheme(
            primary = 0xFFFF6B35,
            onPrimary = 0xFFFFFFFF,
            primaryContainer = 0xFFFFDAB9,
            onPrimaryContainer = 0xFF4A1500,
            secondary = 0xFFB565D8,
            onSecondary = 0xFFFFFFFF,
            secondaryContainer = 0xFFE8D5F2,
            onSecondaryContainer = 0xFF2E0A3D,
            tertiary = 0xFFFFC857,
            onTertiary = 0xFF000000,
            tertiaryContainer = 0xFFFFE8B3,
            onTertiaryContainer = 0xFF4A3800,
            error = 0xFFBA1A1A,
            onError = 0xFFFFFFFF,
            errorContainer = 0xFFFFDAD6,
            onErrorContainer = 0xFF410002,
            background = 0xFFFFF5E1,
            onBackground = 0xFF1A1A1A,
            surface = 0xFFFFFAF0,
            onSurface = 0xFF1A1A1A,
            surfaceVariant = 0xFFFFE4CC,
            onSurfaceVariant = 0xFF4A4A4A,
            outline = 0xFFB8B8B8,
            outlineVariant = 0xFFE0E0E0,
            scrim = 0xFF000000,
            inverseSurface = 0xFF2E2E2E,
            inverseOnSurface = 0xFFF5F5F5,
            inversePrimary = 0xFFFFB399
        )
    }
    override fun getExtraColors(isDark: Boolean): ThemeExtraColors {
        return ThemeExtraColors(
            bars = 0xFFFFF5E1,
            onBars = 0xFF000000,
            isBarLight = true
        )
    }
    override fun getTypography(): ThemeTypography? = null
    override fun getBackgroundAssets(): ThemeBackgrounds? = null
}
