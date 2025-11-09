package ireader.data.di


import ir.kazemcodes.infinityreader.Database
import ireader.data.catalog.CatalogGithubApi
import ireader.data.catalogrepository.CatalogSourceRepositoryImpl
import ireader.data.core.DatabaseVersionManager
import ireader.data.core.createDatabase
import ireader.data.repository.FundingGoalRepositoryImpl
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.data.repository.CatalogSourceRepository
import okio.FileSystem

import org.koin.dsl.module

val DataModule = module {

    single {
        DatabaseVersionManager(get(), get())
    }
    single<Database> {
        // Apply migrations if needed
        val versionManager = get<DatabaseVersionManager>()
        versionManager.upgrade()
        createDatabase(get())
    }

    single<FileSystem> { FileSystem.SYSTEM }


    single<ireader.domain.data.repository.FundingGoalRepository> {
        FundingGoalRepositoryImpl()
    }
    single<SyncRemoteCatalogs> { SyncRemoteCatalogs(get(), CatalogGithubApi(get(),get()),get()) }

    single<CatalogSourceRepository> { CatalogSourceRepositoryImpl(get()) }
}