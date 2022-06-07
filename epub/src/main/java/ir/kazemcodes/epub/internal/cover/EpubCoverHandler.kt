package ir.kazemcodes.epub.internal.cover

import ir.kazemcodes.epub.model.EpubManifestModel
import ir.kazemcodes.epub.model.EpubResourceModel

internal class EpubCoverHandler {

    fun getCoverImageFromManifest(manifestModel: EpubManifestModel): EpubResourceModel? {

        var coverImage = manifestModel
            .resources
            ?.firstOrNull { it.properties?.contains(COVER_IMAGE_ID_NAME) == true }

        if (coverImage == null) {
            coverImage = manifestModel
                .resources
                ?.filter { it.id?.contains(COVER_RESOURCE_VALUE, ignoreCase = true) == true }
                ?.firstOrNull {
                    it.mediaType?.contains(IMAGE_LABEL) == true
                }
        }
        return coverImage
    }

    companion object {
        private const val COVER_IMAGE_ID_NAME = "cover-image"
        private const val COVER_RESOURCE_VALUE = "cover"
        private const val IMAGE_LABEL = "image"
    }
}
