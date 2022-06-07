package ir.kazemcodes.epub.internal.parser

import ir.kazemcodes.epub.epubvalidator.ValidationListeners
import ir.kazemcodes.epub.internal.constants.EpubConstants.OPF_NAMESPACE
import ir.kazemcodes.epub.internal.extensions.getFirstElementByTagNameNS
import ir.kazemcodes.epub.internal.extensions.getTagTextContentsFromDcElementOrEmpty
import ir.kazemcodes.epub.internal.extensions.getTagTextContentsFromDcElementsOrEmpty
import ir.kazemcodes.epub.internal.extensions.orValidationError
import ir.kazemcodes.epub.model.EpubMetadataModel
import org.w3c.dom.Document

internal class EpubMetadataParser {

    fun parse(
        opfDocument: Document,
        validationListeners: ValidationListeners?
    ): EpubMetadataModel {

        val epubSpecVersion = opfDocument.documentElement.getAttribute(VERSION_ATTR)
        val metadataElement = opfDocument.getFirstElementByTagNameNS(OPF_NAMESPACE, METADATA_TAG)
            .orValidationError { validationListeners?.onMetadataMissing() }


        return EpubMetadataModel(
            creators = metadataElement.getTagTextContentsFromDcElementsOrEmpty(CREATOR_TAG),
            languages = metadataElement.getTagTextContentsFromDcElementsOrEmpty(LANGUAGE_TAG)
                .orValidationError { validationListeners?.onAttributeMissing(METADATA_TAG, LANGUAGE_TAG) },
            contributors = metadataElement.getTagTextContentsFromDcElementsOrEmpty(CONTRIBUTOR_TAG),
            title = metadataElement.getTagTextContentsFromDcElementOrEmpty(TITLE_TAG)
                .orValidationError { validationListeners?.onAttributeMissing(METADATA_TAG, TITLE_TAG) },
            subjects = metadataElement.getTagTextContentsFromDcElementsOrEmpty(SUBJECT_TAG),
            sources = metadataElement.getTagTextContentsFromDcElementsOrEmpty(SOURCE_TAG),
            description = metadataElement.getTagTextContentsFromDcElementOrEmpty(DESCRIPTION_TAG),
            rights = metadataElement.getTagTextContentsFromDcElementOrEmpty(RIGHTS_TAG),
            coverage = metadataElement.getTagTextContentsFromDcElementOrEmpty(COVERAGE_TAG),
            relation = metadataElement.getTagTextContentsFromDcElementOrEmpty(RELATION_TAG),
            publisher = metadataElement.getTagTextContentsFromDcElementOrEmpty(PUBLISHER_TAG),
            date = metadataElement.getTagTextContentsFromDcElementOrEmpty(DATE_TAG),
            id = metadataElement.getTagTextContentsFromDcElementOrEmpty(ID_TAG)
                .orValidationError { validationListeners?.onAttributeMissing(METADATA_TAG, ID_TAG) },
            epubSpecificationVersion = epubSpecVersion
        )
    }

    private companion object {
        private const val METADATA_TAG = "metadata"
        private const val CREATOR_TAG = "creator"
        private const val CONTRIBUTOR_TAG = "contributor"
        private const val LANGUAGE_TAG = "language"
        private const val TITLE_TAG = "title"
        private const val SUBJECT_TAG = "subject"
        private const val SOURCE_TAG = "source"
        private const val DESCRIPTION_TAG = "description"
        private const val RIGHTS_TAG = "rights"
        private const val COVERAGE_TAG = "coverage"
        private const val RELATION_TAG = "relation"
        private const val PUBLISHER_TAG = "publisher"
        private const val DATE_TAG = "date"
        private const val ID_TAG = "identifier"
        private const val VERSION_ATTR = "version"
    }
}