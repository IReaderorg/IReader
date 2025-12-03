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
 * 
 * Parses EPUB files by:
 * 1. Extracting the ZIP archive (supports STORED entries, DEFLATE logged as warning)
 * 2. Reading container.xml to find the OPF file
 * 3. Parsing OPF metadata (title, author, etc.)
 * 
 * ## Supported Compression Methods
 * - STORED (0): Uncompressed data - fully supported
 * - DEFLATE (8): Logged as warning, returns raw data (most EPUB metadata files are small/uncompressed)
 * 
 * ## Note
 * For full DEFLATE support, the data module's EpubExportServicePlatform uses the Compression framework.
 * This implementation focuses on reading EPUB metadata which is typically stored uncompressed.
 * 
 * ## Usage
 * ```kotlin
 * val importer = ImportEpub()
 * importer.parse(listOf(epubUri))
 * ```
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
        
        val extractedCount = extractZip(epubData, epubCacheDir.toString())
        println("[ImportEpub] Extracted $extractedCount files")
        
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

    /**
     * Extract ZIP archive
     * 
     * Supports STORED (uncompressed) entries fully.
     * DEFLATE entries are handled with a pure Kotlin implementation.
     * 
     * @return Number of files extracted
     */
    private fun extractZip(zipData: ByteArray, outputDir: String): Int {
        var offset = 0
        var extractedCount = 0
        
        while (offset < zipData.size - 4) {
            val signature = zipData.readInt32LE(offset)
            if (signature != 0x04034b50) break
            
            val compressionMethod = zipData.readInt16LE(offset + 8)
            val compressedSize = zipData.readInt32LE(offset + 18)
            val uncompressedSize = zipData.readInt32LE(offset + 22)
            val fileNameLength = zipData.readInt16LE(offset + 26)
            val extraFieldLength = zipData.readInt16LE(offset + 28)
            
            val headerSize = 30
            val fileName = zipData.sliceArray(offset + headerSize until offset + headerSize + fileNameLength).decodeToString()
            val dataOffset = offset + headerSize + fileNameLength + extraFieldLength
            val compressedData = zipData.sliceArray(dataOffset until dataOffset + compressedSize)
            
            // Decompress based on compression method
            val content: ByteArray? = when (compressionMethod) {
                0 -> compressedData // STORED - no compression
                8 -> {
                    // DEFLATE - use pure Kotlin implementation
                    inflateDeflate(compressedData, uncompressedSize)
                }
                else -> {
                    println("[ImportEpub] Unsupported compression method: $compressionMethod for $fileName")
                    null
                }
            }
            
            if (content != null && !fileName.endsWith("/")) {
                val outputPath = "$outputDir/$fileName".toPath()
                outputPath.parent?.let { parent ->
                    if (!fileSystem.exists(parent)) fileSystem.createDirectories(parent)
                }
                
                fileSystem.sink(outputPath).buffer().use { it.write(content) }
                extractedCount++
            }
            
            offset = dataOffset + compressedSize
        }
        
        return extractedCount
    }
    
    /**
     * Inflate DEFLATE compressed data using pure Kotlin implementation
     * 
     * This is a simplified DEFLATE decompressor that handles the most common cases.
     * For complex compressed data, consider using the data module's Compression framework.
     * 
     * @param compressedData The DEFLATE compressed data (raw, no zlib header)
     * @param expectedSize The expected uncompressed size
     * @return Decompressed data or null on failure
     */
    private fun inflateDeflate(compressedData: ByteArray, expectedSize: Int): ByteArray? {
        if (compressedData.isEmpty()) return ByteArray(0)
        if (expectedSize <= 0) return null
        
        return try {
            val inflater = DeflateInflater(compressedData)
            inflater.inflate(expectedSize)
        } catch (e: Exception) {
            println("[ImportEpub] DEFLATE decompression failed: ${e.message}")
            // Return null - the file won't be extracted but parsing can continue
            null
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

/**
 * Pure Kotlin DEFLATE inflater implementation
 * 
 * Implements RFC 1951 DEFLATE decompression algorithm.
 * Handles:
 * - Non-compressed blocks (BTYPE=00)
 * - Fixed Huffman codes (BTYPE=01)
 * - Dynamic Huffman codes (BTYPE=10)
 */
private class DeflateInflater(private val input: ByteArray) {
    private var bitPos = 0
    private var bytePos = 0
    private val output = mutableListOf<Byte>()
    
    fun inflate(expectedSize: Int): ByteArray {
        var isFinal = false
        
        while (!isFinal && bytePos < input.size) {
            isFinal = readBits(1) == 1
            val blockType = readBits(2)
            
            when (blockType) {
                0 -> inflateStored()
                1 -> inflateFixed()
                2 -> inflateDynamic()
                else -> throw IllegalStateException("Invalid block type: $blockType")
            }
            
            // Safety check to prevent infinite loops
            if (output.size > expectedSize * 2) {
                throw IllegalStateException("Output exceeds expected size")
            }
        }
        
        return output.toByteArray()
    }
    
    private fun readBits(count: Int): Int {
        var result = 0
        for (i in 0 until count) {
            if (bytePos >= input.size) return result
            val bit = (input[bytePos].toInt() shr bitPos) and 1
            result = result or (bit shl i)
            bitPos++
            if (bitPos == 8) {
                bitPos = 0
                bytePos++
            }
        }
        return result
    }
    
    private fun alignToByte() {
        if (bitPos != 0) {
            bitPos = 0
            bytePos++
        }
    }
    
    private fun inflateStored() {
        alignToByte()
        if (bytePos + 4 > input.size) return
        
        val len = (input[bytePos].toInt() and 0xFF) or ((input[bytePos + 1].toInt() and 0xFF) shl 8)
        bytePos += 4 // Skip LEN and NLEN
        
        for (i in 0 until len) {
            if (bytePos >= input.size) break
            output.add(input[bytePos++])
        }
    }
    
    private fun inflateFixed() {
        while (true) {
            val symbol = decodeFixedLitLen()
            when {
                symbol < 256 -> output.add(symbol.toByte())
                symbol == 256 -> return // End of block
                else -> {
                    val length = decodeLength(symbol)
                    val distCode = decodeFixedDist()
                    val distance = decodeDistance(distCode)
                    copyFromOutput(distance, length)
                }
            }
        }
    }
    
    private fun inflateDynamic() {
        val hlit = readBits(5) + 257
        val hdist = readBits(5) + 1
        val hclen = readBits(4) + 4
        
        // Read code length code lengths
        val codeLengthOrder = intArrayOf(16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15)
        val codeLengthLengths = IntArray(19)
        for (i in 0 until hclen) {
            codeLengthLengths[codeLengthOrder[i]] = readBits(3)
        }
        
        val codeLengthTree = buildHuffmanTree(codeLengthLengths)
        
        // Read literal/length and distance code lengths
        val allLengths = IntArray(hlit + hdist)
        var i = 0
        while (i < allLengths.size) {
            val symbol = decodeHuffman(codeLengthTree)
            when {
                symbol < 16 -> allLengths[i++] = symbol
                symbol == 16 -> {
                    val repeat = readBits(2) + 3
                    val value = if (i > 0) allLengths[i - 1] else 0
                    repeat(repeat) { if (i < allLengths.size) allLengths[i++] = value }
                }
                symbol == 17 -> {
                    val repeat = readBits(3) + 3
                    repeat(repeat) { if (i < allLengths.size) allLengths[i++] = 0 }
                }
                symbol == 18 -> {
                    val repeat = readBits(7) + 11
                    repeat(repeat) { if (i < allLengths.size) allLengths[i++] = 0 }
                }
            }
        }
        
        val litLenTree = buildHuffmanTree(allLengths.sliceArray(0 until hlit))
        val distTree = buildHuffmanTree(allLengths.sliceArray(hlit until allLengths.size))
        
        // Decode data
        while (true) {
            val symbol = decodeHuffman(litLenTree)
            when {
                symbol < 256 -> output.add(symbol.toByte())
                symbol == 256 -> return
                else -> {
                    val length = decodeLength(symbol)
                    val distCode = decodeHuffman(distTree)
                    val distance = decodeDistance(distCode)
                    copyFromOutput(distance, length)
                }
            }
        }
    }
    
    private fun decodeFixedLitLen(): Int {
        // Fixed Huffman codes for literal/length
        var code = 0
        var bits = 0
        
        // Read up to 9 bits
        while (bits < 9) {
            code = (code shl 1) or readBits(1)
            bits++
            
            when {
                bits == 7 && code in 0..23 -> return code + 256
                bits == 8 && code in 48..191 -> return code - 48
                bits == 8 && code in 192..199 -> return code - 192 + 280
                bits == 9 && code in 400..511 -> return code - 400 + 144
            }
        }
        return 0
    }
    
    private fun decodeFixedDist(): Int {
        return readBits(5).let { code ->
            // Reverse 5 bits
            var reversed = 0
            for (i in 0 until 5) {
                reversed = (reversed shl 1) or ((code shr i) and 1)
            }
            reversed
        }
    }
    
    private fun decodeLength(symbol: Int): Int {
        val baseLengths = intArrayOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258)
        val extraBits = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0)
        val index = symbol - 257
        if (index < 0 || index >= baseLengths.size) return 3
        return baseLengths[index] + readBits(extraBits[index])
    }
    
    private fun decodeDistance(code: Int): Int {
        val baseDistances = intArrayOf(1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577)
        val extraBits = intArrayOf(0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13)
        if (code < 0 || code >= baseDistances.size) return 1
        return baseDistances[code] + readBits(extraBits[code])
    }
    
    private fun copyFromOutput(distance: Int, length: Int) {
        val start = output.size - distance
        for (i in 0 until length) {
            val index = start + (i % distance)
            if (index >= 0 && index < output.size) {
                output.add(output[index])
            }
        }
    }
    
    private fun buildHuffmanTree(lengths: IntArray): HuffmanTree {
        val maxLen = lengths.maxOrNull() ?: 0
        if (maxLen == 0) return HuffmanTree(emptyMap())
        
        val blCount = IntArray(maxLen + 1)
        lengths.forEach { if (it > 0) blCount[it]++ }
        
        val nextCode = IntArray(maxLen + 1)
        var code = 0
        for (bits in 1..maxLen) {
            code = (code + blCount[bits - 1]) shl 1
            nextCode[bits] = code
        }
        
        val codes = mutableMapOf<Int, Pair<Int, Int>>() // code -> (symbol, bits)
        for (symbol in lengths.indices) {
            val len = lengths[symbol]
            if (len > 0) {
                codes[nextCode[len]] = symbol to len
                nextCode[len]++
            }
        }
        
        return HuffmanTree(codes)
    }
    
    private fun decodeHuffman(tree: HuffmanTree): Int {
        var code = 0
        var bits = 0
        while (bits < 16) {
            code = (code shl 1) or readBits(1)
            bits++
            tree.codes.entries.find { it.value.second == bits && it.key == code }?.let {
                return it.value.first
            }
        }
        return 0
    }
    
    private class HuffmanTree(val codes: Map<Int, Pair<Int, Int>>)
}
