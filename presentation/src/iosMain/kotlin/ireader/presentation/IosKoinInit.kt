package ireader.presentation

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
                presentationPlatformModule,
                // Add other modules as needed
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
