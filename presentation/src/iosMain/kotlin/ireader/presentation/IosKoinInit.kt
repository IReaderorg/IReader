package ireader.presentation

import ireader.data.di.DataModule
import ireader.data.di.dataPlatformModule
import ireader.data.di.remoteModule
import ireader.data.di.remotePlatformModule
import ireader.data.di.repositoryInjectModule
import ireader.data.di.reviewModule
import ireader.domain.di.CatalogModule
import ireader.domain.di.DomainModule
import ireader.domain.di.DomainServices
import ireader.domain.di.PluginModule
import ireader.domain.di.UseCasesInject
import ireader.domain.di.localModule
import ireader.domain.di.platformServiceModule
import ireader.domain.di.preferencesInjectModule
import ireader.presentation.core.di.PresentationModules
import ireader.presentation.core.di.presentationPlatformModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Initialize Koin for iOS
 * This should be called from Swift before using any Kotlin code
 */
fun initKoin(additionalModules: List<Module> = emptyList()) {
    startKoin {
        modules(
            listOf(
                // Data layer
                dataPlatformModule,
                DataModule,
                repositoryInjectModule,
                remotePlatformModule,
                remoteModule,
                reviewModule,
                // Domain layer
                preferencesInjectModule,
                localModule,
                platformServiceModule,
                ireader.domain.di.ServiceModule,
                UseCasesInject,
                DomainServices,
                DomainModule,
                CatalogModule,
                PluginModule,
                // Presentation layer
                PresentationModules,
                presentationPlatformModule,
            ) + additionalModules
        )
    }
}

/**
 * Helper function to create a Koin module from Swift
 */
fun createModule(configure: Module.() -> Unit): Module {
    return module(createdAtStart = false, moduleDeclaration = configure)
}
