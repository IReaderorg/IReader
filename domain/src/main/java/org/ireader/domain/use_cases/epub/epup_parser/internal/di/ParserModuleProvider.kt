package org.ireader.domain.use_cases.epub.epup_parser.internal.di

import org.ireader.domain.use_cases.epub.epup_parser.internal.document.OpfDocumentHandler
import org.ireader.domain.use_cases.epub.epup_parser.internal.document.toc.TocDocumentHandler
import org.ireader.domain.use_cases.epub.epup_parser.internal.parser.EpubManifestParser
import org.ireader.domain.use_cases.epub.epup_parser.internal.parser.EpubMetadataParser
import org.ireader.domain.use_cases.epub.epup_parser.internal.parser.EpubSpineParser
import org.ireader.domain.use_cases.epub.epup_parser.internal.parser.toc.TableOfContentsParserFactory
import org.ireader.domain.use_cases.epub.epup_parser.internal.cover.EpubCoverHandler
import org.ireader.domain.use_cases.epub.epup_parser.internal.decompressor.EpubDecompressor
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

internal interface ParserModule {
    val epubDecompressor: EpubDecompressor
    val documentBuilder: DocumentBuilder
    val epubSpineParser: EpubSpineParser
    val epubMetadataParser: EpubMetadataParser
    val epubManifestParser: EpubManifestParser
    val epubTableOfContentsParserFactory: TableOfContentsParserFactory
    val opfDocumentHandler: OpfDocumentHandler
    val epubCoverHandler: EpubCoverHandler
    val tocDocumentHandler: TocDocumentHandler
}

internal object ParserModuleProvider : ParserModule {
    override val epubSpineParser: EpubSpineParser by lazy { EpubSpineParser() }
    override val epubMetadataParser: EpubMetadataParser by lazy { EpubMetadataParser() }
    override val epubManifestParser: EpubManifestParser by lazy { EpubManifestParser() }
    override val epubTableOfContentsParserFactory: TableOfContentsParserFactory by lazy {
        TableOfContentsParserFactory()
    }
    override val opfDocumentHandler: OpfDocumentHandler by lazy { OpfDocumentHandler() }
    override val epubDecompressor: EpubDecompressor by lazy { EpubDecompressor() }
    override val epubCoverHandler: EpubCoverHandler by lazy { EpubCoverHandler() }
    override val documentBuilder: DocumentBuilder by lazy {
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder()
    }
    override val tocDocumentHandler: TocDocumentHandler by lazy { TocDocumentHandler() }
}