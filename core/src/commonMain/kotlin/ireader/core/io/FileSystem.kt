package ireader.core.io

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic file system abstraction.
 * Replaces direct usage of java.io.File in commonMain code.
 */
interface FileSystem {
    /**
     * Creates a file reference at the given path.
     */
    fun getFile(path: String): VirtualFile
    
    /**
     * Gets the application's data directory.
     */
    fun getDataDirectory(): VirtualFile
    
    /**
     * Gets the application's cache directory.
     */
    fun getCacheDirectory(): VirtualFile
    
    /**
     * Gets the temporary directory.
     */
    fun getTempDirectory(): VirtualFile
    
    /**
     * Creates a temporary file with optional prefix and suffix.
     */
    suspend fun createTempFile(prefix: String = "tmp", suffix: String = ""): VirtualFile
}

/**
 * Platform-agnostic file abstraction.
 */
interface VirtualFile {
    val path: String
    val name: String
    val parent: VirtualFile?
    val extension: String
    
    /**
     * Checks if the file exists.
     */
    suspend fun exists(): Boolean
    
    /**
     * Checks if this is a directory.
     */
    suspend fun isDirectory(): Boolean
    
    /**
     * Checks if this is a regular file.
     */
    suspend fun isFile(): Boolean
    
    /**
     * Gets the file size in bytes.
     */
    suspend fun size(): Long
    
    /**
     * Gets the last modified timestamp.
     */
    suspend fun lastModified(): Long
    
    /**
     * Creates the directory (and parent directories if needed).
     */
    suspend fun mkdirs(): Boolean
    
    /**
     * Creates a new empty file.
     */
    suspend fun createNewFile(): Boolean
    
    /**
     * Deletes the file or directory.
     */
    suspend fun delete(): Boolean
    
    /**
     * Deletes the directory recursively.
     */
    suspend fun deleteRecursively(): Boolean
    
    /**
     * Lists files in the directory.
     */
    suspend fun listFiles(): List<VirtualFile>
    
    /**
     * Lists files matching a filter.
     */
    suspend fun listFiles(filter: (VirtualFile) -> Boolean): List<VirtualFile>
    
    /**
     * Reads the file content as bytes.
     */
    suspend fun readBytes(): ByteArray
    
    /**
     * Reads the file content as text.
     */
    suspend fun readText(): String
    
    /**
     * Writes bytes to the file.
     */
    suspend fun writeBytes(bytes: ByteArray)
    
    /**
     * Writes text to the file.
     */
    suspend fun writeText(text: String)
    
    /**
     * Appends bytes to the file.
     */
    suspend fun appendBytes(bytes: ByteArray)
    
    /**
     * Appends text to the file.
     */
    suspend fun appendText(text: String)
    
    /**
     * Copies this file to the target location.
     */
    suspend fun copyTo(target: VirtualFile, overwrite: Boolean = false): Boolean
    
    /**
     * Moves this file to the target location.
     */
    suspend fun moveTo(target: VirtualFile): Boolean
    
    /**
     * Gets a child file/directory.
     */
    fun resolve(relativePath: String): VirtualFile
    
    /**
     * Opens an input stream for reading.
     */
    suspend fun inputStream(): VirtualInputStream
    
    /**
     * Opens an output stream for writing.
     */
    suspend fun outputStream(append: Boolean = false): VirtualOutputStream
    
    /**
     * Walks the file tree.
     */
    fun walk(): Flow<VirtualFile>
}

/**
 * Platform-agnostic input stream.
 */
interface VirtualInputStream {
    suspend fun read(): Int
    suspend fun read(buffer: ByteArray): Int
    suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int
    suspend fun close()
}

/**
 * Platform-agnostic output stream.
 */
interface VirtualOutputStream {
    suspend fun write(byte: Int)
    suspend fun write(buffer: ByteArray)
    suspend fun write(buffer: ByteArray, offset: Int, length: Int)
    suspend fun flush()
    suspend fun close()
}
