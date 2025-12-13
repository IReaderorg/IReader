# Plugin System Enhancements

This document describes the 8 innovative plugin system enhancements implemented for IReader.

## 1. AI Plugin Type - Local/Cloud AI Integration

### Overview
AI plugins provide intelligent text processing capabilities including summarization, character analysis, reading comprehension Q&A, and more.

### Features
- **Local LLM Support**: llama.cpp (GGUF), ONNX Runtime
- **Cloud AI Providers**: OpenAI, Anthropic (Claude), Google (Gemini)
- **Capabilities**:
  - Text summarization
  - Character analysis
  - Question answering
  - Text generation
  - Embeddings for semantic search

### Usage
```kotlin
// Initialize with local model
aiService.initializeLocal(LocalAIConfig(
    modelPath = "/path/to/model.gguf",
    modelFormat = ModelFormat.GGUF,
    numThreads = 4,
    contextSize = 4096
))

// Or initialize with cloud provider
aiService.initializeCloud(CloudAIConfig(
    provider = CloudProvider.OPENAI,
    apiKey = "your-api-key",
    model = "gpt-4"
))

// Use AI features
val summary = aiService.summarize(bookText)
val characters = aiService.analyzeCharacters(chapterText)
val answer = aiService.answerQuestion(context, "Who is the main character?")
```

### Files
- `plugin-api/.../AIPlugin.kt` - Plugin interface
- `domain/.../ai/AIService.kt` - Unified AI service
- `domain/.../ai/LocalAIProvider.kt` - Local inference
- `domain/.../ai/CloudAIProvider.kt` - Cloud providers

---

## 2. Plugin Composition/Chaining

### Overview
Chain plugins together to create powerful workflows (e.g., Translate → TTS).

### Features
- Pipeline builder pattern
- Conditional step execution
- Error handling and retry logic
- Progress tracking
- Pipeline templates

### Usage
```kotlin
val pipeline = PluginPipeline.builder(pluginResolver)
    .id("translate-and-speak")
    .name("Translate & Read Aloud")
    .addStep("translation-plugin", mapOf("targetLang" to "en"))
    .addStep("tts-plugin", mapOf("voice" to "default"))
    .build()

pipeline.events.collect { event ->
    when (event) {
        is PipelineEvent.Progress -> updateUI(event.progress)
        is PipelineEvent.Completed -> playAudio(event.finalResult)
    }
}

pipeline.execute(PipelineData.text(inputText))
```

### Files
- `domain/.../composition/PluginComposition.kt` - Data models
- `domain/.../composition/PluginPipeline.kt` - Pipeline execution
- `domain/.../composition/PipelineManager.kt` - Pipeline management

---

## 3. Plugin Hot-Reload (Development Mode)

### Overview
Live reload plugins during development without app restart.

### Features
- File system watching
- Automatic change detection
- State preservation during reload
- Rollback on failure
- Reload statistics

### Usage
```kotlin
// Enable hot reload
hotReloadManager.updateConfig(HotReloadConfig(
    enabled = true,
    autoReload = true,
    preserveState = true,
    watchPaths = listOf("/plugins/dev")
))

hotReloadManager.startWatching()

// Listen for events
hotReloadManager.events.collect { event ->
    when (event) {
        is HotReloadEvent.ReloadCompleted -> showSuccess()
        is HotReloadEvent.ReloadFailed -> showError(event.error)
    }
}
```

### Files
- `domain/.../hotreload/PluginHotReload.kt` - Data models
- `domain/.../hotreload/HotReloadManager.kt` - Hot reload logic

---

## 4. Plugin Marketplace Social Features

### Overview
Social features for the plugin marketplace including collections, follows, and recommendations.

### Features
- **Collections**: Curated plugin bundles by users
- **Developer Following**: Follow favorite developers
- **Recommendations**: Personalized plugin suggestions
- **Activity Feed**: See what followed developers are doing
- **Reviews**: Enhanced review system with developer responses

### Usage
```kotlin
// Create a collection
socialManager.createCollection(
    name = "Best Translation Plugins",
    description = "My favorite translation plugins",
    pluginIds = listOf("plugin1", "plugin2"),
    isPublic = true
)

// Follow a developer
socialManager.followDeveloper(developerId)

// Get recommendations
socialManager.loadRecommendations(installedPluginIds)
```

### Files
- `domain/.../marketplace/PluginMarketplaceSocial.kt` - Data models
- `domain/.../marketplace/MarketplaceSocialManager.kt` - Social features
- `supabase/.../schema_13_plugin_social.sql` - Database schema

---

## 5. Plugin Analytics Dashboard

### Overview
Comprehensive analytics for plugin developers including usage stats, crash reports, and A/B testing.

### Features
- **Usage Statistics**: DAU, WAU, MAU, retention
- **Crash Reports**: Grouped crashes, stack traces, breadcrumbs
- **A/B Testing**: Create experiments, track conversions
- **Performance Metrics**: Load times, memory usage
- **Developer Dashboard**: Unified view of all metrics

