package org.ireader.domain.use_cases.remote

import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.domain.R
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapterInfo
import org.ireader.domain.repository.RemoteRepository
import org.ireader.domain.utils.Resource
import tachiyomi.source.Source
import tachiyomi.source.model.Text
import timber.log.Timber
import javax.inject.Inject

class GetRemoteReadingContent @Inject constructor(private val remoteRepository: RemoteRepository) {
    suspend operator fun invoke(
        chapter: Chapter,
        source: Source,
        onError: suspend (message: UiText?) -> Unit,
        onSuccess: suspend (content: List<String>) -> Unit,
    ) {
        try {
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            // val page = source.getPageList(chapter.toChapterInfo())
            val content = mutableListOf<String>()
            source.getPageList(chapter.toChapterInfo())
                .forEach {
                    when (it) {
                        is Text -> {
                            content.add(it.text)
                        }
                        else -> {}
                    }
                }

            if (content.joinToString().isBlank()) {
                onError(UiText.StringResource(R.string.cant_get_content))

            } else {
                Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                onSuccess(content)

            }
        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }

    }
}


interface ResourceCallBack<T> {
    fun onSuccess(): Resource<T>

    fun onFailure(): Resource<T>
}