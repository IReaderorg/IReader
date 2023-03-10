package ireader.data.di

import com.squareup.sqldelight.db.SqlDriver
import ireader.core.db.Transactions
import ireader.core.http.BrowserEngine
import ireader.core.http.HttpClients
import ireader.core.http.WebViewCookieJar
import ireader.core.os.PackageInstaller
import ireader.data.catalog.impl.AndroidCatalogInstallationChanges
import ireader.data.catalog.impl.AndroidCatalogInstaller
import ireader.data.catalog.impl.interactor.InstallCatalogImpl
import ireader.data.core.AndroidDatabaseHandler
import ireader.data.core.AndroidTransaction
import ireader.data.core.DatabaseDriverFactory
import ireader.data.core.DatabaseHandler
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.catalogs.service.CatalogInstallationChanges
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.image.cache.CoverCache
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

actual val dataPlatformModule: DI.Module = DI.Module("androidDataModule") {
    bindSingleton<AndroidDatabaseHandler> { AndroidDatabaseHandler(instance(),instance()) }
    bindSingleton<Transactions> { AndroidTransaction(instance()) }
    bindSingleton<DatabaseHandler> { AndroidDatabaseHandler(instance(),instance()) }
    bindSingleton<SqlDriver> { DatabaseDriverFactory(instance()).create() }
    bindSingleton<CatalogLoader> { ireader.data.catalog.impl.AndroidCatalogLoader(instance(),instance(),instance(),instance(),instance()) }
    bindSingleton<CatalogRemoteApi> { ireader.data.catalog.impl.CatalogGithubApi(instance(),instance()) }
    bindSingleton<UninstallCatalogs> { ireader.data.catalog.impl.interactor.UninstallCatalogImpl(instance(),instance()) }
    bindSingleton<AndroidCatalogInstaller> { AndroidCatalogInstaller(instance(),instance(),instance(),instance(),instance(),instance()) }
    bindSingleton<PackageInstaller> { PackageInstaller(instance(),instance()) }
    bindSingleton<WebViewCookieJar> { WebViewCookieJar(instance()) }
    bindSingleton<CoverCache> { CoverCache(instance(),instance()) }
    bindSingleton<InstallCatalog> { InstallCatalogImpl(instance(),instance(),instance()) }
    bindSingleton<CatalogInstallationChanges> { instance<AndroidCatalogInstallationChanges>() }
    bindSingleton<CatalogInstaller> { AndroidCatalogInstaller(instance(),instance(),instance(),instance(),instance(),instance()) }
    bindSingleton<HttpClients> { HttpClients(instance(), BrowserEngine(instance(), instance()),instance(),instance(),instance()) }
}