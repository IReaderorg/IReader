# Advanced Reader System and Security Features

## Overview

This document describes the comprehensive reader system and security features implemented in IReader, inspired by Mihon's proven patterns. The system provides 50+ customization options for reading experience, advanced color filtering, multiple viewer modes, and robust security features.

**Requirements Coverage:**
- 5.1: Advanced reader customization
- 5.2: Color filter system
- 11.1: Accessibility features
- 11.2: Security preferences
- 11.4: Privacy controls
- 11.5: Incognito mode
- 8.1: Performance optimization
- 8.2: Reading statistics tracking

## Architecture

### Components

1. **ReaderPreferences** - Comprehensive preference management with 50+ options
2. **SecurityPreferences** - App lock and secure screen management
3. **PrivacyPreferences** - Privacy and data collection controls
4. **ColorFilterManager** - Color filter and brightness management
5. **ViewerConfigManager** - Viewer mode and navigation configuration
6. **TrackReadingStatistics** - Reading time and progress tracking
7. **SecurityManager** - Security feature orchestration

### Data Models

- **ReaderStatistics** - Reading time, pages read, chapters completed
- **ReadingSession** - Current session tracking
- **PageProgress** - Page-level progress tracking
- **ColorFilter** - Color filter configuration with blend modes
- **ViewerConfig** - Viewer mode and display settings
- **NavigationConfig** - Navigation controls and tap zones

## Features

### 1. Reader Preferences (50+ Options)

#### Page Transitions and Animations
```kotlin
// Enable smooth page transitions
readerPreferences.pageTransitions().set(true)

// Flash effect on page change
readerPreferences.flashOnPageChange().set(true)
readerPreferences.flashDurationMillis().set(100)
readerPreferences.flashColor().set(FlashColor.BLACK)

// Double tap animation speed
readerPreferences.doubleTapAnimSpeed().set(500)
```

#### Display Settings
```kotlin
// Show page numbers
readerPreferences.showPageNumber().set(true)

// Fullscreen mode
readerPreferences.fullscreen().set(true)

// Keep screen on while reading
readerPreferences.keepScreenOn().set(true)

// Draw under display cutout
readerPreferences.drawUnderCutout().set(true)
```

#### Viewer Modes
```kotlin
// Set default reader mode
readerPreferences.defaultReaderMode().set(ReaderMode.RIGHT_TO_LEFT.flagValue)

// Available modes:
// - LEFT_TO_RIGHT
// - RIGHT_TO_LEFT
// - VERTICAL
// - WEBTOON
// - CONTINUOUS_VERTICAL

// Set orientation
readerPreferences.defaultOrientationType().set(ReaderOrientation.FREE.flagValue)
```

#### Image Scaling and Zoom
```kotlin
// Image scale type
readerPreferences.imageScaleType().set(ImageScaleType.FIT_SCREEN.ordinal)

// Available scale types:
// - FIT_SCREEN: Fit to screen dimensions
// - STRETCH: Stretch to fill screen
// - FIT_WIDTH: Fit to screen width
// - FIT_HEIGHT: Fit to screen height
// - ORIGINAL_SIZE: Display at original size
// - SMART_FIT: Intelligent fitting based on image dimensions

// Zoom start position
readerPreferences.zoomStart().set(ZoomStart.AUTOMATIC.ordinal)

// Landscape zoom
readerPreferences.landscapeZoom().set(true)

// Navigate to pan (zoom on tap)
readerPreferences.navigateToPan().set(true)
```

#### Cropping and Padding
```kotlin
// Crop borders for paged mode
readerPreferences.cropBorders().set(false)

// Crop borders for webtoon mode
readerPreferences.cropBordersWebtoon().set(false)

// Webtoon side padding (0-25)
readerPreferences.webtoonSidePadding().set(0)

// Disable zoom out in webtoon mode
readerPreferences.webtoonDisableZoomOut().set(false)
```

#### Dual Page Support
```kotlin
// Enable dual page split
readerPreferences.dualPageSplitPaged().set(false)

// Invert dual page order
readerPreferences.dualPageInvertPaged().set(false)

// Rotate to fit dual pages
readerPreferences.dualPageRotateToFit().set(false)

// Webtoon dual page support
readerPreferences.dualPageSplitWebtoon().set(false)
readerPreferences.dualPageInvertWebtoon().set(false)
```

### 2. Color Filter System

#### Basic Color Filtering
```kotlin
val colorFilterManager = ColorFilterManager(readerPreferences)

// Enable color filter
colorFilterManager.enableColorFilter()

// Set color value (ARGB format)
colorFilterManager.setColorFilterValue(0x40704214) // Sepia tone

// Set blend mode
colorFilterManager.setBlendMode(ColorFilterBlendMode.MULTIPLY)

// Available blend modes:
// - DEFAULT: Standard overlay
// - MULTIPLY: Multiply colors
// - SCREEN: Screen blend
// - OVERLAY: Overlay blend
// - LIGHTEN: Lighten colors
// - DARKEN: Darken colors
```

