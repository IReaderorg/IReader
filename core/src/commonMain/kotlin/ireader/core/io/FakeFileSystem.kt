package ireader.core.io

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * In-memory fake implementation of FileSystem for testing.
 */
class FakeFileSystem : FileSystem {
    private val files = mutableMapOf<String, FakeFile>()
    private var tempFileCounter = 0
    
    private val dataDir = "/data"
    private val cacheDir = "/cache"
    private val tempDir = "/tmp"
    
    init {
        files[dataDir] = FakeFile(dataDir, isDirectory = true)
        files[cacheDir] = FakeFile(cacheDir, isDirectory = true)
        files[tempDir] = FakeFile(tempDir, isDirectory = true)
    }
    
    override fun getFile(path: String): VirtualFile = FakeVirtualFile(path, this)
    
    override fun getDataDirectory(): VirtualFile = FakeVirtualFile(dataDir, this)
    
    override fun getCacheDirectory(): VirtualFile = FakeVirtualFile(cacheDir, this)
    
    override fun getTempDirectory(): VirtualFile = FakeVirtualFile(tempDir, this)
    
    override suspend fun createTempFile(prefix: String, suffix: String): VirtualFile {
        val path = "$tempDir/${prefix}_${tempFileCounter++}$suffix"
        files[path] = FakeFile(path, isDirectory = false, content = byteArrayOf())
        return FakeVirtualFile(path, this)
    }
    
    internal fun getOrCreate(path: String): FakeFile {
        return files.getOrPut(path) { FakeFile(path) }
    }
    
    internal fun get(path: String): FakeFile? = files[path]
    
    internal fun put(path: String, file: FakeFile) {
        files[path] = file
    }
    
    internal fun remove(path: String) {
        files.remove(path)
    }
    
    internal fun listDirectory(path: String): List<String> {
        val prefix = if (path.endsWith("/")) path else "$path/"
        return files.keys.filter { 
            it.startsWith(prefix) && it.substring(prefix.length).count { c -> c == '/' } == 0
        }
    }
    
    internal fun walkDirectory(path: String): List<String> {
        val prefix = if (path.endsWith("/")) path else "$path/"
        return files.keys.filter { it.startsWith(prefix) }
    }
    
    data class FakeFile(
        val path: String,
        var isDirectory: Boolean = false,
        var content: ByteArray? = null,
        var lastModified: Long = System.currentTimeMillis()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FakeFile) return false
            return path == other.path
        }
        
        override fun hashCode(): Int = path.hashCode()
    }
}

