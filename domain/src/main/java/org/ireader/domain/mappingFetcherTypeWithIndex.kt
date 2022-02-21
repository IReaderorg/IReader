package org.ireader.domain

fun mappingFetcherTypeWithIndex(index: Int): FetchType {
    return when (index) {
        FetchType.LatestFetchType.index -> FetchType.LatestFetchType
        FetchType.PopularFetchType.index -> FetchType.PopularFetchType
        FetchType.SearchFetchType.index -> FetchType.SearchFetchType
        FetchType.DetailFetchType.index -> FetchType.DetailFetchType
        FetchType.ChapterFetchType.index -> FetchType.ChapterFetchType
        FetchType.ContentFetchType.index -> FetchType.ContentFetchType
        else -> FetchType.LatestFetchType
    }
}
