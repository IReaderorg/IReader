package ireader.domain.services.platform

import ireader.domain.models.common.Uri
import ireader.domain.services.common.ServiceResult
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of FileSystemService using Swing JFileChooser
 */
class DesktopFileSystemService : FileSystemService {
    
    private var running = false
    
    override suspend fun initialize() {
        // No initialization needed
    }
    
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    
    override suspend fun cleanup() {
        // No cleanup needed
    }
    
    override suspend fun pickFile(
        initialDirectory: String?,
        fileTypes: List<String>,
        title: String?
    ): ServiceResult<Uri> {
        return try {
            val fileChooser = JFileChooser().apply {
                dialogTitle = title ?: "Select File"
                fileSelectionMode = JFileChooser.FILES_ONLY
                
                if (initialDirectory != null) {
                    currentDirectory = File(initialDirectory)
                }
                
                if (fileTypes.isNotEmpty()) {
                    val description = fileTypes.joinToString(", ") { ".$it" }
                    fileFilter = FileNameExtensionFilter(description, *fileTypes.toTypedArray())
                }
            }
            
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                ServiceResult.Success(Uri.parse(file.absolutePath))
            } else {
                ServiceResult.Error("File selection cancelled")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to pick file: ${e.message}")
        }
    }
    
    override suspend fun pickMultipleFiles(
        initialDirectory: String?,
        fileTypes: List<String>,
        title: String?
    ): ServiceResult<List<Uri>> {
        return try {
            val fileChooser = JFileChooser().apply {
                dialogTitle = title ?: "Select Files"
                fileSelectionMode = JFileChooser.FILES_ONLY
                isMultiSelectionEnabled = true
                
                if (initialDirectory != null) {
                    currentDirectory = File(initialDirectory)
                }
                
                if (fileTypes.isNotEmpty()) {
                    val description = fileTypes.joinToString(", ") { ".$it" }
                    fileFilter = FileNameExtensionFilter(description, *fileTypes.toTypedArray())
                }
            }
            
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val files = fileChooser.selectedFiles.map { Uri.parse(it.absolutePath) }
                ServiceResult.Success(files)
            } else {
                ServiceResult.Error("File selection cancelled")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to pick files: ${e.message}")
        }
    }
    
    override suspend fun pickDirectory(
        initialDirectory: String?,
        title: String?
    ): ServiceResult<Uri> {
        return try {
            val fileChooser = JFileChooser().apply {
                dialogTitle = title ?: "Select Directory"
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                
                if (initialDirectory != null) {
                    currentDirectory = File(initialDirectory)
                }
            }
            
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val directory = fileChooser.selectedFile
                ServiceResult.Success(Uri.parse(directory.absolutePath))
            } else {
                ServiceResult.Error("Directory selection cancelled")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to pick directory: ${e.message}")
        }
    }
    
    override suspend fun saveFile(
        defaultFileName: String,
        fileExtension: String,
        initialDirectory: String?,
        title: String?
    ): ServiceResult<Uri> {
        return try {
            val fileChooser = JFileChooser().apply {
                dialogTitle = title ?: "Save File"
                fileSelectionMode = JFileChooser.FILES_ONLY
                selectedFile = File(defaultFileName)
                
                if (initialDirectory != null) {
                    currentDirectory = File(initialDirectory)
                }
                
                fileFilter = FileNameExtensionFilter(".$fileExtension", fileExtension)
            }
            
            val result = fileChooser.showSaveDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                var file = fileChooser.selectedFile
                // Add extension if not present
                if (!file.name.endsWith(".$fileExtension")) {
                    file = File(file.absolutePath + ".$fileExtension")
                }
                ServiceResult.Success(Uri.parse(file.absolutePath))
            } else {
                ServiceResult.Error("Save cancelled")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to save file: ${e.message}")
        }
    }
    
    override suspend fun fileExists(uri: Uri): Boolean {
        return try {
            File(uri.path).exists()
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getFileSize(uri: Uri): Long? {
        return try {
            val file = File(uri.path)
            if (file.exists()) file.length() else null
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun deleteFile(uri: Uri): ServiceResult<Unit> {
        return try {
            val file = File(uri.path)
            if (file.delete()) {
                ServiceResult.Success(Unit)
            } else {
                ServiceResult.Error("Failed to delete file")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Error deleting file: ${e.message}")
        }
    }
    
    override suspend fun readFileBytes(uri: Uri): ServiceResult<ByteArray> {
        return try {
            val bytes = File(uri.path).readBytes()
            ServiceResult.Success(bytes)
        } catch (e: Exception) {
            ServiceResult.Error("Error reading file: ${e.message}")
        }
    }
    
    override suspend fun writeFileBytes(uri: Uri, bytes: ByteArray): ServiceResult<Unit> {
        return try {
            File(uri.path).writeBytes(bytes)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Error writing file: ${e.message}")
        }
    }
}