class FakeVirtualFile(
    override val path: String,
    private val fileSystem: FakeFileSystem
) : VirtualFile {
    
    override val name: String
        get() = path.substringAfterLast('/')
    
    override val parent: VirtualFile?
        get() {
            val parentPath = path.substringBeforeLast('/', "")
            return if (parentPath.isEmpty()) null else FakeVirtualFile(parentPath, fileSystem)
        }
    
    override val extension: String
        get() = name.substringAfterLast('.', "")
    
    override suspend fun exists(): Boolean = fileSystem.get(path) != null
    
    override suspend fun isDirectory(): Boolean = fileSystem.get(path)?.isDirectory == true
    
    override suspend fun isFile(): Boolean {
        val file = fileSystem.get(path)
        return file != null && !file.isDirectory
    }
    
    override suspend fun size(): Long = fileSystem.get(path)?.content?.size?.toLong() ?: 0L
    
    override suspend fun lastModified(): Long = fileSystem.get(path)?.lastModified ?: 0L
    
    override suspend fun mkdirs(): Boolean {
        var current = path
        val parts = path.split('/').filter { it.isNotEmpty() }
        var accumulated = ""
        
        for (part in parts) {
            accumulated += "/$part"
            if (fileSystem.get(accumulated) == null) {
                fileSystem.put(accumulated, FakeFileSystem.FakeFile(accumulated, isDirectory = true))
            }
        }
        return true
    }
    
    override suspend fun createNewFile(): Boolean {
        if (exists()) return false
        parent?.mkdirs()
        fileSystem.put(path, FakeFileSystem.FakeFile(path, isDirectory = false, content = byteArrayOf()))
        return true
    }
    
    override suspend fun delete(): Boolean {
        if (!exists()) return false
        fileSystem.remove(path)
        return true
    }
    
    override suspend fun deleteRecursively(): Boolean {
        if (!exists()) return false
        
        if (isDirectory()) {
            fileSystem.walkDirectory(path).forEach { filePath ->
                fileSystem.remove(filePath)
            }
        }
        fileSystem.remove(path)
        return true
    }
    
    override suspend fun listFiles(): List<VirtualFile> {
        if (!isDirectory()) return emptyList()
        return fileSystem.listDirectory(path).map { FakeVirtualFile(it, fileSystem) }
    }
    
    override suspend fun listFiles(filter: (VirtualFile) -> Boolean): List<VirtualFile> {
        return listFiles().filter(filter)
    }
    
    override suspend fun readBytes(): ByteArray {
        return fileSystem.get(path)?.content ?: throw NoSuchFileException(path)
    }
    
    override suspend fun readText(): String {
        return readBytes().decodeToString()
    }
    
    override suspend fun writeBytes(bytes: ByteArray) {
        val file = fileSystem.getOrCreate(path)
        fileSystem.put(path, file.copy(content = bytes, isDirectory = false))
    }
    
    override suspend fun writeText(text: String) {
        writeBytes(text.encodeToByteArray())
    }
    
    override suspend fun appendBytes(bytes: ByteArray) {
        val existing = if (exists()) readBytes() else byteArrayOf()
        writeBytes(existing + bytes)
    }
    
    override suspend fun appendText(text: String) {
        appendBytes(text.encodeToByteArray())
    }
    
    override suspend fun copyTo(target: VirtualFile, overwrite: Boolean): Boolean {
        require(target is FakeVirtualFile) { "Target must be a FakeVirtualFile" }
        
        if (target.exists() && !overwrite) return false
        
        val content = readBytes()
        target.writeBytes(content)
        return true
    }
    
    override suspend fun moveTo(target: VirtualFile): Boolean {
        require(target is FakeVirtualFile) { "Target must be a FakeVirtualFile" }
        
        val content = readBytes()
        target.writeBytes(content)
        delete()
        return true
    }
    
    override fun resolve(relativePath: String): VirtualFile {
        val newPath = if (path.endsWith("/")) {
            "$path$relativePath"
        } else {
            "$path/$relativePath"
        }
        return FakeVirtualFile(newPath, fileSystem)
    }
    
    override suspend fun inputStream(): VirtualInputStream {
        return FakeVirtualInputStream(readBytes())
    }
    
    override suspend fun outputStream(append: Boolean): VirtualOutputStream {
        return FakeVirtualOutputStream(this, append)
    }
    
    override fun walk(): Flow<VirtualFile> = flow {
        emit(this@FakeVirtualFile)
        if (isDirectory()) {
            listFiles().forEach { child ->
                child.walk().collect { emit(it) }
            }
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FakeVirtualFile) return false
        return path == other.path
    }
    
    override fun hashCode(): Int = path.hashCode()
    
    override fun toString(): String = "FakeVirtualFile($path)"
}

class FakeVirtualInputStream(private val data: ByteArray) : VirtualInputStream {
    private var position = 0
    
    override suspend fun read(): Int {
        return if (position < data.size) data[position++].toInt() and 0xFF else -1
    }
    
    override suspend fun read(buffer: ByteArray): Int {
        return read(buffer, 0, buffer.size)
    }
    
    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (position >= data.size) return -1
        
        val available = minOf(length, data.size - position)
        data.copyInto(buffer, offset, position, position + available)
        position += available
        return available
    }
    
    override suspend fun close() {
        // Nothing to close
    }
}

class FakeVirtualOutputStream(
    private val file: FakeVirtualFile,
    private val append: Boolean
) : VirtualOutputStream {
    private val buffer = mutableListOf<Byte>()
    
    init {
        if (append) {
            kotlinx.coroutines.runBlocking {
                if (file.exists()) {
                    buffer.addAll(file.readBytes().toList())
                }
            }
        }
    }
    
    override suspend fun write(byte: Int) {
        buffer.add(byte.toByte())
    }
    
    override suspend fun write(buffer: ByteArray) {
        this.buffer.addAll(buffer.toList())
    }
    
    override suspend fun write(buffer: ByteArray, offset: Int, length: Int) {
        this.buffer.addAll(buffer.slice(offset until offset + length))
    }
    
    override suspend fun flush() {
        file.writeBytes(buffer.toByteArray())
    }
    
    override suspend fun close() {
        flush()
    }
}

class NoSuchFileException(path: String) : Exception("File not found: $path")
