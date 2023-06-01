package ireader.data.di

import com.squareup.sqldelight.db.SqlDriver
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
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.catalogs.service.CatalogInstallationChanges
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.catalogs.service.CatalogLoader
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataPlatformModule: Module = module {
    single<DatabaseHandler> { JvmDatabaseHandler(get(),get()) }
    single<SqlDriver> { DatabaseDriverFactory().create() }
    single<Transactions> { DatabaseTransactions(get()) }
    single<CatalogLoader> { DesktopCatalogLoader(get(),get(),get()) }
    single<DesktopCatalogInstallationChanges> { DesktopCatalogInstallationChanges() }
    single<CatalogInstallationChanges> { DesktopCatalogInstallationChanges() }
    single<InstallCatalog> { DesktopInstallCatalog(get()) }
    single<CatalogInstaller> { DesktopCatalogInstaller(get(),get(),get()) }
    single<DesktopCatalogInstaller> { DesktopCatalogInstaller(get(),get(),get()) }
    single<UninstallCatalogs> { DesktopUninstallCatalogs(get()) }
    single<PreferenceStoreFactory> { PreferenceStoreFactory() }
}