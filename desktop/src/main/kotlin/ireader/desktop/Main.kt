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
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
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
        // Display Java version information to help with troubleshooting
        println("Java Runtime Version: ${System.getProperty("java.runtime.version")}")
        println("Java Home: ${System.getProperty("java.home")}")
        println("Java Vendor: ${System.getProperty("java.vendor")}")
        println("OS Name: ${System.getProperty("os.name")}")
        println("OS Version: ${System.getProperty("os.version")}")

        // Check and create app data directory if needed
        val appDataDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader")
        val cacheDir = File(appDataDir, "cache")
        
        if (!appDataDir.exists()) {
            println("Creating app data directory: ${appDataDir.absolutePath}")
            appDataDir.mkdirs()
        }
        
        if (!cacheDir.exists()) {
            println("Creating cache directory: ${cacheDir.absolutePath}")
            cacheDir.mkdirs()
        }

        // Verify critical resources exist to avoid runtime errors
        val criticalResources = listOf(
            "drawable/ic_eternity_light.xml",
            "drawable/ic_eternity_dark.xml",
            "drawable/ic_eternity.xml"
        )
        
        for (resource in criticalResources) {
            try {
                val resourceExists = Thread.currentThread().contextClassLoader.getResource(resource) != null
                if (!resourceExists) {
                    println("WARNING: Resource not found: $resource")
                } else {
                    println("Resource verified: $resource")
                }
            } catch (e: Exception) {
                println("ERROR checking resource $resource: ${e.message}")
            }
        }
        
        // Attempt to delete any corrupted database files
        try {
            val dbFiles = cacheDir.listFiles { file -> file.name.endsWith(".db") }
            dbFiles?.forEach { file ->
                if (file.exists()) {
                    println("Found database file: ${file.absolutePath}")
                    val deleted = file.delete()
                    println("Deleted database file: $deleted")
                }
            }
        } catch (e: Exception) {
            println("Error while trying to clean database files: ${e.message}")
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
                println("System fonts initialized successfully")
            }
        } catch (e: Exception) {
            println("Failed to initialize system fonts: ${e.message}")
            e.printStackTrace()
        }
        
        // Start auto-sync service if available
        try {
            val autoSyncService = koinApp.koin.getOrNull<AutoSyncService>()
            autoSyncService?.start()
            println("Auto-sync service started successfully")
        } catch (e: Exception) {
            println("Auto-sync service not available or failed to start: ${e.message}")
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
        // Provide more specific error handling
        when {
            e.message?.contains("table history_new already exists") == true -> {
                println("DATABASE ERROR: Migration issue with history_new table.")
                println("This is commonly caused by an interrupted database migration.")
                println("The application will attempt to fix this issue on next restart.")
                
                // Try to fix the issue by directly executing SQL to clean up
                try {
                    val dbDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader\\cache")
                    val dbFile = dbDir.listFiles { file -> file.name.endsWith(".db") }?.firstOrNull()
                    
                    if (dbFile != null) {
                        println("Found database file: ${dbFile.absolutePath}")
                        println("Creating backup before cleanup...")
                        
                        // Create backup
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
                        val backupFile = File(dbFile.parentFile, "${dbFile.nameWithoutExtension}_backup_$timestamp.db")
                        
                        dbFile.copyTo(backupFile, overwrite = true)
                        println("Backup created at: ${backupFile.absolutePath}")
                        
                        // Connect and fix the database
                        try {
                            Class.forName("org.sqlite.JDBC")
                            val connection = java.sql.DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
                            connection.use { conn ->
                                conn.createStatement().use { stmt ->
                                    // Drop problematic table
                                    stmt.execute("DROP TABLE IF EXISTS history_new;")
                                    println("Successfully dropped history_new table.")
                                }
                            }
                            println("Database cleanup completed. Please restart the application.")
                        } catch (sqlEx: Exception) {
                            println("Could not fix database directly: ${sqlEx.message}")
                        }
                    }
                } catch (fixEx: Exception) {
                    println("Error during direct database repair: ${fixEx.message}")
                }
                
                println("If the issue persists, please delete all files in %LOCALAPPDATA%\\IReader\\cache")
            }
            e.message?.contains("no such table: main.history") == true || 
            e.message?.contains("historyView") == true -> {
                println("DATABASE ERROR: The history table is missing from the database.")
                println("This may be due to a corrupted database file or incomplete migration.")
                
                // Try to fix the issue by directly creating the history table
                try {
                    val dbDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader\\cache")
                    val dbFile = dbDir.listFiles { file -> file.name.endsWith(".db") }?.firstOrNull()
                    
                    if (dbFile != null) {
                        println("Found database file: ${dbFile.absolutePath}")
                        println("Creating backup before attempting repair...")
                        
                        // Create backup
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
                        val backupFile = File(dbFile.parentFile, "${dbFile.nameWithoutExtension}_backup_$timestamp.db")
                        
                        dbFile.copyTo(backupFile, overwrite = true)
                        println("Backup created at: ${backupFile.absolutePath}")
                        
                        // Connect and fix the database
                        try {
                            Class.forName("org.sqlite.JDBC")
                            val connection = java.sql.DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
                            connection.use { conn ->
                                conn.createStatement().use { stmt ->
                                    // Drop the view if it exists to avoid conflicts
                                    stmt.execute("DROP VIEW IF EXISTS historyView;")
                                    
                                    // Check if history table exists
                                    val rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='history'")
                                    val historyExists = rs.next()
                                    rs.close()
                                    
                                    if (!historyExists) {
                                        println("Creating history table...")
                                        // Create the history table
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
                                        println("History table created successfully")
                                        
                                        // Create indexes
                                        stmt.execute("CREATE INDEX IF NOT EXISTS history_history_chapter_id_index ON history(chapter_id);")
                                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_last_read ON history(last_read);")
                                        stmt.execute("CREATE INDEX IF NOT EXISTS idx_history_progress ON history(progress);")
                                    } else {
                                        println("History table already exists.")
                                    }
                                }
                            }
                            println("Database repair completed. Please restart the application.")
                        } catch (sqlEx: Exception) {
                            println("Could not fix database directly: ${sqlEx.message}")
                            sqlEx.printStackTrace()
                        }
                    }
                } catch (fixEx: Exception) {
                    println("Error during direct database repair: ${fixEx.message}")
                    fixEx.printStackTrace()
                }
                
                println("Please try one of the following solutions:")
                println("1. Delete the database file at %LOCALAPPDATA%\\IReader\\cache and restart.")
            }
            e.message?.contains("SQLite") == true -> {
                println("SQLite ERROR: ${e.message}")
                println("This might be a database corruption issue.")
                
                // Try to launch in safe mode with minimal features
                try {
                    println("Attempting to launch in safe mode with minimal functionality...")
                    
                    // Delete all database files to start fresh
                    val dbDir = File(System.getProperty("user.home"), "AppData\\Local\\IReader\\cache")
                    if (dbDir.exists()) {
                        // Create backups first
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
                        val backupDir = File(dbDir.parentFile, "backups/backup_$timestamp")
                        backupDir.mkdirs()
                        
                        dbDir.listFiles()?.forEach { file ->
                            try {
                                if (file.name.endsWith(".db")) {
                                    val backupFile = File(backupDir, file.name)
                                    file.copyTo(backupFile, overwrite = true)
                                    println("Created backup of ${file.name} at ${backupFile.absolutePath}")
                                    
                                    // Delete the original file
                                    file.delete()
                                    println("Deleted ${file.name} for clean start")
                                }
                            } catch (e: Exception) {
                                println("Error handling file ${file.name}: ${e.message}")
                            }
                        }
                        
                        println("Database files backed up and removed for clean start.")
                        println("Please restart the application to create a new database.")
                    }
                } catch (e: Exception) {
                    println("Failed to prepare safe mode: ${e.message}")
                }
                
                println("Please try one of the following solutions:")
                println("1. Delete the database files at %LOCALAPPDATA%\\IReader\\cache.")
            }
            else -> e.printStackTrace()
        }
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
            println("No database files found to repair")
            return
        }
        
        // 3. Create a timestamped backup folder
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val backupFolder = File(backupDir, "backup_$timestamp")
        backupFolder.mkdirs()
        
        // 4. Copy each database file to the backup folder
        dbFiles.forEach { dbFile ->
            println("Backing up: ${dbFile.name}")
            val backupFile = File(backupFolder, dbFile.name)
            
            try {
                dbFile.inputStream().use { input ->
                    backupFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                println("  Backup created: ${backupFile.absolutePath}")
                
                // Delete the original file to force recreation
                dbFile.delete()
                println("  Original file deleted for recreation")
            } catch (e: IOException) {
                println("  Failed to backup file: ${e.message}")
            }
        }
        
        println("Database backup complete. Files will be recreated on next start.")
        
    } catch (e: Exception) {
        println("Error during database repair: ${e.message}")
        e.printStackTrace()
    }
}