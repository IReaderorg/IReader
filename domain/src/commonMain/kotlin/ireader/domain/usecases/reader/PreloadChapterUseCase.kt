package ireader.domain.usecases.reader
import ireader.domain.utils.extensions.ioDispatcher

import ireader.core.source.model.CommandList
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.toChapterInfo
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for preloading chapter content in the background
 * to enable instant reading when user navigates to next chapter
 */
class PreloadChapterUseCase {
    
    suspend operator fun invoke(
        chapter: Chapter,
        catalog: CatalogLocal?,
        onSuccess: suspend (chapter: Chapter) -> Unit = {},
        onError: suspend (message: UiText?) -> Unit = {},
        commands: CommandList = emptyList()
    ): Result<Chapter> {
        val source = catalog?.source ?: return Result.failure(SourceNotFoundException())
        
        return withContext(ioDispatcher) {
            kotlin.runCatching {
                ireader.core.log.Log.debug("PreloadChapterUseCase: Starting preload for chapter ${chapter.name}")
                
                val page = source.getPageList(chapter.toChapterInfo(), commands)
                
                if (page.isEmpty()) {
                    onError(UiText.MStringResource(Res.string.cant_get_content))
                    Result.failure(Exception("Empty content"))
                } else {
                    val preloadedChapter = chapter.copy(
                        content = page,
                        dateFetch = currentTimeToLong()
                    )
                    
                    ireader.core.log.Log.debug("PreloadChapterUseCase: Successfully preloaded chapter ${chapter.name}")
                    onSuccess(preloadedChapter)
                    Result.success(preloadedChapter)
                }
            }.getOrElse { e ->
                ireader.core.log.Log.error("PreloadChapterUseCase: Failed to preload chapter ${chapter.name}", e)
                onError(exceptionHandler(e))
                Result.failure(e)
            }
        }
    }
}
