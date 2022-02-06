package ir.kazemcodes.infinity.feature_explore.presentation.browse

fun exploreTypeMapper(exploreTypeId: Int) : ExploreType {
    return when (exploreTypeId) {
        ExploreType.Latest.id -> ExploreType.Latest
        else -> ExploreType.Popular
    }
}