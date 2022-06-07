package com.miquido.parsepub.model

/**
 * Epub book metadata model encapsulating all basic information about publication.
 *
 * @property id Element id.
 * @property languages Languages used in book.
 * @property creators Publication creators.
 * @property contributors Publication contributors.
 * @property title Publication title.
 * @property date Date of publication.
 * @property subjects Publication subjects.
 * @property sources Information about a prior resource from which the publication was derived.
 * @property description Publication description
 * @property relation Identifier of an auxiliary resource and its relationship to the publication
 * @property coverage Scope of publication content.
 * @property rights Rights statement or a reference to one.
 * @property publisher Publication publisher.
 * @property epubSpecificationVersion The version number of the epub specification.
 */
data class EpubMetadataModel(
    val id: String? = null,
    val languages: List<String>? = null,
    val creators: List<String>? = null,
    val contributors: List<String>? = null,
    val title: String? = null,
    val date: String? = null,
    val subjects: List<String>? = null,
    val sources: List<String>? = null,
    val description: String? = null,
    val relation: String? = null,
    val coverage: String? = null,
    val rights: String? = null,
    val publisher: String? = null,
    val epubSpecificationVersion: String? = null
) {

    internal fun getEpubSpecificationMajorVersion() = epubSpecificationVersion
            ?.let { Integer.parseInt(it[0].toString()) }
}