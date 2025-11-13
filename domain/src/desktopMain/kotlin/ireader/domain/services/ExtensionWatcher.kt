package ireader.domain.services

import ireader.core.log.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.nio.file.*

/**
 * Watches the extensions directory for changes and emits events when extensions are added or removed.
 * Desktop-only implementation using Java WatchService.
 */
class ExtensionWatcher(
    private val extensionsDir: File,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val _events = MutableSharedFlow<ExtensionEvent>(replay = 0, extraBufferCapacity = 10)
    val events: SharedFlow<ExtensionEvent> = _events.asSharedFlow()
    
    private var watchJob: Job? = null
    private var watchService: WatchService? = null
    
    /**
     * Start watching the extensions directory for changes
     */
    fun start() {
        if (watchJob?.isActive == true) {
            Log.info("ExtensionWatcher is already running")
            return
        }
        
        // Ensure extensions directory exists
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs()
            Log.info("Created extensions directory: ${extensionsDir.absolutePath}")
        }
        
        watchJob = scope.launch {
            try {
                watchService = FileSystems.getDefault().newWatchService()
                val path = extensionsDir.toPath()
                
                // Register directory for create, delete, and modify events
                path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )
                
                Log.info("ExtensionWatcher started monitoring: ${extensionsDir.absolutePath}")
                
                // Track known extensions to detect actual changes
                val knownExtensions = mutableSetOf<String>()
                scanInitialExtensions(knownExtensions)
                
                while (isActive) {
                    val key = watchService?.take() ?: break
                    
                    // Add small delay to debounce rapid file system events
                    delay(500)
                    
                    for (event in key.pollEvents()) {
                        val kind = event.kind()
                        
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue
                        }
                        
                        @Suppress("UNCHECKED_CAST")
                        val ev = event as WatchEvent<Path>
                        val filename = ev.context()
                        val child = path.resolve(filename)
                        val file = child.toFile()
                        
                        // Only process directories (extensions are stored in directories)
                        if (file.isDirectory) {
                            val extensionName = file.name
                            val apkFile = File(file, "$extensionName.apk")
                            
                            when (kind) {
                                StandardWatchEventKinds.ENTRY_CREATE -> {
                                    // Wait a bit for the APK file to be fully copied
                                    delay(1000)
                                    if (apkFile.exists() && !knownExtensions.contains(extensionName)) {
                                        knownExtensions.add(extensionName)
                                        Log.info("Extension added: $extensionName")
                                        _events.emit(ExtensionEvent.Added(file))
                                    }
                                }
                                StandardWatchEventKinds.ENTRY_DELETE -> {
                                    if (knownExtensions.contains(extensionName)) {
                                        knownExtensions.remove(extensionName)
                                        Log.info("Extension removed: $extensionName")
                                        _events.emit(ExtensionEvent.Removed(file))
                                    }
                                }
                                StandardWatchEventKinds.ENTRY_MODIFY -> {
                                    // Check if this is a new extension being added
                                    if (apkFile.exists() && !knownExtensions.contains(extensionName)) {
                                        knownExtensions.add(extensionName)
                                        Log.info("Extension added (via modify): $extensionName")
                                        _events.emit(ExtensionEvent.Added(file))
                                    }
                                }
                            }
                        }
                    }
                    
                    val valid = key.reset()
                    if (!valid) {
                        Log.warn("ExtensionWatcher key no longer valid, stopping")
                        break
                    }
                }
            } catch (e: ClosedWatchServiceException) {
                Log.info("ExtensionWatcher stopped")
            } catch (e: Exception) {
                Log.error(e, "Error in ExtensionWatcher")
            }
        }
    }
    
    /**
     * Scan the extensions directory to build initial list of known extensions
     */
    private fun scanInitialExtensions(knownExtensions: MutableSet<String>) {
        extensionsDir.listFiles()?.forEach { dir ->
            if (dir.isDirectory) {
                val extensionName = dir.name
                val apkFile = File(dir, "$extensionName.apk")
                if (apkFile.exists()) {
                    knownExtensions.add(extensionName)
                }
            }
        }
        Log.info("Found ${knownExtensions.size} existing extensions")
    }
    
    /**
     * Stop watching the extensions directory
     */
    fun stop() {
        watchJob?.cancel()
        watchJob = null
        
        try {
            watchService?.close()
        } catch (e: Exception) {
            Log.error(e, "Error closing WatchService")
        }
        watchService = null
        
        Log.info("ExtensionWatcher stopped")
    }
    
    /**
     * Check if the watcher is currently running
     */
    fun isRunning(): Boolean = watchJob?.isActive == true
}

/**
 * Events emitted by the ExtensionWatcher
 */
sealed class ExtensionEvent {
    /**
     * An extension was added to the extensions directory
     */
    data class Added(val extensionDir: File) : ExtensionEvent()
    
    /**
     * An extension was removed from the extensions directory
     */
    data class Removed(val extensionDir: File) : ExtensionEvent()
}
