package ireader.domain.di

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
    
    // Plugin Preferences
    single { PluginPreferences(get()) }
    
    // Plugin Registry
    single { PluginRegistry(get()) }
    
    // Plugin Validator
    single {
        PluginValidator(
            currentIReaderVersion = "1.0.0", // TODO: Get from BuildConfig
            currentPlatform = getPlatform()
        )
    }
    
    // Note: PluginClassLoader is registered in platform-specific DomainModule
    
    // Plugin Loader
    single {
        PluginLoader(
            fileSystem = get(),
            validator = get(),
            classLoader = get()
        )
    }
    
    // Plugin Permission Manager
    single {
        PluginPermissionManager(
            database = get()
        )
    }
    
    // Plugin Security Manager
    single {
        PluginSecurityManager(
            permissionManager = get(),
            fileSystem = get()
        )
    }
    
    // Monetization Service
    single {
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
    
    // Plugin Manager
    single {
        PluginManager(
            fileSystem = get(),
            loader = get(),
            registry = get(),
            preferences = get(),
            monetization = get(),
            database = get(),
            securityManager = get(),
            performanceMetricsManager = get()
        )
    }
    
    // TTS Plugin Integration
    single { PluginTTSManager(get()) }
    single { AudioStreamHandler() }
    single { TTSErrorHandler() }
    
    // Performance Monitoring
    single { PerformanceMetricsManager(get()) }
    
    // Note: PaymentProcessor, PurchaseRepository, TrialRepository, PluginDatabase, 
    // PluginClassLoader, MemoryTracker, and pluginsDir are provided by platform-specific modules
}

// Helper function to get current platform
private fun getPlatform(): Platform {
    return when {
        System.getProperty("os.name")?.contains("Windows", ignoreCase = true) == true -> Platform.DESKTOP
        System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true -> Platform.DESKTOP
        System.getProperty("os.name")?.contains("Linux", ignoreCase = true) == true -> Platform.DESKTOP
        else -> Platform.ANDROID // Default to Android
    }
}
