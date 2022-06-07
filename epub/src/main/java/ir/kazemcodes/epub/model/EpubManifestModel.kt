package ir.kazemcodes.epub.model

/**
 * Epub manifest model. Contains exhaustive list of all publication resources.
 *
 * @property resources List of all resources available in publication.
 */
data class EpubManifestModel(val resources: List<EpubResourceModel>?)

/**
 * Model of single publication resource.
 *
 * @property id Id of the resource
 * @property href Location of the resource
 * @property mediaType Type and format of the resource
 * @property properties Set of property values
 */
data class EpubResourceModel(
    val id: String? = null,
    val href: String? = null,
    val mediaType: String? = null,
    val properties: HashSet<String>? = null,
    val byteArray: ByteArray?=null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EpubResourceModel

        if (id != other.id) return false
        if (href != other.href) return false
        if (mediaType != other.mediaType) return false
        if (properties != other.properties) return false
        if (byteArray != null) {
            if (other.byteArray == null) return false
            if (!byteArray.contentEquals(other.byteArray)) return false
        } else if (other.byteArray != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (href?.hashCode() ?: 0)
        result = 31 * result + (mediaType?.hashCode() ?: 0)
        result = 31 * result + (properties?.hashCode() ?: 0)
        result = 31 * result + (byteArray?.contentHashCode() ?: 0)
        return result
    }
}
