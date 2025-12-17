# IReader Plugin API

A Kotlin Multiplatform library for creating IReader plugins. This API allows developers to extend IReader with custom themes, TTS engines, translation services, AI features, catalog sources, and more.

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.ireaderorg:plugin-api:2.0.0")
}
```

## Plugin Types

| Type | Interface | Description |
|------|-----------|-------------|
| Theme | `ThemePlugin` | Custom visual themes |
| TTS | `TTSPlugin` | Text-to-speech engines |
| Gradio TTS | `GradioTTSPlugin` | Gradio-based TTS (Coqui, XTTS) |
| Translation | `TranslationPlugin` | Translation services |
| AI | `AIPlugin` | AI text processing (summarization, Q&A) |
| AI Image | `AIImageGeneratorPlugin` | Character portraits, book covers |
| Catalog | `CatalogPlugin` | Content sources (LNReader, UserSource) |
| Image Processing | `ImageProcessingPlugin` | Image upscaling, enhancement |
| Sync | `SyncPlugin` | Data synchronization |
| Community Screen | `CommunityScreenPlugin` | Custom UI screens |
| Glossary | `GlossaryPlugin` | Term/dictionary management |
| Feature | `FeaturePlugin` | Custom features |

## Quick Examples

### Theme Plugin

```kotlin
class OceanThemePlugin : ThemePlugin {
    override val manifest = PluginManifest(
        id = "com.example.ocean-theme",
        name = "Ocean Theme",
        version = "1.0.0",
        versionCode = 1,
        description = "A calming ocean-inspired theme",
        author = PluginAuthor("Developer Name"),
        type = PluginType.THEME,
        permissions = emptyList(),
        minIReaderVersion = "2.0.0",
        platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP)
    )
    
    override fun initialize(context: PluginContext) {}
    override fun cleanup() {}
    
    override fun getColorScheme(isDark: Boolean) = if (isDark) darkColors else lightColors
    override fun getExtraColors(isDark: Boolean) = ThemeExtraColors(
        bars = 0xFF1A237E,
        onBars = 0xFFFFFFFF,
        isBarLight = false
    )
}
```

### Gradio TTS Plugin (Coqui/XTTS)

```kotlin
class CoquiTTSPlugin : GradioTTSPlugin {
    override val manifest = PluginManifest(
        id = "com.example.coqui-tts",
        name = "Coqui TTS",
        version = "1.0.0",
        versionCode = 1,
        description = "High-quality TTS via Coqui/XTTS",
        author = PluginAuthor("Developer Name"),
        type = PluginType.GRADIO_TTS,
        permissions = listOf(PluginPermission.NETWORK, PluginPermission.GRADIO_ACCESS),
        minIReaderVersion = "2.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP)
    )
    
    override val gradioConfig = GradioConfig(
        defaultEndpoint = "http://localhost:7860",
        supportsCustomEndpoint = true,
        supportsStreaming = true,
        supportsVoiceCloning = true
    )
    
    override val availableModels = listOf(
        GradioTTSModel(
            id = "xtts_v2",
            name = "XTTS v2",
            languages = listOf("en", "es", "fr", "de", "ja", "zh"),
            supportsCloning = true,
            supportsStreaming = true,
            quality = 5
        )
    )
    
