package org.ireader.domain.use_cases.remote

import org.ireader.common_extensions.async.withIOContext
import org.ireader.common_models.entities.Chapter
import org.ireader.common_models.entities.toChapterInfo
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.Text
import org.ireader.core_ui.exceptionHandler
import org.ireader.domain.R
import javax.inject.Inject

class GetRemoteReadingContent @Inject constructor() {
    suspend operator fun invoke(
        chapter: Chapter,
        source: Source,
        onError: suspend (message: org.ireader.common_extensions.UiText?) -> Unit,
        onSuccess: suspend (chapter: Chapter) -> Unit,
    ) {
        withIOContext {
            kotlin.runCatching {
                try {
                    org.ireader.core_api.log.Log.debug("Timber: GetRemoteReadingContentUseCase was Called")
                    // val page = source.getPageList(chapter.toChapterInfo())
                    val content = mutableListOf<String>()
                    val page = source.getPageList(chapter.toChapterInfo())

                    page.forEach {
                        when (it) {
                            is Text -> {
                                content.add(it.text)
                            }
                            else -> {}
                        }
                    }

                    if (content.joinToString().isBlank()) {
                        onError(org.ireader.common_extensions.UiText.StringResource(R.string.cant_get_content))
                    } else {
                        org.ireader.core_api.log.Log.debug("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                        onSuccess(chapter.copy(content = content, dateFetch = org.ireader.common_extensions.currentTimeToLong()))
                    }
                } catch (e: Throwable) {
                    onError(exceptionHandler(e))
                }
            }.getOrElse { e ->
                onError(exceptionHandler(e))
            }
        }
    }
}