#### Custom Brightness
```kotlin
// Enable custom brightness (0-100)
colorFilterManager.enableCustomBrightness(30)

// Disable custom brightness
colorFilterManager.disableCustomBrightness()
```

#### Special Effects
```kotlin
// Grayscale mode
colorFilterManager.toggleGrayscale()

// Inverted colors (dark mode)
colorFilterManager.toggleInvertedColors()
```

#### Predefined Presets
```kotlin
// Apply sepia preset
colorFilterManager.updateColorFilter(ColorFilterPresets.SEPIA)

// Apply blue light filter
colorFilterManager.updateColorFilter(ColorFilterPresets.BLUE_LIGHT_FILTER)

// Apply night mode
colorFilterManager.updateColorFilter(ColorFilterPresets.NIGHT_MODE)

// Apply grayscale
colorFilterManager.updateColorFilter(ColorFilterPresets.GRAYSCALE)

// Apply dark mode
colorFilterManager.updateColorFilter(ColorFilterPresets.DARK_MODE)
```

### 3. Navigation Controls

#### Tap Zone Configuration
```kotlin
val viewerConfigManager = ViewerConfigManager(readerPreferences)

// Set tap zone mode
readerPreferences.navigationModePager().set(TapZoneMode.DEFAULT.ordinal)

// Available tap zone modes:
// - DEFAULT: Standard left/right tapping
// - L_SHAPED: L-shaped navigation zones
// - KINDLE: Kindle-style zones
// - EDGE: Edge-only navigation
// - RIGHT_AND_LEFT: Right and left edges only
// - DISABLED: Tap navigation disabled
```

#### Volume Key Navigation
```kotlin
// Enable volume key navigation
viewerConfigManager.enableVolumeKeyNavigation(inverted = false)

// Disable volume key navigation
viewerConfigManager.disableVolumeKeyNavigation()
```

#### Tapping Inversion
```kotlin
// Set tapping invert mode
readerPreferences.pagerNavInverted().set(TappingInvertMode.HORIZONTAL)

// Available modes:
// - NONE: No inversion
// - HORIZONTAL: Invert horizontal tapping
// - VERTICAL: Invert vertical tapping
// - BOTH: Invert both directions
```

#### Long Tap
```kotlin
// Enable long tap for menu
readerPreferences.readWithLongTap().set(true)
```

### 4. Security Features

#### App Lock with Biometric Authentication
```kotlin
val securityManager = SecurityManager(securityPreferences)

// Enable app lock
securityManager.enableAppLock(lockAfterMinutes = 5)

// Lock immediately
securityManager.enableAppLock(lockAfterMinutes = 0)

// Never auto-lock
securityManager.enableAppLock(lockAfterMinutes = -1)

// Disable app lock
securityManager.disableAppLock()

// Check if app should be locked
if (securityManager.shouldLockApp()) {
    // Show authentication screen
}
```

#### Secure Screen Protection
```kotlin
// Set secure screen mode
securityManager.setSecureScreenMode(SecureScreenMode.INCOGNITO)

// Available modes:
// - ALWAYS: Always prevent screenshots
// - INCOGNITO: Prevent screenshots in incognito mode
// - NEVER: Never prevent screenshots

// Check if secure screen should be enabled
if (securityManager.shouldEnableSecureScreen()) {
    // Apply secure screen flag
    window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
}
```

#### Incognito Mode
```kotlin
// Toggle incognito mode
securityManager.toggleIncognitoMode()

// Check if incognito mode is active
if (securityManager.isIncognitoMode()) {
    // Don't track history
    // Hide notification content
}
```

#### Notification Privacy
```kotlin
// Hide notification content
securityPreferences.hideNotificationContent().set(true)

// Check if content should be hidden
if (securityManager.shouldHideNotificationContent()) {
    // Show generic notification without details
}
```

### 5. Reading Statistics Tracking

#### Session Tracking
```kotlin
val trackingUseCase = TrackReadingStatistics(readerPreferences)

// Start reading session
val session = trackingUseCase.startSession(
    bookId = 123,
    chapterId = 456
)

// Track page reads
trackingUseCase.trackPageRead()

// End session
trackingUseCase.endSession(session, pagesRead = 10)

// Increment chapters completed
trackingUseCase.incrementChaptersCompleted()
```

#### Statistics Retrieval
```kotlin
// Get current statistics
val stats = trackingUseCase.getStatistics()

println("Total reading time: ${stats.getFormattedReadingTime()}")
println("Pages read: ${stats.pagesRead}")
println("Chapters completed: ${stats.chaptersCompleted}")
println("Reading speed: ${stats.calculateReadingSpeed()} pages/hour")

// Observe statistics as Flow
trackingUseCase.observeStatistics().collect { stats ->
    updateUI(stats)
}
```

#### Reset Statistics
```kotlin
// Reset all statistics
trackingUseCase.resetStatistics()
```

### 6. Privacy Controls

