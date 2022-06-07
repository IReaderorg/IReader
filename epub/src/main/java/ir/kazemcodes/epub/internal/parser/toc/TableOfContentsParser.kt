package ir.kazemcodes.epub.internal.parser.toc

import ir.kazemcodes.epub.epubvalidator.ValidationListeners
import ir.kazemcodes.epub.internal.constants.EpubConstants.EPUB_MAJOR_VERSION_3
import ir.kazemcodes.epub.model.EpubTableOfContentsModel
import org.w3c.dom.Document
import java.util.zip.ZipEntry

internal interface TableOfContentsParser {
    fun parse(
        tocDocument: Document?,
        validationListeners: ValidationListeners?,
        zipFile:Map<String, Pair<ZipEntry, ByteArray>>
    ): EpubTableOfContentsModel
}

internal class TableOfContentsParserFactory {

    fun getTableOfContentsParser(epuSpecMajorVersion: Int?): TableOfContentsParser {
        return if (epuSpecMajorVersion == EPUB_MAJOR_VERSION_3) {
            Epub3TableOfContentsParser()
        } else {
            Epub2TableOfContentsParser()
        }
    }
}