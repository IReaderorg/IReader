package ireader.domain.plugins.hotreload

import ireader.core.io.FileSystem
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.HashingSink
import okio.blackholeSink
import okio.buffer

/**
 * Implementation of FileWatcher using the core FileSystem.
 */
class FileWatcherImpl(
    private val fileSystem: FileSystem
) : FileWatcher {
    
    private val mutex = Mutex()
    private val fileStates = mutableMapOf<String, FileState>()
    private var watchPaths: List<String> = emptyList()
    private var watchPatterns: List<String> = emptyList()
    
    private data class FileState(
        val path: String,
        val lastModified: Long,
        val size: Long,
        val hash: String
    )
    
    override suspend fun detectChanges(
        paths: List<String>,
        patterns: List<String>
    ): List<FileChange> {
        val changes = mutableListOf<FileChange>()
        
        mutex.withLock {
            for (path in paths) {
                val directory = fileSystem.getDataDirectory().resolve(path)
                if (!directory.exists()) continue
                
                val files = directory.listFiles()
                    .filter { file -> 
                        patterns.any { pattern ->
                            matchesPattern(file.name, pattern)
                        }
                    }
                
                for (file in files) {
                    val filePath = file.path
                    val currentState = getFileState(file)
                    val previousState = fileStates[filePath]
                    
                    when {
                        previousState == null -> {
                            // New file
                            changes.add(FileChange(
                                path = filePath,
                                pluginId = extractPluginId(file.name),
                                changeType = FileChangeType.CREATED,
                                timestamp = currentTimeToLong(),
                                fileSize = currentState.size
                            ))
                            fileStates[filePath] = currentState
                        }
                        previousState.hash != currentState.hash -> {
                            // Modified file
                            changes.add(FileChange(
                                path = filePath,
                                pluginId = extractPluginId(file.name),
                                changeType = FileChangeType.MODIFIED,
                                timestamp = currentTimeToLong(),
                                fileSize = currentState.size
                            ))
                            fileStates[filePath] = currentState
                        }
                    }
                }
                
                // Check for deleted files
                val currentPaths = files.map { it.path }.toSet()
                val deletedPaths = fileStates.keys.filter { 
                    it.startsWith(path) && it !in currentPaths 
                }
                
                for (deletedPath in deletedPaths) {
                    changes.add(FileChange(
                        path = deletedPath,
                        pluginId = extractPluginId(deletedPath.substringAfterLast("/")),
                        changeType = FileChangeType.DELETED,
                        timestamp = currentTimeToLong(),
                        fileSize = null
                    ))
                    fileStates.remove(deletedPath)
                }
            }
        }
        
        return changes
    }
    
    override suspend fun computeFileHash(path: String): String {
        return try {
            val file = fileSystem.getDataDirectory().resolve(path)
            if (!file.exists()) return ""
            
            val hashingSink = HashingSink.sha256(blackholeSink())
            val content = file.readBytes()
            hashingSink.buffer().use { sink ->
                sink.write(content)
            }
            hashingSink.hash.hex()
        } catch (e: Exception) {
            ""
        }
    }
    
    override fun startWatching(paths: List<String>, patterns: List<String>) {
        watchPaths = paths
        watchPatterns = patterns
    }
    
    override fun stopWatching() {
        watchPaths = emptyList()
        watchPatterns = emptyList()
    }
    
    private suspend fun getFileState(file: ireader.core.io.VirtualFile): FileState {
        val hash = computeFileHash(file.path)
        val size = try { file.readBytes().size.toLong() } catch (e: Exception) { 0L }
        return FileState(
            path = file.path,
            lastModified = currentTimeToLong(), // VirtualFile doesn't expose lastModified
            size = size,
            hash = hash
        )
    }
    
    private fun matchesPattern(fileName: String, pattern: String): Boolean {
        // Simple glob pattern matching
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
        return fileName.matches(Regex(regex))
    }
    
    private fun extractPluginId(fileName: String): String? {
        // Extract plugin ID from filename (e.g., "com.example.plugin.iplugin" -> "com.example.plugin")
        return fileName
            .removeSuffix(".iplugin")
            .removeSuffix(".jar")
            .removeSuffix(".dex")
            .takeIf { it != fileName }
    }
}
