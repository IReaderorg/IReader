package ireader.domain.usecases.local.chapter_usecases

import kotlinx.datetime.Clock
import ireader.common.extensions.currentTimeToLong
import ireader.common.models.entities.Chapter
import ireader.common.models.entities.History
import ireader.core.ui.preferences.UiPreferences
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import org.koin.core.annotation.Factory


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
                    id = chapter.bookId,
                    chapterId = chapter.id,
                    readAt = currentTimeToLong(),
                    readDuration = history?.readDuration ?: 0
                )
            )
        }
    }
}
