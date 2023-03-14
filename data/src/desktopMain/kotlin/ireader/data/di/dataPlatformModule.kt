package ireader.data.di

import com.squareup.sqldelight.db.SqlDriver
import ireader.core.db.Transactions
import ireader.data.catalog.impl.DesktopCatalogInstallationChanges
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
import ireader.domain.catalogs.service.CatalogLoader
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

actual val dataPlatformModule: DI.Module = DI.Module("desktopDataModule") {
    bindSingleton<DatabaseHandler> { JvmDatabaseHandler(instance(),instance()) }
    bindSingleton<SqlDriver> { DatabaseDriverFactory().create() }
    bindSingleton<Transactions> { DatabaseTransactions(instance()) }
    bindSingleton<CatalogLoader> { DesktopCatalogLoader() }
    bindSingleton<CatalogInstallationChanges> { DesktopCatalogInstallationChanges() }
    bindSingleton<InstallCatalog> { DesktopInstallCatalog() }
    bindSingleton<UninstallCatalogs> { DesktopUninstallCatalogs() }
}