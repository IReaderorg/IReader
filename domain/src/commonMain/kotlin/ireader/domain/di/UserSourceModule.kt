package ireader.domain.di

import ireader.domain.usersource.catalog.UserSourceCatalogLoader
import ireader.domain.usersource.interactor.*
import ireader.domain.usersource.repository.UserSourceRepository
import org.koin.dsl.module

/**
 * Koin module for User Source feature.
 */
val userSourceModule = module {
    // Repository - provided by data module via DataModule
    // Note: The actual implementation (UserSourceRepositoryImpl) is registered in the data module
    
    // Catalog Loader - integrates user sources with CatalogStore
    single { UserSourceCatalogLoader(get(), get<ireader.core.http.HttpClients>().default) }
    
    // Use Cases
    factory { GetUserSources(get()) }
    factory { GetUserSource(get()) }
    factory { SaveUserSource(get()) }
    factory { DeleteUserSource(get()) }
    factory { ToggleUserSourceEnabled(get()) }
    factory { ImportExportUserSources(get()) }
    factory { ValidateUserSource() }
}
