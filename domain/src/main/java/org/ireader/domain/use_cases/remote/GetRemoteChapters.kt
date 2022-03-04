package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Book.Companion.toBookInfo
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapter
import org.ireader.domain.repository.RemoteRepository
import org.ireader.domain.utils.Resource
import org.jsoup.select.Selector
import retrofit2.HttpException
import tachiyomi.source.Source
import timber.log.Timber
import java.io.IOException

class GetRemoteChapters(private val remoteRepository: RemoteRepository) {
    operator fun invoke(book: Book, source: Source): Flow<Resource<List<Chapter>>> =
        flow {
            try {
                Timber.d("Timber: GetRemoteChaptersUseCase was Called")
                val chapters = source.getChapterList(manga = book.toBookInfo(source.id))
                emit(Resource.Success<List<Chapter>>(chapters.map { it.toChapter(book.id) }))
                Timber.d("Timber: GetRemoteChaptersUseCase was Finished Successfully")

            } catch (e: HttpException) {
                emit(
                    Resource.Error<List<Chapter>>(
                        uiText = UiText.ExceptionString(e)
                    )
                )
            } catch (e: IOException) {
                emit(Resource.Error<List<Chapter>>(uiText = UiText.StringResource(R.string.noInternetError)))
            } catch (e: Selector.SelectorParseException) {
                emit(Resource.Error<List<Chapter>>(uiText = UiText.StringResource(R.string.cant_get_content)))
            } catch (e: Exception) {
                emit(Resource.Error<List<Chapter>>(uiText = UiText.ExceptionString(e)))
            }
        }
}