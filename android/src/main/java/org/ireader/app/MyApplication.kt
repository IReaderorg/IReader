package org.ireader.app

import android.app.Application
import android.os.Build
import android.os.Looper
import android.os.Trace
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import ireader.core.http.WebViewUtil
import ireader.core.startup.LazyInitializer
import ireader.core.startup.StartupProfiler
import ireader.data.core.DatabaseHandler
import ireader.data.core.DatabasePreloader
import ireader.data.di.DataModule
import ireader.data.di.dataPlatformModule
import ireader.data.di.remoteModule
import ireader.data.di.remotePlatformModule
import ireader.data.di.repositoryInjectModule
import ireader.data.di.reviewModule
import ireader.data.remote.AutoSyncService
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
import ireader.presentation.imageloader.CoilLoaderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ireader.app.di.AppModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.ireader.app.crash.CrashHandler
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application(), SingletonImageLoader.Factory, KoinComponent {
    
    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    
    // Lazy coil to avoid initialization during startup
    private val _coil: CoilLoaderFactory by lazy { 
        val factory: CoilLoaderFactory by inject()
        factory
    }
    val coil: CoilLoaderFactory get() = _coil
    
    override fun onCreate() {
        // Add trace for baseline profile generation
        Trace.beginSection("MyApplication.onCreate")
        super.onCreate()
        
        // Start profiling
        StartupProfiler.start()
        
        // Initialize crash handler first (fast, required)
        Trace.beginSection("CrashHandler.init")
        CrashHandler.initialize(this)
        Trace.endSection()
        StartupProfiler.mark("crash_handler")
        
        // Configure Kermit logging
        // Debug: logs to Logcat (default behavior)
        // Release: could add CrashlyticsLogWriter for Firebase integration
        // Kermit auto-configures with sensible defaults, no explicit init needed
        
        // Initialize Koin - all modules
        Trace.beginSection("Koin.init")
        initializeKoin()
        Trace.endSection()
        StartupProfiler.mark("koin_init")
        
        // Schedule background initialization (non-blocking)
        scheduleBackgroundInit()
        StartupProfiler.mark("background_scheduled")
        
        // Finish startup profiling - this is the time to first frame
        StartupProfiler.finish()
        StartupProfiler.printReport()
        
        Trace.endSection() // MyApplication.onCreate
        
        println("")
        println("=== APP READY FOR FIRST FRAME ===")
        println("")
    }
    
    /**
     * Initialize Koin with ULTRA-optimized module loading.
     * 
     * Strategy for native-like startup:
     * 1. Phase 1: Absolute minimum for first frame (preferences only)
     * 2. Phase 2: Core UI modules (loaded immediately after startKoin)
     * 3. Phase 3: Secondary modules (loaded after first frame, before user interaction)
     * 4. Phase 4: Background modules (loaded lazily in background)
     * 
     * Target: <500ms to first frame
     */
    private fun initializeKoin() {
        val totalStart = System.currentTimeMillis()
        
        // Phase 1: Absolute minimum for startKoin (< 50ms)
        // Only preferences - everything else deferred
        val koinApp = startKoin {
            androidLogger(Level.NONE)
            androidContext(this@MyApplication)
            workManagerFactory()
            KoinApplication.init()
            
            modules(
                preferencesInjectModule,  // Lightweight preferences only
                localModule               // Lightweight local utilities
            )
        }
        println("Phase 1 (preferences): ${System.currentTimeMillis() - totalStart}ms")
        
        // Phase 2: Core modules for first frame (synchronous but minimal)
        // Only what's absolutely needed to show the library screen
        val phase2Start = System.currentTimeMillis()
        koinApp.koin.loadModules(listOf(
            dataPlatformModule,           // Platform-specific data
            DataModule,                   // Database (lazy internally)
            repositoryInjectModule,       // Repositories
        ))
        println("Phase 2 (data): ${System.currentTimeMillis() - phase2Start}ms")
        
        // Phase 3: Plugin and domain modules (PluginModule MUST come before CatalogModule)
        val phase3Start = System.currentTimeMillis()
        koinApp.koin.loadModules(listOf(
            PluginModule,                 // MUST be first - CatalogLoader depends on PluginManager
            remotePlatformModule,
            remoteModule,
            DomainModule,                 // Domain logic
            CatalogModule,                // Catalog (depends on PluginManager)
        ))
        println("Phase 3 (domain): ${System.currentTimeMillis() - phase3Start}ms")
        
        // Phase 4: UI and service modules
        val phase4Start = System.currentTimeMillis()
        koinApp.koin.loadModules(listOf(
            PresentationModules,          // ViewModels (all factory)
            presentationPlatformModule,   // Platform UI
            AppModule,                    // App-specific
            reviewModule,
            platformServiceModule,
            ireader.domain.di.ServiceModule,
            UseCasesInject,
            DomainServices
        ))
        println("Phase 4 (UI): ${System.currentTimeMillis() - phase4Start}ms")
        
        // Initialize SecureStorageHelper (fast, needed for plugins)
        initializeSecureStorage()
        
        // Mark modules as fully initialized
        ireader.domain.di.ModuleInitializationState.markFullyInitialized()
        println("=== All modules loaded ===")
        
        println("=== Koin initialization (to first frame): ${System.currentTimeMillis() - totalStart}ms ===")
    }
    
    private fun initializeSecureStorage() {
        try {
            val uiPreferences: ireader.domain.preferences.prefs.UiPreferences by inject()
            ireader.domain.storage.SecureStorageHelper.init(this@MyApplication, uiPreferences)
            
            // Sync JS plugins in background (don't block startup)
            backgroundScope.launch {
                try {
                    val (fromSaf, toSaf) = ireader.domain.storage.SecureStorageHelper.syncJsPluginsBidirectional(this@MyApplication)
                    if (fromSaf > 0 || toSaf > 0) {
                        println("Synced JS plugins: $fromSaf from SAF, $toSaf to SAF")
                    }
                } catch (e: Exception) {
                    println("JS plugin sync failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("SecureStorageHelper init failed: ${e.message}")
        }
    }
    
    private fun scheduleBackgroundInit() {
        // Database initialization in background
        backgroundScope.launch {
            initializeDatabaseAsync()
        }
        
        // Register lazy tasks
        registerLazyTasks()
    }
    
    private suspend fun initializeDatabaseAsync() {
        try {
            val start = System.currentTimeMillis()
            delay(100) // Let UI thread breathe
            
            val databaseHandler: DatabaseHandler by inject()
            databaseHandler.initialize()
            
            println("Database initialization: ${System.currentTimeMillis() - start}ms")
            
            // Delay preloader more to let UI settle first
            delay(2000)
            try {
                println("MyApplication: Starting database preloader...")
                val preloader: DatabasePreloader by inject()
                preloader.preloadCriticalData()
                println("MyApplication: Database preloader completed")
            } catch (e: Exception) {
                println("Database preloader failed: ${e.message}")
            }
        } catch (e: Exception) {
            println("Failed to initialize database: ${e.message}")
        }
    }
    
    private fun registerLazyTasks() {
        LazyInitializer.register("system_fonts", LazyInitializer.Priority.LOW) {
            try {
                delay(2000)
                val systemFontsInitializer: ireader.domain.usecases.fonts.SystemFontsInitializer by inject()
                systemFontsInitializer.initializeSystemFonts()
                println("System fonts initialized")
            } catch (e: Exception) {
                println("System fonts failed: ${e.message}")
            }
        }
        
        LazyInitializer.register("auto_sync", LazyInitializer.Priority.LOW) {
            try {
                delay(5000)
                val autoSyncService: AutoSyncService? by inject()
                autoSyncService?.start()
            } catch (e: Exception) {
                // Silent - not critical
            }
        }
    }
    
    fun onAppVisible() {
        LazyInitializer.start()
    }

    override fun getPackageName(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val stackTrace = Looper.getMainLooper().thread.stackTrace
                val chromiumElement = stackTrace.find {
                    it.className.equals("org.chromium.base.BuildInfo", ignoreCase = true)
                }
                if (chromiumElement?.methodName.equals("getAll", ignoreCase = true)) {
                    return WebViewUtil.SPOOF_PACKAGE_NAME
                }
            } catch (_: Exception) {}
        }
        return super.getPackageName()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return coil.newImageLoader(this)
    }
}
