package ireader.domain.services.common

import android.content.Context
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android implementation of FileService
 */
class AndroidFileService(
    private val context: Context
) : FileService {
    
    private val baseDir: File = context.filesDir
    
    override suspend fun initialize() {}
    override suspend fun start() {}
    override suspend fun stop() {}
    override fun isRunning(): Boolean = true
    override suspend fun cleanup() {}
    
    override suspend fun writeText(path: String, content: String): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(baseDir, path)
                file.parentFile?.mkdirs()
                file.writeText(content)
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to write text: ${e.message}", e)
            }
        }
    }
    
    override suspend fun writeBytes(path: String, content: ByteArray): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(baseDir, path)
                file.parentFile?.mkdirs()
                file.writeBytes(content)
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to write bytes: ${e.message}", e)
            }
        }
    }
    
    override suspend fun readText(path: String): ServiceResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(baseDir, path)
                if (!file.exists()) {
                    return@withContext ServiceResult.Error("File not found: $path")
                }
                ServiceResult.Success(file.readText())
            } catch (e: Exception) {
                ServiceResult.Error("Failed to read text: ${e.message}", e)
            }
        }
    }
    
    override suspend fun readBytes(path: String): ServiceResult<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(baseDir, path)
                if (!file.exists()) {
                    return@withContext ServiceResult.Error("File not found: $path")
                }
                ServiceResult.Success(file.readBytes())
            } catch (e: Exception) {
                ServiceResult.Error("Failed to read bytes: ${e.message}", e)
            }
        }
    }
    
    override suspend fun deleteFile(path: String): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(baseDir, path)
                if (file.exists()) {
                    file.delete()
                }
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to delete file: ${e.message}", e)
            }
        }
    }
    
    override suspend fun fileExists(path: String): Boolean {
        return withContext(Dispatchers.IO) {
            File(baseDir, path).exists()
        }
    }
    
    override suspend fun getFileSize(path: String): ServiceResult<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(baseDir, path)
                if (!file.exists()) {
                    return@withContext ServiceResult.Error("File not found: $path")
                }
                ServiceResult.Success(file.length())
            } catch (e: Exception) {
                ServiceResult.Error("Failed to get file size: ${e.message}", e)
            }
        }
    }
    
    override suspend fun createDirectory(path: String): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(baseDir, path)
                dir.mkdirs()
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to create directory: ${e.message}", e)
            }
        }
    }
    
    override suspend fun listFiles(path: String): ServiceResult<List<FileInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(baseDir, path)
                if (!dir.exists() || !dir.isDirectory) {
                    return@withContext ServiceResult.Error("Directory not found: $path")
                }
                
                val files = dir.listFiles()?.map { file ->
                    FileInfo(
                        name = file.name,
                        path = file.absolutePath,
                        size = file.length(),
                        isDirectory = file.isDirectory,
                        lastModified = file.lastModified()
                    )
                } ?: emptyList()
                
                ServiceResult.Success(files)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to list files: ${e.message}", e)
            }
        }
    }
    
    override suspend fun copyFile(source: String, destination: String): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val sourceFile = File(baseDir, source)
                val destFile = File(baseDir, destination)
                
                if (!sourceFile.exists()) {
                    return@withContext ServiceResult.Error("Source file not found: $source")
                }
                
                destFile.parentFile?.mkdirs()
                sourceFile.copyTo(destFile, overwrite = true)
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to copy file: ${e.message}", e)
            }
        }
    }
    
    override suspend fun moveFile(source: String, destination: String): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val sourceFile = File(baseDir, source)
                val destFile = File(baseDir, destination)
                
                if (!sourceFile.exists()) {
                    return@withContext ServiceResult.Error("Source file not found: $source")
                }
                
                destFile.parentFile?.mkdirs()
                sourceFile.renameTo(destFile)
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to move file: ${e.message}", e)
            }
        }
    }
    
    override suspend fun getAvailableSpace(): ServiceResult<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val stat = StatFs(baseDir.absolutePath)
                val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
                ServiceResult.Success(availableBytes)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to get available space: ${e.message}", e)
            }
        }
    }
}
