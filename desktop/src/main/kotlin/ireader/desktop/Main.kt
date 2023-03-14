package ireader.desktop

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import ireader.data.di.DataModule
import ireader.data.di.dataPlatformModule
import ireader.data.di.repositoryInjectModule
import ireader.desktop.di.DesktopDI
import ireader.desktop.initiators.CatalogStoreInitializer
import ireader.domain.di.*
import ireader.presentation.core.DefaultNavigatorScreenTransition
import ireader.presentation.core.MainStarterScreen
import ireader.presentation.core.di.PresentationModules
import ireader.presentation.core.di.presentationPlatformModule
import ireader.presentation.core.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toOkioPath
import org.kodein.di.DI
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import java.io.File
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
suspend fun main() {
    val di = DI.lazy {
        importAll(localModule,dataPlatformModule, CatalogModule, DataModule,preferencesInjectModule,
                repositoryInjectModule, UseCasesInject, PresentationModules,DomainServices,DomainModule,presentationPlatformModule, DesktopDI)

    }
    val catalogStore : CatalogStoreInitializer by di.instance()

    awaitApplication {
        val state = rememberWindowState()
        Window(
                onCloseRequest = { exitProcess(0) },
                title = "IReader",
                state = state
        ) {
            val coroutineScope = rememberCoroutineScope()
            withDI(di) {
                CompositionLocalProvider(
                        LocalImageLoader provides generateImageLoader(coroutineScope),
                ) {
                    AppTheme(coroutineScope) {
                        Navigator(
                                screen = MainStarterScreen,
                                disposeBehavior = NavigatorDisposeBehavior(
                                        disposeNestedNavigators = false,
                                        disposeSteps = true
                                ),
                        ) { navigator ->


                            DefaultNavigatorScreenTransition(navigator = navigator)
                        }
                    }
                }
            }
        }
    }

}

private  fun generateImageLoader(scope: CoroutineScope): ImageLoader {
    return ImageLoader {
        imageScope = scope
        components {
            // add(ImageIODecoder.Factory())
            setupDefaultComponents(imageScope)
        }
        interceptor {
            memoryCacheConfig {
                // Set the max size to 25% of the app's available memory.
                maxSizePercent(0.25)
            }
            diskCacheConfig {
                directory(getCacheDir().resolve("image_cache").toOkioPath())
                maxSizeBytes(512L * 1024 * 1024) // 512MB
            }
        }
    }
}

enum class OperatingSystem {
    Android, IOS, Windows, Linux, MacOS, Unknown
}

private val currentOperatingSystem: OperatingSystem
    get() {
        val operSys = System.getProperty("os.name").lowercase()
        return if (operSys.contains("win")) {
            OperatingSystem.Windows
        } else if (operSys.contains("nix") || operSys.contains("nux") ||
                operSys.contains("aix")
        ) {
            OperatingSystem.Linux
        } else if (operSys.contains("mac")) {
            OperatingSystem.MacOS
        } else {
            OperatingSystem.Unknown
        }
    }

private fun getCacheDir() = when (currentOperatingSystem) {
    OperatingSystem.Windows -> File(System.getenv("AppData"), "$ApplicationName/cache")
    OperatingSystem.Linux -> File(System.getProperty("user.home"), ".cache/$ApplicationName")
    OperatingSystem.MacOS -> File(System.getProperty("user.home"), "Library/Caches/$ApplicationName")
    else -> throw IllegalStateException("Unsupported operating system")
}

private val ApplicationName = "IReader"