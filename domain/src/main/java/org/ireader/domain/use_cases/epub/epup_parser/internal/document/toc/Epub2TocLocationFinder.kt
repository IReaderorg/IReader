package org.ireader.domain.use_cases.epub.epup_parser.internal.document.toc

import org.ireader.domain.use_cases.epub.epup_parser.internal.extensions.getFirstElementByTagNameNS
import org.ireader.domain.use_cases.epub.epup_parser.model.EpubManifestModel
import org.ireader.domain.use_cases.epub.epup_parser.internal.constants.EpubConstants
import org.w3c.dom.Document

internal class Epub2TocLocationFinder {

    fun findNcxLocation(
        mainOpfDocument: Document?,
        epubManifestModel: EpubManifestModel
    ): String? {

        val ncxResourceId = mainOpfDocument
            ?.getFirstElementByTagNameNS(EpubConstants.OPF_NAMESPACE, SPINE_TAG)
            ?.getAttribute(TOC_ATTR)
        var ncxLocation = epubManifestModel
            .resources
            ?.firstOrNull { it.id == ncxResourceId }
            ?.href

        if (ncxLocation == null) {
            ncxLocation = Epub2TocLocationFinder().fallbackFindNcxPath(epubManifestModel)
        }

        return ncxLocation
    }

    private fun fallbackFindNcxPath(epubManifestModel: EpubManifestModel): String? {
        return epubManifestModel
            .resources
            ?.firstOrNull { NCX_LOCATION_REGEXP.toRegex().matches(it.href.orEmpty()) }
            ?.href
    }

    private companion object {
        private const val SPINE_TAG = "spine"
        private const val TOC_ATTR = "toc"
        private const val NCX_LOCATION_REGEXP = ".*\\.ncx"
    }
}