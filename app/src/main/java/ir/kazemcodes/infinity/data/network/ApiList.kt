package ir.kazemcodes.infinity.data.network

import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.data.network.sources.FreeWebNovel
import ir.kazemcodes.infinity.data.network.sources.RealWebNovelApi

val sources = listOf<Source>(
    FreeWebNovel(),
//    WuxiaWorldApi(),
    RealWebNovelApi()
)