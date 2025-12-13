package ireader.domain.plugins.di

import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginUpdateChecker
import ireader.domain.plugins.ai.AIService
import ireader.domain.plugins.analytics.ABTestManager
import ireader.domain.plugins.analytics.CrashReporter
import ireader.domain.plugins.analytics.PluginAnalyticsManager
import ireader.domain.plugins.analytics.PluginAnalyticsRepository
import ireader.domain.plugins.communication.PluginCommunicationManager
import ireader.domain.plugins.communication.PluginEventBus
import ireader.domain.plugins.communication.PluginServiceRegistry
import ireader.domain.plugins.composition.PipelineManager
import ireader.domain.plugins.composition.PipelineRepository
import ireader.domain.plugins.composition.PluginResolver
import ireader.domain.plugins.hotreload.FileWatcher
import ireader.domain.plugins.hotreload.HotReloadConfig
import ireader.domain.plugins.hotreload.HotReloadManager
import ireader.domain.plugins.marketplace.MarketplaceSocialManager
import ireader.domain.plugins.marketplace.MarketplaceSocialRepository
import ireader.domain.plugins.marketplace.RecommendationEngine
import ireader.domain.plugins.offline.CacheStorage
import ireader.domain.plugins.offline.OfflineCacheManager
import ireader.domain.plugins.offline.PluginDownloadManager
import ireader.domain.plugins.sync.ChangeTracker
import ireader.domain.plugins.sync.PluginSyncManager
import ireader.domain.plugins.sync.PluginSyncRepository

/**
 * Module providing all plugin enhancement dependencies.
 * This is a reference for DI setup - actual implementation depends on the DI framework used.
 */
object PluginEnhancementsModule {
    
    /**
     * Create the plugin event bus (singleton).
     */
    fun providePluginEventBus(): PluginEventBus {
        return PluginEventBus()
    }
    
    /**
     * Create the plugin service registry (singleton).
     */
    fun providePluginServiceRegistry(): PluginServiceRegistry {
        return PluginServiceRegistry()
    }
    
    /**
     * Create the plugin communication manager.
     */
    fun providePluginCommunicationManager(
        eventBus: PluginEventBus,
        serviceRegistry: PluginServiceRegistry
    ): PluginCommunicationManager {
        return PluginCommunicationManager(eventBus, serviceRegistry)
    }
    
    /**
     * Create the pipeline manager.
     */
    fun providePipelineManager(
        pluginResolver: PluginResolver,
        pipelineRepository: PipelineRepository
    ): PipelineManager {
        return PipelineManager(pluginResolver, pipelineRepository)
    }
    
    /**
     * Create the hot reload manager.
     */
    fun provideHotReloadManager(
        pluginManager: PluginManager,
        fileWatcher: FileWatcher,
        config: HotReloadConfig = HotReloadConfig()
    ): HotReloadManager {
        return HotReloadManager(pluginManager, fileWatcher, config)
    }

    /**
     * Create the marketplace social manager.
     */
    fun provideMarketplaceSocialManager(
        socialRepository: MarketplaceSocialRepository,
        recommendationEngine: RecommendationEngine
    ): MarketplaceSocialManager {
        return MarketplaceSocialManager(socialRepository, recommendationEngine)
    }
    
    /**
     * Create the plugin analytics manager.
     */
    fun providePluginAnalyticsManager(
        analyticsRepository: PluginAnalyticsRepository,
        crashReporter: CrashReporter,
        abTestManager: ABTestManager
    ): PluginAnalyticsManager {
        return PluginAnalyticsManager(analyticsRepository, crashReporter, abTestManager)
    }
    
    /**
     * Create the plugin sync manager.
     */
    fun providePluginSyncManager(
        pluginManager: PluginManager,
        syncRepository: PluginSyncRepository,
        changeTracker: ChangeTracker
    ): PluginSyncManager {
        return PluginSyncManager(pluginManager, syncRepository, changeTracker)
    }
    
    /**
     * Create the offline cache manager.
     */
    fun provideOfflineCacheManager(
        pluginManager: PluginManager,
        updateChecker: PluginUpdateChecker,
        cacheStorage: CacheStorage,
        downloadManager: PluginDownloadManager
    ): OfflineCacheManager {
        return OfflineCacheManager(pluginManager, updateChecker, cacheStorage, downloadManager)
    }
    
    /**
     * Create the AI service.
     */
    fun provideAIService(
        localEngine: ireader.domain.plugins.ai.LocalInferenceEngine?,
        cloudClient: ireader.domain.plugins.ai.CloudAIClient?
    ): AIService {
        return AIService(localEngine, cloudClient)
    }
}

/**
 * Extension functions for easy access to plugin enhancements.
 */
object PluginEnhancements {
    
    /**
     * Check if AI features are available.
     */
    fun isAIAvailable(aiService: AIService): Boolean {
        return aiService.isReady.value
    }
    
    /**
     * Check if hot reload is enabled.
     */
    fun isHotReloadEnabled(hotReloadManager: HotReloadManager): Boolean {
        return hotReloadManager.isWatching.value
    }
    
    /**
     * Get the number of pending sync changes.
     */
    fun getPendingSyncCount(syncManager: PluginSyncManager): Int {
        return syncManager.syncStatus.value.pendingChanges
    }
    
    /**
     * Get the number of cached plugins.
     */
    fun getCachedPluginCount(cacheManager: OfflineCacheManager): Int {
        return cacheManager.statistics.value.totalCachedPlugins
    }
}
