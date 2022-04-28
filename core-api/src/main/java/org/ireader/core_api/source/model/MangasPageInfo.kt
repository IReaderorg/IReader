

package org.ireader.core_api.source.model

data class MangasPageInfo(
    val mangas: List<MangaInfo>,
    val hasNextPage: Boolean
)
