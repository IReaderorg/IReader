package ir.kazemcodes.epub.model

/**
 * Main model of parsed .epub publication.
 * Contains all information extracted from decompressed .epub.
 *
 * @property epubOpfFilePath Absolute path to the .opf file.
 * @property epubTocFilePath Absolute path to the .toc file.
 * @property epubCoverImage Model of publication cover image.
 * Contains all information about the publication cover image.
 * @property epubMetadataModel Model of publication metadata.
 * Contains all basic information about the publication.
 * @property epubManifestModel Model of publication manifest.
 * Contains all publication resources.
 * @property epubSpineModel Model of publication spine.
 * Contains list of references in reading order.
 * @property epubTableOfContentsModel Model of publication table of contents.
 */
data class EpubBook(
    val epubOpfFilePath: String? = null,
    val epubTocFilePath: String? = null,
    val epubCoverImage: EpubResourceModel? = null,
    val epubMetadataModel: EpubMetadataModel? = null,
    val epubManifestModel: EpubManifestModel? = null,
    val epubSpineModel: EpubSpineModel? = null,
    val epubTableOfContentsModel: EpubTableOfContentsModel? = null
)