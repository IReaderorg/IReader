package org.ireader.domain.use_cases.remote

import org.ireader.common_extensions.async.withIOContext
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.Chapter
import org.ireader.common_models.entities.toChapterInfo
import org.ireader.common_resources.UiText
import org.ireader.core.exceptions.SourceNotFoundException
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_api.source.model.Text
import org.ireader.core_ui.exceptionHandler
import org.ireader.domain.R
import javax.inject.Inject

class GetRemoteReadingContent @Inject constructor() {
    suspend operator fun invoke(
        chapter: Chapter,
        catalog: CatalogLocal?,
        onError: suspend (message: UiText?) -> Unit,
        onSuccess: suspend (chapter: Chapter) -> Unit,
        commands:CommandList = emptyList()
    ) {
        val source = catalog?.source ?: throw SourceNotFoundException()
        withIOContext {
            kotlin.runCatching {
                try {
                    org.ireader.core_api.log.Log.debug("Timber: GetRemoteReadingContentUseCase was Called")
                    // val page = source.getPageList(chapter.toChapterInfo())
                    val content = mutableListOf<String>()
                    val page = source.getPageList(chapter.toChapterInfo(),commands)

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
                        onSuccess(chapter.copy(content = content.filter { it.isNotBlank() }, dateFetch = org.ireader.common_extensions.currentTimeToLong()))
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
