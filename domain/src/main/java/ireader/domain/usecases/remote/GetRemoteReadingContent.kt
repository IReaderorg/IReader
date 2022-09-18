package ireader.domain.usecases.remote

import ireader.domain.utils.extensions.async.withIOContext
import ireader.common.models.entities.CatalogLocal
import ireader.common.models.entities.Chapter
import ireader.common.models.entities.toChapterInfo
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import ireader.core.source.model.CommandList
import ireader.core.source.model.ImageBase64
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.R
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.async.withIOContext
import ireader.domain.utils.extensions.currentTimeToLong
import org.koin.core.annotation.Factory

@Factory
class GetRemoteReadingContent() {
    suspend operator fun invoke(
        chapter: Chapter,
        catalog: CatalogLocal?,
        onError: suspend (message: UiText?) -> Unit,
        onSuccess: suspend (chapter: Chapter) -> Unit,
        commands: CommandList = emptyList()
    ) {
        val source = catalog?.source ?: throw SourceNotFoundException()
        withIOContext {
            kotlin.runCatching {
                try {
                    ireader.core.log.Log.debug("Timber: GetRemoteReadingContentUseCase was Called")
                    // val page = source.getPageList(chapter.toChapterInfo())
                    val content = mutableListOf<Page>()
                    val page = source.getPageList(chapter.toChapterInfo(), commands)

                    page.forEach { _page ->
                        when (_page) {
                            is Text -> {
                                if (_page.text.isNotBlank()) {
                                    content.add(_page)
                                }
                            }
                            is ImageBase64 -> {
                                if (_page.data.isNotBlank()) {
                                    content.add(_page)
                                }
                            }
                            is ImageUrl -> {
                                if (_page.url.isNotBlank()) {
                                    content.add(_page)
                                }
                            }
                            else -> {}
                        }
                    }

                    if (content.joinToString().isBlank()) {
                        onError(UiText.StringResource(R.string.cant_get_content))
                    } else {
                        ireader.core.log.Log.debug("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                        onSuccess(
                            chapter.copy(
                                content = content,
                                dateFetch = currentTimeToLong()
                            )
                        )
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
