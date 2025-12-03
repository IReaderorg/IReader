package ireader.data.epub

/**
 * iOS implementation of EPUB ZIP creation
 * 
 * Note: For a full implementation, consider using a KMP ZIP library
 * or implementing using libzip via cinterop
 * 
 * TODO: Implement proper ZIP creation using:
 * - okio-zipfilesystem (if available for iOS)
 * - libzip via cinterop
 * - Minimal ZIP format implementation
 */
actual fun createEpubZip(entries: List<EpubExportServiceImpl.EpubZipEntry>): ByteArray {
    // Placeholder - returns empty array
    // Real implementation should create proper EPUB ZIP structure
    return ByteArray(0)
}
