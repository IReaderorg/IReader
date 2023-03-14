package ireader.desktop.di

import ireader.desktop.initiators.CatalogStoreInitializer
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val DesktopDI = DI.Module("DesktopDI"){

    bindSingleton<CatalogStoreInitializer> { CatalogStoreInitializer(instance(),instance()) }
}