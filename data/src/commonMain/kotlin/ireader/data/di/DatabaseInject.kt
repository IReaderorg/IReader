package ireader.data.di


import ir.kazemcodes.infinityreader.Database
import ireader.data.catalog.CatalogGithubApi
import ireader.data.catalogrepository.CatalogSourceRepositoryImpl
import ireader.data.core.createDatabase
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.data.repository.CatalogSourceRepository
import okio.FileSystem
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val DataModule = DI.Module("databaseModule") {


    bindSingleton<Database> { createDatabase(instance()) }

    bindSingleton<FileSystem> { FileSystem.SYSTEM }


    bindSingleton<SyncRemoteCatalogs> { SyncRemoteCatalogs(instance(), CatalogGithubApi(instance(),instance()),instance()) }

    bindSingleton<CatalogSourceRepository> { CatalogSourceRepositoryImpl(instance()) }
}