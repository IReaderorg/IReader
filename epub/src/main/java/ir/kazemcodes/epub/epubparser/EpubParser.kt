package ir.kazemcodes.epub.epubparser

import ir.kazemcodes.epub.epubvalidator.ValidationListeners
import ir.kazemcodes.epub.epubvalidator.ValidationListenersHelper
import ir.kazemcodes.epub.internal.cover.EpubCoverHandler
import ir.kazemcodes.epub.internal.decompressor.EpubDecompressor
import ir.kazemcodes.epub.internal.di.ParserModuleProvider
import ir.kazemcodes.epub.internal.document.OpfDocumentHandler
import ir.kazemcodes.epub.internal.document.toc.TocDocumentHandler
import ir.kazemcodes.epub.internal.parser.EpubManifestParser
import ir.kazemcodes.epub.internal.parser.EpubMetadataParser
import ir.kazemcodes.epub.internal.parser.EpubSpineParser
import ir.kazemcodes.epub.internal.parser.toc.TableOfContentsParserFactory
import ir.kazemcodes.epub.model.EpubBook
import java.io.InputStream

/**
 * Main .epub parser class. Allows to input publication path and parse it into model.
 */
class EpubParser() {

    private val decompressor: EpubDecompressor by lazy {
        ParserModuleProvider.epubDecompressor
    }
    private val opfDocumentHandler: OpfDocumentHandler by lazy {
        ParserModuleProvider.opfDocumentHandler
    }
    private val tocDocumentHandler: TocDocumentHandler by lazy {
        ParserModuleProvider.tocDocumentHandler
    }
    private val metadataParser: EpubMetadataParser by lazy {
        ParserModuleProvider.epubMetadataParser
    }
    private val manifestParser: EpubManifestParser by lazy {
        ParserModuleProvider.epubManifestParser
    }
    private val spineParser: EpubSpineParser by lazy {
        ParserModuleProvider.epubSpineParser
    }
    private val tocParserFactory: TableOfContentsParserFactory by lazy {
        ParserModuleProvider.epubTableOfContentsParserFactory
    }
    private val epubCoverHandler: EpubCoverHandler by lazy {
        ParserModuleProvider.epubCoverHandler
    }
    private var validationListeners: ValidationListeners? = null

    /**
     * Function allowing to parse .epub publication into model
     *
     * @param inputPath Path of .epub publication for parsing
     * @param decompressPath Path to which .epub publication will be decompressed
     * @return Parsed .epub publication model
     */
    fun parse(inputStream: InputStream): EpubBook {

        val entries = decompressor.decompress(inputStream)

        val zipEntries = entries.values.map { it.first }
        val mainOpfDocument = opfDocumentHandler.createOpfDocument(entries, zipEntries)


        val epubOpfFilePath = opfDocumentHandler.getOpfFullFilePath(entries, zipEntries)

        val epubManifestModel = manifestParser.parse(
            mainOpfDocument,
            validationListeners,
            entries
        )

        val epubMetadataModel = metadataParser.parse(
            mainOpfDocument,
            validationListeners
        )
        val tocDocument = tocDocumentHandler.createTocDocument(
            mainOpfDocument,
            zipEntries,
            epubManifestModel,
            entries,
            epubMetadataModel.getEpubSpecificationMajorVersion()
        )

        val epubTocFilePath = tocDocumentHandler.getTocFullFilePath(
            mainOpfDocument,
            zipEntries,
            epubManifestModel,
            epubMetadataModel.getEpubSpecificationMajorVersion()
        )

        return EpubBook(
            epubOpfFilePath,
            epubTocFilePath,
            epubCoverHandler.getCoverImageFromManifest(epubManifestModel),
            epubMetadataModel,
            epubManifestModel,
            spineParser.parse(mainOpfDocument, validationListeners),
            tocParserFactory.getTableOfContentsParser(
                epubMetadataModel.getEpubSpecificationMajorVersion()
            )
                .parse(tocDocument, validationListeners, entries)
        )
    }

    /**
     * Setter method that calls the lambda expressions passed in the parameter
     *
     * @param init lambda expressions to call
     */
    fun setValidationListeners(init: ValidationListenersHelper.() -> Unit) {
        val validationListener = ValidationListenersHelper()
        validationListener.init()
        this.validationListeners = validationListener
    }



}