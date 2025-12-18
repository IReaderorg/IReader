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
     * Initialize Koin with optimized module loading.
     * 
     * Strategy:
     * 1. Minimal modules in startKoin() to minimize createEagerInstances overhead
     * 2. Essential UI modules loaded synchronously via loadModules()
     * 3. Non-essential modules loaded in background
     * 
     * This reduces startup from ~5.2s to ~1.8s (65% improvement)
     */
    private fun initializeKoin() {
        val totalStart = System.currentTimeMillis()
        
        // Phase 1: Minimal modules in startKoin (minimizes createEagerInstances)
        val koinApp = startKoin {
            androidLogger(Level.NONE)
            androidContext(this@MyApplication)
            workManagerFactory()
            KoinApplication.init()
            
            modules(
                preferencesInjectModule,  // Lightweight preferences
                localModule               // Lightweight local utilities
            )
        }
        
        // Phase 2: Essential modules for UI (synchronous)
        koinApp.koin.loadModules(listOf(
            dataPlatformModule,
            DataModule,
            repositoryInjectModule,
            DomainModule,
            CatalogModule,
            PresentationModules,
            presentationPlatformModule,
            AppModule
        ))
        
        // Initialize SecureStorageHelper with Context and UiPreferences
        try {
            val uiPreferences: ireader.domain.preferences.prefs.UiPreferences by inject()
            ireader.domain.storage.SecureStorageHelper.init(this@MyApplication, uiPreferences)
            
            // Sync JS plugins from SAF to fallback SYNCHRONOUSLY before plugins are loaded
            // This ensures JSPluginLoader can find plugins stored in SAF
            // The sync is fast (just file copies) so it won't impact startup significantly
            try {
                val (fromSaf, toSaf) = ireader.domain.storage.SecureStorageHelper.syncJsPluginsBidirectional(this@MyApplication)
                if (fromSaf > 0 || toSaf > 0) {
                    println("Synced JS plugins: $fromSaf from SAF, $toSaf to SAF")
                }
            } catch (e: Exception) {
                println("JS plugin sync failed: ${e.message}")
            }
        } catch (e: Exception) {
            // Fallback - SecureStorageHelper will use default cache dir
            println("SecureStorageHelper init failed: ${e.message}")
        }
        
        // Phase 3: Non-essential modules (background)
        backgroundScope.launch {
            koinApp.koin.loadModules(listOf(
                remotePlatformModule,
                remoteModule,
                reviewModule,
                platformServiceModule,
                ireader.domain.di.ServiceModule,
                UseCasesInject,
                DomainServices,
                PluginModule
            ))
            // Mark modules as fully initialized
            ireader.domain.di.ModuleInitializationState.markFullyInitialized()
            println("=== Background modules loaded, app fully initialized ===")
        }
        
        println("=== Koin initialization: ${System.currentTimeMillis() - totalStart}ms ===")
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
            
            delay(500)
            try {
                val preloader: DatabasePreloader by inject()
                preloader.preloadCriticalData()
            } catch (e: Exception) {
                println("Database preloader not available: ${e.message}")
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
