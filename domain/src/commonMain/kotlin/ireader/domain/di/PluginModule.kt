package ireader.domain.di

import ireader.domain.js.engine.JSEngineProvider
import ireader.domain.monitoring.PerformanceMetricsManager
import ireader.domain.plugins.*
import ireader.domain.services.tts.AudioStreamHandler
import ireader.domain.services.tts.PluginTTSManager
import ireader.domain.services.tts.TTSErrorHandler
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for plugin system dependencies
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 5.1, 5.2, 5.3, 5.4, 5.5, 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5, 14.1, 14.2, 14.3, 14.4, 14.5
 */
val PluginModule = module {
    
    // All plugin components are lazy-loaded (factory) since plugins are not needed at startup
    // This significantly reduces startup time and memory pressure
    
    // Plugin Preferences
    factory { PluginPreferences(get()) }
    
    // Plugin Registry
    factory { PluginRegistry(get()) }
    
    // Plugin Validator
    factory {
        PluginValidator(
            currentIReaderVersion = "1.0.0", // TODO: Get from BuildConfig
            currentPlatform = getPlatform()
        )
    }
    
    // Note: PluginClassLoader is registered in platform-specific DomainModule
    
    // Plugin Loader
    factory {
        PluginLoader(
            fileSystem = get(),
            validator = get(),
            classLoader = get()
        )
    }
    
    // Plugin Permission Manager
    factory {
        PluginPermissionManager(
            database = get()
        )
    }
    
    // Plugin Security Manager
    factory {
        PluginSecurityManager(
            permissionManager = get(),
            fileSystem = get()
        )
    }
    
    // Monetization Service
    factory {
        MonetizationService(
            paymentProcessor = get(),
            purchaseRepository = get(),
            trialRepository = get(),
            getCurrentUserId = { 
                // TODO: Get actual user ID from authentication service
                "default_user"
            }
        )
    }
    
    // Plugin Manager - MUST be singleton to share loaded plugins across the app
    // Note: This may be overridden by DomainModules.kt if both modules are loaded
    single {
        PluginManager(
            fileSystem = get(),
            loader = get(),
            registry = get(),
            preferences = get(),
            monetization = get(),
            database = get(),
            securityManager = get(),
            performanceMetricsManager = get(),
            preferenceStore = get()
        )
    }
    
    // TTS Plugin Integration - lazy loaded when TTS is used
    factory { PluginTTSManager(get()) }
    factory { AudioStreamHandler() }
    factory { TTSErrorHandler() }
    
    // JS Engine Provider - manages JS engine plugins for LNReader sources
    factory { JSEngineProvider(get(), getOrNull()) }
    
    // JS Engine Requirement - handles prompts when JS engine is needed
    factory { ireader.domain.js.engine.JSEngineRequirement(get(), getOrNull()) }
    
    // Performance Monitoring
    factory { PerformanceMetricsManager(get()) }
    
    // Note: PaymentProcessor, PurchaseRepository, TrialRepository, PluginDatabase, 
    // PluginClassLoader, MemoryTracker, and pluginsDir are provided by platform-specific modules
}

// Helper function to get current platform - uses expect/actual pattern
internal expect fun getPlatform(): Platform
