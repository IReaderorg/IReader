package ir.kazemcodes.infinity.data.network

import android.content.Context
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.data.network.sources.FreeWebNovel
import ir.kazemcodes.infinity.data.network.sources.RealWebNovelApi
import ir.kazemcodes.infinity.data.network.sources.WuxiaWorldApi



class Extensions(context: Context) {
    private val sources = listOf<Source>(
        FreeWebNovel(context),
        WuxiaWorldApi(context),
        RealWebNovelApi(context)
    )
    fun getSources() : List<Source>{
        return sources
    }
}