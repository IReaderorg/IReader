package ir.kazemcodes.infinity.core.domain.repository

import ir.kazemcodes.infinity.core.data.repository.PreferencesHelper

interface Repository {

    val localBookRepository: LocalBookRepository

    val localChapterRepository: LocalChapterRepository

    val preferencesHelper: PreferencesHelper

    val remoteRepository: RemoteRepository


}