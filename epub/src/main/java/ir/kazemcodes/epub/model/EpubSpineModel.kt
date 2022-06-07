package ir.kazemcodes.epub.model

/**
 * Model of publication spine defining publication reading order.
 *
 * @property orderedReferences List of elements in reading order
 */
data class EpubSpineModel(val orderedReferences: List<EbupSpineReferenceModel>? = null)

/**
 * Model of spine element.
 *
 * @property idReference Manifest id of element.
 * @property linear Specifies if element content is primary.
 */
data class EbupSpineReferenceModel(val idReference: String, val linear: Boolean = false)
