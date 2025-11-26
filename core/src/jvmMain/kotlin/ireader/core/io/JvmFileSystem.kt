package ireader.core.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * JVM implementation of FileSystem using java.io.File.
 */
class JvmFileSystem(
    private val dataDir: File,
    private val cacheDir: File,
    private val tempDir: File = File(System.getProperty("java.io.tmpdir"))
) : FileSystem {
    
    override fun getFile(path: String): VirtualFile = JvmVirtualFile(File(path))
    
    override fun getDataDirectory(): VirtualFile = JvmVirtualFile(dataDir)
    
    override fun getCacheDirectory(): VirtualFile = JvmVirtualFile(cacheDir)
    
    override fun getTempDirectory(): VirtualFile = JvmVirtualFile(tempDir)
    
    override suspend fun createTempFile(prefix: String, suffix: String): VirtualFile = 
        withContext(Dispatchers.IO) {
            JvmVirtualFile(File.createTempFile(prefix, suffix, tempDir))
        }
}

/**
 * JVM implementation of VirtualFile wrapping java.io.File.
 */
class JvmVirtualFile(val file: File) : VirtualFile {
    
    override val path: String get() = file.absolutePath
    override val name: String get() = file.name
    override val parent: VirtualFile? get() = file.parentFile?.let { JvmVirtualFile(it) }
    override val extension: String get() = file.extension
    
    override suspend fun exists(): Boolean = withContext(Dispatchers.IO) { file.exists() }
    
    override suspend fun isDirectory(): Boolean = withContext(Dispatchers.IO) { file.isDirectory }
    
    override suspend fun isFile(): Boolean = withContext(Dispatchers.IO) { file.isFile }
    
    override suspend fun size(): Long = withContext(Dispatchers.IO) { file.length() }
    
    override suspend fun lastModified(): Long = withContext(Dispatchers.IO) { file.lastModified() }
    
    override suspend fun mkdirs(): Boolean = withContext(Dispatchers.IO) { file.mkdirs() }
    
    override suspend fun createNewFile(): Boolean = withContext(Dispatchers.IO) { file.createNewFile() }
    
    override suspend fun delete(): Boolean = withContext(Dispatchers.IO) { file.delete() }
    
    override suspend fun deleteRecursively(): Boolean = withContext(Dispatchers.IO) { file.deleteRecursively() }
    
    override suspend fun listFiles(): List<VirtualFile> = withContext(Dispatchers.IO) {
        file.listFiles()?.map { JvmVirtualFile(it) } ?: emptyList()
    }
    
    override suspend fun listFiles(filter: (VirtualFile) -> Boolean): List<VirtualFile> = 
        withContext(Dispatchers.IO) {
            file.listFiles()?.map { JvmVirtualFile(it) }?.filter(filter) ?: emptyList()
        }
    
    override suspend fun readBytes(): ByteArray = withContext(Dispatchers.IO) { file.readBytes() }
    
    override suspend fun readText(): String = withContext(Dispatchers.IO) { file.readText() }
    
    override suspend fun writeBytes(bytes: ByteArray) = withContext(Dispatchers.IO) { file.writeBytes(bytes) }
    
    override suspend fun writeText(text: String) = withContext(Dispatchers.IO) { file.writeText(text) }
    
    override suspend fun appendBytes(bytes: ByteArray) = withContext(Dispatchers.IO) { file.appendBytes(bytes) }
    
    override suspend fun appendText(text: String) = withContext(Dispatchers.IO) { file.appendText(text) }
    
    override suspend fun copyTo(target: VirtualFile, overwrite: Boolean): Boolean = 
        withContext(Dispatchers.IO) {
            require(target is JvmVirtualFile) { "Target must be a JvmVirtualFile" }
            file.copyTo(target.file, overwrite)
            true
        }
    
    override suspend fun moveTo(target: VirtualFile): Boolean = withContext(Dispatchers.IO) {
        require(target is JvmVirtualFile) { "Target must be a JvmVirtualFile" }
        file.renameTo(target.file)
    }
    
    override fun resolve(relativePath: String): VirtualFile = JvmVirtualFile(file.resolve(relativePath))
    
    override suspend fun inputStream(): VirtualInputStream = 
        withContext(Dispatchers.IO) {
            JvmVirtualInputStream(FileInputStream(file))
        }
    
    override suspend fun outputStream(append: Boolean): VirtualOutputStream = 
        withContext(Dispatchers.IO) {
            JvmVirtualOutputStream(FileOutputStream(file, append))
        }
    
    override fun walk(): Flow<VirtualFile> = flow {
        file.walk().forEach { emit(JvmVirtualFile(it)) }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JvmVirtualFile) return false
        return file == other.file
    }
    
    override fun hashCode(): Int = file.hashCode()
    
    override fun toString(): String = "JvmVirtualFile($path)"
}

class JvmVirtualInputStream(private val stream: InputStream) : VirtualInputStream {
    override suspend fun read(): Int = withContext(Dispatchers.IO) { stream.read() }
    
    override suspend fun read(buffer: ByteArray): Int = withContext(Dispatchers.IO) { stream.read(buffer) }
    
    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int = 
        withContext(Dispatchers.IO) { stream.read(buffer, offset, length) }
    
    override suspend fun close() = withContext(Dispatchers.IO) { stream.close() }
}

class JvmVirtualOutputStream(private val stream: OutputStream) : VirtualOutputStream {
    override suspend fun write(byte: Int) = withContext(Dispatchers.IO) { stream.write(byte) }
    
    override suspend fun write(buffer: ByteArray) = withContext(Dispatchers.IO) { stream.write(buffer) }
    
    override suspend fun write(buffer: ByteArray, offset: Int, length: Int) = 
        withContext(Dispatchers.IO) { stream.write(buffer, offset, length) }
    
    override suspend fun flush() = withContext(Dispatchers.IO) { stream.flush() }
    
    override suspend fun close() = withContext(Dispatchers.IO) { stream.close() }
}
