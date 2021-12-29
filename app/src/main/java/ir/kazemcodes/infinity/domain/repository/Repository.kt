package ir.kazemcodes.infinity.domain.repository

interface Repository {

    val localBookRepository: LocalBookRepository

    val localChapterRepository: LocalChapterRepository

    val dataStoreRepository: DataStoreHelper

    val remoteRepository : RemoteRepository

}