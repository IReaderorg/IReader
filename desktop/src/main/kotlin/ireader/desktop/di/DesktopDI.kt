package ireader.desktop.di

import ireader.desktop.initiators.CatalogStoreInitializer
import org.koin.dsl.module

val DesktopDI = module {

    single<CatalogStoreInitializer>(createdAtStart = true) { CatalogStoreInitializer(get()) }
}