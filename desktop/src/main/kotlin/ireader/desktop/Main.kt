package ireader.desktop

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.request.crossfade
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ireader.core.http.HttpClients
import ireader.core.http.okhttp
import ireader.data.di.DataModule
import ireader.data.di.dataPlatformModule
import ireader.data.di.repositoryInjectModule
import ireader.desktop.di.DesktopDI
import ireader.domain.catalogs.CatalogStore
import ireader.domain.di.CatalogModule
import ireader.domain.di.DomainModule
import ireader.domain.di.DomainServices
import ireader.domain.di.UseCasesInject
import ireader.domain.di.localModule
import ireader.domain.di.preferencesInjectModule
import ireader.domain.image.CoverCache
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.presentation.core.DefaultNavigatorScreenTransition
import ireader.presentation.core.MainStarterScreen
import ireader.presentation.core.di.PresentationModules
import ireader.presentation.core.di.presentationPlatformModule
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.imageloader.BookCoverFetcher
import ireader.presentation.imageloader.PackageManager
import ireader.presentation.imageloader.coil.imageloader.BookCoverKeyer
import ireader.presentation.imageloader.coil.imageloader.BookCoverMapper
import ireader.presentation.imageloader.coil.imageloader.CatalogKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteMapper
import ireader.presentation.imageloader.coil.imageloader.InstalledCatalogKeyer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.rememberKoinInject
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class,
    ExperimentalCoilApi::class
)
fun main() {
    startKoin {
        modules(
            localModule,dataPlatformModule, CatalogModule, DataModule,preferencesInjectModule,
            repositoryInjectModule, UseCasesInject, PresentationModules,DomainServices,DomainModule,presentationPlatformModule, DesktopDI
        )
    }


    //Dispatchers.setMain(StandardTestDispatcher())
    application {
        Napier.base(DebugAntilog())
        val state = rememberWindowState()

        Window(
            onCloseRequest = { exitProcess(0) },
            title = "IReader",
            state = state,
            icon = painterResource("icon.png")
        ) {
            KoinContext {
                val context = LocalPlatformContext.current
                val catalogStore: CatalogStore = koinInject()
                val getSimpleStorage: GetSimpleStorage = koinInject()
                val coverCache: CoverCache = CoverCache(context, getSimpleStorage)
                val httpClients: HttpClients = koinInject()
                setSingletonImageLoaderFactory { context ->
                    newImageLoader(
                        catalogStore = catalogStore,
                        simpleStorage = getSimpleStorage,
                        client = httpClients,
                        coverCache = coverCache,
                        context = context
                    )
                }
                val coroutineScope = rememberCoroutineScope()
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
fun newImageLoader(context: PlatformContext, simpleStorage: GetSimpleStorage, client:HttpClients, catalogStore: CatalogStore,
                    coverCache: CoverCache,): ImageLoader {
    return ImageLoader.Builder(context).apply {
        val diskCacheInit = { CoilDiskCache.get(simpleStorage) }
        val callFactoryInit = { client.default.okhttp }
        components {
            add(CatalogRemoteMapper())
//            add(BookCoverMapper())
            add(BookCoverKeyer())
            add(CatalogRemoteKeyer())
            add(InstalledCatalogKeyer())
            add(
                BookCoverFetcher.BookCoverFactory(
                    callFactoryLazy = lazy(callFactoryInit),
                    diskCacheLazy = lazy(diskCacheInit),
                    catalogStore,
                    coverCache,
                )
            )
        }
        diskCache(diskCacheInit)
        crossfade((300).toInt())
        // Coil spawns a new thread for every image load by default
        fetcherDispatcher(Dispatchers.IO.limitedParallelism(8))
        decoderDispatcher(Dispatchers.IO.limitedParallelism(2))

    }.build()
}
object CoilDiskCache {

    private const val FOLDER_NAME = "image_cache"
    private var instance: DiskCache? = null

    @Synchronized
    fun get(context: GetSimpleStorage): DiskCache {
        return instance ?: run {
            val safeCacheDir = context.ireaderCacheDir().apply { mkdirs() }
            // Create the singleton disk cache instance.
            DiskCache.Builder().fileSystem(FileSystem.SYSTEM)
                .directory(safeCacheDir.resolve(FOLDER_NAME).toOkioPath())
                .build().also { instance = it }

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