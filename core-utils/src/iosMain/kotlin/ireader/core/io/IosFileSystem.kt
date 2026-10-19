package ireader.core.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL

class IosFileSystem : ireader.core.io.FileSystem {
    private val fs = FileSystem.SYSTEM

    override fun getFile(path: String): VirtualFile = IosVirtualFile(path.toPath(), fs)

    override fun getDataDirectory(): VirtualFile {
        val dir = directory(NSDocumentDirectory).resolve("ireader")
        fs.createDirectories(dir)
        return IosVirtualFile(dir, fs)
    }

    override fun getCacheDirectory(): VirtualFile {
        val dir = directory(NSCachesDirectory).resolve("ireader")
        fs.createDirectories(dir)
        return IosVirtualFile(dir, fs)
    }

    override fun getTempDirectory(): VirtualFile {
        val dir = NSTemporaryDirectory().toPath().resolve("ireader")
        fs.createDirectories(dir)
        return IosVirtualFile(dir, fs)
    }

    override suspend fun createTempFile(prefix: String, suffix: String): VirtualFile {
        return withContext(Dispatchers.Default) {
            val file = getTempDirectory().resolve("$prefix-${kotlin.random.Random.nextLong()}$suffix")
            fs.write(file.path.toPath()) {}
            file
        }
    }

    private fun directory(type: ULong): Path {
        val urls = NSFileManager.defaultManager.URLsForDirectory(type, NSUserDomainMask)
        val path = (urls.firstOrNull() as? NSURL)?.path ?: NSTemporaryDirectory()
        return path.toPath()
    }
}

class IosVirtualFile(
    private val okioPath: Path,
    private val fs: FileSystem,
) : VirtualFile {
    override val path: String get() = okioPath.toString()
    override val name: String get() = okioPath.name
    override val parent: VirtualFile? get() = okioPath.parent?.let { IosVirtualFile(it, fs) }
    override val extension: String get() = name.substringAfterLast('.', "")

    override suspend fun exists(): Boolean = withContext(Dispatchers.Default) { fs.exists(okioPath) }

    override suspend fun isDirectory(): Boolean = withContext(Dispatchers.Default) {
        fs.metadataOrNull(okioPath)?.isDirectory == true
    }

    override suspend fun isFile(): Boolean = withContext(Dispatchers.Default) {
        fs.metadataOrNull(okioPath)?.isRegularFile == true
    }

    override suspend fun size(): Long = withContext(Dispatchers.Default) {
        fs.metadataOrNull(okioPath)?.size ?: 0L
    }

    override suspend fun lastModified(): Long = withContext(Dispatchers.Default) {
        fs.metadataOrNull(okioPath)?.lastModifiedAtMillis ?: 0L
    }

    override suspend fun mkdirs(): Boolean = withContext(Dispatchers.Default) {
        fs.createDirectories(okioPath)
        true
    }

    override suspend fun createNewFile(): Boolean = withContext(Dispatchers.Default) {
        if (fs.exists(okioPath)) return@withContext false
        okioPath.parent?.let { fs.createDirectories(it) }
        fs.write(okioPath) {}
        true
    }

    override suspend fun delete(): Boolean = withContext(Dispatchers.Default) {
        runCatching { fs.delete(okioPath) }.isSuccess
    }

    override suspend fun deleteRecursively(): Boolean = withContext(Dispatchers.Default) {
        runCatching { fs.deleteRecursively(okioPath) }.isSuccess
    }

    override suspend fun listFiles(): List<VirtualFile> = withContext(Dispatchers.Default) {
        runCatching { fs.list(okioPath).map { IosVirtualFile(it, fs) } }.getOrDefault(emptyList())
    }

    override suspend fun listFiles(filter: (VirtualFile) -> Boolean): List<VirtualFile> =
        listFiles().filter(filter)

    override suspend fun readBytes(): ByteArray = withContext(Dispatchers.Default) {
        fs.read(okioPath) { readByteArray() }
    }

    override suspend fun readText(): String = withContext(Dispatchers.Default) {
        fs.read(okioPath) { readUtf8() }
    }

    override suspend fun writeBytes(bytes: ByteArray) {
        withContext(Dispatchers.Default) {
            okioPath.parent?.let { fs.createDirectories(it) }
            fs.write(okioPath) { write(bytes) }
        }
    }

    override suspend fun writeText(text: String) {
        writeBytes(text.encodeToByteArray())
    }

    override suspend fun appendBytes(bytes: ByteArray) {
        withContext(Dispatchers.Default) {
            okioPath.parent?.let { fs.createDirectories(it) }
            fs.appendingSink(okioPath).buffer().use { it.write(bytes) }
        }
    }

    override suspend fun appendText(text: String) = appendBytes(text.encodeToByteArray())

    override suspend fun copyTo(target: VirtualFile, overwrite: Boolean): Boolean =
        withContext(Dispatchers.Default) {
            val destination = target.path.toPath()
            if (!overwrite && fs.exists(destination)) return@withContext false
            runCatching { fs.copy(okioPath, destination) }.isSuccess
        }

    override suspend fun moveTo(target: VirtualFile): Boolean = withContext(Dispatchers.Default) {
        runCatching { fs.atomicMove(okioPath, target.path.toPath()) }.isSuccess
    }

    override fun resolve(relativePath: String): VirtualFile =
        IosVirtualFile(okioPath.resolve(relativePath), fs)

    override suspend fun inputStream(): VirtualInputStream = withContext(Dispatchers.Default) {
        IosVirtualInputStream(fs.source(okioPath).buffer())
    }

    override suspend fun outputStream(append: Boolean): VirtualOutputStream =
        withContext(Dispatchers.Default) {
            okioPath.parent?.let { fs.createDirectories(it) }
            val sink = if (append) fs.appendingSink(okioPath) else fs.sink(okioPath)
            IosVirtualOutputStream(sink.buffer())
        }

    override fun walk(): Flow<VirtualFile> = flow {
        emit(this@IosVirtualFile)
        if (isDirectory()) {
            listFiles().forEach { child ->
                child.walk().collect { emit(it) }
            }
        }
    }
}

private class IosVirtualInputStream(
    private val source: okio.BufferedSource,
) : VirtualInputStream {
    override suspend fun read(): Int = withContext(Dispatchers.Default) {
        if (source.exhausted()) -1 else source.readByte().toInt() and 0xff
    }

    override suspend fun read(buffer: ByteArray): Int = read(buffer, 0, buffer.size)

    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int =
        withContext(Dispatchers.Default) {
            source.read(buffer, offset, length)
        }

    override suspend fun close() = withContext(Dispatchers.Default) { source.close() }
}

private class IosVirtualOutputStream(
    private val sink: okio.BufferedSink,
) : VirtualOutputStream {
    override suspend fun write(byte: Int) {
        withContext(Dispatchers.Default) {
            sink.writeByte(byte)
        }
    }

    override suspend fun write(buffer: ByteArray) {
        withContext(Dispatchers.Default) {
            sink.write(buffer)
        }
    }

    override suspend fun write(buffer: ByteArray, offset: Int, length: Int) {
        withContext(Dispatchers.Default) {
            sink.write(buffer, offset, length)
        }
    }

    override suspend fun flush() = withContext(Dispatchers.Default) { sink.flush() }

    override suspend fun close() = withContext(Dispatchers.Default) { sink.close() }
}
