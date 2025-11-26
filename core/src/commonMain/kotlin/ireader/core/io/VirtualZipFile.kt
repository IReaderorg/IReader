package ireader.core.io

/**
 * Platform-agnostic ZIP file abstraction.
 */
interface VirtualZipFile {
    /**
     * Lists all entries in the ZIP file.
     */
    suspend fun entries(): List<ZipEntry>
    
    /**
     * Gets a specific entry by name.
     */
    suspend fun getEntry(name: String): ZipEntry?
    
    /**
     * Reads the content of an entry.
     */
    suspend fun readEntry(entry: ZipEntry): ByteArray
    
    /**
     * Reads the content of an entry as text.
     */
    suspend fun readEntryAsText(entry: ZipEntry): String
    
    /**
     * Extracts an entry to a file.
     */
    suspend fun extractEntry(entry: ZipEntry, target: VirtualFile)
    
    /**
     * Extracts all entries to a directory.
     */
    suspend fun extractAll(targetDir: VirtualFile)
    
    /**
     * Closes the ZIP file.
     */
    suspend fun close()
}

/**
 * Represents an entry in a ZIP file.
 */
data class ZipEntry(
    val name: String,
    val size: Long,
    val compressedSize: Long,
    val isDirectory: Boolean,
    val comment: String? = null
)

/**
 * Builder for creating ZIP files.
 */
interface VirtualZipBuilder {
    /**
     * Adds a file to the ZIP.
     */
    suspend fun addFile(file: VirtualFile, entryName: String? = null)
    
    /**
     * Adds a directory recursively to the ZIP.
     */
    suspend fun addDirectory(directory: VirtualFile, basePath: String = "")
    
    /**
     * Adds bytes as an entry.
     */
    suspend fun addEntry(name: String, data: ByteArray)
    
    /**
     * Adds text as an entry.
     */
    suspend fun addTextEntry(name: String, text: String)
    
    /**
     * Builds the ZIP file.
     */
    suspend fun build(output: VirtualFile)
    
    /**
     * Closes the builder.
     */
    suspend fun close()
}

/**
 * Opens a ZIP file for reading.
 */
suspend fun VirtualFile.openZip(): VirtualZipFile {
    return createZipFile(this)
}

/**
 * Creates a new ZIP file builder.
 */
fun createZipBuilder(): VirtualZipBuilder {
    return createZipBuilderImpl()
}

/**
 * Platform-specific factory function for creating VirtualZipFile.
 * Implemented in platform-specific source sets.
 */
internal expect suspend fun createZipFile(file: VirtualFile): VirtualZipFile

/**
 * Platform-specific factory function for creating VirtualZipBuilder.
 * Implemented in platform-specific source sets.
 */
internal expect fun createZipBuilderImpl(): VirtualZipBuilder
