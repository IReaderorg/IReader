package ireader.data.di

import app.cash.sqldelight.db.SqlDriver
import ireader.core.db.Transactions
import ireader.core.prefs.PreferenceStoreFactory
import ireader.data.catalog.impl.DesktopCatalogInstallationChanges
import ireader.data.catalog.impl.DesktopCatalogInstaller
import ireader.data.catalog.impl.DesktopCatalogLoader
import ireader.data.catalog.impl.DesktopInstallCatalog
import ireader.data.catalog.impl.DesktopUninstallCatalogs
import ireader.data.core.DatabaseDriverFactory
import ireader.data.core.DatabaseHandler
import ireader.data.core.DatabaseTransactions
import ireader.data.core.JvmDatabaseHandler
import ireader.data.monitoring.DesktopMemoryTracker
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
    single<DatabaseHandler> { JvmDatabaseHandler(get(),get(), preferencesHelper = get(),) }
    single<SqlDriver> { DatabaseDriverFactory().create() }
    single<Transactions> { DatabaseTransactions(get()) }
    
    // Database optimizations - provides caching, batch operations, and performance monitoring
    single<ireader.data.core.DatabaseOptimizations> { ireader.data.core.DatabaseOptimizations(get()) }
    
    // Database preloader - warms up cache during app startup
    single<ireader.data.core.DatabasePreloader> { ireader.data.core.DatabasePreloader(get(), get()) }
    single<CatalogLoader> { DesktopCatalogLoader(get(),get(),get(),get()) }
    single<DesktopCatalogInstallationChanges> { DesktopCatalogInstallationChanges() }
    single<CatalogInstallationChanges> { DesktopCatalogInstallationChanges() }
    single<InstallCatalog> { DesktopInstallCatalog(get()) }
    single<CatalogInstaller> { DesktopCatalogInstaller(get(),get(),get()) }
    single<DesktopCatalogInstaller> { DesktopCatalogInstaller(get(),get(),get()) }
    single<UninstallCatalogs> { DesktopUninstallCatalogs(get()) }
    single<PreferenceStoreFactory> { PreferenceStoreFactory() }
    single<BiometricAuthenticator> { BiometricAuthenticatorImpl() }
    single<MemoryTracker> { DesktopMemoryTracker() }
    single<NotificationRepository> { NotificationRepositoryImpl() }
}