package ireader.core.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry as JavaZipEntry

/**
 * JVM implementation of VirtualZipFile using java.util.zip.ZipFile.
 */
internal class JvmZipFile(private val zipFile: ZipFile) : VirtualZipFile {
    
    override suspend fun entries(): List<ZipEntry> = withContext(Dispatchers.IO) {
        zipFile.entries().toList().map { it.toZipEntry() }
    }
    
    override suspend fun getEntry(name: String): ZipEntry? = withContext(Dispatchers.IO) {
        zipFile.getEntry(name)?.toZipEntry()
    }
    
    override suspend fun readEntry(entry: ZipEntry): ByteArray = withContext(Dispatchers.IO) {
        val javaEntry = zipFile.getEntry(entry.name)
            ?: throw IllegalArgumentException("Entry not found: ${entry.name}")
        zipFile.getInputStream(javaEntry).use { it.readBytes() }
    }
    
    override suspend fun readEntryAsText(entry: ZipEntry): String = withContext(Dispatchers.IO) {
        readEntry(entry).decodeToString()
    }
    
    override suspend fun extractEntry(entry: ZipEntry, target: VirtualFile) {
        val data = readEntry(entry)
        target.writeBytes(data)
    }
    
    override suspend fun extractAll(targetDir: VirtualFile) = withContext(Dispatchers.IO) {
        targetDir.mkdirs()
        
        for (entry in entries()) {
            val targetFile = targetDir.resolve(entry.name)
            
            if (entry.isDirectory) {
                targetFile.mkdirs()
            } else {
                targetFile.parent?.mkdirs()
                extractEntry(entry, targetFile)
            }
        }
    }
    
    override suspend fun close() = withContext(Dispatchers.IO) {
        zipFile.close()
    }
    
    private fun JavaZipEntry.toZipEntry(): ZipEntry {
        return ZipEntry(
            name = name,
            size = size,
            compressedSize = compressedSize,
            isDirectory = isDirectory,
            comment = comment
        )
    }
}

/**
 * JVM implementation of VirtualZipBuilder using java.util.zip.ZipOutputStream.
 */
internal class JvmZipBuilder : VirtualZipBuilder {
    private val entries = mutableListOf<Pair<String, ByteArray>>()
    
    override suspend fun addFile(file: VirtualFile, entryName: String?) {
        val name = entryName ?: file.name
        val data = file.readBytes()
        entries.add(name to data)
    }
    
    override suspend fun addDirectory(directory: VirtualFile, basePath: String) {
        directory.walk().collect { file ->
            if (file.isFile()) {
                val relativePath = file.relativeTo(directory)
                val entryName = if (basePath.isEmpty()) relativePath else "$basePath/$relativePath"
                addFile(file, entryName)
            }
        }
    }
    
    override suspend fun addEntry(name: String, data: ByteArray) {
        entries.add(name to data)
    }
    
    override suspend fun addTextEntry(name: String, text: String) {
        addEntry(name, text.encodeToByteArray())
    }
    
    override suspend fun build(output: VirtualFile) = withContext(Dispatchers.IO) {
        require(output is JvmVirtualFile) { "Output must be a JvmVirtualFile" }
        
        output.parent?.mkdirs()
        
        ZipOutputStream(BufferedOutputStream(FileOutputStream(output.file))).use { zip ->
            for ((name, data) in entries) {
                val entry = JavaZipEntry(name)
                entry.size = data.size.toLong()
                zip.putNextEntry(entry)
                zip.write(data)
                zip.closeEntry()
            }
        }
    }
    
    override suspend fun close() {
        entries.clear()
    }
}

/**
 * JVM implementation of createZipFile.
 */
internal actual suspend fun createZipFile(file: VirtualFile): VirtualZipFile = 
    withContext(Dispatchers.IO) {
        require(file is JvmVirtualFile) { "File must be a JvmVirtualFile" }
        JvmZipFile(ZipFile(file.file))
    }

/**
 * JVM implementation of createZipBuilderImpl.
 */
internal actual fun createZipBuilderImpl(): VirtualZipBuilder = JvmZipBuilder()
