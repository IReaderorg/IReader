package ireader.domain.usecases.local.chapter_usecases

import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.utils.extensions.currentTimeToLong
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
                    id = history?.id ?: 0,
                    chapterId = chapter.id,
                    readAt = currentTimeToLong(),
                    readDuration = history?.readDuration ?: 0,
                )
            )
        }
    }
}