    override suspend fun synthesize(request: GradioTTSRequest): GradioResult<GradioAudioResponse> {
        // Call Gradio API
    }
}
```

### Catalog Plugin (LNReader/UserSource)

```kotlin
class LNReaderCatalogPlugin : CatalogPlugin {
    override val manifest = PluginManifest(
        id = "com.example.lnreader-catalog",
        name = "LNReader Catalog",
        version = "1.0.0",
        versionCode = 1,
        description = "LNReader novel sources",
        author = PluginAuthor("Developer Name"),
        type = PluginType.CATALOG,
        permissions = listOf(PluginPermission.NETWORK, PluginPermission.CATALOG_WRITE),
        minIReaderVersion = "2.0.0",
        platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP)
    )
    
    override val catalogType = CatalogType.LNREADER
    
    override val catalogInfo = CatalogInfo(
        name = "LNReader Sources",
        description = "Novel sources from LNReader",
        languages = listOf("en", "ja", "zh"),
        contentTypes = listOf(ContentType.LIGHT_NOVEL, ContentType.WEB_NOVEL)
    )
    
    override suspend fun getSources(): List<CatalogSource> {
        // Return available sources
    }
    
    override suspend fun search(query: String, filters: SearchFilters): CatalogResult<List<CatalogItem>> {
        // Search across sources
    }
}
```

### AI Image Generator Plugin (Character Portraits)

```kotlin
class StableDiffusionPlugin : AIImageGeneratorPlugin {
    override val manifest = PluginManifest(
        id = "com.example.stable-diffusion",
        name = "Stable Diffusion",
        version = "1.0.0",
        versionCode = 1,
        description = "Generate character portraits with Stable Diffusion",
        author = PluginAuthor("Developer Name"),
        type = PluginType.AI,
        permissions = listOf(
            PluginPermission.LOCAL_SERVER,
            PluginPermission.CHARACTER_DATABASE,
            PluginPermission.IMAGE_PROCESSING
        ),
        minIReaderVersion = "2.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP)
    )
    
    override val generationCapabilities = listOf(
        ImageGenCapability.CHARACTER_PORTRAIT,
        ImageGenCapability.BOOK_COVER,
        ImageGenCapability.SCENE_ILLUSTRATION
    )
    
    override val serverConfig = ImageGenServerConfig(
        defaultEndpoint = "http://localhost:7860",
        apiType = ImageGenApiType.AUTOMATIC1111
    )
    
    override suspend fun generateCharacterPortrait(
        character: CharacterDescription,
        style: ImageStyle,
        options: ImageGenOptions
    ): ImageGenResult<GeneratedImage> {
        val prompt = buildCharacterPrompt(character, style)
        // Call Stable Diffusion API
    }
}
```

### Sync Plugin (Local Server)

```kotlin
class LocalSyncPlugin : SyncPlugin {
    override val manifest = PluginManifest(
        id = "com.example.local-sync",
        name = "Local Server Sync",
        version = "1.0.0",
        versionCode = 1,
        description = "Sync data to local server",
        author = PluginAuthor("Developer Name"),
        type = PluginType.SYNC,
        permissions = listOf(PluginPermission.LOCAL_SERVER, PluginPermission.SYNC_DATA),
        minIReaderVersion = "2.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP)
    )
    
    override val syncType = SyncType.LOCAL_SERVER
    
    override val syncConfig = SyncConfig(
        defaultEndpoint = "http://192.168.1.100:8080",
        supportsAutoSync = true,
        supportsIncrementalSync = true
    )
    
    override val supportedDataTypes = listOf(
        SyncDataType.READING_PROGRESS,
        SyncDataType.LIBRARY,
        SyncDataType.BOOKMARKS,
        SyncDataType.SETTINGS
    )
    
    override suspend fun sync(data: SyncData): SyncResult<SyncResponse> {
        // Sync to local server
    }
}
```

### Image Processing Plugin (Upscaling)

```kotlin
class RealESRGANPlugin : ImageProcessingPlugin {
    override val manifest = PluginManifest(
        id = "com.example.real-esrgan",
        name = "Real-ESRGAN Upscaler",
        version = "1.0.0",
        versionCode = 1,
        description = "Upscale images with Real-ESRGAN",
        author = PluginAuthor("Developer Name"),
        type = PluginType.IMAGE_PROCESSING,
        permissions = listOf(PluginPermission.LOCAL_SERVER, PluginPermission.IMAGE_PROCESSING),
        minIReaderVersion = "2.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP)
    )
    
    override val capabilities = listOf(
        ImageCapability.UPSCALE,
        ImageCapability.DENOISE,
        ImageCapability.FACE_ENHANCE
    )
    
    override val serverConfig = ImageServerConfig(
        defaultEndpoint = "http://localhost:7861"
    )
    
