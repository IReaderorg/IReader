package org.ireader.domain.use_cases.remote

import org.ireader.core.utils.UiText
import org.ireader.core.utils.currentTimeToLong
import org.ireader.core.utils.exceptionHandler
import org.ireader.domain.R
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapterInfo
import tachiyomi.source.Source
import tachiyomi.source.model.Text
import timber.log.Timber
import javax.inject.Inject

class GetRemoteReadingContent @Inject constructor() {
    suspend operator fun invoke(
        chapter: Chapter,
        source: Source,
        onError: suspend (message: UiText?) -> Unit,
        onSuccess: suspend (chapter: Chapter) -> Unit,
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
                onSuccess(chapter.copy(content = content, dateFetch = currentTimeToLong()))

            }
        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }

    }
}