```kotlin
val privacyPreferences = PrivacyPreferences(preferenceStore)

// Enable/disable crash reporting
privacyPreferences.crashReport().set(true)

// Enable/disable analytics
privacyPreferences.analyticsEnabled().set(false)

// Enable/disable telemetry
privacyPreferences.telemetryEnabled().set(false)

// Clear history on exit
privacyPreferences.clearHistoryOnExit().set(true)

// Allow anonymous usage data
privacyPreferences.anonymousUsageData().set(false)
```

## Usage Examples

### Complete Reader Configuration
```kotlin
class ReaderViewModel(
    private val readerPreferences: ReaderPreferences,
    private val colorFilterManager: ColorFilterManager,
    private val viewerConfigManager: ViewerConfigManager,
    private val trackingUseCase: TrackReadingStatistics,
) : ViewModel() {
    
    fun setupReader() {
        viewModelScope.launch {
            // Configure viewer
            val viewerConfig = ViewerConfig(
                readerMode = ReaderMode.RIGHT_TO_LEFT,
                orientation = ReaderOrientation.FREE,
                imageScaleType = ImageScaleType.FIT_SCREEN,
                cropBorders = true,
                dualPageSplit = false,
                landscapeZoom = true
            )
            viewerConfigManager.updateViewerConfig(viewerConfig)
            
            // Configure color filter
            val colorFilter = ColorFilterPresets.NIGHT_MODE
            colorFilterManager.updateColorFilter(colorFilter)
            
            // Configure navigation
            val navConfig = NavigationConfig(
                tapZoneMode = TapZoneMode.DEFAULT,
                volumeKeyNavigation = true,
                volumeKeyInverted = false,
                longTapEnabled = true
            )
            viewerConfigManager.updateNavigationConfig(navConfig)
            
            // Start tracking session
            val session = trackingUseCase.startSession(bookId, chapterId)
        }
    }
}
```

### Security Setup
```kotlin
class SecurityViewModel(
    private val securityManager: SecurityManager,
    private val privacyPreferences: PrivacyPreferences,
) : ViewModel() {
    
    fun setupSecurity() {
        viewModelScope.launch {
            // Enable app lock with 5-minute timeout
            securityManager.enableAppLock(lockAfterMinutes = 5)
            
            // Set secure screen for incognito mode
            securityManager.setSecureScreenMode(SecureScreenMode.INCOGNITO)
            
            // Hide notification content
            securityPreferences.hideNotificationContent().set(true)
            
            // Disable analytics for privacy
            privacyPreferences.analyticsEnabled().set(false)
            privacyPreferences.telemetryEnabled().set(false)
        }
    }
}
```

## Testing

### Unit Tests
```kotlin
class ReaderPreferencesTest {
    private lateinit var preferences: ReaderPreferences
    
    @Test
    fun `test color filter configuration`() = runTest {
        val manager = ColorFilterManager(preferences)
        
        manager.enableColorFilter()
        manager.setColorFilterValue(0x40704214)
        manager.setBlendMode(ColorFilterBlendMode.MULTIPLY)
        
        val filter = manager.getColorFilter()
        assertTrue(filter.enabled)
        assertEquals(0x40704214, filter.colorValue)
        assertEquals(ColorFilterBlendMode.MULTIPLY, filter.blendMode)
    }
    
    @Test
    fun `test reading statistics tracking`() = runTest {
        val tracking = TrackReadingStatistics(preferences)
        
        val session = tracking.startSession(1L, 1L)
        tracking.trackPageRead()
        tracking.trackPageRead()
        tracking.endSession(session, 2)
        
        val stats = tracking.getStatistics()
        assertEquals(2L, stats.pagesRead)
    }
}
```

## Performance Considerations

1. **Preference Caching**: All preferences are cached in memory for fast access
2. **Flow-based Updates**: Use Flow for reactive updates instead of polling
3. **Lazy Initialization**: Managers are initialized only when needed
4. **Efficient Statistics**: Statistics are updated incrementally, not recalculated

## Migration Guide

### From Basic Reader to Advanced Reader

```kotlin
// Old approach
val fontSize = readerPreferences.fontSize().get()
val brightness = readerPreferences.brightness().get()

// New approach with comprehensive configuration
val viewerConfig = viewerConfigManager.getViewerConfig()
val colorFilter = colorFilterManager.getColorFilter()
val navConfig = viewerConfigManager.getNavigationConfig()
```

## Future Enhancements

1. **Cloud Sync**: Sync preferences across devices
2. **Reading Profiles**: Multiple preset configurations
3. **Advanced Analytics**: Reading pattern analysis
4. **Gesture Customization**: Custom gesture actions
5. **Voice Commands**: Voice-controlled navigation

## References

- Mihon ReaderPreferences: `mihon-main/app/src/main/java/eu/kanade/tachiyomi/ui/reader/setting/ReaderPreferences.kt`
- Mihon SecurityPreferences: `mihon-main/core/common/src/main/kotlin/eu/kanade/tachiyomi/core/security/SecurityPreferences.kt`
- Requirements: 5.1, 5.2, 11.1, 11.2, 11.4, 11.5, 8.1, 8.2
