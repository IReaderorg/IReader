package ireader.domain.plugins

import org.koin.dsl.module

/**
 * Koin DI module for plugin update system
 * This module provides all dependencies needed for the plugin update system
 * Requirements: 12.1, 12.2, 12.3, 12.4, 12.5
 */
val pluginUpdateModule = module {
    
    // Marketplace client - use mock for development, replace with production implementation
    single<PluginMarketplaceClient> {
        MockPluginMarketplaceClient()
        // In production, use:
        // PluginMarketplaceClientImpl(httpClient = get())
    }
    
    // Update history repository - use in-memory for development, replace with database implementation
    single<PluginUpdateHistoryRepository> {
        InMemoryPluginUpdateHistoryRepository()
        // In production, use:
        // PluginUpdateHistoryRepositoryImpl(database = get())
    }
    
    // Plugin update checker
    single {
        PluginUpdateChecker(
            pluginManager = get(),
            pluginRegistry = get(),
            pluginLoader = get(),
            pluginDatabase = get(),
            preferences = get(),
            marketplaceClient = get(),
            updateHistoryRepository = get()
        )
    }
}

/**
 * Extension function to start the update checker when the app starts
 */
fun PluginUpdateChecker.startWithApp() {
    // Start periodic update checking
    startPeriodicUpdateChecking()
}

/**
 * Extension function to stop the update checker when the app stops
 */
fun PluginUpdateChecker.stopWithApp() {
    // Stop periodic update checking
    stopPeriodicUpdateChecking()
}
