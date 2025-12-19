package ireader.desktop.di

import ireader.desktop.initiators.CatalogStoreInitializer
import ireader.presentation.ui.update.AppUpdateChecker
import ireader.presentation.ui.update.DesktopAppUpdateChecker
import org.koin.dsl.module

val DesktopDI = module {

    single<CatalogStoreInitializer>(createdAtStart = true) { CatalogStoreInitializer(get(), getOrNull(), getOrNull(), getOrNull()) }
    
    // App Update Checker
    single<AppUpdateChecker> { DesktopAppUpdateChecker(get()) }
}