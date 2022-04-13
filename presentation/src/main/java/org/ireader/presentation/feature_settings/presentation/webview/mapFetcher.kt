package org.ireader.presentation.feature_settings.presentation.webview

import org.ireader.domain.FetchType


fun mapFetcher(fetcher: Int): FetchType {
    return when (fetcher) {
        FetchType.DetailFetchType.index -> FetchType.DetailFetchType
        FetchType.ContentFetchType.index -> FetchType.ContentFetchType
        FetchType.SearchFetchType.index -> FetchType.SearchFetchType
        FetchType.LatestFetchType.index -> FetchType.LatestFetchType
        FetchType.PopularFetchType.index -> FetchType.PopularFetchType
        FetchType.ChaptersFetchType.index -> FetchType.ChaptersFetchType
        else -> FetchType.SearchFetchType
    }
}
