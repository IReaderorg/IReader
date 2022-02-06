package ir.kazemcodes.infinity.feature_settings.presentation.webview

import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType

fun mapFetcher(fetcher: Int): FetchType {
        return when (fetcher) {
            FetchType.Detail.index -> FetchType.Detail
            FetchType.Content.index -> FetchType.Content
            FetchType.Search.index -> FetchType.Search
            FetchType.Latest.index -> FetchType.Latest
            FetchType.Popular.index -> FetchType.Popular
            FetchType.Chapter.index -> FetchType.Chapter
            else -> FetchType.Search
        }
    }
