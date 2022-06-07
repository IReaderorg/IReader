package ir.kazemcodes.epub.internal.document.toc

import ir.kazemcodes.epub.internal.constants.EpubConstants.EPUB_MAJOR_VERSION_3
import ir.kazemcodes.epub.internal.di.ParserModuleProvider
import ir.kazemcodes.epub.model.EpubManifestModel
import org.w3c.dom.Document
import java.util.zip.ZipEntry
import javax.xml.parsers.DocumentBuilder

internal class TocDocumentHandler {

    private val documentBuilder: DocumentBuilder by lazy { ParserModuleProvider.documentBuilder }

    fun createTocDocument(
        mainOpfDocument: Document?,
        epubEntries: List<ZipEntry>,
        epubManifestModel: EpubManifestModel,
        zipFile: Map<String, Pair<ZipEntry, ByteArray>>,
        epubSpecMajorVersion: Int?
    ): Document? {

        val tocLocation = getTocLocation(epubSpecMajorVersion, epubManifestModel, mainOpfDocument)
        val tocFullPath = getTocFullPath(epubEntries, tocLocation)

        return tocLocation?.let {
            documentBuilder.parse(zipFile[tocFullPath]!!.second.inputStream())
        }
    }

    fun getTocFullFilePath(
        mainOpfDocument: Document?,
        epubEntries: List<ZipEntry>,
        epubManifestModel: EpubManifestModel,
        epubSpecMajorVersion: Int?
    ): String? {

        val tocLocation = getTocLocation(epubSpecMajorVersion, epubManifestModel, mainOpfDocument)
        return getTocFullPath(epubEntries, tocLocation)
    }

    private fun getTocLocation(
        epubSpecMajorVersion: Int?,
        epubManifestModel: EpubManifestModel,
        mainOpfDocument: Document?
    ): String? {

        return if (epubSpecMajorVersion == EPUB_MAJOR_VERSION_3) {
            Epub3TocLocationFinder().findNcxLocation(epubManifestModel)
        } else {
            Epub2TocLocationFinder().findNcxLocation(mainOpfDocument, epubManifestModel)
        }
    }

    private fun getTocFullPath(entries: List<ZipEntry>, tocLocation: String?): String? {
        return entries
            .filter {
                tocLocation?.let { toc ->
                    it.name.endsWith(toc)
                } ?: false
            }
            .map { it.name }
            .firstOrNull()
    }
}

