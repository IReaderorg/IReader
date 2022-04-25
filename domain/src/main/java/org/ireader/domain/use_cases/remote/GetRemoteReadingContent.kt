package org.ireader.domain.use_cases.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ireader.core.extensions.withIOContext
import org.ireader.core.utils.UiText
import org.ireader.core.utils.currentTimeToLong
import org.ireader.core.utils.exceptionHandler
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.Text
import org.ireader.domain.R
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapterInfo
import javax.inject.Inject

class GetRemoteReadingContent @Inject constructor(@ApplicationContext private val context: Context)  {
    suspend operator fun invoke(
        chapter: Chapter,
        source: Source,
        onError: suspend (message: UiText?) -> Unit,
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
                        onError(UiText.StringResource(R.string.cant_get_content))

                    } else {
                        org.ireader.core_api.log.Log.debug("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                        onSuccess(chapter.copy(content = content, dateFetch = currentTimeToLong()))

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