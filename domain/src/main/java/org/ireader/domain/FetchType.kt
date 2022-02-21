package org.ireader.domain

sealed class FetchType(val index: Int) {
    object LatestFetchType : FetchType(0)
    object PopularFetchType : FetchType(1)
    object SearchFetchType : FetchType(2)
    object DetailFetchType : FetchType(3)
    object ContentFetchType : FetchType(4)
    object ChapterFetchType : FetchType(5)
}