### Usage
```kotlin
// Track events
analyticsManager.trackPluginUsage(pluginId, "feature_used", durationMs)

// Report crashes
analyticsManager.reportCrash(pluginId, version, exception, breadcrumbs)

// Create A/B test
analyticsManager.createABTest(ABTest(
    pluginId = pluginId,
    name = "New UI Test",
    variants = listOf(
        ABTestVariant("control", "Original", weight = 0.5f),
        ABTestVariant("treatment", "New UI", weight = 0.5f)
    ),
    primaryMetric = "engagement_rate"
))

// Get variant for user
val variant = analyticsManager.getTestVariant(testId, userId)
```

### Files
- `domain/.../analytics/PluginAnalytics.kt` - Data models
- `domain/.../analytics/PluginAnalyticsManager.kt` - Analytics logic
- `supabase/.../schema_14_plugin_analytics.sql` - Database schema

---

## 6. Cross-Plugin Communication

### Overview
Plugins can expose APIs to other plugins and communicate via an event bus.

### Features
- **API Exposure**: Plugins can expose typed APIs
- **Event Bus**: Pub/sub messaging between plugins
- **Service Discovery**: Find plugins by capability
- **Request/Response**: Direct plugin-to-plugin calls

### Usage
```kotlin
// Register an API
communicationManager.registerApiProvider(plugin, object : ApiProvider {
    override fun getExposedApis() = listOf(myApi)
    override suspend fun handleApiCall(apiId, method, params) = ...
})

// Call another plugin's API
val result = communicationManager.callApi(
    callerPluginId = myPluginId,
    apiId = "translation-api",
    method = "translate",
    parameters = mapOf("text" to text, "targetLang" to "en")
)

// Subscribe to events
communicationManager.subscribeToEvents(
    subscriberId = myPluginId,
    eventTypes = setOf(CommonEventTypes.TEXT_SELECTED)
) { event ->
    handleTextSelection(event.payload["text"])
}

// Emit events
communicationManager.emitEvent(
    sourcePluginId = myPluginId,
    eventType = CommonEventTypes.TRANSLATION_COMPLETED,
    payload = mapOf("result" to translatedText)
)
```

### Files
- `domain/.../communication/CrossPluginCommunication.kt` - Interfaces
- `domain/.../communication/PluginEventBus.kt` - Event bus
- `domain/.../communication/PluginCommunicationManager.kt` - Manager

---

## 7. Plugin Backup/Sync

### Overview
Sync installed plugins and settings across devices.

### Features
- **Backup**: Export plugins, settings, pipelines, collections
- **Restore**: Import from backup
- **Sync**: Real-time sync across devices
- **Conflict Resolution**: Handle sync conflicts

### Usage
```kotlin
// Initialize sync
syncManager.initialize(deviceId, deviceName)
syncManager.updateConfig(SyncConfig(
    enabled = true,
    autoSync = true,
    syncIntervalMinutes = 30
))

// Manual sync
val result = syncManager.sync()

// Create backup
val backup = syncManager.createBackup()

// Restore from backup
syncManager.restoreBackup(backup)

// Handle conflicts
syncManager.resolveConflictManually(conflictId, ConflictResolution.USE_NEWEST)
```

### Files
- `domain/.../sync/PluginBackupSync.kt` - Data models
- `domain/.../sync/PluginSyncManager.kt` - Sync logic
- `data/.../sqldelight/pluginSync.sq` - Local database

---

## 8. Offline Plugin Cache

### Overview
Pre-download plugin updates for offline use.

### Features
- **Auto-download**: Automatically cache available updates
- **Storage Management**: Configurable cache size limits
- **Download Queue**: Priority-based download queue
- **Checksum Verification**: Ensure cache integrity

### Usage
```kotlin
// Configure cache
cacheManager.updateConfig(CacheConfig(
    enabled = true,
    maxCacheSizeBytes = 500 * 1024 * 1024, // 500 MB
    autoDownloadUpdates = true,
    downloadOnWifiOnly = true
))

// Check for updates and cache them
cacheManager.checkAndDownloadUpdates()

// Install from cache (offline)
cacheManager.installCachedPlugin(pluginId, version)

// Monitor downloads
cacheManager.events.collect { event ->
    when (event) {
        is DownloadEvent.Progress -> updateProgress(event.progress)
        is DownloadEvent.Completed -> showSuccess()
    }
}
```

### Files
- `domain/.../offline/OfflinePluginCache.kt` - Data models
- `domain/.../offline/OfflineCacheManager.kt` - Cache logic
- `data/.../sqldelight/pluginCache.sq` - Local database

---

## Architecture

All enhancements follow clean architecture principles:
- **Domain Layer**: Business logic and interfaces
- **Data Layer**: Repository implementations
- **Presentation Layer**: UI components (to be implemented)

## Database Schemas

### Local (SQLDelight)
- `pluginPipeline.sq` - Pipeline definitions
- `pluginCache.sq` - Offline cache
- `pluginSync.sq` - Sync changes and conflicts
- `pluginAnalytics.sq` - Local analytics cache

### Remote (Supabase)
- `schema_13_plugin_social.sql` - Social features
- `schema_14_plugin_analytics.sql` - Analytics and A/B testing
