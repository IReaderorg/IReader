package org.ireader.domain.view_models.settings.webview

import org.ireader.domain.models.source.FetchType

fun mapFetcher(fetcher: Int): FetchType {
    return when (fetcher) {
        FetchType.DetailFetchType.index -> FetchType.DetailFetchType
        FetchType.ContentFetchType.index -> FetchType.ContentFetchType
        FetchType.SearchFetchType.index -> FetchType.SearchFetchType
        FetchType.LatestFetchType.index -> FetchType.LatestFetchType
        FetchType.PopularFetchType.index -> FetchType.PopularFetchType
        FetchType.ChapterFetchType.index -> FetchType.ChapterFetchType
        else -> FetchType.SearchFetchType
    }
}
