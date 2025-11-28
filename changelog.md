### Changelog

## v2.0.0
### New Features
- **Advanced TTS**: Fully implemented advanced Text-to-Speech for desktop with word highlighter.
- **New Screens**: Added Donation, Security, and Reading Statistics screens.
- **Resume Book**: Added functionality to resume reading from the last position.

### UI Improvements
- **Source & Chapter**: Redesigned Source Detail and Chapter Report screens.
- **Desktop TTS**: Improved UI for Text-to-Speech on desktop.
- **Migration & Language**: Enhanced UI for Source Migration and Language selection.
- **General**: Various design improvements across multiple screens.

### Bug Fixes
- **Theming**: Fixed theming issues and navigation to the extension screen.
- **Workflow**: Fixed workflow build issues.
- **Downloads**: Fixed bug where downloaded chapters were missing checkmarks.
- **Sources**: Fixed issues with some sources not working.

### Technical
- **JS Engine**: Improved JavaScript engine performance.
- **Plugin Loading**: Optimized plugin loading to speed up startup.

## v1.0.46 (Upcoming)
### New Features
- **Leaderboard System**: Complete leaderboard implementation with Supabase integration
  - Real-time leaderboard with user rankings and statistics
  - Multiple leaderboard categories (reading time, books read, chapters completed)
  - User profile integration with avatars and achievements
  - Supabase database schema and verification scripts
- **Modern Statistics Screen**: Comprehensive reading statistics dashboard
  - Beautiful visualizations of reading habits and progress
  - Time-based analytics (daily, weekly, monthly, yearly)
  - Genre and source distribution charts
  - Reading streak tracking and milestones
- **Browse Settings Screen**: New dedicated settings for browsing and discovery
  - Customizable source preferences
  - Filter and sort options
  - Language and content preferences
- **LNReader Plugin Support**: Enhanced plugin loading system
  - Stub-based loading for faster startup times
  - Improved JavaScript plugin loader with better error handling
  - Android-specific optimizations for plugin management
  - Background plugin loading to improve app responsiveness

### Major UI Redesigns
- **Modern Extension Screen**: Complete redesign of the extensions/sources interface
  - Three design variants: Clean, Enhanced, and Modern
  - Beautiful catalog cards with improved visual hierarchy
  - Enhanced language filtering with multi-select support
  - Better organization of remote and user sources
  - Smooth animations and transitions
- **Modern Source Migration Screen**: Redesigned source migration interface
  - Step-by-step migration wizard
  - Better progress tracking and feedback
  - Improved error handling and recovery
  - Batch migration support
- **Book Detail Screen Redesign**: Enhanced book information display
  - Modern backdrop with gradient overlays
  - Improved book header with better cover presentation
  - New book stats card showing reading progress
  - Enhanced action buttons with better visual feedback
  - Modernized chapter list with improved filtering
  - Review summary cards with rating visualization
- **Downloader Screen Improvements**: Redesigned download management interface
  - Cleaner layout with better visual hierarchy
  - Enhanced progress indicators and status display
  - Improved batch operations
  - Better error handling and retry functionality

### Technical Improvements
- **Theme System Enhancements**: Major improvements to theming capabilities
  - New theme color utilities for better color management
  - Enhanced theme controller with improved state management
  - Better system bars integration (status bar, navigation bar)
  - Improved theme persistence and switching
  - Support for plugin-specific themes
- **Keyboard Handling**: New keyboard-aware components
  - KeyboardAwareContent composable for better keyboard interactions
  - Keyboard modifiers for improved input handling
  - Better focus management in forms and inputs
- **Repository Layer**: Enhanced data layer architecture
  - New LeaderboardRepository for leaderboard data management
  - Improved use case organization and dependency injection
  - Better separation of concerns
- **Download Service Optimization**: Improved download service performance
  - Better state management for downloads
  - Improved error handling and retry logic
  - Enhanced progress tracking
  - More efficient resource usage

### Bug Fixes
- **Workflow Build**: Fixed multiple workflow build issues
- **AutoRepairChapterUseCase**: Fixed chapter repair functionality
- **Downloader Screen**: Fixed various UI and functionality issues
- **Source Migration**: Improved reliability and error handling
- **Book Opening**: Fixed issue where the same book would open multiple times
- **GitHub Issues**: Simplified GitHub issue request handling

### Performance Improvements
- **Plugin Loading**: Significantly faster plugin loading with stub-based approach
- **Background Loading**: Plugins now load in background to prevent UI blocking
- **Catalog Installation**: Optimized catalog installer and loader
- **Image Loading**: Improved catalog image fetching and caching
### UI Improvements
- **Enhanced Settings Screens**: Modernized all settings screens with Material Design 3 components
  - Improved Appearance settings with better theme selection and color customization
  - Enhanced General settings with organized preference groups and clear sections
  - Updated Advanced settings with descriptive subtitles and better visual hierarchy
  - Polished About screen with improved logo, version info, and social links
- **New Reusable Components**: Created comprehensive UI component library
  - `RowPreference`: Flexible preference row with icon, subtitle, and trailing content support
  - `SectionHeader`: Styled headers for grouping related preferences
  - `EnhancedCard`: Material Design 3 card component with elevation and proper spacing
  - `NavigationRowPreference`: Preference row with navigation indicator
  - `PreferenceGroup`: Utility for creating preference groups with headers
- **Explore Screen Enhancements**: Improved novel browsing experience
  - Enhanced novel card design with better cover image presentation
  - Improved grid layout with adaptive column count
  - Modernized filter bottom sheet with better organization
  - Added smooth animations and visual feedback
- **WebView Improvements**: Better novel fetching experience
  - Fetch button now always enabled regardless of page load state
  - Automatic novel fetching with content detection
  - User preference for auto-fetch behavior
  - Improved error handling and retry functionality
- **Browser Engine Optimization**: Faster page loading and better parsing
  - Selective resource loading for improved performance
  - Enhanced HTML parsing algorithms
  - Better error handling and recovery
- **Theme System Enhancements**: More customization options
  - Expanded collection of preset themes
  - Real-time color preview for customization
  - Improved theme persistence
- **Detail Screen Improvements**: Better book information display
  - Enhanced header with improved cover image presentation
  - Better metadata organization
  - Optimized chapter list rendering
- **Download Screen Updates**: Clearer download management
  - Enhanced progress indicators
  - Better status icons and colors
  - Improved action button layout
  - Batch operation support
- **Category Screen Polish**: Improved category management
  - Better drag handle visibility
  - Enhanced reorder animations
  - Improved add/edit dialogs
  - Undo functionality for deletions
- **Accessibility Improvements**: Better support for all users
  - Content descriptions for all interactive elements
  - Minimum 48dp touch targets
  - WCAG AA compliant color contrast
  - Proper semantic structure for screen readers
- **Performance Optimizations**: Smoother experience throughout
  - Optimized list rendering with proper keys
  - Improved image loading and caching
  - Better scroll performance (60 FPS target)

## v0.1.29
- fix some source not working