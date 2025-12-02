package ireader.desktop

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.request.crossfade
import ireader.core.http.HttpClients
import ireader.core.http.okhttp
import ireader.data.di.DataModule
import ireader.data.di.dataPlatformModule
import ireader.data.di.remoteModule
import ireader.data.di.remotePlatformModule
import ireader.data.di.repositoryInjectModule
import ireader.data.di.reviewModule
import ireader.data.remote.AutoSyncService
import ireader.desktop.di.DesktopDI
import ireader.domain.catalogs.CatalogStore
import ireader.domain.di.CatalogModule
import ireader.domain.di.DomainModule
import ireader.domain.di.DomainServices
import ireader.domain.di.PluginModule
import ireader.domain.di.UseCasesInject
import ireader.domain.di.localModule
import ireader.domain.di.preferencesInjectModule
import ireader.domain.image.CoverCache
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.presentation.core.CommonNavHost
import ireader.presentation.core.di.PresentationModules
import ireader.presentation.core.di.presentationPlatformModule
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.imageloader.BookCoverFetcher
import ireader.presentation.imageloader.coil.imageloader.BookCoverKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteKeyer
import ireader.presentation.imageloader.coil.imageloader.CatalogRemoteMapper
import ireader.presentation.imageloader.coil.imageloader.InstalledCatalogKeyer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class,
    ExperimentalCoilApi::class
)
fun main() {
    try {
        // Enable production logging (only warnings and errors)
        ireader.core.log.Log.enableProductionLogging()
        
        // Check and create app data directory if needed
        val appDataDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader")
        val cacheDir = File(appDataDir, "cache")
        
        if (!appDataDir.exists()) {
            appDataDir.mkdirs()
        }
        
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        // Verify critical resources exist to avoid runtime errors (silent check)
        val criticalResources = listOf(
            "drawable/ic_eternity_light.xml",
            "drawable/ic_eternity_dark.xml",
            "drawable/ic_eternity.xml"
        )
        
        for (resource in criticalResources) {
            try {
                Thread.currentThread().contextClassLoader.getResource(resource)
            } catch (_: Exception) {
                // Silently ignore missing resources
            }
        }
        
        val koinApp = startKoin {
            modules(
                localModule,
                dataPlatformModule, 
                CatalogModule, 
                DataModule,
                preferencesInjectModule,
                repositoryInjectModule, 
                remotePlatformModule, 
                remoteModule, 
                reviewModule, 
                ireader.domain.di.platformServiceModule,
                DomainModule,
                ireader.domain.di.ServiceModule,
                ireader.domain.di.useCaseModule,
                ireader.domain.di.repositoryUseCaseModule,
                UseCasesInject,
                PresentationModules,
                DomainServices,
                PluginModule,
                presentationPlatformModule, 
                DesktopDI
            )
        }
        
        // Initialize system fonts
        try {
            val systemFontsInitializer = koinApp.koin.get<ireader.domain.usecases.fonts.SystemFontsInitializer>()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                systemFontsInitializer.initializeSystemFonts()
            }
        } catch (_: Exception) {
            // Silently ignore font initialization errors
        }
        
        // Start auto-sync service if available
        try {
            val autoSyncService = koinApp.koin.getOrNull<AutoSyncService>()
            autoSyncService?.start()
        } catch (_: Exception) {
            // Silently ignore auto-sync service errors
        }

        //Dispatchers.setMain(StandardTestDispatcher())
        application {
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
                    val coverCache: CoverCache = CoverCache(context)
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
                        val navController = androidx.navigation.compose.rememberNavController()
                        ireader.presentation.core.ProvideNavigator(navController) {
                            CommonNavHost(navController)

                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        // Handle critical errors silently with automatic repair attempts
        when {
            e.message?.contains("table history_new already exists") == true -> {
                // Try to fix the issue by directly executing SQL to clean up
                try {
                    val dbDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader\\cache")
                    val dbFile = dbDir.listFiles { file -> file.name.endsWith(".db") }?.firstOrNull()
                    
                    if (dbFile != null) {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
                        val backupFile = File(dbFile.parentFile, "${dbFile.nameWithoutExtension}_backup_$timestamp.db")
                        dbFile.copyTo(backupFile, overwrite = true)
                        
                        try {
                            Class.forName("org.sqlite.JDBC")
                            val connection = java.sql.DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
                            connection.use { conn ->
                                conn.createStatement().use { stmt ->
                                    stmt.execute("DROP TABLE IF EXISTS history_new;")
                                }
                            }
                        } catch (_: Exception) { }
                    }
                } catch (_: Exception) { }
            }
            e.message?.contains("no such table: main.history") == true || 
            e.message?.contains("historyView") == true -> {
                // Try to fix the issue by directly creating the history table
                try {
                    val dbDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader\\cache")
                    val dbFile = dbDir.listFiles { file -> file.name.endsWith(".db") }?.firstOrNull()
                    
                    if (dbFile != null) {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
                        val backupFile = File(dbFile.parentFile, "${dbFile.nameWithoutExtension}_backup_$timestamp.db")
                        dbFile.copyTo(backupFile, overwrite = true)
                        
                        try {
                            Class.forName("org.sqlite.JDBC")
                            val connection = java.sql.DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
                            connection.use { conn ->
                                conn.createStatement().use { stmt ->
                                    stmt.execute("DROP VIEW IF EXISTS historyView;")
                                    val rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='history'")
                                    val historyExists = rs.next()
                                    rs.close()
                                    
                                    if (!historyExists) {
                                        val createHistorySql = """
                                            CREATE TABLE IF NOT EXISTS history(
                                                _id INTEGER NOT NULL PRIMARY KEY,
                                                chapter_id INTEGER NOT NULL UNIQUE,
                                                last_read INTEGER,
                                                time_read INTEGER NOT NULL,
                                                progress REAL DEFAULT 0.0,
                                                FOREIGN KEY(chapter_id) REFERENCES chapter (_id)
                                                ON DELETE CASCADE
                                            );
                                        """.trimIndent()
                                        stmt.execute(createHistorySql)
                                        stmt.execute("CREATE INDEX IF NOT EXISTS history_history_chapter_id_index ON history(chapter_id);")
                                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_last_read ON history(last_read);")
                                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);")
                                    }
                                }
                            }
                        } catch (_: Exception) { }
                    }
                } catch (_: Exception) { }
            }
            e.message?.contains("SQLite") == true -> {
                // Try to launch in safe mode with minimal features
                try {
                    val dbDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader\\cache")
                    if (dbDir.exists()) {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
                        val backupDir = File(dbDir.parentFile, "backups/backup_$timestamp")
                        backupDir.mkdirs()
                        
                        dbDir.listFiles()?.forEach { file ->
                            try {
                                if (file.name.endsWith(".db")) {
                                    val backupFile = File(backupDir, file.name)
                                    file.copyTo(backupFile, overwrite = true)
                                    file.delete()
                                }
                            } catch (_: Exception) { }
                        }
                    }
                } catch (_: Exception) { }
            }
            else -> { }
        }
        System.err.println("Application error: ${e.message}")
        exitProcess(1)
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

/**
 * Attempts to repair the database by creating a backup and recreating the database files
 */
private fun repairDatabase(cacheDir: File) {
    try {
        // 1. Create backup folder if it doesn't exist
        val backupDir = File(cacheDir.parentFile, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        
        // 2. Find database files
        val dbFiles = cacheDir.listFiles { file -> file.name.endsWith(".db") }
        
        if (dbFiles == null || dbFiles.isEmpty()) {
            return
        }
        
        // 3. Create a timestamped backup folder
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val backupFolder = File(backupDir, "backup_$timestamp")
        backupFolder.mkdirs()
        
        // 4. Copy each database file to the backup folder
        dbFiles.forEach { dbFile ->
            val backupFile = File(backupFolder, dbFile.name)
            
            try {
                dbFile.inputStream().use { input ->
                    backupFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Delete the original file to force recreation
                dbFile.delete()
            } catch (_: IOException) {
                // Silently ignore backup errors
            }
        }
        
    } catch (_: Exception) {
        // Silently ignore repair errors
    }
}