package ireader.data.di

import app.cash.sqldelight.db.SqlDriver
import ireader.core.db.Transactions
import ireader.core.http.WebViewCookieJar
import ireader.core.os.PackageInstaller
import ireader.data.catalog.CatalogGithubApi
import ireader.data.catalog.impl.AndroidCatalogInstallationChanges
import ireader.data.catalog.impl.AndroidCatalogInstaller
import ireader.data.catalog.impl.interactor.InstallCatalogImpl
import ireader.data.core.AndroidDatabaseHandler
import ireader.data.core.AndroidTransaction
import ireader.data.core.DatabaseDriverFactory
import ireader.data.core.DatabaseHandler
import ireader.data.core.DatabaseOptimizations
import ireader.data.core.DatabasePreloader
import ireader.data.monitoring.AndroidMemoryTracker
import ireader.data.repository.NotificationRepositoryImpl
import ireader.data.security.BiometricAuthenticator
import ireader.data.security.BiometricAuthenticatorImpl
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.catalogs.service.CatalogInstallationChanges
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.data.repository.NotificationRepository
import ireader.domain.monitoring.MemoryTracker
import org.koin.dsl.module

actual val dataPlatformModule = module {
    // SqlDriver - LAZY: Database driver creation is expensive
    single<SqlDriver>(createdAtStart = false) { 
        DatabaseDriverFactory(get()).create() 
    }
    
    // Database handler - LAZY: depends on SqlDriver
    single<AndroidDatabaseHandler>(createdAtStart = false) { 
        AndroidDatabaseHandler(get(), get(), preferencesHelper = get())
    }
    single<Transactions>(createdAtStart = false) { AndroidTransaction(get()) }
    single<DatabaseHandler>(createdAtStart = false) { get<AndroidDatabaseHandler>() }
    
    // Database optimizations - LAZY: not needed at startup
    single<DatabaseOptimizations>(createdAtStart = false) { DatabaseOptimizations(get()) }
    
    // Database preloader - LAZY: runs in background
    single<DatabasePreloader>(createdAtStart = false) { DatabasePreloader(get(), get(), get()) }

    // CatalogLoader - LAZY: catalog loading is deferred
    single<CatalogLoader>(createdAtStart = false) {
        ireader.data.catalog.impl.AndroidCatalogLoader(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get() // PluginManager for loading engine plugins
        )
    }
    single<CatalogRemoteApi>(createdAtStart = false) { CatalogGithubApi(get(), get(), get()) }
    single<UninstallCatalogs>(createdAtStart = false) {
        ireader.data.catalog.impl.interactor.UninstallCatalogImpl(
            get(),
            get()
        )
    }
    single<AndroidCatalogInstaller>(createdAtStart = false) {
        AndroidCatalogInstaller(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<PackageInstaller>(createdAtStart = false) { PackageInstaller(get(), get()) }
    single<WebViewCookieJar>(createdAtStart = false) { WebViewCookieJar(get()) }
    single<InstallCatalog>(createdAtStart = false) { InstallCatalogImpl(get(), get(), get()) }
    single<CatalogInstallationChanges>(createdAtStart = false) { get<AndroidCatalogInstallationChanges>() }
    single<CatalogInstaller>(createdAtStart = false) {
        AndroidCatalogInstaller(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    // HttpClients is defined in DomainModule with complete dependencies
    single<BiometricAuthenticator>(createdAtStart = false) { BiometricAuthenticatorImpl(get()) }
    single<MemoryTracker>(createdAtStart = false) { AndroidMemoryTracker(get()) }
    single<NotificationRepository>(createdAtStart = false) { NotificationRepositoryImpl(get()) }
}