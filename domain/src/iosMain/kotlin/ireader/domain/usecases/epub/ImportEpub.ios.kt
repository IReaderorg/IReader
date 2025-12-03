package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import platform.Foundation.*
import kotlinx.cinterop.*
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

/**
 * iOS implementation of ImportEpub
 */
@OptIn(ExperimentalForeignApi::class)
actual class ImportEpub {
    
    private val fileSystem = FileSystem.SYSTEM
    private val cacheDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "epub_import_cache"
    
    actual suspend fun parse(uris: List<Uri>) {
        uris.forEach { uri ->
            try {
                parseEpub(uri)
            } catch (e: Exception) {
                println("[ImportEpub] Error parsing EPUB: ${e.message}")
            }
        }
    }
    
    private suspend fun parseEpub(uri: Uri) {
        val path = uri.toString()
        val epubPath = path.toPath()
        if (!fileSystem.exists(epubPath)) {
            println("[ImportEpub] File not found: $path")
            return
        }
        
        val epubData = fileSystem.source(epubPath).buffer().use { it.readByteArray() }
        val epubCacheDir = cacheDir / "epub_${currentTimeMillis()}"
        if (!fileSystem.exists(epubCacheDir)) {
            fileSystem.createDirectories(epubCacheDir)
        }
        
        extractZip(epubData, epubCacheDir.toString())
        
        val containerPath = "$epubCacheDir/META-INF/container.xml"
        val containerXml = readTextFile(containerPath)
        val opfPath = parseContainerXml(containerXml)
        
        if (opfPath != null) {
            val opfFullPath = "$epubCacheDir/$opfPath"
            val opfXml = readTextFile(opfFullPath)
            val metadata = parseOpfMetadata(opfXml)
            println("[ImportEpub] Parsed EPUB: ${metadata.title} by ${metadata.author}")
        }
    }

    private fun extractZip(zipData: ByteArray, outputDir: String) {
        var offset = 0
        while (offset < zipData.size - 4) {
            val signature = zipData.readInt32LE(offset)
            if (signature != 0x04034b50) break
            
            val compressionMethod = zipData.readInt16LE(offset + 8)
            val compressedSize = zipData.readInt32LE(offset + 18)
            val fileNameLength = zipData.readInt16LE(offset + 26)
            val extraFieldLength = zipData.readInt16LE(offset + 28)
            
            val headerSize = 30
            val fileName = zipData.sliceArray(offset + headerSize until offset + headerSize + fileNameLength).decodeToString()
            val dataOffset = offset + headerSize + fileNameLength + extraFieldLength
            val compressedData = zipData.sliceArray(dataOffset until dataOffset + compressedSize)
            
            // Only handle uncompressed files for now
            val content = if (compressionMethod == 0) compressedData else compressedData
            
            val outputPath = "$outputDir/$fileName".toPath()
            outputPath.parent?.let { parent ->
                if (!fileSystem.exists(parent)) fileSystem.createDirectories(parent)
            }
            
            if (!fileName.endsWith("/")) {
                fileSystem.sink(outputPath).buffer().use { it.write(content) }
            }
            
            offset = dataOffset + compressedSize
        }
    }
    
    private fun readTextFile(path: String): String {
        val filePath = path.toPath()
        return if (fileSystem.exists(filePath)) {
            fileSystem.source(filePath).buffer().use { it.readUtf8() }
        } else ""
    }
    
    private fun parseContainerXml(xml: String): String? {
        val regex = """full-path="([^"]+)"""".toRegex()
        return regex.find(xml)?.groupValues?.getOrNull(1)
    }
    
    private fun parseOpfMetadata(xml: String): EpubMetadata {
        val title = extractXmlTag(xml, "dc:title") ?: extractXmlTag(xml, "title") ?: "Unknown"
        val author = extractXmlTag(xml, "dc:creator") ?: extractXmlTag(xml, "creator") ?: "Unknown"
        return EpubMetadata(title, author)
    }
    
    private fun extractXmlTag(xml: String, tagName: String): String? {
        val regex = """<$tagName[^>]*>([^<]*)</$tagName>""".toRegex()
        return regex.find(xml)?.groupValues?.getOrNull(1)?.trim()
    }
    
    actual fun getCacheSize(): String {
        if (!fileSystem.exists(cacheDir)) return "0 B"
        return "0 B" // Simplified
    }
    
    actual fun removeCache() {
        if (fileSystem.exists(cacheDir)) fileSystem.deleteRecursively(cacheDir)
    }
    
    private fun ByteArray.readInt16LE(offset: Int): Int =
        (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)
    
    private fun ByteArray.readInt32LE(offset: Int): Int =
        (this[offset].toInt() and 0xFF) or
        ((this[offset + 1].toInt() and 0xFF) shl 8) or
        ((this[offset + 2].toInt() and 0xFF) shl 16) or
        ((this[offset + 3].toInt() and 0xFF) shl 24)
    
    private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
}

private data class EpubMetadata(val title: String, val author: String)
