package ireader.core.io

/**
 * iOS implementation of VirtualZipFile functions
 * TODO: Implement using libzip or a KMP ZIP library
 */

internal actual suspend fun createZipFile(file: VirtualFile): VirtualZipFile {
    // TODO: Implement proper ZIP reading on iOS
    return object : VirtualZipFile {
        override suspend fun entries(): List<ZipEntry> = emptyList()
        override suspend fun getEntry(name: String): ZipEntry? = null
        override suspend fun readEntry(entry: ZipEntry): ByteArray = ByteArray(0)
        override suspend fun readEntryAsText(entry: ZipEntry): String = ""
        override suspend fun extractEntry(entry: ZipEntry, target: VirtualFile) {}
        override suspend fun extractAll(targetDir: VirtualFile) {}
        override suspend fun close() {}
    }
}

internal actual fun createZipBuilderImpl(): VirtualZipBuilder {
    return object : VirtualZipBuilder {
        private val entries = mutableListOf<Pair<String, ByteArray>>()
        
        override suspend fun addFile(file: VirtualFile, entryName: String?) {
            val name = entryName ?: file.name
            val content = file.readBytes()
            entries.add(name to content)
        }
        
        override suspend fun addDirectory(directory: VirtualFile, basePath: String) {
            // TODO: Implement directory traversal
        }
        
        override suspend fun addEntry(name: String, data: ByteArray) {
            entries.add(name to data)
        }
        
        override suspend fun addTextEntry(name: String, text: String) {
            entries.add(name to text.encodeToByteArray())
        }
        
        override suspend fun build(output: VirtualFile) {
            // TODO: Implement proper ZIP creation
            // For now, just write a placeholder
        }
        
        override suspend fun close() {
            entries.clear()
        }
    }
}
