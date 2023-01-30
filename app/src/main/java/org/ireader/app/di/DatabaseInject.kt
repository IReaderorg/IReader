package org.ireader.app.di

import android.app.Application
import com.squareup.sqldelight.db.SqlDriver
import ir.kazemcodes.infinityreader.Database
import okio.FileSystem
import ireader.core.db.Transactions
import ireader.data.catalogrepository.CatalogSourceRepositoryImpl
import ireader.data.core.*
import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.data.repository.CatalogSourceRepository
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single
import org.koin.dsl.bind
import org.koin.dsl.module

@org.koin.core.annotation.Module
@ComponentScan("org.ireader.app.di.DatabaseInject")
class DatabaseInject {


    @Single
    fun provideSqlDrive(
        app:Application
    ) : SqlDriver {
        return DatabaseDriverFactory(app).create()
    }
    @Single
    fun provideDatabase(driver:SqlDriver): Database {
        return createDatabase(driver)
    }
    @Single
    fun databaseHandler(driver:SqlDriver, db: Database) : DatabaseHandler {
        return             AndroidDatabaseHandler(
            driver = driver,
            db = db
        )
    }


        @Single
    fun provideFileSystem(): FileSystem {
        return FileSystem.SYSTEM
    }
    @Single
    fun androidTransaction(driver:SqlDriver, db: Database) : AndroidDatabaseHandler {
        return AndroidDatabaseHandler(
            driver = driver,
            db = db
        )
    }
    @Single
    fun provideTransaction(handler: AndroidDatabaseHandler) : Transactions {
        return AndroidTransaction(handler)
    }
}

val DataModule = module {
    single(qualifier=null) { ireader.data.catalog.impl.AndroidCatalogInstaller(get(),get(),get(),get(),get(),get()) } bind(CatalogInstaller::class)
    single(qualifier=null) { ireader.data.catalog.impl.AndroidCatalogLoader(get(),get(),get(),get()) } bind(CatalogLoader::class)
    single(qualifier=null) { ireader.data.catalog.impl.AndroidLocalInstaller(get(),get(),get(),get(),get(),get()) } bind(CatalogInstaller::class)
    single(qualifier=null) { ireader.data.catalog.impl.CatalogGithubApi(get(),get()) } bind(CatalogRemoteApi::class)
    single(qualifier=null) { ireader.data.catalog.impl.interactor.UninstallCatalogImpl(get(),get()) } bind(UninstallCatalogs::class)
    single(qualifier=null) { CatalogSourceRepositoryImpl(get()) } bind(CatalogSourceRepository::class)
}