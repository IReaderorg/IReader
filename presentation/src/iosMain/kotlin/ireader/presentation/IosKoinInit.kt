@file:OptIn(kotlin.experimental.ExperimentalObjCName::class)

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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.writeToFile
import kotlin.experimental.ExperimentalNativeApi
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlin.native.setUnhandledExceptionHook

/**
 * Initialize Koin for iOS
 * This should be called from Swift before using any Kotlin code
 */
@ObjCName("IosKoinInitKt")
object IosKoinInit {
    @ObjCName("initKoin")
    fun initKoin(additionalModules: List<Module> = emptyList()) {
        installCrashLog()
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
        // iOS loads every module in this call, same as desktop.
        ireader.domain.di.ModuleInitializationState.markFullyInitialized()
    }

    /**
     * Helper function to create a Koin module from Swift
     */
    @ObjCName("createModule")
    fun createModule(configure: Module.() -> Unit): Module {
        return module(createdAtStart = false, moduleDeclaration = configure)
    }
}

@OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)
private fun installCrashLog() {
    setUnhandledExceptionHook { throwable ->
        val text = "IReaderUncaught\n${throwable.stackTraceToString()}"
        println(text)
        val bytes = text.encodeToByteArray()
        if (bytes.isEmpty()) return@setUnhandledExceptionHook
        val path = NSTemporaryDirectory() + "ireader-crash.txt"
        bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                .writeToFile(path, atomically = true)
        }
    }
}
