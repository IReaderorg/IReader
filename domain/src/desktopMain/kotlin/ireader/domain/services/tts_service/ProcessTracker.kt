package ireader.domain.services.tts_service

import ireader.core.log.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * ProcessTracker - Tracks all child processes spawned by TTS engines
 * 
 * This class maintains a registry of all active processes (Piper, Kokoro, Maya)
 * and provides functionality to detect and clean up zombie processes.
 * 
 * Thread-safe implementation using ConcurrentHashMap.
 */
class ProcessTracker {
    // Map of process ID to ProcessInfo
    private val trackedProcesses = ConcurrentHashMap<Long, ProcessInfo>()
    
    /**
     * Register a process for tracking
     * 
     * @param process The process to track
     * @param engineType The TTS engine that spawned this process (e.g., "Piper", "Kokoro", "Maya")
     * @param description Optional description of what this process is doing
     */
    fun registerProcess(process: Process, engineType: String, description: String = "") {
        val pid = process.pid()
        val info = ProcessInfo(
            process = process,
            engineType = engineType,
            description = description,
            registeredAt = System.currentTimeMillis()
        )
        
        trackedProcesses[pid] = info
        Log.debug { "Registered $engineType process (PID: $pid) - ${trackedProcesses.size} total processes tracked" }
    }
    
    /**
     * Unregister a process from tracking
     * 
     * @param process The process to unregister
     */
    fun unregisterProcess(process: Process) {
        val pid = process.pid()
        val info = trackedProcesses.remove(pid)
        
        if (info != null) {
            Log.debug { "Unregistered ${info.engineType} process (PID: $pid) - ${trackedProcesses.size} total processes tracked" }
        }
    }
    
    /**
     * Get count of active processes
     * 
     * @return Number of currently tracked processes
     */
    fun getActiveProcessCount(): Int {
        return trackedProcesses.size
    }
    
    /**
     * Get count of active processes for a specific engine
     * 
     * @param engineType The engine type to count (e.g., "Kokoro", "Maya")
     * @return Number of active processes for that engine
     */
    fun getActiveProcessCountByEngine(engineType: String): Int {
        return trackedProcesses.values.count { it.engineType == engineType }
    }
    
    /**
     * Detect and return list of zombie processes
     * 
     * A zombie process is one that is no longer alive but still tracked.
     * 
     * @return List of zombie ProcessInfo objects
     */
    fun detectZombieProcesses(): List<ProcessInfo> {
        val zombies = mutableListOf<ProcessInfo>()
        
        trackedProcesses.values.forEach { info ->
            if (!info.process.isAlive) {
                zombies.add(info)
            }
        }
        
        if (zombies.isNotEmpty()) {
            Log.warn { "Detected ${zombies.size} zombie processes" }
        }
        
        return zombies
    }
    
    /**
     * Clean up zombie processes
     * 
     * Removes dead processes from tracking and attempts to forcibly terminate
     * any that are still lingering.
     * 
     * @return Number of zombie processes cleaned up
     */
    fun cleanupZombieProcesses(): Int {
        val zombies = detectZombieProcesses()
        var cleanedCount = 0
        
        zombies.forEach { info ->
            try {
                val pid = info.process.pid()
                
                // Try to forcibly destroy if somehow still alive
                if (info.process.isAlive) {
                    Log.warn { "Forcibly terminating zombie ${info.engineType} process (PID: $pid)" }
                    info.process.destroyForcibly()
                }
                
                // Remove from tracking
                trackedProcesses.remove(pid)
                cleanedCount++
                
                Log.info { "Cleaned up zombie ${info.engineType} process (PID: $pid, age: ${info.getAgeSeconds()}s)" }
            } catch (e: Exception) {
                Log.error { "Failed to clean up zombie process: ${e.message}" }
            }
        }
        
        if (cleanedCount > 0) {
            Log.info { "Cleaned up $cleanedCount zombie processes" }
        }
        
        return cleanedCount
    }
    
    /**
     * Terminate all tracked processes
     * 
     * This should be called when stopping TTS or on app exit.
     * 
     * @return Number of processes terminated
     */
    fun terminateAllProcesses(): Int {
        val count = trackedProcesses.size
        
        if (count == 0) {
            return 0
        }
        
        Log.info { "Terminating all $count tracked processes..." }
        
        trackedProcesses.values.forEach { info ->
            try {
                if (info.process.isAlive) {
                    Log.debug { "Terminating ${info.engineType} process (PID: ${info.process.pid()})" }
                    info.process.destroyForcibly()
                }
            } catch (e: Exception) {
                Log.error { "Failed to terminate process: ${e.message}" }
            }
        }
        
        trackedProcesses.clear()
        Log.info { "All processes terminated and cleared from tracking" }
        
        return count
    }
    
    /**
     * Get statistics about tracked processes
     * 
     * @return Map of engine type to process count
     */
    fun getProcessStatistics(): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        
        trackedProcesses.values.forEach { info ->
            stats[info.engineType] = stats.getOrDefault(info.engineType, 0) + 1
        }
        
        return stats
    }
    
    /**
     * Log current process tracking status
     */
    fun logStatus() {
        if (trackedProcesses.isEmpty()) {
            Log.debug { "No processes currently tracked" }
            return
        }
        
        val stats = getProcessStatistics()
        val statsStr = stats.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        
        Log.info { "Process tracking status: $statsStr (${trackedProcesses.size} total)" }
    }
    
    /**
     * Information about a tracked process
     */
    data class ProcessInfo(
        val process: Process,
        val engineType: String,
        val description: String,
        val registeredAt: Long
    ) {
        /**
         * Get age of this process in seconds
         */
        fun getAgeSeconds(): Long {
            return (System.currentTimeMillis() - registeredAt) / 1000
        }
    }
}
