package org.ireader.app.di


import com.squareup.sqldelight.db.SqlDriver
import ir.kazemcodes.infinityreader.Database
import ireader.core.db.Transactions
import ireader.data.catalogrepository.CatalogSourceRepositoryImpl
import ireader.data.core.*
import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.data.repository.CatalogSourceRepository
import okio.FileSystem
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val DataModule = DI.Module("databaseModule") {

    bindSingleton<SqlDriver> { DatabaseDriverFactory(instance()).create() }
    bindSingleton<Database> { createDatabase(instance()) }
    bindSingleton<DatabaseHandler> { AndroidDatabaseHandler(instance(),instance()) }
    bindSingleton<FileSystem> { FileSystem.SYSTEM }
    bindSingleton<AndroidDatabaseHandler> { AndroidDatabaseHandler(instance(),instance()) }
    bindSingleton<Transactions> { AndroidTransaction(instance()) }

    bindSingleton<CatalogLoader> { ireader.data.catalog.impl.AndroidCatalogLoader(instance(),instance(),instance(),instance()) }
    bindSingleton<CatalogRemoteApi> { ireader.data.catalog.impl.CatalogGithubApi(instance(),instance()) }
    bindSingleton<UninstallCatalogs> { ireader.data.catalog.impl.interactor.UninstallCatalogImpl(instance(),instance()) }
    bindSingleton<CatalogSourceRepository> { CatalogSourceRepositoryImpl(instance()) }
}