package ir.kazemcodes.infinity.core.data.repository

import android.content.Context
import android.webkit.WebView
import ir.kazemcodes.infinity.core.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.core.utils.Resource
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
            if (chapter.content.isNullOrEmpty()) {
                var retries = totalRetries
                var success = false
                while (!success || retries < 0) {
                    kotlinx.coroutines.delay(1000)
                    try {
                        remoteUseCase.getRemoteReadingContentUseCase(chapters[index],
                            source = source)
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
                                    is Resource.Loading -> {

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