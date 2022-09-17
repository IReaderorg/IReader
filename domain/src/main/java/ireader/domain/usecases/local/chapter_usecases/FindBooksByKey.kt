package ireader.domain.usecases.local.chapter_usecases

import kotlinx.datetime.Clock
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.common.models.entities.Chapter
import ireader.common.models.entities.History
import ireader.domain.preferences.prefs.UiPreferences
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
                    dateFetch = if (updateDateFetched) currentTimeToLong() else chapter.dateFetch
                )
            )

            historyUseCase.insertHistory(
                History(
                    id = 0,
                    chapterId = chapter.id,
                    readAt = currentTimeToLong(),
                    readDuration = history?.readDuration ?: 0,
                )
            )
        }
    }
}
