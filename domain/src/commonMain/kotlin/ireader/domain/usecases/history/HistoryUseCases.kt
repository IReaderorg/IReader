package ireader.domain.usecases.history

/**
 * Aggregate class for all history-related use cases
 * Provides a single point of access for history operations
 */
data class HistoryUseCases(
    val getHistory: GetHistoryUseCase,
    val getLastReadNovel: GetLastReadNovelUseCase,
    val updateHistory: UpdateHistoryUseCase,
    val deleteHistory: DeleteHistoryUseCase,
    val clearHistory: ClearHistoryUseCase
)
