package org.ireader.app.di


import ir.kazemcodes.infinityreader.Database
import ireader.data.catalogrepository.CatalogSourceRepositoryImpl
import ireader.data.core.createDatabase
import ireader.domain.data.repository.CatalogSourceRepository
import okio.FileSystem
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val DataModule = DI.Module("databaseModule") {


    bindSingleton<Database> { createDatabase(instance()) }

    bindSingleton<FileSystem> { FileSystem.SYSTEM }



    bindSingleton<CatalogSourceRepository> { CatalogSourceRepositoryImpl(instance()) }
}