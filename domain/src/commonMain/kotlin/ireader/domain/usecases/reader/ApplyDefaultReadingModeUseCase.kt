package ireader.domain.usecases.reader

import ireader.domain.data.repository.BookRepository
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.ReadingMode

/**
 * Use case to apply default reading mode to newly opened books
 */
class ApplyDefaultReadingModeUseCase(
    private val readerPreferences: ReaderPreferences,
    private val bookRepository: BookRepository
) {
    /**
     * Apply default reading mode if this is the first time opening the book
     * @param bookId The ID of the book being opened
     * @param isFirstOpen Whether this is the first time opening this book
     */
    suspend operator fun invoke(bookId: Long, isFirstOpen: Boolean) {
        if (isFirstOpen) {
            val defaultMode = readerPreferences.defaultReadingMode().get()
            readerPreferences.readingMode().set(defaultMode)
        }
    }

    /**
     * Get the default reading mode
     */
    fun getDefaultMode(): ReadingMode {
        return readerPreferences.defaultReadingMode().get()
    }

    /**
     * Set the default reading mode
     */
    fun setDefaultMode(mode: ReadingMode) {
        readerPreferences.defaultReadingMode().set(mode)
    }
}
