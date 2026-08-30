package ireader.core.startup

import ireader.core.log.Log
import ireader.core.time.currentTimeMillis
import ireader.core.util.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages lazy initialization of non-critical components.
 * 
 * Components are initialized in the background after the app is visible,
 * reducing perceived startup time.
 */
object LazyInitializer {
    private const val TAG = "LazyInitializer"
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private val tasks = mutableListOf<InitTask>()
    
    @kotlin.concurrent.Volatile
    private var isStarted = false
    
    @kotlin.concurrent.Volatile
    private var isCompleted = false
    
    data class InitTask(
        val name: String,
        val priority: Priority,
        val task: suspend () -> Unit
    )
    
    enum class Priority {
        HIGH,    // Initialize within 1 second of app visible
        MEDIUM,  // Initialize within 5 seconds
        LOW      // Initialize when idle
    }
    
    /**
     * Register a task for lazy initialization.
     * Synchronous and thread-safe to ensure tasks are registered before start().
     */
    fun register(name: String, priority: Priority = Priority.MEDIUM, task: suspend () -> Unit) {
        synchronized(lock) {
            tasks.add(InitTask(name, priority, task))
            Log.debug("Registered lazy init task: $name (priority: $priority)", TAG)
        }
    }
    
    /**
     * Start executing registered tasks.
     * Call this after the main UI is visible.
     */
    fun start() {
        val tasksToExecute = synchronized(lock) {
            if (isStarted) return
            isStarted = true
            tasks.sortedBy { it.priority.ordinal }
        }
        
        Log.info("Starting lazy initialization with ${tasksToExecute.size} tasks...", TAG)
        
        scope.launch {
            tasksToExecute.forEach { task ->
                try {
                    val start = currentTimeMillis()
                    task.task()
                    val duration = currentTimeMillis() - start
                    Log.info("Lazy init '${task.name}' completed in ${duration}ms", TAG)
                } catch (e: Exception) {
                    Log.error("Lazy init '${task.name}' failed: ${e.message}", TAG)
                }
            }
            
            isCompleted = true
            Log.info("All lazy initialization tasks completed", TAG)
        }
    }
    
    /**
     * Check if all tasks are completed.
     */
    fun isCompleted(): Boolean = isCompleted
    
    /**
     * Clear all registered tasks.
     */
    fun clear() {
        synchronized(lock) {
            tasks.clear()
            isStarted = false
            isCompleted = false
        }
    }
}
