# IReader Plugin Architecture V2

## Vision

A comprehensive, scalable plugin system that enables:
- **TTS Engines** (Gradio, local, cloud)
- **AI Services** (Translation, Summarization, Image Generation)
- **Catalog Loaders** (LNReader, UserSource, custom sources)
- **Themes** (Visual customization)
- **Image Processing** (Upscaling, enhancement)
- **Sync Services** (Local server, cloud sync)
- **Community Screens** (Custom UI from plugins)
- **Glossary/Dictionary** (Term management)

## Plugin Type Hierarchy

```
Plugin (base)
├── ThemePlugin
├── TTSPlugin
│   ├── LocalTTSPlugin (system TTS)
│   ├── GradioTTSPlugin (Gradio/Coqui/XTTS)
│   └── CloudTTSPlugin (ElevenLabs, Azure)
├── TranslationPlugin
│   ├── AITranslationPlugin (LLM-based)
│   └── APITranslationPlugin (DeepL, Google)
├── AIPlugin
│   ├── SummarizerPlugin
│   ├── AIImageGeneratorPlugin (Character portraits, book covers)
│   └── TextAnalysisPlugin
├── CatalogPlugin
│   ├── LNReaderCatalog
│   ├── UserSourceCatalog
│   └── TachiyomiCatalog
├── ImageProcessingPlugin
│   ├── UpscalerPlugin (Real-ESRGAN)
│   └── EnhancerPlugin
├── SyncPlugin
│   ├── LocalSyncPlugin (local server)
│   ├── WebDAVSyncPlugin
│   └── CloudSyncPlugin
├── CommunityScreenPlugin
│   └── Custom UI screens (forums, recommendations)
├── GlossaryPlugin
│   ├── TranslationGlossary
│   ├── CharacterDatabase
│   └── DictionaryPlugin
└── FeaturePlugin
    └── Custom features
```

## Implemented Plugin Types

### 1. GradioTTSPlugin - Gradio-based TTS
Connect to Gradio endpoints for high-quality TTS:
- Coqui TTS
- XTTS v2 (with voice cloning)
- Custom Gradio endpoints
- Streaming support
- Voice cloning from reference audio

### 2. CatalogPlugin - Content Sources
Load novels from various sources:
- LNReader catalog format
- UserSource format
- Tachiyomi/Mihon extensions
- Custom implementations
- Search, popular, latest content
- Chapter content retrieval

### 3. AIImageGeneratorPlugin - Character AI
Generate images for the app:
- Character portraits from descriptions
- Book cover generation
- Scene illustrations
- Stable Diffusion (Automatic1111, ComfyUI)
- DALL-E, Stability AI support
- LoRA and ControlNet support

### 4. ImageProcessingPlugin - Image Enhancement
Upscale and enhance images:
- Real-ESRGAN upscaling (2x, 4x)
- Noise reduction
- Face enhancement
- Connect to local servers
- Batch processing

### 5. SyncPlugin - Data Synchronization
Sync data across devices:
- Local server sync (LAN)
- WebDAV support
- Cloud sync
- Reading progress, library, bookmarks
- Conflict resolution
- Auto-sync support

### 6. CommunityScreenPlugin - Custom UI
Add custom screens to the app:
- Forums and discussions
- Recommendations
- User profiles
- Custom navigation items
- Pull-to-refresh, infinite scroll

### 7. GlossaryPlugin - Term Management
Manage glossaries and dictionaries:
- Translation glossaries
- Character databases
- Import/export (JSON, CSV, TBX)
- Fuzzy matching
- Apply glossary to text

## Permission Model