    override suspend fun processImage(request: ImageProcessRequest): ImageResult<ProcessedImage> {
        // Call Real-ESRGAN server
    }
}
```

### Glossary Plugin

```kotlin
class TranslationGlossaryPlugin : GlossaryPlugin {
    override val manifest = PluginManifest(
        id = "com.example.translation-glossary",
        name = "Translation Glossary",
        version = "1.0.0",
        versionCode = 1,
        description = "Manage translation glossaries",
        author = PluginAuthor("Developer Name"),
        type = PluginType.GLOSSARY,
        permissions = listOf(PluginPermission.GLOSSARY_ACCESS, PluginPermission.STORAGE),
        minIReaderVersion = "2.0.0",
        platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP)
    )
    
    override val glossaryType = GlossaryType.TRANSLATION
    
    override val glossaryConfig = GlossaryConfig(
        supportsFuzzyMatch = true,
        supportedImportFormats = listOf(GlossaryFormat.JSON, GlossaryFormat.CSV)
    )
    
    override suspend fun lookupTerm(
        term: String,
        glossaryIds: List<String>,
        options: LookupOptions
    ): GlossaryResult<List<GlossaryEntry>> {
        // Look up term
    }
    
    override suspend fun applyGlossary(
        text: String,
        glossaryIds: List<String>,
        options: ApplyOptions
    ): GlossaryResult<GlossaryApplyResult> {
        // Apply glossary replacements to text
    }
}
```

### Community Screen Plugin

```kotlin
class ForumPlugin : CommunityScreenPlugin {
    override val manifest = PluginManifest(
        id = "com.example.forum",
        name = "Community Forum",
        version = "1.0.0",
        versionCode = 1,
        description = "Community discussion forum",
        author = PluginAuthor("Developer Name"),
        type = PluginType.COMMUNITY_SCREEN,
        permissions = listOf(PluginPermission.NETWORK, PluginPermission.UI_INJECTION),
        minIReaderVersion = "2.0.0",
        platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP)
    )
    
    override val screens = listOf(
        CommunityScreen(
            id = "forum",
            title = "Forum",
            icon = "forum",
            route = "community/forum",
            type = ScreenType.LIST,
            supportsPullToRefresh = true,
            supportsInfiniteScroll = true
        )
    )
    
    override val navigationItems = listOf(
        CommunityNavItem(
            screenId = "forum",
            label = "Forum",
            icon = "forum",
            placement = NavPlacement.MORE_MENU
        )
    )
    
    override suspend fun getScreenContent(
        screenId: String,
        params: Map<String, String>
    ): CommunityResult<ScreenContent> {
        // Return forum posts
    }
}
```

## Permissions

Plugins must declare required permissions in their manifest:

| Permission | Description |
|------------|-------------|
| `NETWORK` | Make HTTP requests |
| `STORAGE` | Access local storage |
| `READER_CONTEXT` | Access current reading state |
| `LIBRARY_ACCESS` | Access user's library |
| `PREFERENCES` | Read/write preferences |
| `NOTIFICATIONS` | Show notifications |
| `CATALOG_WRITE` | Add/modify catalog sources |
| `SYNC_DATA` | Sync user data |
| `BACKGROUND_SERVICE` | Run in background |
| `LOCAL_SERVER` | Connect to local servers |
| `IMAGE_PROCESSING` | Process images |
| `UI_INJECTION` | Inject custom UI |
| `GLOSSARY_ACCESS` | Access glossaries |
| `CHARACTER_DATABASE` | Access character database |
| `AUDIO_PLAYBACK` | Access audio system |
| `GRADIO_ACCESS` | Access Gradio endpoints |

## Monetization

Plugins support three monetization models:

1. **Free** - No cost
2. **Premium** - One-time purchase with optional trial
3. **Freemium** - Free base with purchasable features

## Plugin Repository

Plugins can be distributed through:
- Official IReader Plugin Repository
- Third-party repositories
- Direct installation from file/URL

## Utilities

### HTTP Client

```kotlin
val client = context.getHttpClient()
val response = client?.get("https://api.example.com/data")
```

### JSON Helper

```kotlin
import ireader.plugin.api.util.JsonHelper

val json = JsonHelper.encode(myObject)
val obj = JsonHelper.decode<MyClass>(jsonString)
```

### String Extensions

```kotlin
import ireader.plugin.api.util.*

val encoded = "hello world".urlEncode()
val decoded = "hello%20world".urlDecode()
val clean = "<p>Hello</p>".stripHtml()
```

## License

Mozilla Public License 2.0
