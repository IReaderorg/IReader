package ireader.domain.use_cases.local.chapter_usecases

import ireader.common.data.repository.ChapterRepository
import kotlinx.datetime.Clock
import ireader.common.extensions.currentTimeToLong
import ireader.common.models.entities.Chapter
import ireader.common.models.entities.History
import ireader.core.ui.preferences.UiPreferences
import ireader.domain.use_cases.history.HistoryUseCase
import ireader.domain.use_cases.local.LocalInsertUseCases
import org.koin.core.annotation.Factory

@Factory
class FindChaptersByKey(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(key: String): List<Chapter> {
        return chapterRepository.findChaptersByKey(key)
    }
}
@Factory
class FindChapterByKey(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(key: String): Chapter? {
        return chapterRepository.findChapterByKey(key)
    }
}
@Factory
class UpdateLastReadTime(
    private val insertUseCases: LocalInsertUseCases,
    private val historyUseCase: HistoryUseCase,
    private val uiPreferences: UiPreferences
) {
    suspend operator fun invoke(chapter: Chapter, updateDateFetched: Boolean = false) {

        if (!uiPreferences.incognitoMode().read()) {
            val history = historyUseCase.findHistory(chapter.id)
            insertUseCases.insertChapter(
                chapter = chapter.copy(
                    read = true,
                    dateFetch = if (updateDateFetched) Clock.System.now()
                        .toEpochMilliseconds() else chapter.dateFetch
                )
            )

            historyUseCase.insertHistory(
                History(
                    bookId = chapter.bookId,
                    chapterId = chapter.id,
                    readAt = currentTimeToLong(),
                    progress = history?.progress ?: 0
                )
            )
        }
    }
}