```kotlin
enum class PluginPermission {
    // Core permissions
    NETWORK,            // Make HTTP requests
    STORAGE,            // Access local storage
    READER_CONTEXT,     // Access current reading state
    LIBRARY_ACCESS,     // Access user's library
    PREFERENCES,        // Read/write preferences
    NOTIFICATIONS,      // Show notifications
    
    // Extended permissions
    CATALOG_WRITE,      // Add/modify catalog sources
    SYNC_DATA,          // Sync user data
    BACKGROUND_SERVICE, // Run in background
    LOCAL_SERVER,       // Connect to local servers
    IMAGE_PROCESSING,   // Process images
    UI_INJECTION,       // Inject custom UI
    GLOSSARY_ACCESS,    // Access/modify glossaries
    CHARACTER_DATABASE, // Access character database
    AUDIO_PLAYBACK,     // Access audio system
    GRADIO_ACCESS,      // Access Gradio endpoints
}
```

## Architecture Principles

1. **Isolation**: Plugins run in sandboxed environments
2. **Versioning**: Semantic versioning with compatibility checks
3. **Hot-reload**: Plugins can be enabled/disabled without restart
4. **Dependency**: Plugins can declare dependencies on other plugins
5. **Marketplace**: Central repository for plugin discovery
6. **Cross-platform**: KMP support for Android, iOS, Desktop

## Plugin Context Services

Plugins can access various services through `PluginContext`:
- `getHttpClient()` - HTTP requests
- `getGlossaryService()` - Glossary operations
- `getCharacterService()` - Character database
- `getSyncService()` - Sync operations
- `getLibraryService()` - Library access
- `getReaderContext()` - Current reading state

## Plugin Lifecycle

Plugins can implement `PluginLifecycle` for:
- `onInstall()` - First-time setup
- `onUpdate()` - Version migrations
- `onUninstall()` - Cleanup
- `onAppStart/Background/Foreground/Terminate()`
- `onBookOpened/Closed()`
- `onChapterStarted/Finished()`
- `onBackgroundWork()` - Periodic background tasks

## Configuration UI

Plugins can implement `ConfigurablePlugin` to provide settings:
- Text, Number, Toggle inputs
- Select, MultiSelect dropdowns
- Slider, Color picker
- Server endpoint with connection test
- API key (masked)
- File picker
- Grouped options

## Additional Plugin Types

### 8. AISummarizerPlugin - Text Summarization
Dedicated AI summarization:
- Chapter summaries
- Book summaries
- "Previously on" recaps
- Key point extraction
- Plot point detection
- Streaming output

### 9. WebhookPlugin - External Notifications
Send notifications to external services:
- Discord, Telegram, Slack
- IFTTT, Pushover, Ntfy
- Custom webhooks
- Trigger-based notifications
- Notification templates

### 10. DownloadPlugin - Download Management
Custom download sources and accelerators:
- Multi-connection downloads
- Resume support
- Download queue management
- Bandwidth limiting
- Checksum verification

## Infrastructure Components

### Plugin Event Bus
Inter-plugin and plugin-to-app communication:
- Publish/subscribe events
- Request/response pattern
- Standard event types (reading, sync, TTS, AI)
- Priority-based delivery

### Batch Operations
Efficient bulk processing:
- Configurable batch sizes
- Parallel processing
- Progress tracking
- Retry logic
- Queue management

### Plugin Security
Sandboxing and resource limits:
- Permission management
- URL/path allowlists
- Resource limits (memory, storage, network)
- Security audit logging
- Signature verification
- Trust levels

### Local Server Support
Common infrastructure for local server plugins:
- Server discovery (mDNS)
- Connection management
- Health monitoring
- Common port definitions

## Plugin Registry

Plugin discovery and management:
- Install/uninstall plugins
- Enable/disable plugins
- Check for updates
- Dependency resolution
- Plugin marketplace integration

## Future Considerations

1. **Plugin Marketplace** - Central repository with ratings, reviews
2. **Plugin Bundles** - Install multiple related plugins together
3. **Plugin Sharing** - Share custom plugins with community
4. **Plugin Analytics** - Usage metrics and crash reporting
5. **Plugin Sandboxing** - Enhanced security isolation
6. **Plugin Hot-reload** - Update without restart
7. **Plugin Templates** - Starter templates for common plugin types
8. **Plugin Testing** - Testing framework for plugin developers
9. **Plugin Documentation** - Auto-generated API docs
