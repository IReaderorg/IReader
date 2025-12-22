### Changelog

## v2.0.6 (Upcoming)

### New Features
- **Source Health Checking**: Added comprehensive source health monitoring and error handling system
- **App Update Service**: New in-app update download service with progress UI
- **Community Translations**: Added community-driven translation feature with plugin support
- **Plugin Reviews & Updates**: Plugin review system and automatic update checking
- **Content Filter**: New content filtering system for sources
- **Unified Source & Plugin API**: Consolidated API for sources and plugins
- **PluginConfig API**: New configuration system for plugins
- **Process State Manager**: Better app lifecycle and state management

### UI Improvements
- **Reader Hub Redesign**: Completely redesigned reader hub interface
- **Source Detail Screen**: Modernized source detail screen design
- **Plugin Details Screen**: Redesigned plugin details and feature store screens
- **Required Plugin Screen**: Improved UI for plugin requirements

### Performance
- **Compose List Optimization**: Native-like scrolling performance with optimized lazy lists
- **Startup Optimization**: Faster app startup with plugin error fixes
- **Composable Optimization**: Replaced modifier extensions for better performance
- **Explicit List Keys**: Added proper keys to lazy lists for efficient recomposition

### Bug Fixes
- **TTS Screen**: Fixed double loading issue in TTS screen
- **Navigation Crashes**: Prevented navigation-related crashes
- **Book Detail Screen**: Fixed data not loading when re-entering from explore screen
- **Book Detail Padding**: Fixed bottom padding issues
- **Plugin Details**: Fixed plugin details screen issues
- **Plugin Reviews**: Fixed plugin review handling
- **V8 Engine Threading**: Ensured each V8 engine instance uses a dedicated thread
- **Desktop TTS Stability**: Improved TTS stability on desktop

### Technical
- **J2V8 Engine Optimization**: Optimized J2V8 engine with pending sources support
- **JS/TTS Plugin Architecture**: Made JS engine and PiperTTS optional plugins
- **GradioTTS Manager**: Refactored TTS configuration management
- **Translation Plugin System**: New translation plugin architecture
- **Coroutine Cleanup**: Improved code structure and coroutine handling
- **iOS Compatibility**: Fixed multiplatform compatibility issues for iOS builds

### Localization
- **String Externalization**: Converted hardcoded strings to strings.xml
- **Translation Updates**: Updated translations across supported languages

---

## v2.0.5

### New Features
- **Plugin Architecture v2**: Complete plugin system redesign with improved loading
- **Legado Source Import**: Import sources from Legado app
- **SAF Support & Hybrid Installer**: Storage Access Framework support with hybrid installation
- **App Updater & Onboarding**: In-app updater and new user onboarding screen
- **Custom Book Covers**: Support for user-defined book covers
- **User-Defined Sources**: Legado-style custom source definitions
- **Feature Store**: Plugin marketplace for monetization
- **Chapter Art AI Generation**: AI-powered chapter art generation
- **Character Art Gallery**: Display character art in book details
- **Multi-Provider AI Images**: Support for multiple AI image generation providers
- **Global Glossary**: Shared glossary with admin user management
- **Continuous TTS**: Seamless text-to-speech across chapters

### UI Improvements
- **Unified Sources Screen**: Consolidated source management interface
- **Storage Permission Screen**: Dedicated permission handling screen
- **Plugin Installer**: New plugin installation interface
- **Add Repository Screen**: Improved repository management UI

### Technical
- **iOS Module Support**: Initial iOS platform support (experimental)
- **KMP Migration**: Continued Kotlin Multiplatform migration
- **Database Optimization**: Optimized for older devices (pre-2018)
- **SQLDelight Formatting**: Cleaned up database schema files

---

## v2.0.4

### New Features
- **Leaderboard System**: Complete leaderboard with Supabase integration
- **Modern Statistics Screen**: Comprehensive reading statistics dashboard
- **Browse Settings Screen**: Dedicated browsing preferences
- **LNReader Plugin Support**: Enhanced JavaScript plugin loading

### Major UI Redesigns
- **Extension Screen**: Three design variants (Clean, Enhanced, Modern)
- **Source Migration Screen**: Step-by-step migration wizard
- **Book Detail Screen**: Modern backdrop with gradient overlays
- **Downloader Screen**: Cleaner download management interface

### Technical Improvements
- **Theme System**: Enhanced theming with better color management
- **Keyboard Handling**: New keyboard-aware components
- **Repository Layer**: Enhanced data architecture
- **Download Service**: Optimized download performance

---

## v2.0.3

### New Features
- **Advanced TTS**: Fully implemented advanced Text-to-Speech for desktop with word highlighter
- **New Screens**: Added Donation, Security, and Reading Statistics screens
- **Resume Book**: Added functionality to resume reading from the last position

### UI Improvements
- **Source & Chapter**: Redesigned Source Detail and Chapter Report screens
- **Desktop TTS**: Improved UI for Text-to-Speech on desktop
- **Migration & Language**: Enhanced UI for Source Migration and Language selection
- **General**: Various design improvements across multiple screens

### Bug Fixes
- **Theming**: Fixed theming issues and navigation to the extension screen
- **Workflow**: Fixed workflow build issues
- **Downloads**: Fixed bug where downloaded chapters were missing checkmarks
- **Sources**: Fixed issues with some sources not working

### Technical
- **JS Engine**: Improved JavaScript engine performance
- **Plugin Loading**: Optimized plugin loading to speed up startup

---

## v2.0.0
- Initial 2.0 release
