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


import org.koin.dsl.bind
import org.koin.dsl.module

val DataModule = module {

    single<SqlDriver>(qualifier=null) { DatabaseDriverFactory(get()).create() }
    single<Database>(qualifier=null) { createDatabase(get()) }
    single<DatabaseHandler>(qualifier=null) { AndroidDatabaseHandler(get(),get()) }
    single<FileSystem>(qualifier=null) { FileSystem.SYSTEM }
    single<AndroidDatabaseHandler>(qualifier=null) { AndroidDatabaseHandler(get(),get()) }
    single<Transactions>(qualifier=null) { AndroidTransaction(get()) }

    single(qualifier=null) { ireader.data.catalog.impl.AndroidCatalogInstaller(get(),get(),get(),get(),get(),get()) } bind(CatalogInstaller::class)
    single(qualifier=null) { ireader.data.catalog.impl.AndroidCatalogLoader(get(),get(),get(),get()) } bind(CatalogLoader::class)
    single(qualifier=null) { ireader.data.catalog.impl.AndroidLocalInstaller(get(),get(),get(),get(),get(),get()) } bind(CatalogInstaller::class)
    single(qualifier=null) { ireader.data.catalog.impl.CatalogGithubApi(get(),get()) } bind(CatalogRemoteApi::class)
    single(qualifier=null) { ireader.data.catalog.impl.interactor.UninstallCatalogImpl(get(),get()) } bind(UninstallCatalogs::class)
    single(qualifier=null) { CatalogSourceRepositoryImpl(get()) } bind(CatalogSourceRepository::class)
}