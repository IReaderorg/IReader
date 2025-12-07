//package ireader.domain.di
//
//import org.koin.core.module.Module
//import org.koin.core.qualifier.named
//import org.koin.dsl.module
//
///**
// * Lazy-loaded modules that are not needed immediately at startup.
// * These modules are loaded on-demand when first accessed.
// *
// * This reduces startup time by deferring initialization of:
// * - Plugin system
// * - Analytics
// * - Cloud backup
// * - Sync services
// * - Translation engines
// */
//
///**
// * Plugin system module - loaded when user accesses plugins.
// */
//val lazyPluginModule: Module = module {
//    // Plugin components are already defined in PluginModule
//    // This is a marker for lazy loading
//}
//
///**
// * Analytics module - loaded after app is visible.
// */
//val lazyAnalyticsModule: Module = module {
//    // Analytics can be deferred
//}
//
///**
// * Cloud backup module - loaded when user accesses backup settings.
// */
//val lazyBackupModule: Module = module {
//    // Backup providers can be deferred
//}
//
///**
// * Sync module - loaded when user enables sync.
// */
//val lazySyncModule: Module = module {
//    // Sync services can be deferred
//}
//
///**
// * Helper object to track which lazy modules have been loaded.
// */
//object LazyModuleLoader {
//    private val loadedModules = mutableSetOf<String>()
//
//    fun isLoaded(moduleName: String): Boolean = moduleName in loadedModules
//
//    fun markLoaded(moduleName: String) {
//        loadedModules.add(moduleName)
//    }
//
//    fun reset() {
//        loadedModules.clear()
//    }
//}
