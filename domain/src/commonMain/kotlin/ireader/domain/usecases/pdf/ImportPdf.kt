package ireader.domain.usecases.pdf

import ireader.domain.models.common.Uri

/**
 * PDF Import Use Case
 * 
 * Imports PDF files into the library as books with chapters.
 * Each page or logical section becomes a chapter with text content
 * that can be read and used with TTS.
 * 
 * Platform-specific implementations handle:
 * - Android: Uses PdfRenderer or iText for text extraction
 * - Desktop: Uses Apache PDFBox for text extraction
 * - iOS: Uses PDFKit for text extraction
 */
expect class ImportPdf {
    /**
     * Parse and import PDF files into the library
     * 
     * @param uris List of PDF file URIs to import
     * @throws Exception if import fails
     */
    suspend fun parse(uris: List<Uri>)
    
    /**
     * Get the current cache size used by PDF imports
     */
    fun getCacheSize(): String
    
    /**
     * Clear the PDF import cache
     */
    fun removeCache()
}
