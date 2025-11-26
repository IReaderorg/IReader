package ireader.core.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Desktop implementation of FileSystem using java.io.File
 */
class DesktopFileSystem : FileSystem {
    override fun getFile(path: String): VirtualFile {
        return DesktopVirtualFile(File(path))
    }
    
    override fun getDataDirectory(): VirtualFile {
        val appDataDir = File(System.getProperty("user.home"), ".ireader/data")
        appDataDir.mkdirs()
        return DesktopVirtualFile(appDataDir)
    }
    
    override fun getCacheDirectory(): VirtualFile {
        val cacheDir = File(System.getProperty("user.home"), ".ireader/cache")
        cacheDir.mkdirs()
        return DesktopVirtualFile(cacheDir)
    }
    
    override fun getTempDirectory(): VirtualFile {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "ireader")
        tempDir.mkdirs()
        return DesktopVirtualFile(tempDir)
    }
    
    override suspend fun createTempFile(prefix: String, suffix: String): VirtualFile {
        return withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile(prefix, suffix, getTempDirectory().toJavaFile())
            DesktopVirtualFile(tempFile)
        }
    }
}

/**
 * Desktop implementation of VirtualFile wrapping java.io.File
 */
class DesktopVirtualFile(private val file: File) : VirtualFile {
    override val path: String get() = file.absolutePath
    override val name: String get() = file.name
    override val parent: VirtualFile? get() = file.parentFile?.let { DesktopVirtualFile(it) }
    override val extension: String get() = file.extension
    
    override suspend fun exists(): Boolean = withContext(Dispatchers.IO) {
        file.exists()
    }
    
    override suspend fun isDirectory(): Boolean = withContext(Dispatchers.IO) {
        file.isDirectory
    }
    
    override suspend fun isFile(): Boolean = withContext(Dispatchers.IO) {
        file.isFile
    }
    
    override suspend fun size(): Long = withContext(Dispatchers.IO) {
        file.length()
    }
    
    override suspend fun lastModified(): Long = withContext(Dispatchers.IO) {
        file.lastModified()
    }
    
    override suspend fun mkdirs(): Boolean = withContext(Dispatchers.IO) {
        file.mkdirs()
    }
    
    override suspend fun createNewFile(): Boolean = withContext(Dispatchers.IO) {
        file.createNewFile()
    }
    
    override suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        file.delete()
    }
    
    override suspend fun deleteRecursively(): Boolean = withContext(Dispatchers.IO) {
        file.deleteRecursively()
    }
    
    override suspend fun listFiles(): List<VirtualFile> = withContext(Dispatchers.IO) {
        file.listFiles()?.map { DesktopVirtualFile(it) } ?: emptyList()
    }
    
    override suspend fun listFiles(filter: (VirtualFile) -> Boolean): List<VirtualFile> = withContext(Dispatchers.IO) {
        file.listFiles()?.map { DesktopVirtualFile(it) }?.filter(filter) ?: emptyList()
    }
    
    override suspend fun readBytes(): ByteArray = withContext(Dispatchers.IO) {
        file.readBytes()
    }
    
    override suspend fun readText(): String = withContext(Dispatchers.IO) {
        file.readText()
    }
    
    override suspend fun writeBytes(bytes: ByteArray) = withContext(Dispatchers.IO) {
        file.writeBytes(bytes)
    }
    
    override suspend fun writeText(text: String) = withContext(Dispatchers.IO) {
        file.writeText(text)
    }
    
    override suspend fun appendBytes(bytes: ByteArray) = withContext(Dispatchers.IO) {
        file.appendBytes(bytes)
    }
    
    override suspend fun appendText(text: String) = withContext(Dispatchers.IO) {
        file.appendText(text)
    }
    
    override suspend fun copyTo(target: VirtualFile, overwrite: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            file.copyTo((target as DesktopVirtualFile).file, overwrite)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun moveTo(target: VirtualFile): Boolean = withContext(Dispatchers.IO) {
        file.renameTo((target as DesktopVirtualFile).file)
    }
    
    override fun resolve(relativePath: String): VirtualFile {
        return DesktopVirtualFile(File(file, relativePath))
    }
    
    override suspend fun inputStream(): VirtualInputStream {
        return DesktopVirtualInputStream(file.inputStream())
    }
    
    override suspend fun outputStream(append: Boolean): VirtualOutputStream {
        return DesktopVirtualOutputStream(file.outputStream())
    }
    
    override fun walk(): Flow<VirtualFile> = flow {
        file.walk().forEach { emit(DesktopVirtualFile(it)) }
    }
    
    fun toJavaFile(): File = file
}

class DesktopVirtualInputStream(private val stream: java.io.InputStream) : VirtualInputStream {
    override suspend fun read(): Int = withContext(Dispatchers.IO) {
        stream.read()
    }
    
    override suspend fun read(buffer: ByteArray): Int = withContext(Dispatchers.IO) {
        stream.read(buffer)
    }
    
    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int = withContext(Dispatchers.IO) {
        stream.read(buffer, offset, length)
    }
    
    override suspend fun close() = withContext(Dispatchers.IO) {
        stream.close()
    }
}

class DesktopVirtualOutputStream(private val stream: java.io.OutputStream) : VirtualOutputStream {
    override suspend fun write(byte: Int) = withContext(Dispatchers.IO) {
        stream.write(byte)
    }
    
    override suspend fun write(buffer: ByteArray) = withContext(Dispatchers.IO) {
        stream.write(buffer)
    }
    
    override suspend fun write(buffer: ByteArray, offset: Int, length: Int) = withContext(Dispatchers.IO) {
        stream.write(buffer, offset, length)
    }
    
    override suspend fun flush() = withContext(Dispatchers.IO) {
        stream.flush()
    }
    
    override suspend fun close() = withContext(Dispatchers.IO) {
        stream.close()
    }
}
