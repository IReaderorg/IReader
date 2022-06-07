package org.ireader.domain.use_cases.epub.epup_parser.internal.document.toc

import org.ireader.domain.use_cases.epub.epup_parser.model.EpubManifestModel

internal class Epub3TocLocationFinder {

    fun findNcxLocation(epubManifestModel: EpubManifestModel): String? {
        return epubManifestModel
            .resources
            ?.firstOrNull { it.properties?.contains(NAV_PROPERTY) == true }
            ?.href
    }

    private companion object {
        private const val NAV_PROPERTY = "nav"
    }
}