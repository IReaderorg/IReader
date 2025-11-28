package ireader.data.di

import app.cash.sqldelight.db.SqlDriver
import ireader.core.db.Transactions
import ireader.core.http.BrowserEngine
import ireader.core.http.HttpClients
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
    single<AndroidDatabaseHandler> { 
        val handler = AndroidDatabaseHandler(get(), get(), preferencesHelper = get())
        handler.initialize()
        handler
    }
    single<Transactions> { AndroidTransaction(get()) }
    single<DatabaseHandler> { get<AndroidDatabaseHandler>() }
    
    // Database optimizations - provides caching, batch operations, and performance monitoring
    single<DatabaseOptimizations> { DatabaseOptimizations(get()) }
    
    // Database preloader - warms up cache during app startup
    single<DatabasePreloader> { DatabasePreloader(get(), get()) }
    single<SqlDriver> { DatabaseDriverFactory(get()).create() }
    single<CatalogLoader> {
        ireader.data.catalog.impl.AndroidCatalogLoader(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<CatalogRemoteApi> { CatalogGithubApi(get(), get(), get()) }
    single<UninstallCatalogs> {
        ireader.data.catalog.impl.interactor.UninstallCatalogImpl(
            get(),
            get()
        )
    }
    single<AndroidCatalogInstaller> {
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
    single<PackageInstaller> { PackageInstaller(get(), get()) }
    single<WebViewCookieJar> { WebViewCookieJar(get()) }
    single<InstallCatalog> { InstallCatalogImpl(get(), get(), get()) }
    single<CatalogInstallationChanges> { get<AndroidCatalogInstallationChanges>() }
    single<CatalogInstaller> {
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
    single<HttpClients> {
        HttpClients(
            get(),
            BrowserEngine(get(), get()),
            get(),
            get(),
            get(),
            getOrNull() // WebViewManager is optional, may not be available yet
        )
    }
    single<BiometricAuthenticator> { BiometricAuthenticatorImpl(get()) }
    single<MemoryTracker> { AndroidMemoryTracker(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
}