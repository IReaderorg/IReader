package ir.kazemcodes.epub.internal.document

import ir.kazemcodes.epub.internal.di.ParserModuleProvider
import ir.kazemcodes.epub.internal.extensions.getFirstElementByTag
import org.w3c.dom.Document
import java.util.zip.ZipEntry
import javax.xml.parsers.DocumentBuilder

internal class OpfDocumentHandler {

    private val documentBuilder: DocumentBuilder by lazy { ParserModuleProvider.documentBuilder }

    fun createOpfDocument(
        zipFile: Map<String, Pair<ZipEntry, ByteArray>>,
        entries: List<ZipEntry>
    ): Document {
        val opfFileHref = getOpfFileHref(zipFile, entries)
        return parseFileAsDocument(zipFile, entries, opfFileHref)
    }

    fun getOpfFullFilePath(
        zipFile: Map<String, Pair<ZipEntry, ByteArray>>,
        entries: List<ZipEntry>
    ): String? {
        val opfFileHref = getOpfFileHref(zipFile, entries)
        return entries.firstOrNull { it.name.endsWith(opfFileHref) }?.name
    }

    private fun getOpfFileHref(
        zipFile: Map<String, Pair<ZipEntry, ByteArray>>,
        entries: List<ZipEntry>
    ): String {
        val containerDocument = parseFileAsDocument(zipFile, entries, CONTAINER_HREF)
        return getOpfFileHref(containerDocument)
    }

    private fun getOpfFileHref(container: Document): String {

        val rootFiles = container.getFirstElementByTag(MAIN_CONTAINER_ROOT_FILES_TAG)
        val rootFile = rootFiles?.getFirstElementByTag(MAIN_CONTAINER_ROOT_FILE_TAG)

        return rootFile?.getAttribute(MAIN_CONTAINER_FULL_PATH_ATTRIBUTE).let {
            if (it.isNullOrEmpty()) DEFAULT_OPF_DOCUMENT_HREF else it
        }
    }

    private fun parseFileAsDocument(
        zipFile: Map<String, Pair<ZipEntry, ByteArray>>,
        entries: List<ZipEntry>,
        href: String,
    ): Document {
        return entries
            .filter { it.name.endsWith(href) }
            .map {
                val result = documentBuilder.parse(zipFile[it.name]!!.second.inputStream())

                result
            }
            .firstOrNull()
            ?: documentBuilder.newDocument()
    }

    companion object {
        const val MAIN_CONTAINER_FULL_PATH_ATTRIBUTE = "full-path"
        const val DEFAULT_OPF_DOCUMENT_HREF = "OEBPS/content.opf"
        const val CONTAINER_HREF = "META-INF/container.xml"
        const val MAIN_CONTAINER_ROOT_FILES_TAG = "rootfiles"
        const val MAIN_CONTAINER_ROOT_FILE_TAG = "rootfile"
    }
}