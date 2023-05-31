package ireader.data.di


import ir.kazemcodes.infinityreader.Database
import ireader.data.catalog.CatalogGithubApi
import ireader.data.catalogrepository.CatalogSourceRepositoryImpl
import ireader.data.core.createDatabase
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.data.repository.CatalogSourceRepository
import okio.FileSystem

import org.koin.dsl.module

val DataModule = module {


    single<Database> { createDatabase(get()) }

    single<FileSystem> { FileSystem.SYSTEM }


    single<SyncRemoteCatalogs> { SyncRemoteCatalogs(get(), CatalogGithubApi(get(),get()),get()) }

    single<CatalogSourceRepository> { CatalogSourceRepositoryImpl(get()) }
}