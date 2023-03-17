package ireader.domain.usecases.epub


expect class ImportEpub {


    suspend fun parse(uri: ireader.domain.models.common.Uri)

    fun getCacheSize() : String
    fun removeCache()
}
