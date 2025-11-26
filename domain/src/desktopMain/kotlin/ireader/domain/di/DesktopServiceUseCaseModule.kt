package ireader.domain.di

import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.usecases.services.StartDownloadServicesUseCase
import ireader.domain.usecases.services.StartLibraryUpdateServicesUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Desktop-specific service use cases module
 * Loaded after UseCasesInject to avoid circular dependencies
 * 
 * Using singleOf for automatic dependency resolution
 */
val desktopServiceUseCaseModule = module {
    
    // Use singleOf for automatic constructor injection
    singleOf(::StartDownloadServicesUseCase)
    singleOf(::StartLibraryUpdateServicesUseCase)
    
    single<ServiceUseCases> {
        ServiceUseCases(
            startDownloadServicesUseCase = get(),
            startLibraryUpdateServicesUseCase = get(),
            startTTSServicesUseCase = get()
        )
    }
}
