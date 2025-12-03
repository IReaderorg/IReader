package ireader.data.di

import app.cash.sqldelight.db.SqlDriver
import ireader.core.db.Transactions
import ireader.core.prefs.PreferenceStoreFactory
import ireader.data.catalog.impl.IosCatalogInstallationChanges
import ireader.data.catalog.impl.IosCatalogInstaller
import ireader.data.catalog.impl.IosCatalogLoader
import ireader.data.catalog.impl.IosInstallCatalog
import ireader.data.catalog.impl.IosUninstallCatalogs
import ireader.data.core.DatabaseDriverFactory
import ireader.data.core.DatabaseHandler
import ireader.data.core.DatabaseTransactions
import ireader.data.core.IosDatabaseHandler
import ireader.data.monitoring.IosMemoryTracker
import ireader.data.repository.NotificationRepositoryImpl
import ireader.data.security.BiometricAuthenticator
import ireader.data.security.BiometricAuthenticatorImpl
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.catalogs.service.CatalogInstallationChanges
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.data.repository.NotificationRepository
import ireader.domain.monitoring.MemoryTracker
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataPlatformModule: Module = module {
    single<DatabaseHandler> { IosDatabaseHandler(get(), get(), preferencesHelper = get()) }
    single<SqlDriver> { DatabaseDriverFactory().create() }
    single<Transactions> { DatabaseTransactions(get()) }
    
    // Database optimizations
    single<ireader.data.core.DatabaseOptimizations> { ireader.data.core.DatabaseOptimizations(get()) }
    single<ireader.data.core.DatabasePreloader> { ireader.data.core.DatabasePreloader(get(), get()) }
    
    // Catalog management
    single<CatalogLoader> { IosCatalogLoader(get(), get(), get()) }
    single<IosCatalogInstallationChanges> { IosCatalogInstallationChanges() }
    single<CatalogInstallationChanges> { IosCatalogInstallationChanges() }
    single<InstallCatalog> { IosInstallCatalog(get()) }
    single<CatalogInstaller> { IosCatalogInstaller(get(), get(), get()) }
    single<IosCatalogInstaller> { IosCatalogInstaller(get(), get(), get()) }
    single<UninstallCatalogs> { IosUninstallCatalogs(get()) }
    
    // Preferences
    single<PreferenceStoreFactory> { PreferenceStoreFactory() }
    
    // Security
    single<BiometricAuthenticator> { BiometricAuthenticatorImpl() }
    
    // Monitoring
    single<MemoryTracker> { IosMemoryTracker() }
    
    // Notifications
    single<NotificationRepository> { NotificationRepositoryImpl() }
}
