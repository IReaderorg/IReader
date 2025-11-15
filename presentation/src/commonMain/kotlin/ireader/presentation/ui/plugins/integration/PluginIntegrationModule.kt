package ireader.presentation.ui.plugins.integration

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin DI module for plugin integration components
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
val pluginIntegrationModule = module {
    
    // Plugin data storage
    single<PluginDataStorage> { InMemoryPluginDataStorage() }
    
    // Feature plugin integration
    single {
        FeaturePluginIntegration(
            pluginManager = get(),
            pluginDataStorage = get()
        )
    }
    
    // Reader context handler
    factory {
        ReaderContextHandler(
            featurePluginIntegration = get()
        )
    }
    
    // Reader plugin event notifier
    factory {
        ReaderPluginEventNotifier(
            featurePluginIntegration = get()
        )
    }
    
    // Feature plugin view model
    factoryOf(::FeaturePluginViewModel)
}
