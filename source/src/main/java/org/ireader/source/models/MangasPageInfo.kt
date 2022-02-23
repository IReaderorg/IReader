package org.ireader.source.models


data class MangasPageInfo(
    val mangas: List<MangaInfo> = emptyList(),
    val hasNextPage: Boolean = false,
)

