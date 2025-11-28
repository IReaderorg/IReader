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
     * Initialize Koin with all modules.
     */
    private fun initializeKoin() {
        val totalStart = System.currentTimeMillis()
        
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@MyApplication)
            workManagerFactory()
            KoinApplication.init()
            
            // Load all modules at once (faster than one by one)
            modules(
                dataPlatformModule,
                DataModule,
                preferencesInjectModule,
                localModule,
                repositoryInjectModule,
                remotePlatformModule,
                remoteModule,
                reviewModule,
                platformServiceModule,
                ireader.domain.di.ServiceModule,
                UseCasesInject,
                DomainServices,
                DomainModule,
                CatalogModule,
                PluginModule,
                PresentationModules,
                presentationPlatformModule,
                AppModule
            )
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
