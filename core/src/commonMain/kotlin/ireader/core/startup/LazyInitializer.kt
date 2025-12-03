package ireader.core.startup

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ireader.core.time.currentTimeMillis

/**
 * Manages lazy initialization of non-critical components.
 * 
 * Components are initialized in the background after the app is visible,
 * reducing perceived startup time.
 */
object LazyInitializer {
    private const val TAG = "LazyInitializer"
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tasks = mutableListOf<InitTask>()
    private val mutex = Mutex()
    private var isStarted = false
    
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
     */
    fun register(name: String, priority: Priority = Priority.MEDIUM, task: suspend () -> Unit) {
        scope.launch {
            mutex.withLock {
                tasks.add(InitTask(name, priority, task))
                Log.debug("Registered lazy init task: $name (priority: $priority)", TAG)
            }
        }
    }
    
    /**
     * Start executing registered tasks.
     * Call this after the main UI is visible.
     */
    fun start() {
        if (isStarted) return
        isStarted = true
        
        Log.info("Starting lazy initialization...", TAG)
        
        scope.launch {
            val sortedTasks = mutex.withLock {
                tasks.sortedBy { it.priority.ordinal }
            }
            
            sortedTasks.forEach { task ->
                try {
                    val start = currentTimeMillis()
                    task.task()
                    val duration = currentTimeMillis() - start
                    Log.info("Lazy init '${task.name}' completed in ${duration}ms", TAG)
                } catch (e: Exception) {
                    Log.error("Lazy init '${task.name}' failed: ${e.message}", TAG)
                }
            }
            
            Log.info("All lazy initialization tasks completed", TAG)
        }
    }
    
    /**
     * Check if all tasks are completed.
     */
    suspend fun isCompleted(): Boolean = mutex.withLock {
        tasks.isEmpty()
    }
    
    /**
     * Clear all registered tasks.
     */
    suspend fun clear() = mutex.withLock {
        tasks.clear()
        isStarted = false
    }
}
