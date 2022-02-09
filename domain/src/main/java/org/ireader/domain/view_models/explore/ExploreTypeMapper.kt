package org.ireader.domain.view_models.explore

import org.ireader.domain.models.ExploreType

fun exploreTypeMapper(exploreTypeId: Int): ExploreType {
    return when (exploreTypeId) {
        ExploreType.Latest.id -> ExploreType.Latest
        else -> ExploreType.Popular
    }
}