package ireader.domain.usecases.remote

import ireader.core.source.model.CommandList
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.toChapterInfo
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GetRemoteReadingContent() {
    suspend operator fun invoke(
        chapter: Chapter,
        catalog: CatalogLocal?,
        onError: suspend (message: UiText?) -> Unit,
        onSuccess: suspend (chapter: Chapter) -> Unit,
        commands: CommandList = emptyList()
    ) {
        val source = catalog?.source ?: throw SourceNotFoundException()
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                try {
                    ireader.core.log.Log.debug("Timber: GetRemoteReadingContentUseCase was Called")

                    val page = source.getPageList(chapter.toChapterInfo(), commands)

                    if (page.isEmpty()) {
                        onError(UiText.MStringResource() { xml ->
                            xml.cantGetContent
                        })
                    } else {
                        ireader.core.log.Log.debug("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                        onSuccess(
                            chapter.copy(
                                content = page,
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
