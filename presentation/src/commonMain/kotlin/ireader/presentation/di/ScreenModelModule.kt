package ireader.presentation.di

import ireader.presentation.ui.book.viewmodel.BookDetailScreenModelNew
import ireader.presentation.ui.home.explore.viewmodel.ExploreScreenModelNew
import ireader.presentation.ui.migration.MigrationListScreenModel
import ireader.presentation.ui.migration.MigrationConfigScreenModel
import ireader.presentation.ui.migration.MigrationProgressScreenModel
import ireader.presentation.ui.download.DownloadQueueScreenModel
import ireader.presentation.ui.download.DownloadSettingsScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Dependency injection module for new StateScreenModel implementations.
 * Provides screen models following Mihon's StateScreenModel pattern.
 */
val screenModelModule = module {
    
    // Book detail screen model
    factoryOf(::BookDetailScreenModelNew)
    
    // Explore screen model
    factoryOf(::ExploreScreenModelNew)
    
    // Migration screen models
    factoryOf(::MigrationListScreenModel)
    factoryOf(::MigrationConfigScreenModel)
    factoryOf(::MigrationProgressScreenModel)
    
    // Download screen models
    factoryOf(::DownloadQueueScreenModel)
    factoryOf(::DownloadSettingsScreenModel)
    
    // Statistics screen model
    factoryOf(::ireader.presentation.ui.settings.statistics.StatsScreenModel)
}