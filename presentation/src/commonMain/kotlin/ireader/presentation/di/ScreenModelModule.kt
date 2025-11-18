package ireader.presentation.di

import ireader.presentation.ui.settings.statistics.StatsScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Dependency injection module for new StateScreenModel implementations.
 * Provides screen models following Mihon's StateScreenModel pattern.
 */
val screenModelModule = module {
    
    // Statistics screen model
    factoryOf(::StatsScreenModel)
    
    // Note: Other screen models should be added here as they are migrated
    // to the StateScreenModel pattern
}