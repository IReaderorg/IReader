package org.ireader.domain.models

sealed class ExploreType(val id: Int) {
    object Latest : ExploreType(0)
    object Popular : ExploreType(1)
    object Search : ExploreType(2)
}