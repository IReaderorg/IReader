package ireader.core.http.cloudflare

import java.io.File

/**
 * Desktop implementation of FlareSolverrAutoStarter.
 * Manages the FlareSolverr process lifecycle on desktop platforms.
 */
class DesktopFlareSolverrAutoStarter : FlareSolverrAutoStarter {
    
    private var serverProcess: Process? = null
    
    companion object {
        private val PLATFORM_INFO = mapOf(
            "windows-x64" to "flaresolverr.exe",
            "linux-x64" to "flaresolverr",
            "macos-x64" to "flaresolverr",
            "macos-arm64" to "flaresolverr"
        )
        
        private fun detectPlatform(): String {
            val os = System.getProperty("os.name").lowercase()
            val arch = System.getProperty("os.arch").lowercase()
            return when {
                os.contains("win") -> "windows-x64"
                os.contains("mac") -> if (arch.contains("aarch64") || arch.contains("arm")) "macos-arm64" else "macos-x64"
                else -> "linux-x64"
            }
        }
        
        private fun getPluginDataDir(): File {
            val os = System.getProperty("os.name").lowercase()
            val baseDir = when {
                os.contains("win") -> File(System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home"), "IReader")
                os.contains("mac") -> File(System.getProperty("user.home"), "Library/Application Support/IReader")
                else -> File(System.getProperty("user.home"), ".local/share/IReader")
            }
            return File(baseDir, "plugins/flaresolverr")
        }
    }
    
    private val platform = detectPlatform()
    private val executableName = PLATFORM_INFO[platform] ?: "flaresolverr"
    
    override fun isDownloaded(): Boolean {
        val executable = findExecutable()
        return executable != null
    }
    
    override fun startServer(): Boolean {
        if (serverProcess?.isAlive == true) {
            return true
        }
        
        val executable = findExecutable()
        if (executable == null) {
            return false
        }
        
        return try {
            val pb = ProcessBuilder(executable.absolutePath)
                .directory(executable.parentFile)
                .redirectErrorStream(true)
            
            pb.environment().apply {
                put("PORT", "8191")
                put("HOST", "0.0.0.0")
                put("LOG_LEVEL", "info")
                put("HEADLESS", "true")
            }
            
            serverProcess = pb.start()
            
            // Start a thread to consume output (prevents buffer overflow)
            Thread {
                serverProcess?.inputStream?.bufferedReader()?.forEachLine { line ->
                    // FlareSolverr output - only print errors
                    if (line.contains("ERROR", ignoreCase = true)) {
                        println("[FlareSolverr] $line")
                    }
                }
            }.start()
            
            // Add shutdown hook to clean up
            Runtime.getRuntime().addShutdownHook(Thread {
                stopServer()
            })
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun isProcessRunning(): Boolean {
        return serverProcess?.isAlive == true
    }
    
    /**
     * Stop the FlareSolverr server.
     */
    fun stopServer() {
        serverProcess?.let { process ->
            if (process.isAlive) {
                process.destroy()
                Thread.sleep(2000)
                if (process.isAlive) {
                    process.destroyForcibly()
                }
            }
        }
        serverProcess = null
    }
    
    /**
     * Find the FlareSolverr executable.
     */
    private fun findExecutable(): File? {
        val baseDir = File(getPluginDataDir(), "native/$platform/flaresolverr")
        
        if (!baseDir.exists()) {
            return null
        }
        
        // Direct path
        val directExe = File(baseDir, executableName)
        if (directExe.exists() && directExe.isFile) {
            return directExe
        }
        
        // Search in common nested paths
        val searchPaths = listOf(
            File(baseDir, "flaresolverr/$executableName"),
            File(baseDir, "flaresolverr/flaresolverr/$executableName"),
            File(baseDir, "FlareSolverr/$executableName"),
            File(baseDir, "FlareSolverr/FlareSolverr/$executableName")
        )
        
        for (path in searchPaths) {
            if (path.exists() && path.isFile) {
                return path
            }
        }
        
        // Fallback: recursive search
        return try {
            baseDir.walkTopDown()
                .maxDepth(4)
                .filter { it.isFile && it.name.equals(executableName, ignoreCase = true) }
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
