package ir.kazemcodes.infinity.data.repository

import android.content.Context
import android.webkit.WebView
import ir.kazemcodes.infinity.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.domain.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class RemoteRepositoryImpl(
    private val chapterDao: ChapterDao,
    private val remoteUseCase: RemoteUseCase,
) : RemoteRepository {


    override suspend fun downloadChapter(
        book: Book,
        source: Source,
        chapters: List<Chapter>,
        factory: (Context) -> WebView,
        totalRetries: Int,
    ): Flow<Chapter> = flow {

        chapters.forEachIndexed { index, chapter ->
            if (chapter.content == null) {
                var retries = totalRetries
                var success = false
                while (!success || retries < 0) {
                    kotlinx.coroutines.delay(1000)
                    try {
                        remoteUseCase.getRemoteReadingContentUseCase(chapters[index], source = source)
                            .collect { result ->
                                when (result) {
                                    is Resource.Success -> {
                                        if (result.data != null) {
                                            emit(chapter.copy(content = result.data.content))
                                            success = true
                                        } else {
                                            success = false
                                        }
                                    }
                                    is Resource.Error -> {
                                        success = false
                                    }
                                    else -> {
                                        success = false
                                    }
                                }
                            }


                    } catch (e: Exception) {
                        if (--retries < 0) throw  e
                    }
                }
            }
        }
    }
}