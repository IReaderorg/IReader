package ir.kazemcodes.infinity.api_feature.network

import ir.kazemcodes.infinity.domain.network.apis.FreeWebNovel
import ir.kazemcodes.infinity.domain.network.models.ParsedHttpSource

val apis = listOf<ParsedHttpSource>(
    FreeWebNovel()
)