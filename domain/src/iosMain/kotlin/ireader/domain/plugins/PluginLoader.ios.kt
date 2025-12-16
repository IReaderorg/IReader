package ireader.domain.plugins

import ireader.core.io.VirtualFile
import platform.Foundation.*
import kotlinx.cinterop.*

/**
 * iOS implementation of ZIP extraction for plugins
 * 
 * Uses manual ZIP parsing with pure Kotlin DEFLATE decompression.
 * 
 * ## Supported Compression Methods
 * - STORED (0): Uncompressed data
 * - DEFLATE (8): Standard ZIP compression using pure Kotlin implementation
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun extractZipEntry(file: VirtualFile, entryName: String): String? {
    try {
        // Read the ZIP file content
        val zipData = file.readBytes() ?: return null
        
        // Parse ZIP and find the entry
        val entry = findZipEntry(zipData, entryName) ?: return null
        
        // Decompress if needed
        val content = when (entry.compressionMethod) {
            0 -> entry.data // STORED - no compression
            8 -> inflateDeflate(entry.data, entry.uncompressedSize) // DEFLATE
            else -> {
                println("[PluginLoader] Unsupported compression method: ${entry.compressionMethod}")
                return null
            }
        }
        
        return content?.decodeToString()
    } catch (e: Exception) {
        println("[PluginLoader] Error extracting ZIP entry: ${e.message}")
        return null
    }
}

/**
 * iOS implementation of plugin instantiation
 * Not supported on iOS - use JS plugins instead
 */
actual fun instantiatePlugin(pluginClass: Any): Plugin {
    throw UnsupportedOperationException("Native plugin instantiation is not supported on iOS. Use JS plugins instead.")
}

/**
 * iOS implementation of ZIP entry listing for debugging
 */
actual suspend fun listZipEntries(file: VirtualFile): List<String> {
    return try {
        val zipData = file.readBytes() ?: return emptyList()
        val entries = mutableListOf<String>()
        var offset = 0
        
        while (offset < zipData.size - 4) {
            val signature = zipData.readInt32LE(offset)
            if (signature != 0x04034b50) break
            
            val fileNameLength = zipData.readInt16LE(offset + 26)
            val extraFieldLength = zipData.readInt16LE(offset + 28)
            val compressedSize = zipData.readInt32LE(offset + 18)
            
            val headerSize = 30
            val fileName = zipData.sliceArray(offset + headerSize until offset + headerSize + fileNameLength)
                .decodeToString()
            
            entries.add(fileName)
            offset = offset + headerSize + fileNameLength + extraFieldLength + compressedSize
        }
        
        entries
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * iOS implementation of file download
 * Uses NSURLSession for downloading plugin files
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun downloadFile(url: String, destination: okio.Path) {
    val nsUrl = NSURL.URLWithString(url) 
        ?: throw Exception("Invalid URL: $url")
    
    val request = NSMutableURLRequest.requestWithURL(nsUrl)
    request.setHTTPMethod("GET")
    request.setTimeoutInterval(30.0)
    
    val session = NSURLSession.sharedSession
    
    // Use synchronous download for simplicity
    var downloadError: NSError? = null
    var responseData: NSData? = null
    
    val semaphore = platform.darwin.dispatch_semaphore_create(0)
    
    session.dataTaskWithRequest(request) { data, response, error ->
        responseData = data
        downloadError = error
        platform.darwin.dispatch_semaphore_signal(semaphore)
    }.resume()
    
    platform.darwin.dispatch_semaphore_wait(semaphore, platform.darwin.DISPATCH_TIME_FOREVER)
    
    downloadError?.let { throw Exception("Download failed: ${it.localizedDescription}") }
    
    val data = responseData ?: throw Exception("No data received")
    
    // Write to file
    val destUrl = NSURL.fileURLWithPath(destination.toString())
    val success = data.writeToURL(destUrl, true)
    
    if (!success) {
        throw Exception("Failed to write file to $destination")
    }
}

/**
 * Data class for ZIP entry information
 */
private data class ZipEntry(
    val name: String,
    val compressionMethod: Int,
    val compressedSize: Int,
    val uncompressedSize: Int,
    val data: ByteArray
)

/**
 * Find and extract a specific entry from a ZIP file
 */
private fun findZipEntry(zipData: ByteArray, entryName: String): ZipEntry? {
    var offset = 0
    
    while (offset < zipData.size - 4) {
        // Check for local file header signature
        val signature = zipData.readInt32LE(offset)
        if (signature != 0x04034b50) {
            break
        }
        
        // Parse local file header
        val compressionMethod = zipData.readInt16LE(offset + 8)
        val compressedSize = zipData.readInt32LE(offset + 18)
        val uncompressedSize = zipData.readInt32LE(offset + 22)
        val fileNameLength = zipData.readInt16LE(offset + 26)
        val extraFieldLength = zipData.readInt16LE(offset + 28)
        
        val headerSize = 30
        val fileName = zipData.sliceArray(offset + headerSize until offset + headerSize + fileNameLength)
            .decodeToString()
        
        val dataOffset = offset + headerSize + fileNameLength + extraFieldLength
        
        if (fileName == entryName) {
            val data = zipData.sliceArray(dataOffset until dataOffset + compressedSize)
            return ZipEntry(
                name = fileName,
                compressionMethod = compressionMethod,
                compressedSize = compressedSize,
                uncompressedSize = uncompressedSize,
                data = data
            )
        }
        
        // Move to next entry
        offset = dataOffset + compressedSize
    }
    
    return null
}

/**
 * Inflate DEFLATE compressed data using pure Kotlin implementation
 * 
 * @param compressedData The DEFLATE compressed data (raw, no zlib header)
 * @param expectedSize The expected uncompressed size from ZIP header
 * @return Decompressed data or null on failure
 */
private fun inflateDeflate(compressedData: ByteArray, expectedSize: Int): ByteArray? {
    if (compressedData.isEmpty()) return ByteArray(0)
    if (expectedSize <= 0) return null
    
    return try {
        val inflater = DeflateInflater(compressedData)
        inflater.inflate(expectedSize)
    } catch (e: Exception) {
        println("[PluginLoader] DEFLATE decompression failed: ${e.message}")
        null
    }
}

private fun ByteArray.readInt16LE(offset: Int): Int {
    return (this[offset].toInt() and 0xFF) or
           ((this[offset + 1].toInt() and 0xFF) shl 8)
}

private fun ByteArray.readInt32LE(offset: Int): Int {
    return (this[offset].toInt() and 0xFF) or
           ((this[offset + 1].toInt() and 0xFF) shl 8) or
           ((this[offset + 2].toInt() and 0xFF) shl 16) or
           ((this[offset + 3].toInt() and 0xFF) shl 24)
}

/**
 * Pure Kotlin DEFLATE inflater implementation
 * 
 * Implements RFC 1951 DEFLATE decompression algorithm.
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
        bytePos += 4
        
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
                symbol == 256 -> return
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
        
        val codeLengthOrder = intArrayOf(16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15)
        val codeLengthLengths = IntArray(19)
        for (i in 0 until hclen) {
            codeLengthLengths[codeLengthOrder[i]] = readBits(3)
        }
        
        val codeLengthTree = buildHuffmanTree(codeLengthLengths)
        
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
        var code = 0
        var bits = 0
        
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
        
        val codes = mutableMapOf<Int, Pair<Int, Int>>()
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
