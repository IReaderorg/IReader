package ir.kazemcodes.infinity.domain.repository

import ir.kazemcodes.infinity.data.repository.PreferencesHelper

interface Repository {

    val localBookRepository: LocalBookRepository

    val localChapterRepository: LocalChapterRepository

    val preferencesHelper: PreferencesHelper

    val remoteRepository: RemoteRepository


}